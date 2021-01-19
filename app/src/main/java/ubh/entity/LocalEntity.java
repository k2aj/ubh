package ubh.entity;

import ubh.Battlefield;
import ubh.math.ReferenceFrame;
import ubh.math.Vector2;

public abstract class LocalEntity extends Entity {
    protected ReferenceFrame referenceFrame;
    public LocalEntity(ReferenceFrame referenceFrame) {
        this.referenceFrame = referenceFrame;
    }
    @Override
    public void update(Battlefield battlefield, float deltaT) {
        referenceFrame.update(deltaT);
    }
    public Vector2 getPosition() {
    	return referenceFrame.getPosition();
    }
}