package ubh.attack;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.UBHGraphics;
import ubh.entity.Affiliation;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.Circle;
import ubh.math.ReferenceFrame;
import ubh.math.Shape;

public class Explosion extends AbstractProjectile {
	
	private final float radius;
	
    protected Explosion(Builder<?> builder) {
		super(builder);
		this.radius = builder.radius;
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
		
		private float radius = 1;
		{
			maxLifetime(0.2f);
			pierce(50);
		}
		
    	@Override
		public Explosion build() {
			return new Explosion(this);
		}
    	
		public This radius(float radius) {
    		this.radius = radius;
    		return (This) this;
    	}
		@Override
		public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "radius": radius(json.asFloat()); break;
				default: super.loadFieldFromJson(field, registry, json);
			}
		}
    }

	public class Entity extends AbstractProjectile.Entity {
		
		private final Circle hitbox;

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame, affiliation);
			hitbox = new Circle(referenceFrame.getPosition(),0);
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			super.update(battlefield, deltaT);
			hitbox.setRadius(Math.max(0, radius * (1 - 2*Math.abs(lifetime/maxLifetime - 0.5f))));
			hitbox.setPosition(referenceFrame.getPosition());
		}

		@Override
		public void draw(UBHGraphics g) {
			if(sprite.isEmpty()) {
				g.setColor(color);
				g.enableFill();
				hitbox.draw(g);
			} else {
				g.drawImage(sprite.get(), getPosition(), spriteRadii.mul(hitbox.getRadius()/radius), getRotation());
			}
		}

		@Override
		protected Shape getHitbox() {
			return hitbox;
		}
	}
	
	public static Explosion fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
	}
}
