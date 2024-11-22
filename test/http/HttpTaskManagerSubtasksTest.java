package http;

import com.google.gson.Gson;
import http.handler.BaseHttpHandler;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {
    TaskManager taskManager;
    HttpTaskServer taskServer;
    Gson gson = BaseHttpHandler.getGson();
    Epic epic;
    Subtask subtask;
    Subtask subtask1;

    public HttpTaskManagerSubtasksTest() {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        subtask = new Subtask("Подзадача", "Описание подзадачи", LocalDateTime.now(),
                Duration.ofMinutes(5), epic.getId());
        subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(5), epic.getId());
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnAllSubtasks() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        String allSubtasksJson = gson.toJson(taskManager.getAllSubtasks());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(allSubtasksJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnSubtaskById() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        String subtaskJson = gson.toJson(taskManager.getSubtaskById(subtask.getId()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(subtaskJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldNotReturnNonExistentSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Подзадача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldCreateSubtask() throws IOException, InterruptedException {
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Подзадача", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void shouldCreateSubtaskWhenEpicNonExistent() throws IOException, InterruptedException {
        taskManager.deleteAllEpics();
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertEquals("Эпик для создания подзадачи не найден", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        Subtask subtaskUpdate = new Subtask(subtask.getId(), "Новое имя", "Описание подзадачи", Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(5), epic.getId());
        String subtaskUpdateJson = gson.toJson(subtaskUpdate);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getAllSubtasks();

        assertEquals("Новое имя", subtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldNotUpdateNonExistentSubtask() throws IOException, InterruptedException {
        String subtaskUpdateJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Подзадача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не удалена");
    }

    @Test
    public void shouldDeleteSubtasks() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи не удалены");
    }

    @Test
    public void shouldNotDeleteNonExistentSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Задачи не удалены");
        assertEquals(404, response.statusCode());
        assertEquals("Подзадача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldNotCreateSubtaskWhenHasInteractions() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        Subtask interactionSubtask = new Subtask(1, "Подзадача", "Описание подзадачи", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5), epic.getId());
        String subtaskJson = gson.toJson(interactionSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Подзадача пересекается по времени с существующей", response.body(),
                "Тело ответа не соответсвует ожидаемому");

    }

    @Test
    public void shouldNotUpdateSubtaskWhenHasInteractions() throws IOException, InterruptedException {
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        Subtask interactionsubtask = new Subtask(subtask.getId(), "Новое имя", "Описание подзадачи", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(5), epic.getId());
        String subtaskUpdateJson = gson.toJson(interactionsubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Подзадача пересекается по времени с существующей", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnBadRequestWhenEndpointNonExistent() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/non-existent");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Неверный запрос", response.body(),
                "Тело ответа не соответсвует ожидаемому");

    }

    @Test
    public void shouldReturnBadRequestWhenNotJson() throws IOException, InterruptedException {
        String subtaskString = subtask.toString();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Передан объект в неверном формате", response.body(),

                "Тело ответа не соответсвует ожидаемому");
    }
}
