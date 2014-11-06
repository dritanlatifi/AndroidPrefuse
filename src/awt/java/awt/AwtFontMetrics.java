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
/**
 * @author Ilya S. Okomin
 * @version $Revision$
 */

package awt.java.awt;
import java.io.Serializable;
import org.apache.harmony.awt.internal.nls.Messages;


/**
 * The FontMetrics class contains information about the rendering of a
 * particular font on a particular screen.
 * <p>
 * Each character in the Font has three values that help define where to place
 * it: an ascent, a descent, and an advance. The ascent is the distance the
 * character extends above the baseline. The descent is the distance the
 * character extends below the baseline. The advance width defines the position
 * at which the next character should be placed.
 * <p>
 * An array of characters or a string has an ascent, a descent, and an advance
 * width too. The ascent or descent of the array is specified by the maximum
 * ascent or descent of the characters in the array. The advance width is the
 * sum of the advance widths of each of the characters in the character array.
 * </p>
 * 
 * @since Android 1.0
 */
public class AwtFontMetrics implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1681126225205050147L;

    /**
     * The font from which the FontMetrics is created.
     */
    protected Font font;

    public void setFont(Font font) {
		this.font = font;
	}

	/**
     * Instantiates a new font metrics from the specified Font.
     * 
     * @param fnt
     *            the Font.
     */
    public AwtFontMetrics(Font fnt) {
        this.font = fnt;
    }

    /**
     * Returns the String representation of this FontMetrics.
     * 
     * @return the string.
     */
    @Override
    public String toString() {
        return this.getClass().getName() + "[font=" + this.getFont() + //$NON-NLS-1$
                "ascent=" + this.getAscent() + //$NON-NLS-1$
                ", descent=" + this.getDescent() + //$NON-NLS-1$
                ", height=" + this.getHeight() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Gets the font associated with this FontMetrics.
     * 
     * @return the font associated with this FontMetrics.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the height of the text line in this Font.
     * 
     * @return the height of the text line in this Font.
     */
    public int getHeight() {
        return this.getAscent() + this.getDescent() + this.getLeading();
    }

    /**
     * Gets the font ascent of the Font associated with this FontMetrics. The
     * font ascent is the distance from the font's baseline to the top of most
     * alphanumeric characters.
     * 
     * @return the ascent of the Font associated with this FontMetrics.
     */
    public int getAscent() {
    	android.graphics.Paint.FontMetrics fm = this.font.getPaint().getFontMetrics();
    	return (int) fm.ascent;
    }

    /**
     * Gets the font descent of the Font associated with this FontMetrics. The
     * font descent is the distance from the font's baseline to the bottom of
     * most alphanumeric characters with descenders.
     * 
     * @return the descent of the Font associated with this FontMetrics.
     */
    public int getDescent() {
    	android.graphics.Paint.FontMetrics fm = this.font.getPaint().getFontMetrics();
    	return (int) fm.descent;
    }

    /**
     * Gets the leading of the Font associated with this FontMetrics.
     * 
     * @return the leading of the Font associated with this FontMetrics.
     */
    public int getLeading() {
    	android.graphics.Paint.FontMetrics fm = this.font.getPaint().getFontMetrics();
    	return (int) fm.leading;
    }

    /**
     * Returns the distance from the leftmost point to the rightmost point on
     * the string's baseline showing the specified array of bytes in this Font.
     * 
     * @param data
     *            the array of bytes to be measured.
     * @param off
     *            the start offset.
     * @param len
     *            the number of bytes to be measured.
     * @return the advance width of the array.
     */
    public int bytesWidth(byte[] data, int off, int len) {
        int width = 0;
        if ((off >= data.length) || (off < 0)) {
            // awt.13B=offset off is out of range
            throw new IllegalArgumentException(Messages.getString("awt.13B")); //$NON-NLS-1$
        }

        if ((off + len > data.length)) {
            // awt.13C=number of elemets len is out of range
            throw new IllegalArgumentException(Messages.getString("awt.13C")); //$NON-NLS-1$
        }

        for (int i = off; i < off + len; i++) {
            width += charWidth(data[i]);
        }

        return width;
    }

    /**
     * Returns the distance from the leftmost point to the rightmost point on
     * the string's baseline showing the specified array of characters in this
     * Font.
     * 
     * @param data
     *            the array of characters to be measured.
     * @param off
     *            the start offset.
     * @param len
     *            the number of bytes to be measured.
     * @return the advance width of the array.
     */
    public int charsWidth(char[] data, int off, int len) {
        int width = 0;
        if ((off >= data.length) || (off < 0)) {
            // awt.13B=offset off is out of range
            throw new IllegalArgumentException(Messages.getString("awt.13B")); //$NON-NLS-1$
        }

        if ((off + len > data.length)) {
            // awt.13C=number of elemets len is out of range
            throw new IllegalArgumentException(Messages.getString("awt.13C")); //$NON-NLS-1$
        }

        for (int i = off; i < off + len; i++) {
            width += charWidth(data[i]);
        }

        return width;
    }

    /**
     * Returns the distance from the leftmost point to the rightmost point of
     * the specified character in this Font.
     * 
     * @param ch
     *            the specified Unicode point code of character to be measured.
     * @return the advance width of the character.
     */
    public int charWidth(int ch) {
        return charWidth((char) ch);
    }

    /**
     * Returns the distance from the leftmost point to the rightmost point of
     * the specified character in this Font.
     * 
     * @param ch
     *            the specified character to be measured.
     * @return the advance width of the character.
     */
    public int charWidth(char ch) {
        return stringWidth(Character.toString(ch));
    }

    /**
     * Gets the maximum advance width of character in this Font.
     * 
     * @return the maximum advance width of character in this Font.
     */
//    public int getMaxAdvance() {
//        return 0;
//    }

    /**
     * Gets the maximum font ascent of the Font associated with this
     * FontMetrics.
     * 
     * @return the maximum font ascent of the Font associated with this
     *         FontMetrics.
     */
//    public int getMaxAscent() {
//        return 0;
//    }

    /**
     * Gets the maximum font descent of character in this Font.
     * 
     * @return the maximum font descent of character in this Font.
     * @deprecated Replaced by getMaxDescent() method.
     */
//    @Deprecated
//    public int getMaxDecent() {
//        return 0;
//    }

    /**
     * Gets the maximum font descent of character in this Font.
     * 
     * @return the maximum font descent of character in this Font.
     */
//    public int getMaxDescent() {
//        return 0;
//    }

    /**
     * Gets the advance widths of the characters in the Font.
     * 
     * @return the advance widths of the characters in the Font.
     */
//    public int[] getWidths() {
//        return null;
//    }

    /**
     * Returns the advance width for the specified String in this Font.
     * 
     * @param str
     *            String to be measured.
     * @return the the advance width for the specified String in this Font.
     */
    public int stringWidth(String str) {
        return (int) this.font.getPaint().getTextSize(); // TODO for Dritan: Check if this is correct
    }

}
