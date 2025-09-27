package kanban.manager;

import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTaskById(int id);
    Task createTask(Task newTask);
    void updateTask(Task task);
    void deleteTaskById(int id);

    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicById(int id);
    Epic createEpic(Epic newEpic);
    void updateEpic(Epic epic);
    void deleteEpicById(int id);

    List<Subtask> getAllSubtasks();
    List<Subtask> getSubtasksByEpicId(int epicId);
    void deleteAllSubtasks();
    Subtask getSubtaskById(int id);
    Subtask createSubtask(Subtask newSubtask);
    void updateSubtask(Subtask subtask);
    void deleteSubtaskById(int id);

    List<Task> getHistory();

}

