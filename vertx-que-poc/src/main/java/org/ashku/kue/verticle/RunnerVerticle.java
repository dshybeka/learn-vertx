package org.ashku.kue.verticle;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 *
 * fix redeploy args before testing
 *
  run org.ashku.kue.verticle.RunnerVerticle --redeploy=**\/*.class --launcher-class=io.vertx.core.Launcher
 */
public class RunnerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {

        Single<String> storageVerticle = vertx.rxDeployVerticle(StorageVerticle.class.getName());
        Single<String> proxyVerticle = vertx.rxDeployVerticle(ProxyVerticle.class.getName());
        Single<String> schedulerVerticle = vertx.rxDeployVerticle(SchedulerVerticle.class.getName());

        storageVerticle
                .flatMap(storageInitialized -> Single.zip(proxyVerticle, schedulerVerticle, (p, sch) -> true))
                .subscribe(
                        result -> {
                            System.out.println("Deployed successfully");
                            startFuture.complete();
                        },
                        throwable -> {

                            throwable.printStackTrace();
                            System.out.println("cannot deploy");

                            startFuture.fail(throwable);
                        });
    }
}
