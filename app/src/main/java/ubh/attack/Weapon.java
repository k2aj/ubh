package ubh.attack;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Affiliation;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.loader.JsonLoadable;
import ubh.math.ReferenceFrame;

public final class Weapon {
	private final float reloadTime, fireTime, velocity, inheritVelocity;
	private final int maxAmmo;
	private final Attack attack;
	
	public Weapon(Builder builder) {
		this.attack = builder.attack;
        this.reloadTime = builder.reloadTime;
        this.fireTime = builder.fireTime;
        this.velocity = builder.velocity;
        this.maxAmmo = builder.maxAmmo;
        this.inheritVelocity = builder.inheritVelocity;
	}
	
	public State createState() {
		return new State();
	}
	
	public static Builder builder() {
        return new Builder();
    }
	
	public static final class Builder implements JsonLoadable {
        private Attack attack = Attack.NULL;
        private float reloadTime = 1, fireTime = 0.1f, velocity = 15, inheritVelocity = 1f;
        private int maxAmmo = 1;
        public Builder attack(Attack attack) {
            this.attack = attack;
            return this;
        }
        public Builder reloadTime(float reloadTime) {
            this.reloadTime = reloadTime;
            return this;
        }
        public Builder reloadRate(float reloadRate) {
            this.reloadTime = 1/reloadRate;
            return this;
        }
        public Builder fireTime(float fireTime) {
            this.fireTime = fireTime;
            return this;
        }
        public Builder fireRate(float fireRate) {
            fireTime = 1/fireRate;
            return this;
        }
        public Builder maxAmmo(int maxAmmo) {
            this.maxAmmo = maxAmmo;
            return this;
        }
        public Builder velocity(float velocity) {
            this.velocity = velocity;
            return this;
        }
        public Builder inheritVelocity(float fraction) {
            this.inheritVelocity = fraction;
            return this;
        }
        public Weapon build() {
            return new Weapon(this);
        }
		@Override
		public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
			case "attack": attack(registry.load(Attack.class, json)); break;
			case "reloadTime": reloadTime(json.asFloat()); break;
			case "reloadRate": reloadRate(json.asFloat()); break;
			case "fireTime": fireTime(json.asFloat()); break;
			case "fireRate": fireRate(json.asFloat()); break;
			case "maxAmmo": maxAmmo(json.asInt()); break;
			case "velocity": velocity(json.asFloat()); break;
			case "inheritVelocity": inheritVelocity(json.asFloat()); break;
			}
		}
    }
	
	public class State {
		private int ammo;
        private float reloadLeft = 0;
        private State() {
            ammo = maxAmmo;
        }
        public void update(float deltaT) {
            reloadLeft = Math.max(0, reloadLeft - deltaT);
        }
        public void fire(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaT) {
            final var attackFrame = referenceFrame.deepCopy();
            attackFrame.setVelocity(attackFrame.getRotation().scaleTo(velocity).add(referenceFrame.getVelocity().mul(inheritVelocity)));
            while(reloadLeft < deltaT) {
                if(ammo == 0)
                    ammo = maxAmmo;
                attackFrame.setPosition(referenceFrame.getPositionAfter(reloadLeft));
                attack.attack(battlefield, attackFrame, affiliation, deltaT - reloadLeft);
                --ammo;
                if(ammo > 0) 
                    reloadLeft += fireTime;
                else
                    reloadLeft += reloadTime;
            }
        }
	}

	public static Weapon fromJson(ContentRegistry registry, JsonValue json) {
		var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
	}
}
