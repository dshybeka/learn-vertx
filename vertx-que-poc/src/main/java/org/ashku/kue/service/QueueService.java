package org.ashku.kue.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.ashku.kue.service.impl.QueueServiceImpl;
import org.ashku.kue.store.RedisDataStore;

import java.util.List;

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

    @GenIgnore
    static org.ashku.kue.service.reactivex.QueueService createProxy(Vertx vertx, String address) {

        return new org.ashku.kue.service.reactivex.QueueService(new QueueServiceVertxEBProxy(vertx, address));
    }

    @Fluent
    QueueService findAllDefault(Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    QueueService findAllProcess(Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    QueueService addToQueue(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    QueueService deleteFromProcessQueue(Long oldestRecordTime, Handler<AsyncResult<Long>> handler);

    @Fluent
    QueueService moveToProcessQueue(Long limit, Handler<AsyncResult<Boolean>> handler);

    @Fluent
    QueueService countProcessQueue(Handler<AsyncResult<Long>> handler);
}
