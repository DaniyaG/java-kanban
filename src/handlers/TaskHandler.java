package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.data.Task;
import kanban.manager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager) {
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

            if (path.startsWith("/tasks")) {
                switch (method) {
                    case "GET" -> handleGetTasks(exchange, path);
                    case "POST" -> handlePostTasks(exchange);
                    case "DELETE" -> handleDeleteTasks(exchange, path);
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

    private void handleGetTasks(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            if (tasks == null || tasks.isEmpty()) {
                sendNotFound(exchange);
            } else {
                String response = gson.toJson(tasks);
                sendText(exchange, response);
            }
        } else {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange);
                } else {
                    String response = gson.toJson(task);
                    sendText(exchange, response);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange);
            }
        }
    }

    private void handlePostTasks(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        if (requestBody.isEmpty()) {
            sendText(exchange, "Empty request body", 400);
            return;
        }

        Task task = gson.fromJson(requestBody, Task.class);

        if (task != null) {
            if (task.getId() == null) {
                taskManager.createTask(task);
            } else {
                taskManager.updateTask(task);
            }
            sendText(exchange, "Task created/updated", 201);
        } else {
            sendText(exchange, "Invalid task data", 400);
        }
    }

    private void handleDeleteTasks(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            taskManager.deleteTaskById(id);
            sendText(exchange, "Task deleted", 200);
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

