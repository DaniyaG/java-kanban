package kanban.data;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(Integer id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

    public List<Integer> getSubtaskIds() {

        return new ArrayList<>(subtaskIds);
    }

    public boolean addSubtaskId(int subtaskId) {
        if (subtaskId == this.getId()) {
            return false;
        }
        this.subtaskIds.add(subtaskId);
        return true;
    }
    public void removeSubtaskId(int subtaskId) {

        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

}

