package ubh.level;

import org.hjson.JsonValue;

import ubh.attack.Attack;
import ubh.loader.ContentRegistry;

public class Wave {
	
	private final float duration;
	private final Attack attack;
	
	private Wave() {
		duration = 0;
		attack = Attack.NULL;
	}
	
	public static final Wave EMPTY = new Wave();

	public Wave(ContentRegistry registry, JsonValue json) {
		float duration = 1;
		Attack attack = Attack.NULL;
		for(var member : json.asObject()) {
			switch(member.getName()) {
			case "duration": duration = member.getValue().asFloat(); break;
			case "attack": attack = registry.load(Attack.class, member.getValue()); break;
			}
		}
		this.duration = duration;
		this.attack = attack;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public Attack getAttack() {
		return attack;
	}
}
