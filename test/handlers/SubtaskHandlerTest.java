package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import exceptions.NotFoundException;
import kanban.data.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

class SubtaskHandlerTest {
    private static final int PORT = 8090;
    private static final String BASE_URI = "http://localhost:" + PORT;
    private HttpServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/subtasks", new SubtaskHandler(taskManager));
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
    void testGetAllSubtasks_whenExist_shouldReturnJsonArrayAnd200() throws Exception {
        Subtask subtask1 = new Subtask(null, "Subtask1", "Desc1", TaskStatus.NEW,1, Duration.ofMinutes(15), LocalDateTime.of(2000,1,1,10,0));
        Subtask subtask2 = new Subtask(null, "Subtask2", "Desc2", TaskStatus.NEW, 1,  Duration.ofMinutes(30), LocalDateTime.of(2000,2,2,11,0));
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedJson = gson.toJson(taskManager.getAllSubtasks());
        assertEquals(expectedJson, response.body());
    }

    @Test
    void testGetSubtaskById_whenExists_shouldReturnJsonAnd200() throws Exception {
        Subtask subtask = new Subtask(0, "Subtask", "Desc", TaskStatus.NEW, 1, Duration.ofMinutes(20), LocalDateTime.of(2000,3,3,12,0));
        taskManager.createSubtask(subtask);
        int id = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/subtasks/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedJson = gson.toJson(taskManager.getSubtaskById(id));
        assertEquals(expectedJson, response.body());
    }

    @Test
    void testPostSubtask_create_shouldRespond201AndStore() throws Exception {
        Subtask newSubtask = new Subtask(1, "New Subtask", "Desc", TaskStatus.NEW,1, Duration.ofMinutes(25), LocalDateTime.of(2000,4,4,13,0));
        taskManager.createSubtask(newSubtask);
        String json = gson.toJson(newSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNotNull(taskManager.getSubtaskById(newSubtask.getId()));
        assertFalse(response.body().isEmpty());
    }

    @Test
    void testPostSubtask_update_shouldRespond201AndUpdate() throws Exception {
        Subtask subtask = new Subtask(null, "Original", "Desc", TaskStatus.NEW, 1,  Duration.ofMinutes(10), LocalDateTime.of(2000,5,5,14,0));
        taskManager.createSubtask(subtask);
        int id = subtask.getId();

        subtask.setTitle("Updated Name");
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Subtask updated = taskManager.getSubtaskById(id);
        assertEquals("Updated Name", updated.getTitle());
    }

    @Test
    void testDeleteSubtask_shouldRespond200AndRemove() throws Exception {
        Subtask subtask = new Subtask(null, "ToDelete", "Desc", TaskStatus.NEW,1, Duration.ofMinutes(10), LocalDateTime.of(2000,6,6,15,0));
        taskManager.createSubtask(subtask);
        int id = subtask.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/subtasks/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> String.valueOf(taskManager.getTaskById(id)));
    }
}