package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import exceptions.NotFoundException;
import kanban.data.Task;
import kanban.data.TaskStatus;
import kanban.manager.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {

    private static final int PORT = 8080;
    private static final String BASE_URI = "http://localhost:" + PORT;
    private HttpServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(taskManager));
        server.start();
        client = HttpClient.newHttpClient();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        this.gson = gsonBuilder.create();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testHandleGetTasks_taskList_shouldReturnJsonArrayAnd200() throws IOException, InterruptedException {
        taskManager.createTask(new Task(0, "Task1", "Description1", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0)));
        taskManager.createTask(new Task(0, "Task2", "Description2", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.of(2000, 1, 2, 11, 0)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> expectedTasks = taskManager.getAllTasks();
        String expectedJson = gson.toJson(expectedTasks);
        assertEquals(expectedJson, response.body());
    }

    @Test
    void handleGetTaskById_existingTask_shouldReturnJsonAnd200() throws IOException, InterruptedException {
        Task task = new Task(0, "Task1", "Description1", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        taskManager.createTask(task);
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/tasks/" + taskId))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedJson = gson.toJson(task);
        assertEquals(expectedJson, response.body());
    }

    @Test
    void handlePostTasks_createTask_shouldCreateAndReturn201() throws IOException, InterruptedException {

        Task task = new Task(0, "Task1", "Description1", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNotNull(taskManager.getTaskById(task.getId()));
        assertFalse(response.body().isEmpty());  

    }

    @Test
    void handleDeleteTasks_existingTask_shouldDeleteAndReturn200() throws IOException, InterruptedException {

        Task task = new Task(0, "Task1", "Description1", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        taskManager.createTask(task);
        int taskId = task.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/tasks/" + taskId))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> String.valueOf(taskManager.getTaskById(taskId)));
    }
}
