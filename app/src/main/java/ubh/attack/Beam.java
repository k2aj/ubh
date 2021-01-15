package ubh.attack;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.UBHGraphics;
import ubh.entity.Affiliation;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.Rectangle;
import ubh.math.ReferenceFrame;
import ubh.math.Shape;
import ubh.math.Vector2;

/* TODO:
 * - make beams shorter if they hit their pierce cap
 */
public class Beam extends AbstractProjectile {
	
	protected final float width, length;
	
	protected Beam(Builder<?> builder) {
		super(builder);
		width = builder.width;
		length = builder.length;
	}
	
	@Override
	public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		battlefield.spawn(new Entity(referenceFrame.deepCopy(), affiliation), deltaTLeft);
	}

	@SuppressWarnings("unchecked") 
    public static class Builder <This extends Builder<This>> extends AbstractProjectile.Builder<This> {
    	private float width = 2, length = 60;
    	{
    		pierce(50);
    		maxLifetime(0.5f);
    	}
		
    	@Override
		public Beam build() {
			return new Beam(this);
		}
		
		public This width(float width) {
			this.width = width;
			return (This) this;
		}
		public This length(float length) {
			this.length = length;
			return (This) this;
		}
		@Override
		public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "width": width(json.asFloat()); break;
				case "length": length(json.asFloat()); break;
				default: super.loadFieldFromJson(field, registry, json);
			}
		}
    }
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	public class Entity extends AbstractProjectile.Entity {
		
		private final Rectangle hitbox;

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame, affiliation);
			hitbox = new Rectangle(referenceFrame.getPosition(), Vector2.ZERO, referenceFrame.getRotation());
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			super.update(battlefield, deltaT);
			
			// Update beam shape
			float lifetimeProgress = lifetime / maxLifetime;
			float currLength = length*lifetimeProgress;
			float currWidth = Math.max(0, (1 - 2*Math.abs(lifetimeProgress-0.5f))*width);
			
			hitbox.setRadii(new Vector2(currLength/2, currWidth/2));
			hitbox.setPosition(referenceFrame.getPosition().add(referenceFrame.getRotation().mul(currLength/2)));
		}
		
		@Override
		protected Shape getHitbox() {
			return hitbox;
		}

		@Override
		public void draw(UBHGraphics g) {
			g.setColor(color);
			g.enableFill();
			hitbox.draw(g);
			g.drawCircle(referenceFrame.getPosition(), hitbox.getRadii().y()*1.5f);
		}
	}
	
	public static Beam fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
	}
}
