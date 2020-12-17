package ubh.attack;

import java.util.List;

import ubh.Battlefield;
import ubh.math.ReferenceFrame;
import ubh.entity.Affiliation;

/** An Attack is some action you can perform to inflict damage on things. */
public interface Attack {
    /** Performs the attack.
     * @param battlefield Battlefield in which the attack was performed.
     * @param referenceFrame Reference frame of the attack. ReferenceFrame may be reused by the caller;
     *                       Attack is responsible for deepCopying the received ReferenceFrame if necessary.
     * @param deltaTLeft Amount of time which will pass between performing the attack and the end of current frame.
     */
    public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft);

    /** NULL Attack does nothing.*/
    public static final Attack NULL = (a,b,c,d) -> {};
    
    public static Attack combine(Attack... attacks) {
    	return new MultiAttack(List.of(attacks));
    }
}