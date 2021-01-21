package ubh.ui;

import ubh.UBHGraphics;

public interface GuiNode {

	public void update(GuiContext ctx, float deltaT);
	public void draw(UBHGraphics g);
	
}
