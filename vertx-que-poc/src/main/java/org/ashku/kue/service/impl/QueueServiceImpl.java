package org.ashku.kue.service.impl;

import com.google.common.base.Preconditions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.CompletableHelper;
import org.ashku.kue.service.QueueService;
import org.ashku.kue.store.RedisDataStore;

import static org.ashku.kue.Constants.USER_ID_SNAKE;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class QueueServiceImpl implements QueueService {

    private final RedisDataStore redisDataStore;

    public QueueServiceImpl(RedisDataStore redisDataStore) {

        this.redisDataStore = redisDataStore;
    }

    public QueueService findAll(Handler<AsyncResult<JsonArray>> handler) {

        redisDataStore.findAll()
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(handler));

        return this;
    }

    public QueueService addToQueue(JsonObject request, Handler<AsyncResult<Void>> handler) {

        String userId = request.getString(USER_ID_SNAKE);

        Preconditions.checkNotNull(userId, "User id should be provided");

        redisDataStore.add(System.currentTimeMillis(), userId)
                .toCompletable()
                .subscribe(CompletableHelper.toObserver(handler));

        return this;
    }
}
