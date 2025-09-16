import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;
import kanban.manager.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task(null, "Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task(null, "Задача 2", "Описание задачи 2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic(null, "Эпик 1", "Описание эпика 1", TaskStatus.NEW);
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask(null, "Подзадача 1 для эпика 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(null, "Подзадача 2 для эпика 1", "Описание подзадачи 2", TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic(null, "Эпик 2", "Описание эпика 2", TaskStatus.NEW);
        manager.createEpic(epic2);
        Subtask subtask3 = new Subtask(null, "Подзадача 1 для эпика 2", "Описание подзадачи 3", TaskStatus.NEW, epic2.getId());
        manager.createSubtask(subtask3);

        System.out.println("Все эпики: " + manager.getAllEpics());
        System.out.println("Все задачи: " + manager.getAllTasks());
        System.out.println("Все подзадачи: " + manager.getAllSubtasks());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);
        subtask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("\nПосле изменения статусов:");
        System.out.println("Задача 1: " + manager.getTaskById(task1.getId()));
        System.out.println("Подзадача 1 (эпик 1): " + manager.getSubtaskById(subtask1.getId()));
        System.out.println("Подзадача 2 (эпик 1): " + manager.getSubtaskById(subtask2.getId()));
        System.out.println("Подзадача 3 (эпик 2): " + manager.getSubtaskById(subtask3.getId()));
        System.out.println("Обновленный эпик 1: " + manager.getEpicById(epic1.getId()));
        System.out.println("Обновленный эпик 2: " + manager.getEpicById(epic2.getId()));

        System.out.println("\nСтатус эпика 1: " + manager.getEpicById(epic1.getId()).getStatus());
        System.out.println("Статус эпика 2: " + manager.getEpicById(epic2.getId()).getStatus());

        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic1.getId());

        System.out.println("\nПосле удаления:");
        System.out.println("Все эпики: " + manager.getAllEpics());
        System.out.println("Все задачи: " + manager.getAllTasks());
        System.out.println("Все подзадачи: " + manager.getAllSubtasks());
    }
}

