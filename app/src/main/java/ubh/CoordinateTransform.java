package ubh;

import java.awt.Dimension;
import java.awt.Point;

import ubh.math.Vector2;

public class CoordinateTransform {
	
    private Dimension windowSize = new Dimension(1,1);
    private Vector2 worldSize = new Vector2(1,1);
    private float translationX, translationY, scaleX, scaleY;
    
    public void setWorldSize(Vector2 worldSize) {
    	this.worldSize = worldSize;
    	tryUpdate();
    }
    
    public void setWindowSize(Dimension windowSize) {
    	this.windowSize = windowSize;
    	tryUpdate();
    }
    
    public Dimension getWindowSize() {
    	return windowSize;
    }
    
    private void tryUpdate() {
    	if(windowSize != null & worldSize != null) {
	    	translationX = windowSize.width/2f;
	        translationY = windowSize.height/2f;
	        scaleX = windowSize.width/worldSize.x();
	        scaleY = -windowSize.height/worldSize.y();
    	}
    }
	
	// Functions to convert between screen space & world space coordinates
    public int xToScreenSpace(float x) {
        return (int)(x*scaleX+translationX);
    }
    public int yToScreenSpace(float y) {
        return (int)(y*scaleY+translationY);
    }
    public float xToWorldSpace(int x) {
        return (x-translationX)/scaleX;
    }
    public float yToWorldSpace(int y) {
        return (y-translationY)/scaleY;
    }
    public int widthToScreenSpace(float width) {
        return (int)(width*scaleX);
    }
    public int heightToScreenSpace(float height) {
        return (int)(-height*scaleY);
    }
    
    /**@param windowPosition Screen space coordinates.
     * @return windowPosition converted to world space. 
     */
    public Vector2 transformIntoWorldSpace(Point windowPosition) {
        return new Vector2(xToWorldSpace(windowPosition.x), yToWorldSpace(windowPosition.y));
    }

}
