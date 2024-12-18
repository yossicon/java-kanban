package http.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.util.regex.Pattern;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        System.out.println("Началась обработка /history запроса от клиента.");
        String path = exchange.getRequestURI().getPath();
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("GET")) {
            try {
                if (Pattern.matches("^/history$", path)) {
                    if (!taskManager.getHistory().isEmpty()) {
                        String text = getGson().toJson(taskManager.getHistory());
                        sendText(exchange, text, 200);
                    } else {
                        sendNotFound(exchange, "История просмотров не найдена");
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
