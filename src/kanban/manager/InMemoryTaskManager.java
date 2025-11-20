package kanban.manager;

import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected HashMap<Integer, Task> idToTask = new HashMap<>();
    protected HashMap<Integer, Epic> idToEpic = new HashMap<>();
    protected HashMap<Integer, Subtask> idToSubtask = new HashMap<>();
    protected int counter = 1;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null)
            return Integer.compare(t1.getId(), t2.getId());
        if (t1.getStartTime() == null)
            return 1;
        if (t2.getStartTime() == null)
            return -1;
        int cmp = t1.getStartTime().compareTo(t2.getStartTime());
        if (cmp != 0) return cmp;
        return Integer.compare(t1.getId(), t2.getId());
    });

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
        if (hasOverlapWithExisting(newTask)) {
            throw new IllegalArgumentException("Задача или подзадача пересекается по времени с уже существующей");
        }
        idToTask.put(newTask.getId(), newTask);
        if (newTask.getStartTime() != null) {
            prioritizedTasks.add(newTask);
        }
        return newTask;
    }

    @Override
    public void updateTask(Task task) {
        Task oldTask = idToTask.get(task.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
        }
        if (hasOverlapWithExisting(task)) {
            throw new IllegalArgumentException("Задача или подзадача пересекается по времени с уже существующей");
        }
        idToTask.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = idToTask.remove(id);
        if (task != null) {
            idToTask.remove(id);
            historyManager.remove(id);
            prioritizedTasks.remove(task);
        }
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
    public Epic createEpic(Epic newEpic) {
        int newId = nextId();
        newEpic.setId(newId);
        newEpic.setStatus(TaskStatus.NEW);
        idToEpic.put(newEpic.getId(), newEpic);
        return newEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (idToEpic.containsKey(epic.getId())) {
            updateEpicStatus(epic.getId());
            idToEpic.put(epic.getId(), epic);
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = idToEpic.get(epicId);
        if (epic == null) return;

        List<Subtask> subtaskList = getSubtasksByEpicId(epicId);
        boolean allDone = subtaskList.stream()
                .allMatch(s -> s.getStatus() == TaskStatus.DONE);

        boolean allNew = subtaskList.stream()
                .allMatch(s -> s.getStatus() == TaskStatus.NEW);

        if (subtaskList.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
        idToEpic.put(epicId, epic);
    }

    public void updateEpicTiming(int epicId) {
        Epic epic = idToEpic.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasks = getSubtasksByEpicId(epicId);
        if (subtasks.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask s : subtasks) {
            if (s.getStartTime() != null && s.getDuration() != null) {
                if (earliestStart == null || s.getStartTime().isBefore(earliestStart)) {
                    earliestStart = s.getStartTime();
                }
                LocalDateTime subtaskEnd = s.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
                totalDuration = totalDuration.plus(s.getDuration());
            }
        }

        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);
        epic.setDuration(totalDuration);

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
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(idToSubtask.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        return idToSubtask.values().stream()
                .filter(s -> s.getEpicId() != null && s.getEpicId().equals(epicId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllSubtasks() {
        idToSubtask.clear();
        for (Epic epic : idToEpic.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTiming(epic.getId());
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
        if (hasOverlapWithExisting(newSubtask)) {
            throw new IllegalArgumentException("Задача или подзадача пересекается по времени с уже существующей");
        }

        idToSubtask.put(newSubtask.getId(), newSubtask);

        int epicId = newSubtask.getEpicId();
        if (idToEpic.containsKey(epicId)) {
            idToEpic.get(epicId).addSubtaskId(newId);
            updateEpicStatus(epicId);
            updateEpicTiming(epicId);
        }
        if (newSubtask.getStartTime() != null) {
            prioritizedTasks.add(newSubtask);
        }
        return newSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask oldSubtask = idToSubtask.get(subtask.getId());
        if (oldSubtask != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        if (hasOverlapWithExisting(subtask)) {
            throw new IllegalArgumentException("Задача или подзадача пересекается по времени с уже существующей");
        }
        if (idToSubtask.containsKey(subtask.getId())) {
            idToSubtask.put(subtask.getId(), subtask);
            int epicId = subtask.getEpicId();
            if (idToEpic.containsKey(epicId)) {
                updateEpicStatus(epicId);
                updateEpicTiming(epicId);
            }
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (idToSubtask.containsKey(id)) {
            int epicId = idToSubtask.get(id).getEpicId();
            Subtask subtask = idToSubtask.remove(id);
            prioritizedTasks.remove(subtask);
            if (idToEpic.containsKey(epicId)) {
                idToEpic.get(epicId).removeSubtaskId(id);
                updateEpicStatus(epicId);
                updateEpicTiming(epicId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean isOverlapping(Task t1, Task t2) {
        if (t1.getStartTime() == null || t2.getStartTime() == null ||
                t1.getEndTime() == null || t2.getEndTime() == null) {
            return false;
        }
        return t1.getStartTime().isBefore(t2.getEndTime()) &&
                t2.getStartTime().isBefore(t1.getEndTime());
    }

    public boolean hasOverlapWithExisting(Task task) {
        return getPrioritizedTasks().stream()
                .anyMatch(existingTask -> isOverlapping(task, existingTask));
    }

}




