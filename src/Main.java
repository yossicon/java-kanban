import task.Epic;
import task.Subtask;
import task.Task;
import taskManager.Managers;
import taskManager.TaskManager;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = Managers.getDefault();

        System.out.println("Тест истории задач");
        System.out.println("Создаём две задачи, эпик с тремя подзадачами и эпик без подзадач");
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task1Created = taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        Task task2Created = taskManager.createTask(task2);
        Epic epic1 = new Epic("Эпик 1 (с 3 подзадачами)", "Описание эпика 1");
        Epic epic1Created = taskManager.createEpic(epic1);
        Subtask subtask1_1 = new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1Created.getId());
        Subtask subtask1_1Created = taskManager.createSubtask(subtask1_1);
        Subtask subtask1_2 = new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1Created.getId());
        Subtask subtask1_2Created = taskManager.createSubtask(subtask1_2);
        Subtask subtask1_3 = new Subtask("Подзадача 1-3", "Описание подзадачи 1-3",
                epic1Created.getId());
        Subtask subtask1_3Created = taskManager.createSubtask(subtask1_3);
        Epic epic2 = new Epic("Эпик 2 (без подзадач)", "Описание эпика 2");
        Epic epic2Created = taskManager.createEpic(epic2);

        System.out.println("Просматриваем задачи");
        taskManager.getTaskById(task2Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getEpicById(epic2Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask1_1Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask1_3Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(task1Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(task2Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getSubtaskById(subtask1_2Created.getId());
        System.out.println(taskManager.getHistory());
        taskManager.getTaskById(task1Created.getId());
        System.out.println(taskManager.getHistory());

        System.out.println("Удаляем задачу 1");
        taskManager.deleteTaskById(task1Created.getId());
        System.out.println("Проверяем, что задачи 1 нет в истории");
        System.out.println(taskManager.getHistory());

        System.out.println("Удаляем эпик с тремя подзадачами");
        taskManager.deleteEpicById(epic1Created.getId());
        System.out.println("Проверяем, что эпика и его подзадач нет в истории");
        System.out.println(taskManager.getHistory());
    }
}

