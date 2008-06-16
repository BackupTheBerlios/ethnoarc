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
import java.awt.Component;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.apache.log4j.Logger;

/**
 * $Id: FormattableComboBox.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * @author fokus
 */
public class FormattableComboBox extends BasicComboBoxEditor  {
	static Logger logger = Logger.getLogger(FormattableComboBox.class.getName());

	private InputFormatableTextField tf;
	private Color bg;
	public FormattableComboBox(String formatString,Color bg)
	{
		super();
		this.bg=bg;
		//logger.error("FormattableComboBox: "+formatString);
		try {
			tf=new InputFormatableTextField(formatString,bg);
			tf.addFocusListener(this);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JTextField createEditorComponent() 
	{
		return tf;
	}
	public Component getEditorComponent()
	{
		return tf;
	}
	public void addActionListener(ActionListener l) {
		//logger.error("ADD Action Listern "+l.toString());
		//tf.addActionListener(l);
	}

	public void removeActionListener(ActionListener l) {
		//logger.error("REMOVE ACTION LISTENER");
		//tf.removeActionListener(dInputVerifier);
	}
	public void setItem(Object anObject) {
		if (anObject != null) {
			tf.setText(anObject.toString());
		}
	}
	public Object getItem() { 
		return tf.getText().trim();
	}
}
