package org.ashku.kue.verticle;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.ashku.kue.Constants;
import org.ashku.kue.store.RedisDataStore;

import static org.ashku.kue.Constants.*;
import static org.ashku.kue.Constants.Event.STORE_OPERATIONS_ADD;
import static org.ashku.kue.Constants.Event.STORE_OPERATIONS_FIND_ALL;
import static org.ashku.kue.Constants.Storage.HOST;
import static org.ashku.kue.Constants.Storage.PORT;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class StorageVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        RedisClient redisClient = RedisClient.create(vertx, new RedisOptions()
                .setHost(config().getString(HOST, Constants.Storage.DEFAULT_REDIS_HOST))
                .setPort(config().getInteger(PORT, Constants.Storage.DEFAULT_REDIS_PORT))
        );
        redisClient.rxPing()
                .subscribe(
                        pingResult -> {

                            RedisDataStore redisDataStore = new RedisDataStore(redisClient);

                            EventBus eventBus = vertx.eventBus();

                            eventBus.consumer(STORE_OPERATIONS_FIND_ALL, message -> {

                                redisDataStore.findAll()
                                        .subscribe(
                                                result -> message.reply(new JsonObject().put(SUCCESS, true).put(DATA, result)),
                                                throwable -> message.reply(new JsonObject().put(SUCCESS, true).put(DATA, throwable))
                                        );
                            });

                            eventBus.consumer(STORE_OPERATIONS_ADD, message -> {

                                JsonObject request = (JsonObject) message.body();

                                redisDataStore.add(System.currentTimeMillis(), request.getString(USER_ID_SNAKE))
                                        .subscribe(
                                                result -> message.reply(new JsonObject().put(SUCCESS, true).put(DATA, result)),
                                                throwable -> message.reply(new JsonObject().put(SUCCESS, true).put(DATA, throwable.getMessage()))
                                        );
                            });

                            System.out.println("Storage initialized");

                            startFuture.complete();
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            startFuture.fail(throwable);
                        });
    }


}
