/*
 * Copyright 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package awt.java.awt;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Map;

import prefuse.PDisplay;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelXorXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import awt.java.awt.geom.Area;
import awt.java.awt.RenderingHints.Key;
import awt.java.awt.font.FontRenderContext;
import awt.java.awt.font.GlyphVector;
import awt.java.awt.geom.AffineTransform;
import awt.java.awt.geom.GeneralPath;
import awt.java.awt.geom.NoninvertibleTransformException;
import awt.java.awt.geom.PathIterator;
import awt.java.awt.image.BufferedImage;
import awt.java.awt.image.BufferedImageOp;
import awt.java.awt.image.ImageObserver;
import awt.java.awt.image.RenderedImage;
import awt.java.awt.image.renderable.RenderableImage;

/**
 * @author Dritan
 * 
 */
public class AndroidGraphics2D implements Graphics2D
{

	protected Paint currentPaint = new Paint();
	protected Canvas canvas = null;
	protected AffineTransform afineTransform = new AffineTransform();
	protected Font font = Font.DEFAULT_FONT;
	protected AwtFontMetrics fontMetrics = new AwtFontMetrics(this.font);
	protected Matrix currentMatrix;
	protected PDisplay display;
	protected Area mCurrClip;
	protected Color currentColor;
	protected RenderingHints currentRenderingHints;
	public final static double RAD_360 = Math.PI / 180 * 360;
//	protected Path path = new Path();
	protected ArrayList<Float> points = new ArrayList<Float>();
	public AndroidGraphics2D(Canvas canvas, PDisplay display)
	{
		this.canvas = canvas;
		this.display = display;
		currentMatrix = new Matrix();
		currentMatrix.reset();
		currentMatrix = display.getMatrix();
		Rect r = canvas.getClipBounds();
		int cl[] =
		{ -1, r.top, r.left, -2, r.top, r.right, -2, r.bottom, r.right, -2, r.bottom, r.left };
		mCurrClip = new Area(createShape(cl));
	}

	public float[] getMatrix()
	{
		float[] f = new float[9];
		display.getMatrix().getValues(f);
		return f;
	}

	/**
	 * 
	 * @return a Matrix in Android format
	 */
	public float[] getInverseMatrix()
	{
		AffineTransform af = new AffineTransform(createAWTMatrix(getMatrix()));
		try
		{
			af = af.createInverse();
		} catch (NoninvertibleTransformException e)
		{
		}
		return createMatrix(af);
	}

	public static Matrix createMatrixObj(AffineTransform Tx)
	{
		Matrix m = new Matrix();
		m.reset();
		m.setValues(createMatrix(Tx));
		return m;
	}

	public static float[] createMatrix(AffineTransform Tx)
	{
		double[] at = new double[9];
		Tx.getMatrix(at);
		float[] f = new float[at.length];
		f[0] = (float) at[0];
		f[1] = (float) at[2];
		f[2] = (float) at[4];
		f[3] = (float) at[1];
		f[4] = (float) at[3];
		f[5] = (float) at[5];
		f[6] = 0;
		f[7] = 0;
		f[8] = 1;
		return f;
	}

