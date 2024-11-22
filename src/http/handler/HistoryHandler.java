package http.handler;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;

import java.io.IOException;
import java.util.regex.Pattern;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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
            exchange.sendResponseHeaders(405, 0);
        }
        exchange.close();
    }
}
