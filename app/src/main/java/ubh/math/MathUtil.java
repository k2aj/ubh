package ubh.math;

public final class MathUtil {
    private MathUtil() {}

    /** @return true if parameters are approximately equal. */
    public static boolean approxEquals(float a, float b) {
        return Math.abs(a-b) < 1e-3f;
    }
    
    public static int clamp(int value, int min, int max) {
    	return Math.min(max, Math.max(min, value));
    }
}
