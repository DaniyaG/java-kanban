package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.manager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter());
        this.gson = gsonBuilder.create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/epics")) {
                switch (method) {
                    case "GET" -> handleGetEpics(exchange, path);
                    case "POST" -> handlePostEpics(exchange);
                    case "DELETE" -> handleDeleteEpics(exchange, path);
                    case null, default -> {
                        exchange.sendResponseHeaders(405, 0);
                        exchange.close();
                    }
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange,e);
        }
    }

    private void handleGetEpics(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            if (epics == null || epics.isEmpty()) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(epics);
                sendText(exchange, response);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            try {
                int epicId = extractIdFromPath(path, "/epics/(\\d+)/subtasks");
                List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epicId);
                if (subtasks == null || subtasks.isEmpty()) {
                    sendNotFound(exchange);
                } else {
                    String response = gson.toJson(subtasks);
                    sendText(exchange, response);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        } else {
            try {
                int id = extractIdFromPath(path, "/epics/(\\d+)");
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange);
                } else {
                    String response = gson.toJson(epic);
                    sendText(exchange, response);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        }
    }

    private void handlePostEpics(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        if (requestBody.isEmpty()) {
            sendText(exchange, "Empty request body", 400);
            return;
        }

        Epic epic = gson.fromJson(requestBody, Epic.class);

        if (epic != null) {
            if (epic.getId() == null) {
                taskManager.createEpic(epic);
            } else {
                taskManager.updateEpic(epic);
            }
            sendText(exchange, "Epic created/updated", 201);
        } else {
            sendText(exchange, "Invalid epic data", 400);
        }
    }

    private void handleDeleteEpics(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path, "/epics/(\\d+)");
            taskManager.deleteEpicById(id);
            sendText(exchange, "Epic deleted", 200);
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private int extractIdFromPath(String path, String regex) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(path);
        if (matcher.find() && matcher.groupCount() == 1) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }
}