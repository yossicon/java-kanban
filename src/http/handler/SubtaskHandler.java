package http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TaskOverlapException;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /subtasks запроса от клиента.");
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        try {
            switch (requestMethod) {
                case "GET":
                    handleGetSubtask(exchange, path);
                    break;
                case "POST":
                    handlePostSubtask(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteSubtask(exchange, path);
                    break;
                default:
                    System.out.println("Запрос не соответствует ожидаемому (GET, POST или DELETE). Получен запрос: "
                            + requestMethod);
                    exchange.sendResponseHeaders(405, 0);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private void handleGetSubtask(HttpExchange exchange, String path) throws IOException {
        try {
            if (Pattern.matches("^/subtasks/\\d+$", path)) {
                String pathId = path.replaceFirst("/subtasks/", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getSubtaskById(id) != null) {
                        String text = getGson().toJson(taskManager.getSubtaskById(id));
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "Подзадача с id " + id + " не найдена");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/subtasks$", path)) {
                String text = getGson().toJson(taskManager.getAllSubtasks());
                sendText(exchange, text, 200);
            } else {
                sendBadRequest(exchange, "Неверный запрос");
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handlePostSubtask(HttpExchange exchange, String path) throws IOException {
        try (InputStream bodyInputStream = exchange.getRequestBody()) {
            String body = new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
            try {
                if (Pattern.matches("^/subtasks/\\d+$", path)) {
                    String pathId = path.replaceFirst("/subtasks/", "");
                    int id = parsePathId(pathId);
                    if (id != -1) {
                        if (taskManager.getSubtaskById(id) != null) {
                            Subtask subtask = getGson().fromJson(body, Subtask.class);
                            String text = getGson().toJson(taskManager.updateSubtask(subtask));
                            sendText(exchange, text, 201);
                        } else {
                            sendNotFound(exchange, "Подзадача с id " + id + " не найдена");
                        }
                    } else {
                        sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                    }
                } else if (Pattern.matches("^/subtasks$", path)) {
                    Subtask subtask = getGson().fromJson(body, Subtask.class);
                    if (taskManager.getEpicById(subtask.getEpicId()) != null) {
                        String text = getGson().toJson(taskManager.createSubtask(subtask));
                        sendText(exchange, text, 201);
                    } else {
                        sendNotFound(exchange, "Эпик для создания подзадачи не найден");
                    }
                } else {
                    sendBadRequest(exchange, "Неверный запрос");
                }
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Передан объект в неверном формате");
            } catch (TaskOverlapException e) {
                sendHasInteractions(exchange, "Подзадача пересекается по времени с существующей");
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, String path) throws IOException {
        try {
            if (Pattern.matches("^/subtasks/\\d+$", path)) {
                String pathId = path.replaceFirst("/subtasks/", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getSubtaskById(id) != null) {
                        taskManager.deleteSubtaskById(id);
                        sendText(exchange, "Подзадача удалена", 200);
                    } else {
                        sendNotFound(exchange, "Подзадача с id " + id + " не найдена");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/subtasks$", path)) {
                taskManager.deleteAllSubtasks();
                sendText(exchange, "Все подзадачи удалены", 200);
            } else {
                sendBadRequest(exchange, "Неверный запрос");
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}
