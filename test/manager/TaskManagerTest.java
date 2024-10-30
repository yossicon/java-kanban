package manager;

import exceptions.TaskOverlapException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static task.Status.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected HistoryManager historyManager;
    protected Task task1;
    protected Task task2;
    protected Epic epic1;
    protected Epic epic2;
    protected Subtask subtask1;
    protected Subtask subtask2;

    @BeforeEach
    void beforeEach() {
        taskManager = createTaskManager();
        historyManager = Managers.getDefaultHistory();

        task1 = new Task("Задача1", "Описание1",
                LocalDateTime.of(2020, 11, 20, 22, 0), Duration.ofMinutes(20));
        taskManager.createTask(task1);
        epic1 = new Epic("Эпик1", "Описание1");
        taskManager.createEpic(epic1);
        epic2 = new Epic("Эпик2", "Описание2");
        taskManager.createEpic(epic2);
        subtask1 = new Subtask("Подзадача1", "Описание1", LocalDateTime.of(2022, 11,
                20, 18, 0), Duration.ofMinutes(20), epic1.getId());
        taskManager.createSubtask(subtask1);
        subtask2 = new Subtask(3, "Подзадача2", "Описание2", DONE,
                LocalDateTime.of(2022, 11, 20, 22, 0), Duration.ofMinutes(20),
                epic1.getId());
        taskManager.createSubtask(subtask2);
        task2 = new Task("Задача2", "Описание2",
                LocalDateTime.of(2023, 12, 1, 11, 0), Duration.ofMinutes(20));
        taskManager.createTask(task2);
    }

    protected abstract T createTaskManager();

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
    }

    @Test
    void shouldBeNewWhenAllNew() {
        subtask1.setStatus(NEW);
        subtask2.setStatus(NEW);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(NEW, epic1.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldBeDoneWhenAllDone() {
        subtask1.setStatus(DONE);
        subtask2.setStatus(DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(DONE, epic1.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldBeInProgressWhenNewAndDone() {
        subtask1.setStatus(NEW);
        subtask2.setStatus(DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(IN_PROGRESS, epic1.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldBeInProgressWhenAllInProgress() {
        subtask1.setStatus(IN_PROGRESS);
        subtask2.setStatus(IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);

        assertEquals(IN_PROGRESS, epic1.getStatus(), "Статус эпика неверен");
    }

    @Test
    void shouldUpdateTask() {
        Task taskToUpdate = new Task(task1.getId(), "Новое имя", "Описание",
                LocalDateTime.of(2030, 11, 20, 22, 0), Duration.ofMinutes(20));
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
                "Описание", IN_PROGRESS,
                LocalDateTime.of(2022, 11, 20, 18, 0),
                Duration.ofMinutes(20), epic1.getId());
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

    @Test
    void shouldFindOverlapCorrectly() {
        Task overlappingTask = new Task("Задача с пересечением", "Описание",
                LocalDateTime.of(2020, 11, 20, 22, 0), Duration.ofMinutes(20));
        Subtask overlappingSubtask = new Subtask("Подзадача1", "Описание1",
                LocalDateTime.of(2023, 12, 1, 11, 0),
                Duration.ofMinutes(20), epic1.getId());

        assertThrows(TaskOverlapException.class, () -> {
            taskManager.createTask(overlappingTask);
        }, "Добавление задачи с пересечением должно приводить к исключению");

        assertThrows(TaskOverlapException.class, () -> {
            taskManager.createSubtask(overlappingSubtask);
        }, "Добавление подзадачи с пересечением должно приводить к исключению");
    }

    @Test
    void shouldAddTaskToPrioritizedSetCorrectly() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();

        Task task = new Task("Имя", "Описание",
                LocalDateTime.of(1999, 1, 1, 12, 0), Duration.ofMinutes(20));
        taskManager.createTask(task);
        Subtask subtask = new Subtask("Имя", "Описание",
                LocalDateTime.of(1998, 1, 1, 12, 0),
                Duration.ofMinutes(20), epic1.getId());
        taskManager.createSubtask(subtask);
        Task anotherTask = new Task("Имя", "Описание",
                LocalDateTime.of(2002, 1, 1, 12, 0), Duration.ofMinutes(20));
        taskManager.createTask(anotherTask);

        TreeSet<Task> prioritizedTasks = (TreeSet<Task>) taskManager.getPrioritizedTasks();

        assertEquals(subtask, prioritizedTasks.first(), "Задача добавлена в приоритет неверно");
        assertEquals(anotherTask, prioritizedTasks.last(), "Задача добавлена в приоритет неверно");
    }

    @Test
    void shouldCalculateEpicDurationCorrectly() {
        assertEquals(subtask1.getStartTime(), epic1.getStartTime(),
                "Время старта эпика не совпадает с временем старта более ранней подзадачи");
        assertEquals(subtask2.getEndTime(), epic1.getEndTime(),
                "Время конца эпика не совпадает с временем конца более поздней подзадачи");
        assertEquals(260, epic1.getDuration().toMinutes());
    }
}

