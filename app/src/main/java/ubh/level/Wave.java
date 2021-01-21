package ubh.level;

import java.util.Optional;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.attack.Attack;
import ubh.entity.Affiliation;
import ubh.loader.ContentRegistry;
import ubh.math.Shape;
import ubh.math.Vector2;
import ubh.math.AABB;
import ubh.math.Circle;
import ubh.math.ReferenceFrame;

public class Wave {
	
	private final float duration;
	private final Attack attack;
	private final int count;
	private final Shape normalizedSpawnArea;
	private final float velocity;
	
	private static final Shape DEFAULT_SPAWN_AREA = AABB.centered(new Vector2(0, 1.25f), new Vector2(0.75f,0.25f));
	
	private Wave() {
		duration = 0;
		attack = Attack.NULL;
		count = 1;
		normalizedSpawnArea = DEFAULT_SPAWN_AREA;
		velocity = 0;
	}
	
	public static final Wave EMPTY = new Wave();

	public Wave(ContentRegistry registry, JsonValue json) {
		float duration = 1, velocity = 20;
		Attack attack = Attack.NULL;
		int count = 1;
		Shape normalizedSpawnArea = DEFAULT_SPAWN_AREA;
		for(var member : json.asObject()) {
			switch(member.getName()) {
			case "duration": duration = member.getValue().asFloat(); break;
			case "attack": attack = registry.load(Attack.class, member.getValue()); break;
			case "count": count = member.getValue().asInt(); break;
			case "spawnArea": normalizedSpawnArea = registry.load(Shape.class, member.getValue()); break;
			case "velocity": velocity = member.getValue().asFloat(); break;
			}
		}
		this.duration = duration;
		this.attack = attack;
		this.velocity = velocity;
		this.count = count; 
		if(count <= 0) throw new IllegalArgumentException("\"count\" member not positive");
		this.normalizedSpawnArea = normalizedSpawnArea;
	}
	
	public void spawn(Battlefield battlefield) {
		for(int i=0; i<count; ++i) {
			var box = battlefield.getBoundingBox();
			var spawnPos = normalizedSpawnArea.randomPoint().mul(box.getRadii()).add(box.getPosition());
			var orientation = spawnPos.mul(-1).normalize();
			attack.attack(
				battlefield, 
				new ReferenceFrame(spawnPos, orientation.mul(velocity), orientation), 
				Affiliation.ENEMY, 
				0
			);
		}
	}
	
	public float getDuration() {
		return duration;
	}
	
	public Attack getAttack() {
		return attack;
	}
}
