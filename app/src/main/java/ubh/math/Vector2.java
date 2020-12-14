package ubh.math;

/** Represents a 2D floating point vector.
 *  Vector2 is immutable; methods such as normalize() etc. return new instances of Vector2 instead of mutating the current instance.
 */
public final class Vector2 {
    private final float x, y;

    /** Creates a Vector2 from a pair of cartesian coordinates.*/
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    /** Creates a Vector2 from polar coordinates.
     * @param arg    Argument of the created vector.
     * @param length Length of the created vector.
     */
    public static Vector2 polar(float arg, float length) {
        return new Vector2(
            length * (float) Math.cos(arg),
            length * (float) Math.sin(arg)
        );
    }

    public static final Vector2
        ZERO = new Vector2(0,0),
        UNIT_X = new Vector2(1,0),
        UNIT_Y = new Vector2(0,1);

    public float x() {
        return x;
    }
    public float y() {
        return y;
    }
    /** @return Squared length of this Vector2.*/
    public float length2() {
        return dot(this);
    }
    public float length() {
        return (float) Math.sqrt(length2());
    }
    /** @return Vector2 with same direction as this Vector2, but length equal to
     * targetLength. If length of this Vector2 is approximately equal to 0, the
     * direction of the returned Vector2 is undefined.
     */
    public Vector2 scaleTo(float targetLength) {
        float l = length();
        if(MathUtil.approxEquals(l, 0)) return new Vector2(targetLength,0);
        else return mul(targetLength/l);
    }
    /** @return Vector2 with same direction as this Vector2, but length equal to
     * 1. If length of this Vector2 is approximately equal to 0, the
     * direction of the returned Vector2 is undefined.
     */
    public Vector2 normalize() {
        return scaleTo(1);
    }
    public Vector2 add(Vector2 other) {
        return new Vector2(x+other.x, y+other.y);
    }
    public Vector2 sub(Vector2 other) {
        return new Vector2(x-other.x, y-other.y);
    }
    public Vector2 mul(float scalar) {
        return new Vector2(x*scalar, y*scalar);
    }
    public Vector2 div(float scalar) {
        return new Vector2(x/scalar, y/scalar);
    }
    public Vector2 mul(Vector2 other) {
        return new Vector2(x*other.x, y*other.y);
    }
    public Vector2 div(Vector2 other) {
        return new Vector2(x/other.x, y/other.y);
    }
    /** @return Dot product of this Vector2 and other. */
    public float dot(Vector2 other) {
        return x*other.x + y*other.y;
    }
    /** @return Squared distance between this Vector2 and other.*/
    public float distance2(Vector2 other) {
        return (x-other.x)*(x-other.x) + (y-other.y)*(y-other.y);
    }
    public float distance(Vector2 other) {
        return (float) Math.sqrt(distance2(other));
    }
    public Vector2 scaleRotate(float scaleCos, float scaleSin) {
        return new Vector2(x*scaleCos - y*scaleSin, x*scaleSin + y*scaleCos);
    }
    public Vector2 scaleRotate(Vector2 newXAxis) {
    	return scaleRotate(newXAxis.x(), newXAxis.y());
    }
    /** Rotates this Vector2 about [0,0].
     * @param angle How much to rotate, in radians.
     * @return The rotated Vector2.
    */
    public Vector2 rotate(float angle) {
        return scaleRotate((float) Math.cos(angle), (float) Math.sin(angle));
    }
    /** Reflects this Vector2 over a line.
     * @param normal Normal vector of the line. Length of normal must be approximately equal to 1.
     * @return The reflected vector.
     */
    public Vector2 reflect(Vector2 normal) {
        assert(MathUtil.approxEquals(normal.length2(), 1));
        return sub(normal.mul(2*dot(normal)));
    }
    @Override
    public boolean equals(Object other) {
        if(other instanceof Vector2) {
            var o = (Vector2) other;
            return MathUtil.approxEquals(x, o.x) && MathUtil.approxEquals(y, o.y);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%.3f, %.3f]", x, y);
    }
}
