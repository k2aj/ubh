package ubh.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ubh.UBHGraphics;

public class GuiNotLeafNode implements GuiNode {

	private List<GuiNode> children = new ArrayList<>();
	
	public void addChild(GuiNode child) {
		children.add(child);
	}
	
	@Override
	public void update(GuiContext ctx, float deltaT) {
		for(var child : children)
			child.update(ctx, deltaT);
	}
	
	@Override
	public void draw(UBHGraphics g) {
		g.clear(Color.BLACK);
		for(var child : children)
			child.draw(g);
	}
}
