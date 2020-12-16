package ubh.attack;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.UBHGraphics;
import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.Rectangle;
import ubh.math.ReferenceFrame;
import ubh.math.Shape;

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
		
		protected void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			if(field.equals("bouncy"))
				bouncy = json.asBoolean();
			else super.loadFieldFromJson(field, registry, json);
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
		
		@Override
		public void collide(Battlefield battlefield, Living.Entity entity, Shape hitbox) {
			super.collide(battlefield, entity, hitbox);
			if(bouncy) {
				final var normal = referenceFrame.getPosition().sub(hitbox.getPosition()).normalize();
				referenceFrame.setVelocity(referenceFrame.getVelocity().reflect(normal));
				referenceFrame.setRotation(referenceFrame.getVelocity().normalize());
				if(this.hitbox instanceof Rectangle)
					((Rectangle) this.hitbox).setRotation(referenceFrame.getRotation());
			}
		}
	}
	
	public static Bullet fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		return builder().loadJson(registry, json).build();
	}
}
