package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static task.Status.DONE;
import static task.Status.IN_PROGRESS;

class InMemoryTaskManagerTest {

    TaskManager taskManager;
    HistoryManager historyManager;
    Task task1;
    Task task2;
    Epic epic1;
    Epic epic2;
    Subtask subtask1;
    Subtask subtask2;


    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();

        task1 = new Task("Задача1", "Описание1");
        taskManager.createTask(task1);
        epic1 = new Epic("Эпик1", "Описание1");
        taskManager.createEpic(epic1);
        epic2 = new Epic("Эпик2", "Описание2");
        taskManager.createEpic(epic2);
        subtask1 = new Subtask("Подзадача1", "Описание1", epic1.getId());
        taskManager.createSubtask(subtask1);
        subtask2 = new Subtask(3, "Подзадача2", "Описание2", DONE, epic1.getId());
        taskManager.createSubtask(subtask2);
        task2 = new Task("Задача2", "Описание2");
        taskManager.createTask(task2);

    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void shouldCreateTask() {
        Task sameTask = taskManager.getTaskById(task1.getId());
        assertNotNull(task1, "Задача не найдена.");
        assertEquals(task1, sameTask, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(2, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldCreateEpicAndSubtask() {
        Epic sameEpic = taskManager.getEpicById(epic1.getId());
        Subtask sameSubtask = taskManager.getSubtaskById(subtask1.getId());

        assertNotNull(sameEpic, "Эпик не найден.");
        assertEquals(epic1, sameEpic, "Эпики не совпадают.");
        assertNotNull(sameSubtask, "Подзадача не найдена.");
        assertEquals(subtask1, sameSubtask, "Подзадачи не совпадают.");

        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(2, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают.");
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
    @Test
    void addAndGeneratedIdsShouldBeEqual() {
        Task generatedTask = new Task(1, "Задача1", "Описание1");

        assertEquals(task1.getId(), generatedTask.getId(), "Задачи конфликтуют");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void shouldNotChangeTaskWhenAddInManager() {
        historyManager.add(task1);
        List<Task> tasks = historyManager.getHistory();

        assertEquals(task1.getId(), tasks.get(0).getId(), "ID не равны");
        assertEquals(task1.getName(), tasks.get(0).getName(), "Названия не равны");
        assertEquals(task1.getDescription(), tasks.get(0).getDescription(), "Описания не равны");
        assertEquals(task1.getStatus(), tasks.get(0).getStatus(), "Статусы не равны");
    }

    @Test
    void shouldTrueWhenTaskDeletedById() {
        taskManager.deleteTaskById(task1.getId());

        assertNull(taskManager.getTaskById(task1.getId()), "Задача не удалена");
    }

    @Test
    void shouldTrueWhenEpicAndItsSubtaskDeletedById() {
        taskManager.deleteEpicById(epic1.getId());
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNull(taskManager.getEpicById(epic1.getId()), "Эпик не удален");
        assertTrue(subtasks.isEmpty(), "Подзадачи не удалены");
    }

    @Test
    void shouldTrueWhenAllTasksDeleted() {
        taskManager.deleteAllTasks();
        List<Task> tasks = taskManager.getAllTasks();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void shouldTrueWhenAllEpicsAndSubtasksDeleted() {
        taskManager.deleteAllSubtasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        taskManager.deleteAllEpics();
        List<Epic> epics = taskManager.getAllEpics();

        assertTrue(epics.isEmpty(), "Список эпиков не пуст");
        assertTrue(subtasks.isEmpty(), "Список подзадач не пуст");
    }

    @Test
    void shouldUpdateEpicStatus() {
        assertEquals(IN_PROGRESS, epic1.getStatus(), "Статус эпика неверен");

        taskManager.deleteSubtaskById(subtask1.getId());

        assertEquals(DONE, epic1.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldUpdateTask() {
        Task taskToUpdate = new Task(task1.getId(), "Новое имя", "Описание");
        taskManager.updateTask(taskToUpdate);
        List<Task> tasks = taskManager.getAllTasks();

        assertEquals("Новое имя", tasks.get(0).getName(), "Задача не обновлена");
    }

    @Test
    void shouldUpdateEpicAndSubtask() {
        Epic epicToUpdate = new Epic("Эпик1", "Новое описание");
        taskManager.updateEpic(epicToUpdate);
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals("Новое описание", epics.get(0).getDescription(), "Эпик не обновлен");

        Subtask subtaskToUpdate = new Subtask(subtask1.getId(), "Подзадача",
                "Описание", IN_PROGRESS, epic1.getId());
        taskManager.updateSubtask(subtaskToUpdate);
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(IN_PROGRESS, subtasks.get(0).getStatus(), "Подзадача не обновлена");
    }

    @Test
    void shouldReturnSubtasksByEpic() {
        List<Subtask> subtasksByEpic = new ArrayList<>();
        subtasksByEpic.add(subtask1);
        subtasksByEpic.add(subtask2);
        List<Subtask> subtasksByEpic1 = taskManager.getSubtasksByEpic(epic1.getId());

        assertEquals(subtasksByEpic, subtasksByEpic1, "Списки подзадач не равны");
    }

    @Test
    void shouldNotReturnDeletedSubtaskById() {
        taskManager.deleteSubtaskById(subtask1.getId());

        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Удаленная подзадача хранит старый айди");
    }
}
