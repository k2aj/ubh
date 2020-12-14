package ubh.math;

import ubh.UBHGraphics;

public final class Circle implements Shape {

    private float radius;
	private Vector2 center;

    public Circle(Vector2 center, float radius) {
        setPosition(center);
        setRadius(radius);
    }

    @Override
    public boolean intersects(Shape other) {
        return other.nearestPoint(getPosition()).distance2(getPosition()) <= radius*radius;
    }

    @Override
    public boolean contains(Vector2 point) {
        return getPosition().distance2(point) <= radius*radius;
    }

    @Override
    public Vector2 nearestPoint(Vector2 point) {
        if(contains(point))
            return point;
        else 
            return point.sub(getPosition()).scaleTo(radius).add(getPosition());
    }
    
    @Override
    public Circle deepCopy() {
        return new Circle(getPosition(), radius);
    }

    @Override
    public void draw(UBHGraphics graphics) {
        graphics.drawCircle(getPosition(), radius);
    }

	@Override
	public Vector2 getPosition() {
		return center;
	}

	@Override
	public void setPosition(Vector2 position) {
		center = position;
	}
	
    public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		if(radius < 0)
			throw new IllegalArgumentException("Negative radius");
		this.radius = radius;
	}
}
