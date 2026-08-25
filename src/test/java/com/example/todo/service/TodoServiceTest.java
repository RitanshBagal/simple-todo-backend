package com.example.todo.service;

import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.dto.UpdateTodoRequest;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TodoServiceTest {

    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoService();
    }

    @Test
    void createTodo_shouldAddAndReturnTodo() {
        CreateTodoRequest request = new CreateTodoRequest("Buy milk", "2 liters of whole milk", false);
        Todo created = todoService.createTodo(request);

        assertNotNull(created.getId());
        assertEquals("Buy milk", created.getTitle());
        assertEquals("2 liters of whole milk", created.getDescription());
        assertFalse(created.isCompleted());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    void getTodoById_existingId_shouldReturnTodo() {
        Todo created = todoService.createTodo(new CreateTodoRequest("Task 1", "Desc 1", false));
        Todo found = todoService.getTodoById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Task 1", found.getTitle());
    }

    @Test
    void getTodoById_nonExistingId_shouldThrowException() {
        assertThrows(TodoNotFoundException.class, () -> todoService.getTodoById(999L));
    }

    @Test
    void getAllTodos_shouldReturnAllTodos() {
        todoService.createTodo(new CreateTodoRequest("Task 1", "Desc 1", false));
        todoService.createTodo(new CreateTodoRequest("Task 2", "Desc 2", true));

        List<Todo> allTodos = todoService.getAllTodos(null, null);
        assertEquals(2, allTodos.size());
    }

    @Test
    void getAllTodos_filterByCompleted() {
        todoService.createTodo(new CreateTodoRequest("Task 1", "Desc 1", false));
        todoService.createTodo(new CreateTodoRequest("Task 2", "Desc 2", true));

        List<Todo> completedTodos = todoService.getAllTodos(true, null);
        assertEquals(1, completedTodos.size());
        assertTrue(completedTodos.get(0).isCompleted());

        List<Todo> pendingTodos = todoService.getAllTodos(false, null);
        assertEquals(1, pendingTodos.size());
        assertFalse(pendingTodos.get(0).isCompleted());
    }

    @Test
    void getAllTodos_filterBySearch() {
        todoService.createTodo(new CreateTodoRequest("Buy groceries", "Milk, Eggs, Bread", false));
        todoService.createTodo(new CreateTodoRequest("Read book", "Spring in Action", false));

        List<Todo> searchResults = todoService.getAllTodos(null, "groceries");
        assertEquals(1, searchResults.size());
        assertEquals("Buy groceries", searchResults.get(0).getTitle());

        List<Todo> searchInDescription = todoService.getAllTodos(null, "bread");
        assertEquals(1, searchInDescription.size());
        assertEquals("Buy groceries", searchInDescription.get(0).getTitle());
    }

    @Test
    void updateTodo_shouldModifyFields() {
        Todo created = todoService.createTodo(new CreateTodoRequest("Old Title", "Old Desc", false));

        UpdateTodoRequest updateRequest = new UpdateTodoRequest("New Title", "New Desc", true);
        Todo updated = todoService.updateTodo(created.getId(), updateRequest);

        assertEquals("New Title", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
        assertTrue(updated.isCompleted());
    }

    @Test
    void toggleTodoStatus_shouldInvertCompletion() {
        Todo created = todoService.createTodo(new CreateTodoRequest("Task", "Desc", false));
        assertFalse(created.isCompleted());

        Todo toggled = todoService.toggleTodoStatus(created.getId());
        assertTrue(toggled.isCompleted());

        Todo toggledAgain = todoService.toggleTodoStatus(created.getId());
        assertFalse(toggledAgain.isCompleted());
    }

    @Test
    void deleteTodo_shouldRemoveTodo() {
        Todo created = todoService.createTodo(new CreateTodoRequest("Task", "Desc", false));
        assertEquals(1, todoService.getAllTodos(null, null).size());

        todoService.deleteTodo(created.getId());
        assertEquals(0, todoService.getAllTodos(null, null).size());
    }

    @Test
    void deleteAllTodos_shouldClearList() {
        todoService.createTodo(new CreateTodoRequest("Task 1", "Desc 1", false));
        todoService.createTodo(new CreateTodoRequest("Task 2", "Desc 2", true));
        assertEquals(2, todoService.getAllTodos(null, null).size());

        todoService.deleteAllTodos();
        assertEquals(0, todoService.getAllTodos(null, null).size());
    }
}
