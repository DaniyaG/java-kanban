package javakanban.manager;

import javakanban.data.Epic;
import javakanban.data.Subtask;
import javakanban.data.Task;
import javakanban.data.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;


public class TaskManager {
    private HashMap<Integer, Task> idToTask = new HashMap<>();
    private HashMap<Integer, Epic> idToEpic = new HashMap<>();
    private HashMap<Integer, Subtask> idToSubtask = new HashMap<>();
    private int counter = 1;

    private int nextId() {
        return counter++;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(idToTask.values());
    }
    public void deleteAllTasks() {
        idToTask.clear();
    }
    public Task getTaskById(int id) {
        return idToTask.get(id);
    }

    public Task createTask(Task newTask) {
        int newId = nextId();
        newTask.setId(newId);
        newTask.setStatus(TaskStatus.NEW);
        idToTask.put(newTask.getId(), newTask);
        return newTask;
    }
    public void updateTask(Task task) {
        idToTask.put(task.getId(), task);
    }

    public void deleteTaskById(int id) {
        idToTask.remove(id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(idToEpic.values());
    }

    public void deleteAllEpics() {
        idToEpic.clear();
    }

    public Epic getEpicById(int id) {
        return idToEpic.get(id);
    }
    public Epic createEpic(Epic newEpic){
        int newId = nextId();
        newEpic.setId(newId);
        newEpic.setStatus(TaskStatus.NEW);
        idToEpic.put(newEpic.getId(),newEpic);
        return newEpic;
    }
    public void updateEpic(Epic epic) {
        idToEpic.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }
    private void updateEpicStatus(int epicId) {
        Epic epic = idToEpic.get(epicId);
        if (epic == null) return;

        ArrayList<Subtask> subtaskList = getSubtasksByEpicId(epicId);
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
    public void deleteEpicById(int id) {
        if (idToEpic.containsKey(id)) {
            Epic epic = idToEpic.get(id);
            for (Integer subId : epic.getSubtaskIds()) {
                idToSubtask.remove(subId);
            }
            idToEpic.remove(id);
        }
    }
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(idToSubtask.values());
    }

    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        for (Subtask s : idToSubtask.values()) {
            if (s.getEpicId() == epicId) {
                result.add(s);
            }
        }
        return result;
    }
    public void deleteAllSubtasks() {
        idToSubtask.clear();
        for (Epic epic : idToEpic.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    public Subtask getSubtaskById(int id) {
        return idToSubtask.get(id);
    }
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

    public void updateSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        idToSubtask.put(subtask.getId(), subtask);
        if (idToEpic.containsKey(epicId)) {
            updateEpicStatus(epicId);
        }
    }

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

}

