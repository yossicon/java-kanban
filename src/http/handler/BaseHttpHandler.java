package http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.adapter.*;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class BaseHttpHandler implements HttpHandler {

    void sendText(HttpExchange exchange, String text, int responseCode) {
        try {
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(responseCode, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }

    //я сделала методы protected, чтобы предоставить доступ только наследникам
    protected void sendBadRequest(HttpExchange exchange, String text) {
        sendText(exchange, text, 400);
    }

    protected void sendNotFound(HttpExchange exchange, String text) {
        sendText(exchange, text, 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String text) {
        sendText(exchange, text, 406);
    }

    protected void sendInternalServerError(HttpExchange exchange) {
        try {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) {
        try {
            exchange.sendResponseHeaders(405, 0);
            exchange.close();
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа: " + e.getMessage());
        }
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .serializeNulls();
        return gsonBuilder.create();
    }

    protected Integer parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
