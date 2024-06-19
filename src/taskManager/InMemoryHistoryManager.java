package taskManager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyList = new ArrayList<>();
    private static final int MAX_HISTORY_LIST_SIZE = 10;

    @Override
    public void add(Task task) {
        if (historyList.size() == MAX_HISTORY_LIST_SIZE) {
            historyList.remove(0);
        }
            historyList.add(task);
        }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}
