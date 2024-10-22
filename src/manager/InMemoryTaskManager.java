package manager;

import exceptions.TaskOverlapException;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected int nextId = 1;

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
        if (epic == null) {
            return null;
        }
        return epic.getSubtasksList();
    }

    //удаление всех задач
    @Override
    public void deleteAllTasks() {
        prioritizedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        prioritizedTasks.removeAll(subtasks.values());
        subtasks.clear();
        epics.values().forEach(this::updateEpicStatus);
    }

    //получение задач по идентификатору
    @Override
    public Task getTaskById(Integer taskId) {
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        historyManager.add(subtask);
        return subtask;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        if (isOverlap(task)) {
            throw new TaskOverlapException("Задача пересекается по времени выполнения с существующей задачей.");
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        calculateEpicDuration(epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (isOverlap(subtask)) {
            throw new TaskOverlapException("Задача пересекается по времени выполнения с существующей задачей.");
        }
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
        calculateEpicDuration(epic);
        addToPrioritized(subtask);
        return subtask;
    }

    //обновление задач
    @Override
    public Task updateTask(Task task) {
        Integer taskId = task.getId();
        Task previousTask = tasks.get(taskId);
        if (previousTask != null) {
            prioritizedTasks.remove(previousTask);
            if (isOverlap(task)) {
                prioritizedTasks.add(previousTask);
                throw new TaskOverlapException("Задача пересекается по времени выполнения с существующей задачей.");
            }
        }
        tasks.put(taskId, task);
        prioritizedTasks.add(task);
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
        Subtask previousSubtask = subtasks.get(subtaskId);

        if (previousSubtask != null) {
            prioritizedTasks.remove(previousSubtask);
            if (isOverlap(subtask)) {
                prioritizedTasks.add(previousSubtask);
                throw new TaskOverlapException("Задача пересекается по времени выполнения с существующей задачей.");
            }
        }
        subtasks.put(subtaskId, subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            List<Subtask> subtasksToUpdate = epic.getSubtasksList();

            for (Subtask updatedSubtask : subtasksToUpdate) {
                if (updatedSubtask.getId().equals(subtaskId)) {
                    int index = subtasksToUpdate.indexOf(updatedSubtask);
                    subtasksToUpdate.set(index, subtask);
                }
            }
            updateEpicStatus(epic);
            calculateEpicDuration(epic);
        }
        return subtask;
    }

    //удаление по идентификатору
    @Override
    public void deleteTaskById(Integer taskId) {
        Task task = tasks.get(taskId);
        if (task == null) {
            return;
        }
        prioritizedTasks.remove(task);
        tasks.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        for (Subtask subtask : epic.getSubtasksList()) {
            prioritizedTasks.remove(subtask);
            subtasks.remove(subtask.getId());
            historyManager.remove(subtask.getId());
        }
        epics.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask == null) {
            return;
        }
        Integer epicId = subtask.getEpicId();
        prioritizedTasks.remove(subtask);
        subtasks.remove(subtaskId);
        historyManager.remove(subtaskId);
        Epic epic = epics.get(epicId);
        epic.getSubtasksList().remove(subtask);
        updateEpicStatus(epic);
        calculateEpicDuration(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
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
            if (subtask.getStatus() == Status.IN_PROGRESS) {
                isAllDone = false;
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

    public void calculateEpicDuration(Epic epic) {
        List<Subtask> epicSubtasksList = epic.getSubtasksList();
        LocalDateTime startTime;
        LocalDateTime endTime;

        if (epicSubtasksList.isEmpty()) {
            return;
        }

        startTime = epicSubtasksList.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        epic.setStartTime(startTime);

        endTime = epicSubtasksList.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        epic.setEndTime(endTime);

        Duration duration = Duration.between(startTime, endTime);
        epic.setDuration(duration);
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private boolean isOverlap(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(prioritizedTask -> !(task.getStartTime().isAfter(prioritizedTask.getEndTime())
                        || task.getEndTime().isBefore(prioritizedTask.getStartTime())));
    }

    //генератор айди
    private int getNextId() {
        return nextId++;
    }
}

