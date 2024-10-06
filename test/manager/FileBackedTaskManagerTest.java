package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File tempFile = File.createTempFile("tempFile", ".csv");
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
        fileBackedTaskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список загруженных задач не пуст");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список загруженных эпиков не пуст");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список загруженных подзадач не пуст");
        tempFile.deleteOnExit();
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        File tempFile = File.createTempFile("tempFile", ".csv");
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
        Task task1 = fileBackedTaskManager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = fileBackedTaskManager.createTask(new Task("Задача 2", "Описание задачи 2"));
        Task task3 = fileBackedTaskManager.createTask(new Task("Задача 3", "Описание задачи 3"));
        Epic epic1 = fileBackedTaskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1.getId()));
        Subtask subtask2 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1.getId()));
        Epic epic2 = fileBackedTaskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(fileBackedTaskManager.getAllTasks(), loadedManager.getAllTasks(),
                "Сохранённые и загруженные задачи не совпадают");
        assertEquals(fileBackedTaskManager.getAllEpics(), loadedManager.getAllEpics(),
                "Сохранённые и загруженные эпики не совпадают");
        assertEquals(fileBackedTaskManager.getAllSubtasks(), loadedManager.getAllSubtasks(),
                "Сохранённые и загруженные подзадачи не совпадают");
    }
}