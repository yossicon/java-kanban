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

public class HttpTaskManagerPrioritizedTest {
    TaskManager taskManager;
    HttpTaskServer taskServer;
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerPrioritizedTest() {
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
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Приоретизированный список задач не найден", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnCorrectPrioritizedTasks() throws IOException, InterruptedException {
        Task task = new Task("Имя", "Описание",
                LocalDateTime.of(1999, 1, 1, 12, 0), Duration.ofMinutes(20));
        taskManager.createTask(task);
        Epic epic = new Epic("Эпик1", "Описание1");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Имя", "Описание",
                LocalDateTime.of(1998, 1, 1, 12, 0),
                Duration.ofMinutes(20), epic.getId());
        taskManager.createSubtask(subtask);
        Task anotherTask = new Task("Имя", "Описание",
                LocalDateTime.of(2002, 1, 1, 12, 0), Duration.ofMinutes(20));
        taskManager.createTask(anotherTask);

        String prioritizedTasksJson = gson.toJson(List.of(subtask, task, anotherTask));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(prioritizedTasksJson, response.body(),
                "Тело ответа не соответсвует ожидаемому");
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
