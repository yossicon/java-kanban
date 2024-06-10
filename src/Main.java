import manager.TaskManager;
import task.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        System.out.println("Тест 1");
        List<Task> tasks = taskManager.getAllTasks();
        System.out.println("Список задач пуст: " + tasks.isEmpty());
        System.out.println();

        System.out.println("Тест 2");
        System.out.println("Создаём 2 задачи");
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task1Created = taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        Task task2Created = taskManager.createTask(task2);
        System.out.println("Созданная задача 1 содержит id: " + (task1Created.getId() != null));
        System.out.println("Созданная задача 2 содержит id: " + (task2Created.getId() != null));
        System.out.println("Список задач содержит добавленные задачи: ");
        System.out.println(taskManager.getAllTasks());
        System.out.println();

        System.out.println("Тест 3");
        List<Epic> epics = taskManager.getAllEpics();
        System.out.println("Список эпиков пуст: " + epics.isEmpty());
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        System.out.println("Список подзадач пуст: " + subtasks.isEmpty());
        System.out.println();

        System.out.println("Тест 4");
        System.out.println("Создаём эпик с двумя подзадачами");
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic1Created = taskManager.createEpic(epic1);
        Subtask subtask1_1 = new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1Created.getId());
        Subtask subtask1_1Created = taskManager.createSubtask(subtask1_1);
        Subtask subtask1_2 = new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1Created.getId());
        Subtask subtask1_2Created = taskManager.createSubtask(subtask1_2);
        System.out.println("Созданный эпик содержит id: " + (epic1Created.getId() != null));
        System.out.println("Созданные подзадачи содержат id: " + ((subtask1_1Created.getId() != null)
                && (subtask1_2Created.getId() != null)));
        System.out.println("Список эпиков содержит добавленный эпик: ");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Список подзадач содержит добавленные подзадачи: ");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("Тест 5");
        System.out.println("Создаём эпик с одной подзадачей");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        Epic epic2Created = taskManager.createEpic(epic2);
        Subtask subtask2_1 = new Subtask("Подзадача 2-1", "Описание подзадачи 2-1",
                epic2Created.getId());
        Subtask subtask2_1Created = taskManager.createSubtask(subtask2_1);
        System.out.println("Созданный эпик содержит id: " + (epic2Created.getId() != null));
        System.out.println("Созданная подзача содержит id: " + (subtask2_1Created.getId() != null));
        System.out.println("Список эпиков содержит добавленный эпик: ");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Список подзадач содержит добавленную подзадачу: ");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();


        System.out.println("Тест 6");
        System.out.println("Обновляем задачу 1");
        task1Created.setStatus(Status.IN_PROGRESS);
        System.out.println("Задача 1 обновлена: " + taskManager.updateTask(task1Created));
        System.out.println();

        System.out.println("Тест 7");
        System.out.println("Обновляем эпик");
        epic1Created.setName("Новое имя эпика 1");
        epic1Created.setDescription("Новое описание эпика 1");
        System.out.println("Эпик с обновлёнными полями: " + taskManager.updateEpic(epic1Created));
        System.out.println();

        System.out.println("Тест 8");
        System.out.println("Обновляем подзадачу");
        subtask1_1Created.setStatus(Status.DONE);
        System.out.println("Подзадача 1-1 обновлена: " + taskManager.updateSubtask(subtask1_1Created));
        System.out.println("В списке подзадач эпика 1 подзадача 1-1 обновлена: ");
        System.out.println(taskManager.getSubtasksByEpic(epic1Created.getId()));
        System.out.println("Статус эпика 1 рассчитался верно (ожидается IN_PROGRESS): " + epic1Created.getStatus());
        System.out.println();

        System.out.println("Тест 9");
        System.out.println("Удаляем задачу по id");
        taskManager.deleteTaskById(task1Created.getId());
        System.out.println("Задача 1 удалена из списка задач: " + taskManager.getAllTasks());
        System.out.println();

        System.out.println("Тест 10");
        System.out.println("Удаляем подзадачу по id");
        taskManager.deleteSubtaskById(subtask1_1Created.getId());
        System.out.println("Подзадача 1-1 удалена из списка подзадач: " + taskManager.getAllSubtasks());
        System.out.print("Подзадача удалена из списка задач эпика 1: ");
        System.out.println(taskManager.getSubtasksByEpic(subtask1_1Created.getEpicId()));
        System.out.println("Статус эпика 1 рассчитался верно (ожидается NEW): " + epic1Created.getStatus());
        System.out.println();

        System.out.println("Тест 11");
        System.out.println("Удаляем эпик по id");
        taskManager.deleteEpicById(epic1Created.getId());
        System.out.println("Эпик 1 удалён из списка эпиков: " + taskManager.getAllEpics());
    }
}
