package org.test.zokki;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.test.zokki.model.Todo;
import org.test.zokki.service.TodoService;

import java.util.HashSet;

/**
 * Created by Dzianis_Shybeka on 12/5/2017
 */
public class SingleApplicationVerticle extends AbstractVerticle {

    private static final String HTTP_HOST = "0.0.0.0";
    private static final String REDIS_HOST = "127.0.0.1";
    private static final int HTTP_PORT = 8082;
    private static final int REDIS_PORT = 6379;

    private RedisClient redis;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        initData();

        TodoService todoService = new TodoService(redis);

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

        router.get(Constants.API_GET).handler(todoService::getTodo);
        router.get(Constants.API_LIST_ALL).handler(todoService::getAll);
        router.post(Constants.API_CREATE).handler(todoService::createTodo);
        router.delete(Constants.API_DELETE).handler(todoService::deleteTodo);
        router.delete(Constants.API_DELETE_ALL).handler(todoService::deleteAll);
        router.patch(Constants.API_UPDATE).handler(todoService::updateTodo);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(8080)
                .subscribe(result -> startFuture.complete(),
                        error -> startFuture.fail(error));

    }

    private void initData() {

        RedisOptions redisOptions = new RedisOptions()
                .setHost(config().getString("redis.host", REDIS_HOST))
                .setPort(config().getInteger("redis.port", REDIS_PORT));

        redis = RedisClient.create(vertx, redisOptions);

        redis.rxHset(Constants.REDIS_TODO_KEY, "24",
                Todo.TodoBuilder.aTodo()
                        .withId(24)
                        .withTitle("Smth to do")
                        .withCompleted(false)
                        .withOrder(1)
                        .withUrl("vrf ?")
                        .build()
                        .toJson()
                        .toString())
        .subscribe(data -> System.out.println("Redis initialized and checked"),
                   throwable -> throwable.printStackTrace());
    }
}
