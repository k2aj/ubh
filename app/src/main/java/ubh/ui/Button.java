package ubh.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import ubh.UBHGraphics;
import ubh.math.AABB;
import ubh.math.Vector2;

public class Button implements GuiNode {

	private AABB bounds = AABB.centered(Vector2.ZERO, new Vector2(12,3));
	private Color color = Color.YELLOW;
	private String text = "";
	boolean clicked = false;

	@Override
	public void update(GuiContext ctx, float deltaT) {
		var input = ctx.getUserInput();
		clicked = input.isMouseButtonClicked(1) && bounds.contains(input.getCursorWorldPos());
	}

	@Override
	public void draw(UBHGraphics g) {
		g.setColor(color);
		g.disableFill();
		bounds.draw(g);
		g.drawText(bounds.getPosition(), text);
	}
	
	public boolean isClicked() {
		return clicked;
	}

	public AABB getBounds() {
		return bounds;
	}

	public Color getColor() {
		return color;
	}

	public String getText() {
		return text;
	}

	public void setBounds(AABB bounds) {
		this.bounds = bounds;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setText(String text) {
		this.text = text;
	}

}
