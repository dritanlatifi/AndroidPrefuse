package prefuse.controls;

//import awt.java.awt.event.KeyEvent;
//import awt.java.awt.event.MotionEvent;
//import awt.java.awt.event.MouseWheelEvent;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.KeyEvent.DispatcherState;
import prefuse.PDisplay;
import prefuse.visual.VisualItem;

/**
 * Adapter class for processing prefuse interface events. Subclasses can override the desired methods to perform user interface event handling.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ControlAdapter implements Control
{

	private boolean m_enabled = true;
	protected PDisplay display;
	
	public ControlAdapter()
	{
		
	}
	
	public ControlAdapter(PDisplay display)
	{
		this.display = display;
	}
	/**
	 * @see prefuse.controls.Control#isEnabled()
	 */
	public boolean isEnabled()
	{
		return m_enabled;
	}

	/**
	 * @see prefuse.controls.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled)
	{
		m_enabled = enabled;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemDragged(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemMoved(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemMoved(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemWheelMoved(prefuse.visual.VisualItem, java.awt.event.MouseWheelEvent)
	 */
	// public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemClicked(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemPressed(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemReleased(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemEntered(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MotionEvent)
	 */
	public void itemExited(VisualItem item, MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#itemKeyPressed(prefuse.visual.VisualItem, java.awt.event.KeyEvent)
	 */
	// public void itemKeyPressed(VisualItem item, KeyEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#itemKeyReleased(prefuse.visual.VisualItem, java.awt.event.KeyEvent)
	 */
	// public void itemKeyReleased(VisualItem item, KeyEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#itemKeyTyped(prefuse.visual.VisualItem, java.awt.event.KeyEvent)
	 */
	// public void itemKeyTyped(VisualItem item, KeyEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#mouseEntered(java.awt.event.MotionEvent)
	 */
	// public void mouseEntered(MotionEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#mouseExited(java.awt.event.MotionEvent)
	 */
	// public void mouseExited(MotionEvent e) {
	// }

	/**
	 * @see prefuse.controls.Control#mousePressed(java.awt.event.MotionEvent)
	 */
	public void onTouchDown(MotionEvent e)
	{
		
	}

	/**
	 * @see prefuse.controls.Control#touchReleased(java.awt.event.MotionEvent)
	 */
	public void touchReleased(MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#mouseClicked(java.awt.event.MotionEvent)
	 */
	public void mouseClicked(MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#mouseDragged(java.awt.event.MotionEvent)
	 */
	public void touchDragged(MotionEvent e)
	{
	}

	/**
	 * @see prefuse.controls.Control#mouseMoved(java.awt.event.MotionEvent)
	 */
	public void mouseMoved(MotionEvent e)
	{
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e, MotionEvent arg1, float arg2, float arg3)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{

	}

	@Override
	public boolean onScroll(MotionEvent e, MotionEvent arg1, float arg2, float arg3)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void itemLongPress(VisualItem item, MotionEvent e)
	{
	}

	@Override
	public void setDisplay(PDisplay display)
	{
		this.display = display;
	}
	
	public PDisplay getDisplay()
	{
		return display;
	}

	@Override
	public void itemDoubleTaped(VisualItem item, MotionEvent e)
	{
		
	}

} // end of class ControlAdapter
