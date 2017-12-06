package org.test.zokki.verticles;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;

/**
 * Created by Dzianis_Shybeka on 12/6/2017
 */
public abstract class AbstractWebVerticle extends AbstractVerticle {

    protected void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    protected void badRequest(HttpServerResponse response) {
        sendError(HttpResponseStatus.BAD_REQUEST.code(), response);
    }

    protected void notFound(HttpServerResponse response) {
        sendError(HttpResponseStatus.NOT_FOUND.code(), response);
    }

    protected void serviceUnavailable(HttpServerResponse response) {
        sendError(HttpResponseStatus.SERVICE_UNAVAILABLE.code(), response);
    }

    protected void internalServerError(HttpServerResponse response) {
        sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), response);
    }
}
