package com.example.spintracks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SharedExecutor {
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private SharedExecutor() {}

    public static void execute(Runnable command) {
        executor.execute(command);
    }
}
