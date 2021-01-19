package ubh.entity.ai;

import ubh.Battlefield;
import ubh.entity.Ship.Entity;
import ubh.math.Vector2;
import ubh.ui.UserInput;

public class PlayerAI implements AI {

	private final UserInput userInput;
	
	public PlayerAI(UserInput userInput) {
		this.userInput = userInput;
	}
	
	private class State implements AI.State {
		
		private int activeWeapon = 0;

		@Override
		public void update(Battlefield battlefield, float deltaT, Entity ship) {
			if(!ship.isDead()) {
				final var thrust = new Vector2(
		            (userInput.isKeyPressed('D') ? 1 : 0) + (userInput.isKeyPressed('A') ? -1 : 0),
		            (userInput.isKeyPressed('W') ? 1 : 0) + (userInput.isKeyPressed('S') ? -1 : 0)
	            );
				for(int weapon=0; weapon < ship.weaponCount(); ++weapon)
					if(userInput.isKeyPressed((char)('1'+weapon)))
						activeWeapon = weapon;
				ship.setThrust(thrust.length2() == 0 ? Vector2.ZERO : thrust.normalize());
	    		if(userInput.isMouseButtonPressed(1))
	    			ship.fireWeapon(battlefield, deltaT, activeWeapon, userInput.getCursorWorldPos()); 
			}
		}
		
	}
	
	@Override
	public AI.State createState() {
		return new State();
	}

}
