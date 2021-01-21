package ubh;

import java.util.Optional;

import ubh.entity.Affiliation;
import ubh.entity.Ship;
import ubh.entity.ai.PlayerAI;
import ubh.level.Level;
import ubh.math.AABB;
import ubh.math.ReferenceFrame;
import ubh.math.Vector2;
import ubh.ui.UserInput;

public class Game {
	
	private GameState state = GameState.INIT;

	private Level level;
	private float nextWaveDelay;
	private int waveProgress;
	private Battlefield battlefield;
	private Ship.Entity playerShipEntity;
	private PlayerAI.State playerAIState;
	private AABB boundingBox;
	
	private Vector2 backgroundSpriteRadii;
	private int backgroundSpriteCount;
	private float backgroundSpriteYOffset;
		
	private void installPlayerAI() {
		playerAIState = PlayerAI.getInstance().createState();
		playerShipEntity.setAIState(playerAIState);
	}
	
	public void start(Level level) {
		this.level = level;
		
		boundingBox = AABB.centered(Vector2.ZERO, level.getBattlefieldRadii());
		battlefield = new Battlefield(boundingBox);
		level.getBackgroundSprite().ifPresent(background -> {
			float rx = level.getBattlefieldRadii().x(),
				  ry = rx * background.getHeight() / (float) background.getWidth();
			backgroundSpriteRadii = new Vector2(rx,ry);
			backgroundSpriteCount = ((int) Math.ceil(level.getBattlefieldRadii().y() / backgroundSpriteRadii.y())) + 1;
			backgroundSpriteYOffset = 0;
		});
		
		playerShipEntity = level.getPlayerShip().createEntity(
			new ReferenceFrame(Vector2.ZERO, Vector2.ZERO, Vector2.UNIT_Y), 
			Affiliation.FRIENDLY
		);
		installPlayerAI();
		battlefield.spawn(playerShipEntity, 0);
		
		state = GameState.RUNNING;
		nextWaveDelay = 0;
		waveProgress = 0;
	}
	
	public GameState getState() {
		return state;
	}
	
	private static float mod(float a, float b) {
		return (a%b+b)%b;
	}
	
	private Vector2 getExtendedWorldSize(float aspectRatio) {
		
		float battlefieldAspectRatio = level.getBattlefieldRadii().x() / level.getBattlefieldRadii().y();
		
		if(aspectRatio >= battlefieldAspectRatio)
			return level.getBattlefieldRadii().mul(new Vector2(2*aspectRatio/battlefieldAspectRatio, 2));
		else
			return level.getBattlefieldRadii().mul(new Vector2(2, 2/aspectRatio*battlefieldAspectRatio));
	}
	
	public void update(UserInput userInput, float deltaT) {
		var oldWorldSize = userInput.getTransform().getWorldSize();
		
		switch(state) {
		case RUNNING:
			userInput.getTransform().setWorldSize(getExtendedWorldSize(userInput.getTransform().getAspectRatio()));
			playerAIState.input(userInput);
			battlefield.update(deltaT);
			
			backgroundSpriteYOffset = mod(
				backgroundSpriteYOffset + deltaT * level.getBackgroundScrollSpeed(), 
				backgroundSpriteRadii.y()*2
			);
			
			if(waveProgress < level.getWaves().size()) {
				// Try to spawn the next wave
				nextWaveDelay = Math.max(0, nextWaveDelay - deltaT);
				while(nextWaveDelay < deltaT) {
					var wave = level.getWaves().get(waveProgress++);
					nextWaveDelay += wave.getDuration();
					wave.getAttack().attack(
						battlefield, 
						new ReferenceFrame(new Vector2(0, boundingBox.getRadii().y()*2), Vector2.ZERO, Vector2.UNIT_Y.mul(-1)), 
						Affiliation.ENEMY, 
						0
					);
				}
			}
			break;
		default:
			break;
		}
		
		userInput.getTransform().setWorldSize(oldWorldSize);
	}
	
	public void draw(UBHGraphics g) {
	
		if(state != GameState.INIT) {
				
			var oldWorldSize = g.getTransform().getWorldSize();
			g.getTransform().setWorldSize(getExtendedWorldSize(g.getTransform().getAspectRatio()));
				
			g.setClip(Optional.of(boundingBox));
			g.enableFill();
			g.setColor(level.getBackgroundColor());
			g.drawCenteredRect(Vector2.ZERO, level.getBattlefieldRadii());
			level.getBackgroundSprite().ifPresent(background -> {
				for(int i=0; i<backgroundSpriteCount; ++i) {
					g.drawImage(background, new Vector2(0, backgroundSpriteYOffset+2*(i-1)*backgroundSpriteRadii.y()), backgroundSpriteRadii, Vector2.UNIT_X);
				}
			});
			
			battlefield.draw(g);
			
			g.setClip(Optional.empty());
			g.getTransform().setWorldSize(oldWorldSize);
		}
	}
}
