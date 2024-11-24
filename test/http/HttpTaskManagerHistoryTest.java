package http;

import com.google.gson.Gson;
import http.handler.BaseHttpHandler;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerHistoryTest {
    TaskManager taskManager;
    HttpTaskServer taskServer;
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerHistoryTest() {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldNotFoundWhenEmptyHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("История просмотров не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnCorrectHistory() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        taskManager.createTask(task);
        Epic epic = new Epic("Подзадача", "Описание подзадачи");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Эпик", "Описание эпика", LocalDateTime.now(),
                Duration.ofMinutes(5), epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.getSubtaskById(subtask.getId());
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());
        taskManager.getTaskById(task.getId());

        String expectedHistory = gson.toJson(List.of(epic, subtask, task));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(expectedHistory, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnBadRequestWhenEndpointNonExistent() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history/non-existent");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Неверный запрос", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }
}
