package org.ashku.kue.verticle;

import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public class Runner {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        Single<String> storageVerticle = vertx.rxDeployVerticle(StorageVerticle.class.getName());
        Single<String> proxyVerticle = vertx.rxDeployVerticle(ProxyVerticle.class.getName());
        Single<String> schedulerVerticle = vertx.rxDeployVerticle(SchedulerVerticle.class.getName());

        storageVerticle
                .flatMap(storageInitialized -> Single.zip(proxyVerticle, schedulerVerticle, (p, sch) -> true))
                .subscribe(
                        result -> System.out.println("Deployed successfully"),
                        throwable -> {

                            throwable.printStackTrace();
                            System.out.println("cannot deploy");

                            vertx.close();
                        });
    }
}
