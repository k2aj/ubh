package ubh.entity;

import ubh.math.Shape;
import ubh.math.Vector2;
import ubh.math.AABB;
import ubh.math.ReferenceFrame;
import ubh.Battlefield;
import ubh.attack.Attack;

public abstract class Living implements Attack {
	
	protected final float maxHealth;
	protected final Shape hitbox;
	protected final Attack deathAttack;
	
	public Living(Builder<?> builder) {
		maxHealth = builder.maxHealth;
		hitbox = builder.hitbox.deepCopy();
		deathAttack = builder.deathAttack;
	}
	
    @SuppressWarnings("unchecked")
    public static abstract class Builder<This extends Builder<This>> {
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
	
	    public Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
	        super(referenceFrame);
	        this.health = maxHealth;
	        this.hitbox = Living.this.hitbox.deepCopy();
	        this.affiliation = affiliation;
	    }
	
	    @Override
	    public void onDespawned(Battlefield battlefield) {
	    	/* Check if we got killed or despawned for some other reason.
	    	 * isDead() doesn't work because it might be overriden by a subclass, e.g. to despawn when going out of bounds.
	    	 */
	        if(getCurrentHealth() <= 0)
	            deathAttack.attack(battlefield, referenceFrame, affiliation, 0);
	    }
	
	    @Override
	    public void update(Battlefield battlefield, float deltaT) {
	        super.update(battlefield, deltaT);
	        hitbox.setPosition(referenceFrame.getPosition());
	    }
	
	    public float damage(float amount) {
	        health = Math.max(health-amount, 0);
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
	
	    @Override
	    public boolean isDead() {
	        return health <= 0;
	    }
	}
}