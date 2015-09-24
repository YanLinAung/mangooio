package io.mangoo.scheduler;

import java.util.Objects;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooJobFactory implements JobFactory {
    private Injector injector;

    @Inject
    public MangooJobFactory(Injector injector) {
        this.injector = Objects.requireNonNull(injector, "Injector can not be null");
    }

    @Override
    public Job newJob(final TriggerFiredBundle triggerFiredBundle, final Scheduler scheduler) {
        Preconditions.checkNotNull(triggerFiredBundle, "triggerFiredBundle is required for a new job");
        Preconditions.checkNotNull(scheduler, "scheduler is required for a new job");

        return injector.getInstance(triggerFiredBundle.getJobDetail().getJobClass());
    }
}