package prefuse.controls;

import prefuse.visual.VisualItem;
import android.view.MotionEvent;

/**
 * Pans the display, changing the viewable region of the visualization. By default, panning is accomplished by clicking on the background of a visualization with the left mouse button and then
 * dragging.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class PanControl extends ControlAdapter
{

	private boolean m_panOverItem;
	private int m_xDown, m_yDown;
	private int m_button;

	/**
	 * Create a new PanControl.
	 */
	public PanControl()
	{
		this(false);
	}

	/**
	 * Create a new PanControl.
	 * 
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is over a visual item.
	 */
	public PanControl(boolean panOverItem)
	{
		m_panOverItem = panOverItem;
	}


	@Override
	public void onTouchDown(MotionEvent event)
	{
		 m_xDown = (int) event.getX();
		 m_yDown = (int) event.getY();
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		display.pan(distanceX, distanceY);
		display.invalidate();
		return true;
	}
	
	@Override
	public void itemDragged(VisualItem item, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		if(!m_panOverItem)
			return;
		onScroll(e1, e2, distanceX, distanceY);
	}
	
	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	// public void mouseDragged(MouseEvent e) {
	// if ( UILib.isButtonPressed(e, m_button) ) {
	// PDisplay display = (PDisplay)e.getComponent();
	// int x = e.getX(), y = e.getY();
	// int dx = x-m_xDown, dy = y-m_yDown;
	// display.pan(dx,dy);
	// m_xDown = x;
	// m_yDown = y;
	// display.repaint();
	// }
	// }

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	// public void mouseReleased(MouseEvent e) {
	// if ( UILib.isButtonPressed(e, m_button) ) {
	// e.getComponent().setCursor(Cursor.getDefaultCursor());
	// m_xDown = -1;
	// m_yDown = -1;
	// }
	// }

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	// public void itemPressed(VisualItem item, MouseEvent e) {
	// if ( m_panOverItem )
	// mousePressed(e);
	// }

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	// public void itemDragged(VisualItem item, MouseEvent e) {
	// if ( m_panOverItem )
	// mouseDragged(e);
	// }
	//
	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	// public void itemReleased(VisualItem item, MouseEvent e) {
	// if ( m_panOverItem )
	// mouseReleased(e);
	// }

} // end of class PanControl
