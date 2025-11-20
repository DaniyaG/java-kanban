package kanban.manager;

import kanban.data.Task;
import kanban.data.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        task1 = new Task(1, "Task 1", "Description 1",TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        task2 = new Task(2, "Task 2", "Description 2", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
        task3 = new Task(3, "Task 3", "Description 3", TaskStatus.NEW, Duration.ofMinutes(20), LocalDateTime.of(2000, 1, 1, 11, 0));
    }

    @Test
    void testAddTasksAndGetHistoryOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void testAddDuplicateMovesTaskToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void testRemoveHead() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    void testRemoveTail() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void testRemoveMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void testRemoveNonexistent() {
        historyManager.add(task1);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void testGetTasksReturnsCorrectOrder() {
        historyManager.add(task3);
        historyManager.add(task1);
        List<Task> tasks = historyManager.getTasks();

        assertEquals(2, tasks.size());
        assertEquals(task3, tasks.get(0));
        assertEquals(task1, tasks.get(1));
    }

    @Test
    void testGetHistoryEmpty() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}