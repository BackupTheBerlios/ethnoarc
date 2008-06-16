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

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JRadioButton;

public class CustomRadioButton extends JRadioButton{
	private String name;
	public CustomRadioButton()
	{
		super();
		setDefaults();
	}
	public CustomRadioButton(String name,ImageIcon icon,ImageIcon actIcon, ImageIcon disIcon,ImageIcon rollIcon)
	{
		super(name,icon);
		this.name=name;
		this.setSelectedIcon(actIcon);
		this.setDisabledIcon(disIcon);
		this.setDisabledSelectedIcon(actIcon);
		this.setRolloverIcon(rollIcon);
		setDefaults();
	}
	private void setDefaults()
	{
		this.setBackground(AppConstants.APP_COLOR_DARK);
		this.setPreferredSize(new Dimension(130,23));
		this.setFont(AppConstants.APP_FONT_MENU_BOLD);
		this.setRolloverEnabled(true);
		this.setFocusPainted(false);
		this.setBorder(null);
		this.setBorderPainted(false);
		
		//this.setBorder(BorderFactory.createLoweredBevelBorder());
		//this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED,AppConstants.APP_COLOR_DEFAULT,AppConstants.APP_COLOR_DARK));
	}
	public void setText(String text)
	{
		name=text;
		super.setText(text);
	}
	public void setSelected(boolean b)
	{
		super.setSelected(b);
		if(b)
			this.setBackground(AppConstants.APP_COLOR_DEFAULT);
		else
			this.setBackground(AppConstants.APP_COLOR_DARK);
	}
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		if(!b)
		{
			this.setText("<html><body color=\"#000000\">"+ name+"</body></html>");
			//this.setBackground(AppConstants.APP_COLOR_DEFAULT);
			//this.setBorderPainted(true);
		}
		else
		{
			this.setText(name);
			//this.setBackground(AppConstants.APP_COLOR_DARK);
			//this.setBorderPainted(false);
		}
	}
}
