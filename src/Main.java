import taskManager.Managers;
import taskManager.TaskManager;

import task.Epic;
import task.Subtask;
import task.Task;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        System.out.println("Тест истории задач");
        System.out.println("Создаём несколько задач, эпиков и подзадач");
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task1Created = taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        Task task2Created = taskManager.createTask(task2);
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic1Created = taskManager.createEpic(epic1);
        Subtask subtask1_1 = new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1Created.getId());
        Subtask subtask1_1Created = taskManager.createSubtask(subtask1_1);
        Subtask subtask1_2 = new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1Created.getId());
        Subtask subtask1_2Created = taskManager.createSubtask(subtask1_2);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        Epic epic2Created = taskManager.createEpic(epic2);
        Subtask subtask2_1 = new Subtask("Подзадача 2-1", "Описание подзадачи 2-1",
                epic2Created.getId());
        Subtask subtask2_1Created = taskManager.createSubtask(subtask2_1);
        System.out.println("Просматриваем 11 задач");
        taskManager.getTaskById(task2Created.getId());
        taskManager.getEpicById(epic2Created.getId());
        taskManager.getSubtaskById(subtask1_1Created.getId());
        taskManager.getSubtaskById(subtask1_2Created.getId());
        taskManager.getEpicById(epic2Created.getId());
        taskManager.getTaskById(task1Created.getId());
        taskManager.getSubtaskById(subtask2_1Created.getId());
        taskManager.getTaskById(task2Created.getId());
        taskManager.getEpicById(epic2Created.getId());
        taskManager.getSubtaskById(subtask1_1Created.getId());
        taskManager.getTaskById(task1Created.getId());
        System.out.println("Ожидается список из 10 последних просмотренных задач:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}

