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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;
import de.fhg.fokus.se.ethnoarc.dbmanager.helper.SpringUtilities;


/**
 * $Id: DBConnectionPanel.java,v 1.4 2008/07/02 09:58:40 fchristian Exp $
 * Panel to display database connection configuration. 
 * @author fokus
 */
public class DBConnectionPanel extends JPanel implements ActionListener{
	/** The logger */
	static Logger logger = Logger.getLogger(DBConnectionPanel.class.getName());
	private JLabel msg = new JLabel();
	private JPanel topPanel = new JPanel();
	private JPanel mainPanel = new JPanel();
	private CustomTextField tfDBurl;
	private CustomTextField tfDBusername;
	private CustomPasswordField tfDBpwd;

	private JButton butConnect;
	private JButton butClose;
	boolean dbConnectMode=false;
	private String panelmsg="";

	private MainUIFrame mainapp;
	/**
	 * 
	 */
	public static enum COMMANDS {
		Connect,
		Close,
	}
	/**
	 * 
	 * @param mainapp
	 */
	public DBConnectionPanel(MainUIFrame mainapp, boolean dbConnectMode)
	{
		this.mainapp=mainapp;
		this.dbConnectMode=dbConnectMode;
		if(dbConnectMode)
			panelmsg="<html><b>Database Connection Configuration</b><br>"+
			"<font size=\"2\">Please specify the appropriate connection and press <i>Connect</i> to connect to the DB.</font></html>";
		else
			panelmsg="<html><b>DB Manager Login</b><br>"+
			"<font size=\"2\">Please specify login details and press <i>Login</i>.</font></html>";
		initGui();
		setValues();
		JRootPane rootPane = mainapp.getRootPane();
		rootPane.setDefaultButton(butConnect);
		tfDBurl.requestFocus();
	}
	public DBConnectionPanel(DBException e,MainUIFrame mainapp,boolean dbConnectMode)
	{
		this.mainapp=mainapp;
		this.dbConnectMode=dbConnectMode;
		if(!mainapp.isDBAccessible())
			panelmsg = "<html><b>Connection to the database failed:</b><br><font size=\"2\" color=\"red\">"+e.getMessage()+"</font><br><font size=\"2\">Please verify the connection and try again.</font><hr></html>";
		else
			panelmsg = "<html><b>DB Manager Login failed:</b><br><font size=\"2\" color=\"red\">"+e.getMessage()+"</font><br><font size=\"2\">Please verify the login details and try again.</font><hr></html>";

		initGui();
		setValues();
		JRootPane rootPane = mainapp.getRootPane();
		rootPane.setDefaultButton(butConnect);
		tfDBurl.requestFocus();
	}
	public void setFocusUrl()
	{
		if(dbConnectMode)
			tfDBurl.requestFocus();
		else
			tfDBpwd.requestFocus();
	}
	
	public void setError(DBException e,boolean dbConnectMode)
	{
		MainUIFrame.setIsPerformingTask(false);
		this.dbConnectMode=dbConnectMode;
		if(dbConnectMode)
			panelmsg = "<html><b>Connection to the database failed:<br></b><font color=\"red\"><b>"+e.getMessage()+"</b></font><br><font size=\"2\">Please specify the DB login details and try again.</font><hr></html>";
		else
			panelmsg = "<html><b>DB Manager Login failed:</b><br><font size=\"2\" color=\"red\">"+e.getMessage()+"</font><br><font size=\"2\">Please verify the login details and try again.</font><hr></html>";
		msg.setText(panelmsg);
	}
	
