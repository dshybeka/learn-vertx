package org.test.zokki.verticles;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.test.zokki.Constants;
import org.test.zokki.model.Todo;
import org.test.zokki.service.TodoService;
import org.test.zokki.service.impl.TodoServiceRedisImpl;

import java.util.HashSet;

import static org.test.zokki.Constants.REDIS_HOST;
import static org.test.zokki.Constants.REDIS_PORT;


/**
 * Created by Dzianis_Shybeka on 12/6/2017
 */
public class TodoVerticle extends AbstractWebVerticle {

    private TodoService todoService;

    public void init() {

        RedisOptions redisOptions = new RedisOptions()
                .setHost(config().getString("redis.host", REDIS_HOST))
                .setPort(config().getInteger("redis.port", REDIS_PORT));

        this.todoService = new TodoServiceRedisImpl(RedisClient.create(vertx, redisOptions));
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        init();
        initData();

        Router router = Router.router(vertx);

        HashSet<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");

        router.route().handler(
                CorsHandler.create("*")
                        .allowedHeaders(allowHeaders)
                        .allowedMethod(HttpMethod.GET)
                        .allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.DELETE)
                        .allowedMethod(HttpMethod.PATCH)
        );
        router.route().handler(BodyHandler.create());

        router.get(Constants.API_GET).handler(this::getTodo);
        router.get(Constants.API_LIST_ALL).handler(this::getAll);
        router.post(Constants.API_CREATE).handler(this::createTodo);
        router.delete(Constants.API_DELETE).handler(this::deleteTodo);
        router.delete(Constants.API_DELETE_ALL).handler(this::deleteAll);
        router.patch(Constants.API_UPDATE).handler(this::updateTodo);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(8080)
                .subscribe(result -> startFuture.complete(),
                        error -> startFuture.fail(error));
    }

    private void initData() {

        todoService.initData()
                .subscribe(data -> System.out.println("Redis initialized and verified"),
                        throwable -> throwable.printStackTrace());
    }

    private void getTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        String todoId = routingContext.request().getParam("todoId");
        if (todoId == null) {
            badRequest(response);
            return;
        }

        todoService.getTodo(todoId)
                .subscribe(
                        maybeTodo -> {

                            if (maybeTodo.isPresent()) {

                                response.end(Json.encode(maybeTodo.get()));
                            } else {
                                notFound(response);
                            }
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            internalServerError(response);
                        });
    }

    private void getAll(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        todoService.getAll()
                .subscribe(
                        data -> {
                            response.end(Json.encode(data));
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            internalServerError(response);
                        });
    }

    private void createTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        try {

            final Todo todo = wrapObject(new Todo(routingContext.getBodyAsString()), routingContext);

            todoService.createTodo(todo)
                    .subscribe(
                            result -> response.setStatusCode(HttpResponseStatus.CREATED.code()).end(Json.encode(todo)),
                            throwable -> {
                                throwable.printStackTrace();
                                internalServerError(response);
                            }
                    );
        } catch (DecodeException e) {

            e.printStackTrace();
            badRequest(response);
        }
    }

    private void updateTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        try {

            String todoId = routingContext.request().getParam("todoId");

            if (todoId == null) {
                badRequest(response);
                return;
            }

            final Todo todo = wrapObject(new Todo(routingContext.getBodyAsString()), routingContext);

            todoService.updateTodo(todoId, todo)
                    .subscribe(
                            result -> response.end(Json.encode(todo)),
                            throwable -> {
                                throwable.printStackTrace();
                                internalServerError(response);
                            }
                    );
        } catch (DecodeException e) {

            badRequest(response);
        }
    }

    private void deleteTodo(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        String todoId = routingContext.request().getParam("todoId");

        if (todoId == null) {
            badRequest(response);
            return;
        }

        todoService.deleteTodo(todoId)
                .subscribe(
                        result -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(),
                        throwable -> {

                            throwable.printStackTrace();
                            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }
                );
    }

    private void deleteAll(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response().putHeader("content-type", "application/json");

        todoService.deleteAll()
                .subscribe(
                        result -> response.setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end(),
                        throwable -> {
                            throwable.printStackTrace();
                            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                        }
                );
    }

    private Todo wrapObject(Todo todo, RoutingContext context) {
        int id = todo.getId();
        if (id > Todo.getIncId()) {
            Todo.setIncIdWith(id);
        } else if (id == 0)
            todo.setIncId();
        todo.setUrl(context.request().absoluteURI() + "/" + todo.getId());
        return todo;
    }
}
