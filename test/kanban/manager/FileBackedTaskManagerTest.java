package kanban.manager;

import kanban.data.Epic;
import kanban.data.Subtask;
import kanban.data.Task;
import kanban.data.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        if (tempFile.exists()) {
            tempFile.delete();
        }
        tempFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testSaveAndLoadEmptyFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSaveMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task(0, "Task1", "Description1", TaskStatus.NEW);
        Epic epic = new Epic(0, "Epic1", "Epic Description", TaskStatus.NEW);
        Subtask subtask = new Subtask(0, "Subtask1", "Subdesc", TaskStatus.NEW, 1);

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subtask);

        manager.save();

        assertTrue(tempFile.length() > 0);
    }

    @Test
    void testLoadMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task1 = new Task(0, "Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task(0, "Задача 2", "Описание 2", TaskStatus.IN_PROGRESS);

        task1 = manager.createTask(task1);
        task2 = manager.createTask(task2);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loadedManager.getAllTasks().size());

        Task loadedTask1 = loadedManager.getTaskById(task1.getId());
        Task loadedTask2 = loadedManager.getTaskById(task2.getId());

        assertNotNull(loadedTask1);
        assertNotNull(loadedTask2);
        assertEquals(task1.getTitle(), loadedTask1.getTitle());
        assertEquals(task2.getTitle(), loadedTask2.getTitle());
        assertEquals(task1.getDescription(), loadedTask1.getDescription());
        assertEquals(task2.getDescription(), loadedTask2.getDescription());
        assertEquals(task1.getStatus(), loadedTask1.getStatus());
        assertEquals(task2.getStatus(), loadedTask2.getStatus());
    }
}
