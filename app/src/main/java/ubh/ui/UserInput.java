package ubh.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.event.MouseInputListener;

import ubh.CoordinateTransform;
import ubh.math.Vector2;

public class UserInput implements KeyListener, MouseInputListener {
	
	private final CoordinateTransform transform = new CoordinateTransform();
    private Point cursorScreenPos = new Point(0,0);
    private Vector2 cursorWorldPos = Vector2.ZERO;
    private final HashMap<Character,Boolean> keyPressed = new HashMap<>();
    private final HashMap<Character,Boolean> keyClicked = new HashMap<>();
    private boolean shiftPressed = false;
    private boolean[] mouseButtonPressed = new boolean[8];
    private boolean[] mouseButtonClicked = new boolean[8];
    
    public CoordinateTransform getTransform() {
    	return transform;
    }
    
    public boolean isShiftPressed() {
    	return shiftPressed;
    }
    
    public void update() {
    	Arrays.fill(mouseButtonClicked, false);
    	keyClicked.replaceAll((k,v) -> false);
    }
    
	public boolean isKeyPressed(char key) {
		return keyPressed.getOrDefault(key, false);
	}
	
	public boolean isKeyClicked(char key) {
		return keyClicked.getOrDefault(key, false);
	}
	
	public boolean isMouseButtonPressed(int index) {
		return mouseButtonPressed[index];
	}
	
	public boolean isMouseButtonClicked(int index) {
		return mouseButtonClicked[index];
	}
	
	public Point getCursorScreenPos() {
		return cursorScreenPos;
	}
	
	public Vector2 getCursorWorldPos() {
		return cursorWorldPos;
	}

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        keyPressed.put(Character.toUpperCase(e.getKeyChar()), true);
        keyClicked.put(Character.toUpperCase(e.getKeyChar()), true);
        if(e.getKeyCode() == KeyEvent.VK_SHIFT)
            shiftPressed = true;
    }
    @Override public void keyReleased(KeyEvent e) {
    	 keyPressed.put(Character.toUpperCase(e.getKeyChar()), false);
         keyClicked.put(Character.toUpperCase(e.getKeyChar()), false);
        if(e.getKeyCode() == KeyEvent.VK_SHIFT)
            shiftPressed = false;
    }
    @Override public void mouseClicked(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {
        final var button = e.getButton();
        if(button < mouseButtonPressed.length) {
            mouseButtonPressed[button] = true;
            mouseButtonClicked[button] = true;
        }
        cursorScreenPos = e.getPoint();
        cursorWorldPos = transform.transformIntoWorldSpace(cursorScreenPos);
    }
	@Override public void mouseReleased(MouseEvent e) {
        final var button = e.getButton();
        if(button < mouseButtonPressed.length)
            mouseButtonPressed[button] = false;
        cursorScreenPos = e.getPoint();
        cursorWorldPos = transform.transformIntoWorldSpace(cursorScreenPos);
    }
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {
    	cursorScreenPos = e.getPoint();
    	cursorWorldPos = transform.transformIntoWorldSpace(cursorScreenPos);
    }
    @Override public void mouseMoved(MouseEvent e) {
    	cursorScreenPos = e.getPoint();
    	cursorWorldPos = transform.transformIntoWorldSpace(cursorScreenPos);
    }
}
