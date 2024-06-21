package taskManager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {
    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldNotNullWhenGetDefault() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Экземпляр менеджера не проинициализирован");
    }

    @Test
    void shouldNotNullWhenGetHistoryDefault() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Экземпляр менеджера не проинициализирован");
    }
}