package ubh.attack;

import java.awt.Color;

import ubh.Battlefield;
import ubh.Collider;
import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.entity.LocalEntity;
import ubh.math.Circle;
import ubh.math.Rectangle;
import ubh.math.ReferenceFrame;
import ubh.math.Shape;
import ubh.math.Vector2;

public abstract class AbstractProjectile implements Attack {
	
	protected final float damage, maxLifetime;
	protected final int pierce;
	protected final Color color;
	protected final Shape hitbox;
	
	protected AbstractProjectile (Builder<?> builder) {
		this.damage = builder.damage;
		this.maxLifetime = builder.maxLifetime;
		this.pierce = builder.pierce;
		this.color = builder.color;
		this.hitbox = builder.hitbox.deepCopy();
	}

    @SuppressWarnings("unchecked") 
    public static abstract class Builder <This extends Builder<This>> {
    	private float 
			damage = 100, 
			maxLifetime = 10;
		private int pierce = 1;
		private Color color = Color.WHITE;
		private Shape hitbox = new Circle(Vector2.ZERO, 5);
		
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
    }
    
    public abstract class Entity extends LocalEntity implements Collider {
    	
    	protected final Affiliation affiliation;
    	protected final Shape hitbox;
    	protected boolean outOfBounds = false;
    	protected float lifetime = 0;
    	protected int pierce;
    	private AutoCloseable collider;

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame);
			this.affiliation = affiliation;
			this.hitbox = AbstractProjectile.this.hitbox.deepCopy();
			this.pierce = AbstractProjectile.this.pierce;
			if(this.hitbox instanceof Rectangle)
				((Rectangle) this.hitbox).setRotation(referenceFrame.getRotation());
		}
		
		@Override
		public void onSpawned(Battlefield battlefield) {
			collider = battlefield.getCollisionSystem().registerCollider(this, hitbox, affiliation);
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
				if(pierce > 0)
					--pierce;
			}
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT) {
			super.update(battlefield, deltaT);
			hitbox.setPosition(referenceFrame.getPosition());
			if(!battlefield.inBounds(hitbox))
				outOfBounds = true;
			lifetime += deltaT;
		}

		@Override
		public boolean isDead() {
			return outOfBounds || lifetime >= maxLifetime || pierce == 0;
		}
    }
}