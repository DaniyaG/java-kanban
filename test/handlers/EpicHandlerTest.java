package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import exceptions.NotFoundException;
import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.TaskStatus;
import kanban.manager.InMemoryTaskManager;
import org.junit.jupiter.api.*;

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

class EpicHandlerTest {

    private static final int PORT = 8081;
    private static final String BASE_URI = "http://localhost:" + PORT;
    private HttpServer server;
    private HttpClient client;
    private Gson gson;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/epics", new EpicHandler(taskManager));
        server.start();
        client = HttpClient.newHttpClient();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        gson = gsonBuilder.create();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void testGetAllEpics_empty_shouldReturn404() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testGetAllEpics_withData_shouldReturnJsonAnd200() throws IOException, InterruptedException {
        Epic epic1 = new Epic(0, "Epic1", "Epic1 Description", TaskStatus.NEW);
        Epic epic2 = new Epic(0, "Epic2", "Epic2 Description", TaskStatus.NEW);

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Epic> expectedEpics = taskManager.getAllEpics();
        String expectedJson = gson.toJson(expectedEpics);
        assertEquals(expectedJson, response.body());
    }

    @Test
    void testGetEpicById_existing_shouldReturnEpicJsonAnd200() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic1", "Epic Description", TaskStatus.NEW);
        taskManager.createEpic(epic);
        int id = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/epics/" + id))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedJson = gson.toJson(taskManager.getEpicById(id));
        assertEquals(expectedJson, response.body());
    }

    @Test
    void testDeleteExistingEpic_shouldReturn200AndRemoveEpic() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic1", "Epic Description", TaskStatus.NEW);
        taskManager.createEpic(epic);
        int id = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/epics/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> String.valueOf(taskManager.getEpicById(id)));
    }

    @Test
    void testGetSubtasksByEpicId_existing_shouldReturnSubtasksJsonAnd200() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic with Subtasks", "Desc",TaskStatus.NEW);
        taskManager.createEpic(epic);
        int epicId = epic.getId();

        Subtask sub1 = new Subtask(0, "Subtask1", "Desc",TaskStatus.NEW, epicId, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        taskManager.createSubtask(sub1);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URI + "/epics/" + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), List.class);
        assertNotNull(subtasks);
    }
}