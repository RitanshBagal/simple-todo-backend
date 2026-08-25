package com.example.todo.controller;

import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.dto.UpdateTodoRequest;
import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllTodos_shouldReturnTodoList() throws Exception {
        Todo todo1 = new Todo(1L, "Task 1", "Desc 1", false, LocalDateTime.now(), LocalDateTime.now());
        Todo todo2 = new Todo(2L, "Task 2", "Desc 2", true, LocalDateTime.now(), LocalDateTime.now());
        List<Todo> todos = Arrays.asList(todo1, todo2);

        when(todoService.getAllTodos(null, null)).thenReturn(todos);

        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    void getTodoById_existingId_shouldReturnTodo() throws Exception {
        Todo todo = new Todo(1L, "Task 1", "Desc 1", false, LocalDateTime.now(), LocalDateTime.now());
        when(todoService.getTodoById(1L)).thenReturn(todo);

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    void getTodoById_notFound_shouldReturn404() throws Exception {
        when(todoService.getTodoById(99L)).thenThrow(new TodoNotFoundException(99L));

        mockMvc.perform(get("/api/todos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Todo not found with id: 99"));
    }

    @Test
    void createTodo_validRequest_shouldReturn201() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("Buy milk", "2 liters", false);
        Todo createdTodo = new Todo(1L, "Buy milk", "2 liters", false, LocalDateTime.now(), LocalDateTime.now());

        when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(createdTodo);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Buy milk"));
    }

    @Test
    void createTodo_blankTitle_shouldReturn400() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("", "Desc", false);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateTodo_validRequest_shouldReturn200() throws Exception {
        UpdateTodoRequest request = new UpdateTodoRequest("Updated Title", "Updated Desc", true);
        Todo updatedTodo = new Todo(1L, "Updated Title", "Updated Desc", true, LocalDateTime.now(), LocalDateTime.now());

        when(todoService.updateTodo(eq(1L), any(UpdateTodoRequest.class))).thenReturn(updatedTodo);

        mockMvc.perform(put("/api/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void toggleTodoStatus_shouldReturnUpdatedTodo() throws Exception {
        Todo toggledTodo = new Todo(1L, "Task 1", "Desc 1", true, LocalDateTime.now(), LocalDateTime.now());
        when(todoService.toggleTodoStatus(1L)).thenReturn(toggledTodo);

        mockMvc.perform(patch("/api/todos/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteTodo_shouldReturn204() throws Exception {
        doNothing().when(todoService).deleteTodo(1L);

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAllTodos_shouldReturn204() throws Exception {
        doNothing().when(todoService).deleteAllTodos();

        mockMvc.perform(delete("/api/todos"))
                .andExpect(status().isNoContent());
    }
}
