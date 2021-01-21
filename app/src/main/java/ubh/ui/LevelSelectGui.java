package ubh.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import ubh.Alignment;
import ubh.App;
import ubh.UBHGraphics;
import ubh.level.Level;
import ubh.loader.ContentException;
import ubh.loader.LoadingState;
import ubh.math.Vector2;

public class LevelSelectGui extends GuiNotLeafNode {
	
	private Button btnBack = new Button();
	private List<String> levelIDs = null;
	private List<Button> levelButtons = new ArrayList<>();

	public LevelSelectGui() {
		addChild(btnBack);
		btnBack.setText("Back");
		btnBack.getBounds().setPosition(Vector2.ZERO);
	}
	
	private static final LevelSelectGui INSTANCE = new LevelSelectGui();
	public static LevelSelectGui getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void update(GuiContext ctx, float deltaT) {
		
		if(levelIDs == null)
			levelIDs = App.REGISTRY.getIds("Level");
		
		if(btnBack.isClicked())
			ctx.close();
		
		// Ensure there are enough buttons
		var transform = ctx.getUserInput().getTransform();
		var worldSize = transform.getWorldSize();
		float maxButtonHeight = worldSize.y()*0.45f;
		float heightPerButton = 8;
		int levelButtonsNeeded = Math.min(
			(int) Math.floor((0.5f*worldSize.y() + maxButtonHeight) / heightPerButton),
			levelIDs.size()
		);
		
		while(levelButtons.size() < levelButtonsNeeded) {
			var button = new Button();
			button.getBounds().setRadii(new Vector2(24,3));
			levelButtons.add(button);
			addChild(button);
		}
		
		// Update state of every button that can fit all the screen
		for(int i=0; i<levelButtonsNeeded; ++i) {
			var button = levelButtons.get(i);
			var bounds = button.getBounds();
			bounds.setPosition(new Vector2(
				-0.5f*worldSize.x() + 3f + bounds.getRadii().x(),
				maxButtonHeight - bounds.getRadii().y() - i*heightPerButton
			));
			
			var levelID = levelIDs.get(i);
			button.setText(levelID);
			if(App.REGISTRY.getLoadingState(levelID) == LoadingState.NOT_LOADED);
				try {
					App.REGISTRY.load(Level.class, levelID);
				}
				catch(ContentException ce) {
					// ignore the exception for now
					// TODO: write info about error to a log file or something
				}
				
			if(App.REGISTRY.getLoadingState(levelID) == LoadingState.ERROR)
				button.setColor(Color.RED);
			else {
				button.setColor(Color.YELLOW);
				if(button.isClicked()) {
					GameGui.getInstance().startLevel(App.REGISTRY.load(Level.class, levelID));
					ctx.open(GameGui.getInstance());
				}
			}
		}
		
		super.update(ctx, deltaT);
	}
	
	@Override
	public void draw(UBHGraphics g) {
		super.draw(g);
		g.setColor(Color.WHITE);
		g.drawText(new Vector2(0, 4), "Levels", Alignment.CENTER);
	}
}
