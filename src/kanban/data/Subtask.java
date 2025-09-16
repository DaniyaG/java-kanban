package kanban.data;

public class Subtask extends Task {
    private Integer epicId;

    public Subtask(Integer id, String title, String description, TaskStatus status, Integer epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }



}
