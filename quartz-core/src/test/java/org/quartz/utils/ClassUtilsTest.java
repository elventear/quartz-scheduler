package org.quartz.utils;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import junit.framework.TestCase;

/**
 * @author Alex Snaps
 */
public class ClassUtilsTest extends TestCase {

    public void testIsAnnotationPresentOnSuperClass() throws Exception {
        assertTrue(ClassUtils.isAnnotationPresent(BaseJob.class, DisallowConcurrentExecution.class));
        assertFalse(ClassUtils.isAnnotationPresent(BaseJob.class, PersistJobDataAfterExecution.class));
        assertTrue(ClassUtils.isAnnotationPresent(ExtendedJob.class, DisallowConcurrentExecution.class));
        assertFalse(ClassUtils.isAnnotationPresent(ExtendedJob.class, PersistJobDataAfterExecution.class));
        assertTrue(ClassUtils.isAnnotationPresent(ReallyExtendedJob.class, DisallowConcurrentExecution.class));
        assertTrue(ClassUtils.isAnnotationPresent(ReallyExtendedJob.class, PersistJobDataAfterExecution.class));
    }

    @DisallowConcurrentExecution
    private static class BaseJob implements Job {
        public void execute(final JobExecutionContext context) throws JobExecutionException {
            System.out.println(this.getClass().getSimpleName());
        }
    }

    private static class ExtendedJob extends BaseJob {
    }

    @PersistJobDataAfterExecution
    private static class ReallyExtendedJob extends ExtendedJob {

    }
}
