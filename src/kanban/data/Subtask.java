package kanban.data;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(Integer id, String title, String description, TaskStatus status, Integer epicId, Duration duration, LocalDateTime startTime) {
        super(id, title, description, status, duration,startTime);
        this.setType(TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public Integer getEpicId() {

        return epicId;
    }

    public boolean updateEpicId(Integer epicId) {
        if (epicId != null && this.id != null && this.id.equals(epicId)) {
            return false;
        }
        this.epicId = epicId;
        return true;
    }
}
