package ubh.math;

import org.hjson.JsonValue;

import ubh.UBHGraphics;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;

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
	
	@Override
	public AABB getBoundingBox() {
		return AABB.centered(center, new Vector2(radius,radius));
	}
	
	public static Circle fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		float x=0, y=0, r=5;
		for(var member : json.asObject()) {
			switch(member.getName()) {
			case "center":
				var center = registry.load(Vector2.class, member.getValue());
				x = center.x();
				y = center.y();
				break;
			case "x":
				x = member.getValue().asFloat();
				break;
			case "y":
				y = member.getValue().asFloat();
				break;
			case "radius":
				r = member.getValue().asFloat();
				break;
			}
		}
		return new Circle(new Vector2(x,y),r);
	}
}
