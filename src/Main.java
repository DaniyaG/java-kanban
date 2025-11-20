import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.Managers;
import kanban.manager.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        //TaskManager taskManager = Managers.getDefault();

       /* Task task1 = new Task(null, "Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task(null, "Задача 2", "Описание задачи 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Epic epic1 = new Epic(null, "Эпик 1", "Эпик с тремя подзадачами", TaskStatus.NEW);
        taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask(null, "Подзадача 1 для эпика 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask(null, "Подзадача 2 для эпика 1", "Описание подзадачи 2", TaskStatus.NEW, epic1.getId());
        Subtask subtask3 = new Subtask(null, "Подзадача 3 для эпика 1", "Описание подзадачи 3", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        Epic epic2 = new Epic(null, "Эпик 2", "Эпик без подзадач", TaskStatus.NEW);
        taskManager.createEpic(epic2);

        System.out.println(">>> Запрос задачи 1");
        taskManager.getTaskById(task1.getId());
        System.out.println(taskManager.getHistory());

        System.out.println(">>> Запрос эпика 1 с подзадачами");
        taskManager.getEpicById(epic1.getId());
        System.out.println(taskManager.getHistory());

        System.out.println(">>> Запрос задачи 2 и эпика 2 без подзадач");
        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic2.getId());
        System.out.println(taskManager.getHistory());

        System.out.println(">>> Повторный запрос задачи 1");
        taskManager.getTaskById(task1.getId());
        System.out.println(taskManager.getHistory());

        System.out.println(">>> Удаляем задачу 1");
        taskManager.deleteTaskById(task1.getId());
        System.out.println(taskManager.getHistory());

        System.out.println(">>> Удаляем эпик с подзадачами");
        taskManager.deleteEpicById(epic1.getId());
        System.out.println(taskManager.getHistory());*/


    }


}
