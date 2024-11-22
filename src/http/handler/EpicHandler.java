package http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /epics запроса от клиента.");
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        try {
            switch (requestMethod) {
                case "GET":
                    handleGetEpic(exchange, path);
                    break;
                case "POST":
                    handlePostEpic(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteEpic(exchange, path);
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

    private void handleGetEpic(HttpExchange exchange, String path) throws IOException {
        try {
            if (Pattern.matches("^/epics/\\d+$", path)) {
                String pathId = path.replaceFirst("/epics/", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getEpicById(id) != null) {
                        String text = getGson().toJson(taskManager.getEpicById(id));
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "Эпик с id " + id + " не найден");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/epics/\\d+/subtasks$", path)) {
                String pathId = path.replaceFirst("/epics/", "").replaceFirst("/subtasks", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getEpicById(id) != null) {
                        String text = getGson().toJson(taskManager.getSubtasksByEpic(id));
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "Эпик с id " + id + " не найден");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/epics$", path)) {
                String text = getGson().toJson(taskManager.getAllEpics());
                sendText(exchange, text, 200);
            } else {
                sendBadRequest(exchange, "Неверный запрос");
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange, String path) throws IOException {
        try (InputStream bodyInputStream = exchange.getRequestBody()) {
            String body = new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
            try {
                if (Pattern.matches("^/epics$", path)) {
                    Epic epic = getGson().fromJson(body, Epic.class);
                    String text = getGson().toJson(taskManager.createEpic(epic));
                    sendText(exchange, text, 201);
                } else {
                    sendNotFound(exchange, "Неверный запрос");
                }
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Передан объект в неверном формате");
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, String path) throws IOException {
        if (Pattern.matches("^/epics/\\d+$", path)) {
            String pathId = path.replaceFirst("/epics/", "");
            int id = parsePathId(pathId);
            if (id != -1) {
                if (taskManager.getEpicById(id) != null) {
                    taskManager.deleteEpicById(id);
                    sendText(exchange, "Эпик удален", 200);
                } else {
                    sendNotFound(exchange, "Эпик с id " + id + " не найден");
                }
            } else {
                System.out.println("Полученный id " + pathId + " некорректен"); //not like that
            }
        } else if (Pattern.matches("^/epics$", path)) {
            taskManager.deleteAllEpics();
            sendText(exchange, "Все эпики удалены", 200);
        } else {
            sendBadRequest(exchange, "Неверный запрос");
        }
    }
}
