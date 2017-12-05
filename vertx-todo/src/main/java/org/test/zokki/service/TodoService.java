package org.test.zokki.service;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Observable;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.redis.RedisClient;
import org.test.zokki.Constants;
import org.test.zokki.model.Todo;

/**
 * Created by Dzianis_Shybeka on 12/5/2017
 */
public class TodoService {

    private final RedisClient redis;

    public TodoService(RedisClient redis) {
        this.redis = redis;
    }

    public void getTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        String todoIdOrNull = routingContext.request().getParam("todoId");

        if (todoIdOrNull != null) {

            redis.rxHget(Constants.REDIS_TODO_KEY, todoIdOrNull)
                    .subscribe(data -> response.end(new Todo(data).toJson().toString()),
                            throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
        } else {

            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
        }

    }

    public void getAll(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        redis.rxHvals(Constants.REDIS_TODO_KEY)
                .flatMapObservable(data -> Observable.<String>fromIterable(data.getList()))
                .subscribe(data -> response.end(new Todo(data.toString()).toJson().toString()),
                        throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
    }

    public void createTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        Todo newTodo = new Todo(routingContext.getBodyAsJson());
        newTodo.setIncId();
        newTodo.setUrl(routingContext.request().absoluteURI() + "/" + newTodo.getId());

        redis.rxHset(Constants.REDIS_TODO_KEY, String.valueOf(newTodo.getId()), newTodo.toJson().toString())
                .subscribe(data -> response.setStatusCode(HttpResponseStatus.CREATED.code()).end(newTodo.toJson().toString()),
                        throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
    }

    public void updateTodo(RoutingContext routingContext) {

        String todoId = routingContext.request().getParam("todoId");
        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        Todo todo = new Todo(routingContext.getBodyAsJson());

        if (todoId == null || todo == null) {

            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }

        redis.rxHget(Constants.REDIS_TODO_KEY, todoId)
                .subscribe(found -> {

                            if (found != null) {

                                Todo updatedTodo = new Todo(found).merge(todo);

                                redis.rxHset(Constants.REDIS_TODO_KEY, String.valueOf(updatedTodo.getId()), updatedTodo.toJson().toString())
                                        .subscribe(data -> response.end(updatedTodo.toJson().toString()),
                                                throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
                            } else {

                                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
                            }
                        },
                        throwable -> {

                            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        });
    }

    public void deleteTodo(RoutingContext routingContext) {

        String todoId = routingContext.request().getParam("todoId");
        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        if (todoId == null) {
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }

        redis.rxHdel(Constants.REDIS_TODO_KEY, todoId)
                .subscribe(
                        result -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(),
                        throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
    }

    public void deleteAll(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        redis.rxDel(Constants.REDIS_TODO_KEY)
                .subscribe(result -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(),
                           throwable -> response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end());
    }
}
