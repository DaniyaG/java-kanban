package kanban.manager;

import kanban.data.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();
    private static final int HISTORY_LIMIT = 10;

    @Override
    public void add(Task task) {
        history.add(task);
        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}