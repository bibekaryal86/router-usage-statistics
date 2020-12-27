package router.usage.statistics.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import router.usage.statistics.servlet.ServletClass;

import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;
import static router.usage.statistics.util.UtilClass.SERVER_PORT;
import static router.usage.statistics.util.UtilClass.getSystemEnvProperty;

public class ServerClass {

    private static final Logger LOGGER = getLogger(ServerClass.class);

    public static final int SERVER_MAX_THREADS = 100;
    public static final int SERVER_MIN_THREADS = 20;
    public static final int SERVER_IDLE_TIMEOUT = 120;

    private Server server;

    public void start() throws Exception {
        LOGGER.info("Start Jetty Server");
        QueuedThreadPool threadPool = new QueuedThreadPool(SERVER_MAX_THREADS, SERVER_MIN_THREADS, SERVER_IDLE_TIMEOUT);

        server = new Server(threadPool);

        try (ServerConnector connector = new ServerConnector(server)) {
            int port = getSystemEnvProperty(SERVER_PORT) == null ? 8080 : parseInt(getSystemEnvProperty(SERVER_PORT));
            connector.setPort(port);
            server.setConnectors(new Connector[]{connector});
        }

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(ServletClass.class, "/");

        server.setHandler(servletHandler);
        server.start();
        LOGGER.info("Finish Jetty Server");
    }

    public void stop() throws Exception {
        server.stop();
    }
}
