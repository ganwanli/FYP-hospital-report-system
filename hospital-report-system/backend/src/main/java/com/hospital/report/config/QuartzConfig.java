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
        
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        
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