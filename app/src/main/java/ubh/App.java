package ubh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ubh.math.Vector2;
import ubh.entity.Affiliation;
import ubh.entity.Ship;
import ubh.entity.ai.PlayerAI;
import ubh.loader.ContentRegistry;
import ubh.math.AABB;
import ubh.math.ReferenceFrame;
import ubh.ui.UserInput;

public final class App extends WindowAdapter implements AutoCloseable {
	
    public static void main(String[] args) {
    	try(final var app = new App(1280,720)) {
    		app.run();
    	}
    }
	
	private static final float 
		MAX_DELTA_T = 1/20f,
		WORLD_HEIGHT = 72;
	
	private final JFrame frame;
	private final CoordinateTransform transform = new CoordinateTransform();
	private final UserInput userInput = new UserInput(transform);
	private final PlayerAI playerAI = new PlayerAI(userInput);
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
        frame.addKeyListener(userInput);
        frame.addMouseListener(userInput);
        frame.addMouseMotionListener(userInput);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        battlefield = new Battlefield(AABB.centered(Vector2.ZERO, new Vector2(WORLD_HEIGHT*2, WORLD_HEIGHT)));
        var playerShipEntity = SHIP.createEntity(new ReferenceFrame(Vector2.ZERO), Affiliation.FRIENDLY);
        playerShipEntity.setAI(playerAI);
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
    
    private Battlefield battlefield;
    
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
            transform.setWorldSize(new Vector2(worldWidth, WORLD_HEIGHT));
            transform.setWindowSize(frameSize);
            
            battlefield.update(deltaT);
            try(final var graphics = new UBHGraphics((Graphics2D) bufferStrategy.getDrawGraphics(), transform)) {
            	draw(graphics);
            }
            
        	bufferStrategy.show();
        	
        	lastFrameTime = (System.currentTimeMillis() - frameStartTimeMs) / 1000f;
        }
    }
    private boolean windowAlive = true;
    
    @Override public void windowClosing(WindowEvent e) {
        windowAlive = false;
    }
}
