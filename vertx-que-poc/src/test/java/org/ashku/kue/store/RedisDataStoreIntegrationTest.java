package org.ashku.kue.store;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.ashku.kue.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.ashku.kue.store.RedisDataStore.DEFAULT_QUEUE;
import static org.ashku.kue.store.RedisDataStore.PROCESS_QUEUE;

/**
 * Created by Dzianis_Shybeka on 12/11/2017
 */
@Ignore
@RunWith(VertxUnitRunner.class)
public class RedisDataStoreIntegrationTest {

    private static final long PROCESS_QUEUE_SIZE = 5L;

    private Vertx vertx;
    private RedisDataStore redisDataStore;
    private RedisClient redisClient;

    @Before
    public void setUp() throws Exception {

        vertx = Vertx.vertx();

        redisClient = RedisClient.create(vertx, new RedisOptions()
                .setHost(Constants.Storage.DEFAULT_REDIS_HOST)
                .setPort(Constants.Storage.DEFAULT_REDIS_PORT)
        );

        redisDataStore = new RedisDataStore(redisClient, 1000L * 120, PROCESS_QUEUE_SIZE);

        Single<Long> deleteDefaultQueue = redisClient.rxDel(DEFAULT_QUEUE);
        Single<Long> deleteProcessQueue = redisClient.rxDel(PROCESS_QUEUE);

        Boolean isDataRemoved = deleteDefaultQueue.zipWith(deleteProcessQueue, (a, b) -> true)
                .blockingGet();

        System.out.println("Data from redis storage has been removed: " + isDataRemoved);
    }

    @Test
    public void addToProcessQueue(TestContext testContext) throws Exception {

        //  given:
        Async async = testContext.async();

        //  when:
        Single<JsonObject> added1 = redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "121");
        Single<JsonObject> added2 = redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "122");
        Single<JsonObject> added3 = redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "123");
        Single<JsonObject> added4 = redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "124");
        Single<JsonObject> added5 = redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "125");

        //  then:
        Single.zip(added1, added2, added3, added4, added5, (a1, a2, a3, a4, a5) -> true)
                .subscribe(
                        (result) -> {

                            testContext.assertNotNull(result);

                            redisDataStore.addToProcessQueueWithCheck(System.currentTimeMillis(), "126")
                                    .subscribe(result2 -> {

                                        Single<TestContext> processedQueueCount = redisDataStore.count(PROCESS_QUEUE)
                                                .map(processedCount -> testContext.assertEquals(5, processedCount.intValue()));

                                        Single<TestContext> defaultQueueCount = redisDataStore.count(DEFAULT_QUEUE)
                                                .map(defaultCount -> testContext.assertEquals(1, defaultCount.intValue()));

                                        processedQueueCount.zipWith(defaultQueueCount, (a, b) -> true)
                                                .subscribe(countsResult -> async.complete());
                                    });
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            testContext.fail();

                            async.complete();
                        }
                );


        async.awaitSuccess(50000);
    }

    @After
    public void tearDown() throws Exception {

        vertx.close();
    }
}