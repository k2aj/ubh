package ubh.math;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ShapeTest {
	@Test
	public void testIntersects() {
		// CIRCLE
		// Distance between circle centers = 15
		var c1 = new Circle(new Vector2(0,0), 10);
		var c2 = new Circle(new Vector2(9,12), 5.1f);
		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
		
		c2.setRadius(4.9f);
		assertFalse(c1.intersects(c2));
		assertFalse(c2.intersects(c1));
		
		// Move c2 inside c1
		c2.setPosition(new Vector2(0,0));
		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
		
		// AABB
		var a1 = AABB.centered(new Vector2(0,0), new Vector2(10,10));
		var a2 = AABB.centered(new Vector2(15,5), new Vector2(5.1f,5));
		assertTrue(a1.intersects(a2));
		assertTrue(a2.intersects(a1));
		
		a2.setRadii(new Vector2(4.9f,5f));
		assertFalse(a1.intersects(a2));
		assertFalse(a2.intersects(a1));
		
		// Move a2 inside a1
		a2.setPosition(new Vector2(0,0));
		assertTrue(a1.intersects(a2));
		assertTrue(a2.intersects(a1));
		
		// RECTANGLE
		var r1 = new Rectangle(new Vector2(5,0), new Vector2(8,1), Vector2.polar(0.25f*(float)Math.PI, 1));
		var r2 = new Rectangle(new Vector2(-5,0), new Vector2(8,1), Vector2.polar(-0.25f*(float)Math.PI, 1));
		assertTrue(r1.intersects(r2));
		assertTrue(r2.intersects(r1));
		
		r1.setRotation(Vector2.UNIT_Y);
		
		assertFalse(r1.intersects(r2));
		assertFalse(r2.intersects(r1));
	}
			
}
