/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.ashku.kue.service.reactivex;

import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link org.ashku.kue.service.QueueService original} non RX-ified interface using Vert.x codegen.
 */

@io.vertx.lang.reactivex.RxGen(org.ashku.kue.service.QueueService.class)
public class QueueService {

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QueueService that = (QueueService) o;
    return delegate.equals(that.delegate);
  }
  
  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  public static final io.vertx.lang.reactivex.TypeArg<QueueService> __TYPE_ARG = new io.vertx.lang.reactivex.TypeArg<>(
    obj -> new QueueService((org.ashku.kue.service.QueueService) obj),
    QueueService::getDelegate
  );

  private final org.ashku.kue.service.QueueService delegate;
  
  public QueueService(org.ashku.kue.service.QueueService delegate) {
    this.delegate = delegate;
  }

  public org.ashku.kue.service.QueueService getDelegate() {
    return delegate;
  }

  public QueueService findAll(Handler<AsyncResult<JsonArray>> handler) { 
    delegate.findAll(handler);
    return this;
  }

  public Single<JsonArray> rxFindAll() { 
    return new io.vertx.reactivex.core.impl.AsyncResultSingle<JsonArray>(handler -> {
      findAll(handler);
    });
  }

  public QueueService addToQueue(JsonObject request, Handler<AsyncResult<Void>> handler) { 
    delegate.addToQueue(request, handler);
    return this;
  }

  public Completable rxAddToQueue(JsonObject request) { 
    return new io.vertx.reactivex.core.impl.AsyncResultCompletable(handler -> {
      addToQueue(request, handler);
    });
  }


  public static  QueueService newInstance(org.ashku.kue.service.QueueService arg) {
    return arg != null ? new QueueService(arg) : null;
  }
}
