package prefuse.util.display;

import awt.java.awt.AndroidGraphics2D;

import prefuse.PDisplay;
import prefuse.util.PrefuseLib;

/**
 * PinatListener that paints useful debugging statistics over a prefuse
 * display. This includes the current frame rate, the number of visible
 * items, memory usage, and display navigation information.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DebugStatsPainter implements PaintListener {

    /**
     * Does nothing.
     * @see prefuse.util.display.PaintListener#prePaint(prefuse.PDisplay, java.awt.Graphics2D)
     */
    public void prePaint(PDisplay d, AndroidGraphics2D g) {
        
    }
    
    /**
     * Prints a debugging statistics string in the Display.
     * @see prefuse.util.display.PaintListener#postPaint(prefuse.PDisplay, java.awt.Graphics2D)
     */
    public void postPaint(PDisplay d, AndroidGraphics2D g) {
//        g.setFont(d.getFont());
//        g.setColor(d.getForeground());
//        g.drawString(PrefuseLib.getDisplayStats(d), 5, 15);
    }

} // end of class DebugStatsPainter
