package router.usage.statistics.scheduler;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.StdSchedulerFactory.getDefaultScheduler;
import static router.usage.statistics.service.ServiceClass.insertDataUsages;

public class SchedulerClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerClass.class);

    public void start() {
        LOGGER.info("Start Quartz Scheduler");

        try {
            Scheduler scheduler = getDefaultScheduler();
            scheduler.start();

            JobDetail jobDetail = newJob(JobClass.class)
                    .withIdentity("Job_One")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("Trigger_One")
                    .startNow()
                    .withSchedule(dailyAtHourAndMinute(1, 1)
                            .inTimeZone(TimeZone.getTimeZone("America/Denver")))
                    .forJob(jobDetail)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            LOGGER.error("Start Scheduler Error", ex);
        }

        LOGGER.info("Finish Quartz Scheduler");
    }
}

class JobClass implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobClass.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Start Execute");
        insertDataUsages();
        LOGGER.info("Finish Execute");
    }
}
