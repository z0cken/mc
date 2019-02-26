package com.z0cken.mc.core.util;

import com.z0cken.mc.core.CoreBridge;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public abstract class CoreTask implements Runnable {

    private boolean async;
    private TimeUnit timeUnit;
    private Long delay, interval;
    private int id;

    public CoreTask(boolean async) {
        this(async, null, null, null, false);
    }

    public CoreTask(boolean async, @Nonnull TimeUnit timeUnit, Long delay) {
        this(async, timeUnit, null, delay, false);
    }

    public CoreTask(boolean async, @Nonnull TimeUnit timeUnit, Long delay, Long interval) {
        this(async, timeUnit, interval, delay, false);
    }

    private CoreTask(boolean async, TimeUnit timeUnit, Long interval, Long delay, boolean flag) {
        this.async = async;
        this.timeUnit = timeUnit;
        this.delay = delay;
        this.interval = interval;
    }

    public boolean isAsync() {
        return async;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Long getDelay() {
        return delay;
    }

    public Long getInterval() {
        return interval;
    }

    protected void cancel() {
        CoreBridge.getPlugin().cancelTask(id);
    }

    public void schedule() {
        id = CoreBridge.getPlugin().schedule(this);
    }

    @Override
    public abstract void run();

}
