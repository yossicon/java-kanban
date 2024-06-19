package taskManager;

import org.junit.jupiter.api.Test;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryHistoryManagerTest {
    HistoryManager historyManager = Managers.getDefaultHistory();
    Task task = new Task("Задача", "Описание задачи");

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void shouldAddTaskInHistory() {
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая");
        assertEquals(1, history.size(), "История пустая");
        assertEquals(task, history.getFirst(), "Данные задачи не сохранены при добавлении");
    }

    @Test
    void shouldReturn10Tasks() {
        for (int i = 0; i < 11; i++) {
            historyManager.add(task);
        }
        final List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size(), "В истории не 10 задач");
    }
}