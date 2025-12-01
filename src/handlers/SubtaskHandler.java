package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.data.Subtask;
import kanban.manager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager) {
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

            if (path.startsWith("/subtasks")) {
                switch (method) {
                    case "GET" -> handleGetSubtasks(exchange, path);
                    case "POST" -> handlePostSubtasks(exchange);
                    case "DELETE" -> handleDeleteSubtasks(exchange, path);
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

    private void handleGetSubtasks(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            if (subtasks == null || subtasks.isEmpty()) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(subtasks);
                sendText(exchange, response);
            }
        } else {
            try {
                int id = extractIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange);
                } else {
                    String response = gson.toJson(subtask);
                    sendText(exchange, response);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        }
    }

    private void handlePostSubtasks(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        if (requestBody.isEmpty()) {
            sendText(exchange, "Empty request body", 400);
            return;
        }

        Subtask subtask = gson.fromJson(requestBody, Subtask.class);

        if (subtask != null) {
            if (subtask.getId() == null) {
                taskManager.createSubtask(subtask);
            } else {
                taskManager.updateSubtask(subtask);
            }
            sendText(exchange, "Subtask created/updated", 201);
        } else {
            sendText(exchange, "Invalid subtask data", 400);
        }
    }

    private void handleDeleteSubtasks(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            taskManager.deleteSubtaskById(id);
            sendText(exchange, "Subtask deleted", 200);
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }
    }

    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length == 3) {
            return Integer.parseInt(parts[2]);
        }
        return -1;
    }
}
