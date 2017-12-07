package org.ashku.kue.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.ashku.kue.service.impl.QueueServiceImpl;
import org.ashku.kue.store.RedisDataStore;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
@ProxyGen
@VertxGen
public interface QueueService {

    @GenIgnore
    static QueueService create(RedisDataStore redisDataStore) {

        return new QueueServiceImpl(redisDataStore);
    }

    @Fluent
    QueueService findAll(Handler<AsyncResult<JsonArray>> handler);

    @Fluent
    QueueService addToQueue(JsonObject request, Handler<AsyncResult<Void>> handler);
}
