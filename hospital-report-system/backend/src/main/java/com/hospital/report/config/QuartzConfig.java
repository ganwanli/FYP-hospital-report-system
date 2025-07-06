package com.hospital.report.config;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, QuartzJobFactory quartzJobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        
        factory.setDataSource(dataSource);
        factory.setJobFactory(quartzJobFactory);
        factory.setApplicationContextSchedulerContextKey("applicationContext");
        
        Properties quartzProperties = new Properties();
        quartzProperties.put("org.quartz.scheduler.instanceName", "HospitalReportScheduler");
        quartzProperties.put("org.quartz.scheduler.instanceId", "AUTO");
        quartzProperties.put("org.quartz.scheduler.skipUpdateCheck", "true");
        
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        quartzProperties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        quartzProperties.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
        quartzProperties.put("org.quartz.jobStore.isClustered", "true");
        quartzProperties.put("org.quartz.jobStore.clusterCheckinInterval", "20000");
        quartzProperties.put("org.quartz.jobStore.maxMisfiresToHandleAtATime", "1");
        quartzProperties.put("org.quartz.jobStore.misfireThreshold", "120000");
        quartzProperties.put("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS WHERE SCHED_NAME = {1} AND LOCK_NAME = ? FOR UPDATE");
        
        quartzProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        quartzProperties.put("org.quartz.threadPool.threadCount", "10");
        quartzProperties.put("org.quartz.threadPool.threadPriority", "5");
        quartzProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
        
        factory.setQuartzProperties(quartzProperties);
        factory.setStartupDelay(30);
        factory.setAutoStartup(true);
        factory.setOverwriteExistingJobs(true);
        
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
        return schedulerFactoryBean.getScheduler();
    }
}