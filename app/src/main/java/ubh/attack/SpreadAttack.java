package ubh.attack;

import org.hjson.JsonValue;

import ubh.Battlefield;
import ubh.entity.Affiliation;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.ReferenceFrame;
import ubh.math.Vector2;

/** Executes an attack multiple times, while modifying reference frame to spread out its effects. */
public class SpreadAttack implements Attack {
	
	private final Attack attack;
	private final float
		baseVelocityWeight,
		randomVelocityWeight,
		spreadVelocityWeight,
		baseRotationWeight,
		randomRotationWeight,
		spreadRotationWeight,
		spreadAngle;
	private final int 
		minRepeats, 
		maxRepeats;
	
	protected SpreadAttack(Builder<?> builder) {
		this.attack = builder.attack;
		this.randomVelocityWeight = builder.randomVelocityWeight;
		this.baseVelocityWeight = builder.baseVelocityWeight;
		this.spreadVelocityWeight = builder.spreadVelocityWeight;
		this.randomRotationWeight = builder.randomRotationWeight;
		this.baseRotationWeight = builder.baseRotationWeight;
		this.spreadRotationWeight = builder.spreadRotationWeight;
		this.spreadAngle = builder.spreadAngle;
		this.minRepeats = builder.minRepeats;
		this.maxRepeats = builder.maxRepeats;
	}
	
	@Override
	public void attack(Battlefield battlefield, ReferenceFrame referenceFrame, Affiliation affiliation, float deltaTLeft) {
		var rframe = referenceFrame.deepCopy();
		var vBase = rframe.getVelocity();
		var rBase = rframe.getRotation();
		var count = (int)(Math.random()*(maxRepeats-minRepeats+0.9999)) + minRepeats;
		
		for(int i=0; i<count; ++i) {
			
			var randomVector = Vector2.polar((float)(Math.random()*2*Math.PI), (float)(Math.random()));
			float angle = 0;
			if(count > 1)
				angle = spreadAngle*(i/(float)(count)-0.5f);
			
			var vRandom = vBase.scaleRotate(randomVector);
			var rRandom = rBase.scaleRotate(randomVector);
			var vSpread = vBase.rotate(angle);
			var rSpread = rBase.rotate(angle);
			
			rframe.setVelocity(
				vBase.mul(baseVelocityWeight)
				.add(vRandom.mul(randomVelocityWeight))
				.add(vSpread.mul(spreadVelocityWeight))
			);
			rframe.setRotation(
				rBase.mul(baseRotationWeight)
				.add(rRandom.mul(randomRotationWeight))
				.add(rSpread.mul(spreadRotationWeight))
				.normalize()
			);
			attack.attack(battlefield, rframe, affiliation, deltaTLeft);
		}
	}
	
	public static Builder<?> builder() {
		return new Builder<>();
	}
	
	@SuppressWarnings("unchecked")
	public static class Builder<This extends Builder<This>> {
		
		private Attack attack = Attack.NULL;
		
		private float
			baseVelocityWeight = 1,
			randomVelocityWeight = 0,
			spreadVelocityWeight = 0,
			baseRotationWeight = 1,
			randomRotationWeight = 0,
			spreadRotationWeight = 0,
			spreadAngle = 0;
		private int
			minRepeats = 1,
			maxRepeats = 1;
		
		public This attack(Attack attack) {
			this.attack = attack;
			return (This) this;
		}
		
		public SpreadAttack build() {
			return new SpreadAttack(this);
		}
		
		public This baseVelocityWeight(float weight) {
			baseVelocityWeight = weight;
			return (This) this;
		}
		public This randomVelocityWeight(float weight) {
			randomVelocityWeight = weight;
			return (This) this;
		}
		public This spreadVelocityWeight(float weight) {
			spreadVelocityWeight = weight;
			return (This) this;
		}
		public This baseRotationWeight(float weight) {
			baseRotationWeight = weight;
			return (This) this;
		}
		public This randomRotationWeight(float weight) {
			randomRotationWeight = weight;
			return (This) this;
		}
		public This spreadRotationWeight(float weight) {
			spreadRotationWeight = weight;
			return (This) this;
		}
		public This baseWeight(float weight) {
			return baseVelocityWeight(weight).baseRotationWeight(weight);
		}
		public This randomWeight(float weight) {
			return randomVelocityWeight(weight).randomRotationWeight(weight);
		}
		public This spreadWeight(float weight) {
			return spreadVelocityWeight(weight).spreadRotationWeight(weight);
		}
		public This spreadAngle(float angle) {
			spreadAngle = angle;
			return (This) this;
		}
		public This minRepeats(int repeats) {
			minRepeats = repeats;
			return (This) this;
		}
		public This maxRepeats(int repeats) {
			maxRepeats = repeats;
			return (This) this;
		}
		public This repeats(int repeats) {
			return minRepeats(repeats).maxRepeats(repeats);
		}
		protected void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException {
			switch(field) {
				case "attack": attack(registry.load(Attack.class, json)); break;
				case "baseVelocityWeight": baseVelocityWeight(json.asFloat()); break;
				case "randomVelocityWeight": randomVelocityWeight(json.asFloat()); break;
				case "spreadVelocityWeight": spreadVelocityWeight(json.asFloat()); break;
				case "baseRotationWeight": baseRotationWeight(json.asFloat()); break;
				case "randomRotationWeight": randomRotationWeight(json.asFloat()); break;
				case "spreadRotationWeight": spreadRotationWeight(json.asFloat()); break;
				case "baseWeight": baseWeight(json.asFloat()); break;
				case "randomWeight": randomWeight(json.asFloat()); break;
				case "spreadWeight": spreadWeight(json.asFloat()); break;
				case "spreadAngle": spreadAngle((float) Math.toRadians(json.asDouble())); break;
				case "minRepeats": minRepeats(json.asInt()); break;
				case "maxRepeats": maxRepeats(json.asInt()); break;
				case "repeats": repeats(json.asInt()); break;
			}
		}
		protected This loadJson(ContentRegistry registry, JsonValue json) throws ContentException {
			for(var member : json.asObject())
				loadFieldFromJson(member.getName(), registry, member.getValue());
			return (This) this;
		}
	}
	
	public static SpreadAttack fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
		return builder().loadJson(registry, json).build();
	}
}
