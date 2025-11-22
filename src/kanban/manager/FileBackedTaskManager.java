package kanban.manager;

import kanban.data.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,duration,startTime,endTime");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(toStringTask(task));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toStringTask(epic));
                writer.newLine();
            }

            for (Task subtask : getAllSubtasks()) {
                writer.write(toStringTask(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (!file.exists() || Files.size(file.toPath()) == 0) {
                return manager;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Не удалось определить размер файла ", ex);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task task = fromString(line);
                switch (task.getType()) {
                    case TASK:
                        manager.idToTask.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.idToEpic.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        manager.idToSubtask.put(task.getId(), (Subtask) task);
                        break;
                }
            }

            int maxId = 0;
            for (Task t : manager.getAllTasks()) {
                if (t.getId() > maxId) maxId = t.getId();
            }
            for (Epic e : manager.getAllEpics()) {
                if (e.getId() > maxId) maxId = e.getId();
            }
            for (Subtask s : manager.getAllSubtasks()) {
                if (s.getId() > maxId) maxId = s.getId();
            }
            manager.counter = maxId + 1;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла", e);
        }
        return manager;
    }

    @Override
    public Task createTask(Task newTask) {
        Task task = super.createTask(newTask);
        save();
        return task;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public Epic createEpic(Epic newEpic) {
        Epic epic = super.createEpic(newEpic);
        save();
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public Subtask createSubtask(Subtask newSubtask) {
        Subtask subtask = super.createSubtask(newSubtask);
        save();
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    private String toStringTask(Task task) {
        String taskType = task.getType().toString();
        String taskTitle = task.getTitle();
        String taskStatus = task.getStatus().toString();
        String taskDescription = task.getDescription();
        String subtaskEpicId = "";
        String taskDuration = "";
        String taskStartTime = "";
        String taskEndTime = "";

        if (task.getDuration() != null) {
            taskDuration = String.valueOf(task.getDuration().toMinutes());
        }
        if (task.getStartTime() != null) {
            taskStartTime = task.getStartTime().toString();
        }
        if (task.getEndTime() != null) {
            taskEndTime = task.getEndTime().toString();
        }

        if (task instanceof Subtask) {
            subtaskEpicId = String.valueOf(((Subtask) task).getEpicId());
        }
        String idStr = String.valueOf(task.getId());
        StringBuilder builder = new StringBuilder();
        builder.append(idStr).append(",")
                .append(taskType).append(",")
                .append(taskTitle).append(",")
                .append(taskStatus).append(",")
                .append(taskDescription).append(",")
                .append(subtaskEpicId).append(",")
                .append(taskDuration).append(",")
                .append(taskStartTime).append(",")
                .append(taskEndTime);
        return builder.toString();
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        String taskType = parts[1];
        String taskTitle = parts[2];
        String taskStatus = parts[3];
        String taskDescription = parts[4];
        String subtaskEpicId = parts.length > 5 ? parts[5] : "";
        String taskDuration = parts.length > 6 ? parts[6] : "";
        String taskStartTime = parts.length > 7 ? parts[7] : "";
        String taskEndTime = parts.length > 8 ? parts[8] : "";

        TaskType type = TaskType.valueOf(taskType);
        TaskStatus status = TaskStatus.valueOf(taskStatus);

        Duration duration = null;
        if (!taskDuration.isEmpty()) {
            try {
                long minutes = Long.parseLong(taskDuration);
                duration = Duration.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Некорректный формат duration: " + taskDuration);
            }
        }

        LocalDateTime startTime = null;
        if (!taskStartTime.isEmpty()) {
            try {
                startTime = LocalDateTime.parse(taskStartTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Некорректный формат startTime: " + taskStartTime);
            }
        }

        LocalDateTime endTime = null;
        if (!taskEndTime.isEmpty()) {
            try {
                endTime = LocalDateTime.parse(taskEndTime);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Некорректный формат endTime: " + taskEndTime);
            }
        }

        switch (type) {
            case TASK:
                return new Task(id, taskTitle, taskDescription, status, duration, startTime);
            case EPIC:
                return new Epic(id, taskTitle, taskDescription, status);
            case SUBTASK:
                int epicId = 0;
                if (!subtaskEpicId.isEmpty()) {
                    try {
                        epicId = Integer.parseInt(subtaskEpicId);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Некорректный формат epicId: " + subtaskEpicId);
                    }
                }
                return new Subtask(id, taskTitle, taskDescription, status, epicId, duration, startTime);
            default:
                throw new RuntimeException("Неизвестный тип задачи: " + taskType);
        }
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void main(String[] args) {

        File file = new File("tasks.csv");

        if (file.exists()) {
            file.delete();
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task(0, "Задача 1", "Описание задачи 1", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        Task task2 = new Task(0, "Задача 2", "Описание задачи 2", TaskStatus.NEW,Duration.ofMinutes(45), LocalDateTime.of(2000, 2, 1, 12, 0));

        Epic epic1 = new Epic(0, "Эпик 1", "Описание эпика 1", TaskStatus.NEW);
        Epic epic2 = new Epic(0, "Эпик 2", "Описание эпика 2", TaskStatus.NEW);

        task1 = manager.createTask(task1);
        task2 = manager.createTask(task2);

       epic1 = manager.createEpic(epic1);
       epic2 = manager.createEpic(epic2);

        Subtask subtask1 = new Subtask(0, "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId(),Duration.ofMinutes(26), LocalDateTime.of(2000, 1, 2, 10, 0));
        Subtask subtask2 = new Subtask(0, "Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW, epic1.getId(),Duration.ofMinutes(28), LocalDateTime.of(2000, 1, 1, 13, 0));

        subtask1 = manager.createSubtask(subtask1);
        subtask2 = manager.createSubtask(subtask2);

        manager.save();

       FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        System.out.println("Все задачи в загруженном менеджере:");
        for (Task t : loadedManager.getAllTasks()) {
            System.out.println(t);
        }

        System.out.println("\nВсе эпики в загруженном менеджере:");
        for (Epic e : loadedManager.getAllEpics()) {
            System.out.println(e);
        }

        System.out.println("\nВсе подзадачи в загруженном менеджере:");
        for (Subtask s : loadedManager.getAllSubtasks()) {
            System.out.println(s);
        }
    }
}