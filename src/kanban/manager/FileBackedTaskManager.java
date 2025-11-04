package kanban.manager;

import kanban.data.*;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic");
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
        String typeStr = task.getType().toString();
        String name = task.getTitle();
        String status = task.getStatus().toString();
        String description = task.getDescription();
        String epicIdStr = "";

        if (task instanceof Subtask) {
            epicIdStr = String.valueOf(((Subtask) task).getEpicId());
        }
        String idStr = String.valueOf(task.getId());
        StringBuilder sb = new StringBuilder();
        sb.append(idStr).append(",")
                .append(typeStr).append(",")
                .append(name).append(",")
                .append(status).append(",")
                .append(description).append(",")
                .append(epicIdStr);
        return sb.toString();
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        String typeStr = parts[1];
        String name = parts[2];
        String statusStr = parts[3];
        String description = parts[4];
        String epicIdStr = parts.length > 5 ? parts[5] : "";

        TaskType type = TaskType.valueOf(typeStr);
        TaskStatus status = TaskStatus.valueOf(statusStr);

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = 0;
                if (!epicIdStr.isEmpty()) {
                    try {
                        epicId = Integer.parseInt(epicIdStr);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Некорректный формат epicId: " + epicIdStr);
                    }
                }
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new RuntimeException("Неизвестный тип задачи: " + typeStr);
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

        Task task1 = new Task(0, "Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task(0, "Задача 2", "Описание задачи 2", TaskStatus.NEW);

        Epic epic1 = new Epic(0, "Эпик 1", "Описание эпика 1", TaskStatus.NEW);
        Epic epic2 = new Epic(0, "Эпик 2", "Описание эпика 2", TaskStatus.NEW);

        task1 = manager.createTask(task1);
        task2 = manager.createTask(task2);

        epic1 = manager.createEpic(epic1);
        epic2 = manager.createEpic(epic2);

        Subtask subtask1 = new Subtask(0, "Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(0, "Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW, epic1.getId());

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