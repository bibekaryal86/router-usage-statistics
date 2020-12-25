/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package router.usage.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.usage.statistics.scheduler.SchedulerClass;
import router.usage.statistics.server.ServerClass;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("Begin router-usage-statistics initialization...");
        new ServerClass().start();
        new SchedulerClass().start();
        LOGGER.info("End router-usage-statistics initialization...");
    }
}