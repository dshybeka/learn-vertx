package org.ashku.kue;

/**
 * Created by Dzianis_Shybeka on 12/12/2017
 */
public class Config {

    static {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    }
}
