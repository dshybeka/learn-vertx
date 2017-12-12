package org.ashku.kue.verticle;


import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.StringUtils;
import org.ashku.kue.service.QueueService;

import static org.ashku.kue.Constants.*;
import static org.ashku.kue.Constants.Event.STORE_OPERATIONS;

/**
 * Created by Dzianis_Shybeka on 12/6/2017
 */
public class ProxyVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyVerticle.class);

    private org.ashku.kue.service.reactivex.QueueService queueService;

    @Override
    public void start(Future<Void> startFuture) {

        queueService = QueueService.createProxy(vertx.getDelegate(), STORE_OPERATIONS);

        Router router = Router.router(vertx);

        router.get("/proxy/:userId").handler(this::proxyRequest);
        router.get("/queue").handler(this::showAllDefault);
        router.get("/process-queue").handler(this::showAllProcess);
        router.route("/*").handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(config().getInteger("http.port", DEFAULT_HTTP_PORT))
                .subscribe(done -> {

                            LOG.info("Http proxy server started ");
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

            queueService.rxAddToQueue(new JsonObject().put(USER_ID_SNAKE, userId))
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

    private void showAllDefault(RoutingContext routingContext) {


        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString());

        queueService.rxFindAllDefault()
                .subscribe(
                        result -> {

                            response.end(new JsonObject().put(SUCCESS, true).put(DATA, result).toString());
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            internalError(response);
                        }
                );
    }

    private void showAllProcess(RoutingContext routingContext) {


        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString());

        queueService.rxFindAllProcess()
                .subscribe(
                        result -> {

                            response.end(new JsonObject().put(SUCCESS, true).put(DATA, result).toString());
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
