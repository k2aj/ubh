package ubh.math;

import ubh.UBHGraphics;

public interface Shape {
	
    /**@param other
     * @return true if this Shape and other Shape intersect.
     */
    public boolean intersects(Shape other);
    
    /**@param point
     * @return true if point is inside this Shape.
     */
    public boolean contains(Vector2 point);
    
    /**@param point
     * @return Point belonging to this Shape which lies closest to given point.
     */
    public Vector2 nearestPoint(Vector2 point);
    
    /** @return Position of this shape; usually the center of the shape or a point close to the center.*/
    public Vector2 getPosition();
    public void setPosition(Vector2 position);
    
    /** Draws this Shape.
     *  @implSpec
     *  Implementations only call drawing methods on the graphics object and not change any other state,
     *  to allow this state to be specified by the caller.
     */
    public void draw(UBHGraphics graphics);
    
    /** @return Deep copy of this Shape. */
    public Shape deepCopy();
}
