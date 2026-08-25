package com.example.todo.service;

import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.dto.UpdateTodoRequest;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.model.Todo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TodoService {

    // In-memory list to manage todos without a database
    private final List<Todo> todoList = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Todo> getAllTodos(Boolean completed, String search) {
        return todoList.stream()
                .filter(todo -> {
                    if (completed != null && todo.isCompleted() != completed) {
                        return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String lowerSearch = search.trim().toLowerCase();
                        boolean titleMatches = todo.getTitle() != null && todo.getTitle().toLowerCase().contains(lowerSearch);
                        boolean descMatches = todo.getDescription() != null && todo.getDescription().toLowerCase().contains(lowerSearch);
                        return titleMatches || descMatches;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public Todo getTodoById(Long id) {
        return todoList.stream()
                .filter(todo -> todo.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public Todo createTodo(CreateTodoRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Todo todo = new Todo(
                idCounter.getAndIncrement(),
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.isCompleted(),
                now,
                now
        );
        todoList.add(todo);
        return todo;
    }

    public Todo updateTodo(Long id, UpdateTodoRequest request) {
        Todo existingTodo = getTodoById(id);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            existingTodo.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            existingTodo.setDescription(request.getDescription().trim());
        }
        if (request.getCompleted() != null) {
            existingTodo.setCompleted(request.getCompleted());
        }

        existingTodo.setUpdatedAt(LocalDateTime.now());
        return existingTodo;
    }

    public Todo toggleTodoStatus(Long id) {
        Todo existingTodo = getTodoById(id);
        existingTodo.setCompleted(!existingTodo.isCompleted());
        existingTodo.setUpdatedAt(LocalDateTime.now());
        return existingTodo;
    }

    public void deleteTodo(Long id) {
        Todo existingTodo = getTodoById(id);
        todoList.remove(existingTodo);
    }

    public void deleteAllTodos() {
        todoList.clear();
    }
}
