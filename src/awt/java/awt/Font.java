/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package awt.java.awt;

import android.graphics.Paint;
import android.graphics.Typeface;
import java.io.Serializable;
import org.apache.harmony.misc.HashCode;

/**
 * The Font class represents fonts for rendering text. This class allow to map
 * characters to glyphs.
 * <p>
 * A glyph is a shape used to render a character or a sequence of characters.
 * For example one character of Latin writing system represented by one glyph,
 * but in complex writing system such as South and South-East Asian there is
 * more complicated correspondence between characters and glyphs.
 * <p>
 * The Font object is identified by two types of names. The logical font name is
 * the name that is used to construct the font. The font name is the name of a
 * particular font face (for example, Arial Bold). The family name is the font's
 * family name that specifies the typographic design across several faces (for
 * example, Arial). In all the Font is identified by three attributes: the
 * family name, the style (such as bold or italic), and the size.
 * 
 * @since Android 1.0
 */
public class Font implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -4206021311591459213L;

    /**
     * The Constant PLAIN indicates font's plain style.
     */
    public static final int PLAIN = Typeface.NORMAL;

    /**
     * The Constant BOLD indicates font's bold style.
     */
    public static final int BOLD = Typeface.BOLD;

    /**
     * The Constant ITALIC indicates font's italic style.
     */
    public static final int ITALIC = Typeface.ITALIC;


    /**
     * The Constant DEFAULT_FONT.
     */
    static final Font DEFAULT_FONT = new Font( null, Font.PLAIN, 12); //$NON-NLS-1$

    /**
     * Font functions in Android 
     */
    protected Typeface tf;
    
    /**
     * The name of the Font.
     */
    protected String name;

    /**
     * The style of the Font.
     */
    protected int style;

    /**
     * The size of the Font.
     */
    protected int size;

    /**
     * The point size of the Font.
     */
    protected float pointSize;

    
    /**
     * android Paint. this is needed at the class AwtFontMetrics
     */
    protected Paint paint;

    /**
     * @author Dritan
     * @return
     */
    public Paint getPaint() {
		return paint;
	}

    /**
     * @author Dritan
     * @param paint
     */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}

	/**
     * Instantiates a new Font with the specified name, style and size.
     * 
     * @param name
     *            the name of font.
     * @param style
     *            the style of font.
     * @param size
     *            the size of font.
     */
    public Font(String name, int style, int size) {

        this.name = (name == "Default") ? null : name; //$NON-NLS-1$
        this.size = (size >= 0) ? size : 0;
        this.style = (style & ~0x03) == 0 ? style : Font.PLAIN;
        if(this.paint == null)
        	this.paint = new Paint();
        
        this.pointSize = this.size;
        this.tf = Typeface.create(name, style);
        this.paint.setTextSize( size );
        this.paint.setTypeface(tf);        
    }

    /**
     * @author Dritan
     * @return
     */
    public Typeface getTypeface()
    {
    	return this.tf;
    }
    
    /**
     * 
     * @author Dritan
     * @param name
     * @param style
     * @param size
     * @param paint
     */
    public Font( String name, int style, int size, Paint paint )
    {
    	this(name, style, size);
    	this.paint = paint;
    	this.paint.setTextSize(size);
    }
    

    /**
     * Gets the font name.
     * 
     * @return the font name.
     */
    public String getFontName() {
    	// TODO for Dritan: implement this
    	return null;
    }



    /**
     * Returns the String representation of this Font.
     * 
     * @return the String representation of this Font.
     */
    @Override
    public String toString() {
        String stl = "plain"; //$NON-NLS-1$
        String result;

        if (this.isBold() && this.isItalic()) {
            stl = "bolditalic"; //$NON-NLS-1$
        }
        if (this.isBold() && !this.isItalic()) {
            stl = "bold"; //$NON-NLS-1$
        }

        if (!this.isBold() && this.isItalic()) {
            stl = "italic"; //$NON-NLS-1$
        }

        result = this.getClass().getName() + 
                ",name=" + this.name + //$NON-NLS-1$
                ",style=" + stl + //$NON-NLS-1$
                ",size=" + this.size + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        return result;
    }

    /**
     * Gets the logical name of this Font.
     * 
     * @return the logical name of this Font.
     */
    public String getName() {
        return (this.name);
    }


    /**
     * Checks if this font has plain style or not.
     * 
     * @return true, if this font has plain style, false otherwise.
     */
    public boolean isPlain() {
        return (this.style == PLAIN);
    }

    /**
     * Checks if this font has italic style or not.
     * 
     * @return true, if this font has italic style, false otherwise.
     */
    public boolean isItalic() {
        return (this.style & ITALIC) != 0;
    }

    /**
     * Checks if this font has bold style or not.
     * 
     * @return true, if this font has bold style, false otherwise.
     */
    public boolean isBold() {
        return (this.style & BOLD) != 0;
    }


    /**
     * Returns hash code of this Font object.
     * 
     * @return the hash code of this Font object.
     */
    @Override
    public int hashCode() {
        HashCode hash = new HashCode();

        hash.append(this.name);
        hash.append(this.style);
        hash.append(this.size);
        hash.append(tf.hashCode());
        return hash.hashCode();
    }

    /**
     * Gets the style of this Font.
     * 
     * @return the style of this Font.
     */
    public int getStyle() {
    	return this.style;
    }

    /**
     * Gets the size of this Font.
     * 
     * @return the size of this Font.
     */
    public int getSize() {
        return this.size;
    }


}
