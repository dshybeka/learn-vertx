package org.ashku.kue.store;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.redis.RedisClient;

import java.util.List;
import java.util.stream.Collectors;

import static org.ashku.kue.Constants.SCORE;
import static org.ashku.kue.Constants.USER_ID;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class RedisDataStore {

    public static final String DEFAULT_QUEUE = "default.queue";

    private final RedisClient redisClient;

    public RedisDataStore(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public Single<List<JsonObject>> findAll() {

        Single<JsonArray> data = redisClient.rxZrange(DEFAULT_QUEUE, 0, Long.MAX_VALUE);

        Single<List<JsonObject>> result = data.map(row -> {

            return row.stream().map(rawData -> new JsonObject(rawData.toString()))
                    .collect(Collectors.toList());
        });

        return result;
    }

    public Single<Void> addToQueue(long score, String userId) {

        return redisClient.rxZadd(DEFAULT_QUEUE, score, new JsonObject().put(USER_ID, userId).put(SCORE, score).toString())
                .map(r -> (Void) null);
    }
}
