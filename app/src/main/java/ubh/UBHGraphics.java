package ubh;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import ubh.math.AABB;
import ubh.math.Rectangle;
import ubh.math.Vector2;

/** Wrapper for java.awt.Graphics2D 
 *  <p>
 *  All coordinates and sizes passed to methods are in world space unless specified otherwise.
 */
public final class UBHGraphics implements AutoCloseable {
    private final Graphics2D graphics;
    private final CoordinateTransform transform;
    private boolean fillEnabled = true;

    public UBHGraphics(Graphics2D graphics, CoordinateTransform transform) {
        this.graphics = graphics;
        this.transform = transform;
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
        int wx = transform.xToScreenSpace(x-radius),
            wy = transform.yToScreenSpace(y+radius),
            wd = transform.widthToScreenSpace(2*radius);
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
    		transform.xToScreenSpace(begin.x()),
    		transform.yToScreenSpace(begin.y()),
    		transform.xToScreenSpace(end.x()),
    		transform.yToScreenSpace(end.y())
        );
    }

    /**
     * @param x x coordinate of the center of the rectangle
     * @param y y coordinate of the center of the rectangle
     * @param rx half of the width of the rectangle
     * @param ry half of the height of the rectangle
     */
    public void drawCenteredRect(float x, float y, float rx, float ry) {
        int wx = transform.xToScreenSpace(x-rx),
            wy = transform.yToScreenSpace(y+ry),
            wwidth = transform.widthToScreenSpace(2*rx), 
            wheight = transform.heightToScreenSpace(2*ry);
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
            xs[i] = transform.xToScreenSpace(x + curRx*cos - curRy*sin);
            ys[i] = transform.yToScreenSpace(y + curRx*sin + curRy*cos);
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
    
    public void drawImage(BufferedImage image, Vector2 center, Vector2 radii, Vector2 dir) {
    	var xform = new AffineTransform();
    	xform.translate(transform.xToScreenSpace(center.x()), transform.yToScreenSpace(center.y()));
    	xform.rotate(dir.x(), -dir.y());
    	var pRx = transform.widthToScreenSpace(radii.x());
    	var pRy = transform.heightToScreenSpace(radii.y());
    	xform.translate(-pRx, -pRy);
    	xform.scale(2*pRx/(double)image.getWidth(), 2*pRy/(double)image.getHeight());
    	graphics.drawImage(image, xform, null);
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
        graphics.clearRect(0,0,transform.getWindowSize().width,transform.getWindowSize().height);
    }
    
    public void setClip(Optional<AABB> area) {
    	if(area.isEmpty()) {
    		graphics.setClip(null);
    	} else {
    		var p = area.get().getPosition();
    		var r = area.get().getRadii();
    		int wx = transform.xToScreenSpace(p.x() - r.x()),
    			wy = transform.yToScreenSpace(p.y()+r.y()),
    			wwidth = transform.widthToScreenSpace(2*r.x()), 
                wheight = transform.heightToScreenSpace(2*r.y());
    		graphics.setClip(wx,wy,wwidth,wheight);
    	}
    }

    @Override
    public void close() {
        graphics.dispose();
    }
}
