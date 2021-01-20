package ubh.level;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hjson.JsonValue;

import ubh.entity.Ship;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.Vector2;

public class Level {
	private final Optional<BufferedImage> backgroundSprite;
	private final Color backgroundColor;
	private final float backgroundScrollSpeed;
	private final Vector2 battlefieldRadii;
	private final Ship playerShip;
	private final List<Wave> waves;
	
	public Level(ContentRegistry registry, JsonValue json) {
		
		Optional<BufferedImage> backgroundSprite = Optional.empty();
		Color backgroundColor = Color.BLACK;
		float backgroundScrollSpeed = 0;
		Vector2 battlefieldRadii = new Vector2(80, 60);
		Ship playerShip = null;
		List<Wave> waves = List.of();
		
		for(var member : json.asObject()) {
			switch(member.getName()) {
			case "backgroundSprite": 
				backgroundSprite = Optional.of(registry.load(BufferedImage.class, member.getValue())); 
				break;
			case "backgroundColor": backgroundColor = registry.load(Color.class, member.getValue()); break;
			case "backgroundScrollSpeed": backgroundScrollSpeed = member.getValue().asFloat(); break;
			case "battlefieldSize": battlefieldRadii = registry.load(Vector2.class, member.getValue()).div(2); break;
			case "playerShip": playerShip = registry.load(Ship.class, member.getValue()); break;
			case "waves": 
				waves = member.getValue()
					.asArray().values().stream()
					.map(w -> registry.load(Wave.class, w))
					.collect(Collectors.toList());
				break;
			}
		}
		
		if(playerShip == null)
			throw new ContentException("playerShip not specified");
		
		this.backgroundSprite = backgroundSprite;
		this.backgroundColor = backgroundColor;
		this.backgroundScrollSpeed = backgroundScrollSpeed;
		this.battlefieldRadii = battlefieldRadii;
		this.playerShip = playerShip;
		this.waves = Collections.unmodifiableList(waves);
	}
	
	public Optional<BufferedImage> getBackgroundSprite() {
		return backgroundSprite;
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public float getBackgroundScrollSpeed() {
		return backgroundScrollSpeed;
	}
	
	public Vector2 getBattlefieldRadii() {
		return battlefieldRadii;
	}
	
	public Ship getPlayerShip() {
		return playerShip;
	}
	
	public List<Wave> getWaves() {
		return waves;
	}
}
