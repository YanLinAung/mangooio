package io.mangoo.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.exceptions.MangooSchedulerException;

/**
 * Convenient class for interacting with the quartz scheduler
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Scheduler {
    private static final Logger LOG = LogManager.getLogger(Scheduler.class);
    private static final Config CONFIG = Application.getConfig();
    private org.quartz.Scheduler quartzScheduler;

    public Scheduler() {
        CONFIG.getAllConfigurations().entrySet().forEach((Map.Entry<String, String> entry) -> {
            if (entry.getKey().startsWith(Default.SCHEDULER_PREFIX.toString())) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * Returns the current scheduler instance
     * @deprecated As of release 3.1.0, replaced by {@link #getQuartzScheduler()}
     *
     * @return Scheduler instance, null if scheduler is not initialize or started
     */
    @Deprecated
    public org.quartz.Scheduler getScheduler() {
        return this.quartzScheduler;
    }
    
    /**
     * Returns the current quartz scheduler instance
     *
     * @return Scheduler instance, null if scheduler is not initialize or started
     */
    public org.quartz.Scheduler getQuartzScheduler() {
        return this.quartzScheduler;
    }

    public void start() {
        initialize();
        try {
            this.quartzScheduler.start();
            if (this.quartzScheduler.isStarted()) {
                LOG.info("Successfully started quartz scheduler");
            } else {
                LOG.error("Scheduler is not started");
            }
        } catch (final SchedulerException e) {
            LOG.error("Failed to start scheduler", e);
        }
    }

    public void shutdown() {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized or started");

        try {
            this.quartzScheduler.shutdown();
            if (this.quartzScheduler.isShutdown()) {
                LOG.info("Successfully shutdown quartz scheduler");
            } else {
                LOG.error("Failed to shutdown scheduler");
            }
        } catch (final SchedulerException e) {
            LOG.error("Failed to shutdown scheduler", e);
        }
    }

    public void standby() {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized or started");

        try {
            this.quartzScheduler.standby();
            if (this.quartzScheduler.isInStandbyMode()) {
                LOG.info("Scheduler is now in standby");
            } else {
                LOG.error("Failed to put scheduler in standby");
            }
        } catch (final SchedulerException e) {
            LOG.error("Failed to put scheduler in standby", e);
        }
    }

    /**
     * Prepares the scheduler for being started by creating a
     * scheduler instance from quartz scheduler factory
     */
    private void initialize() {
        if (this.quartzScheduler == null) {
            try {
                this.quartzScheduler = new StdSchedulerFactory().getScheduler();
                this.quartzScheduler.setJobFactory(Application.getInstance(SchedulerFactory.class));
            } catch (final SchedulerException e) {
                LOG.error("Failed to initialize scheduler", e);
            }
        }
    }

    /**
     * Adds a new job with a given JobDetail and Trigger to the scheduler
     *
     * @param jobDetail The JobDetail for the Job
     * @param trigger The Trigger for the job
     */
    public void schedule(JobDetail jobDetail, Trigger trigger) {
        Objects.requireNonNull(jobDetail, "JobDetail is required for schedule");
        Objects.requireNonNull(trigger, "trigger is required for schedule");
        initialize();

        try {
            this.quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (final SchedulerException e) {
            LOG.error("Failed to schedule a new job", e);
        }
    }
    

    /**
     * Retrieves a list of all jobs and their current status
     * 
     * @return List of io.mangoo.models.Job objects
     * @throws MangooSchedulerException if an error occurs during access to the Quartz Scheduler
     */
    @SuppressWarnings("unchecked")
    public List<io.mangoo.models.Job> getAllJobs() throws MangooSchedulerException {
        List<io.mangoo.models.Job> jobs = new ArrayList<>();
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                jobs.add(new io.mangoo.models.Job(TriggerState.PAUSED.equals(triggerState) ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
            }
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return jobs;
    }
    
    /**
     * Executes a single Quartz Scheduler job right away only once
     * 
     * @param jobName The name of the job to execute
     * @throws MangooSchedulerException if an error occurs during execution of the job
     */
    public void executeJob(String jobName) throws MangooSchedulerException {
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    scheduler.triggerJob(jobKey);  
                }
            }
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }
    
    /**
     * Retrieves a list of all JobKeys from the Quartz Scheduler
     * 
     * @return List of all JobKey objects
     * @throws MangooSchedulerException if an errors occurs during access to the scheduler
     */
    public List<JobKey> getAllJobKeys() throws MangooSchedulerException {
        List<JobKey> jobKeys = new ArrayList<>();
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getScheduler();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                jobKeys.addAll(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)));
            }            
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return jobKeys;
    }
    
    /**
     * Changes the state of a normally running job from pause to resume or resume to pause
     * 
     * @param jobName The name of the job
     * @throws MangooSchedulerException if an error occurs during access to the quartz scheuler
     */
    public void changeState(String jobName) throws MangooSchedulerException {
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    TriggerState triggerState = getTriggerState(scheduler, jobKey);
                    if (TriggerState.NORMAL.equals(triggerState)) {
                        scheduler.pauseJob(jobKey);                        
                    } else {
                        scheduler.resumeJob(jobKey);
                    }
                }
            }            
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private TriggerState getTriggerState(org.quartz.Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        Trigger trigger = triggers.get(0);  

        return scheduler.getTriggerState(trigger.getKey());
    }
}