package net.migats21.sculkinfected.capabilities;

import net.minecraft.server.level.ServerPlayer;

public interface ISculkTimer {
    public int get();
    public void set(int i);
    public void setChanged(boolean transform);
    public void tick();
    public void copy(ISculkTimer sculkInfection);
    public void setRelative(int deltaTime);
    public float getDamage();
    public void reset();
    public void infect();
    public void cure();

}