	public void actionPerformed(ActionEvent e) {
		switch (COMMANDS.valueOf(e.getActionCommand())) {
		case Connect:
			if(tfDBurl.getText().trim().equals("")|| 
					tfDBusername.getText().trim().equals("")|| 
					String.valueOf(tfDBpwd.getPassword()).equals(""))
			{
				if(dbConnectMode)
					panelmsg = "<html><b>Connection to the database failed:</b><br><font size=\"2\" color=\"red\">Not all values specified.</font><br><font size=\"2\">Please verify the connection and try again.</font><hr></html>";
				else
					panelmsg = "<html><b>DB Manager Login failed:</b><br><font size=\"2\" color=\"red\">Not all values specified.</font><br><font size=\"2\">Please verify the login details and try again.</font><hr></html>";
				msg.setText(panelmsg);
				return;
			}
				mainapp.initDBManager(tfDBurl.getText().trim(), tfDBusername.getText().trim(),String.valueOf(tfDBpwd.getPassword()));
			break;
		case Close:
			mainapp.dispose();
			break;
		}
	}

	private void setValues()
	{
		try {
			AppPropertyManager propManager =AppPropertyManager.getDBPropertyManagerInstant(); 
			tfDBurl.setText(propManager.getDBUrl());

			if(dbConnectMode)
			{
				tfDBurl.setEnabled(true);
				tfDBusername.setText(propManager.getDBUserName());
				tfDBpwd.setText(propManager.getDBPassword());
				butConnect.setText("Connect");
			}
			else
			{
				tfDBurl.setEnabled(false);
				tfDBusername.setText(propManager.getAppUserName());
				butConnect.setText("Login");
			}
		} catch (DBException e) {
			MainUIFrame.setStatusMessage("Error getting properties", MessageLevel.error);
		}
	}
	private void initGui()
	{
		this.setLayout(new BorderLayout());

		//set top panel
		topPanel.add(msg);
		
		msg.setText(panelmsg);
		this.add(topPanel,BorderLayout.NORTH);

		//set main panel
		SpringLayout gl = new SpringLayout();
		
		mainPanel.setLayout(gl);

		// DB URL
		tfDBurl = new CustomTextField(AppConstants.APP_COLOR_DEFAULT);
		JLabel lbDBurl = new JLabel("DB URL", JLabel.TRAILING);
		lbDBurl.setLabelFor(tfDBurl);
		mainPanel.add(lbDBurl);
		tfDBurl.setPreferredSize(new Dimension(350,20));
		tfDBurl.setMaximumSize(new Dimension(350,20));

		mainPanel.add(tfDBurl);

		//DB User Name
		tfDBusername = new CustomTextField(AppConstants.APP_COLOR_DEFAULT);
		JLabel lbDBusername = new JLabel("User Name", JLabel.TRAILING);
		lbDBusername.setLabelFor(tfDBusername);
		mainPanel.add(lbDBusername);
		tfDBusername.setPreferredSize(new Dimension(250,20));
		tfDBusername.setMaximumSize(new Dimension(250,20));
		mainPanel.add(tfDBusername);

		//DB User Name
		tfDBpwd=new CustomPasswordField(AppConstants.APP_COLOR_DEFAULT);
		JLabel lbDBpwd = new JLabel("Password", JLabel.TRAILING);
		lbDBpwd.setLabelFor(tfDBpwd);
		mainPanel.add(lbDBpwd);
		tfDBpwd.setPreferredSize(new Dimension(200,20));
		tfDBpwd.setMaximumSize(new Dimension(200,20));
		mainPanel.add(tfDBpwd);

		//buts
		butConnect = new JButton("Connect");
		butConnect.setToolTipText("Connect to the database & start the application.");
		butConnect.setActionCommand(COMMANDS.Connect.toString());
		butConnect.addActionListener(this);

		butClose=new JButton("Close");
		butConnect.setToolTipText("Close the application.");
		butClose.setActionCommand(COMMANDS.Close.toString());
		butClose.addActionListener(this);

		mainPanel.add(butClose);
		mainPanel.add(butConnect);

//		Lay out the panel.
		SpringUtilities.makeCompactGrid(mainPanel,
				4, 2, //rows, cols
				6, 6,        //initX, initY
				6, 6);       //xPad, yPad

		this.add(mainPanel,BorderLayout.CENTER);
	}
}
