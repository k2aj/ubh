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
import ubh.attack.Attack;
import ubh.attack.Weapon;
import ubh.entity.Affiliation;
import ubh.entity.Ship;
import ubh.loader.ContentRegistry;
import ubh.math.AABB;
import ubh.math.ReferenceFrame;

public final class App extends WindowAdapter implements KeyListener, MouseInputListener, AutoCloseable {
	
    public static void main(String[] args) {
    	try(final var app = new App(1280,720)) {
    		app.run();
    	}
    }
	
	private static final float 
		MAX_DELTA_T = 1/20f,
		WORLD_HEIGHT = 72;
	
	private final JFrame frame;
	private static final ContentRegistry REGISTRY;
	static {
		REGISTRY = ContentRegistry.createDefault();
		REGISTRY.addHjsonSource(App.class, "example_bullet.hjson");
		REGISTRY.addHjsonSource(App.class, "example_ship.hjson");
		REGISTRY.addHjsonSource(App.class, "example_weapon.hjson");
	}
	
	private static final Ship SHIP = REGISTRY.load(Ship.class, "example_ship");
	
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
        
        battlefield = new Battlefield(AABB.centered(Vector2.ZERO, new Vector2(WORLD_HEIGHT*2, WORLD_HEIGHT)));
        playerShipEntity = SHIP.createEntity(new ReferenceFrame(Vector2.ZERO), Affiliation.FRIENDLY);
        battlefield.spawn(playerShipEntity, 0);
        
        for(int i=0; i<20; ++i)
        	SHIP.attack(
        		battlefield, 
        		new ReferenceFrame(new Vector2(
        			((float) Math.random() - 0.5f)*2*WORLD_HEIGHT, 
        			WORLD_HEIGHT*(float) Math.random())
        		),
        		Affiliation.ENEMY, 
        		0
        	);
    }
    
    private float time = 0;
    private Vector2 cursorWorldPos;
    private Battlefield battlefield;
    private Ship.Entity playerShipEntity;
    //private Weapon.State weaponState;
    
    /** Updates the game's state.
     * @param deltaT How much in-game time has passed since last call to this method.
     */
    private void update(float deltaT) {
    	battlefield.update(deltaT);
    	//weaponState.update(deltaT);
    	if(!playerShipEntity.isDead()) {
    		final var thrust = new Vector2(
	            (keyboard.getOrDefault('D',false) ? 1 : 0) + (keyboard.getOrDefault('A',false) ? -1 : 0),
	            (keyboard.getOrDefault('W',false) ? 1 : 0) + (keyboard.getOrDefault('S',false) ? -1 : 0)
            );
    		for(int i=0; i<Math.min(playerShipEntity.weaponCount(), 9); ++i)
    			if(keyboard.getOrDefault((char)('1'+i), false)) {
    				activeWeapon = i;
    			}
    		
    		playerShipEntity.setThrust(thrust.length2() == 0 ? Vector2.ZERO : thrust.normalize());
    		if(mouseButtonPressed[1])
    			playerShipEntity.fireWeapon(battlefield, deltaT, activeWeapon, cursorWorldPos);
    	}
    }
    
    /** Draws game objects, UI, etc.
     * @param g Graphics object used for rendering.
     */
    private void draw(UBHGraphics g) {
    	g.clear(Color.BLACK);
    	battlefield.draw(g);
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
    private int activeWeapon = 0;
    
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
