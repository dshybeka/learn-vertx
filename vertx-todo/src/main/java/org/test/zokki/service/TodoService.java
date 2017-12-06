package org.test.zokki.service;

import io.reactivex.Single;
import org.test.zokki.model.Todo;

import java.util.List;
import java.util.Optional;

/**
 * Created by Dzianis_Shybeka on 12/6/2017
 */
public interface TodoService {

    Single<Boolean> initData();

    Single<Optional<Todo>> getTodo(String id);

    Single<List<Todo>> getAll();

    Single<Boolean> createTodo(Todo todo);

    Single<Optional<Todo>> updateTodo(String todoId, Todo newTodo);

    Single<Boolean> deleteTodo(String todoId);

    Single<Boolean> deleteAll();
}
