package ubh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import ubh.math.Vector2;
import ubh.math.Rectangle;

public final class App extends WindowAdapter implements KeyListener, MouseInputListener, AutoCloseable {
	
    public static void main(String[] args) {
    	try(final var app = new App(1280,720)) {
    		app.run();
    	}
    }
	
	private static final float 
		MAX_DELTA_T = 1/20f,
		WORLD_HEIGHT = 72;
	
	private JFrame frame;
    
    private App(int width, int height) {
    	// Setup window
    	frame = new JFrame();
    	frame.setTitle("UBH PrePrePreAlpha");
        frame.setSize(width, height);
        
        frame.addWindowListener(this);
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    
    private float time = 0;
    private Vector2 cursorWorldPos;
    
    private Rectangle
    	r1 = new Rectangle(new Vector2(5,0), new Vector2(24,5), Vector2.polar(0.25f*(float)Math.PI, 1)),
		r2 = new Rectangle(new Vector2(-5,0), new Vector2(16,7), Vector2.polar(-0.25f*(float)Math.PI, 1));
    
    /** Updates the game's state.
     * @param deltaT How much in-game time has passed since last call to this method.
     */
    private void update(float deltaT) {
    	time += deltaT;
    	r1.setPosition(cursorWorldPos);
    	r1.setRotation(Vector2.polar(time/2, 1));
    }
    
    /** Draws game objects, UI, etc.
     * @param g Graphics object used for rendering.
     */
    private void draw(UBHGraphics g) {
    	g.clear(Color.BLACK);
    	g.setColor(r1.intersects(r2) ? Color.RED : Color.GREEN);
    	g.disableFill();
    	r1.draw(g);
    	r2.draw(g);
        
    }
    
    @Override
    public void close() {
    	frame.dispose();
    }
    
    /** Runs the main loop of the game. */
    private void run() {

    	frame.setVisible(true);
    	frame.createBufferStrategy(2);
        final var bufferStrategy = frame.getBufferStrategy();
        
        var lastFrameTime = 1/60f;
        
        while(windowAlive) {
        	final var frameStartTimeMs = System.currentTimeMillis();
        	final var deltaT = Math.min(lastFrameTime, MAX_DELTA_T);
        	
        	final var frameSize = frame.getSize();
            final float 
                aspectRatio = (float) (frameSize.getWidth() / frameSize.getHeight()), 
                worldWidth = WORLD_HEIGHT * aspectRatio;
            try(final var graphics = new UBHGraphics(
                (Graphics2D) bufferStrategy.getDrawGraphics(), frameSize,
                new Vector2(worldWidth, WORLD_HEIGHT)
            )) {
            	cursorWorldPos = graphics.transformIntoWorldSpace(cursorScreenPos);
            	update(deltaT);
            	draw(graphics);
            }
        	bufferStrategy.show();
        	
        	lastFrameTime = (System.currentTimeMillis() - frameStartTimeMs) / 1000f;
        }
    }
    private boolean windowAlive = true;
    private Point cursorScreenPos = new Point(0,0);
    private final HashMap<Character,Boolean> keyboard = new HashMap<>();
    private boolean shiftPressed = false;
    
    //------------------------------LISTENERS------------------------------
    @Override public void windowClosing(WindowEvent e) {
        windowAlive = false;
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        keyboard.put(Character.toUpperCase(e.getKeyChar()), true);
        if(e.getKeyCode() == KeyEvent.VK_SHIFT)
            shiftPressed = true;
    }
    @Override public void keyReleased(KeyEvent e) {
        keyboard.put(Character.toUpperCase(e.getKeyChar()), false);
        if(e.getKeyCode() == KeyEvent.VK_SHIFT)
            shiftPressed = false;
    }
    @Override public void mouseClicked(MouseEvent e) {}
    private boolean[] mouseButtonPressed = new boolean[8];
	@Override public void mousePressed(MouseEvent e) {
        final var button = e.getButton();
        if(button < mouseButtonPressed.length)
            mouseButtonPressed[button] = true;
        cursorScreenPos = e.getPoint();
    }
	@Override public void mouseReleased(MouseEvent e) {
        final var button = e.getButton();
        if(button < mouseButtonPressed.length)
            mouseButtonPressed[button] = false;
        cursorScreenPos = e.getPoint();
    }
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {
    	cursorScreenPos = e.getPoint();
    }
    @Override public void mouseMoved(MouseEvent e) {
    	cursorScreenPos = e.getPoint();
    }
}
