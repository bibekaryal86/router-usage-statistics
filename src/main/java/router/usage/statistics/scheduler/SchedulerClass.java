package router.usage.statistics.scheduler;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;

import java.util.Date;

import static java.sql.Timestamp.valueOf;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.StdSchedulerFactory.getDefaultScheduler;
import static org.slf4j.LoggerFactory.getLogger;

public class SchedulerClass {

    private static final Logger LOGGER = getLogger(SchedulerClass.class);
    private static final Integer MINUTE_TO_EXECUTE = 3;

    public void start() {
        LOGGER.info("Start Quartz Scheduler");

        try {
            Scheduler scheduler = getDefaultScheduler();
            scheduler.start();

            JobDetail jobDetail = newJob(SchedulerJobClass.class)
                    .withIdentity("Job_One")
                    .build();

            int hour = now().getHour();
            int minute = now().getMinute();

            if (minute > MINUTE_TO_EXECUTE) {
                if (hour == 23) {
                    hour = 0;
                } else {
                    hour = hour + 1;
                }
            }

            Date triggerStartTime = valueOf(of(now().getYear(), now().getMonth(), now().getDayOfMonth(), hour, MINUTE_TO_EXECUTE));

            Trigger triggerOne = newTrigger()
                    .withIdentity("Trigger_One")
                    .startAt(triggerStartTime)
                    .withSchedule(simpleSchedule()
                            .withIntervalInHours(1)
                            .repeatForever())
                    .forJob(jobDetail)
                    .build();

            scheduler.scheduleJob(jobDetail, triggerOne);
        } catch (SchedulerException ex) {
            LOGGER.error("Start Scheduler Error", ex);
        }

        LOGGER.info("Finish Quartz Scheduler");
    }
}
