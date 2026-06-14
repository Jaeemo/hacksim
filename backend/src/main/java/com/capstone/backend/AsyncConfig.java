package com.capstone.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Runs detonations off the HTTP request thread. The pool is single-threaded because there is one
 * detonation VM — runs serialise rather than racing for the guest — with a small queue so bursts are
 * accepted (202) and processed in order instead of blocking callers.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("detonationExecutor")
    public Executor detonationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("detonation-");
        executor.initialize();
        return executor;
    }
}
