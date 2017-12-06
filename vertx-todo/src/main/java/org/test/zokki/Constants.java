package org.test.zokki;

/**
 * Created by Dzianis_Shybeka on 12/5/2017
 */
public final class Constants {

    private Constants() {}

    public static final String API_GET = "/todos/:todoId";
    public static final String API_LIST_ALL = "/todos";
    public static final String API_CREATE = "/todos";
    public static final String API_UPDATE = "/todos/:todoId";
    public static final String API_DELETE = "/todos/:todoId";
    public static final String API_DELETE_ALL = "/todos";

    public static final String REDIS_TODO_KEY = "todos";

    public static final String REDIS_HOST = "127.0.0.1";
    public static final int REDIS_PORT = 6379;
}
