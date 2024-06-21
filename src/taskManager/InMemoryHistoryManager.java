package taskManager;

import task.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyList = new LinkedList<>();
    private static final int MAX_HISTORY_LIST_SIZE = 10;

    @Override
    public void add(Task task) {
        if (historyList.size() == MAX_HISTORY_LIST_SIZE) {
            historyList.removeFirst();
        }
            historyList.add(task);
        }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyList);
    }
}
