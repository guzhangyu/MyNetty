package com.netty.assist;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 当前线程直接执行策略
 * Created by guzy on 16/9/20.
 */
public class SimpleThreadExecutors implements Executor{
    public void execute(Runnable command) {
        command.run();
    }
}
