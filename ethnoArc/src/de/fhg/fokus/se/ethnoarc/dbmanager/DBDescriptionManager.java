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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;
import de.fhg.fokus.se.ethnoarc.dbmanager.helper.TextAreaInput;


public class DBDescriptionManager extends MouseAdapter implements ActionListener{
	private JPopupMenu popup,tooltip;
	private String tableName,currentDisplayName,currentDescr,currentDescrEnglish,tooltiptext;
	private static Logger logger = Logger.getLogger(DBDescriptionManager.class.getName());
	private enum Commands
	{
		UpdateDisplayname,
		UpdateDescription,
		UpdateEnglishDescription,
		//UpdateLongDescription,
		UpdateAll
	}
	public DBDescriptionManager(String tableName,
			String currentDisplayName,
			String currentDescr,
			String currentDescrEnglish)
	{
		this.tableName=tableName;
		this.currentDisplayName=currentDisplayName;
		this.currentDescr=currentDescr;
		this.currentDescrEnglish=currentDescrEnglish;
		//this.longDescription=longDescription;
		setRightClickPoupup();
		
	}
	
	public DBDescriptionManager(String tableName,
			String currentDisplayName,
			String currentDescr,
			String currentDescrEnglish,
			String tooltiptext)
	{
		this.tableName=tableName;
		this.currentDisplayName=currentDisplayName;
		this.currentDescr=currentDescr;
		this.currentDescrEnglish=currentDescrEnglish;
		this.tooltiptext=tooltiptext;

		setRightClickPoupup();
		setTooltipPopup();
	}
	private void setRightClickPoupup()
	{
		popup=new JPopupMenu();

		//Menu change displayname
		JMenuItem menuDisplayname = new JMenuItem(Commands.UpdateDisplayname.toString());
		JMenuItem menuDescription = new JMenuItem(Commands.UpdateDescription.toString());
		JMenuItem menuDescriptionEnglish = new JMenuItem(Commands.UpdateEnglishDescription.toString());
		//JMenuItem menuDescriptionLong = new JMenuItem(Commands.UpdateLongDescription.toString());
		JMenuItem menuAll = new JMenuItem(Commands.UpdateAll.toString());

		menuDisplayname.addActionListener(this);
		menuDescription.addActionListener(this);
		menuDescriptionEnglish.addActionListener(this);
		menuAll.addActionListener(this);

		popup.add(menuDisplayname);
		popup.add(menuDescription);
		popup.add(menuDescriptionEnglish);
	}
	public void updateTooltip(String tooltip)
	{
		this.tooltiptext=tooltip;
		tooltipLabel.setText(this.tooltiptext);
	}
	private JLabel tooltipLabel;
	private void setTooltipPopup()
	{
		tooltip=new JPopupMenu();
		
		tooltipLabel = new JLabel(tooltiptext);
		tooltip.add(tooltipLabel);
	}
	public void actionPerformed(ActionEvent e) {
		String r="";
		switch (Commands.valueOf(e.getActionCommand())) {
		case UpdateDisplayname:
			r =JOptionPane.showInputDialog("<html>Please specify new display name.<br><font size=2 color='blue'>" +
					"Updates will be displayed when the application is again started.</font><html>",currentDisplayName);

			if(r!=null&&!r.equals(currentDisplayName))
			{
				String query = getQuery("DisplayName", r);
				logger.debug("update Displayname query: "+query);
				updateDetails(query);

				MainUIFrame.setStatusMessage("Displayname changed to '"+r+"'");
				logger.info("Displayname of "+tableName+" changed from '"+currentDisplayName+"' to '"+r+"'");
				currentDisplayName=r;
			}
			break;
		case UpdateDescription:
			//r =JOptionPane.showInputDialog("Please specify new Description.",currentDescr);
			r=TextAreaInput.showDialog("Update Description", currentDescr,"<html>Please specify new description.<br><font size=2 color='blue'>" +
					"Updates will be displayed when the application is again started.</font><html>");
			if(r!=null&&!r.equals(currentDescr))
			{
				String query = getQuery("Description", r);
				logger.debug("update Description query: "+query);
				updateDetails(query);

				MainUIFrame.setStatusMessage("Description changed to '"+r+"'");
				logger.info("Description of "+tableName+" changed from '"+currentDescr+"' to '"+r+"'");
				currentDescr=r;
			}
			break;
		case UpdateEnglishDescription:
			//r =JOptionPane.showInputDialog("Please specify new English Description.",currentDescrEnglish);
			r=TextAreaInput.showDialog("Update English Description", currentDescrEnglish,"<html>Please specify new English description.<br><font size=2 color='blue'>" +
			"Updates will be displayed when the application is again started.</font><html>");
			if(r!=null&&!r.equals(currentDescrEnglish))
			{
				String query = getQuery("EnglishDescription", r);
				logger.debug("update EngDescription query: "+query);
				updateDetails(query);

				MainUIFrame.setStatusMessage("EngDescription changed to '"+r+"'");
				logger.info("Description of "+tableName+" changed from '"+currentDescrEnglish+"' to '"+r+"'");
				currentDescrEnglish=r;
			}
			break;
	/*	case UpdateLongDescription:
			//r =JOptionPane.showInputDialog("Please specify new English Description.",currentDescrEnglish);
			r=TextAreaInput.showDialog("Update Long Description", longDescription,"<html>Please specify new long description.<br><font size=2 color='blue'>" +
			"Updates will be displayed when the application is again started.</font><html>");
			if(r!=null&&!r.equals(currentDescrEnglish))
			{
				String query = getQuery("EnglishDescription", r);
				logger.debug("update EngDescription query: "+query);
				updateDetails(query);

				MainUIFrame.setStatusMessage("EngDescription changed to '"+r+"'");
				logger.info("Description of "+tableName+" changed from '"+currentDescrEnglish+"' to '"+r+"'");
				currentDescrEnglish=r;
			}
			break;*/
		case UpdateAll:
			System.out.println("update All: "+currentDisplayName+"_"+currentDescr+"_"+currentDescrEnglish);
			break;
		default:
			break;
		}
	}

	private String getQuery(String columnName,String updatedValue)
	{
		return "UPDATE databasedescription SET "+columnName+"='"+updatedValue+"' WHERE tablename ='"+tableName+"'";
	}

	private void updateDetails(String query)
	{
		try {
			DBSqlHandler.getInstance().executeQuery(query);
		} catch (DBException e) {
			String msg="Error updating details.";
			if(logger.isDebugEnabled())
			{
				MainUIFrame.setStatusMessage(msg+e.getMessage(), MessageLevel.error);
				logger.error(msg+":"+e);
			}
			else
			{
				MainUIFrame.setStatusMessage(msg, MessageLevel.error);
				logger.error(msg+":"+e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		if(tooltip!=null)
		tooltip.show(e.getComponent(),
				e.getX(), e.getY());
	}
	public void mouseExited(MouseEvent e)
	{
		if(tooltip!=null)
		tooltip.setVisible(false);
	}
	
	
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) 
		{
			popup.show(e.getComponent(),
					e.getX(), e.getY());
		}
	}
	
}
