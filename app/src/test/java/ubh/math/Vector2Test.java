import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ubh.math.Vector2;
import ubh.math.MathUtil;

public class Vector2Test {

    private final Vector2
        a = new Vector2(3,4), 
        b = new Vector2(5,6);

    @Test
    public void testComponentWiseOperations() {
        assertEquals(new Vector2(1.337f, 0.42f), new Vector2(1.3379f, 0.4209f));
        assertEquals(new Vector2(8,10), a.add(b));
        assertEquals(new Vector2(-2,-2), a.sub(b));
        assertEquals(new Vector2(6,8), a.mul(2));
        assertEquals(new Vector2(1, 1.3333f), a.div(3));
    }

    @Test
    public void testVectorOperations() {
        assertTrue(MathUtil.approxEquals(25, a.length2()));
        assertTrue(MathUtil.approxEquals(5, a.length()));
        assertTrue(MathUtil.approxEquals(8, a.distance2(b)));
        assertTrue(MathUtil.approxEquals(2*(float)Math.sqrt(2), a.distance(b)));
        assertTrue(MathUtil.approxEquals(39, a.dot(b)));
        assertEquals(new Vector2(0.6f, 0.8f), a.normalize());
        assertEquals(new Vector2(6f, 8f), a.scaleTo(10));
        assertEquals(new Vector2(4,3), a.reflect(new Vector2(1,-1).normalize()));
        assertEquals(new Vector2(1,1).normalize(), Vector2.UNIT_X.rotate(1/4f*(float)Math.PI));
    }
}
