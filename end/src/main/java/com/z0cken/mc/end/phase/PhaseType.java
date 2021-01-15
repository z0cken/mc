package com.z0cken.mc.end.phase;


import com.z0cken.mc.end.End;

public enum PhaseType {
    DRAGON(DragonPhase.class), COMBAT(CombatPhase.class), MAINTENANCE(MaintenancePhase.class);

    private Class<? extends EndPhase> clazz;
    private PhaseType follower;

    static {
        DRAGON.follower = COMBAT;
        COMBAT.follower = DRAGON;
        MAINTENANCE.follower = DRAGON;
    }

    PhaseType(Class<? extends EndPhase> clazz) {
        this.clazz = clazz;
    }

    public EndPhase make(End end) throws Exception {
        return clazz.getDeclaredConstructor(End.class).newInstance(end);
    }

    public PhaseType getNext() {
        return follower;
    }
}
