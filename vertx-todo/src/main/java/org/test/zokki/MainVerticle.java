package org.test.zokki;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        vertx.createHttpServer()
                .requestHandler(requestHolder -> {

                    requestHolder.response().end("Zokki main here");
                }).listen(8080, startupHolder -> {

                    if (startupHolder.succeeded()) {

                        System.out.println("main verticale deployed");

                        startFuture.complete();
                    } else {

                        startFuture.fail(startupHolder.cause());
                    }
                });
    }
}
