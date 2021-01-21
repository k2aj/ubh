package ubh.ui;

import ubh.App;
import ubh.Game;
import ubh.GameState;
import ubh.UBHGraphics;
import ubh.level.Level;

public class GameGui extends GuiNotLeafNode {
	
	private Game game = new Game();
	
	private static final GameGui INSTANCE = new GameGui();
	public static GameGui getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void update(GuiContext ctx, float deltaT) {
		super.update(ctx, deltaT);
		if(game.getState() == GameState.INIT)
			game.start(App.REGISTRY.load(Level.class, "example_level"));
		game.update(ctx.getUserInput(), deltaT);
	}

	@Override
	public void draw(UBHGraphics g) {
		
		super.draw(g);
		game.draw(g);
	}
}
