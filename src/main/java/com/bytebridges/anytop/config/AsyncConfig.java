package com.bytebridges.anytop.config;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

@Configuration
public class AsyncConfig {

    @Bean(name = "gsmExecutor")
    public TaskExecutor gsmExecutor() {
        return new TaskExecutorAdapter(
                Executors.newVirtualThreadPerTaskExecutor()
        );
    }
}
