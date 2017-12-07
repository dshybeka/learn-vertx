package org.ashku.kue;

/**
 * Created by Dzianis_Shybeka on 12/7/2017
 */
public abstract class Constants {

    private Constants() {}

    public static final Integer DEFAULT_HTTP_PORT = 8080;

    public static final String OPERATION = "operation";

    public static final String USER_ID_SNAKE = "user_id";
    public static final String USER_ID = "userId";

    public static final String SCORE = "score";

    public static final String SUCCESS = "success";
    public static final String DATA = "data";

    public static abstract class Event {

        public static final String STORE_OPERATIONS = "kue.store.operations";
        public static final String STORE_OPERATIONS_FIND_ALL = "kue.store.operations.find.all";
        public static final String STORE_OPERATIONS_ADD = "kue.store.operations.add";

    }

    public static abstract class Storage {

        public static final Integer DEFAULT_REDIS_PORT = 6379;

        public static final String DEFAULT_REDIS_HOST = "localhost";

        public static final String HOST = "host";

        public static final String PORT = "host";
    }
}