	private float[] createAWTMatrix(float[] matrix)
	{
		float[] at = new float[9];
		at[0] = matrix[0];
		at[1] = matrix[3];
		at[2] = matrix[1];
		at[3] = matrix[4];
		at[4] = matrix[2];
		at[5] = matrix[5];
		at[6] = 0;
		at[7] = 0;
		at[8] = 1;
		return at;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#create(int, int, int, int)
	 */
	@Override
	public Graphics create(int x, int y, int width, int height)
	{
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
	public void drawBytes(byte[] bytes, int off, int len, int x, int y)
	{
		drawString(new String(bytes, off, len), x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawChars(char[], int, int, int, int)
	 */
	@Override
	public void drawChars(char[] chars, int off, int len, int x, int y)
	{
		currentPaint.setTextSize(this.font.getSize());
		canvas.drawText(chars, off, len, x, y, currentPaint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolygon(awt.java.awt.Polygon)
	 */
	@Override
	public void drawPolygon(Polygon p)
	{
		drawPolygon(p.xpoints, p.ypoints, p.npoints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawRect(int, int, int, int)
	 */
	@Override
	public void drawRect(int x, int y, int width, int height)
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.STROKE);
		RectF rect = new RectF(x, y, x + width, y + height);
		canvas.drawRect(rect, currentPaint);
		currentPaint.setStyle(tmp);
	}

	public void drawRect(float x, float y, float width, float height)
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.STROKE);
		RectF rect = new RectF(x, y, x + width, y + height);
		canvas.drawRect(rect, currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillPolygon(awt.java.awt.Polygon)
	 */
	@Override
	public void fillPolygon(Polygon p)
	{
		fillPolygon(p.xpoints, p.ypoints, p.npoints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClipBounds(awt.java.awt.Rectangle)
	 */
	@Override
	public Rectangle getClipBounds(Rectangle r)
	{
		Shape clip = getClip();
		if (clip != null)
		{
			Rectangle b = clip.getBounds();
			r.x = b.x;
			r.y = b.y;
			r.width = b.width;
			r.height = b.height;
		}
		return r;
	}

	public Rectangle getClipBounds()
	{
		Rect r = canvas.getClipBounds();
		return new Rectangle(r.left, r.top, r.width(), r.height());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClipRect()
	 */
	@Override
	@Deprecated
	public Rectangle getClipRect()
	{
		// TODO for Dritan: check if it is necessary to implement this
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFontMetrics()
	 */
	@Override
	public AwtFontMetrics getFontMetrics()
	{
		return this.fontMetrics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#hitClip(int, int, int, int)
	 */
	@Override
	public boolean hitClip(int x, int y, int width, int height)
	{
		return getClipBounds().intersects(new Rectangle(x, y, width, height));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#clearRect(int, int, int, int)
	 */
	@Override
	public void clearRect(int x, int y, int width, int height)
	{
		canvas.clipRect(x, y, x + width, y + height);
		if (currentColor != null)
		{
			canvas.drawARGB(currentColor.getAlpha(), currentColor.getBlue(), currentColor.getGreen(), currentColor.getRed());
		} else
		{
			canvas.drawARGB(0xff, 0xff, 0xff, 0xff);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#clipRect(int, int, int, int)
	 */
	@Override
	public boolean clipRect(int x, int y, int width, int height)
	{
		int cl[] =
		{ -1, x, y, -2, x, y + width, -2, x + height, y + width, -2, x + height, y };
		Shape shp = createShape(cl);
		mCurrClip.intersect(new Area(shp));
		canvas.clipRect(new Rect(x, y, x + width, y + height), Region.Op.INTERSECT);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	@Override
	public void copyArea(int sx, int sy, int width, int height, int dx, int dy)
	{
		// TODO for Dritan: check if it is neccesary to implement this method
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#create()
	 */
	@Override
	public Graphics create()
	{
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#dispose()
	 */
	@Override
	public void dispose()
	{
		canvas = null;
		currentPaint = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawArc(int, int, int, int, int, int)
	 */
	@Override
	public void drawArc(int x, int y, int width, int height, int sa, int ea)
	{
		if (currentPaint == null)
		{
			currentPaint = new Paint();
		}
		currentPaint.setStrokeWidth(0);
		canvas.drawArc(new RectF(x, y, x + width, y + height), 360 - (ea + sa), ea, true, currentPaint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, awt.java.awt.Color, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer)
	{
		// TODO for Dritan: this method is already used in prefuse. check how it can be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int, int, awt.java.awt.Color, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int, int, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int, int, int, int, int, int, awt.java.awt.Color, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawImage(awt.java.awt.Image, int, int, int, int, int, int, int, int, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawLine(int, int, int, int)
	 */
	@Override
	public void drawLine(int x1, int y1, int x2, int y2)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		this.setStroke();
		canvas.drawLine((float) x1, (float) y1, (float) x2, (float) y2, currentPaint);  // remove this line to test the performance without drawing-operation (for performance tests)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawOval(int, int, int, int)
	 */
	@Override
	public void drawOval(int x, int y, int width, int height)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		RectF oval = new RectF(x, y, x + width, y + height);
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.STROKE);
		canvas.drawOval(oval, currentPaint); // remove this line to test the performance without drawing-operation (for performance tests)
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolygon(int[], int[], int)
	 */
	@Override
	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		canvas.drawLine(xpoints[npoints - 1], ypoints[npoints - 1], xpoints[0], ypoints[0], currentPaint);
		for (int i = 0; i < npoints - 1; i++)
		{
			canvas.drawLine(xpoints[i], ypoints[i], xpoints[i + 1], ypoints[i + 1], currentPaint);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawPolyline(int[], int[], int)
	 */
	@Override
	public void drawPolyline(int[] xpoints, int[] ypoints, int npoints)
	{
		for (int i = 0; i < npoints - 1; i++)
		{
			drawLine(xpoints[i], ypoints[i], xpoints[i + 1], ypoints[i + 1]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.STROKE);
		canvas.drawRoundRect(new RectF(x, y, width, height), arcWidth, arcHeight, currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillArc(int, int, int, int, int, int)
	 */
	@Override
	public void fillArc(int x, int y, int width, int height, int sa, int ea)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawArc(new RectF(x, y, x + width, y + height), 360 - (sa + ea), ea, true, currentPaint);

		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillOval(int, int, int, int)
	 */
	@Override
	public void fillOval(int x, int y, int width, int height)
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		RectF oval = new RectF(x, y, x + width, y + height);
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL);
		canvas.drawOval(oval, currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillPolygon(int[], int[], int)
	 */
	@Override
	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		Paint.Style tmp = currentPaint.getStyle();
		canvas.save(Canvas.CLIP_SAVE_FLAG);

		currentPaint.setStyle(Paint.Style.FILL);

		GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, npoints);
		filledPolygon.moveTo(xpoints[0], ypoints[0]);
		for (int index = 1; index < xpoints.length; index++)
		{
			filledPolygon.lineTo(xpoints[index], ypoints[index]);
		}
		filledPolygon.closePath();
		Path path = getPath(filledPolygon);
		canvas.clipPath(path);
		canvas.drawPath(path, currentPaint);

		currentPaint.setStyle(tmp);
		canvas.restore();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#fillRect(int, int, int, int)
	 */
	@Override
	public void fillRect(int x, int y, int width, int height)
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL);
		RectF rect = new RectF(x, y, x + width, y + height);
		canvas.drawRect(rect, currentPaint);
		currentPaint.setStyle(tmp);
	}

	public void fillRect(float x, float y, float width, float height)
	{
		RectF rect = new RectF(x, y, x + width, y + height);
		if (currentPaint == null)
			currentPaint = new Paint();
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL);
		canvas.drawRect(rect, currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc) FIXME Dritan: this method is not programmed proper. please invest more time to make it right
	 * 
	 * @see awt.java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL);
		canvas.drawRoundRect(new RectF(x, y, x + width, y + height), arcWidth, arcHeight, currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getClip()
	 */
	@Override
	public Shape getClip()
	{
		return mCurrClip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getColor()
	 */
	@Override
	public Color getColor()
	{
		if (currentPaint != null)
			return new Color(currentPaint.getColor());

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFont()
	 */
	@Override
	public Font getFont()
	{
		return this.font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#getFontMetrics(awt.java.awt.Font)
	 */
	@Override
	public AwtFontMetrics getFontMetrics(Font font)
	{
		return new AwtFontMetrics(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setClip(int, int, int, int)
	 */
	@Override
	public void setClip(int x, int y, int width, int height)
	{
		int cl[] =
		{ -1, x, y, -2, x, y + width, -2, x + height, y + width, -2, x + height, y };
		mCurrClip = new Area(createShape(cl));
		canvas.clipRect(x, y, x + width, y + height, Region.Op.REPLACE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setClip(awt.java.awt.Shape)
	 */
	@Override
	public void setClip(Shape clip)
	{
		mCurrClip = new Area(clip);
		canvas.clipPath(getPath(clip), Region.Op.REPLACE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setColor(awt.java.awt.Color)
	 */
	@Override
	public void setColor(Color c)
	{
		this.currentPaint.setColor(c.getAndroidColorRepresentation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setFont(awt.java.awt.Font)
	 */
	@Override
	public void setFont(Font font)
	{
		this.font = font;
		this.fontMetrics.setFont(font);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setPaintMode()
	 */
	@Override
	public void setPaintMode()
	{
		if (currentPaint == null)
			currentPaint = new Paint();
		currentPaint.setXfermode(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics#setXORMode(awt.java.awt.Color)
	 */
	@Override
	public void setXORMode(Color color)
	{
		if (currentPaint == null)
		{
			currentPaint = new Paint();
		}
		currentPaint.setXfermode(new PixelXorXfermode(color.getRGB()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#addRenderingHints(java.util.Map)
	 */
	@Override
	public void addRenderingHints(Map<?, ?> hints)
	{
		if (currentRenderingHints == null)
			currentRenderingHints = (RenderingHints) hints;

		currentRenderingHints.add((RenderingHints) hints);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#clip(awt.java.awt.Shape)
	 */
	@Override
	public void clip(Shape s)
	{
		canvas.clipPath(getPath(s));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#draw(awt.java.awt.Shape)
	 */
	@Override
	public void draw(Shape s)
	{
		if (currentPaint == null)
		{
			currentPaint = new Paint();
		}
		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.STROKE);
//		canvas.drawLines(getLineCoordiantes(s), currentPaint);
		canvas.drawPath( getPath(s), currentPaint); // remove this line to test the performance without drawing-operation (for performance tests)
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawGlyphVector(awt.java.awt.font.GlyphVector, float, float)
	 */
	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y)
	{
		// TODO draw at x, y
		// draw(g.getOutline());
		/*
		 * Matrix matrix = new Matrix(); matrix.setTranslate(x, y); Path pth = getPath(g.getOutline()); pth.transform(matrix); draw(pth);
		 */

		// TODO for Dritan: check if it is neccesary to implement this
		// Path path = new Path();
		// char[] c = ((AndroidGlyphVector)g).getGlyphs();
		// mP.getTextPath(c, 0, c.length, x, y, path);
		// mC.drawPath(path, mP);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawImage(awt.java.awt.image.BufferedImage, awt.java.awt.image.BufferedImageOp, int, int)
	 */
	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y)
	{
		// TODO for Dritan: this should be implemented

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawImage(awt.java.awt.Image, awt.java.awt.geom.AffineTransform, awt.java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs)
	{
		// TODO for Dritan: this should be implemented
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawRenderableImage(awt.java.awt.image.renderable .RenderableImage, awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform)
	{
		// TODO for Dritan: this should be implemented

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawRenderedImage(awt.java.awt.image.RenderedImage , awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform)
	{
		// TODO for Dritan: this should be implemented

	}

	/*
	 * (non-Javadoc) FIXME Dritan: String attributes are ignored. Fix this if there is time or if it's neccessary
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y)
	{
		String myString = "";
		for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next())
		{
			myString += c;
		}
		drawString(myString, x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y)
	{
		drawString(iterator, (float) x, (float) y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.lang.String, float, float)
	 */
	@Override
	public void drawString(String s, float x, float y)
	{
		currentPaint.setTextSize(this.font.getSize());
		canvas.drawText(s, x, y, currentPaint);  // remove this line to test the performance without drawing-operation (for performance tests)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#drawString(java.lang.String, int, int)
	 */
	@Override
	public void drawString(String str, int x, int y)
	{
		currentPaint.setTextSize(this.font.getSize());
		canvas.drawText(str, x, y, currentPaint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#fill(awt.java.awt.Shape)
	 */
	@Override
	public void fill(Shape s)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		Paint.Style tmp = currentPaint.getStyle();
		currentPaint.setStyle(Paint.Style.FILL);
		canvas.drawPath(getPath(s), currentPaint);
		currentPaint.setStyle(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getBackground()
	 */
	@Override
	public Color getBackground()
	{
		return currentColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getComposite()
	 */
	@Override
	public Composite getComposite()
	{
		throw new RuntimeException("Composite not implemented!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getDeviceConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDeviceConfiguration()
	{
		// TODO for Dritan: check if it is needed to implement this
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getFontRenderContext()
	 */
	@Override
	public FontRenderContext getFontRenderContext()
	{
		// TODO for Dritan: check if it is needed to implement this
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getPaint()
	 */
	@Override
	public PPaint getPaint()
	{
		throw new RuntimeException("AWT Paint not implemented in Android!");
	}

	public Paint getCurrentPaint()
	{
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
	 * @see awt.java.awt.Graphics2D#getRenderingHint(awt.java.awt.RenderingHints.Key)
	 */
	@Override
	public Object getRenderingHint(Key key)
	{
		if (currentRenderingHints == null)
		{
			return null;
		}
		return currentRenderingHints.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getRenderingHints()
	 */
	@Override
	public RenderingHints getRenderingHints()
	{
		return currentRenderingHints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getStroke()
	 */
	@Override
	public Stroke getStroke()
	{
		if (currentPaint != null)
			return new BasicStroke(currentPaint.getStrokeWidth(), currentPaint.getStrokeCap().ordinal(), currentPaint.getStrokeJoin().ordinal());

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#getTransform()
	 */
	@Override
	public AffineTransform getTransform()
	{
		return afineTransform;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#hit(awt.java.awt.Rectangle, awt.java.awt.Shape, boolean)
	 */
	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke)
	{
		return s.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#rotate(double)
	 */
	@Override
	public void rotate(double theta)
	{
		this.afineTransform.rotate(theta);
		currentMatrix.preRotate((float) AndroidGraphics2D.getDegree((float) (RAD_360 - theta)));
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#rotate(double, double, double)
	 */
	@Override
	public void rotate(double theta, double x, double y)
	{
		this.afineTransform.rotate(theta, x, y);
		currentMatrix.preRotate((float) AndroidGraphics2D.getDegree((float) theta), (float) x, (float) y);
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#scale(double, double)
	 */
	@Override
	public void scale(double sx, double sy)
	{
		this.afineTransform.scale(sx, sy);
		currentMatrix.setScale((float) sx, (float) sy);
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setBackground(awt.java.awt.Color)
	 */
	@Override
	public void setBackground(Color color)
	{
		currentColor = color;
		canvas.clipRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
		// TODO don't limit to current clip
		canvas.drawARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setComposite(awt.java.awt.Composite)
	 */
	@Override
	public void setComposite(Composite comp)
	{
		throw new RuntimeException("Composite not implemented!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setPaint(awt.java.awt.Paint)
	 */
	@Override
	public void setPaint(PPaint paint)
	{
		this.currentPaint.setColor(paint.getAndroidColorRepresentation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setRenderingHint(awt.java.awt.RenderingHints.Key, java.lang.Object)
	 */
	@Override
	public void setRenderingHint(Key key, Object value)
	{
		if (currentRenderingHints == null)
		{
			currentRenderingHints = new RenderingHints(key, value);
		} else
		{
			currentRenderingHints.put(key, value);
		}
		applyHints();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setRenderingHints(java.util.Map)
	 */
	@Override
	public void setRenderingHints(Map<?, ?> hints)
	{
		currentRenderingHints = (RenderingHints) hints;
		applyHints();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setStroke(awt.java.awt.Stroke)
	 */
	@Override
	public void setStroke(Stroke s)
	{
		if (currentPaint == null)
			currentPaint = new Paint();

		BasicStroke bs = (BasicStroke) s;
		currentPaint.setStyle(Paint.Style.STROKE);
		currentPaint.setStrokeWidth(bs.getLineWidth());

		int cap = bs.getEndCap();
		if (cap == 0)
		{
			currentPaint.setStrokeCap(Paint.Cap.BUTT);
		} else if (cap == 1)
		{
			currentPaint.setStrokeCap(Paint.Cap.ROUND);
		} else if (cap == 2)
		{
			currentPaint.setStrokeCap(Paint.Cap.SQUARE);
		}

		int join = bs.getLineJoin();
		if (join == 0)
		{
			currentPaint.setStrokeJoin(Paint.Join.MITER);
		} else if (join == 1)
		{
			currentPaint.setStrokeJoin(Paint.Join.ROUND);
		} else if (join == 2)
		{
			currentPaint.setStrokeJoin(Paint.Join.BEVEL);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#setTransform(awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void setTransform(AffineTransform Tx)
	{
		
		/*
		 ORIGINAL CODE FROM ANDROID TODO for Dritan: Check it the original Code is necessary. 
		 At the moment it throws an exception with the message matrix can not be changed (at reset and setValues). 
		currentMatrix.reset();
		
		//  if(Tx.isIdentity()) { mM = new Matrix(); }
		 
		currentMatrix.setValues(createMatrix(Tx));
		Matrix m = new Matrix();
		m.setValues(getInverseMatrix());
		canvas.concat(m);
		canvas.concat(currentMatrix);
		*/
		this.afineTransform.setTransform(Tx);
		Matrix m = new Matrix();
		m.setValues(createMatrix(Tx));
		canvas.concat(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#shear(double, double)
	 */
	@Override
	public void shear(double shx, double shy)
	{
		this.afineTransform.shear(shx, shy);
		currentMatrix.setSkew((float) shx, (float) shy);
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#transform(awt.java.awt.geom.AffineTransform)
	 */
	@Override
	public void transform(AffineTransform Tx)
	{
		this.afineTransform.concatenate(Tx);
		Matrix m = new Matrix();
		m.setValues(createMatrix(Tx));
		canvas.concat(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#translate(double, double)
	 */
	@Override
	public void translate(double tx, double ty)
	{
		this.afineTransform.translate(tx, ty);
		currentMatrix.setTranslate((float) tx, (float) ty);
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#translate(int, int)
	 */
	@Override
	public void translate(int x, int y)
	{
		this.afineTransform.translate(x, y);
		currentMatrix.setTranslate((float) x, (float) y);
		canvas.concat(currentMatrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
	 */
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised)
	{
		Color color = getColor();
		Color colorUp, colorDown;
		if (raised)
		{
			colorUp = color.brighter();
			colorDown = color.darker();
			setColor(color);
		} else
		{
			colorUp = color.darker();
			colorDown = color.brighter();
			setColor(colorUp);
		}

		width--;
		height--;
		fillRect(x + 1, y + 1, width - 1, height - 1);

		setColor(colorUp);
		fillRect(x, y, width, 1);
		fillRect(x, y + 1, 1, height);

		setColor(colorDown);
		fillRect(x + width, y, 1, height);
		fillRect(x + 1, y + height, width, 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see awt.java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
	 */
	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised)
	{
		Color color = getColor();
		Color colorUp, colorDown;
		if (raised)
		{
			colorUp = color.brighter();
			colorDown = color.darker();
		} else
		{
			colorUp = color.darker();
			colorDown = color.brighter();
		}

		setColor(colorUp);
		fillRect(x, y, width, 1);
		fillRect(x, y + 1, 1, height);

		setColor(colorDown);
		fillRect(x + width, y, 1, height);
		fillRect(x + 1, y + height, width, 1);
	}

	private Shape createShape(int[] arr)
	{
		Shape s = new GeneralPath();
		for (int i = 0; i < arr.length; i++)
		{
			int type = arr[i];
			switch (type)
			{
				case -1:
					// MOVETO
					((GeneralPath) s).moveTo(arr[++i], arr[++i]);
					break;
				case -2:
					// LINETO
					((GeneralPath) s).lineTo(arr[++i], arr[++i]);
					break;
				case -3:
					// QUADTO
					((GeneralPath) s).quadTo(arr[++i], arr[++i], arr[++i], arr[++i]);
					break;
				case -4:
					// CUBICTO
					((GeneralPath) s).curveTo(arr[++i], arr[++i], arr[++i], arr[++i], arr[++i], arr[++i]);
					break;
				case -5:
					// CLOSE
					return s;
				default:
					break;
			}
		}
		return s;
	}

	private Path getPath(Shape s)
	{
		//path.rewind();
		Path path1 = new Path();
		PathIterator pi = s.getPathIterator(null);
		while (pi.isDone() == false)
		{
			getCurrentSegment(pi, path1);
			pi.next();
		}
		return path1;
	}

	/**
	 * a try to replace the drawPath with drawLines (because of performance issues)
	 * 1. this solution was not faster
	 * 2. it has a bug, it does not draws the last line to close the shape 
	 * @param s
	 * @return
	 */
	private float[] getLineCoordiantes( Shape s )
	{
		float[] coordinates = new float[6];
		points.clear();
		PathIterator pi = s.getPathIterator(null);
		while (pi.isDone() == false)
		{
			int type = pi.currentSegment(coordinates);
			switch (type)
			{
				case PathIterator.SEG_MOVETO:
					points.add(coordinates[0]);
					points.add(coordinates[1]);
					break;
				case PathIterator.SEG_LINETO:
					points.add(coordinates[0]);
					points.add(coordinates[1]);
					points.add(coordinates[0]);
					points.add(coordinates[1]);					
					break;
				default:
					break;
			}			
			pi.next();
		}

		float[] floatPoints = new float[points.size()];
		int i = 0;
		for(float p: points)
			floatPoints[i++] = p;
		
		return floatPoints;
	}
	
	private void getCurrentSegment(PathIterator pi, Path path)
	{
		float[] coordinates = new float[6];
		int type = pi.currentSegment(coordinates);
		switch (type)
		{
			case PathIterator.SEG_MOVETO:
				path.moveTo(coordinates[0], coordinates[1]);
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(coordinates[0], coordinates[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.cubicTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5]);
				break;
			case PathIterator.SEG_CLOSE:
				path.close();
				break;
			default:
				break;
		}
	}

	private float[] getCurrentCoordiantes(PathIterator pi, Path path)
	{
		float[] coordinates = new float[6];
		int type = pi.currentSegment(coordinates);
		switch (type)
		{
			case PathIterator.SEG_MOVETO:
				path.moveTo(coordinates[0], coordinates[1]);
				break;
			case PathIterator.SEG_LINETO:
				path.lineTo(coordinates[0], coordinates[1]);
				break;
			case PathIterator.SEG_QUADTO:
				path.quadTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
				break;
			case PathIterator.SEG_CUBICTO:
				path.cubicTo(coordinates[0], coordinates[1], coordinates[2], coordinates[3], coordinates[4], coordinates[5]);
				break;
			case PathIterator.SEG_CLOSE:
				path.close();
				break;
			default:
				break;
		}
		return coordinates;
	}

	
	public static float getDegree(float radian)
	{
		return (float) ((180 / Math.PI) * radian);
	}

	private void applyHints()
	{
		Object o;

		// TODO do something like this:
		/*
		 * Set s = mRh.keySet(); Iterator it = s.iterator(); while(it.hasNext()) { o = it.next(); }
		 */

		// /////////////////////////////////////////////////////////////////////
		// not supported in skia
		/*
		 * o = mRh.get(RenderingHints.KEY_ALPHA_INTERPOLATION); if (o.equals(RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY))
		 * { } else if (o.equals(RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_COLOR_RENDERING); if (o.equals(RenderingHints.VALUE_COLOR_RENDER_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_COLOR_RENDER_QUALITY)) { } else if
		 * (o.equals(RenderingHints.VALUE_COLOR_RENDER_SPEED)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_DITHERING); if (o.equals(RenderingHints.VALUE_DITHER_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_DITHER_DISABLE)) { } else if
		 * (o.equals(RenderingHints.VALUE_DITHER_ENABLE)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_FRACTIONALMETRICS); if (o.equals(RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_FRACTIONALMETRICS_OFF)) { } else
		 * if (o.equals(RenderingHints.VALUE_FRACTIONALMETRICS_ON)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_INTERPOLATION); if (o.equals(RenderingHints.VALUE_INTERPOLATION_BICUBIC)) { } else if (o.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) { } else if (o
		 * .equals(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_RENDERING); if (o.equals(RenderingHints.VALUE_RENDER_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_RENDER_QUALITY)) { } else if
		 * (o.equals(RenderingHints.VALUE_RENDER_SPEED)) { }
		 * 
		 * o = mRh.get(RenderingHints.KEY_STROKE_CONTROL); if (o.equals(RenderingHints.VALUE_STROKE_DEFAULT)) { } else if (o.equals(RenderingHints.VALUE_STROKE_NORMALIZE)) { } else if
		 * (o.equals(RenderingHints.VALUE_STROKE_PURE)) { }
		 */

		o = currentRenderingHints.get(RenderingHints.KEY_ANTIALIASING);
		if (o != null)
		{
			if (o.equals(RenderingHints.VALUE_ANTIALIAS_DEFAULT))
			{
				currentPaint.setAntiAlias(false);
			} else if (o.equals(RenderingHints.VALUE_ANTIALIAS_OFF))
			{
				currentPaint.setAntiAlias(false);
			} else if (o.equals(RenderingHints.VALUE_ANTIALIAS_ON))
			{
				currentPaint.setAntiAlias(true);
			}
		}

		o = currentRenderingHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
		if (o != null)
		{
			if (o.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT))
			{
				currentPaint.setAntiAlias(false);
			} else if (o.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF))
			{
				currentPaint.setAntiAlias(false);
			} else if (o.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_ON))
			{
				currentPaint.setAntiAlias(true);
			}
		}
	}

	public Canvas getCanvas()
	{
		return canvas;
	}
}
