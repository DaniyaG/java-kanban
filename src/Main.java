import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;
import kanban.manager.InMemoryTaskManager;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

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

        Task retrievedTask1 = manager.getTaskById(task1.getId());

        Epic retrievedEpic1 = manager.getEpicById(epic1.getId());


        Subtask retrievedSubtask1 = manager.getSubtaskById(subtask1.getId());

        manager.getTaskById(task2.getId());
        manager.getTaskById(task1.getId());
        printAllTasks(manager);
    }

    private static void printAllTasks(InMemoryTaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask sub : manager.getSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + sub);
            }
        }
        System.out.println("Подзадачи:");
        for (Task sub : manager.getAllSubtasks()) {
            System.out.println(sub);
        }

        System.out.println("История:");
        for (Task t : manager.getHistory()) {
            System.out.println(t);
        }
    }
}
