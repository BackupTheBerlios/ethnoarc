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

import javax.swing.JPasswordField;
/**
 * $Id: CustomPasswordField.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus
 */
public class CustomPasswordField extends JPasswordField implements FocusListener{
	Color bg;
	public CustomPasswordField(Color bg)
	{
		super();
		this.bg=bg;
		this.setDisabledTextColor(AppConstants.APP_COLOR_FONT_DISABLED);
		this.addFocusListener(this);
	}
	public void setEnabled(boolean enabled)
	{ 
		super.setEnabled(enabled);
		if(enabled)
			this.setBackground(Color.white);
		else
		{
			this.setBackground(bg);
		}
	}
	public void focusGained(FocusEvent arg0) {
		this.selectAll();	
	}
	public void focusLost(FocusEvent arg0) {
		// ignore
	}
}