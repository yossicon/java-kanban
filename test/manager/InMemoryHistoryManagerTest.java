package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;
    Task task1;
    Task task2;
    Task task3;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task(1, "Задача", "Описание задачи", Status.NEW,
                LocalDateTime.of(2024, 10, 19, 22, 0), Duration.ofMinutes(20));
        task2 = new Task(2, "Задача1", "Описание1", Status.NEW,
                LocalDateTime.of(2020, 11, 20, 22, 0), Duration.ofMinutes(20));
        task3 = new Task(3, "Задача1", "Описание1", Status.NEW,
                LocalDateTime.of(2021, 11, 20, 22, 0), Duration.ofMinutes(20));
    }

    @Test
    void shouldAddTaskInHistory() {
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "Неверный размер истории");
    }

    @Test
    void shouldRemoveNode() {
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История пустая");

        historyManager.remove(task1.getId());
        final List<Task> history1 = historyManager.getHistory();
        int history1Size = history1.size();

        assertEquals(0, history1Size, "История не пустая");
    }

    @Test
    void shouldRemoveNodeFromMid() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        final List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "Неверный размер истории");

        historyManager.remove(task2.getId());
        final List<Task> history1 = historyManager.getHistory();

        assertEquals(2, history1.size(), "Неверный размер истории");

    }

    @Test
    void shouldRemoveNodeFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        final List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "Неверный размер истории");

        historyManager.remove(task3.getId());
        final List<Task> history1 = historyManager.getHistory();

        assertEquals(2, history1.size(), "Неверный размер истории");
    }

    @Test
    void shouldNotDoubleTaskInHistory() {
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Неверный размер истории");

        historyManager.add(task1);
        final List<Task> history1 = historyManager.getHistory();

        assertEquals(1, history1.size(), "Неверный размер истории");
    }
}