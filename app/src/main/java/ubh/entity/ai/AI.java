package ubh.entity.ai;

import ubh.Battlefield;
import ubh.entity.Ship;

public interface AI {
	
	public State createState();
	
	public static interface State {
		public void update(Battlefield battlefield, float deltaT, Ship.Entity ship);
	}

	public static final State NULL_STATE = (battlefield, deltaT, ship) -> {};
	public static final AI NULL = () -> NULL_STATE;
}
