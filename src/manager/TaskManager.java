package manager;
import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
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
    public List<Subtask> getSubtasksByEpic(Integer epicId) {
        Epic epic = epics.get(epicId);
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

        for (Epic epic : epics.values()) {
                    updateEpicStatus(epic);
        }

    }

    //получение задач по идентификатору
    public Task getTaskById (Integer taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpicById (Integer epicId) {
        return epics.get(epicId);
    }

    public Subtask getSubtaskById (Integer subtaskId) {
        return subtasks.get(subtaskId);
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
        tasks.put(taskId, task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();
        epic.setSubtasksList(epics.get(epicId).getSubtasksList());
        updateEpicStatus(epic);
        epics.put(epicId, epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskId = subtask.getId();

        subtasks.put(subtaskId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        ArrayList<Subtask> subtasksUpdate = epic.getSubtasksList();

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
    public void deleteTaskById(Integer taskId) {
        tasks.remove(taskId);
    }

    public void deleteEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        for (Subtask subtask : epic.getSubtasksList()) {
                subtasks.remove(subtask.getId());
            }
        epics.remove(epicId);
    }

    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        Integer epicId = subtask.getEpicId();
        subtasks.remove(subtaskId);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtasksList = epic.getSubtasksList();
        subtasksList.remove(subtask);
        epic.setSubtasksList(subtasksList);
        updateEpicStatus(epic);
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
