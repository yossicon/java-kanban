package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("tempFile", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void afterEach() {
        tempFile.deleteOnExit();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список загруженных задач не пуст");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список загруженных эпиков не пуст");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список загруженных подзадач не пуст");
    }

    @Test
    void shouldSaveAndLoadTasks() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(taskManager.getAllTasks(), loadedManager.getAllTasks(),
                "Сохранённые и загруженные задачи не совпадают");
        assertEquals(taskManager.getAllEpics(), loadedManager.getAllEpics(),
                "Сохранённые и загруженные эпики не совпадают");
        assertEquals(taskManager.getAllSubtasks(), loadedManager.getAllSubtasks(),
                "Сохранённые и загруженные подзадачи не совпадают");
    }
}