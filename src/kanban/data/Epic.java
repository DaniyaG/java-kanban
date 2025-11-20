package kanban.data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(Integer id, String title, String description, TaskStatus status) {
        super(id, title, description, status, Duration.ZERO, null);
        this.setType(TaskType.EPIC);
        this.endTime = null;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public boolean addSubtaskId(int subtaskId) {
        if (subtaskId == this.getId()) {
            return false;
        }
        subtaskIds.add(subtaskId);
        return true;
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

}

