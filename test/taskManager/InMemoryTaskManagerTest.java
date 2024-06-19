package taskManager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static task.Status.DONE;
import static task.Status.IN_PROGRESS;

class InMemoryTaskManagerTest {

    TaskManager taskManager = Managers.getDefault();

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void shouldCreateTask() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        Task task1 = taskManager.getTaskById(task.getId());
        assertNotNull(task1, "Задача не найдена.");
        assertEquals(task, task1, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void shouldCreateEpicAndSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        Epic epic1 = taskManager.getEpicById(epic.getId());
        Subtask subtask1 = taskManager.getSubtaskById(subtask.getId());

        assertNotNull(epic1, "Эпик не найден.");
        assertEquals(epic, epic1, "Эпики не совпадают.");
        assertNotNull(subtask1, "Подзадача не найдена.");
        assertEquals(subtask, subtask1, "Подзадачи не совпадают.");

        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
    @Test
    void addAndGeneratedIdsShouldBeEqual() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        Task task1 = new Task(1, "Задача1", "Описание1");

        assertEquals(task.getId(), task1.getId(), "Задачи конфликтуют");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void shouldNotChangeTaskWhenAddInManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        historyManager.add(task);
        List<Task> tasks = historyManager.getHistory();

        assertEquals(task.getId(), tasks.getFirst().getId(), "ID не равны");
        assertEquals(task.getName(), tasks.getFirst().getName(), "Названия не равны");
        assertEquals(task.getDescription(), tasks.getFirst().getDescription(), "Описания не равны");
        assertEquals(task.getStatus(), tasks.getFirst().getStatus(), "Статусы не равны");
    }

    @Test
    void shouldTrueWhenTaskDeletedById() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        List<Task> tasks = taskManager.getAllTasks();

        assertTrue(tasks.isEmpty(), "Список задач не пуст");
    }

    @Test
    void shouldTrueWhenEpicAndItsSubtaskDeletedById() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.deleteEpicById(epic.getId());
        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertTrue(epics.isEmpty(), "Список эпиков не пуст");
        assertTrue(subtasks.isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldTrueWhenAllTasksDeleted() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        Task task1 = new Task("Задача1", "Описание1");
        taskManager.createTask(task1);
        taskManager.deleteAllTasks();
        List<Task> tasks = taskManager.getAllTasks();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void shouldTrueWhenAllEpicsAndSubtasksDeleted() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        Epic epic1 = new Epic("Эпик1", "Описание1");
        taskManager.createEpic(epic1);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        Subtask subtask1 = new Subtask("Подзадача1", "Описание1", epic.getId());
        taskManager.createSubtask(subtask1);

        taskManager.deleteAllSubtasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        taskManager.deleteAllEpics();
        List<Epic> epics = taskManager.getAllEpics();

        assertTrue(epics.isEmpty(), "Список эпиков не пуст");
        assertTrue(subtasks.isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldUpdateEpicStatus() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        Subtask subtask1 = new Subtask(3,"Подзадача1", "Описание1", DONE, epic.getId());
        taskManager.createSubtask(subtask1);

        assertEquals(IN_PROGRESS, epic.getStatus(), "Статус эпика неверен");

        taskManager.deleteSubtaskById(subtask.getId());

        assertEquals(DONE, epic.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Задача", "Описание");
        taskManager.createTask(task);
        Task task1 = new Task(task.getId(), "Новое имя", "Описание");
        taskManager.updateTask(task1);
        List<Task> tasks = taskManager.getAllTasks();

        assertEquals("Новое имя", tasks.getFirst().getName(), "Задача не обновлена");
    }

    @Test
    void shouldUpdateEpicAndSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);
        Epic epic1 = new Epic("Эпик1", "Новое описание");
        taskManager.updateEpic(epic1);
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals("Новое описание", epics.getFirst().getDescription(), "Эпик не обновлен");

        Subtask subtask = new Subtask("Подзадача", "Описание", epic1.getId());
        taskManager.createTask(subtask);
        Subtask subtask1 = new Subtask(subtask.getId(), "Подзадача",
                "Описание", IN_PROGRESS, epic1.getId());
        taskManager.updateSubtask(subtask1);
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(IN_PROGRESS, subtasks.getFirst().getStatus(), "Подзадача не обновлена");
    }

    @Test
    void shouldReturnSubtasksByEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        taskManager.createSubtask(subtask);
        Subtask subtask1 = new Subtask("Подзадача1", "Описание1", epic.getId());
        taskManager.createSubtask(subtask1);
        List<Subtask> subtasksByEpic = new ArrayList<>();
        subtasksByEpic.add(subtask);
        subtasksByEpic.add(subtask1);
        List<Subtask> subtasksByEpic1 = taskManager.getSubtasksByEpic(epic.getId());

        assertEquals(subtasksByEpic, subtasksByEpic1, "Списки подзадач не равны");
    }
}
