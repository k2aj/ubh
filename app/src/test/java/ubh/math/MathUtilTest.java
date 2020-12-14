package ubh.math;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class MathUtilTest {
    @Test
    public void testApproxEquals() {
        assertTrue(MathUtil.approxEquals(1.2345f, 1.2346f));
        assertFalse(MathUtil.approxEquals(1,2));
    }
}
