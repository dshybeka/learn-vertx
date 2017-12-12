package org.ashku.kue.verticle;

import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.ashku.kue.Constants;
import org.ashku.kue.service.QueueService;
import org.ashku.kue.store.RedisDataStore;

import static org.ashku.kue.Constants.Event.STORE_OPERATIONS;
import static org.ashku.kue.Constants.Storage.HOST;
import static org.ashku.kue.Constants.Storage.PORT;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class StorageVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(StorageVerticle.class);

    public static final long DEFAULT_QUEUE_RETENTION_MILLS = 1000L * 60 * 2;
    public static final long DEFAULT_QUEUE_SIZE = 5;

    @Override
    public void start(Future<Void> startFuture) {

        RedisClient redisClient = RedisClient.create(vertx, new RedisOptions()
                .setHost(config().getString(HOST, Constants.Storage.DEFAULT_REDIS_HOST))
                .setPort(config().getInteger(PORT, Constants.Storage.DEFAULT_REDIS_PORT))
        );
        RedisDataStore redisDataStore = new RedisDataStore(redisClient, config().getLong(Constants.Storage.DEFAULT_QUEUE_RETENTION, DEFAULT_QUEUE_RETENTION_MILLS), DEFAULT_QUEUE_SIZE);

        ServiceBinder serviceBinder = new ServiceBinder(vertx.getDelegate());
        serviceBinder.setAddress(STORE_OPERATIONS);

        redisClient.rxPing()
                .subscribe(
                        pingResult -> {

                            serviceBinder.register(QueueService.class, QueueService.create(redisDataStore));

                            LOG.info("Storage initialized");

                            startFuture.complete();
                        },
                        throwable -> {

                            LOG.error("Cannot connect to db ", throwable);

                            startFuture.fail(throwable);
                        });
    }
}
