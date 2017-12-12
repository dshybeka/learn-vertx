package org.ashku.kue.service.impl;

import com.google.common.base.Preconditions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;
import org.ashku.kue.service.QueueService;
import org.ashku.kue.store.RedisDataStore;

import java.util.List;

import static org.ashku.kue.Constants.USER_ID_SNAKE;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class QueueServiceImpl implements QueueService {

    private final RedisDataStore redisDataStore;

    public QueueServiceImpl(RedisDataStore redisDataStore) {

        this.redisDataStore = redisDataStore;
    }

    public QueueService findAllDefault(Handler<AsyncResult<List<JsonObject>>> handler) {

        redisDataStore.findAllDefault()
                .map(found -> found)
                .subscribe(SingleHelper.toObserver(handler));

        return this;
    }

    @Override
    public QueueService findAllProcess(Handler<AsyncResult<List<JsonObject>>> handler) {

        redisDataStore.findAllProcess()
                .map(found -> found)
                .subscribe(SingleHelper.toObserver(handler));

        return this;
    }

    public QueueService addToQueue(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

        String userId = request.getString(USER_ID_SNAKE);

        Preconditions.checkNotNull(userId, "User id should be provided");

        redisDataStore.addToProcessOrDefaultQueue(System.currentTimeMillis(), userId)
                .subscribe(SingleHelper.toObserver(handler));

        return this;
    }

    public QueueService moveToProcessQueue(Long limit, Handler<AsyncResult<Boolean>> handler) {

        redisDataStore.findFromDefaultQueueLimit(limit)
                .flatMap(rowsToMove -> {

                    return redisDataStore.moveToProcessQueue(rowsToMove);
                })
        .subscribe(SingleHelper.toObserver(handler));

        return this;
    }

    @Override
    public QueueService countProcessQueue(Handler<AsyncResult<Long>> handler) {

        redisDataStore.countProcessQueue()
                .subscribe(SingleHelper.toObserver(handler));

        return this;
    }

    @Override
    public QueueService deleteFromProcessQueue(Long oldestRecordTime, Handler<AsyncResult<Long>> handler) {

        redisDataStore.removeFromProcessQueueOlderThan(oldestRecordTime)
                .subscribe(SingleHelper.toObserver(handler));

        return this;
    }
}
