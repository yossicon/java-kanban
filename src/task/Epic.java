package task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Subtask> subtasksList = new ArrayList<>();
    private final TaskType taskType = TaskType.EPIC;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(Integer id, String name, String description) {
        super(id, name, description, Status.NEW);
    }

    public void addSubtask(Subtask subtask) {
        subtasksList.add(subtask);
    }

    public List<Subtask> getSubtasksList() {
        return subtasksList;
    }

    @Override
    public TaskType getTaskType() {
        return taskType;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasksList=" + getSubtasksList() +
                ", taskType=" + getTaskType() +
                '}';
    }

}
