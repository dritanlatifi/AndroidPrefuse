package prefuse.util.display;

import awt.java.awt.AndroidGraphics2D;
import java.util.EventListener;

import prefuse.PDisplay;

/**
 * Listener interface for monitoring paint events on a Display. This
 * listener is notified both directly before and after a Display has
 * been painted, and allows for custom painting to be performed.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface PaintListener extends EventListener {

    /**
     * Notification that Display painting is beginning.
     * @param d the Display about to paint itself
     * @param g the Graphics context for the Display
     */
    public void prePaint(PDisplay d, AndroidGraphics2D g);
    
    /**
     * Notification that Display painting has completed.
     * @param d the Display about to paint itself
     * @param g the Graphics context for the Display
     */
    public void postPaint(PDisplay d, AndroidGraphics2D g);
    
} // end of interface PaintListener
