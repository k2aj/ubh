package ubh.attack;

import java.util.ArrayList;
import java.util.List;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Affiliation;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.ReferenceFrame;

/** Executes multiple attacks as a single attack. */
public class MultiAttack implements Attack {
	
	private final List<Attack> attacks;
	
	public MultiAttack(List<Attack> attacks) {
		this.attacks = new ArrayList<>(attacks);
	}

	@Override
	public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		for(var attack : attacks)
			attack.attack(battlefield, referenceFrame, affiliation, deltaTLeft);
	}
	
	public static MultiAttack fromJsonArray(ContentRegistry registry, JsonValue json) throws ContentException {
		List<Attack> attacks = new ArrayList<>();
		for(var member : json.asArray())
			attacks.add(registry.load(Attack.class, member));
		return new MultiAttack(attacks);
	}

	public static MultiAttack fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		return fromJsonArray(registry, json.asObject().get("attacks"));
	}
}
