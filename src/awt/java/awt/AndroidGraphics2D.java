/**
 * 
 */
package awt.java.awt;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import awt.java.awt.RenderingHints.Key;
import awt.java.awt.font.FontRenderContext;
import awt.java.awt.font.GlyphVector;
import awt.java.awt.geom.AffineTransform;
import awt.java.awt.geom.GeneralPath;
import awt.java.awt.geom.PathIterator;
import awt.java.awt.geom.Rectangle2D;
import awt.java.awt.image.BufferedImage;
import awt.java.awt.image.BufferedImageOp;
import awt.java.awt.image.ImageObserver;
import awt.java.awt.image.RenderedImage;
import awt.java.awt.image.renderable.RenderableImage;

/**
 * @author Dritan
 * 
 */
public class AndroidGraphics2D implements Graphics2D {

	protected Paint currentPaint = new Paint();
	protected Canvas canvas = null;
	protected AffineTransform afineTransform = new AffineTransform();
	protected Font font = Font.DEFAULT_FONT;
	protected AwtFontMetrics fm = new AwtFontMetrics( this.font );
	
	public AndroidGraphics2D(Canvas canvas)
	{
		this.canvas = canvas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#create(int, int, int, int)
	 */
	@Override
	public Graphics create(int x, int y, int width, int height) {
		Graphics res = create();
		res.translate(x, y);
		res.clipRect(0, 0, width, height);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawBytes(byte[], int, int, int, int)
	 */
	@Override
	public void drawBytes(byte[] bytes, int off, int len, int x, int y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawChars(char[], int, int, int, int)
	 */
	@Override
	public void drawChars(char[] chars, int off, int len, int x, int y) {
		canvas.drawText(chars, off, len, x, y, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolygon(awt.java.awt.Polygon)
	 */
	@Override
	public void drawPolygon(Polygon p) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawRect(int, int, int, int)
	 */
	@Override
	public void drawRect(int x, int y, int width, int height) {
		RectF rect = new RectF(x, y, x + width, y + height);
		this.setStroke();
		canvas.drawRect(rect, getCurrentPaint());		
	}

	public void drawRect(float x, float y, float width, float height) {
		RectF rect = new RectF(x, y, x + width, y + height);
		this.setStroke();
		canvas.drawRect(rect, getCurrentPaint());		
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillPolygon(awt.java.awt.Polygon)
	 */
	@Override
	public void fillPolygon(Polygon p) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClipBounds(awt.java.awt.Rectangle)
	 */
	@Override
	public Rectangle getClipBounds(Rectangle r) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClipRect()
	 */
	@Override
	@Deprecated
	public Rectangle getClipRect() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFontMetrics()
	 */
	@Override
	public AwtFontMetrics getFontMetrics() {

		return this.fm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#hitClip(int, int, int, int)
	 */
	@Override
	public boolean hitClip(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#clearRect(int, int, int, int)
	 */
	@Override
	public void clearRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#clipRect(int, int, int, int)
	 */
	@Override
	public boolean clipRect(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	@Override
	public void copyArea(int sx, int sy, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#create()
	 */
	@Override
	public Graphics create() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#dispose()
	 */
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawArc(int, int, int, int, int, int)
	 */
	@Override
	public void drawArc(int x, int y, int width, int height, int sa, int ea) {
		RectF oval = new RectF(x, y, x + width, y + height);
		this.setStroke();
		canvas.drawArc(oval, sa, ea, false, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int,
	 * awt.java.awt.Color, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int,
	 * awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int,
	 * int, awt.java.awt.Color, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int,
	 * int, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int,
	 * int, int, int, int, int, awt.java.awt.Color,
	 * awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int,
	 * int, int, int, int, int, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawLine(int, int, int, int)
	 */
	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		this.setStroke();
		canvas.drawLine((float) x1, (float) y1, (float) x2, (float) y2,
				getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawOval(int, int, int, int)
	 */
	@Override
	public void drawOval(int x, int y, int width, int height) {
		RectF oval = new RectF(x, y, x + width, y + height);
		setStroke();
		canvas.drawOval(oval, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolygon(int[], int[], int)
	 */
	@Override
	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolyline(int[], int[], int)
	 */
	@Override
	public void drawPolyline(int[] xpoints, int[] ypoints, int npoints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc) FIXME Dritan: this method is not programmed proper. please
	 * invest more time to make it right
	 * 
	 * @see awt.java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		RectF rect = new RectF(x, y, x + width, y + height);
		float r = height / 2 + (width * width) / (8 * height);
		double q = 1; // I do not know what q is see =>
						// http://mathforum.org/library/drmath/view/53027.html
		double rx = x + width - Math.sqrt(r * r - (q / 2) * (q / 2))
				* (y + height) / 2;
		double ry = y + height - Math.sqrt(r * r - (q / 2) * (q / 2))
				* (x + width) / 2;
		this.setStroke();
		canvas.drawRoundRect(rect, (float) rx, (float) ry, getCurrentPaint());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillArc(int, int, int, int, int, int)
	 */
	@Override
	public void fillArc(int x, int y, int width, int height, int sa, int ea) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillOval(int, int, int, int)
	 */
	@Override
	public void fillOval(int x, int y, int width, int height) {
		RectF oval = new RectF(x, y, x + width, y + height);
		this.setFill();
		canvas.drawOval(oval, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillPolygon(int[], int[], int)
	 */
	@Override
	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillRect(int, int, int, int)
	 */
	@Override
	public void fillRect(int x, int y, int width, int height) {
		RectF rect = new RectF(x, y, x + width, y + height);
		this.setFill();
		canvas.drawRect(rect, getCurrentPaint());		
	}
	
	public void fillRect(float x, float y, float width, float height) {
		RectF rect = new RectF(x, y, x + width, y + height);
		this.setFill();
		canvas.drawRect(rect, getCurrentPaint());		
	}

	/*
	 * (non-Javadoc) FIXME Dritan: this method is not programmed proper. please
	 * invest more time to make it right
	 * 
	 * @see awt.java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		RectF rect = new RectF(x, y, x + width, y + height);
		float r = height / 2 + (width * width) / (8 * height);
		double q = 1; // I do not know what q is see =>
						// http://mathforum.org/library/drmath/view/53027.html
		double rx = x + width - Math.sqrt(r * r - (q / 2) * (q / 2))
				* (y + height) / 2;
		double ry = y + height - Math.sqrt(r * r - (q / 2) * (q / 2))
				* (x + width) / 2;
		this.setFill();
		canvas.drawRoundRect(rect, (float) rx, (float) ry, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClip()
	 */
	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClipBounds()
	 */
	// @Override
	// public Rect getClipBounds() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getColor()
	 */
	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFont()
	 */
	@Override
	public Font getFont() {
		return this.font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFontMetrics(awt.java.awt.Font)
	 */
	@Override
	public AwtFontMetrics getFontMetrics(Font font) {
		 
		return new AwtFontMetrics(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setClip(int, int, int, int)
	 */
	@Override
	public void setClip(int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setClip(awt.java.awt.Shape)
	 */
	@Override
	public void setClip(Shape clip) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setColor(awt.java.awt.Color)
	 */
	@Override
	public void setColor(Color c) {
		this.currentPaint.setColor(c.getAndroidColorRepresentation());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setFont(awt.java.awt.Font)
	 */
	@Override
	public void setFont(Font font) {
		this.font = font;
		this.fm.setFont(font);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setPaintMode()
	 */
	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setXORMode(awt.java.awt.Color)
	 */
	@Override
	public void setXORMode(Color color) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#addRenderingHints(java.util.Map)
	 */
	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#clip(awt.java.awt.Shape)
	 */
	@Override
	public void clip(Shape s) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#draw(awt.java.awt.Shape)
	 */
	@Override
	public void draw(Shape s) {
		paintShape(s, true);
	}
	
	/**
	 * paint a shape. currently only rectangle or general path is supported as a shape.
	 * @param s				shape to be painted
	 * @param setStroke		if true, than paint in stroke mode, else paint in fill mode.
	 */
	public void paintShape( Shape s, boolean setStroke )
	{
		if(s instanceof Rectangle2D.Double)
		{
			Rectangle2D.Double r = (Rectangle2D.Double) s;
			if( setStroke )
				drawRect((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float)r.getHeight());
			else
				fillRect((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float) r.getHeight());
		}
		else
		{
			GeneralPath path = (GeneralPath) s;
			Path aPath = transformPath(path);
			if( setStroke )
				this.setStroke();
			else
				setFill();
			
			canvas.drawPath(aPath, getCurrentPaint());
		}
	}

	public Path transformPath(GeneralPath path) {
		Path aPath = new Path();
		aPath.reset();
		PathIterator it = path.getPathIterator(null);
		while (!it.isDone()) {
			float coords[] = new float[6];
			switch (it.currentSegment(coords)) {
			case PathIterator.SEG_MOVETO:
				// if (typeSize == 0) {
				aPath.moveTo(coords[0], coords[1]);
				break;
			// }
			// if (types[typeSize - 1] != PathIterator.SEG_CLOSE
			// && points[pointSize - 2] == coords[0]
			// && points[pointSize - 1] == coords[1]) {
			// break;
			// }
			// NO BREAK;
			case PathIterator.SEG_LINETO:
				aPath.lineTo(coords[0], coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				aPath.quadTo(coords[0], coords[1], coords[2], coords[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				aPath.cubicTo(coords[0], coords[1], coords[2], coords[3],
						coords[4], coords[5]);
				break;
			case PathIterator.SEG_CLOSE:
				aPath.close();
				break;
			}
			it.next();
		}
		return aPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#drawGlyphVector(awt.java.awt.font.GlyphVector,
	 * float, float)
	 */
	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawImage(awt.java.awt.image.BufferedImage,
	 * awt.java.awt.image.BufferedImageOp, int, int)
	 */
	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawImage(awt.java.awt.Image,
	 * awt.java.awt.geom.AffineTransform, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#drawRenderableImage(awt.java.awt.image.renderable
	 * .RenderableImage, awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#drawRenderedImage(awt.java.awt.image.RenderedImage
	 * , awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc) FIXME Dritan: String attributes are ignored. Fix this if
	 * there is time or if it's neccessary
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
	 * float, float)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		String myString = "";
		for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator
				.next()) {
			myString += c;
		}
		drawString(myString, x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
	 * int, int)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		drawString(iterator, (float) x, (float) y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.lang.String, float, float)
	 */
	@Override
	public void drawString(String s, float x, float y) {
		canvas.drawText(s, x, y, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.lang.String, int, int)
	 */
	@Override
	public void drawString(String str, int x, int y) {
		canvas.drawText(str, x, y, getCurrentPaint());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#fill(awt.java.awt.Shape)
	 */
	@Override
	public void fill(Shape s) {
		paintShape(s, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getBackground()
	 */
	@Override
	public Color getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getComposite()
	 */
	@Override
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getDeviceConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getFontRenderContext()
	 */
	@Override
	public FontRenderContext getFontRenderContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getPaint()
	 */
	@Override
	public PPaint getPaint() {
		// TODO Auto-generated method stub
		return null;
	}

	public Paint getCurrentPaint() {
		return this.currentPaint;
	}

	/**
	 * set the current paint only at stroke mode. i.e not fill
	 */
	public void setStroke()
	{
		this.currentPaint.setStyle(Paint.Style.STROKE);
	}

	/**
	 * set the current paint in fill mode
	 */
	public void setFill()
	{
		this.currentPaint.setStyle(Paint.Style.FILL);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#getRenderingHint(awt.java.awt.RenderingHints.Key)
	 */
	@Override
	public Object getRenderingHint(Key key) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getRenderingHints()
	 */
	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getStroke()
	 */
	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getTransform()
	 */
	@Override
	public AffineTransform getTransform() {
		
		return afineTransform;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#hit(awt.java.awt.Rectangle,
	 * awt.java.awt.Shape, boolean)
	 */
	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#rotate(double)
	 */
	@Override
	public void rotate(double theta) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#rotate(double, double, double)
	 */
	@Override
	public void rotate(double theta, double x, double y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#scale(double, double)
	 */
	@Override
	public void scale(double sx, double sy) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setBackground(awt.java.awt.Color)
	 */
	@Override
	public void setBackground(Color color) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setComposite(awt.java.awt.Composite)
	 */
	@Override
	public void setComposite(Composite comp) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setPaint(awt.java.awt.Paint)
	 */
	@Override
	public void setPaint(PPaint paint) {
		this.currentPaint.setColor(paint.getAndroidColorRepresentation());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#setRenderingHint(awt.java.awt.RenderingHints.Key,
	 * java.lang.Object)
	 */
	@Override
	public void setRenderingHint(Key key, Object value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setRenderingHints(java.util.Map)
	 */
	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setStroke(awt.java.awt.Stroke)
	 */
	@Override
	public void setStroke(Stroke s) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * awt.java.awt.Graphics2D#setTransform(awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void setTransform(AffineTransform Tx) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#shear(double, double)
	 */
	@Override
	public void shear(double shx, double shy) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#transform(awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void transform(AffineTransform Tx) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#translate(double, double)
	 */
	@Override
	public void translate(double tx, double ty) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#translate(int, int)
	 */
	@Override
	public void translate(int x, int y) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
	 */
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
	 */
	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		// TODO Auto-generated method stub

	}

}
