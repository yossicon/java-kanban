package taskManager;

import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {

    HistoryManager historyManager = Managers.getDefaultHistory();
    Task task = new Task(1, "Задача", "Описание задачи");


    @Test
    void shouldAddTaskInHistory() {
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "История пустая");
    }


    @Test
    void shouldRemoveNode() {
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая");

        historyManager.remove(task.getId());
        final List<Task> history1 = historyManager.getHistory();
        int history1Size = history1.size();
        assertEquals(0, history1Size, "История не пустая");
    }

}