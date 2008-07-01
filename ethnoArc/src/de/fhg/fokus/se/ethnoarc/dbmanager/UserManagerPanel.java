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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;


/**
 * $Id: UserManagerPanel.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $ 
 * @author fokus
 */
public class UserManagerPanel extends JPanel implements ActionListener {

//	-------- LOGGING -----
	static Logger logger = Logger.getLogger(UserManagerPanel.class.getName());

	private JPanel mainPanel;

	private JButton butClose;
	private JButton butNew;
	private Color bg;
	private MainUIFrame mainapp;
	private enum Commands
	{
		New,
		Close
	}
	public UserManagerPanel(MainUIFrame mainapp,Color bg)
	{
		this.mainapp=mainapp;
		this.bg=bg;
		initGUI(); 
		//get existing user details
		getUsers();
	}

	public void actionPerformed(ActionEvent e) {
		switch (Commands.valueOf(e.getActionCommand())) {
		case New:
			mainPanel.add(new UserDetailPanel(true));
			mainPanel.updateUI();
			break;
		case Close:
			mainapp.closeUserManagerPanel();
			break;
		default:
			break;
		}
	}	
	private void getUsers()
	{
		mainPanel.removeAll();
		String query = "SELECT * FROM _dbmappuser";

		try {
			java.sql.ResultSet rs = DBSqlHandler.getInstance().executeQuery(query);
			String usn,upwd,ul;
			int id;
			while( rs.next() ) {
				try {
					//set the ID
					id=rs.getInt("id");
					usn=rs.getString("username");
					upwd=rs.getString("pwd");
					ul=rs.getString("userlevel");
					//logger.error(id+". usn:"+usn+":"+upwd+":"+ul);
					mainPanel.add(new UserDetailPanel(id,usn,upwd,ul));
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
		} catch (Exception e) {
			String msg="Error getting existing users:";
			if(logger.isDebugEnabled())
				logger.error(msg+":"+e.getMessage());
			else
				logger.error(msg);
			MainUIFrame.setStatusMessage(msg, MessageLevel.error);
		}
	}

	private void initGUI()
	{
		this.setLayout(new BorderLayout());
		initTopPanel();
		initMainPanel();
	}

	private void initTopPanel()
	{
		JPanel topPanel = new JPanel(new BorderLayout());
		
		JPanel bPanel=new JPanel();
		bPanel.setBackground(AppConstants.APP_COLOR_DARK);
		butNew = new JButton("New");
		butNew.setToolTipText("Create new user");
		butNew.setActionCommand(Commands.New.toString());
		butNew.addActionListener(this);
		bPanel.add(butNew);

		butClose = new JButton("Close");
		butClose.setToolTipText("Close User Manager Panel");
		butClose.setActionCommand(Commands.Close.toString());
		butClose.addActionListener(this);
		bPanel.add(butClose);

		topPanel.add(bPanel,BorderLayout.NORTH);
		
		JLabel labTitle=new JLabel("Manage Application Users",SwingConstants.CENTER);
		labTitle.setFont(AppConstants.APP_FONT_TITLE);
		//labTitle.setForeground(Color.blue);
		
		topPanel.add(labTitle);

		topPanel.add(labTitle,BorderLayout.SOUTH);
		this.add(topPanel,BorderLayout.NORTH);
	}
	private void initMainPanel()
	{
		JPanel midPanel=new JPanel(new BorderLayout());
		this.add(midPanel,BorderLayout.CENTER);
		
		JPanel labPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));

		JLabel labUsername=new JLabel("Username");
		labUsername.setPreferredSize(new Dimension(150, 20));
		labUsername.setAlignmentX(Component.RIGHT_ALIGNMENT);
		labUsername.setFont(AppConstants.APP_FONT_FIELDS_BOLD);
		labPanel.add(labUsername);

		JLabel labPassword=new JLabel("Password");
		labPassword.setPreferredSize(new Dimension(150, 20));
		labPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
		labPassword.setFont(AppConstants.APP_FONT_FIELDS_BOLD);
		labPanel.add(labPassword);

		JLabel labLevel=new JLabel("User Level");
		labLevel.setPreferredSize(new Dimension(100, 20));
		labLevel.setAlignmentX(Component.LEFT_ALIGNMENT);
		labLevel.setFont(AppConstants.APP_FONT_FIELDS_BOLD);
		labPanel.add(labLevel);

		midPanel.add(labPanel,BorderLayout.NORTH);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPanelContent = new JScrollPane(mainPanel);
		scrollPanelContent.setBorder(null);
		
		midPanel.add(scrollPanelContent, BorderLayout.CENTER);
	}

	
	private class UserDetailPanel extends JPanel
	{
		private CustomTextField butUsername;
		private CustomPasswordField butPassword;
		private CustomComboBox cbUserLevel;
		private JButton butSave;

		private int id=-1;
		private String username="";
		private String password="";
		private String userLevel="Browser";

