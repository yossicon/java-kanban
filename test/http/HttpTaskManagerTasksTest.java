package http;

import com.google.gson.Gson;
import http.handler.BaseHttpHandler;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    TaskManager taskManager;
    HttpTaskServer taskServer;
    Task task;
    Task task1;
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerTasksTest() {
    }

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        task = new Task("Задача", "Описание задачи", LocalDateTime.now(), Duration.ofMinutes(5));
        task1 = new Task("Задача 1", "Описание задачи 1", LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(5));
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void shouldReturnAllTasks() throws IOException, InterruptedException {
        taskManager.createTask(task);
        taskManager.createTask(task1);
        String allTasksJson = gson.toJson(taskManager.getAllTasks());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(allTasksJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        taskManager.createTask(task);
        String taskJson = gson.toJson(taskManager.getTaskById(task.getId()));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(taskJson, response.body(), "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldNotReturnNonExistentTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Задача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldCreateTask() throws IOException, InterruptedException {
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Задача", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task taskUpdate = new Task(task.getId(), "Новое имя", "Описание задачи", Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        String taskUpdateJson = gson.toJson(taskUpdate);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertEquals("Новое имя", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldNotUpdateNonExistentTask() throws IOException, InterruptedException {
        String taskUpdateJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Задача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        taskManager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(taskManager.getTaskById(task.getId()), "Задача не удалена");
    }

    @Test
    public void shouldDeleteTasks() throws IOException, InterruptedException {
        taskManager.createTask(task);
        taskManager.createTask(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertTrue(taskManager.getAllTasks().isEmpty(), "Задачи не удалены");
    }

    @Test
    public void shouldNotDeleteNonExistentTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        assertTrue(taskManager.getAllTasks().isEmpty(), "Задачи не удалены");
        assertEquals(404, response.statusCode());
        assertEquals("Задача с id 1 не найдена", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldNotCreateTaskWhenHasInteractions() throws IOException, InterruptedException {
        taskManager.createTask(task);
        Task interactionTask = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW, LocalDateTime.now(),
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(interactionTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующей", response.body(),
                "Тело ответа не соответсвует ожидаемому");

    }

    @Test
    public void shouldNotUpdateTaskWhenHasInteractions() throws IOException, InterruptedException {
        taskManager.createTask(task);
        taskManager.createTask(task1);
        Task interactionTask = new Task(1, "Новое имя", "Описание задачи", Status.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(5));
        String taskUpdateJson = gson.toJson(interactionTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskUpdateJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertEquals("Задача пересекается по времени с существующей", response.body(),
                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnBadRequestWhenBodyIsEmpty() throws IOException, InterruptedException {
        String emptyString = "";
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(emptyString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Тело запроса пустое", response.body(),

                "Тело ответа не соответсвует ожидаемому");
    }

    @Test
    public void shouldReturnBadRequestWhenEndpointNonExistent() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/non-existent");
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
        String taskString = task.toString();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskString))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Передан объект в неверном формате", response.body(),

                "Тело ответа не соответсвует ожидаемому");
    }
}