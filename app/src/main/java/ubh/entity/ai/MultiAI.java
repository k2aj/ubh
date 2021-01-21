package ubh.entity.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Ship.Entity;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.MathUtil;

public class MultiAI implements AI {
	
	private final List<AI> ais;
	private final boolean phased;

	public MultiAI(List<AI> ais, boolean phased) {
		this.ais = new ArrayList<>(ais);
		this.phased = phased;
	}

	@Override
	public State createState() {
		return new State();
	}
	
	public class State implements AI.State {
		
		private List<AI.State> aiStates = ais.stream().map(AI::createState).collect(Collectors.toList());

		@Override
		public void update(Battlefield battlefield, float deltaT, Entity ship) {
			if(phased) {
				int activePhase = aiStates.size() - 1 - MathUtil.clamp(
					(int)(aiStates.size() * ship.getCurrentHealth() / ship.getMaxHealth()), 
					0, 
					aiStates.size() - 1
				);
				aiStates.get(activePhase).update(battlefield, deltaT, ship);
			} else {
				for(var state : aiStates) state.update(battlefield, deltaT, ship);
			}
		}
		
	}
	
	private static MultiAI fromJsonArray(ContentRegistry registry, JsonValue json, boolean phased) {
		return new MultiAI(json.asArray().values().stream().map(elem -> registry.load(AI.class, elem)).collect(Collectors.toList()), phased);
	}
	
	private static MultiAI fromJsonArray(ContentRegistry registry, JsonValue json) {
		return fromJsonArray(registry, json, false);
	}

	public static MultiAI fromJson(ContentRegistry registry, JsonValue json) {
		if(json.isArray()) return fromJsonArray(registry, json);
		else {
			JsonValue aiArray = null;
			boolean phased = false;
			for(var member : json.asObject()) {
				switch(member.getName()) {
				case "ais": aiArray = member.getValue(); break;
				case "phased": phased = member.getValue().asBoolean(); break;
				}
			}
			if(aiArray != null)
				return fromJsonArray(registry, aiArray, phased);
			else 
				throw new ContentException("Failed to load MultiAI");
		}
		
	}
}
