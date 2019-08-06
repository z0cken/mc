package com.z0cken.mc.end.phase;

import com.z0cken.mc.end.End;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public abstract class EndPhase {

    private final PhaseType type;
    private final End end;
    protected final Set<BukkitTask> tasks = new HashSet<>();

    protected EndPhase(PhaseType type, End end) {
        this.type = type;
        this.end = end;
    }

    public abstract void start();

    public void stop() {
        tasks.forEach(BukkitTask::cancel);
    }

    public PhaseType getType() {
        return type;
    }

    protected End getEnd() {
        return end;
    }

    protected void runNextPhase() {
        getEnd().runPhase(getType().getNext());
    }
}
