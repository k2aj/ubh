package ubh.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import ubh.UBHGraphics;
import ubh.math.Vector2;

public class MainMenu extends GuiNotLeafNode {
	
	Button btnPlay = new Button(), 
		   btnSettings = new Button();

	private MainMenu() {
		addChild(btnPlay);
		addChild(btnSettings);
		btnPlay.setText("Play");
		btnPlay.getBounds().setPosition(new Vector2(0, 4));
		btnSettings.setText("Settings");
		btnSettings.getBounds().setPosition(new Vector2(0, -4));
	}
	
	private static final MainMenu INSTANCE = new MainMenu();
	public static MainMenu getInstance() {
		return INSTANCE;
	}

	@Override
	public void update(GuiContext ctx, float deltaT) {
		if(btnPlay.isClicked()) ctx.open(GameGui.getInstance());
		else if(btnSettings.isClicked()) ctx.open(SettingsGui.getInstance());
		else {
			
		}
		super.update(ctx, deltaT);
	}
	@Override
	public void draw(UBHGraphics g) {
		super.draw(g);
		g.setColor(Color.WHITE);
		g.drawText(new Vector2(0, 8), "Main menu");
	}
}
