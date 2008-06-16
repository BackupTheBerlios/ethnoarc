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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.fhg.fokus.se.ethnoarc.dbmanager.DataFieldUI;

public class TextAreaEditor  extends MouseAdapter implements ActionListener{
	private JPopupMenu popup;
	private boolean editable=false;
	private String title;
	private DataFieldUI comp;
	public TextAreaEditor(DataFieldUI comp,boolean editable,String title)
	{
		this.editable=editable;
		this.title=title;
		this.comp=comp;
		popup=new JPopupMenu();
		
//		Menu change displayname
		menuTextArea = new JMenuItem("View in larger area.");
		menuTextArea.addActionListener(this);

		popup.add(menuTextArea);
	}
	private JMenuItem menuTextArea;
	public void actionPerformed(ActionEvent e) {
		String msg ="",t;
		boolean compIsEnabled = comp.getJComponent().isEnabled();
		if(compIsEnabled)
		 msg="Edit "+title;
		else
			msg="View "+title;
			
		if(comp.getIsExclusiveTakesValueFrom())
			t= TextAreaInput.showDialog(title, comp.getText(), msg,false);
		else
			t= TextAreaInput.showDialog(title, comp.getText(), msg,compIsEnabled);
		
		if(t!=null)
			comp.setText(t);
		
	}	
	public void setText(String text)
	{
		menuTextArea.setText(text);
		popup.setName(text);
	}
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			popup.show(e.getComponent(),
					e.getX(), e.getY());
		}
	}
}
