package org.ashku.kue.store;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(RedisDataStore.class);

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

    public Single<List<JsonObject>> findAllDefault() {

        Single<JsonArray> data = redisClient.rxZrangebyscore(DEFAULT_QUEUE, String.valueOf(0), String.valueOf(Double.MAX_VALUE), RangeLimitOptions.NONE);

        Single<List<JsonObject>> result = data.map(row -> {

            List<JsonObject> collected = row.stream().map(rawData -> new JsonObject(rawData.toString()))
                    .collect(Collectors.toList());

            return collected;
        });

        return result;
    }

    public Single<List<JsonObject>> findFromDefaultQueueLimit(Long limitInclusive) {

        Single<JsonArray> data = redisClient.rxZrange(DEFAULT_QUEUE, 0, limitInclusive-1);

        Single<List<JsonObject>> result = data.map(row -> {

            List<JsonObject> collected = row.stream().map(rawData -> new JsonObject(rawData.toString()))
                    .collect(Collectors.toList());

            return collected;
        });

        return result;
    }

    public Single<List<JsonObject>> findAllProcess() {

        Single<JsonArray> data = redisClient.rxZrangebyscore(PROCESS_QUEUE, String.valueOf(0), String.valueOf(Double.MAX_VALUE), RangeLimitOptions.NONE);

        Single<List<JsonObject>> result = data.map(row -> {

            List<JsonObject> collected = row.stream().map(rawData -> new JsonObject(rawData.toString()))
                    .collect(Collectors.toList());

            return collected;
        });

        return result;
    }

    public Single<JsonObject> addToDefaultQueue(long score, String userId) {

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

    public Single<JsonObject> addToProcessOrDefaultQueue(long score, String userId) {

        return countProcessQueue()
                .flatMap(count -> {

                    if (count >= processQueueSize) {

                        LOG.info("Adding to default queue " + count);

                        return addToDefaultQueue(score, userId);

                    } else {

                        LOG.info("Adding to processing queue " + count);

                        return addToProcessingQueue(score, userId);
                    }
                });
    }

    public Single<JsonObject> addToProcessingQueue(long score, String userId) {

        return redisClient.rxZadd(PROCESS_QUEUE, score, new JsonObject().put(USER_ID, userId).put(SCORE, score).toString())
                .map(r -> new JsonObject().put(SCORE, score).put(USER_ID_SNAKE, userId));
    }

    public Single<Long> countProcessQueue() {

        return count(PROCESS_QUEUE);
    }

    public Single<Long> count(String queueName) {

        return redisClient.rxZcard(queueName);
    }

    public Single<Long> removeFromProcessQueueOlderThan(Long oldestRecordTime) {

        return redisClient.rxZremrangebyscore(PROCESS_QUEUE, String.valueOf(0), String.valueOf(oldestRecordTime));
    }

    public Single<Boolean> moveToProcessQueue(List<JsonObject> jsonObjects) {

        RedisTransaction transaction = redisClient.transaction();

        return transaction.rxMulti()
                .flatMap(r -> {

                    List<Single<Boolean>> removedAnAdded = jsonObjects.stream().map(jsonObject -> {

                        Single<String> removed = transaction.rxZremrangebyscore(DEFAULT_QUEUE, jsonObject.getLong(SCORE).toString(), jsonObject.getLong(SCORE).toString());
                        Single<String> added = transaction.rxZadd(PROCESS_QUEUE, jsonObject.getLong(SCORE), jsonObject.toString());

                        return Single.zip(removed, added, (rm, a) -> true);
                    }).collect(Collectors.toList());

                    removedAnAdded.add(transaction.rxExec().map(value -> true));

                    return Single.zip(removedAnAdded, (value) -> true);
                });
    }
}
