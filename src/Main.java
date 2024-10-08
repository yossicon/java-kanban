import manager.FileBackedTaskManager;
import manager.Managers;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        File file = new File("./src", "file.csv");

        FileBackedTaskManager fileBackedTaskManager = Managers.getFileBackedTaskManager(file);

        Task task1 = fileBackedTaskManager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = fileBackedTaskManager.createTask(new Task("Задача 2", "Описание задачи 2"));
        Epic epic1 = fileBackedTaskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1.getId()));
        Subtask subtask2 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1.getId()));
        Epic epic2 = fileBackedTaskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
        file.delete();
    }
}