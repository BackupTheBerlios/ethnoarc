/**
*
* Copyright (C) 2006-2008 FhG Fokus
*
* This file is part of the ethnoArc toolkit - a set of programs aimed
* at providing database tools and services for ethnological archives.
*
* You can redistribute the ethnoArc tools and/or modify it
* under the terms of the GNU General Public License Version 3 as published by
* the Free Software Foundation.
*
* For a license to use the ethnoArc tools software under conditions
* other than those described here, or to purchase support for this
* software, please contact Fraunhofer FOKUS by e-mail at the following
* addresses:
*   support@ethnoArc.org
*
* The ethnoArc toolkit is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>
* or write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/
package de.fhg.fokus.se.ethnoarc.dbmanager.helper;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * 
 * @author Kevin Day
 * Trumpet, Inc.
 * Revised to allow it to work with new versions of Java & swing.
 */

public class TextPaneAppender extends AppenderSkeleton {
	
	JTextPane textpane;
	StyledDocument doc;
	StringWriter sw;
	QuietWriter qw;
	Hashtable attributes;
	Hashtable icons;
	int maxBufSize;

	private String label;

	private boolean fancy;

	final String LABEL_OPTION = "Label";
	final String COLOR_OPTION_FATAL = "Color.Emerg";
	final String COLOR_OPTION_ERROR = "Color.Error";
	final String COLOR_OPTION_WARN = "Color.Warn";
	final String COLOR_OPTION_INFO = "Color.Info";
	final String COLOR_OPTION_DEBUG = "Color.Debug";
	final String COLOR_OPTION_BACKGROUND = "Color.Background";
	final String FANCY_OPTION = "Fancy";
	final String FONT_NAME_OPTION = "Font.Name";
	final String FONT_SIZE_OPTION = "Font.Size";

	public static Image loadIcon ( String path ) {
		Image img = null;
		try {
			URL url = TextPaneAppender.class.getResource(path); 
			if (url != null){
				img = (Image) (Toolkit.getDefaultToolkit()).getImage(url);
			} else {
				System.out.println("Unable to get image from " + path);
			}

		} catch (Exception e) {
			System.out.println("Exception occured: " + e.getMessage() + 
					" - " + e );   
		}	
		return (img);
	}

	public TextPaneAppender(Layout layout, String name, int maxBufSize) {
		this(layout, name);
		this.maxBufSize = maxBufSize;
	}
	
	public TextPaneAppender(Layout layout, String name) {
		this();
		this.layout = layout;
		this.name = name;
		setTextPane(new JTextPane());
		createAttributes();
		//createIcons();
		//setPattern();
	}

	public TextPaneAppender() {
		super();
		maxBufSize = 128000;
		setTextPane(new JTextPane());
		createAttributes();
		//createIcons();
		this.label="";
		this.sw = new StringWriter();
		this.qw = new QuietWriter(sw, errorHandler);
		this.fancy =true;
		//setPattern();
	}
	

	public
	void close() {

	}
	private int fontSize=11;

	private void createAttributes() {	

		attributes = new Hashtable();

		MutableAttributeSet att = new SimpleAttributeSet();
		attributes.put(Level.FATAL, att);
		StyleConstants.setFontSize(att,fontSize);

		att = new SimpleAttributeSet();
		attributes.put(Level.ERROR, att);
		StyleConstants.setFontSize(att,fontSize);

		att = new SimpleAttributeSet();
		attributes.put(Level.WARN, att);
		StyleConstants.setFontSize(att,fontSize);

		att = new SimpleAttributeSet();
		attributes.put(Level.INFO, att);
		StyleConstants.setFontSize(att,fontSize);

		att = new SimpleAttributeSet();
		attributes.put(Level.DEBUG, att);
		StyleConstants.setFontSize(att,fontSize);

		StyleConstants.setForeground((MutableAttributeSet)attributes.get(Level.FATAL),Color.red);
		StyleConstants.setForeground((MutableAttributeSet)attributes.get(Level.ERROR),Color.red);
		StyleConstants.setForeground((MutableAttributeSet)attributes.get(Level.WARN),Color.orange);
		StyleConstants.setForeground((MutableAttributeSet)attributes.get(Level.INFO),Color.gray);
		StyleConstants.setForeground((MutableAttributeSet)attributes.get(Level.DEBUG),Color.black);
	}

	private void createIcons() {

		icons = new Hashtable();

		try {
			icons.put(Level.FATAL,new ImageIcon(loadIcon("/icons/red.gif")));
			icons.put(Level.ERROR,new ImageIcon(loadIcon("/icons/red.gif")));
			icons.put(Level.WARN,new ImageIcon(loadIcon("/icons/yellow.gif")));
			icons.put(Level.INFO,new ImageIcon(loadIcon("/icons/green.gif")));
			icons.put(Level.DEBUG,new ImageIcon(loadIcon("/icons/green.gif")));
		} catch (NullPointerException e) {
			System.out.println("TextPaneAppender: Unable to load icons");
		}
	}

