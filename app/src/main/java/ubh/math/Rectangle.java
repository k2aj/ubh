package ubh.math;

import ubh.UBHGraphics;

public final class Rectangle implements Shape {

    private final AABB bounds;
    private Vector2 rotation;

    public Rectangle(Vector2 center, Vector2 radii, Vector2 rotation) {
        this.bounds = AABB.centered(center, radii);
        setRotation(rotation);
    }

    private Vector2 toAabbSpace(Vector2 point) {
        return point.sub(getPosition()).scaleRotate(rotation.x(), -rotation.y()).add(getPosition());
    }
    private Vector2 toWorldSpace(Vector2 point) {
        return point.sub(getPosition()).scaleRotate(rotation).add(getPosition());
    }
    private Rectangle rotatedBoundingRectangle(Vector2 resultRotation, Vector2 radiiIncrease) {
        Vector2 requiredRotation = rotation.scaleRotate(resultRotation.x(), -resultRotation.y()),
             v1 = bounds.getRadii().scaleRotate(requiredRotation),
             v2 = bounds.getRadii().mul(new Vector2(1,-1)).scaleRotate(requiredRotation);
        return new Rectangle(
            bounds.getPosition(),
            new Vector2(
                Math.max(Math.abs(v1.x()), Math.abs(v2.x())) + radiiIncrease.x(),
                Math.max(Math.abs(v1.y()), Math.abs(v2.y())) + radiiIncrease.y()
            ),
            resultRotation
        );
    }

    private boolean intersectsRectangle(Rectangle other) {
        Rectangle thisExpandedRect = rotatedBoundingRectangle(other.rotation, other.bounds.getRadii()),
                  otherExpandedRect = other.rotatedBoundingRectangle(this.rotation, this.bounds.getRadii());
        return thisExpandedRect.contains(other.getPosition()) && otherExpandedRect.contains(this.getPosition());
    }

    @Override
    public boolean intersects(Shape other) {
        if(other instanceof Circle) 
            return ((Circle) other).intersects(this);
        else if(other instanceof AABB) 
            return intersectsRectangle(new Rectangle(((AABB) other).getPosition(), ((AABB) other).getRadii(), Vector2.UNIT_X));
        else if(other instanceof Rectangle) 
            return intersectsRectangle((Rectangle) other);
        else throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Vector2 point) {
        return bounds.contains(toAabbSpace(point));
    }

    @Override
    public Vector2 nearestPoint(Vector2 point) {
        return toWorldSpace(bounds.nearestPoint(toAabbSpace(point)));
    }

    @Override
    public void draw(UBHGraphics graphics) {
        graphics.drawRotatedRect(bounds.getPosition(), bounds.getRadii(), rotation);
    }

    @Override
    public void setPosition(Vector2 position) {
        bounds.setPosition(position);
    }

    @Override
    public Vector2 getPosition() {
        return bounds.getPosition();
    }
    
    public Vector2 getRadii() {
    	return bounds.getRadii();
    }
    public void setRadii(Vector2 radii) {
    	bounds.setRadii(radii);
    }
    public Vector2 getRotation() {
    	return rotation;
    }
    public void setRotation(Vector2 rotation) {
    	if(!MathUtil.approxEquals(rotation.length2(), 1))
    		throw new IllegalArgumentException("Lenght of rotation vector not equal to 1");
    	this.rotation = rotation;
    }
    @Override
    public Rectangle deepCopy() {
        return new Rectangle(bounds.getPosition(), bounds.getRadii(), rotation);
    }
}
