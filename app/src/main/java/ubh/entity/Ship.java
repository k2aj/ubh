package ubh.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.UBHGraphics;
import ubh.attack.Weapon;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.ReferenceFrame;
import ubh.math.Vector2;
import ubh.entity.ai.AI;

public class Ship extends Living {
	
	protected final Vector2 maxThrust;
	protected final float friction;
	protected final List<Weapon> weapons;
	protected final AI ai;
	protected final Optional<BufferedImage> sprite;
	protected final Vector2 spriteRadii;
	
	protected Ship(Builder<?> builder) {
		super(builder);
		this.maxThrust = builder.maxThrust;
		this.friction = builder.friction;
		this.weapons = new ArrayList<>(builder.weapons);
		this.ai = builder.ai;
		this.sprite = builder.sprite;
		this.spriteRadii = builder.spriteRadii;
	}
	public static Builder<?> builder() {
        return new Builder<>();
    }
    @SuppressWarnings("unchecked") 
    public static class Builder <This extends Builder<This>> extends Living.Builder<This> {
    	
    	private Vector2 maxThrust = new Vector2(100,100);
    	private float friction = 0.8f;
    	private List<Weapon> weapons = List.of();
    	private AI ai = AI.NULL;
    	private Optional<BufferedImage> sprite = Optional.empty();
    	private Vector2 spriteRadii = new Vector2(5,5);
    	
    	public This maxThrust(Vector2 maxThrust) {
    		this.maxThrust = maxThrust;
    		return (This) this;
    	}
    	
    	public This weapons(List<Weapon> weapons) {
    		this.weapons = new ArrayList<Weapon>(weapons);
    		return (This) this;
    	}
    	
    	public This friction(float friction) {
    		this.friction = friction;
    		return (This) this;
    	}
    	
    	public This ai(AI ai) {
    		this.ai = ai;
    		return (This) this;
    	}
    	
    	public This sprite(BufferedImage sprite) {
    		this.sprite = Optional.ofNullable(sprite);
    		return (This) this;
    	}
    	
    	public This spriteSize(Vector2 size) {
    		this.spriteRadii = size.div(2);
    		return (This) this;
    	}
    	
    	@Override
		public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "maxThrust": maxThrust(registry.load(Vector2.class, json)); break;
				case "weapons": 
					weapons(
						json.asArray().values().stream()
						.map(elem -> registry.load(Weapon.class, elem))
						.collect(Collectors.toList())
					);
				break;
				case "friction": friction(json.asFloat()); break;
				case "ai": ai(registry.load(AI.class, json)); break;
				case "sprite": sprite(registry.load(BufferedImage.class, json)); break;
				case "spriteSize": spriteSize(registry.load(Vector2.class, json)); break;
				default: super.loadFieldFromJson(field, registry, json);
			}
		}
    	
		@Override
		public Ship build() {
			return new Ship(this);
		}
    }
    
	@Override
	public Entity createEntity(ReferenceFrame referenceFrame, Affiliation affiliation) {
		return new Entity(referenceFrame, affiliation);
	}
	
    public class Entity extends Living.Entity {
    	
    	private Vector2 thrust = Vector2.ZERO;
    	private List<Weapon.State> weaponStates; 
    	private AI.State aiState;

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame, affiliation);
			weaponStates = weapons.stream().map(Weapon::createState).collect(Collectors.toList());
			setAI(ai);
		}
		
		public void setThrust(Vector2 thrust) {
			this.thrust = thrust;
		}
		
		public void flyTo(Vector2 targetPosition) {
			Vector2 displacementNeeded = targetPosition.sub(getPosition());
			var dumbThrustEstimate = displacementNeeded.div(maxThrust.length()).mul(friction);
			if(dumbThrustEstimate.length2() > 1)
				dumbThrustEstimate = dumbThrustEstimate.normalize();
			setThrust(dumbThrustEstimate);
		}
		
		public int weaponCount() {
			return weaponStates.size();
		}
		
		public void fireWeapon(Battlefield battlefield, float deltaT, int weaponIndex, Vector2 target) {
			var weaponRframe = referenceFrame.deepCopy();
			weaponRframe.setRotation(target.sub(weaponRframe.getPosition()).normalize());
			weaponStates.get(weaponIndex).fire(battlefield, weaponRframe, affiliation, deltaT);
		}
		
		public void setAI(AI ai) {
			aiState = ai.createState();
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			aiState.update(battlefield, deltaT, this);
			Vector2 acceleration = thrust.mul(maxThrust);
			referenceFrame.setVelocity(referenceFrame.getVelocity().add(acceleration.mul(deltaT)));
			referenceFrame.setVelocity(referenceFrame.getVelocity().mul((float) Math.pow(1-friction, deltaT)));
			super.update(battlefield, deltaT);
			for(var w : weaponStates)
				w.update(deltaT);
		}
		
		@Override
		public void draw(UBHGraphics g) {
			if(sprite.isEmpty()) {
				g.setColor(affiliation == Affiliation.FRIENDLY ? Color.green : Color.red);
				g.enableFill();
				hitbox.draw(g);
			} else {
				g.drawImage(sprite.get(), hitbox.getPosition(), spriteRadii, getRotation());
			}
		}
    }

    public static Ship fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
    	var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
    }

}
