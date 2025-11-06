package kanban.manager;

import kanban.data.Task;
import kanban.data.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

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
}