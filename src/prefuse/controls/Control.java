package prefuse.controls;

//import awt.java.awt.event.KeyEvent;
//import awt.java.awt.event.KeyListener;
//import awt.java.awt.event.MotionEvent;
//import awt.java.awt.event.MouseListener;
//import awt.java.awt.event.MouseMotionListener;
//import awt.java.awt.event.MouseWheelEvent;
//import awt.java.awt.event.MouseWheelListener;
import java.util.EventListener;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import prefuse.PDisplay;
import prefuse.visual.VisualItem;

/**
 * Listener interface for processing user interface events on a Display.
 * 
 * @author alan newberger
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */

// public interface Control extends EventListener,
// MouseListener, MouseMotionListener, MouseWheelListener, KeyListener

public interface Control extends EventListener, ScaleGestureDetector.OnScaleGestureListener,  GestureDetector.OnDoubleTapListener , GestureDetector.OnGestureListener
{

	/** Represents the use of the left mouse button */
	// public static final int LEFT_MOUSE_BUTTON = MotionEvent.BUTTON1_MASK;
	// /** Represents the use of the middle mouse button */
	// public static final int MIDDLE_MOUSE_BUTTON = MotionEvent.BUTTON2_MASK;
	// /** Represents the use of the right mouse button */
	// public static final int RIGHT_MOUSE_BUTTON = MotionEvent.BUTTON3_MASK;

	/**
	 * set the display of the control
	 * @param diplay
	 */
	public void setDisplay( PDisplay diplay );
	
	
	/**
	 * Indicates if this Control is currently enabled.
	 * 
	 * @return true if the control is enabled, false if disabled
	 */
	public boolean isEnabled();

	/**
	 * Sets the enabled status of this control.
	 * 
	 * @param enabled
	 *            true to enable the control, false to disable it
	 */
	public void setEnabled(boolean enabled);

	// -- Actions performed on VisualItems ------------------------------------

	/**
	 * Invoked when a mouse button is pressed on a VisualItem and then dragged.
	 */
	public void itemDragged(VisualItem item, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

	/**
	 * Invoked when the mouse cursor has been moved onto a VisualItem but no buttons have been pushed.
	 */
	public void itemMoved(VisualItem item, MotionEvent e);

	/**
	 * Invoked when the mouse wheel is rotated while the mouse is over a VisualItem.
	 */
	// public void itemWheelMoved(VisualItem item, MouseWheelEvent e);

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a VisualItem.
	 */
	public void itemClicked(VisualItem item, MotionEvent e);

	/**
	 * Invoked when a mouse button has been pressed on a VisualItem.
	 */
	public void itemPressed(VisualItem item, MotionEvent e);

	/**
	 * Invoked when a mouse button has been released on a VisualItem.
	 */
	public void itemReleased(VisualItem item, MotionEvent e);

	/**
	 * Invoked when the mouse enters a VisualItem.
	 */
	public void itemEntered(VisualItem item, MotionEvent e);
	
	/**
	 * Invoked when the pressing on a item for a "long" time (approximately 2 sec.)
	 */
	public void itemLongPress(VisualItem item, MotionEvent e);

	/**
	 * Invoked when the taping on a item twice
	 */	
	public void itemDoubleTaped(VisualItem item, MotionEvent e);
	
	

	/**
	 * Invoked when the mouse exits a VisualItem.
	 */
	public void itemExited(VisualItem item, MotionEvent e);

	/**
	 * Invoked when a key has been pressed, while the mouse is over a VisualItem.
	 */
	// public void itemKeyPressed(VisualItem item, KeyEvent e);

	/**
	 * Invoked when a key has been released, while the mouse is over a VisualItem.
	 */
	// public void itemKeyReleased(VisualItem item, KeyEvent e);

	/**
	 * Invoked when a key has been typed, while the mouse is over a VisualItem.
	 */
	// public void itemKeyTyped(VisualItem item, KeyEvent e);

	// -- Actions performed on the Display ------------------------------------

	/**
	 * Invoked when the mouse enters the Display.
	 */
	// public void mouseEntered(MotionEvent e);

	/**
	 * Invoked when the mouse exits the Display.
	 */
	// public void mouseExited(MotionEvent e);

	/**
	 * Invoked when a mouse button has been pressed on the Display but NOT on a VisualItem.
	 */
	public void onTouchDown(MotionEvent e);


	/**
	 * Invoked when a user drags the finger with touch on display (but NOT a VisualItem) and then dragged.
	 */
	public void touchDragged(MotionEvent e);

	/**
	 * Invoked when the mouse cursor has been moved on the Display (but NOT a VisualItem) and no buttons have been pushed.
	 */
	public void mouseMoved(MotionEvent e);

	/**
	 * Invoked when the mouse wheel is rotated while the mouse is over the Display (but NOT a VisualItem).
	 */
	// public void mouseWheelMoved(MouseWheelEvent e);

	/**
	 * Invoked when a key has been pressed, while the mouse is NOT over a VisualItem.
	 */
	// public void keyPressed(KeyEvent e);

	/**
	 * Invoked when a key has been released, while the mouse is NOT over a VisualItem.
	 */
	// public void keyReleased(KeyEvent e);

	/**
	 * Invoked when a key has been typed, while the mouse is NOT over a VisualItem.
	 */
	// public void keyTyped(KeyEvent e);

} // end of inteface ControlListener
