package ubh.entity;

import ubh.Battlefield;
import ubh.math.ReferenceFrame;
import ubh.math.Vector2;

public abstract class LocalEntity extends Entity {
	
    protected ReferenceFrame referenceFrame;
    private final Vector2 spawnPos;
    
    public LocalEntity(ReferenceFrame referenceFrame) {
        this.referenceFrame = referenceFrame;
        spawnPos = referenceFrame.getPosition();
    }
    
    @Override
    public void update(Battlefield battlefield, float deltaT) {
        referenceFrame.update(deltaT);
        // If the entity spawned in bounds, kill it when it goes out of bounds
        // If the entity spawned out of bounds, kill it when it goes out of bounds on the opposite side of the origin
        if(!inBounds(battlefield) && (spawnPos.dot(getPosition()) < 0 || battlefield.inBounds(spawnPos)))
        	die();
    }
    public Vector2 getPosition() {
    	return referenceFrame.getPosition();
    }
    public Vector2 getRotation() {
    	return referenceFrame.getRotation();
    }
    public Vector2 getVelocity() {
    	return referenceFrame.getVelocity();
    }
    protected boolean inBounds(Battlefield battlefield) {
    	return battlefield.inBounds(getPosition());
    }
}