package ubh.attack;

import java.awt.Color;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.Collider;
import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.entity.LocalEntity;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.Circle;
import ubh.math.Rectangle;
import ubh.math.ReferenceFrame;
import ubh.math.Shape;
import ubh.math.Vector2;

public abstract class AbstractProjectile implements Attack {
	
	protected final float damage, maxLifetime;
	protected final int pierce;
	protected final Color color;
	
	protected final Attack 
		hitAttack,
		pierceDepletedAttack;
	
	protected AbstractProjectile (Builder<?> builder) {
		this.damage = builder.damage;
		this.maxLifetime = builder.maxLifetime;
		this.pierce = builder.pierce;
		this.color = builder.color;
		this.hitAttack = builder.hitAttack;
		this.pierceDepletedAttack = builder.pierceDepletedAttack;
	}

    @SuppressWarnings("unchecked") 
    public static abstract class Builder <This extends Builder<This>> {
    	private float 
			damage = 100, 
			maxLifetime = 10;
		private int pierce = 1;
		private Color color = Color.WHITE;
		private Shape hitbox = new Circle(Vector2.ZERO, 5);
		private Attack 
			hitAttack = Attack.NULL,
			pierceDepletedAttack = Attack.NULL;
		
		public abstract AbstractProjectile build();
		
		public This damage(float damage) {
			this.damage = damage;
			return (This) this;
		}
		public This maxLifetime(float maxLifetime) {
			this.maxLifetime = maxLifetime;
			return (This) this;
		}
		public This pierce(int pierce) {
			this.pierce = pierce;
			return (This) this;
		}
		public This color(Color color) {
			this.color = color;
			return (This) this;
		}
		public This hitbox(Shape hitbox) {
			this.hitbox = hitbox.deepCopy();
			return (This) this;
		}
		public This hitAttack(Attack attack) {
			hitAttack = attack;
			return (This) this;
		}
		public This pierceDepletedAttack(Attack attack) {
			pierceDepletedAttack = attack;
			return (This) this;
		}
		protected void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "damage": 		damage(json.asFloat()); 	 			      break;
				case "maxLifetime": maxLifetime(json.asFloat()); 			      break;
				case "pierce":		pierce((int)json.asFloat()); 		          break;
				case "color":		color(registry.load(Color.class, json));      break;
				case "hitAttack":   hitAttack(registry.load(Attack.class, json)); break;
				case "pierceDepletedAttack": pierceDepletedAttack(registry.load(Attack.class, json)); break;
			}
		}
		protected This loadJson(ContentRegistry registry, JsonValue json) throws ContentException {
			for(var member : json.asObject())
				loadFieldFromJson(member.getName(), registry, member.getValue());
			return (This) this;
		}
    }
    
    public abstract class Entity extends LocalEntity implements Collider {
    	
    	protected final Affiliation affiliation;
    	protected boolean outOfBounds = false;
    	protected float lifetime = 0;
    	protected int pierce;
    	private AutoCloseable collider;

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame);
			this.affiliation = affiliation;
			this.pierce = AbstractProjectile.this.pierce;
		}
		
		protected abstract Shape getHitbox();
		
		@Override
		public void onSpawned(Battlefield battlefield) {
			collider = battlefield.getCollisionSystem().registerCollider(this, getHitbox(), affiliation);
		}
		
		@Override
		public void onDespawned(Battlefield battlefield) {
			// Destroy collider
			if(collider != null)
				try {
					// This should never throw
					collider.close();
				} catch(Exception e) {
					throw new Error("This should never happen", e);
				}
		}
		
		@Override
		public void collide(Battlefield battlefield, Living.Entity entity, Shape hitbox) {
			if(pierce != 0) {
				entity.damage(damage);
				hitAttack.attack(battlefield, referenceFrame, affiliation, 0);
				if(pierce > 0) {
					--pierce;
					if(pierce == 0)
						pierceDepletedAttack.attack(battlefield, referenceFrame, affiliation, 0);
				}
			}
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			super.update(battlefield, deltaT);
			if(!battlefield.inBounds(getHitbox()))
				outOfBounds = true;
			lifetime += deltaT;
		}

		@Override
		public boolean isDead() {
			return outOfBounds || lifetime >= maxLifetime || pierce == 0;
		}
    }
}