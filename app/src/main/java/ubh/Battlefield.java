package ubh;

import java.util.ArrayList;
import java.util.EnumMap;

import ubh.entity.Affiliation;
import ubh.entity.Entity;
import ubh.math.AABB;
import ubh.math.Shape;
import ubh.math.Vector2;

public class Battlefield {
	private final ArrayList<Entity> 
	    entities = new ArrayList<>(),
	    newEntities = new ArrayList<>();
	private final Shape bounds;
	private final CollisionSystem collisionSystem = new CollisionSystem();
	
	public Battlefield(Shape bounds) {
		this.bounds = bounds;
	}
	
	/** Updates the state of battlefield and all spawned entities. */
	public void update(float deltaT) {
		// Insert new entities
		for(var e : newEntities)
			e.onSpawned(this);
        entities.addAll(newEntities);
        newEntities.clear();
		
		// Update state of existing entities
		for(var e : entities) 
			e.update(this, deltaT);
		collisionSystem.update(deltaT);
		collisionSystem.runCollisions(this);
		
		// Remove dead entities
		for(var e : entities)
			if(e.isDead())
				e.onDespawned(this);
		entities.removeIf(Entity::isDead);
	}
	
	/** Draws this Battlefield and all spawned entities. */
	public void draw(UBHGraphics g) {
		for(var e : entities)
			e.draw(g);
	}
	/** Inserts a new Entity into this Battlefield.
	 * @param entity
	 * @param deltaTLeft How much in-game time will pass before next call to update().
	 */
	public void spawn(Entity entity, float deltaTLeft) {
		entity.update(this, deltaTLeft);
		newEntities.add(entity);
	}
	public boolean inBounds(Shape shape) {
		return bounds.intersects(shape);
	}
	public boolean inBounds(Vector2 point) {
		return bounds.contains(point);
	}
	public AABB getBoundingBox() {
		return bounds.getBoundingBox();
	}
	public CollisionSystem getCollisionSystem() {
		return collisionSystem;
	}
}
