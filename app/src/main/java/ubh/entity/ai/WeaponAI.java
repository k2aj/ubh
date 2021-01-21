package ubh.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.entity.Ship;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;

public class WeaponAI implements AI {
	
	List<Integer> weapons;

	public WeaponAI(List<Integer> weapons) {
		for(var weapon : weapons)
			if(weapon < 0)
				throw new IllegalArgumentException("Invalid weapon index: "+weapon);
		this.weapons = new ArrayList<>(weapons);
	}

	@Override
	public State createState() {
		return new State();
	}
	
	public class State implements AI.State {
		
		private Optional<Living.Entity> target = Optional.empty();

		@Override
		public void update(Battlefield battlefield, float deltaT, Ship.Entity ship) {
			if(!ship.isDead()) {
				if(target.isEmpty() || target.get().isDead()) {
					target = battlefield.getCollisionSystem().getRandomEntity(Affiliation.FRIENDLY);
				} else {
					for(var weapon : weapons) 
						if(weapon < ship.weaponCount())
							ship.fireWeapon(battlefield, deltaT, weapon, target.get().getPosition());
				}		
			}
		}
		
	}
	
	public static WeaponAI fromJson(ContentRegistry registry, JsonValue json) {
		for(var member : json.asObject())
			if(member.getName().equals("weapons"))
				return new WeaponAI(member.getValue().asArray().values().stream().map(JsonValue::asInt).collect(Collectors.toList()));
		throw new ContentException("Missing \"weapons\" member");
	}
}
