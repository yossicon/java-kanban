package manager;

import exceptions.ManagerSaveException;
import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String PATH_TO_FILE = "/resources";

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public FileBackedTaskManager() {
        file = new File(PATH_TO_FILE, "file.csv");
    }

    public void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic,start,duration\n");

            if (!Files.exists(file.toPath())) {
                Files.createFile(Paths.get(PATH_TO_FILE, "file.csv"));
            }

            for (Task task : getAllTasks()) {
                String taskToString = toString(task);
                bufferedWriter.write(taskToString + "\n");
            }

            for (Epic epic : getAllEpics()) {
                String taskToString = toString(epic);
                bufferedWriter.write(taskToString + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                String taskToString = toString(subtask);
                bufferedWriter.write(taskToString + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.isFile()) {
            file = new File(PATH_TO_FILE, "file.csv");
        }
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);

        try {
            List<String> strings = Files.readAllLines(file.toPath());
            for (int i = 1; i < strings.size(); i++) {
                String[] fields = strings.get(i).split(",");
                int id = Integer.parseInt(fields[0]);
                String name = fields[2];
                Status status = Status.valueOf(fields[3]);
                String description = fields[4];
                Duration duration = Duration.ofMinutes(Long.parseLong(fields[7]));

                if (id > fileBackedTaskManager.nextId) {
                    fileBackedTaskManager.nextId = id;
                }
                switch (TaskType.valueOf(fields[1])) {
                    case TASK:
                        Task task = new Task(id, name, description, status,
                                LocalDateTime.parse(fields[6], DateTimeUtil.FORMATTER), duration);
                        fileBackedTaskManager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = new Epic(id, name, description);
                        fileBackedTaskManager.epics.put(epic.getId(), epic);
                        fileBackedTaskManager.calculateEpicDuration(epic);
                        break;
                    case SUBTASK:
                        Subtask subtask = new Subtask(id, name, description, status,
                                LocalDateTime.parse(fields[6], DateTimeUtil.FORMATTER), duration, Integer.parseInt(fields[5]));
                        Epic epicOfSubtask = fileBackedTaskManager.epics.get(subtask.getEpicId());
                        fileBackedTaskManager.subtasks.put(subtask.getId(), subtask);
                        epicOfSubtask.addSubtask(subtask);
                        fileBackedTaskManager.calculateEpicDuration(epicOfSubtask);
                        fileBackedTaskManager.updateEpicStatus(epicOfSubtask);
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения", e);
        }
        return fileBackedTaskManager;
    }

    private String toString(Task task) {
        String epicId = "";
        String startTime;
        long duration;

        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        if (task.getStartTime() == null || task.getDuration() == null) {
            startTime = null;
            duration = 0;
        } else {
            startTime = task.getStartTime().format(DateTimeUtil.FORMATTER);
            duration = task.getDuration().toMinutes();
        }
        return String.format("%d,%s,%s,%s,%s,%s,%s,%d", task.getId(),
                task.getTaskType(),
                task.getName(),
                task.getStatus(), task.getDescription(), epicId, startTime, duration);
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public void deleteSubtaskById(Integer subtaskId) {
        super.deleteSubtaskById(subtaskId);
        save();
    }

    @Override
    public void deleteTaskById(Integer taskId) {
        super.deleteTaskById(taskId);
        save();
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        super.deleteEpicById(epicId);
        save();
    }
}



