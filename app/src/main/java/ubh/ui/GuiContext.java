package ubh.ui;

import java.util.Stack;

import ubh.UBHGraphics;

public class GuiContext {
	
	private Stack<GuiNode> nodes = new Stack<>();
	private UserInput userInput;
	
	public GuiContext(GuiNode initialRoot, UserInput userInput) {
		open(initialRoot);
		this.userInput = userInput;
	}
	
	public void open(GuiNode node) {
		nodes.push(node);
	}
	
	public void close() {
		assert(!nodes.isEmpty());
		nodes.pop();
	}

	public void update(float deltaT) {
		nodes.peek().update(this, deltaT);
	}
	public void draw(UBHGraphics g) {
		nodes.peek().draw(g);
	}

	public UserInput getUserInput() {
		return userInput;
	}

}
