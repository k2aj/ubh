package ubh.attack;

import java.awt.image.BufferedImage;
import java.util.Optional;

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
	protected final Optional<BufferedImage> beamSprite;
	protected final Vector2 beamSpriteRadii;
	
	protected Beam(Builder<?> builder) {
		super(builder);
		width = builder.width;
		length = builder.length;
		beamSprite = builder.beamSprite;
		beamSpriteRadii = builder.beamSpriteRadii.orElse(new Vector2(length*0.5f, width));
	}
	
	@Override
	public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		battlefield.spawn(new Entity(referenceFrame.deepCopy(), affiliation), deltaTLeft);
	}

	@SuppressWarnings("unchecked") 
    public static class Builder <This extends Builder<This>> extends AbstractProjectile.Builder<This> {
    	private float width = 2, length = 60;
    	private Optional<BufferedImage> beamSprite = Optional.empty();
    	private Optional<Vector2> beamSpriteRadii = Optional.empty();
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
		public This beamSprite(BufferedImage beamSprite) {
			this.beamSprite = Optional.ofNullable(beamSprite);
			return (This) this;
		}
		public This beamSpriteSize(Vector2 size) {
			this.beamSpriteRadii = size == null ? Optional.empty() : Optional.of(size.div(2));
			return (This) this;
		}
		@Override
		public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "width": width(json.asFloat()); break;
				case "length": length(json.asFloat()); break;
				case "beamSprite": beamSprite(registry.load(BufferedImage.class, json)); break;
				case "beamSpriteSize": beamSpriteSize(registry.load(Vector2.class, json)); break;
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
	
		private float lifetimeProgress() {
			return lifetime / maxLifetime;
		}
		
		private float currBeamRelativeRx() {
			return lifetimeProgress();
		}
		
		private float currBeamRx() {
			return 0.5f*length*currBeamRelativeRx();
		}
		
		private float currBeamRelativeRy() {
			return Math.max(0, (1 - 2*Math.abs(lifetimeProgress()-0.5f)));
		}
		
		private float currBeamRy() {
			return currBeamRelativeRy() * 0.5f * width;
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			super.update(battlefield, deltaT);
			
			// Update beam shape
			hitbox.setRadii(new Vector2(currBeamRx(), currBeamRy()));
			hitbox.setPosition(referenceFrame.getPosition().add(referenceFrame.getRotation().mul(currBeamRx())));
		}
		
		@Override
		protected Shape getHitbox() {
			return hitbox;
		}

		@Override
		public void draw(UBHGraphics g) {
			g.setColor(color);
			g.enableFill();
			if(beamSprite.isEmpty()) {
				hitbox.draw(g);
			} else {
				g.drawImage(
					beamSprite.get(), 
					hitbox.getPosition(), 
					beamSpriteRadii.mul(new Vector2(currBeamRelativeRx(), currBeamRelativeRy())), 
					hitbox.getRotation()
				);
			}
			if(sprite.isEmpty()) {
				g.drawCircle(referenceFrame.getPosition(), currBeamRy()*2);
			} else {
				g.drawImage(sprite.get(), getPosition(), spriteRadii.mul(currBeamRelativeRy()), hitbox.getRotation());
			}
		}
	}
	
	public static Beam fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
	}
}
