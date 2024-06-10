package task;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasksList = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(Integer id, String name, String description) {
        super(id, name, description);
    }

    public void addSubtask(Subtask subtask) {
        subtasksList.add(subtask);
    }

    public ArrayList<Subtask> getSubtasksList() {
        return subtasksList;
    }

    public void setSubtasksList(ArrayList<Subtask> subtasksList) {
        this.subtasksList = subtasksList;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasksList=" + getSubtasksList() +
                '}';
    }

}
