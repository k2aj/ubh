package ubh.math;

import ubh.UBHGraphics;

/** Axis-aligned bounding box. */
public final class AABB implements Shape {

    private Vector2 radii;
    private Vector2 center;

    private AABB(Vector2 center, Vector2 radii) {
        this.center = center;
        assert(Math.min(radii.x(), radii.y()) >= 0);
        this.radii = radii;
    }

    public static AABB centered(Vector2 center, Vector2 radii) {
        return new AABB(center, radii);
    }

    public static AABB lohi(Vector2 lo, Vector2 hi) {
        return new AABB(lo.add(hi).div(2), hi.sub(lo).div(2));
    }

    public Vector2 getRadii() {
        return radii;
    }

    @Override
    public boolean intersects(Shape other) {
        if(other instanceof AABB)
            return intersectsAABB((AABB)other);
        else if(other instanceof Circle)
            return ((Circle)other).intersects(this);
        else if(other instanceof Rectangle)
            return ((Rectangle)other).intersects(this);
        else
            throw new UnsupportedOperationException();
    }

    private boolean intersectsAABB(AABB other) {
        return Math.abs(other.getPosition().x() - getPosition().x()) <= radii.x() + other.radii.x() && 
               Math.abs(other.getPosition().y() - getPosition().y()) <= radii.y() + other.radii.y();
    }

    @Override
    public boolean contains(Vector2 point) {
        return Math.abs(point.x() - getPosition().x()) <= radii.x() && 
               Math.abs(point.y() - getPosition().y()) <= radii.y();
    }

    private static float clamp(float x, float min, float max) {
        return Math.min(Math.max(min,x),max);
    }

    @Override
    public Vector2 nearestPoint(Vector2 point) {
        return new Vector2(
            clamp(point.x(), getPosition().x()-radii.x(), getPosition().x()+radii.x()), 
            clamp(point.y(), getPosition().y()-radii.y(), getPosition().y()+radii.y())
        );
    }

    @Override
    public AABB deepCopy() {
        return new AABB(getPosition(), radii);
    }
    
    @Override
    public void draw(UBHGraphics graphics) {
        graphics.drawCenteredRect(getPosition(), radii);
    }

	@Override
	public Vector2 getPosition() {
		return center;
	}

	@Override
	public void setPosition(Vector2 position) {
		center = position;
	}
	
	@Override
	public AABB getBoundingBox() {
		return deepCopy();
	}

	public void setRadii(Vector2 radii) {
		if(radii.x() < 0 || radii.y() < 0)
			throw new IllegalArgumentException("Negative radii");
		this.radii = radii;
	}

	@Override
	public Vector2 randomPoint() {
		return center.add(radii.mul(new Vector2((float)Math.random()*2-1, (float)Math.random()*2-1)));
	}
}