package kanban.data;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(Integer id, String title, String description, TaskStatus status, Integer epicId) {
        super(id, title, description, status);
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
