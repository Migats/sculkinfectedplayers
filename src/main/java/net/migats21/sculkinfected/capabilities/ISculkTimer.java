package net.migats21.sculkinfected.capabilities;

public interface ISculkTimer {
    int get();
    void set(int i);
    void setChanged(boolean transform);
    void tick();
    void copy(ISculkTimer sculkInfection);
    void setRelative(int deltaTime);
    float getDamage();
    void reset();
    void infect();
    void cure();

}
