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
	
	public void pause() {
		if(state == GameState.RUNNING) state = GameState.PAUSED;
		else throw new IllegalStateException(state.toString());
	}
	
	public void resume() {
		if(state == GameState.PAUSED) state = GameState.RUNNING;
		else throw new IllegalStateException(state.toString());
	}
	
	public Ship.Entity getPlayerShipEntity() {
		return playerShipEntity;
	}
	
	public Vector2 getBattlefieldRadii() {
		return level.getBattlefieldRadii();
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
		
		if(state == GameState.RUNNING) {
			
			userInput.getTransform().setWorldSize(getExtendedWorldSize(userInput.getTransform().getAspectRatio()));
			playerAIState.input(userInput);
			battlefield.update(deltaT);
			
			if(level.getBackgroundSprite().isPresent()) {
				backgroundSpriteYOffset = mod(
					backgroundSpriteYOffset + deltaT * level.getBackgroundScrollSpeed(), 
					backgroundSpriteRadii.y()*2
				);
			}
			
			if(waveProgress < level.getWaves().size()) {
				// Try to spawn the next wave
				nextWaveDelay = Math.max(0, nextWaveDelay - deltaT);
				if(nextWaveDelay < deltaT) {
					var wave = level.getWaves().get(waveProgress++);
					wave.spawn(battlefield);
					nextWaveDelay += wave.getDuration();
				}
			} else { 
				// all waves are gone
				// if all enemies are gone too, then you win the level
				if(battlefield.getCollisionSystem().getRandomEntity(Affiliation.ENEMY).isEmpty())
					state = GameState.WON;
			}
			// the game is lost if all friendly ships are dead
			if(battlefield.getCollisionSystem().getRandomEntity(Affiliation.FRIENDLY).isEmpty())
				state = GameState.LOST;
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
