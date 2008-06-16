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

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaInput {
	public static String showDialog(String title, String orgText,String msg){
		return showDialog(title,orgText,msg,true);
	}
	public static String showDialog(String title, String orgText,String msg,boolean editable){
		JTextArea area = new JTextArea(10, 15);
		area.setLineWrap(true);
		area.setText(orgText);
		
		area.setDisabledTextColor(Color.black);
		int opt=JOptionPane.OK_CANCEL_OPTION;
		int optPaneMsg=JOptionPane.QUESTION_MESSAGE;
		if(!editable)
		{
			area.setEnabled(false);
			opt=JOptionPane.CLOSED_OPTION;
			optPaneMsg=JOptionPane.INFORMATION_MESSAGE;
		}
		
		JScrollPane pane = new JScrollPane(area);
		
		int result = JOptionPane.showOptionDialog(
				null,
				new Object[] {msg, pane},
				title,
				opt,
				optPaneMsg,
				null, null, null);

		if (result == JOptionPane.OK_OPTION) {
			return area.getText();
		}
		else
			return null;
	}
}

