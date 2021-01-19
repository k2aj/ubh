package ubh.entity.ai;

import java.util.Optional;

import ubh.Battlefield;
import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.entity.Ship.Entity;
import ubh.math.MathUtil;

/** BossAI targets a random friendly entity and shoots at it until it dies.
 *  Then it switches to another random friendly entity.
 *  
 *  BossAI will start by using it's first weapon, and switch to other weapons as health of the controlled ship decreases.
 *  (e.g. if controlled ship has two weapons, then first will be used between 50% and 100% HP, and second between 0% and 50% HP)
 * 
 */
public class BossAI implements AI {

	@Override
	public AI.State createState() {
		return new State();
	}

	private static final class State implements AI.State {
		
		private Optional<Living.Entity> target = Optional.empty();

		@Override
		public void update(Battlefield battlefield, float deltaT, Entity ship) {
			if(!ship.isDead()) {
				if(ship.weaponCount() > 0) {
					if(target.isEmpty() || target.get().isDead()) {
						target = battlefield.getCollisionSystem().getRandomEntity(Affiliation.FRIENDLY);
					} else {
						int activeWeapon = ship.weaponCount() - 1 - MathUtil.clamp(
							(int)(ship.weaponCount() * ship.getCurrentHealth() / ship.getMaxHealth()), 
							0, 
							ship.weaponCount() - 1
						);
						ship.fireWeapon(battlefield, deltaT, activeWeapon, target.get().getPosition());
					}
				}
			}
		}
	}
	
	private static final BossAI INSTANCE = new BossAI();
	public static AI getInstance() {
		return INSTANCE;
	}
}
