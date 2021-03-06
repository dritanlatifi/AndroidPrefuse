package prefuse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import awt.java.awt.Color;
import awt.java.awt.Dimension;
import awt.java.awt.Font;
import awt.java.awt.Graphics;
import awt.java.awt.AndroidGraphics2D;
import awt.java.awt.Insets;

import awt.java.awt.Image;
import awt.java.awt.Point;
import awt.java.awt.Rectangle;
import awt.java.awt.RenderingHints;

import awt.java.awt.geom.AffineTransform;
import awt.java.awt.geom.NoninvertibleTransformException;
import awt.java.awt.geom.Point2D;
import awt.java.awt.geom.Rectangle2D;
import awt.java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import prefuse.activity.PActivity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.Control;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.StringLib;
import prefuse.util.UpdateListener;
import prefuse.util.collections.CopyOnWriteArrayList;
import prefuse.util.display.BackgroundPainter;
import prefuse.util.display.Clip;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.display.RenderingQueue;
import prefuse.util.io.LogTime;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

/**
 * <p>
 * User interface component that provides an interactive view onto a visualization. The Display is responsible for drawing items to the screen and providing callbacks for user interface actions such
 * as mouse and keyboard events. A Display must be associated with an {@link prefuse.Visualization} from which it pulls the items to visualize.
 * </p>
 * 
 * <p>
 * To control which {@link prefuse.visual.VisualItem} instances are drawn, the Display also maintains an optional {@link prefuse.data.expression.Predicate} for filtering items. The drawing order of
 * items is controlled by an {@link prefuse.visual.sort.ItemSorter} instance, which calculates a score for each item. Items with higher scores are drawn later, and hence on top of lower scoring items.
 * </p>
 * 
 * <p>
 * The {@link prefuse.controls.Control Control} interface provides the user interface callbacks for supporting interaction. The {@link prefuse.controls} package contains a number of pre-built
 * <code>Control</code> implementations for common interactions.
 * </p>
 * 
 * <p>
 * The Display class also supports arbitrary graphics transforms through the <code>java.awt.geom.AffineTransform</code> class. The {@link #setTransform(java.awt.geom.AffineTransform) setTransform}
 * method allows arbitrary transforms to be applied, while the {@link #pan(double,double) pan} and {@link #zoom(java.awt.geom.Point2D,double) zoom} methods provide convenience methods that
 * appropriately update the current transform to achieve panning and zooming of the presentation space.
 * </p>
 * 
 * <p>
 * Additionally, each Display instance also supports use of a text editor to facilitate direct editing of text. See the various {@link #editText(prefuse.visual.VisualItem, String)} methods.
 * </p>
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author <a href="http://dritan.at">Dritan Latifi</a>
 * @see Visualization
 * @see prefuse.controls.Control
 * @see prefuse.controls
 */
public class PDisplay extends View
{
	private int numberThreads = 1;
	
	public int getNumberThreads()
	{
		return numberThreads;
	}

	public void setNumberThreads(int numberThreads)
	{
		if (numberThreads < 1)
			throw new IllegalArgumentException();
		this.numberThreads = numberThreads;
	}

	private VisualItem activeItem = null;

	private InputEventCapturer inputEC = new InputEventCapturer();

	// State objects and values related to gesture tracking.
	private ScaleGestureDetector mScaleGestureDetector;
	private GestureDetectorCompat mGestureDetector;

	/**
	 * Preferred width of the screen, if not set android will calculate the needed size
	 */
	protected int preferredWidth = 0;

	/**
	 * Preferred width of the screen. if not set android will calculate the needed size
	 */
	protected int preferredHeight = 0;

	private static final Logger s_logger = Logger.getLogger(PDisplay.class.getName());

	// visual item source
	protected Visualization m_vis;
	protected AndPredicate m_predicate = new AndPredicate();

	// listeners
	protected CopyOnWriteArrayList<Control> m_controls = new CopyOnWriteArrayList<Control>();
	protected CopyOnWriteArrayList<PaintListener> m_painters;
	protected CopyOnWriteArrayList<ItemBoundsListener> m_bounders;

	// display
	protected Canvas m_offscreen;
	protected Bitmap m_offscreen_bitmap;
	protected Clip m_clip = new Clip();
	protected Clip m_screen = new Clip();
	protected Clip m_bounds = new Clip();
	protected Rectangle2D m_rclip = new Rectangle2D.Double();
	protected boolean m_damageRedraw = true;
	protected boolean m_highQuality = false;

	// optional background image
	protected BackgroundPainter m_bgpainter = null;

	// rendering queue
	protected RenderingQueue m_queue = new RenderingQueue();
	protected int m_visibleCount = 0;

	// transform variables
	protected AffineTransform m_transform = new AffineTransform();
	protected AffineTransform m_itransform = new AffineTransform();
	protected TransformActivity m_transact = new TransformActivity();
	protected Point2D m_tmpPoint = new Point2D.Double();

	// frame count and debugging output
	protected double frameRate;
	protected int nframes = 0;
	private int sampleInterval = 10;
	private long mark = -1L;

	// text editing variables
	private EditText m_editor;
	private boolean m_editing;
	private VisualItem m_editItem;
	private String m_editAttribute;

	// imitate Insets of JComponent
	protected Insets m_insets = new Insets(0, 0, 0, 0);
	
	protected void log(String key)
	{
		LogTime.log(key);
	}
	
	/**
	 * Creates a new Display instance. You will need to associate this Display with a {@link Visualization} for it to display anything.
	 */
	public PDisplay(Context context)
	{
		this(context, null);
	}

	/**
	 * Creates a new Display associated with the given Visualization. By default, all {@link prefuse.visual.VisualItem} instances in the {@link Visualization} will be drawn by the Display.
	 * 
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 */
	public PDisplay(Context context, Visualization visualization)
	{
		this(context, visualization, (Predicate) null);
	}

	/**
	 * Creates a new Display associated with the given Visualization that draws all VisualItems in the visualization that pass the given Predicate. The predicate string will be parsed by the
	 * {@link prefuse.data.expression.parser.ExpressionParser} to get a {@link prefuse.data.expression.Predicate} instance.
	 * 
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 * @param predicate
	 *            a predicate expression in the prefuse expression language. This expression will be parsed; if the parsing fails or does not result in a Predicate instance, an exception will result.
	 */
	public PDisplay(Context context, Visualization visualization, String predicate)
	{
		this(context, visualization, (Predicate) ExpressionParser.parse(predicate, true));
	}

	/**
	 * Creates a new Display associated with the given Visualization that draws all VisualItems in the visualization that pass the given Predicate.
	 * 
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 * @param predicate
	 *            the filtering {@link prefuse.data.expression.Predicate}
	 */
	public PDisplay(Context context, Visualization visualization, Predicate predicate)
	{
		super(context);
		// setDoubleBuffered(false);
		setBackgroundColor(android.graphics.Color.WHITE);

		// initialize text editor
		m_editing = false;
		m_editor = new EditText(context);
		m_editor.setVisibility(View.GONE);

		// register input event capturer TODO for Dritan: implement Event

		// Sets up interactions
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
		mGestureDetector = new GestureDetectorCompat(context, mGestureListener);

		// invalidate the display when the filter changes
		m_predicate.addExpressionListener(new UpdateListener()
		{
			public void update(Object src)
			{
				damageReport();
			}
		});

		setVisualization(visualization);
		setPredicate(predicate);
		setSize(400, 400); // set a default size
	}

	/**
	 * Resets the display by clearing the offscreen buffer and flushing the internal rendering queue. This method can help reclaim memory when a Display is not visible.
	 */
	public void reset()
	{
		m_offscreen = null;
		m_queue.clean();
	}

