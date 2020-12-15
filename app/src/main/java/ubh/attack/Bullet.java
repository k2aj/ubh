package ubh.attack;

import ubh.Battlefield;
import ubh.UBHGraphics;
import ubh.entity.Affiliation;
import ubh.math.ReferenceFrame;

public class Bullet extends AbstractProjectile {

	protected final boolean bouncy;
	
	protected Bullet(Builder<?> builder) {
		super(builder);
		this.bouncy = builder.bouncy;
	}
	
	@Override
	public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		battlefield.spawn(new Entity(referenceFrame.deepCopy(), affiliation), deltaTLeft);
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}

	@SuppressWarnings("unchecked") 
    public static class Builder <This extends Builder<This>> extends AbstractProjectile.Builder<This> {
    	private boolean bouncy = false;
		
    	@Override
		public Bullet build() {
			return new Bullet(this);
		}
		
		public This bouncy(boolean bouncy) {
			this.bouncy = bouncy;
			return (This) this;
		}
    }
	
	public class Entity extends AbstractProjectile.Entity {

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame, affiliation);
		}

		@Override
		public void draw(UBHGraphics g) {
			g.setColor(color);
			g.enableFill();
			hitbox.draw(g);
		}
	}
}
