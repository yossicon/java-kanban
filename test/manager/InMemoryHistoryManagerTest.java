package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager;
    Task task;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task = new Task(1, "Задача", "Описание задачи");
        historyManager.add(task);
    }


    @Test
    void shouldAddTaskInHistory() {
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "История пустая");
    }


    @Test
    void shouldRemoveNode() {
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая");

        historyManager.remove(task.getId());
        final List<Task> history1 = historyManager.getHistory();
        int history1Size = history1.size();
        assertEquals(0, history1Size, "История не пустая");
    }

}