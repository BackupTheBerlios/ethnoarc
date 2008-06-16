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
package de.fhg.fokus.se.ethnoarc.dbmanager;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import org.apache.log4j.Logger;

/**
 * $Id: InputFormatableTextField.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus
 */
public class InputFormatableTextField extends JFormattedTextField implements FocusListener{
	static Logger logger = Logger.getLogger(InputFormatableTextField.class.getName());

	/** A single numeric digit (0-9) */
	private static final String DB_IDENTIFIER_NUMERIC_SINGLE ="n";
	/** An arbitrary number of numeric digits (0-9) */
	private static final String DB_IDENTIFIER_NUMERIC ="N";
	/** An arbitrary number of characters (a-z) */
	private static final String DB_IDENTIFIER_CHAR ="A";
	/** A single character (a-z) */
	private static final String DB_IDENTIFIER_CHAR_SINGLE ="a";
	/** Day (numeric) */
	private static final String DB_IDENTIFIER_DAY="dd";
	/** Month (numeric). */
	private static final String DB_IDENTIFIER_MONTH_N="mm";
	/** Month three letter abbreviation. */
	private static final String DB_IDENTIFIER_MONTH="mon";
	/** Month in word */
	private static final String DB_IDENTIFIER_MONTH_FULL="month";
	/** Year two digit numeric */
	private static final String DB_IDENTIFIER_YEAR="yy";
	/** Year four digit numeric */
	private static final String DB_IDENTIFIER_YEAR_FOUR="yyyy";
	/** Hour numeric 24 hours */
	private static final String DB_IDENTIFIER_HOUR="hh";
	/** Minutes (Numeric) */
	private static final String DB_IDENTIFIER_MIN="mi";
	/** Seconds numeric */
	private static final String DB_IDENTIFIER_SECOND="ss";
	/** URL */
	private static final String DB_IDENTIFIER_URL="URL";
	/** URI */
	private static final String DB_IDENTIFIER_URI="URI";

	/**
	 * Types of formatter. Specified format text is classified as one of these <code>formatterTypes</code> to create a formatter.
	 */
	public enum formatterTypes{
		/** Masked formatter for fixed numerical or character formats.*/
		Masked,
		/** DateTime formatter for date time field. */
		DateTime,
		/** RegexFormatter for complex format. */
		RegExp,
		Unknown
	}

