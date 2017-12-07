package org.ashku.kue.verticle;


import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.StringUtils;

import static org.ashku.kue.Constants.DEFAULT_HTTP_PORT;
import static org.ashku.kue.Constants.Event.STORE_OPERATIONS_ADD;
import static org.ashku.kue.Constants.Event.STORE_OPERATIONS_FIND_ALL;
import static org.ashku.kue.Constants.USER_ID_SNAKE;

/**
 * Created by Dzianis_Shybeka on 12/6/2017
 */
public class ProxyVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {

        Router router = Router.router(vertx);

        router.get("/proxy/:userId").handler(this::proxyRequest);
        router.get("/queue").handler(this::showAll);
        router.route("/*").handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(config().getInteger("http.port", DEFAULT_HTTP_PORT))
                .subscribe(done -> {

                            System.out.println("Http proxy server started ");
                            startFuture.complete();
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            startFuture.fail(throwable);
                        });
    }

    private void proxyRequest(RoutingContext routingContext) {

        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString());

        String userId = routingContext.request().getParam("userId");

        if(StringUtils.isNotEmpty(userId)) {

            vertx.eventBus().rxSend(STORE_OPERATIONS_ADD, new JsonObject().put(USER_ID_SNAKE, userId))
                    .subscribe(
                            result -> {
                                response.end("proxied");
                            },
                            throwable -> {

                                throwable.printStackTrace();
                                internalError(response);
                            });
        } else {

            badRequest(response);
        }
    }

    private void showAll(RoutingContext routingContext) {


        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString());

        vertx.eventBus().rxSend(STORE_OPERATIONS_FIND_ALL, "")
                .subscribe(
                        result -> {

                            response.end(Json.encode(result.body()));
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            internalError(response);
                        }
                );
    }

    private void internalError(HttpServerResponse response) {

        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
    }

    private void badRequest(HttpServerResponse response) {

        response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
    }
}
