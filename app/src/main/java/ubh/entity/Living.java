package ubh.entity;

import ubh.math.Shape;
import ubh.math.Vector2;
import ubh.math.AABB;
import ubh.math.Rectangle;
import ubh.math.ReferenceFrame;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.attack.Attack;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.loader.JsonLoadable;

public abstract class Living implements Attack {
	
	protected final float maxHealth;
	protected final Shape hitbox;
	protected final Attack deathAttack;
	
	protected Living(Builder<?> builder) {
		maxHealth = builder.maxHealth;
		hitbox = builder.hitbox.deepCopy();
		deathAttack = builder.deathAttack;
	}
	
    @SuppressWarnings("unchecked")
    public static abstract class Builder<This extends Builder<This>> implements JsonLoadable {
        protected float maxHealth = 1000;
        protected Shape hitbox = AABB.centered(Vector2.ZERO, new Vector2(5,5));
        protected Attack deathAttack = Attack.NULL;

        public This maxHealth(float maxHealth) {
            this.maxHealth = maxHealth;
            return (This) this;
        }
        public This hitbox(Shape hitbox) {
            this.hitbox = hitbox;
            return (This) this;
        }
        public This deathAttack(Attack deathAttack) {
        	this.deathAttack = deathAttack;
            return (This) this;
        }
        public abstract Living build();
        
        @Override
        public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "maxHealth": 	maxHealth(json.asFloat()); 	 			  		 break;
				case "deathAttack":	deathAttack(registry.load(Attack.class, json));  break;
				case "hitbox":		hitbox(registry.load(Shape.class, json)); 		 break;
			}
		}
    }
    
    public abstract Entity createEntity(ReferenceFrame referenceFrame, Affiliation affiliation);
    @Override
	public final void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		battlefield.spawn(createEntity(referenceFrame.deepCopy(), affiliation), deltaTLeft);
	}

	public abstract class Entity extends LocalEntity {
	
	    private float health;
	    protected final Shape hitbox;
	    protected final Affiliation affiliation;
	    private AutoCloseable collider;
	
	    protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
	        super(referenceFrame);
	        this.health = maxHealth;
	        this.hitbox = Living.this.hitbox.deepCopy();
	        this.affiliation = affiliation;
	    }
	    
		@Override
		public void onSpawned(Battlefield battlefield) {
			collider = battlefield.getCollisionSystem().registerEntity(this, hitbox, affiliation);
		}
	
	    @Override
	    public void onDespawned(Battlefield battlefield) {
	    	/* Check if we got killed or despawned for some other reason.
	    	 * isDead() doesn't work because it might be overriden by a subclass, e.g. to despawn when going out of bounds.
	    	 */
	        if(getCurrentHealth() <= 0)
	            deathAttack.attack(battlefield, referenceFrame, affiliation, 0);
	        
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
	    public void update(Battlefield battlefield, float deltaT) {
	        super.update(battlefield, deltaT);
	        hitbox.setPosition(referenceFrame.getPosition());
	        if(hitbox instanceof Rectangle)
	        	((Rectangle) hitbox).setRotation(referenceFrame.getRotation());
	        if(health <= 0) die();
	    }
	    
	    @Override
		protected boolean inBounds(Battlefield battlefield) {
			return battlefield.inBounds(hitbox);
		}
	
	    public float damage(float amount) {
	        health = Math.max(health-amount, 0);
	        if(health <= 0) die();
	        return amount;
	    }
	
	    public void heal(float amount) {
	        health = Math.min(health+amount, maxHealth);
	    }
	    
	    public float getMaxHealth() {
	        return maxHealth;
	    }
	
	    public float getCurrentHealth() {
	        return health;
	    }
	    
	    public Affiliation getAffiliation() {
	    	return affiliation;
	    }
	}
}