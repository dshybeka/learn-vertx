package org.ashku.kue.verticle;

import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class Runner {

    private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        Single<String> storageVerticle = vertx.rxDeployVerticle(StorageVerticle.class.getName());
        Single<String> proxyVerticle = vertx.rxDeployVerticle(ProxyVerticle.class.getName());
        Single<String> schedulerVerticle = vertx.rxDeployVerticle(SchedulerVerticle.class.getName());

        storageVerticle
                .flatMap(storageInitialized -> Single.zip(proxyVerticle, schedulerVerticle, (p, sch) -> true))
                .subscribe(
                        result -> LOG.info("Deployed successfully"),
                        throwable -> {

                            throwable.printStackTrace();
                            LOG.info("cannot deploy");

                            vertx.close();
                        });
    }
}
