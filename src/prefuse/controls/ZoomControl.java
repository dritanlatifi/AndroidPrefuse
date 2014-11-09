package prefuse.controls;

//import awt.java.awt.Cursor;
//import awt.java.awt.event.MouseEvent;
import android.graphics.Canvas;
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

	private int yLast;
	private Point2D down = new Point2D.Float();
	private float scaleFactor = 1.0f;

	// private int button = RIGHT_MOUSE_BUTTON;

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
		down.setLocation(event.getX(), event.getY());
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
		scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
		// if (UILib.isButtonPressed(e, button)) {
		if (display.isTranformInProgress() || yLast == -1)
		{
			yLast = -1;
			return true;
		}

		int y = (int) detector.getFocusY();
		int dy = y - yLast;
		double zoom = 1 + ((double) dy) / 100;
		//zoom(display, down, scaleFactor, true); // TODO for Dritan: check if it is necessary or better to use scaleFactor instead of calculating it 
		zoom(display, down, zoom, true);

		yLast = y;
		display.invalidate();
		return true;
	}

} // end of class ZoomControl
