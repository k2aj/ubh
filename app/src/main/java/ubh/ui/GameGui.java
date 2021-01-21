package ubh.ui;

import java.awt.Color;
import java.util.Optional;

import ubh.Alignment;
import ubh.Game;
import ubh.GameState;
import ubh.UBHGraphics;
import ubh.attack.Weapon;
import ubh.entity.Ship;
import ubh.level.Level;
import ubh.math.Vector2;

public class GameGui extends GuiNotLeafNode {
	
	private Game game = new Game();
	private Level level;
	private boolean shouldStartLevel = false;
	
	private Button 
		btnRestart = new Button(), 
		btnSettings = new Button(),
		btnQuit = new Button();
	private GameGui() {
		addChild(btnSettings);
		addChild(btnRestart);
		addChild(btnQuit);
		btnSettings.getBounds().setPosition(Vector2.ZERO);
		btnSettings.setText("Settings");
		btnRestart.getBounds().setPosition(new Vector2(0,btnSettings.getBounds().getRadii().y()*(-3)));
		btnRestart.setText("Restart");
		btnQuit.getBounds().setPosition(new Vector2(0,btnSettings.getBounds().getRadii().y()*(-6)));
		btnQuit.setText("Quit");
	}
	
	private static final GameGui INSTANCE = new GameGui();
	public static GameGui getInstance() {
		return INSTANCE;
	}
	
	public void startLevel(Level level) {
		this.level = level;
		shouldStartLevel = true;
	}
	
	@Override
	public void update(GuiContext ctx, float deltaT) {
		super.update(ctx, deltaT);
		if(shouldStartLevel) {
			game.start(level);
			shouldStartLevel = false;
		}
		game.update(ctx.getUserInput(), deltaT);
		
		switch(game.getState()) {
		case RUNNING:
			if(ctx.getUserInput().isKeyClicked('P'))
				game.pause();
			break;
		case INIT: 
			break;
		case PAUSED:
			if(ctx.getUserInput().isKeyClicked('P')) {
				game.resume();
				break;
			}
		default: 
			if(btnSettings.isClicked())
				ctx.open(SettingsGui.getInstance());
			else if(btnRestart.isClicked())
				startLevel(level);
			else if(btnQuit.isClicked())
				ctx.close();
		}
	}

	@Override
	public void draw(UBHGraphics g) {
		
		g.clear(Color.BLACK);
		game.draw(g);
		if(game.getState() != GameState.INIT) {
			var ship = game.getPlayerShipEntity();
			var radii = g.getTransform().getWorldSize().div(2);
			drawWeaponInfo(g, radii.mul(new Vector2(-0.75f,0.7f)), ship);
			g.setColor(Color.DARK_GRAY);
			g.drawHpBar(radii.mul(0.8f), radii.mul(new Vector2(0.15f,0.04f)), radii.x()/100, ship.getCurrentHealth() / ship.getMaxHealth());
			
		} 
		if(game.getState() != GameState.RUNNING) {
			
			String message = "ERROR";
			switch(game.getState()) {
			case PAUSED: message = "PAUSED"; break;
			case WON: message = "YOU WON!"; break;
			case LOST: message = "YOU LOST!"; break;
			}
		
			g.enableFill();
			g.setColor(Color.BLACK);
			g.drawCenteredRect(Vector2.ZERO, btnRestart.getBounds().getRadii().mul(new Vector2(1.25f, 4)));
			
			g.setColor(Color.YELLOW);
			g.drawText(new Vector2(0, btnRestart.getBounds().getRadii().y()*2f), message, Alignment.CENTER);
			btnRestart.draw(g);
			btnSettings.draw(g);
			btnQuit.draw(g);
		}
		
	}
	
	private void drawRow(UBHGraphics g, Vector2 pos, String elem1, String elem2) {
		g.setColor(Color.DARK_GRAY);
		g.enableFill();
		g.drawCenteredRect(pos, new Vector2(21,2.5f));
		g.setColor(Color.YELLOW);
		g.drawText(pos.add(new Vector2(-20,0)), elem1, Alignment.LEFT);
		g.drawText(pos.add(new Vector2(20,0)), elem2, Alignment.RIGHT);
	}
	
	private void drawWeaponInfo(UBHGraphics g, Vector2 pos, Weapon.State weaponState) {
		drawRow(
			g, pos,
			weaponState.getName(),
			weaponState.getRemainingAmmo() > 0 ? 
				String.format("%d/%d", weaponState.getRemainingAmmo(), weaponState.getMaxAmmo()) :
				String.format("%.1f%%", weaponState.getReloadProgress()*100)
		);
	}
	
	private void drawWeaponInfo(UBHGraphics g, Vector2 pos, Ship.Entity ship) {
		g.setColor(Color.WHITE);
		for(int i=0; i<ship.weaponCount(); ++i)
			drawWeaponInfo(g, pos.add(new Vector2(0,-5*i)), ship.getWeapon(i));
	}
}
