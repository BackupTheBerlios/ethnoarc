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

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * $Id: NaviButtonBase.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * @author fokus
 */
public class NaviButtonBase extends JButton {
	private java.awt.Font butFont = AppConstants.APP_FONT_DEFAULT;
	private Insets butInset = new Insets(0,0,0,0);
	public NaviButtonBase(String butName,String imageName) {
		super();
		setButton(butName);
		setIcon(new ImageIcon("res/images/"+imageName));
	}
	public NaviButtonBase(String butName,String imageName,String disabledImageName,String rolloverImageName) {
		super();
		setButton(butName);
		setIcon(new ImageIcon("res/images/"+imageName));
		setDisabledIcon(new ImageIcon("res/images/"+disabledImageName));
		setRolloverIcon(new ImageIcon("res/images/"+rolloverImageName));
		setRolloverEnabled(true);
	}
	public NaviButtonBase(String butName,String imageName,String disabledImageName) {
		super();
		setButton(butName);
		setIcon(new ImageIcon("res/images/"+imageName));
		setDisabledIcon(new ImageIcon("res/images/"+disabledImageName));
	}
	public NaviButtonBase(String butName) {
		super();
		setButton(butName);
		this.setText(butName);
	}
	private void setButton(String butName)
	{
		this.setName(butName);
		this.setFont(butFont);
		this.setMargin(butInset);
		this.setBorderPainted(false);
		this.setFocusPainted(false);
		//this.setBorder();
		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setEnabled(false);

	}
	public void setToolTipText(String txt)
	{
		super.setToolTipText("<html><body style=\"background-color:#f8f8f8\">"+
				txt+"</html></body>");
	}
}
