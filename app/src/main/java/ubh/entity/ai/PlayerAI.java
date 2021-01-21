package ubh.entity.ai;

import ubh.Battlefield;
import ubh.entity.Ship.Entity;
import ubh.math.Vector2;
import ubh.ui.UserInput;

public class PlayerAI implements AI {
	
	private PlayerAI() {}
	
	private static final PlayerAI INSTANCE = new PlayerAI();
	public static PlayerAI getInstance() {
		return INSTANCE;
	}
	
	public class State implements AI.State {
		
		private Vector2 rawThrust = Vector2.ZERO, aimPos = Vector2.ZERO;
		private int activeWeapon = 0, nextActiveWeapon = 0;
		private boolean weaponFiring = false;
		private boolean slowMode = false;

		@Override
		public void update(Battlefield battlefield, float deltaT, Entity ship) {
			if(!ship.isDead()) {
				
				var thrust = rawThrust.length2() == 0 ? Vector2.ZERO : rawThrust.normalize();
				if(slowMode) thrust = thrust.div(3);
				ship.setThrust(thrust);
				
				while(!battlefield.inBounds(ship.getPosition())) {
					ship.setPosition(ship.getPosition().add(ship.getPosition().mul(-1).normalize()));
				}
				
				if(nextActiveWeapon < ship.weaponCount())
					activeWeapon = nextActiveWeapon;
	    		if(weaponFiring)
	    			ship.fireWeapon(battlefield, deltaT, activeWeapon, aimPos); 
			}
		}
		
		public void input(UserInput userInput) {
			rawThrust = new Vector2(
	            (userInput.isKeyPressed('D') ? 1 : 0) + (userInput.isKeyPressed('A') ? -1 : 0),
	            (userInput.isKeyPressed('W') ? 1 : 0) + (userInput.isKeyPressed('S') ? -1 : 0)
	        );
			for(int i=0; i<9; ++i)
				if(userInput.isKeyPressed((char)('1'+i)))
					nextActiveWeapon = i;
			weaponFiring = userInput.isMouseButtonPressed(1);
			aimPos = userInput.getCursorWorldPos();
			slowMode = userInput.isShiftPressed();
		}
		
	}
	
	@Override
	public State createState() {
		return new State();
	}

}
