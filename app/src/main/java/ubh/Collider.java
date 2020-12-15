package ubh;

import ubh.entity.Living;
import ubh.math.Shape;

public interface Collider {
	public void collide(Battlefield battlefield, Living.Entity entity, Shape hitbox);
}
