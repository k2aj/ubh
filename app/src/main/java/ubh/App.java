package ubh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import ubh.math.Vector2;
import ubh.level.Level;
import ubh.loader.ContentRegistry;
import ubh.ui.*;

public final class App extends WindowAdapter implements AutoCloseable {
	
    public static void main(String[] args) {
    	/* Enable Java2D's OpenGL pipeline.
    	 * This dramatically improves performance and should work on any system
    	 * with non-ancient OpenGL implementation.
    	 */
    	System.getProperties().put("sun.java2d.opengl", "true");  
    	try(final var app = new App(1280,720)) {
    		app.run();
    	}
    }
	
	private static final float 
		MAX_DELTA_T = 1/20f,
		WORLD_HEIGHT = 144;
	
	private final JFrame frame;
	private final CoordinateTransform transform = new CoordinateTransform();
	private final UserInput userInput = new UserInput();
	private final GuiContext gui = new GuiContext(MainMenu.getInstance(), userInput);
	
	public static final ContentRegistry REGISTRY = ContentRegistry.createDefault();
	
	static {
		/* Try to find all .hjson resource files and add them to REGISTRY */
		File sourceCodeLocation;
		try {
			sourceCodeLocation = new File(App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			throw new Error("Failed to locate resources, was the JAR file modified?", e);
		}
		var isJar = sourceCodeLocation.isFile() && sourceCodeLocation.getPath().endsWith(".jar");
		try {
			if(isJar) {
				// game was run from JAR file
				var thisJarFile = new JarFile(sourceCodeLocation);
				REGISTRY.addSource(thisJarFile);
			} else {
				// game was run from a regular folder containing compiled classes, probably by launching from an IDE?
				REGISTRY.addSource(sourceCodeLocation);
			}
		} catch(IOException e) {
			throw new Error("Failed load content sources, was the JAR file modified?", e);
		}
		REGISTRY.registerDefault(BufferedImage.class, REGISTRY.load(BufferedImage.class, "badimage.png"));
		
		// Load user content
		File userContentLocation = null;
		if(isJar) 
			userContentLocation = new File("ubhcontent");
		else {
			var directory = new File(sourceCodeLocation.getParent());
			//System.out.println(directory);
			for(var child : directory.listFiles())
				if(child.getName().equals("ubhcontent") && child.isDirectory())
					userContentLocation = child;
		}
		if(userContentLocation != null)
			REGISTRY.addSource(userContentLocation);
	}
	
	
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
            userInput.getTransform().setWorldSize(transform.getWorldSize());
            transform.setWindowSize(frameSize);
            userInput.getTransform().setWindowSize(frameSize);
            
            gui.update(deltaT);
            userInput.update();
            try(final var graphics = new UBHGraphics((Graphics2D) bufferStrategy.getDrawGraphics(), transform)) {
            	graphics.clear(Color.DARK_GRAY);
            	gui.draw(graphics);
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
