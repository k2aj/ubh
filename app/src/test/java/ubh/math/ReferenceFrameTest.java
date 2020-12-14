package ubh.math;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ReferenceFrameTest {
	@Test
	public void test() {
		var r1 = new ReferenceFrame(Vector2.ZERO, new Vector2(1,2));
		r1.update(5);
		assertEquals(new Vector2(5,10), r1.getPosition());
		assertEquals(new Vector2(8,16), r1.getPositionAfter(3));
	}
}
