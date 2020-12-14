package ubh;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import ubh.math.Vector2;

/** Wrapper for java.awt.Graphics2D 
 *  <p>
 *  All coordinates and sizes passed to methods are in world space unless specified otherwise.
 */
public final class UBHGraphics implements AutoCloseable {
    private final Graphics2D graphics;
    private final float translationX, translationY, scaleX, scaleY;
    private final Dimension windowSize;
    private boolean fillEnabled = true;

    /**@param graphics
     * @param windowSize Dimensions of the window in screen space.
     * @param worldSize Dimensions of the window in world space.
     */
    public UBHGraphics(Graphics2D graphics, Dimension windowSize, Vector2 worldSize) {
        this.graphics = graphics;
        this.windowSize = windowSize;
        translationX = windowSize.width/2f;
        translationY = windowSize.height/2f;
        scaleX = windowSize.width/worldSize.x()/2;
        scaleY = -windowSize.height/worldSize.y()/2;
    }

    // Functions to convert between screen space & world space coordinates
    private int xToScreenSpace(float x) {
        return (int)(x*scaleX+translationX);
    }
    private int yToScreenSpace(float y) {
        return (int)(y*scaleY+translationY);
    }
    private float xToWorldSpace(int x) {
        return (x-translationX)/scaleX;
    }
    private float yToWorldSpace(int y) {
        return (y-translationY)/scaleY;
    }
    private int widthToScreenSpace(float width) {
        return (int)(width*scaleX);
    }
    private int heightToScreenSpace(float height) {
        return (int)(-height*scaleY);
    }

    /**@param windowPosition Screen space coordinates.
     * @return windowPosition converted to world space. 
     */
    public Vector2 transformIntoWorldSpace(Point windowPosition) {
        return new Vector2(xToWorldSpace(windowPosition.x), yToWorldSpace(windowPosition.y));
    }

    /** Sets color used by future drawing operations. */
    public void setColor(Color color) {
        graphics.setColor(color);
    }

    /** Causes future drawing operations to draw filled shapes. */
    public void enableFill() {
        fillEnabled = true;
    }

    /** Causes future drawing operations to only draw shape edges, without filling them. */
    public void disableFill() {
        fillEnabled = false;
    }

    /**
     * @param x x coordinate of the center of the circle
     * @param y y coordinate of the center of the circle
     * @param radius
     */
    public void drawCircle(float x, float y, float radius) {
        int wx = xToScreenSpace(x-radius),
            wy = yToScreenSpace(y+radius),
            wd = widthToScreenSpace(2*radius);
        if(fillEnabled)
            graphics.fillOval(wx,wy,wd,wd);
        else
            graphics.drawOval(wx,wy,wd,wd);
    }

    public void drawCircle(Vector2 center, float radius) {
        drawCircle(center.x(), center.y(), radius);
    }

    public void drawLine(Vector2 begin, Vector2 end) {
        graphics.drawLine(
            xToScreenSpace(begin.x()),
            yToScreenSpace(begin.y()),
            xToScreenSpace(end.x()),
            yToScreenSpace(end.y())
        );
    }

    /**
     * @param x x coordinate of the center of the rectangle
     * @param y y coordinate of the center of the rectangle
     * @param rx half of the width of the rectangle
     * @param ry half of the height of the rectangle
     */
    public void drawCenteredRect(float x, float y, float rx, float ry) {
        int wx = xToScreenSpace(x-rx),
            wy = yToScreenSpace(y+ry),
            wwidth = widthToScreenSpace(2*rx), 
            wheight = heightToScreenSpace(2*ry);
        if(fillEnabled)
            graphics.fillRect(wx,wy,wwidth,wheight);
        else
            graphics.drawRect(wx,wy,wwidth,wheight);
    }

    /** @param radii half of width & height of drawn rectangle */
    public void drawCenteredRect(Vector2 center, Vector2 radii) {
        drawCenteredRect(center.x(), center.y(), radii.x(), radii.y());
    }

    public void drawLoHiRect(float xlo, float ylo, float xhi, float yhi) {
        drawCenteredRect((xlo+xhi)/2, (ylo+yhi)/2, (xhi-xlo)/2, (yhi-ylo)/2);
    }

    public void drawLoHiRect(Vector2 lo, Vector2 hi) {
        drawLoHiRect(lo.x(),lo.y(),hi.x(),hi.y());
    }

    /**
     * @param x x coordinate of the center of the rectangle
     * @param y y coordinate of the center of the rectangle
     * @param rx half of the width of the rectangle
     * @param ry half of the height of the rectangle
     * @param cos cosine of the rotation angle
     * @param sin sine of the rotation angle
     */
    public void drawRotatedRect(float x, float y, float rx, float ry, float cos, float sin) {
        int[] xs = new int[4], ys = new int[4];
        for(int i=0; i<4; ++i) {
            float curRx = i==0 || i==3 ? -rx : rx,
                  curRy = i<2 ? -ry : ry;
            xs[i] = xToScreenSpace(x + curRx*cos - curRy*sin);
            ys[i] = yToScreenSpace(y + curRx*sin + curRy*cos);
        }
        if(fillEnabled)
            graphics.fillPolygon(xs,ys,4);
        else
            graphics.drawPolygon(xs,ys,4);
    }

	/**
	 * @param center 
	 * @param radii  half of width & height of drawn rectangle
	 * @param dir	 rotation vector 
	 */
    public void drawRotatedRect(Vector2 center, Vector2 radii, Vector2 dir) {
        drawRotatedRect(center.x(), center.y(), radii.x(), radii.y(), dir.x(), dir.y());
    }

    public void drawHpBar(Vector2 center, Vector2 radii, float padding, float hpPercentage) {
        enableFill();
        drawCenteredRect(center, radii);
        setColor(
            hpPercentage > 0.7f ? Color.GREEN :
            hpPercentage > 0.3f ? Color.YELLOW :
            Color.RED
        );
        drawCenteredRect(center.x(), center.y(), (radii.x()-padding)*hpPercentage, radii.y()-padding);
    }

    /** Clears the screen to given color */
    public void clear(Color color) {
        graphics.setBackground(color);
        graphics.clearRect(0,0,windowSize.width,windowSize.height);
    }

    @Override
    public void close() {
        graphics.dispose();
    }
}
