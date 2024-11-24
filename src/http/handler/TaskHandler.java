package http.handler;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exceptions.TaskOverlapException;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("Началась обработка /tasks запроса от клиента.");
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();

        try {
            switch (requestMethod) {
                case "GET":
                    handleGetTask(exchange, path);
                    break;
                case "POST":
                    handlePostTask(exchange, path);
                    break;
                case "DELETE":
                    handleDeleteTask(exchange, path);
                    break;
                default:
                    System.out.println("Запрос не соответствует ожидаемому (GET, POST или DELETE). Получен запрос: "
                            + requestMethod);
                    sendMethodNotAllowed(exchange);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }

    private void handleGetTask(HttpExchange exchange, String path) {
        try {
            if (Pattern.matches("^/tasks/\\d+$", path)) {
                String pathId = path.replaceFirst("/tasks/", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getTaskById(id) != null) {
                        String text = getGson().toJson(taskManager.getTaskById(id));
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "Задача с id " + id + " не найдена");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/tasks$", path)) {
                String text = getGson().toJson(taskManager.getAllTasks());
                sendText(exchange, text, 200);
            } else {
                sendBadRequest(exchange, "Неверный запрос");
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handlePostTask(HttpExchange exchange, String path) {
        try (InputStream bodyInputStream = exchange.getRequestBody()) {
            String body = new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
            if (body.isBlank()) {
                sendBadRequest(exchange, "Тело запроса пустое");
            }
            try {
                if (Pattern.matches("^/tasks/\\d+$", path)) {
                    String pathId = path.replaceFirst("/tasks/", "");
                    int id = parsePathId(pathId);
                    if (id != -1) {
                        if (taskManager.getTaskById(id) != null) {
                            Task task = getGson().fromJson(body, Task.class);
                            String text = getGson().toJson(taskManager.updateTask(task));
                            sendText(exchange, text, 200);
                        } else {
                            sendNotFound(exchange, "Задача с id " + id + " не найдена");
                        }
                    } else {
                        sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                    }
                } else if (Pattern.matches("^/tasks$", path)) {
                    Task task = getGson().fromJson(body, Task.class);
                    String text = getGson().toJson(taskManager.createTask(task));
                    sendText(exchange, text, 201);
                } else {
                    sendBadRequest(exchange, "Неверный запрос");
                }
            } catch (JsonSyntaxException e) {
                sendBadRequest(exchange, "Передан объект в неверном формате");
            } catch (TaskOverlapException e) {
                sendHasInteractions(exchange, "Задача пересекается по времени с существующей");
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при получении запроса: " + e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange, String path) {
        try {
            if (Pattern.matches("^/tasks/\\d+$", path)) {
                String pathId = path.replaceFirst("/tasks/", "");
                int id = parsePathId(pathId);
                if (id != -1) {
                    if (taskManager.getTaskById(id) != null) {
                        taskManager.deleteTaskById(id);
                        sendText(exchange, "Задача удалена", 200);
                    } else {
                        sendNotFound(exchange, "Задача с id " + id + " не найдена");
                    }
                } else {
                    sendBadRequest(exchange, "Полученный id " + pathId + " некорректен");
                }
            } else if (Pattern.matches("^/tasks$", path)) {
                taskManager.deleteAllTasks();
                sendText(exchange, "Все задачи удалены", 200);
            } else {
                sendBadRequest(exchange, "Неверный запрос");
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}
