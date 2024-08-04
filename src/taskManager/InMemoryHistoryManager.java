package taskManager;

import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private Node head;
    private Node tail;
    private final HashMap<Integer, Node> nodeMap = new HashMap<>();

    @Override
    public void add(Task task) {

        if (task != null) {
            remove(task.getId());
            linkLast(task);
        }
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);

        if (node == null) {
            return;
        }
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {

        Node oldTail = tail;
        Node node = new Node(oldTail, null, task);
        tail = node;

        if (oldTail == null) {
            head = node;
        } else {
            oldTail.next = node;
        }
        nodeMap.put(task.getId(), node);
    }

    private List<Task> getTasks() {
        List<Task> historyList = new ArrayList<>();
        Node node = head;
        while (node != null) {
            historyList.add(node.value);
            node = node.next;
        }
        return historyList;
    }

    private void removeNode(Node node) {

        if (node != null) {
            Node prev = node.prev;
            Node next = node.next;
            node.value = null;
            if (head == node && tail == node) {
                head = null;
                tail = null;
            } else if (head == node) {
                head = head.next;
                head.prev = null;
            } else if (tail == node) {
                tail = tail.prev;
                tail.next = null;
            } else {
                prev.next = next;
                next.prev = prev;

            }
        }
    }
}
