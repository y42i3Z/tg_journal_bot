package org.y42i3Z.tg_journal_bot;

import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    public static final String REMOTE_URL = "";
    public static final String USERNAME = "";
    public static final String PASSWORD = "";
    public static final String TOKEN = "";
    public static final String BOT_NAME = "";

    // had to use user/password scheme because of JSCH bug, info in readme
    @Bean
    public static CredentialsProvider getUserPasswordCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD);
    }

    @Bean
    public static ProgressMonitor getProgressMonitor() {
        return new SimpleProgressMonitor();
    }

    private static class SimpleProgressMonitor implements ProgressMonitor {
        @Override
        public void start(int totalTasks) {
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork) {
            System.out.println("Start " + title + ": " + totalWork);
        }

        @Override
        public void update(int completed) {
            System.out.print(completed + "-");
        }

        @Override
        public void endTask() {
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}