package org.test.zokki.service.impl;

import io.reactivex.Single;
import io.vertx.reactivex.redis.RedisClient;
import org.test.zokki.Constants;
import org.test.zokki.model.Todo;
import org.test.zokki.service.TodoService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Dzianis_Shybeka on 12/5/2017
 */
public class TodoServiceRedisImpl implements TodoService {

    private final RedisClient redis;

    public TodoServiceRedisImpl(RedisClient redis) {
        this.redis = redis;
    }

    public Single<Boolean> initData() {

        return redis.rxHset(Constants.REDIS_TODO_KEY, "24",
                Todo.TodoBuilder.aTodo()
                        .withId(24)
                        .withTitle("Smth to do")
                        .withCompleted(false)
                        .withOrder(1)
                        .withUrl("vrf ?")
                        .build()
                        .toJson()
                        .toString())
                .map(result -> result != null);
    }

    @Override
    public Single<Optional<Todo>> getTodo(String id) {

        return redis.rxHget(Constants.REDIS_TODO_KEY, id)
                .map(data -> new Todo(data))
                .map(Optional::ofNullable);
    }

    @Override
    public Single<List<Todo>> getAll() {

        return redis.rxHvals(Constants.REDIS_TODO_KEY)
                .map(data -> data.stream()
                        .map(row -> new Todo((String)row))
                        .collect(Collectors.toList()));
    }

    @Override
    public Single<Boolean> createTodo(Todo todo) {

        return redis.rxHset(Constants.REDIS_TODO_KEY, String.valueOf(todo.getId()), todo.toJson().toString())
                .map(data -> data != null);
    }

    @Override
    public Single<Optional<Todo>> updateTodo(String todoId, final Todo newTodo) {

        return redis.rxHget(Constants.REDIS_TODO_KEY, todoId)
                .flatMap(maybeData -> {

                    return Optional.ofNullable(maybeData)
                            .map(data -> {

                                Todo updatedTodo = new Todo(maybeData).merge(newTodo);
                                return redis.rxHset(Constants.REDIS_TODO_KEY, String.valueOf(updatedTodo.getId()), updatedTodo.toJson().toString())
                                        .map(result -> Optional.of(updatedTodo));
                            }).orElse(Single.fromCallable(() -> Optional.empty()));
                });
    }

    @Override
    public Single<Boolean> deleteTodo(String todoId) {
        return redis.rxHdel(Constants.REDIS_TODO_KEY, todoId)
                .map(result -> result != null);
    }

    @Override
    public Single<Boolean> deleteAll() {
        return redis.rxDel(Constants.REDIS_TODO_KEY).map(result -> result != null);
    }

}
