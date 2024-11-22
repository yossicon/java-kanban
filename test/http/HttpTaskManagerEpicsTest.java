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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
    TaskManager taskManager;
    HttpTaskServer taskServer;
    Epic epic;
    Epic epic1;
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerEpicsTest() {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        epic = new Epic("Эпик", "Описание эпика");
        epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnAllEpics() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        taskManager.createEpic(epic1);
        String allEpicsJson = gson.toJson(taskManager.getAllEpics());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(allEpicsJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        String epicJson = gson.toJson(taskManager.getEpicById(epic.getId()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(epicJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnSubtasksByEpic() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", LocalDateTime.now(),
                Duration.ofMinutes(5), epic.getId());
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(5), epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtask1);
        String allEpicSubtasksJson = gson.toJson(taskManager.getSubtasksByEpic(epic.getId()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(allEpicSubtasksJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldNotReturnNonExistentEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Эпик с id 1 не найден", response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldCreateEpic() throws IOException, InterruptedException {
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = taskManager.getAllEpics();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Эпик", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }

    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        taskManager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpicById(epic.getId()), "Эпик не удалён");
    }

    @Test
    public void shouldDeleteEpics() throws IOException, InterruptedException {
        taskManager.createEpic(epic);
        taskManager.createEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty(), "Эпики не удалены");
    }

    @Test
    public void shouldNotDeleteNonExistentEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Эпик с id 1 не найден", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnBadRequestWhenEndpointNonExistent() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/non-existent");
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
        String epicString = epic.toString();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Передан объект в неверном формате", response.body(),

                "Тело ответа не соответсвует ожидаемому");
    }
}
