package ubh.entity;

import ubh.Battlefield;
import ubh.UBHGraphics;

public abstract class Entity {
	
	private boolean dead = false;
	
    public abstract void update(Battlefield battlefield, float deltaT);
    public final boolean isDead() {
    	return dead;
    }
    protected final void die() {
    	dead = true;
    }
    public abstract void draw(UBHGraphics graphics);

    public void onSpawned(Battlefield battlefield) {}
    public void onDespawned(Battlefield battlefield) {}
}