	private formatterTypes formatterType;
	private Color bg;
	private String formatText;
	public InputFormatableTextField(String formatText, Color bg) throws ParseException
	{
		this.formatText=formatText;
		this.bg=bg;
		if(MainUIFrame.getApplicationMode().equals(AppConstants.ApplicationModes.DataEntry))
			parseFieldFormat();
		this.setDisabledTextColor(AppConstants.APP_COLOR_FONT_DISABLED);
		this.addFocusListener(this);
	}
	public InputFormatableTextField(formatterTypes formatterType, String formatText, Color bg) throws ParseException
	{
		this.formatterType=formatterType;
		this.formatText=formatText;
		this.bg=bg;
		//if(MainUIFrame.getApplicationMode().equals(AppConstants.ApplicationModes.DataEntry))
		//	parseFieldFormat();
		this.setDisabledTextColor(AppConstants.APP_COLOR_FONT_DISABLED);
		this.addFocusListener(this);
		setFormatter();
	}
	/**
	 * Parses the specified format for the field to identify the type of format and the sets the 
	 * identified format type in the param <code>formatType</code>.
	 * @see formatterTypes
	 * @see formatType
	 * @param fieldFormat Specified field format string.
	 */
	private void parseFieldFormat()
	{	
		if(formatText.equals(DB_IDENTIFIER_URL))
		{
			formatterType = formatterTypes.RegExp;
			formatText= "^(ftp|http|https|file)://([^/]+)(/.*)?(/.*)";
		}
		else if(formatText.equals(DB_IDENTIFIER_URI))
		{
			formatterType = formatterTypes.RegExp;
			formatText= "^(ftp|http|https|file)://([^/]+)(/.*)?(/.*)";
		}
		//check if the format contains datatime format
		else if(formatText.contains(DB_IDENTIFIER_DAY)||
				formatText.contains(DB_IDENTIFIER_MONTH_N)||
				formatText.contains(DB_IDENTIFIER_MONTH)||
				formatText.contains(DB_IDENTIFIER_MONTH_FULL)||
				formatText.contains(DB_IDENTIFIER_YEAR)||
				formatText.contains(DB_IDENTIFIER_YEAR_FOUR)||
				formatText.contains(DB_IDENTIFIER_HOUR)||
				formatText.contains(DB_IDENTIFIER_MIN)||
				formatText.contains(DB_IDENTIFIER_SECOND))
		{
			formatText=formatText.replace(DB_IDENTIFIER_MONTH, "MMM");
			formatText=formatText.replace(DB_IDENTIFIER_MONTH_N, "MM");
			formatText=formatText.replace(DB_IDENTIFIER_MIN, "mm");
			if(!formatText.contains(DB_IDENTIFIER_CHAR)&&
					!formatText.contains(DB_IDENTIFIER_CHAR_SINGLE)&&
					!formatText.contains(DB_IDENTIFIER_NUMERIC)&&
					!formatText.contains(DB_IDENTIFIER_NUMERIC_SINGLE))
			{
				formatterType=formatterTypes.DateTime;
			}
			else
			{
				formatterType=formatterTypes.RegExp;
				//TODO: specify regular expression
			}
		}
		else if(formatText.contains(DB_IDENTIFIER_NUMERIC)||
				formatText.contains(DB_IDENTIFIER_CHAR))
		{
			formatterType=formatterTypes.RegExp;
			formatText=formatText.replace(DB_IDENTIFIER_NUMERIC, "\\d*");
			formatText=formatText.replace(DB_IDENTIFIER_CHAR, "\\D*");
			formatText=formatText.replace(DB_IDENTIFIER_NUMERIC_SINGLE, "\\d");
			formatText=formatText.replace(DB_IDENTIFIER_CHAR_SINGLE, "\\D");
		}
		else if(formatText.contains(DB_IDENTIFIER_CHAR_SINGLE))
		{
			formatterType=formatterTypes.Masked;
			formatText=formatText.replace(DB_IDENTIFIER_CHAR_SINGLE, "?");
			formatText=formatText.replace(DB_IDENTIFIER_NUMERIC_SINGLE, "#");
		}
		else if(formatText.contains(DB_IDENTIFIER_NUMERIC_SINGLE))
		{
			formatterType=formatterTypes.Masked;
			formatText=formatText.replace(DB_IDENTIFIER_CHAR_SINGLE, "?");
			formatText=formatText.replace(DB_IDENTIFIER_NUMERIC_SINGLE, "#");
		}
		else
		{
			formatterType=formatterTypes.Unknown;
		}
		//logger.error(formatText+" FORMA TYPE: "+formatterType);
		setFormatter();

	}
	private void setFormatter()
	{
		switch (formatterType) {
		case Masked:
			try {
				if(logger.isDebugEnabled())
					logger.debug("------- Masked Formatter '"+formatText+"'");
				MaskFormatter formatter = new MaskFormatter(formatText);
				this.setFormatterFactory(new DefaultFormatterFactory(formatter));
				this.setInputVerifier(new DataInputVerifier(formatter,this));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case RegExp:
			if(logger.isDebugEnabled())
				logger.debug("------- Regular Formatter '"+formatText+"' ");
			RegexFormatter formatter = new RegexFormatter(formatText);
			formatter.setAllowsInvalid(false);
			this.setInputVerifier(new DataInputVerifier(formatter,this));
			break;
		case DateTime:
			if(logger.isDebugEnabled())
				logger.debug("------- DateTIME Formatter '"+formatText+"'");


			String inpformatText=formatText.replace("dd","##");
			inpformatText=inpformatText.replace("MMM","UAA");
			inpformatText=inpformatText.replace("MM","##");
			inpformatText=inpformatText.replace("yyyy","####");
			inpformatText=inpformatText.replace("jjjj","####");
			inpformatText=inpformatText.replace("yy","##");
			inpformatText=inpformatText.replace("jj","##");
			inpformatText=inpformatText.replace("hh","##");
			inpformatText=inpformatText.replace("mm","##");
			inpformatText=inpformatText.replace("ss","##");

			formatText=formatText.replace("j", "y");

			try{
				MaskFormatter inpformatter = new MaskFormatter(inpformatText);
				//inpformatter.setPlaceholder("   :1900");
				this.setFormatterFactory(new DefaultFormatterFactory(inpformatter));

				DateFormatter validatorformatter = new DateFormatter(new SimpleDateFormat(formatText));
				this.setInputVerifier(new DataInputVerifier(validatorformatter,this));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if(enabled)
			this.setBackground(Color.white);
		else
		{
			//logger.error("COLOR '"+this.getParent().getBackground());
			this.setBackground(bg);
		}
	}
	public class DataInputVerifier extends InputVerifier //implements ActionListener
	{
		Boolean isValid=false;
		AbstractFormatter fieldFormatter;
		JTextField f;
		public DataInputVerifier(AbstractFormatter	fieldFormatter,JTextField f)
		{
			//logger.error("INPUT VERIFIER "+fieldFormatter.getMask());
			this.fieldFormatter=fieldFormatter;
			this.f=f;
		}
		public boolean verify(JComponent input) {
			try {
				Object o = fieldFormatter.stringToValue(f.getText());
				
//				f.setText(o.toString());
				return true;
			} catch (ParseException e) {
				return false;
			}
		}
		public boolean shouldYieldFocus(JComponent input) {
			boolean inputOK = verify(input);
			logger.debug("YIELD "+inputOK);
			if (inputOK) {
				input.setBackground(Color.WHITE);
				return true;
			} else {
				input.setBackground(Color.YELLOW);
				MainUIFrame.setStatusMessage("Data format mismatch.",MainUIFrame.MessageLevel.warn);
				/*if(MainUIFrame.getApplicationMode().equals(AppConstants.ApplicationModes.Search))
					return true;
				else*/
				return false;
			}
		}
	}
	public void focusGained(FocusEvent arg0) {
		this.selectAll();	

	}
	public void focusLost(FocusEvent arg0) {
		// ignore
	}
}
