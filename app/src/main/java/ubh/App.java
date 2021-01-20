package ubh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ubh.math.Vector2;
import ubh.entity.Affiliation;
import ubh.entity.Ship;
import ubh.entity.ai.*;
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
		WORLD_HEIGHT = 144;
	
	private final JFrame frame;
	private final CoordinateTransform transform = new CoordinateTransform();
	private final UserInput userInput = new UserInput(transform);
	private final PlayerAI playerAI = new PlayerAI(userInput);
	private static final ContentRegistry REGISTRY = ContentRegistry.createDefault();
	
	static {
		/* Try to find all .hjson resource files and add them to REGISTRY */
		try {
			var sourceCodeLocation = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			if(sourceCodeLocation.isFile() && sourceCodeLocation.getPath().endsWith(".jar")) {
				// game was run from JAR file
				var thisJarFile = new JarFile(sourceCodeLocation);
				REGISTRY.addSource(thisJarFile);
			} else {
				// game was run from a regular folder containing compiled classes, probably by launching from an IDE?
				REGISTRY.addSource(sourceCodeLocation);
			}
		} catch (URISyntaxException | IOException e) {
			throw new Error(e);
		}
	}
	
	private static final Ship SHIP = REGISTRY.load(Ship.class, "example_ship");
	private static final BufferedImage IMAGE = REGISTRY.load(BufferedImage.class, "ohno.png");
	
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
        
        battlefield = new Battlefield(AABB.centered(Vector2.ZERO, new Vector2(WORLD_HEIGHT, WORLD_HEIGHT/2)));
        var playerShipEntity = SHIP.createEntity(new ReferenceFrame(Vector2.ZERO), Affiliation.FRIENDLY);
        playerShipEntity.setAI(playerAI);
        battlefield.spawn(playerShipEntity, 0);
        
        for(int i=0; i<5; ++i) {
        	var enemyShip = SHIP.createEntity(
        		new ReferenceFrame(new Vector2(
        			((float) Math.random() - 0.5f)*WORLD_HEIGHT, 
        			WORLD_HEIGHT/2*(float) Math.random())
        		),
        		Affiliation.ENEMY
        	);
        	battlefield.spawn(enemyShip, 0);
        }
    }
    
    private Battlefield battlefield;
    
    /** Draws game objects, UI, etc.
     * @param g Graphics object used for rendering.
     */
    private void draw(UBHGraphics g) {
    	g.clear(Color.BLACK);
    	battlefield.draw(g);
    	g.drawImage(IMAGE, Vector2.ZERO, new Vector2(10, 5), Vector2.polar(t,1));
    }
    
    @Override
    public void close() {
    	frame.dispose();
    }
    
    float t = 0;
    
    /** Runs the main loop of the game. */
    private void run() {

    	frame.setVisible(true);
    	frame.createBufferStrategy(2);
        final var bufferStrategy = frame.getBufferStrategy();
        
        var lastFrameTime = 1/60f;
        
        while(windowAlive) {
        	final var frameStartTimeMs = System.currentTimeMillis();
        	final var deltaT = Math.min(lastFrameTime, MAX_DELTA_T);
        	t += deltaT;
        	
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
