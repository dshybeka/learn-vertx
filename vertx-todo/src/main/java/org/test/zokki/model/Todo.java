package org.test.zokki.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dzianis_Shybeka on 12/5/2017
 */
@DataObject(generateConverter = true)
public class Todo {

    private static final AtomicInteger acc = new AtomicInteger(0);

    private int id;
    private String title;
    private Boolean completed;
    private Integer order;
    private String url;

    public Todo() {
    }

    public Todo(Todo other) {
        this.id = other.id;
        this.title = other.title;
        this.completed = other.completed;
        this.order = other.order;
        this.url = other.url;
    }

    public Todo(JsonObject other) {

        TodoConverter.fromJson(other, this);
    }

    public Todo(String other) {
        TodoConverter.fromJson(new JsonObject(other), this);
    }

    public JsonObject toJson() {

        JsonObject result = new JsonObject();

        TodoConverter.toJson(this, result);

        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIncId() {
        this.id = acc.incrementAndGet();
    }

    public static int getIncId() {
        return acc.get();
    }

    public static void setIncIdWith(int n) {
        acc.set(n);
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (id != todo.id) return false;
        if (title != null ? !title.equals(todo.title) : todo.title != null) return false;
        if (completed != null ? !completed.equals(todo.completed) : todo.completed != null) return false;
        if (order != null ? !order.equals(todo.order) : todo.order != null) return false;
        return url != null ? url.equals(todo.url) : todo.url == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (completed != null ? completed.hashCode() : 0);
        result = 31 * result + (order != null ? order.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}';
    }

    private <T> T getOrElse(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public Todo merge(Todo todo) {

        return TodoBuilder.aTodo()
                .withId(id)
                .withTitle(getOrElse(todo.title, title))
                .withCompleted(getOrElse(todo.completed, completed))
                .withOrder(getOrElse(todo.order, order))
                .withUrl(url)
                .build();
    }

    public static final class TodoBuilder {
        private int id;
        private String title;
        private Boolean completed;
        private Integer order;
        private String url;

        private TodoBuilder() {
        }

        public static TodoBuilder aTodo() {
            return new TodoBuilder();
        }

        public TodoBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public TodoBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TodoBuilder withCompleted(Boolean completed) {
            this.completed = completed;
            return this;
        }

        public TodoBuilder withOrder(Integer order) {
            this.order = order;
            return this;
        }

        public TodoBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Todo build() {
            Todo todo = new Todo();
            todo.setId(id);
            todo.setTitle(title);
            todo.setCompleted(completed);
            todo.setOrder(order);
            todo.setUrl(url);
            return todo;
        }
    }
}
