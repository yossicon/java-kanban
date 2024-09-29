package manager;

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
            oldTail.setNext(node);
        }
        nodeMap.put(task.getId(), node);
    }

    private List<Task> getTasks() {
        List<Task> historyList = new ArrayList<>();
        Node node = head;
        while (node != null) {
            historyList.add(node.getValue());
            node = node.getNext();
        }
        return historyList;
    }

    private void removeNode(Node node) {

        Node prev = node.getPrev();
        Node next = node.getNext();
        if (head == node && tail == node) {
            head = null;
            tail = null;
        } else if (head == node) {
            head = head.getNext();
            head.setPrev(null);
        } else if (tail == node) {
            tail = tail.getPrev();
            tail.setNext(null);
        } else {
            prev.setNext(next);
            next.setPrev(prev);
        }
    }
}
