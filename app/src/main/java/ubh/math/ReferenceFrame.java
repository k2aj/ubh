package ubh.math;

/** Represents a moving coordinate system in 2D space. */
public class ReferenceFrame {
    private Vector2 position, velocity, rotation;

    public ReferenceFrame(Vector2 position, Vector2 velocity, Vector2 orientation) {
        this.setPosition(position);
        this.setVelocity(velocity);
        this.setRotation(orientation);
    }
    
    public ReferenceFrame(Vector2 position, Vector2 velocity) {
        this(position, velocity, velocity.normalize());
    }
    
    public ReferenceFrame(Vector2 position) {
        this(position, Vector2.ZERO, Vector2.UNIT_X);
    }

    public void update(float deltaT) {
        position = getPositionAfter(deltaT);
    }
    
    /**
     * @param deltaT
     * @returns Future position of the origin of this ReferenceFrame's coordinate system.
     */
    public Vector2 getPositionAfter(float deltaT) {
        return position.add(velocity.mul(deltaT));
    }

    public ReferenceFrame deepCopy() {
        return new ReferenceFrame(position, velocity, rotation);
    }

    /** @returns Vector representing the X axis of this ReferenceFrame's coordinate system.
     *  Length of this vector will always be equal to 1.*/
    public Vector2 getRotation() {
        return rotation;
    }

    /** @see ReferenceFrame#getRotation() */
    public ReferenceFrame setRotation(Vector2 rotation) {
    	if(!MathUtil.approxEquals(rotation.length2(), 1))
    		throw new IllegalArgumentException("Lenght of rotation vector not equal to 1");
        this.rotation = rotation;
        return this;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public ReferenceFrame setVelocity(Vector2 velocity) {
        this.velocity = velocity;
        return this;
    }

    /** @returns Position of the origin of this ReferenceFrame's coordinate system. */
    public Vector2 getPosition() {
        return position;
    }

    /** @see ReferenceFrame#getPosition() */
    public ReferenceFrame setPosition(Vector2 position) {
        this.position = position;
        return this;
    }
}
