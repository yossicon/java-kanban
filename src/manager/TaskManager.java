package manager;
import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TaskManager {
    final private Map<Integer, Task> tasks = new HashMap<>();
    final private Map<Integer, Epic> epics = new HashMap<>();
    final private Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    //получение списка всех задач
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    //получение подзадач определённого эпика
    public List<Subtask> getSubtasksByEpic(Epic epic) {
        return epic.getSubtasksList();
    }

    //удаление всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();

        if (!epics.isEmpty()) {
            for (Epic epic : epics.values()) {
                    updateEpicStatus(epic);
            }
        }
    }

    //получение задач по идентификатору
    public Task getTaskById (Integer taskId) {
        Task task = tasks.get(taskId);

        if (task == null || !tasks.containsKey(taskId)) {
            return null;
        }
        return task;
    }

    public Epic getEpicById (Integer epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null || !tasks.containsKey(epicId)) {
            return null;
        }
        return epic;
    }

    public Subtask getSubtaskById (Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);

        if (subtask == null || !tasks.containsKey(subtaskId)) {
            return null;
        }
        return subtask;
    }

    //создание задач
    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        return subtask;
    }

    //обновление задач
    public Task updateTask(Task task) {
        Integer taskId = task.getId();

        if (taskId == null || !tasks.containsKey(taskId)) {
            return null;
        }
        tasks.put(taskId, task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();

        if (epicId == null || !epics.containsKey(epicId)) {
            return null;
        }
        epic.setSubtasksList(epics.get(epicId).getSubtasksList());
        updateEpicStatus(epic);
        epics.put(epicId, epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskId = subtask.getId();

        if (subtaskId == null || !subtasks.containsKey(subtaskId)) {
            return null;
        }
        subtasks.put(subtaskId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        List<Subtask> subtasksUpdate = epic.getSubtasksList();

        for (Subtask previousSubtask : subtasksUpdate) {
            if (previousSubtask.getId().equals(subtaskId)) {
                int index = subtasksUpdate.indexOf(previousSubtask);
                subtasksUpdate.set(index, subtask);
            }
        }
        epic.setSubtasksList(subtasksUpdate);
        updateEpicStatus(epic);
        return subtask;
    }

    //удаление по идентификатору
    public boolean deleteTaskById(Integer taskId) {
        boolean isDeleted = false;
        Task task = tasks.get(taskId);

        if (task != null) {
            tasks.remove(taskId);
            isDeleted = true;
        }
        return isDeleted;
    }

    public boolean deleteEpicById(Integer epicId) {
        boolean isDeleted = false;
        Epic epic = epics.get(epicId);

        if (epic != null) {
            for (Subtask subtask : epic.getSubtasksList()) {
                subtasks.remove(subtask.getId());
            }
            epics.remove(epicId);
            isDeleted = true;
        }
        return isDeleted;

    }

    public boolean deleteSubtaskById(Integer subtaskId) {
        boolean isDeleted = false;
        Subtask subtask = subtasks.get(subtaskId);

        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                //epic.removeSubtask(subtask);
                subtasks.remove(subtaskId);
                updateEpicStatus(epic);
                isDeleted = true;
            }
        }
        return isDeleted;

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
