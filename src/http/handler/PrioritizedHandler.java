package http.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("Началась обработка /prioritized запроса от клиента.");
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("GET")) {
            try {
                if (Pattern.matches("^/prioritized$", path)) {
                    if (!taskManager.getPrioritizedTasks().isEmpty()) {
                        String text = getGson().toJson(taskManager.getPrioritizedTasks());
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "Приоретизированный список задач не найден");
                    }
                } else {
                    sendBadRequest(exchange, "Неверный запрос");
                }
            } catch (Exception e) {
                sendInternalServerError(exchange);
            }
        } else {
            System.out.println("Запрос не соответствует ожидаемому (GET). Получен запрос: "
                    + requestMethod);
            sendMethodNotAllowed(exchange);
        }
        exchange.close();
    }
}