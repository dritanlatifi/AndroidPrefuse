package prefuse.controls;

//import awt.java.awt.Cursor;
//import awt.java.awt.event.MouseEvent;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import awt.java.awt.AndroidGraphics2D;
import awt.java.awt.geom.Point2D;

import prefuse.PDisplay;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * Zooms the display, changing the scale of the viewable region. By default, zooming is achieved by pressing the right mouse button on the background of the visualization and dragging the mouse up or
 * down. Moving the mouse up zooms out the display around the spot the mouse was originally pressed. Moving the mouse down similarly zooms in the display, making items larger.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ZoomControl extends AbstractZoomControl
{
	private Point2D down = new Point2D.Float();
	private float scaleFactor = 1.0f;

	/**
	 * Create a new zoom control.
	 */
	public ZoomControl()
	{
		// do nothing
	}
	
	@Override
	public void onTouchDown(MotionEvent event)
	{
		display.getAbsoluteCoordinate(new Point2D.Float(event.getX(), event.getY()), down);
		Log.d("PZOOM", "event touch: ("+event.getX() + "," + event.getY() + ")");
	}

	@Override
	public void itemEntered(VisualItem item, MotionEvent event)
	{
		down.setLocation(event.getX(), event.getY());
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector)
	{
		scaleFactor *= detector.getScaleFactor();
		// don't let the object get too small or too large.
		scaleFactor = Math.max(0.9f, Math.min(scaleFactor, 1.2f));
		
		int currentSpan = (int) detector.getCurrentSpan();
		int previousSpan = (int) detector.getPreviousSpan();
		
		// if (UILib.isButtonPressed(e, button)) {
		if (display.isTranformInProgress() || Math.abs(currentSpan - previousSpan ) < 10 ) // if some other transformation is in progress or the distance between to fingers is not changing (while the fingers remain on screen)
			return true;
		
		display.getAbsoluteCoordinate(new Point2D.Float(detector.getFocusX(), detector.getFocusY()), down);
		zoom(display, down, scaleFactor, true);

		display.invalidate();
		return true;
	}

} // end of class ZoomControl
