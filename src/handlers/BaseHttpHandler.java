package handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
            byte[] resp = text.getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(statusCode, resp.length);
            h.getResponseBody().write(resp);
            h.close();
        }

        protected void sendText(HttpExchange h, String text) throws IOException {
            sendText(h, text, 200);
        }

        protected void sendNotFound(HttpExchange h) throws IOException {
            String message = "Resource not found";
            sendText(h, message, 404);
        }

        protected void sendHasIntersections(HttpExchange h) throws IOException {
            String message = "Task has intersections";
            sendText(h, message, 406);
        }

    protected void sendInternalServerError(HttpExchange h, Exception e) throws IOException {
        String message = "Internal Server Error: " + e.getMessage();
        sendText(h, message, 500);
    }

        protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}