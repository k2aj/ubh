package ubh.ui;

import java.awt.Color;

import ubh.UBHGraphics;
import ubh.math.Vector2;

public class SettingsGui extends GuiNotLeafNode {
	
	private Button btnBack = new Button();

	public SettingsGui() {
		addChild(btnBack);
		btnBack.setText("Back");
		btnBack.getBounds().setPosition(Vector2.ZERO);
	}
	
	private static final SettingsGui INSTANCE = new SettingsGui();
	public static SettingsGui getInstance() {
		return INSTANCE;
	}
	
	@Override
	public void update(GuiContext ctx, float deltaT) {
		if(btnBack.isClicked())
			ctx.close();
		super.update(ctx, deltaT);
	}
	
	@Override
	public void draw(UBHGraphics g) {
		super.draw(g);
		g.setColor(Color.WHITE);
		g.drawText(new Vector2(0, 2), "Settings");
	}
}
