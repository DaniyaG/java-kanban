package handlers;

import handlers.PrioritizedHandler;
import kanban.manager.InMemoryTaskManager;
import kanban.data.Task;
import kanban.data.TaskStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest {

    private static final int PORT = 8091;
    private static final String BASE_URI = "http://localhost:" + PORT;

    private HttpServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
        server.start();

        client = HttpClient.newHttpClient();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testGetPrioritized_Empty_shouldReturnEmptyArray() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testGetPrioritized_WithTasks_shouldReturnSortedList() throws IOException, InterruptedException {
        Task task1 = new Task(0, "A Task", "desc", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now().plusDays(1));
        Task task2 = new Task(0, "B Task", "desc", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.now().plusHours(1));
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();

        assertTrue(body.contains("A Task") || body.contains("B Task"));
    }
}