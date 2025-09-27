package kanban.manager;

import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager{

    private HashMap<Integer, Task> idToTask = new HashMap<>();
    private HashMap<Integer, Epic> idToEpic = new HashMap<>();
    private HashMap<Integer, Subtask> idToSubtask = new HashMap<>();
    private int counter = 1;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private int nextId() {
        return counter++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(idToTask.values());
    }

    @Override
    public void deleteAllTasks() {
        idToTask.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = idToTask.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task newTask) {
        int newId = nextId();
        newTask.setId(newId);
        newTask.setStatus(TaskStatus.NEW);
        idToTask.put(newTask.getId(), newTask);
        return newTask;
    }

    @Override
    public void updateTask(Task task) {
        Task oldTask = idToTask.get(task.getId());
        if (oldTask != null) {
            historyManager.add(oldTask);
        }
        idToTask.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        idToTask.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(idToEpic.values());
    }

    @Override
    public void deleteAllEpics() {
        idToEpic.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = idToEpic.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic newEpic){
        int newId = nextId();
        newEpic.setId(newId);
        newEpic.setStatus(TaskStatus.NEW);
        idToEpic.put(newEpic.getId(),newEpic);
        return newEpic;
    }
    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = idToEpic.get(epic.getId());
        if (oldEpic != null) {
            historyManager.add(oldEpic);
        }
        updateEpicStatus(epic.getId());
        idToEpic.put(epic.getId(), epic);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = idToEpic.get(epicId);
        if (epic == null) return;

        List<Subtask> subtaskList = getSubtasksByEpicId(epicId);
        if (subtaskList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allDone = true;
        boolean allNew = true;

        for (Subtask s : subtaskList) {
            if (s.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (s.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
        }
        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        idToEpic.put(epicId, epic);
    }

    @Override
    public void deleteEpicById(int id) {
        if (idToEpic.containsKey(id)) {
            Epic epic = idToEpic.get(id);
            for (Integer subId : epic.getSubtaskIds()) {
                idToSubtask.remove(subId);
            }
            idToEpic.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(idToSubtask.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        for (Subtask s : idToSubtask.values()) {
            if (s.getEpicId() != null && s.getEpicId().equals(epicId)) {
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public void deleteAllSubtasks() {
        idToSubtask.clear();
        for (Epic epic : idToEpic.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
            Subtask subtask = idToSubtask.get(id);
            if (subtask != null) {
                historyManager.add(subtask);
            }
            return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask newSubtask) {
        int newId = nextId();
        newSubtask.setId(newId);
        newSubtask.setStatus(TaskStatus.NEW);

        idToSubtask.put(newSubtask.getId(), newSubtask);

        int epicId = newSubtask.getEpicId();
        if (idToEpic.containsKey(epicId)) {
            idToEpic.get(epicId).addSubtaskId(newId);
            updateEpicStatus(epicId);
        }
        return newSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = idToSubtask.get(subtask.getId());
        if (oldSubtask != null) {
            historyManager.add(oldSubtask);
        }
        int epicId = subtask.getEpicId();
        idToSubtask.put(subtask.getId(), subtask);
        if (idToEpic.containsKey(epicId)) {
            updateEpicStatus(epicId);
        }
    }
    @Override
    public void deleteSubtaskById(int id) {
        if (idToSubtask.containsKey(id)) {
            int epicId = idToSubtask.get(id).getEpicId();
            idToSubtask.remove(id);
            if (idToEpic.containsKey(epicId)) {
                idToEpic.get(epicId).removeSubtaskId(id);
                updateEpicStatus(epicId);
            }
        }
    }
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

}
