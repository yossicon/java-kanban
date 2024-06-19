package taskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int nextId = 1;

    //получение списка всех задач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //получение подзадач определённого эпика
    @Override
    public List<Subtask> getSubtasksByEpic(Integer epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtasksList();
    }

    //удаление всех задач
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
                    updateEpicStatus(epic);
        }

    }

    //получение задач по идентификатору
    @Override
    public Task getTaskById(Integer taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    //обновление задач
    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        tasks.put(taskId, task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();
        epics.put(epicId, epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskId = subtask.getId();

        subtasks.put(subtaskId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        List<Subtask> subtasksUpdate = epic.getSubtasksList();

        for (Subtask previousSubtask : subtasksUpdate) {
            if (previousSubtask.getId().equals(subtaskId)) {
                int index = subtasksUpdate.indexOf(previousSubtask);
                subtasksUpdate.set(index, subtask);
            }
        }
        updateEpicStatus(epic);
        return subtask;
    }

    //удаление по идентификатору
    @Override
    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        for (Subtask subtask : epic.getSubtasksList()) {
                subtasks.remove(subtask.getId());
            }
        epics.remove(epicId);
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Integer epicId = subtask.getEpicId();
        subtasks.remove(subtaskId);
        Epic epic = epics.get(epicId);
        epic.getSubtasksList().remove(subtask);
        updateEpicStatus(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //обновление статуса эпика
    public void updateEpicStatus(Epic epic) {
        List<Subtask> epicSubtasksList = epic.getSubtasksList();
        boolean isAllNew = true;
        boolean isAllDone = true;

       if (epicSubtasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

       for (Subtask subtask : epicSubtasksList) {
           if (subtask.getStatus() == Status.NEW) {
               isAllDone = false;
           }
           if (subtask.getStatus() == Status.DONE) {
               isAllNew = false;
           }
       }

       if (isAllNew) {
           epic.setStatus(Status.NEW);
       } else if (isAllDone) {
           epic.setStatus(Status.DONE);
       } else {
           epic.setStatus(Status.IN_PROGRESS);
       }

    }

    //генератор айди
    private int getNextId() {
        return nextId++;
    }
}