	/**
	 * Set the preferred size of the Display. 
	 * Note: The size of the view is specified from the layout manager. i.e. the view must fit in a layout. As result at most of the time 
	 * this method does not affect the size of the view. Only if for the View is specified MeasureSpec.UNSPECIFIED has this method an effect on the view size 
	 * 
	 * @incomplete
	 * @param width
	 *            the width of the Display in pixels
	 * @param height
	 *            the height of the Display in pixels
	 * 
	 */
	public void setSize(int width, int height)
	{
		m_offscreen = null;
		this.preferredWidth = width;
		this.preferredHeight = height;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		if (preferredHeight != 0 && preferredWidth != 0)
			setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec)
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY)
		{
			// We were told how big to be
			result = specSize;
		} else
		{
			// Measure the text
			result = preferredWidth + getPaddingLeft() + getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}

		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec)
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY)
		{
			// We were told how big to be
			result = specSize;
		} else
		{
			// Measure the text (beware: ascent is a negative number)
			result = preferredHeight + getPaddingTop() + getPaddingBottom();
			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by measureSpec
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Set the size of the Display. TODO for Dritan: on android size of View cannot be set
	 * 
	 * @param d
	 *            the dimensions of the Display in pixels
	 */
	public void setSize(Dimension d)
	{
		// m_offscreen = null;
		// setPreferredSize(d);
		// super.setSize(d);
	}

	/**
	 * Invalidates this component. Overridden to ensure that an internal damage report is generated. TODO for Dritan: see if the invalidate method from android.View is the same as
	 * java.awt.Component#invalidate => i.e. see if call of damageReport is necessary
	 * 
	 */
	public void invalidate()
	{
		damageReport();
		super.invalidate();
	}

	/**
	 * Sets the font used by this Display. This determines the font used by this Display's text editor and in any debugging text. 
	 * TODO: add font support. if it is not possible, then remove this method
	 * 
	 * @param f
	 *            the Font to use
	 */
	public void setFont(Font f)
	{
		
	}

	/**
	 * Returns the running average frame rate for this Display.
	 * 
	 * @return the frame rate
	 */
	public double getFrameRate()
	{
		return frameRate;
	}

	/**
	 * Determines if the Display uses a higher quality rendering, using anti-aliasing. This causes drawing to be much slower, however, and so is disabled by default.
	 * 
	 * @param on
	 *            true to enable anti-aliased rendering, false to disable it
	 */
	public void setHighQuality(boolean on)
	{
		if (m_highQuality != on)
			damageReport();
		m_highQuality = on;
	}

	/**
	 * Indicates if the Display is using high quality (return value true) or regular quality (return value false) rendering.
	 * 
	 * @return true if high quality rendering is enabled, false otherwise
	 */
	public boolean isHighQuality()
	{
		return m_highQuality;
	}

	/**
	 * Returns the Visualization backing this Display.
	 * 
	 * @return this Display's {@link Visualization}
	 */
	public Visualization getVisualization()
	{
		return m_vis;
	}

	/**
	 * Set the Visualiztion associated with this Display. This Display will render the items contained in the provided visualization. If this Display is already associated with a different
	 * Visualization, the Display unregisters itself with the previous one.
	 * 
	 * @param vis
	 *            the backing {@link Visualization} to use.
	 */
	public void setVisualization(Visualization vis)
	{
		// TODO: synchronization?
		if (m_vis == vis)
		{
			// nothing need be done
			return;
		} else if (m_vis != null)
		{
			// remove this display from it's previous registry
			m_vis.removeDisplay(this);
		}
		m_vis = vis;
		if (m_vis != null)
			m_vis.addDisplay(this);
	}

	/**
	 * Returns the filtering Predicate used to control what items are drawn by this display.
	 * 
	 * @return the filtering {@link prefuse.data.expression.Predicate}
	 */
	public Predicate getPredicate()
	{
		if (m_predicate.size() == 1)
		{
			return BooleanLiteral.TRUE;
		} else
		{
			return m_predicate.get(0);
		}
	}

	/**
	 * Sets the filtering Predicate used to control what items are drawn by this Display.
	 * 
	 * @param expr
	 *            the filtering predicate to use. The predicate string will be parsed by the {@link prefuse.data.expression.parser.ExpressionParser}. If the parse fails or does not result in a
	 *            {@link prefuse.data.expression.Predicate} instance, an exception will be thrown.
	 */
	public void setPredicate(String expr)
	{
		Predicate p = (Predicate) ExpressionParser.parse(expr, true);
		setPredicate(p);
	}

	/**
	 * Sets the filtering Predicate used to control what items are drawn by this Display.
	 * 
	 * @param p
	 *            the filtering {@link prefuse.data.expression.Predicate} to use
	 */
	public synchronized void setPredicate(Predicate p)
	{
		if (p == null)
		{
			m_predicate.set(VisiblePredicate.TRUE);
		} else
		{
			m_predicate.set(new Predicate[]
			{ p, VisiblePredicate.TRUE });
		}
	}

	/**
	 * Returns the number of visible items processed by this Display. This includes items not currently visible on screen due to the current panning or zooming state.
	 * 
	 * @return the count of visible items
	 */
	public int getVisibleItemCount()
	{
		return m_visibleCount;
	}

	/**
	 * Get the ItemSorter that determines the rendering order of the VisualItems. Items are drawn in ascending order of the scores provided by the ItemSorter.
	 * 
	 * @return this Display's {@link prefuse.visual.sort.ItemSorter}
	 */
	public ItemSorter getItemSorter()
	{
		return m_queue.sort;
	}

	/**
	 * Set the ItemSorter that determines the rendering order of the VisualItems. Items are drawn in ascending order of the scores provided by the ItemSorter.
	 * 
	 * @return the {@link prefuse.visual.sort.ItemSorter} to use
	 */
	public synchronized void setItemSorter(ItemSorter cmp)
	{
		damageReport();
		m_queue.sort = cmp;
	}

	/**
	 * Set a background image for this display.
	 * 
	 * @param image
	 *            the background Image. If a null value is provided, than no background image will be shown.
	 * @param fixed
	 *            true if the background image should stay in a fixed position, invariant to panning, zooming, or rotation; false if the image should be subject to view transforms
	 * @param tileImage
	 *            true to tile the image across the visible background, false to only include the image once
	 */
	public synchronized void setBackgroundImage(Image image, boolean fixed, boolean tileImage)
	{
		BackgroundPainter bg = null;
		if (image != null)
			bg = new BackgroundPainter(image, fixed, tileImage);
		setBackgroundPainter(bg);
	}

	/**
	 * Set a background image for this display.
	 * 
	 * @param location
	 *            a location String of where to retrieve the image file from. Uses {@link prefuse.util.io.IOLib#urlFromString(String)} to resolve the String. If a null value is provided, than no
	 *            background image will be shown.
	 * @param fixed
	 *            true if the background image should stay in a fixed position, invariant to panning, zooming, or rotation; false if the image should be subject to view transforms
	 * @param tileImage
	 *            true to tile the image across the visible background, false to only include the image once
	 */
	public synchronized void setBackgroundImage(String location, boolean fixed, boolean tileImage)
	{
		BackgroundPainter bg = null;
		if (location != null)
			bg = new BackgroundPainter(location, fixed, tileImage);
		setBackgroundPainter(bg);
	}

	private void setBackgroundPainter(BackgroundPainter bg)
	{
		if (m_bgpainter != null)
			removePaintListener(m_bgpainter);
		m_bgpainter = bg;
		if (bg != null)
			addPaintListener(bg);
	}

	// ------------------------------------------------------------------------
	// Clip / Bounds Management

	/**
	 * Indicates if damage/redraw rendering is enabled. If enabled, the display will only redraw within the bounding box of all areas that have changed since the last rendering operation. For small
	 * changes, such as a single item being dragged, this can result in a significant performance increase. By default, the damage/redraw optimization is enabled. It can be disabled, however, if
	 * rendering artifacts are appearing in your visualization. Be careful though, as this may not be the best solution. Rendering artifacts may result because the item bounds returned by
	 * {@link prefuse.visual.VisualItem#getBounds()} are not accurate and the item's {@link prefuse.render.Renderer} is drawing outside of the reported bounds. In this case, there is usually a bug in
	 * the Renderer. One reported problem arises from Java itself, however, which inaccurately redraws images outside of their reported bounds. If you have a visulization with a number of images and
	 * are seeing rendering artifacts, try disabling damage/redraw.
	 * 
	 * @return true if damage/redraw optimizations are enabled, false otherwise (in which case the entire Display is redrawn upon a repaint)
	 */
	public synchronized boolean isDamageRedraw()
	{
		return m_damageRedraw;
	}

	/**
	 * Sets if damage/redraw rendering is enabled. If enabled, the display will only redraw within the bounding box of all areas that have changed since the last rendering operation. For small
	 * changes, such as a single item being dragged, this can result in a significant performance increase. By default, the damage/redraw optimization is enabled. It can be disabled, however, if
	 * rendering artifacts are appearing in your visualization. Be careful though, as this may not be the best solution. Rendering artifacts may result because the item bounds returned by
	 * {@link prefuse.visual.VisualItem#getBounds()} are not accurate and the item's {@link prefuse.render.Renderer} is drawing outside of the reported bounds. In this case, there is usually a bug in
	 * the Renderer. One reported problem arises from Java itself, however, which inaccurately redraws images outside of their reported bounds. If you have a visulization with a number of images and
	 * are seeing rendering artifacts, try disabling damage/redraw.
	 * 
	 * @param b
	 *            true to enable damage/redraw optimizations, false otherwise (in which case the entire Display will be redrawn upon a repaint)
	 */
	public synchronized void setDamageRedraw(boolean b)
	{
		m_damageRedraw = b;
		m_clip.invalidate();
	}

	/**
	 * Reports damage to the Display within in the specified region.
	 * 
	 * @param region
	 *            the damaged region, in absolute coordinates
	 */
	public synchronized void damageReport(Rectangle2D region)
	{
		if (m_damageRedraw)
			m_clip.union(region);
	}

	/**
	 * Reports damage to the entire Display.
	 */
	public synchronized void damageReport()
	{
		m_clip.invalidate();
	}

	/**
	 * Clears any reports of damaged regions, causing the Display to believe that the display contents are up-to-date. If used incorrectly this can cause inaccurate rendering. <strong>Call this method
	 * only if you know what you are doing.</strong>
	 */
	public synchronized void clearDamage()
	{
		if (m_damageRedraw)
			m_clip.reset();
	}

	/**
	 * Returns the bounds, in absolute (item-space) coordinates, of the total bounds occupied by all currently visible VisualItems. This method allocates a new Rectangle2D instance for the result.
	 * 
	 * @return the bounding box of all visibile VisualItems
	 * @see #getItemBounds(Rectangle2D)
	 */
	public synchronized Rectangle2D getItemBounds()
	{
		return getItemBounds(new Rectangle2D.Double());
	}

	/**
	 * Returns the bounds, in absolute (item-space) coordinates, of the total bounds occupied by all currently visible VisualItems.
	 * 
	 * @param b
	 *            the Rectangle2D to use to store the return value
	 * @return the bounding box of all visibile VisualItems
	 */
	public synchronized Rectangle2D getItemBounds(Rectangle2D b)
	{
		b.setFrameFromDiagonal(m_bounds.getMinX(), m_bounds.getMinY(), m_bounds.getMaxX(), m_bounds.getMaxY());
		return b;
	}

	// ------------------------------------------------------------------------
	// Rendering

	/**
	 * Returns the offscreen buffer used for double buffering.
	 * 
	 * @return the offscreen buffer
	 */
	public Canvas getOffscreenBuffer()
	{
		return m_offscreen;
	}

	/**
	 * @incomplete TODO for Dritan: this code is not tested and not necessary for the Version 1.0 Creates a new buffered image to use as an offscreen buffer.
	 */
	protected BufferedImage getNewOffscreenBuffer(int width, int height)
	{
//		BufferedImage img = null;
		// if (!GraphicsEnvironment.isHeadless()) {
		// try {
		// img = (BufferedImage) createImage(width, height);
		// } catch (Exception e) {
		// img = null;
		// }
		// }
		// if (img == null) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// }
		// return img;
	}

	/**
	 * Saves a copy of this display as an image to the specified output stream.
	 * 
	 * @incomplete TODO for Dritan: this code is not tested and not necessary for the Version 1.0
	 * @param output
	 *            the output stream to write to.
	 * @param format
	 *            the image format (e.g., "JPG", "PNG"). The number and kind of available formats varies by platform. See {@link javax.imageio.ImageIO} and related classes for more.
	 * @param scale
	 *            how much to scale the image by. For example, a value of 2.0 will result in an image with twice the pixel width and height of this Display.
	 * @return true if image was successfully saved, false if an error occurred.
	 */
	public boolean saveImage(OutputStream output, Bitmap.CompressFormat format, int quality, double scale)
	{
		try
		{

			// get an image to draw into
			// Define a bitmap with the same size as the view
			Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
			// Bind a canvas to it
			Canvas canvas = new Canvas(bitmap);
			// Get the view's background
			Drawable bgDrawable = this.getBackground();
			if (bgDrawable != null)
				// has background drawable, then draw it on the canvas
				bgDrawable.draw(canvas);
			else
				// does not have background drawable, then draw white background
				// on the canvas
				canvas.drawColor(android.graphics.Color.WHITE);
			// draw the view on the canvas
			this.draw(canvas);

			bitmap.compress(format, quality, output);
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * TODO for Dritan: review this old code
	 * 
	 * @param output
	 * @param format
	 * @param scale
	 * @return
	 */
	public boolean saveImageOld(OutputStream output, String format, double scale)
	{
		try
		{
			// get an image to draw into
			// Dimension d = new Dimension((int) (scale * getWidth()),
			// (int) (scale * getHeight()));
			// BufferedImage img = getNewOffscreenBuffer(d.width, d.height);
			// Graphics2D g = (Graphics2D) img.getGraphics();
			//
			// // set up the display, render, then revert to normal settings
			// Point2D p = new Point2D.Double(0, 0);
			// zoom(p, scale); // also takes care of damage report
			// boolean q = isHighQuality();
			// setHighQuality(true);
			// paintDisplay(g, d);
			// setHighQuality(q);
			// zoom(p, 1 / scale); // also takes care of damage report
			//
			// // save the image and return
			// ImageIO.write(img, format, output);
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Paints the offscreen buffer to the provided graphics context.
	 * 
	 * @incomplete TODO for Dritan: not for the Version 1.0
	 * @param g
	 *            the Graphics context to paint to
	 */
	protected void paintBufferToScreen(AndroidGraphics2D g)
	{

		 synchronized (this) {
			 g.getCanvas().drawBitmap(m_offscreen_bitmap, 0, 0, null);
		 }
	}

	/**
	 * @incomplete TODO for Dritan: not for the Version 1.0 Immediately repaints the contents of the offscreen buffer to the screen. This bypasses the usual rendering loop.
	 */
	public void repaintImmediate()
	{
		// Graphics g = this.getGraphics();
		// if (g != null && m_offscreen != null) {
		// paintBufferToScreen(g);
		// }
	}

	/**
	 * Sets the transform of the provided Graphics context to be the transform of this Display and sets the desired rendering hints.
	 * 
	 * @param g
	 *            the Graphics context to prepare.
	 */
	protected void prepareGraphics(AndroidGraphics2D g)
	{
		if (m_transform != null)
			g.transform(m_transform);
	}

	/**
	 * Sets the rendering hints that should be used while drawing the visualization to the screen. Subclasses can override this method to set hints as desired. Such subclasses should consider honoring
	 * the high quality flag in one form or another.
	 * 
	 * @param g
	 *            the Graphics context on which to set the rendering hints
	 */
	protected void setRenderingHints(AndroidGraphics2D g)
	{
		if (m_highQuality)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		else
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // TODO for Dritan: Check this
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); // TODO for Dritan: Check this

	}

	/**
	 * disable hardware accelaration , otherwise some drawing code do not work. e.g. antialiasing on drawing with Path for more information see
	 * http://developer.android.com/guide/topics/graphics/hardware-accel.html
	 */
	public void disableHardwareAcceleration()
	{
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas)
	{
		Log.d("PERFORMANCE", "--------------------------------------------------------------");
		log("ALL");
		super.onDraw(canvas);
		
		log("creating-AndroidGraphics2D");
		int width = getWidth();
		int height = getHeight();
				
		if(m_offscreen_bitmap == null)
		{
			m_offscreen_bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		}
		if (m_offscreen == null)
		{
			m_offscreen = new Canvas(m_offscreen_bitmap);
			damageReport();
		}
		AndroidGraphics2D g2D = new AndroidGraphics2D(canvas, this); 
		AndroidGraphics2D buf_g2D = new AndroidGraphics2D ( m_offscreen, this);
		
		log("creating-AndroidGraphics2D");
		
		// Why not fire a pre-paint event here?
		// Pre-paint events are fired by the clearRegion method
		if(isHardwareAccelerated())
			Log.d("PERFORMANCE", "Harfware accelerated");
		else
			Log.d("PERFORMANCE", "Harfware acceleration is disabled");
		
		// paint the visualization
		m_offscreen.save();
		paintDisplay(buf_g2D, new Dimension(width, height));

		paintBufferToScreen(g2D); 
		m_offscreen.restore();
		
		// fire post-paint events to any painters
		firePostPaint(g2D);
		
		buf_g2D.dispose();
		// compute frame rate
		nframes++;
		if (mark < 0)
		{
			mark = System.currentTimeMillis();
			nframes = 0;
		} else if (nframes == sampleInterval)
		{
			long t = System.currentTimeMillis();
			frameRate = (1000.0 * nframes) / (t - mark);
			mark = t;
			nframes = 0;
		}
		Log.d("PERFORMANCE", "framerate: " + nframes + " - " + frameRate );
		log("ALL");
		log("ALL-START");
	}

	/**
	 * set borders of display
	 * 
	 * @author Dritan
	 * @param top
	 * @param left
	 * @param bottom
	 * @param right
	 */
	public void setBorders(int top, int left, int bottom, int right)
	{
		this.m_insets.set(top, left, bottom, right);
	}

	/**
	 * Renders the display within the given graphics context and size bounds.
	 * 
	 * @param g2D
	 *            the <code>Graphics2D</code> context to use for rendering
	 * @param d
	 *            the rendering width and height of the Display
	 */
	public void paintDisplay(AndroidGraphics2D g2D, Dimension d)
	{
		// if double-locking *ALWAYS* lock on the visualization first
		log("synchMVis");
		Log.d("PERFORMANCE", "synchMVis Aquire");
		synchronized (m_vis)
		{
			log("synchMVis");
			synchronized (this)
			{
				Log.d("PERFORMANCE", "synchMVis GOT");
				if (m_clip.isEmpty())
					return; // no damage, no render
				
				log("transform");
				// map the screen bounds to absolute coords
				m_screen.setClip(0, 0, d.width + 1, d.height + 1);
				m_screen.transform(m_itransform);
				log("transform");
				// compute the approximate size of an "absolute pixel"
				// values too large are OK (though cause unnecessary rendering)
				// values too small will cause incorrect rendering
				log("default1");
				double pixel = 1.0 + 1.0 / getScale();

				if (m_damageRedraw)
				{
					if (m_clip.isInvalid())
					{
						// if clip is invalid, we clip to the entire screen
						m_clip.setClip(m_screen);
					} else
					{
						// otherwise intersect damaged region with display
						// bounds
						m_clip.intersection(m_screen);
					}

					// expand the clip by the extra pixel margin
					m_clip.expand(pixel);
					
					// set the transform, rendering keys, etc
					prepareGraphics(g2D);

					// now set the actual rendering clip
					m_rclip.setFrameFromDiagonal(m_clip.getMinX(), m_clip.getMinY(), m_clip.getMaxX(), m_clip.getMaxY());
					g2D.setClip(m_rclip);

					// finally, we want to clear the region we'll redraw. we
					// clear
					// a slightly larger area than the clip. if we don't do
					// this,
					// we sometimes get rendering artifacts, possibly due to
					// scaling mismatches in the Java2D implementation
					m_rclip.setFrameFromDiagonal(m_clip.getMinX() - pixel, m_clip.getMinY() - pixel, m_clip.getMaxX() + pixel, m_clip.getMaxY() + pixel);

				} else
				{
					// set the background region to clear
					m_rclip.setFrame(m_screen.getMinX(), m_screen.getMinY(), m_screen.getWidth(), m_screen.getHeight());

					// set the item clip to the current screen
					m_clip.setClip(m_screen);

					// set the transform, rendering keys, etc
					prepareGraphics(g2D);
				}

				// now clear the region
				clearRegion(g2D, m_rclip);

				// -- render ----------------------------
				// the actual rendering loop

				// copy current item bounds into m_rclip, reset item bounds
				getItemBounds(m_rclip);
				m_bounds.reset();

				// fill the rendering and picking queues
				m_queue.clear(); // clear the queue
				log( "default1" );
				log("prepareItems");
				Iterator<VisualItem> items = m_vis.items(m_predicate);
				for (m_visibleCount = 0; items.hasNext(); ++m_visibleCount)
				{
					VisualItem item =  items.next();
					Rectangle2D bounds = item.getBounds();
					m_bounds.union(bounds); // add to item bounds

					if (m_clip.intersects(bounds, pixel))
						m_queue.addToRenderQueue(item);
					if (item.isInteractive())
						m_queue.addToPickingQueue(item);
				}
				log("prepareItems");
				log("sortItems");
				// sort the rendering queue
				m_queue.sortRenderQueue();
				log("sortItems");
				log("renderItems");

				// render each visual item
				ArrayList<Thread> threads = new ArrayList<Thread>();
				
				Thread t ;
				for (int i = 0; i < numberThreads; i++)
				{
					AndroidGraphics2D gThread = new AndroidGraphics2D(g2D.getCanvas(), this);
					setRenderingHints(gThread);
					t = new RenderThread( gThread, i );
					t.start();
					threads.add(t);
				}
				
				for (Thread thread : threads) {
					try
					{
						thread.join();
					} catch (InterruptedException e)
					{
						Log.d("PERFORMANCE", "*********error********" + e.getMessage() );
					}
				}				
				log("renderItems");
				
				// no more damage so reset the clip
				
				log("checkBounds");
				if (m_damageRedraw)
					m_clip.reset();

				
				// fire bounds change, if appropriate
				checkItemBoundsChanged(m_rclip);
				log("checkBounds");
			}
		} // end synchronized block
	}

	/**
	 * Immediately render the given VisualItem to the screen. This method bypasses the Display's offscreen buffer. TODO for Dritan: see if it is neccessary to implement this method
	 * 
	 * @param item
	 *            the VisualItem to render immediately
	 */
	// public void renderImmediate(VisualItem item) {
	// this.getC
	// AndroidGraphics2D g2D = (AndroidGraphics2D) this.getGraphics();
	// prepareGraphics(g2D);
	// item.render(g2D);
	// }

	/**
	 * Paints the graph to the provided graphics context, for output to a printer. This method does not double buffer the painting, in order to provide the maximum print quality. TODO for Dritan: see
	 * if it is neccesary to implement this method <b>This method may not be working correctly, and will be repaired at a later date.</b>
	 * 
	 * @param g
	 *            the printer graphics context.
	 *  @incomplete
	 */
	protected void printComponent(Graphics g)
	{
		// boolean wasHighQuality = m_highQuality;
		// try {
		// // Set the quality to high for the duration of the printing.
		// m_highQuality = true;
		// // Paint directly to the print graphics context.
		// paintDisplay((AndroidGraphics2D) g, getSize());
		// } finally {
		// // Reset the quality to the state it was in before printing.
		// m_highQuality = wasHighQuality;
		// }
	}

	/**
	 * Clears the specified region of the display in the display's offscreen buffer.
	 * 
	 * @incomplete
	 */
	protected void clearRegion(AndroidGraphics2D g, Rectangle2D r)
	{
		ColorDrawable bg = (ColorDrawable) getBackground();
		int color = bg.getColor();

		g.setColor(new Color(color));
		g.fill(r);
		// fire pre-paint events to any painters
		firePrePaint(g);
	}

	// ------------------------------------------------------------------------
	// Transformations

	/**
	 * Set the 2D AffineTransform (e.g., scale, shear, pan, rotate) used by this display before rendering visual items. The provided transform must be invertible, otherwise an expection will be
	 * thrown. For simple panning and zooming transforms, you can instead use the provided pan() and zoom() methods.
	 */
	public synchronized void setTransform(AffineTransform transform) throws NoninvertibleTransformException
	{
		damageReport();
		m_transform = transform;
		m_itransform = m_transform.createInverse();
	}

	/**
	 * Returns a reference to the AffineTransformation used by this Display. Changes made to this reference WILL corrupt the state of this display. Use setTransform() to safely update the transform
	 * state.
	 * 
	 * @return the AffineTransform
	 */
	public AffineTransform getTransform()
	{
		return m_transform;
	}

	/**
	 * Returns a reference to the inverse of the AffineTransformation used by this display. Direct changes made to this reference WILL corrupt the state of this display.
	 * 
	 * @return the inverse AffineTransform
	 */
	public AffineTransform getInverseTransform()
	{
		return m_itransform;
	}

	/**
	 * Gets the absolute co-ordinate corresponding to the given screen co-ordinate.
	 * 
	 * @param screen
	 *            the screen co-ordinate to transform
	 * @param abs
	 *            a reference to put the result in. If this is the same object as the screen co-ordinate, it will be overridden safely. If this value is null, a new Point2D instance will be created
	 *            and returned.
	 * @return the point in absolute co-ordinates
	 */
	public Point2D getAbsoluteCoordinate(Point2D screen, Point2D abs)
	{
		return m_itransform.transform(screen, abs);
	}

	/**
	 * Returns the current scale (zoom) value.
	 * 
	 * @return the current scale. This is the scaling factor along the x-dimension, so be careful when using this value in rare non-uniform scaling cases.
	 */
	public double getScale()
	{
		return m_transform.getScaleX();
	}

	/**
	 * Returns the x-coordinate of the top-left of the display, in absolute (item-space) co-ordinates.
	 * 
	 * @return the x co-ord of the top-left corner, in absolute coordinates
	 */
	public double getDisplayX()
	{
		return -m_transform.getTranslateX();
	}

	/**
	 * Returns the y-coordinate of the top-left of the display, in absolute (item-space) co-ordinates.
	 * 
	 * @return the y co-ord of the top-left corner, in absolute coordinates
	 */
	public double getDisplayY()
	{
		return -m_transform.getTranslateY();
	}

	/**
	 * Pans the view provided by this display in screen coordinates.
	 * 
	 * @param dx
	 *            the amount to pan along the x-dimension, in pixel units
	 * @param dy
	 *            the amount to pan along the y-dimension, in pixel units
	 */
	public synchronized void pan(double dx, double dy)
	{
		dx *= -1;
		dy *= -1;
		m_tmpPoint.setLocation(dx, dy);
		m_itransform.transform(m_tmpPoint, m_tmpPoint);
		double panx = m_tmpPoint.getX();
		double pany = m_tmpPoint.getY();
		m_tmpPoint.setLocation(0, 0);
		m_itransform.transform(m_tmpPoint, m_tmpPoint);
		panx -= m_tmpPoint.getX();
		pany -= m_tmpPoint.getY();
		panAbs(panx, pany);
	}

	/**
	 * Pans the view provided by this display in absolute (i.e. item-space) coordinates.
	 * 
	 * @param dx
	 *            the amount to pan along the x-dimension, in absolute co-ords
	 * @param dy
	 *            the amount to pan along the y-dimension, in absolute co-ords
	 */
	public synchronized void panAbs(double dx, double dy)
	{
		damageReport();
		m_transform.translate(dx, dy);
		try
		{
			m_itransform = m_transform.createInverse();
		} catch (Exception e)
		{ /* will never happen here */
		}
	}

	/**
	 * Pans the display view to center on the provided point in screen (pixel) coordinates.
	 * 
	 * @param p
	 *            the point to center on, in screen co-ords
	 */
	public synchronized void panTo(Point2D p)
	{
		m_itransform.transform(p, m_tmpPoint);
		panToAbs(m_tmpPoint);
	}

	/**
	 * Pans the display view to center on the provided point in absolute (i.e. item-space) coordinates.
	 * 
	 * @param p
	 *            the point to center on, in absolute co-ords
	 */
	public synchronized void panToAbs(Point2D p)
	{
		double sx = m_transform.getScaleX();
		double sy = m_transform.getScaleY();
		double x = p.getX();
		x = (Double.isNaN(x) ? 0 : x);
		double y = p.getY();
		y = (Double.isNaN(y) ? 0 : y);
		x = getWidth() / (2 * sx) - x;
		y = getHeight() / (2 * sy) - y;

		double dx = x - (m_transform.getTranslateX() / sx);
		double dy = y - (m_transform.getTranslateY() / sy);

		damageReport();
		m_transform.translate(dx, dy);
		try
		{
			m_itransform = m_transform.createInverse();
		} catch (Exception e)
		{ /* will never happen here */
		}
	}

	/**
	 * Zooms the view provided by this display by the given scale, anchoring the zoom at the specified point in screen coordinates.
	 * 
	 * @param p
	 *            the anchor point for the zoom, in screen coordinates
	 * @param scale
	 *            the amount to zoom by
	 */
	public synchronized void zoom(final Point2D p, double scale)
	{
		m_itransform.transform(p, m_tmpPoint);
		zoomAbs(m_tmpPoint, scale);
	}

	/**
	 * Zooms the view provided by this display by the given scale, anchoring the zoom at the specified point in absolute coordinates.
	 * 
	 * @param p
	 *            the anchor point for the zoom, in absolute (i.e. item-space) co-ordinates
	 * @param scale
	 *            the amount to zoom by
	 */
	public synchronized void zoomAbs(final Point2D p, double scale)
	{
		double zx = p.getX(), zy = p.getY();
		damageReport();
		m_transform.translate(zx, zy);
		m_transform.scale(scale, scale);
		m_transform.translate(-zx, -zy);
		try
		{
			m_itransform = m_transform.createInverse();
		} catch (Exception e)
		{ /* will never happen here */
		}
	}

	/**
	 * Rotates the view provided by this display by the given angle in radians, anchoring the rotation at the specified point in screen coordinates.
	 * 
	 * @param p
	 *            the anchor point for the rotation, in screen coordinates
	 * @param theta
	 *            the angle to rotate by, in radians
	 */
	public synchronized void rotate(final Point2D p, double theta)
	{
		m_itransform.transform(p, m_tmpPoint);
		rotateAbs(m_tmpPoint, theta);
	}

	/**
	 * Rotates the view provided by this display by the given angle in radians, anchoring the rotation at the specified point in absolute coordinates.
	 * 
	 * @param p
	 *            the anchor point for the rotation, in absolute (i.e. item-space) co-ordinates
	 * @param theta
	 *            the angle to rotation by, in radians
	 */
	public synchronized void rotateAbs(final Point2D p, double theta)
	{
		double zx = p.getX(), zy = p.getY();
		damageReport();
		m_transform.translate(zx, zy);
		m_transform.rotate(theta);
		m_transform.translate(-zx, -zy);
		try
		{
			m_itransform = m_transform.createInverse();
		} catch (Exception e)
		{ /* will never happen here */
		}
	}

	/**
	 * Animate a pan along the specified distance in screen (pixel) co-ordinates using the provided duration.
	 * 
	 * @param dx
	 *            the amount to pan along the x-dimension, in pixel units
	 * @param dy
	 *            the amount to pan along the y-dimension, in pixel units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePan(double dx, double dy, long duration)
	{
		double panx = dx / m_transform.getScaleX();
		double pany = dy / m_transform.getScaleY();
		animatePanAbs(panx, pany, duration);
	}

	/**
	 * Animate a pan along the specified distance in absolute (item-space) co-ordinates using the provided duration.
	 * 
	 * @param dx
	 *            the amount to pan along the x-dimension, in absolute co-ords
	 * @param dy
	 *            the amount to pan along the y-dimension, in absolute co-ords
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAbs(double dx, double dy, long duration)
	{
		m_transact.pan(dx, dy, duration);
	}

	/**
	 * Animate a pan to the specified location in screen (pixel) co-ordinates using the provided duration.
	 * 
	 * @param p
	 *            the point to pan to in screen (pixel) units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanTo(Point2D p, long duration)
	{
		Point2D pp = new Point2D.Double();
		m_itransform.transform(p, pp);
		animatePanToAbs(pp, duration);
	}

	/**
	 * Animate a pan to the specified location in absolute (item-space) co-ordinates using the provided duration.
	 * 
	 * @param p
	 *            the point to pan to in absolute (item-space) units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanToAbs(Point2D p, long duration)
	{
		m_tmpPoint.setLocation(0, 0);
		m_itransform.transform(m_tmpPoint, m_tmpPoint);
		double x = p.getX();
		x = (Double.isNaN(x) ? 0 : x);
		double y = p.getY();
		y = (Double.isNaN(y) ? 0 : y);
		double w = getWidth() / (2 * m_transform.getScaleX());
		double h = getHeight() / (2 * m_transform.getScaleY());
		double dx = w - x + m_tmpPoint.getX();
		double dy = h - y + m_tmpPoint.getY();
		animatePanAbs(dx, dy, duration);
	}

	/**
	 * Animate a zoom centered on a given location in screen (pixel) co-ordinates by the given scale using the provided duration.
	 * 
	 * @param p
	 *            the point to center on in screen (pixel) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animateZoom(final Point2D p, double scale, long duration)
	{
		Point2D pp = new Point2D.Double();
		m_itransform.transform(p, pp);
		animateZoomAbs(pp, scale, duration);
	}

	/**
	 * Animate a zoom centered on a given location in absolute (item-space) co-ordinates by the given scale using the provided duration.
	 * 
	 * @param p
	 *            the point to center on in absolute (item-space) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animateZoomAbs(final Point2D p, double scale, long duration)
	{
		m_transact.zoom(p, scale, duration);
	}

	/**
	 * Animate a pan to the specified location in screen (pixel) co-ordinates and zoom to the given scale using the provided duration.
	 * 
	 * @param p
	 *            the point to center on in screen (pixel) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAndZoomTo(final Point2D p, double scale, long duration)
	{
		Point2D pp = new Point2D.Double();
		m_itransform.transform(p, pp);
		animatePanAndZoomToAbs(pp, scale, duration);
	}

	/**
	 * Animate a pan to the specified location in absolute (item-space) co-ordinates and zoom to the given scale using the provided duration.
	 * 
	 * @param p
	 *            the point to center on in absolute (item-space) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAndZoomToAbs(final Point2D p, double scale, long duration)
	{
		m_transact.panAndZoom(p, scale, duration);
	}

	/**
	 * Indicates if a view transformation is currently underway.
	 * 
	 * @return true if a transform is in progress, false otherwise
	 */
	public boolean isTranformInProgress()
	{
		return m_transact.isRunning();
	}

	/**
	 * Activity for conducting animated view transformations.
	 */
	private class TransformActivity extends PActivity
	{
		// TODO: clean this up to be more general...
		// TODO: change mechanism so that multiple transform activities can be
		// running at once?

		private double[] src, dst;
		private AffineTransform m_at;

		public TransformActivity()
		{
			super(2000, 20, 0);
			src = new double[6];
			dst = new double[6];
			m_at = new AffineTransform();
			setPacingFunction(new SlowInSlowOutPacer());
		}

		private AffineTransform getTransform()
		{
			if (this.isScheduled())
				m_at.setTransform(dst[0], dst[1], dst[2], dst[3], dst[4], dst[5]);
			else
				m_at.setTransform(m_transform);
			return m_at;
		}

		public void panAndZoom(final Point2D p, double scale, long duration)
		{
			AffineTransform at = getTransform();
			this.cancel();
			setDuration(duration);

			m_tmpPoint.setLocation(0, 0);
			m_itransform.transform(m_tmpPoint, m_tmpPoint);
			double x = p.getX();
			x = (Double.isNaN(x) ? 0 : x);
			double y = p.getY();
			y = (Double.isNaN(y) ? 0 : y);
			double w = getWidth() / (2 * m_transform.getScaleX());
			double h = getHeight() / (2 * m_transform.getScaleY());
			double dx = w - x + m_tmpPoint.getX();
			double dy = h - y + m_tmpPoint.getY();
			at.translate(dx, dy);

			at.translate(p.getX(), p.getY());
			at.scale(scale, scale);
			at.translate(-p.getX(), -p.getY());

			at.getMatrix(dst);
			m_transform.getMatrix(src);
			this.run();
		}

		public void pan(double dx, double dy, long duration)
		{
			AffineTransform at = getTransform();
			this.cancel();
			setDuration(duration);
			at.translate(dx, dy);
			at.getMatrix(dst);
			m_transform.getMatrix(src);
			this.run();
		}

		public void zoom(final Point2D p, double scale, long duration)
		{
			AffineTransform at = getTransform();
			this.cancel();
			setDuration(duration);
			double zx = p.getX(), zy = p.getY();
			at.translate(zx, zy);
			at.scale(scale, scale);
			at.translate(-zx, -zy);
			at.getMatrix(dst);
			m_transform.getMatrix(src);
			this.run();
		}

		protected void run(long elapsedTime)
		{
			double f = getPace(elapsedTime);
			damageReport();
			m_transform.setTransform(src[0] + f * (dst[0] - src[0]), src[1] + f * (dst[1] - src[1]), src[2] + f * (dst[2] - src[2]), src[3] + f * (dst[3] - src[3]), src[4] + f * (dst[4] - src[4]), src[5]
					+ f * (dst[5] - src[5]));
			try
			{
				m_itransform = m_transform.createInverse();
			} catch (Exception e)
			{ /* won't happen */
			}
			// repaint();
			postInvalidate(); // TODO for Dritan: see if this is a correct
								// replacement for the repaint method
		}
	} // end of inner class TransformActivity

	// ------------------------------------------------------------------------
	// Paint Listeners

	/**
	 * Add a PaintListener to this Display to receive notifications about paint events.
	 * 
	 * @param pl
	 *            the {@link prefuse.util.display.PaintListener} to add
	 */
	public void addPaintListener(PaintListener pl)
	{
		if (m_painters == null)
			m_painters = new CopyOnWriteArrayList<PaintListener>();
		m_painters.add(pl);
	}

	/**
	 * Remove a PaintListener from this Display.
	 * 
	 * @param pl
	 *            the {@link prefuse.util.display.PaintListener} to remove
	 */
	public void removePaintListener(PaintListener pl)
	{
		m_painters.remove(pl);
	}

	/**
	 * Fires a pre-paint notification to PaintListeners.
	 * 
	 * @param g
	 *            the current graphics context
	 */
	protected void firePrePaint(AndroidGraphics2D g)
	{
		if (m_painters != null && m_painters.size() > 0)
		{
			Object[] lstnrs = m_painters.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				try
				{
					((PaintListener) lstnrs[i]).prePaint(this, g);
				} catch (Exception e)
				{
					s_logger.warning("Exception thrown by PaintListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	/**
	 * Fires a post-paint notification to PaintListeners.
	 * 
	 * @param g
	 *            the current graphics context
	 */
	protected void firePostPaint(AndroidGraphics2D g)
	{
		if (m_painters != null && m_painters.size() > 0)
		{
			Object[] lstnrs = m_painters.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				try
				{
					((PaintListener) lstnrs[i]).postPaint(this, g);
				} catch (Exception e)
				{
					s_logger.warning("Exception thrown by PaintListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	// Item Bounds Listeners

	/**
	 * Add an ItemBoundsListener to receive notifications when the bounds occupied by the VisualItems in this Display change.
	 * 
	 * @param ibl
	 *            the {@link prefuse.util.display.ItemBoundsListener} to add
	 */
	public void addItemBoundsListener(ItemBoundsListener ibl)
	{
		if (m_bounders == null)
			m_bounders = new CopyOnWriteArrayList<ItemBoundsListener>();
		m_bounders.add(ibl);
	}

	/**
	 * Remove an ItemBoundsListener to receive notifications when the bounds occupied by the VisualItems in this Display change.
	 * 
	 * @param ibl
	 *            the {@link prefuse.util.display.ItemBoundsListener} to remove
	 */
	public void removeItemBoundsListener(ItemBoundsListener ibl)
	{
		m_bounders.remove(ibl);
	}

	/**
	 * Check if the item bounds has changed, and if so, fire a notification.
	 * 
	 * @param prev
	 *            the previous item bounds of the Display
	 */
	protected void checkItemBoundsChanged(Rectangle2D prev)
	{
		if (m_bounds.equals(prev))
			return; // nothing to do

		if (m_bounders != null && m_bounders.size() > 0)
		{
			Object[] lstnrs = m_bounders.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				try
				{
					((ItemBoundsListener) lstnrs[i]).itemBoundsChanged(this);
				} catch (Exception e)
				{
					s_logger.warning("Exception thrown by ItemBoundsListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	// Control Listeners

	/**
	 * Adds a ControlListener to receive all input events on VisualItems.
	 * 
	 * @param cl
	 *            the listener to add.
	 */
	public void addControlListener(Control cl)
	{
		m_controls.add(cl);
	}

	/**
	 * Removes a registered ControlListener.
	 * 
	 * @param cl
	 *            the listener to remove.
	 */
	public void removeControlListener(Control cl)
	{
		m_controls.remove(cl);
	}

	/**
	 * Returns the VisualItem located at the given point.
	 * 
	 * @param p
	 *            the Point at which to look
	 * @return the VisualItem located at the given point, if any
	 */
	public synchronized VisualItem findItem(Point p)
	{
		// transform mouse point from screen space to item space
		Point2D p2 = (m_itransform == null ? p : m_itransform.transform(p, m_tmpPoint));
		// ensure that the picking queue has been z-sorted
		if (!m_queue.psorted)
			m_queue.sortPickingQueue();
		// walk queue from front to back looking for hits
		for (int i = m_queue.psize; --i >= 0;)
		{
			VisualItem vi = m_queue.pitems[i];
			if (!vi.isValid())
				continue; // in case tuple went invalid
			Renderer r = vi.getRenderer();
			if (r != null && vi.isInteractive() && r.locatePoint(p2, vi))
			{
				return vi;
			}
		}
		return null;
	}

	private boolean validityCheck()
	{
		if (activeItem.isValid())
			return true;
		activeItem = null;
		return false;
	}

	private void fireItemExited(VisualItem item, MotionEvent event)
	{
		if (item.isValid())
			item.setHover(false);
		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			if (ctrl.isEnabled())
				try
				{
					ctrl.itemExited(item, event);
				} catch (Exception ex)
				{
					s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
				}
		}
	}

	/**
	 * TODO for Dritan: Check if the implementation is correct. See if and how is it possible to catch Events over different views
	 * @param event
	 * @return
	 */
	private boolean isOffComponent(MotionEvent event)
	{
		float x = event.getX(), y = event.getY();
		return (x < 0 || x > getWidth() || y < 0 || y > getHeight());
	}


	private void fireItemMoved(VisualItem item, MotionEvent event)
	{
		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			if (ctrl.isEnabled())
				try
				{
					ctrl.itemMoved(item, event);
				} catch (Exception ex)
				{
					s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
				}
		}
	}

	private void fireItemClicked(VisualItem item, MotionEvent event)
	{
		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			if (ctrl.isEnabled())
				try
				{
					ctrl.itemClicked(item, event);
				} catch (Exception ex)
				{
					s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
				}
		}
	}

	private void fireItemPressed(VisualItem item, MotionEvent event)
	{
		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			if (ctrl.isEnabled())
				try
				{
					ctrl.itemPressed(item, event);
				} catch (Exception ex)
				{
					s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
				}
		}
	}

	private void fireItemReleased(VisualItem item, MotionEvent event)
	{
		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			if (ctrl.isEnabled())
				try
				{
					ctrl.itemReleased(item, event);
				} catch (Exception ex)
				{
					s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
				}
		}
	}

	// ------------------------------------------------------------------------
	// Text Editing

	/**
	 * Returns the TextComponent used for on-screen text editing.
	 * 
	 * @return the TextComponent used for text editing
	 */
	public EditText getTextEditor()
	{
		return m_editor;
	}

	/**
	 * Sets the TextComponent used for on-screen text editing.
	 * 
	 * @param tc
	 *            the TextComponent to use for text editing
	 */
	public void setTextEditor(EditText tc)
	{
		m_editor = tc;
	}

	/**
	 * Edit text for the given VisualItem and attribute. Presents a text editing widget spaning the item's bounding box. Use stopEditing() to hide the text widget. When stopEditing() is called, the
	 * data field will automatically be updated with the VisualItem.
	 * 
	 * @param item
	 *            the VisualItem to edit
	 * @param attribute
	 *            the attribute to edit
	 */
	public void editText(VisualItem item, String attribute)
	{
		if (m_editing)
		{
			stopEditing();
		}
		Rectangle2D b = item.getBounds();
		Rectangle r = m_transform.createTransformedShape(b).getBounds();

		// Font f = getFont();
		// int size = (int) Math.round(f.getSize() * m_transform.getScaleX());
		// Font nf = new Font(f.getFontName(), f.getStyle(), size);
		// m_editor.setFont(nf);

		editText(item, attribute, r);
	}

	/**
	 * Edit text for the given VisualItem and field. Presents a text editing widget spaning the given bounding box. Use stopEditing() to hide the text widget. When stopEditing() is called, the field
	 * will automatically be updated with the VisualItem.
	 * 
	 * @param item
	 *            the VisualItem to edit
	 * @param attribute
	 *            the attribute to edit
	 * @param r
	 *            Rectangle representing the desired bounding box of the text editing widget
	 */
	public void editText(VisualItem item, String attribute, Rectangle r)
	{
		if (m_editing)
		{
			stopEditing();
		}
		String txt = item.getString(attribute);
		m_editItem = item;
		m_editAttribute = attribute;
		Color tc = ColorLib.getColor(item.getTextColor());
		Color fc = ColorLib.getColor(item.getFillColor());
		m_editor.setTextColor(tc.getAndroidColorRepresentation());
		m_editor.setBackgroundColor(fc.getAndroidColorRepresentation());
		editText(txt, r);
	}

	/**
	 * Show a text editing widget containing the given text and spanning the specified bounding box. Use stopEditing() to hide the text widget. Use the method calls getTextEditor().getText() to get
	 * the resulting edited text. TODO for Dritan: currently this method can not be completed because it is not possible to set the absolute position like in swing over the method setBounds. => => it
	 * is possible to call setFrame, but the values are relative to layout in which the EditText is
	 * 
	 * @param txt
	 *            the text string to display in the text widget
	 * @param r
	 *            Rectangle representing the desired bounding box of the text
	 * 
	 * @incomplete
	 * @deprecated editing widget
	 */
	public void editText(String txt, Rectangle r)
	{
		if (m_editing)
		{
			stopEditing();
		}
		// int width = getWidth();
		// int height = getHeight();
		m_editing = true;
		// m_editor.setBounds(r.x,r.y,r.width,r.height);
		m_editor.setWidth(r.width);
		m_editor.setHeight(r.height);
		m_editor.setLeft(r.x); // TODO for Dritan: I'm not sure this is how it
								// should work
		m_editor.setBottom(r.y); // TODO for Dritan: I'm not sure this is how it
									// should work
		m_editor.setText(txt);
		m_editor.setVisibility(View.VISIBLE);
		m_editor.setSelection(txt.length());
		m_editor.requestFocus();
	}

	/**
	 * Stops text editing on the display, hiding the text editing widget. If the text editor was associated with a specific VisualItem (ie one of the editText() methods which include a VisualItem as
	 * an argument was called), the item is updated with the edited text.
	 */
	public void stopEditing()
	{
		m_editor.setVisibility(View.GONE);
		if (m_editItem != null)
		{
			Editable txt = m_editor.getText();
			m_editItem.set(m_editAttribute, txt.toString());
			m_editItem = null;
			m_editAttribute = null;
		}
		m_editing = false;
	}

	public Insets getInsets(Insets insets)
	{
		insets.set(this.m_insets.top, this.m_insets.left, this.m_insets.bottom, this.m_insets.right);
		return insets;
	}

	public Insets getInsets()
	{
		return m_insets;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		/**
		 * set to all controlers the display
		 */

		Object[] lstnrs = m_controls.getArray();
		for (int i = 0; i < lstnrs.length; ++i)
		{
			Control ctrl = (Control) lstnrs[i];
			ctrl.setDisplay(this);
		}
		int maskedAction = event.getActionMasked();

		switch (maskedAction)
		{

			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				Point p = new Point((int) event.getX(), (int) event.getY());
				activeItem = findItem(p);
				if (activeItem != null)
				{
					inputEC.fireItemEntered(activeItem, event);
				} else
				{
					inputEC.fireTouchDown(event);
				}
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{ // a pointer was moved
				// TODO use data
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
			{
				// TODO use data
				break;
			}
		}

		boolean retVal = mScaleGestureDetector.onTouchEvent(event);
		retVal = mGestureDetector.onTouchEvent(event) || retVal;
		retVal = retVal || super.onTouchEvent(event);
		return retVal;
	}

	/**
	 * Captures all mouse and key events on the display, detects relevant VisualItems, and informs ControlListeners.
	 */
	public class InputEventCapturer
	{
		public void fireItemEntered(VisualItem item, MotionEvent event)
		{
			item.setHover(true);
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.itemEntered(item, event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireItemDoubleTaped(VisualItem item, MotionEvent event)
		{
			item.setHover(true);
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.itemDoubleTaped(item, event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireItemLongPres(VisualItem item, MotionEvent event)
		{
			item.setHover(true);
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.itemLongPress(item, event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		private void fireItemDragged(VisualItem item, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.itemDragged(item, e1, e2, distanceX, distanceY);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		/* NON ITEM EVENTS */

		public void fireScale(ScaleGestureDetector scaleGestureDetector)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onScale(scaleGestureDetector);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireLongPress(MotionEvent event)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onLongPress(event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireSingleTapUp(MotionEvent event)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onSingleTapUp(event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireDoubleTap(MotionEvent event)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onDoubleTap(event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireTouchDown(MotionEvent event)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onTouchDown(event);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}

		public void fireScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			Object[] lstnrs = m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i)
			{
				Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled())
					try
					{
						ctrl.onScroll(e1, e2, distanceX, distanceY);
					} catch (Exception ex)
					{
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
			}
		}
	}

	/**
	 * The gesture listener, used for handling simple gestures such as double touches, scrolls, and flings.
	 */
	private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener()
	{
		@Override
		public boolean onDown(MotionEvent event)
		{
			// TODO for Dritan: Implement this
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent event)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "ondoubletap");
				// check if we've gone over any item
				Point p = new Point((int) event.getX(), (int) event.getY());
				activeItem = findItem(p);
				if (activeItem != null)
				{
					inputEC.fireItemDoubleTaped(activeItem, event);
				} else
				{
					inputEC.fireDoubleTap(event);
				}
				return true;
			}
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "onSingleTapConfirmed");
				// check if we've gone over any item
				Point p = new Point((int) event.getX(), (int) event.getY());
				activeItem = findItem(p);
				if (activeItem != null)
				{
					inputEC.fireItemEntered(activeItem, event);
				} else
				{
					inputEC.fireSingleTapUp(event);
				}
				return true;
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "onSingleTapUp");
				// check if we've gone over any item
				Point p = new Point((int) event.getX(), (int) event.getY());
				activeItem = findItem(p);
				if (activeItem != null)
				{
					inputEC.fireItemEntered(activeItem, event);
				} else
				{
					inputEC.fireSingleTapUp(event);
				}
				return true;
			}
		}

		@Override
		public void onLongPress(MotionEvent event)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "onLongPress");
				// check if we've gone over any item
				Point p = new Point((int) event.getX(), (int) event.getY());
				activeItem = findItem(p);
				if (activeItem != null)
				{
					inputEC.fireItemEntered(activeItem, event);
					inputEC.fireItemLongPres(activeItem, event);
				} else
				{
					inputEC.fireLongPress(event);
				}
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "onScroll");
				Point p = new Point((int) e1.getX(), (int) e2.getY());
				activeItem = findItem(p);
				if (activeItem != null)
					inputEC.fireItemDragged(activeItem, e1, e2, distanceX, distanceY);
				else
					inputEC.fireScroll(e1, e2, distanceX, distanceY);
				return true;
			}
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			// TODO for Dritan: Implement this
			return true;
		}

	};

	/**
	 * The scale listener, used for handling multi-finger scale gestures.
	 */
	private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener()
	{
		@Override
		public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector)
		{
			// TODO for Dritan: Implement this
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector scaleGestureDetector)
		{
			synchronized (m_vis)
			{
				Log.d("PERFORMANCE", "onScale");
				inputEC.fireScale(scaleGestureDetector);
				return true;
			}
		}
	};

	class RenderThread extends Thread
	{
		AndroidGraphics2D g;
		int threadNr;
		
		public RenderThread(AndroidGraphics2D g, int threadNr)
		{
			this.g = g;
			this.threadNr = threadNr;
		}

		public void setThreadNr(int threadNr)
		{
			this.threadNr = threadNr;
		}
		@Override
		public void run()
		{
			int from = threadNr * m_queue.rsize/numberThreads ;
			int to = (threadNr + 1) * m_queue.rsize/numberThreads ;
			for (int i = from; i < to; ++i)
			{
//				Log.d("PERFORMANCE", "i1: " + i);
				m_queue.ritems[i].render(g);
			}
		}		
	}
	
} // end of class Display

