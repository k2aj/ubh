package ubh.entity;

import ubh.Battlefield;
import ubh.UBHGraphics;

public abstract class Entity {
    public abstract void update(Battlefield battlefield, float deltaT);
    public abstract boolean isDead();
    public abstract void draw(UBHGraphics graphics);

    public void onSpawned(Battlefield battlefield) {}
    public void onDespawned(Battlefield battlefield) {}
}
