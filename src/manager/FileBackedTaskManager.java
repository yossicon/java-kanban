package manager;

import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("id,type,name,status,description,epic\n");


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
            throw new ManagerSaveException("Ошибка сохранения");
        }
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);

        try {
            List<String> strings = Files.readAllLines(file.toPath());
            for (int i = 1; i < strings.size(); i++) {
                String[] fields = strings.get(i).split(",");
                int id = Integer.parseInt(fields[0]);
                String name = fields[2];
                Status status = Status.valueOf(fields[3]);
                String description = fields[4];

                if (id > fileBackedTaskManager.nextId) {
                    fileBackedTaskManager.nextId = id;
                }
                switch (TaskType.valueOf(fields[1])) {
                    case TASK:
                        Task task = new Task(id, name, description, status);
                        fileBackedTaskManager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = new Epic(id, name, description);
                        fileBackedTaskManager.epics.put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        Subtask subtask = new Subtask(id, name, description, status, Integer.parseInt(fields[5]));
                        fileBackedTaskManager.subtasks.put(subtask.getId(), subtask);
                        fileBackedTaskManager.epics.get(subtask.getEpicId()).addSubtask(subtask);
                        break;
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения");
        }
        return fileBackedTaskManager;
    }

    private String toString(Task task) {
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }
        return String.format("%d,%s,%s,%s,%s,%s", task.getId(), task.taskType(), task.getName(), task.getStatus(),
                task.getDescription(), epicId);
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

    public static void main(String[] args) throws IOException {
        File tempFile = File.createTempFile("tempFile", ".csv");
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
        Task task1 = fileBackedTaskManager.createTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = fileBackedTaskManager.createTask(new Task("Задача 2", "Описание задачи 2"));
        Epic epic1 = fileBackedTaskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-1", "Описание подзадачи 1-1",
                epic1.getId()));
        Subtask subtask2 = fileBackedTaskManager.createSubtask(new Subtask("Подзадача 1-2", "Описание подзадачи 1-2",
                epic1.getId()));
        Epic epic2 = fileBackedTaskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2"));
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        System.out.println(loadedManager.getAllTasks());
        System.out.println(loadedManager.getAllEpics());
        System.out.println(loadedManager.getAllSubtasks());
        tempFile.deleteOnExit();
    }
}