		UserDetailPanel(boolean editMode)
		{
			initGui();
		}
		UserDetailPanel(int id,String username, String password, String userLevel)
		{
			this.id=id;
			this.username=username;
			this.password=password;
			this.userLevel=userLevel;
			initGui();
		}
		private void initGui()
		{
			this.setLayout(new FlowLayout(FlowLayout.LEFT,2,0));
			this.setAlignmentX(Component.LEFT_ALIGNMENT);

			this.setMaximumSize(new Dimension(1000,25));

			butUsername = new CustomTextField(bg);
			if(!username.equals(""))
				butUsername.setText(username);
			butUsername.setPreferredSize(new Dimension(150, 22));
			this.add(butUsername);

			butPassword = new CustomPasswordField(bg);
			if(!password.equals(""))
				butPassword.setText("****");
			butPassword.setPreferredSize(new Dimension(150, 22));
			this.add(butPassword);

			cbUserLevel=new CustomComboBox(bg,false);
			cbUserLevel.addItem("Admin");
			cbUserLevel.addItem("Editor");
			cbUserLevel.addItem("Browser");
			cbUserLevel.addItem("BrowserFull");
			cbUserLevel.setEditable(false);

			cbUserLevel.setSelectedItem(userLevel);
			this.add(cbUserLevel);

			butSave=new JButton();
			if(id==-1)
			{
				butSave.setText("Save");
				butUsername.setEnabled(true);
				butPassword.setEnabled(true);
				cbUserLevel.setEnabled(true);
				butNew.setEnabled(false);
			}
			else
			{
				butSave.setText("Remove");
				butUsername.setEnabled(false);
				butPassword.setEnabled(false);
				cbUserLevel.setEnabled(false);
			}
			butSave.setPreferredSize(new Dimension(100, 22));

			butSave.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) {
					//Save New User
					if(id==-1)
					{
						String usn=butUsername.getText().trim();
						String pwd=String.valueOf(butPassword.getPassword()).trim();
						String lev=cbUserLevel.getSelectedItem().toString();
						//check if not empty
						if(usn.equals("")||pwd.equals(""))
						{
							JOptionPane.showMessageDialog(null, "Username or Password empty. Please specify and try again","DBManager Error",JOptionPane.WARNING_MESSAGE);
							return;
						}
						else
						{
							String query="";
							logger.debug("Saving new user data: usn- "+usn+"_pwd-"+pwd+"_level-"+lev);
							
							//Check if username already exists
							query="SELECT COUNT(username) FROM _dbmappuser WHERE username='"+usn+"'";
							try {
								logger.debug("Verify if username already exixt.");
								 String userCount= DBSqlHandler.getInstance().executeGetValue(query);
								 
								 if(!userCount.equals("0"))
								 {
									 String msg="Specified new user "+usn+" already exist. Please specify other username.";
									 JOptionPane.showMessageDialog(null, msg,"DBManager Warning",JOptionPane.WARNING_MESSAGE);
									 butUsername.requestFocus();
									 return;
								 }
								//rs.get
								logger.debug("Username does not exist.");
								
							} catch (DBException e1) {
								String msg ="Error verifying if username exist. ";
								JOptionPane.showMessageDialog(null, msg,"DBManager Error",JOptionPane.ERROR_MESSAGE);
								if(!logger.isDebugEnabled())
									logger.error(msg+e1.getDetailedMsg());
								else
									logger.error(msg+e1.getDetailedMsg(),e1);
								return;
							} catch (Exception e1) {
								String msg ="Error verifying if username exist. ";
								JOptionPane.showMessageDialog(null, msg,"DBManager Error",JOptionPane.ERROR_MESSAGE);
								if(!logger.isDebugEnabled())
									logger.error(msg+e1.getMessage());
								else
									logger.error(msg+e1.getMessage(),e1);
								return;
							}
							
							try {
								pwd=UserManager.hashPassword(pwd);
							} catch (DBException e1) {
								JOptionPane.showMessageDialog(null, "Username or Password empty. Please specify and try again","DBManager Error",JOptionPane.WARNING_MESSAGE);
							}
							query="INSERT INTO _dbmappuser (username,pwd,userlevel) VALUES ('"+usn+"','"+pwd+"','"+lev+"')";
							try {
								DBSqlHandler.getInstance().executeQuery(query);
								MainUIFrame.setStatusMessage("User detail saved.");
								logger.info("User detail saved.");
								getUsers();
								mainPanel.updateUI();
							} catch (DBException e1) {
								JOptionPane.showMessageDialog(null, "Error saving user details.","DBManager Error",JOptionPane.ERROR_MESSAGE);
								if(!logger.isDebugEnabled())
									logger.error("Error saving user details:"+e1.getDetailedMsg());
								else
									logger.error("Error saving user details:"+e1.getDetailedMsg(),e1);
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "Error saving user details.","DBManager Error",JOptionPane.ERROR_MESSAGE);
								if(!logger.isDebugEnabled())
									logger.error("Error saving user details:"+e1.getMessage());
								else
									logger.error("Error saving user details:"+e1.getMessage(),e1);
							}
						}
						butNew.setEnabled(true);
					}
					else // Remove User
					{
						logger.debug("Remove user");
						
						//display warning
						int res=JOptionPane.showConfirmDialog(null, "Do you really want to remove user '"+username+"'", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
						if(res==0)
						{
							String query="DELETE FROM _dbmappuser WHERE id="+id;
							try {
								DBSqlHandler.getInstance().executeQuery(query);
								logger.info("User details removed");
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "Error removing user details.","DBManager Error",JOptionPane.ERROR_MESSAGE);
								if(!logger.isDebugEnabled())
									logger.error("Error removing user details:"+e1.getMessage());
								else
									logger.error("Error removing user details:"+e1.getMessage(),e1);
							}
							getUsers();
							mainPanel.updateUI();
						}
					}
					
				}
			});
			this.add(butSave);
		}
	}
}

