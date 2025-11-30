import com.sun.net.httpserver.HttpServer;
import handlers.*;
import kanban.manager.Managers;
import kanban.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final String TASKS_PATH = "/tasks";
    private static final String SUBTASKS_PATH = "/subtasks";
    private static final String EPICS_PATH = "/epics";
    private static final String HISTORY_PATH = "/history";
    private static final String PRIORITIZED_PATH = "/prioritized";

    private HttpServer httpServer;
    private TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext(TASKS_PATH, new TaskHandler(taskManager));
        httpServer.createContext(SUBTASKS_PATH, new SubtaskHandler(taskManager));
        httpServer.createContext(EPICS_PATH, new EpicHandler(taskManager));
        httpServer.createContext(HISTORY_PATH, new HistoryHandler(taskManager));
        httpServer.createContext(PRIORITIZED_PATH, new PrioritizedHandler(taskManager));
        httpServer.setExecutor(null);
    }

    public void start() {
        System.out.println("Запускаем сервер на порту " + PORT);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Остановили сервер на порту " + PORT);
    }
    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}

