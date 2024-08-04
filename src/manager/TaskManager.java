package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

public interface TaskManager {
    //получение списка всех задач
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    //получение подзадач определённого эпика
    List<Subtask> getSubtasksByEpic(Integer epicId);

    //удаление всех задач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    //получение задач по идентификатору
    Task getTaskById(Integer taskId);

    Epic getEpicById(Integer epicId);

    Subtask getSubtaskById(Integer subtaskId);

    //создание задач
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    //обновление задач
    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    //удаление по идентификатору
    void deleteTaskById(Integer taskId);

    void deleteEpicById(Integer epicId);

    void deleteSubtaskById(Integer subtaskId);

    List<Task> getHistory();
}
