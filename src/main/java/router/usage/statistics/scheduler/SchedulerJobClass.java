package router.usage.statistics.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;
import static router.usage.statistics.service.ServiceClass.insertDataUsages;

public class SchedulerJobClass implements Job {

    private static final Logger LOGGER = getLogger(SchedulerJobClass.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Start Execute");
        insertDataUsages();
        LOGGER.info("Finish Execute");
    }
}