	public void append(LoggingEvent event) {
		String text = null;
		ThrowableInformation ti = event.getThrowableInformation();
		if (ti != null){
			StringBuffer exbuf = new StringBuffer();
			String[] excDesc = ti.getThrowableStrRep();
			for (int i = 0; i < excDesc.length; i++) {
				exbuf.append(excDesc[i]);
				exbuf.append("\n");
			}
			text = exbuf.toString();
		} else{
			text = this.layout.format(event);
		}
		
		try {
			int overBufferCount = doc.getLength() - maxBufSize;
			if (overBufferCount > 0){
				doc.remove(0, overBufferCount);
				doc.insertString(0, "<< Snip >>", (MutableAttributeSet)attributes.get(Level.INFO) );
			}     	
			if (fancy) {
				textpane.setCaretPosition(doc.getLength());
				//textpane.insertIcon((ImageIcon)icons.get(event.getLevel()));
			}
			
			doc.insertString(doc.getLength(),text,
					(MutableAttributeSet)attributes.get(event.getLevel()));
		}	
		catch (BadLocationException badex) {
			System.err.println(badex);
		}	
		textpane.setCaretPosition(doc.getLength());
	}

	public
	JTextPane getTextPane() {
		return textpane;
	}

	private
	static
	Color parseColor (String v) {
		StringTokenizer st = new StringTokenizer(v,",");
		int val[] = {255,255,255,255};
		int i=0;
		while (st.hasMoreTokens()) {
			val[i]=Integer.parseInt(st.nextToken());
			i++;
		}
		return new Color(val[0],val[1],val[2],val[3]);
	}

	private
	static
	String colorToString(Color c) {
		// alpha component emitted only if not default (255)
		String res = ""+c.getRed()+","+c.getGreen()+","+c.getBlue();
		return c.getAlpha() >= 255 ? res : res + ","+c.getAlpha();
	}

	public
	void setLayout(Layout layout) {
		this.layout=layout;
	}

	public
	void setName(String name) {
		this.name = name;
	}


	public
	void setTextPane(JTextPane textpane) {
		this.textpane=textpane;
		this.doc=textpane.getStyledDocument();
	}

	private
	void setColor(Priority p, String v) {
		StyleConstants.setForeground(
				(MutableAttributeSet)attributes.get(p),parseColor(v));	
	}

	private
	String getColor(Priority p) {
		Color c =  StyleConstants.getForeground(
				(MutableAttributeSet)attributes.get(p));
		return c == null ? null : colorToString(c);
	}

	/////////////////////////////////////////////////////////////////////
	// option setters and getters

	public
	void setLabel(String label) {
		this.label = label;
	}
	public
	String getLabel() {
		return label;
	}

	public
	void setColorEmerg(String color) {
		setColor(Level.FATAL, color);
	}
	public
	String getColorEmerg() {
		return getColor(Level.FATAL);
	}

	public
	void setColorError(String color) {
		setColor(Level.ERROR, color);
	}
	public
	String getColorError() {
		return getColor(Level.ERROR);
	}

	public
	void setColorWarn(String color) {
		setColor(Level.WARN, color);
	}
	public
	String getColorWarn() {
		return getColor(Level.WARN);
	}

	public
	void setColorInfo(String color) {
		setColor(Level.INFO, color);
	}
	public
	String getColorInfo() {
		return getColor(Level.INFO);
	}

	public
	void setColorDebug(String color) {
		setColor(Level.DEBUG, color);
	}
	public
	String getColorDebug() {
		return getColor(Level.DEBUG);
	}

	public
	void setColorBackground(String color) {
		textpane.setBackground(parseColor(color));
	}
	public
	String getColorBackground() {
		return colorToString(textpane.getBackground());
	}

	public
	void setFancy(boolean fancy) {
		this.fancy = fancy;
	}
	public
	boolean getFancy() {
		return fancy;
	}

	public
	void setFontSize(int size) {
		Enumeration e = attributes.elements();
		while (e.hasMoreElements()) {
			StyleConstants.setFontSize((MutableAttributeSet)e.nextElement(),size);
		}
		return;
	}

	public
	int getFontSize() {
		AttributeSet attrSet = (AttributeSet) attributes.get(Level.INFO);
		return StyleConstants.getFontSize(attrSet);
	}

	public
	void setFontName(String name) {
		Enumeration e = attributes.elements();
		while (e.hasMoreElements()) {
			StyleConstants.setFontFamily((MutableAttributeSet)e.nextElement(),name);
		}
		return;
	}

	public
	String getFontName() {
		AttributeSet attrSet = (AttributeSet) attributes.get(Level.INFO);
		return StyleConstants.getFontFamily(attrSet);
	}

	public
	boolean requiresLayout() {
		return true;
	}
} // TextPaneAppender
