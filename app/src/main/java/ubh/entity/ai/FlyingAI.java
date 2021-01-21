package ubh.entity.ai;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Ship.Entity;
import ubh.loader.ContentRegistry;
import ubh.math.AABB;
import ubh.math.Shape;
import ubh.math.Vector2;

/** 
 */
public class FlyingAI implements AI {
	
	private final Shape normalizedFlightTarget;
	
	public FlyingAI(Shape normalizedFlightTarget) {
		this.normalizedFlightTarget = normalizedFlightTarget;
	}

	@Override
	public AI.State createState() {
		return new State(normalizedFlightTarget.randomPoint());
	}

	private static final class State implements AI.State {

		private final Vector2 normalizedFlightTarget;

		public State(Vector2 normalizedFlightTarget) {
			this.normalizedFlightTarget = normalizedFlightTarget;
		}
		
		@Override
		public void update(Battlefield battlefield, float deltaT, Entity ship) {
			
			if(!ship.isDead()) {
				var box = battlefield.getBoundingBox();
				ship.flyTo(normalizedFlightTarget.mul(box.getRadii()).add(box.getPosition()));
			}
		}
	}
	
	public static FlyingAI fromJson(ContentRegistry registry, JsonValue json) {
		Shape normalizedFlightTarget = AABB.centered(new Vector2(0, 0.5f), new Vector2(0.4f, 0.2f));
		for(var member : json.asObject()) {
			if(member.getName().equals("flightTarget")) {
				normalizedFlightTarget = registry.load(Shape.class, member.getValue());
			}
		}
		return new FlyingAI(normalizedFlightTarget);
	}
}
