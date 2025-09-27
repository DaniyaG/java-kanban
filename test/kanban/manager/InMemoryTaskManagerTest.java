package kanban.manager;

import static org.junit.jupiter.api.Assertions.*;

import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    public void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void testCreateAndGetTask() {
        Task task = new Task(1, "Test task", "Description", TaskStatus.NEW);
        Task created = taskManager.createTask(task);

        assertNotNull(created.getId());
        assertEquals("Test task", created.getTitle());

        Task fetched = taskManager.getTaskById(created.getId());
        assertEquals(created, fetched);
    }

    @Test
    public void testCreateAndGetEpic() {
        Epic epic = new Epic(1, "Epic title", "Epic description", TaskStatus.NEW);
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId());
        assertTrue(createdEpic.getSubtaskIds().isEmpty());

        Epic fetched = taskManager.getEpicById(createdEpic.getId());
        assertEquals(createdEpic, fetched);
    }

    @Test
    public void testCreateSubtaskAndLinkToEpic() {
        Epic epic = taskManager.createEpic(new Epic(null, "Epic1", "Description", TaskStatus.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask(null, "Subtask1", "SubDesc", TaskStatus.NEW, epic.getId()));

        assertEquals(epic.getId(), subtask.getEpicId());
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    public void testUpdateTask() {
        Task task = taskManager.createTask(new Task(null, "Task1", "Desc1", TaskStatus.NEW));
        task.setTitle("Updated title");
        task.setStatus(TaskStatus.DONE);

        taskManager.updateTask(task);
        Task updated = taskManager.getTaskById(task.getId());

        assertEquals("Updated title", updated.getTitle());
        assertEquals(TaskStatus.DONE, updated.getStatus());
    }

    @Test
    public void testUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = taskManager.createEpic(new Epic(null, "Epic", "Desc", TaskStatus.NEW));
        Subtask st1 = taskManager.createSubtask(new Subtask(null, "Sub1", "Desc", TaskStatus.NEW, epic.getId()));
        Subtask st2 = taskManager.createSubtask(new Subtask(null, "Sub2", "Desc", TaskStatus.NEW, epic.getId()));

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epic.getId()).getStatus());

        st1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(st1);
        Epic updatedEpic1 = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic1.getStatus());

        st2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(st2);
        Epic updatedEpic2 = taskManager.getEpicById(epic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic2.getStatus());
    }

    @Test
    public void testDeleteTask() {
        Task task = taskManager.createTask(new Task(null, "Task", "Desc", TaskStatus.NEW));
        int id = task.getId();

        taskManager.deleteTaskById(id);

        assertNull(taskManager.getTaskById(id));
        assertFalse(taskManager.getAllTasks().contains(task));
    }

    @Test
    public void testDeleteSubtaskAndUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic(null, "Epic", "Desc", TaskStatus.NEW));
        Subtask subtask = taskManager.createSubtask(new Subtask(null, "Sub", "Desc", TaskStatus.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId));
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertFalse(updatedEpic.getSubtaskIds().contains(subtaskId));
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    }

    @Test
    public void testHistoryManagerAddsAndLimitsSize() {
        for (int i = 0; i < 15; i++) {
            Task task = taskManager.createTask(new Task(null, "Task " + i, "Desc", TaskStatus.NEW));
            taskManager.getTaskById(task.getId());
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size());
    }

    @Test
    public void testTasksEqualityById() {
        Task task1 = new Task(null, "Test task 1", "Description 1", TaskStatus.NEW);
        task1.setId(100);
        Task task2 = new Task(null, "Different title", "Different description", TaskStatus.NEW);
        task2.setId(100);

        assertEquals(task1, task2, "Задачи с одинаковым id должны считаться равными");

        task2.setId(101);
        assertNotEquals(task1, task2, "Задачи с разными id не должны быть равны");
    }

    @Test
    public void testEpicEqualityById() {
        Epic epic1 = new Epic(null, "Epic One", "Desc", TaskStatus.NEW);
        epic1.setId(10);
        Epic epic2 = new Epic(null, "Another epic", "Other Desc", TaskStatus.NEW);
        epic2.setId(10);

        assertEquals(epic1, epic2, "Epic с одинаковым id должны быть равны");

        epic2.setId(11);
        assertNotEquals(epic1, epic2, "Epic с разными id не должны быть равны");
    }

    @Test
    public void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask(null, "Subtask 1", "Desc", TaskStatus.NEW, 1);
        subtask1.setId(20);
        Subtask subtask2 = new Subtask(null, "Other subtask", "Another Desc", TaskStatus.DONE, 1);
        subtask2.setId(20);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым id должны быть равны");

        subtask2.setId(21);
        assertNotEquals(subtask1, subtask2, "Subtask с разными id не должны быть равны");
    }


    @Test
    public void testManagersUtilityClassInitialization() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager, "Менеджер не должен быть null");
        assertTrue(manager instanceof InMemoryTaskManager, "Должен возвращаться экземпляр InMemoryTaskManager");

        Task task = new Task(1, "Test", "Description", TaskStatus.NEW);
        manager.createTask(task);

        Task retrieved = manager.getTaskById(task.getId());
        assertEquals(task, retrieved, "Созданная и полученная задачи должны совпадать");
    }

    @Test
    void testCannotAddEpicAsSubtaskToItself() {
        Epic epic = new Epic(1, "Epic 1", "Description", TaskStatus.NEW);
        boolean result = epic.addSubtaskId(epic.getId());
        assertFalse(result, "Epic не должен добавляться как подзадача сам себе");
    }

    @Test
    void testCannotSetSubtaskEpicToItself() {
        Epic epic = new Epic(1, "Epic 1", "Description", TaskStatus.NEW);
        Subtask subtask = new Subtask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        boolean result = subtask.updateEpicId(subtask.getId());
        assertFalse(result, "Subtask не должен ссылаться сам на себя как на эпик");
    }

    @Test
    void testTaskImmutabilityOnAdd() {
        // Создаем задачу с начальным id (который будет изменен внутри createTask)
        Task task = new Task(0, "Task 1", "Description", TaskStatus.NEW);
        Task createdTask = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(createdTask.getId(), retrievedTask.getId());
        assertEquals(createdTask.getTitle(), retrievedTask.getTitle());
        assertEquals(createdTask.getDescription(), retrievedTask.getDescription());
        assertEquals(createdTask.getStatus(), retrievedTask.getStatus());

        assertNotEquals(0, createdTask.getId());
    }

}