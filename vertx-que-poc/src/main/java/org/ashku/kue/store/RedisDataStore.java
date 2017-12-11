package org.ashku.kue.store;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.reactivex.redis.RedisTransaction;
import io.vertx.redis.op.RangeLimitOptions;

import java.util.List;
import java.util.stream.Collectors;

import static org.ashku.kue.Constants.*;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class RedisDataStore {

    public static final String DEFAULT_QUEUE = "default.queue";
    public static final String PROCESS_QUEUE = "process.queue";

    private final RedisClient redisClient;
    private final Long queueRetentionMills;
    private final Long processQueueSize;

    public RedisDataStore(RedisClient redisClient,
                          Long queueRetentionMills,
                          Long processQueueSize) {

        this.redisClient = redisClient;
        this.queueRetentionMills = queueRetentionMills;
        this.processQueueSize = processQueueSize;
    }

    public Single<List<JsonObject>> findAll() {

        Single<JsonArray> data = redisClient.rxZrangebyscore(DEFAULT_QUEUE, String.valueOf(System.currentTimeMillis() - queueRetentionMills * 10), String.valueOf(System.currentTimeMillis()), RangeLimitOptions.NONE);

        Single<List<JsonObject>> result = data.map(row -> {

            List<JsonObject> collected = row.stream().map(rawData -> new JsonObject(rawData.toString()))
                    .collect(Collectors.toList());

            return collected;
        });

        return result;
    }

    public Single<JsonObject> addToQueue(long score, String userId) {

        long oldestRecordTime = score - queueRetentionMills;

        RedisTransaction transaction = redisClient.transaction();

        return transaction.rxMulti()
                .flatMap(r -> {

                    Single<String> removed = transaction.rxZremrangebyscore(DEFAULT_QUEUE, String.valueOf(0), String.valueOf(oldestRecordTime));
                    Single<String> added = transaction.rxZadd(DEFAULT_QUEUE, score, new JsonObject().put(USER_ID, userId).put(SCORE, score).toString());
                    Single<JsonArray> exec = transaction.rxExec();

                    return Single.zip(removed, added, exec, (first, second, third) -> true);
                }).map(r -> new JsonObject().put(SCORE, score).put(USER_ID_SNAKE, userId));
    }

    public Single<JsonObject> addToProcessQueueWithCheck(long score, String userId) {

        return count(PROCESS_QUEUE)
                .flatMap(count -> {

                    if (count >= processQueueSize) {

                        System.out.println("Adding to default queue " + count);

                        return addToQueue(score, userId);

                    } else {

                        System.out.println("Adding to processing queue " + count);

                        return addToProcessingQueue(score, userId);
                    }
                });
    }

    public Single<JsonObject> addToProcessingQueue(long score, String userId) {

        RedisTransaction transaction = redisClient.transaction();

        return transaction.rxZadd(PROCESS_QUEUE, score, new JsonObject().put(USER_ID, userId).put(SCORE, score).toString())
                .map(r -> new JsonObject().put(SCORE, score).put(USER_ID_SNAKE, userId));
    }

    public Single<Long> count(String queueName) {

        return redisClient.rxZcount(queueName, 0, Double.MAX_VALUE);
    }
}
