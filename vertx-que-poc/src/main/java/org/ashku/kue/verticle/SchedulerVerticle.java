package org.ashku.kue.verticle;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import org.ashku.kue.service.QueueService;

import static org.ashku.kue.Constants.Event.STORE_OPERATIONS;
import static org.ashku.kue.verticle.StorageVerticle.DEFAULT_QUEUE_SIZE;

/**
 * Created by Dzianis_Shybeka on 12/12/2017
 */
public class SchedulerVerticle extends AbstractVerticle {

    private static final int DEFAULT_PERIOD = 1000 * 10;
    private static final int DEFAULT_TTL_FOR_PROCESS_QUEUE = 1000 * 60 * 2;

    private org.ashku.kue.service.reactivex.QueueService queueService;

    @Override
    public void start(Future<Void> startFuture) {

        queueService = QueueService.createProxy(vertx.getDelegate(), STORE_OPERATIONS);

        vertx.setPeriodic(DEFAULT_PERIOD, result -> {

            Single<Long> deletedSingle = queueService.rxDeleteFromProcessQueue(System.currentTimeMillis() - DEFAULT_TTL_FOR_PROCESS_QUEUE);
            Single<Long> availableSingle = queueService.rxCountProcessQueue();

            deletedSingle.zipWith(availableSingle, (deleted, available) -> deleted == 0 ? DEFAULT_QUEUE_SIZE - available : deleted)
                    .filter(value -> value > 0)
                    .flatMap(deleteResult -> {

                        System.out.println("Move to next queue elements #" + deleteResult);

                        return queueService.rxMoveToProcessQueue(deleteResult).toMaybe();
                    })
                    .subscribe(
                      deleteResult -> {
                          System.out.println("Data removed from process queue ");
                      },
                      throwable -> {

                          System.out.println("Error while removing data from queue");
                          throwable.printStackTrace();
                      }
                    );
        });

        startFuture.complete();
    }
}
