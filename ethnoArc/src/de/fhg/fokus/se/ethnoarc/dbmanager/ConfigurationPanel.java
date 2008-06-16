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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
import de.fhg.fokus.se.ethnoarc.dbmanager.InputFormatableTextField.formatterTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;


public class ConfigurationPanel extends JPanel {
	private MainUIFrame mainapp;
	private Color bg;
	private AppPropertyManager appProperties;
	/** The logger */
	static Logger logger = Logger.getLogger(ConfigurationPanel.class.getName());
	public ConfigurationPanel(MainUIFrame mainapp,Color bg)
	{
		this.mainapp=mainapp;
		this.bg=bg;
		try {
			appProperties=AppPropertyManager.getDBPropertyManagerInstant();
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initGUI();
	}
	private void initGUI()
	{
		this.setLayout(new BorderLayout());
		initTopPanel();
		initMainPanel();
	}
	private JButton butClose;
	private void initTopPanel()
	{
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel bPanel=new JPanel();
		bPanel.setBackground(AppConstants.APP_COLOR_DARK);

		butClose = new JButton("Close");
		butClose.setToolTipText("Close Configuration Panel");
		//butClose.setActionCommand(Commands.Close.toString());
		butClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				mainapp.closeConfigPanel();
			}
		});
		bPanel.add(butClose);

		topPanel.add(bPanel,BorderLayout.NORTH);

		JLabel labTitle=new JLabel("Manage Application Users",SwingConstants.CENTER);
		labTitle.setFont(AppConstants.APP_FONT_TITLE);
		//labTitle.setForeground(Color.blue);

		topPanel.add(labTitle);

		topPanel.add(labTitle,BorderLayout.SOUTH);
		this.add(topPanel,BorderLayout.NORTH);
	}
	private CustomComboBox cbElementType;
	private JCheckBox cbAppSizePos,cbAutocomplete,cbImpliesFeature,cbDisplayTextArea,
		cbDistHelperTable,cbDispEditDescMenu,cbDisplayDeleteButton;
	private JButton butChangePassword;
	private JPasswordField oldPwdField,newPwdField1,newPwdField2;
	private InputFormatableTextField tfvalElementLength,tfvalTAHeight;
	private void initMainPanel()
	{
		JPanel midPanel = new JPanel();
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

		JScrollPane scrollPanelContent = new JScrollPane(midPanel);
		scrollPanelContent.setBorder(null);

		this.add(scrollPanelContent, BorderLayout.CENTER);

		JPanel appConfigPanel = new JPanel();
		TitledBorder b = BorderFactory.createTitledBorder("Application Configuration");
		b.setTitleFont(AppConstants.APP_FONT_FIELDS_BOLD);
		appConfigPanel.setBorder(b);
		appConfigPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		appConfigPanel.setLayout(new BoxLayout(appConfigPanel, BoxLayout.Y_AXIS));
		midPanel.add(appConfigPanel);

		// tooltip config panel
		ConfigItemPanel tooltipPanel = new ConfigItemPanel();

		JLabel tooltipLabel=new JLabel("Tooltip Type");
		tooltipLabel.setToolTipText(MainUIFrame.getToolTipString(
		"Type of description of an element to be displayed as tooltip."));
		tooltipLabel.setFont(AppConstants.APP_FONT_FIELDS_SM);
		tooltipPanel.add(tooltipLabel);

		cbElementType=new CustomComboBox(AppConstants.ElementTooltipOptions.values(),bg);
		cbElementType.setSelectedItem(appProperties.getElementTooltipType());
		cbElementType.setSize(cbElementType.WIDTH,20);

		tooltipPanel.add(cbElementType);

		appConfigPanel.add(tooltipPanel);

		// initial application size and position config panel
		ConfigItemPanel appSizePosPanel = new ConfigItemPanel();

		cbAppSizePos= new JCheckBox("Start with last application size and position.",appProperties.getStartLastSize());
		cbAppSizePos.setToolTipText(MainUIFrame.getToolTipString("Specifies if to start the " +
		"application from the last known size and position or not."));
		cbAppSizePos.setFont(AppConstants.APP_FONT_FIELDS_SM);
		appSizePosPanel.add(cbAppSizePos);
		appConfigPanel.add(appSizePosPanel);

		//	use auto complete feature config panel
		ConfigItemPanel autoCompletePanel = new ConfigItemPanel();

		cbAutocomplete= new JCheckBox("Enable autocomplete feature in combobox.",appProperties.getEnableAutoComplete());
		cbAutocomplete.setToolTipText(MainUIFrame.getToolTipString("Specifies if to enable " +
		"autocomplete feature in combobox."));
		cbAutocomplete.setFont(AppConstants.APP_FONT_FIELDS_SM);
		autoCompletePanel.add(cbAutocomplete);
		appConfigPanel.add(autoCompletePanel);

//		use implies feature config panel
		ConfigItemPanel impliesPanel = new ConfigItemPanel();

		cbImpliesFeature= new JCheckBox("Enable implies feature.",appProperties.getEnableImpliesFeature());
		cbImpliesFeature.setToolTipText(MainUIFrame.getToolTipString("Specifies if to enable " +
		"implies feature."));
		cbImpliesFeature.setFont(AppConstants.APP_FONT_FIELDS_SM);
		impliesPanel.add(cbImpliesFeature);
		appConfigPanel.add(impliesPanel);

		//value element control field length
		ConfigItemPanel vElementLengthPanel = new ConfigItemPanel();
		//vElementLengthPanel.setPreferredSize(new Dimension(200,40));
		JLabel valElmLabel= new JLabel("<html>Value Element Length. (between:"+AppConstants.ELEMENT_VALUE_LENGTH_MIN+" and "+AppConstants.ELEMENT_VALUE_LENGTH_MAX+")</html>");
		valElmLabel.setFont(AppConstants.APP_FONT_FIELDS_SM);
		vElementLengthPanel.add(valElmLabel);

		try {
			tfvalElementLength = new InputFormatableTextField(formatterTypes.RegExp, AppConstants.ELEMENT_VALUE_LENGTH_REGEX,Color.white);
			tfvalElementLength.setColumns(3);
			tfvalElementLength.setText(appProperties.getValueElementLength()+"");
		} catch (ParseException e2) {

		}
		vElementLengthPanel.add(tfvalElementLength);
		appConfigPanel.add(vElementLengthPanel);

		//display text area
		ConfigItemPanel displayTAPanel = new ConfigItemPanel();

		cbDisplayTextArea= new JCheckBox("Display long text in Text Area",appProperties.getDisplayTextArea());
		//cbDisplayTextArea.setToolTipText(MainUIFrame.getToolTipString("Display long " +
		//		"autocomplete feature in combobox."));
		cbDisplayTextArea.setFont(AppConstants.APP_FONT_FIELDS_SM);
		displayTAPanel.add(cbDisplayTextArea);
		appConfigPanel.add(displayTAPanel);

		// Text area height
		ConfigItemPanel taHeightPanel = new ConfigItemPanel();
		//vElementLengthPanel.setPreferredSize(new Dimension(200,40));
		JLabel valTALabel= new JLabel("<html>Text Area heigth. (between:"+AppConstants.ELEMENT_HEIGHT_TEXTAREA_MIN+" and "+AppConstants.ELEMENT_HEIGHT_TEXTAREA_MAX+")</html>");
		valTALabel.setFont(AppConstants.APP_FONT_FIELDS_SM);
		taHeightPanel.add(valTALabel);

		try {
			//tfvalTAHeight = new InputFormatableTextField(formatterTypes.RegExp, "[1-9][0-9]{1,2}",Color.white);
			tfvalTAHeight = new InputFormatableTextField(formatterTypes.RegExp, AppConstants.ELEMENT_TAHEIGHT_REGEX,Color.white);
			tfvalTAHeight.setColumns(3);
			tfvalTAHeight.setText(appProperties.getTextAreaHeight()+"");
		} catch (ParseException e2) {

		}
		taHeightPanel.add(tfvalTAHeight);
		appConfigPanel.add(taHeightPanel);

		//Distinguish helper tables
		ConfigItemPanel distHelperTBPanel = new ConfigItemPanel();

		cbDistHelperTable= new JCheckBox("Distinguish helper tables (e.g., tables with lists) with '*'. " +
				"Requires application restart!",appProperties.getDistinguishHelperTables());

		cbDistHelperTable.setFont(AppConstants.APP_FONT_FIELDS_SM);
		distHelperTBPanel.add(cbDistHelperTable);
		appConfigPanel.add(distHelperTBPanel);

		//Display Edit description menu
		ConfigItemPanel disEditDescMenuPanel = new ConfigItemPanel();

		cbDispEditDescMenu= new JCheckBox("Display popup menu to edit element description. " +
				"Requires application restart!",appProperties.getDisplayEditDescriptionMenu());
		cbDispEditDescMenu.setToolTipText(MainUIFrame.getToolTipStringN("Displays popup menu to edit element description in the DB" +
		"using the right click."));
		cbDispEditDescMenu.setFont(AppConstants.APP_FONT_FIELDS_SM);
		disEditDescMenuPanel.add(cbDispEditDescMenu);
		appConfigPanel.add(disEditDescMenuPanel);
		if(!MainUIFrame.getUserLevel().equals(UserLevels.Admin))
		{
			cbDispEditDescMenu.setText(cbDispEditDescMenu.getText()+" Enabled for users with Admin right.");
			cbDispEditDescMenu.setEnabled(false);
		}
		
		//Display Delete button
		ConfigItemPanel disDeleteButtonPanel = new ConfigItemPanel();

		cbDisplayDeleteButton= new JCheckBox("Display delete button to delete data. " +
				"Requires application restart!",appProperties.getDisplayEditDescriptionMenu());
//		cbDisplayDeleteButton.setToolTipText(MainUIFrame.getToolTipStringN("Display delete button to delete data."));
		cbDisplayDeleteButton.setFont(AppConstants.APP_FONT_FIELDS_SM);
		disDeleteButtonPanel.add(cbDisplayDeleteButton);
		appConfigPanel.add(disDeleteButtonPanel);
		if(!MainUIFrame.getUserLevel().equals(UserLevels.Admin)&&
				!MainUIFrame.getUserLevel().equals(UserLevels.Editor))

		{
			cbDisplayDeleteButton.setText(cbDisplayDeleteButton.getText()+" Enabled for users with Editor or Admin right.");
			cbDisplayDeleteButton.setEnabled(false);
		}

		// Warning lebel
		ConfigItemPanel warnPanel = new ConfigItemPanel();
		final JLabel warnLabel=new JLabel("NOT SAVED");
		warnLabel.setForeground(Color.red);
		warnLabel.setVisible(false);
		warnPanel.add(warnLabel);
		appConfigPanel.add(warnPanel);
		// Update button
		JButton butUpdate=new JButton("Update");
		butUpdate.setToolTipText("Update configuration");
		butUpdate.setFont(AppConstants.APP_FONT_FIELDS);
		butUpdate.setIcon(new ImageIcon("res\\images\\save.gif"));
		butUpdate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				//validate input field for value element field length#
				int l;
				try {
					l = Integer.valueOf(tfvalElementLength.getText()).intValue();
				} catch (NumberFormatException e2) {
					MainUIFrame.setStatusMessage("Lenght of the value field is not an number.",MessageLevel.warn);
					warnLabel.setVisible(true);
					return;
				}

				if(l!=appProperties.getValueElementLength())
				{
					if(l<AppConstants.ELEMENT_VALUE_LENGTH_MIN||l>AppConstants.ELEMENT_VALUE_LENGTH_MAX)
					{
						JOptionPane.showMessageDialog(null, "The value of the lenght of the value element field must be between "+AppConstants.ELEMENT_VALUE_LENGTH_MIN+" and "+AppConstants.ELEMENT_VALUE_LENGTH_MAX);
						tfvalElementLength.setText(appProperties.getValueElementLength()+"");
						//return;
						MainUIFrame.setStatusMessage("Application Configuration NOT updated.",MessageLevel.warn);
						warnLabel.setVisible(true);
						return;
					}
				}

				//validate input field for value element field length#
				int lta;
				try {
					lta = Integer.valueOf(tfvalTAHeight.getText()).intValue();
				} catch (NumberFormatException e2) {
					MainUIFrame.setStatusMessage("Height of the text area is not an number.",MessageLevel.warn);
					warnLabel.setVisible(true);
					return;
				}

				if(l!=appProperties.getTextAreaHeight())
				{
					if(lta<AppConstants.ELEMENT_HEIGHT_TEXTAREA_MIN||lta>AppConstants.ELEMENT_HEIGHT_TEXTAREA_MAX)
					{
						JOptionPane.showMessageDialog(null, "The value of the height of the text area field must be between "+AppConstants.ELEMENT_HEIGHT_TEXTAREA_MIN+" and "+AppConstants.ELEMENT_HEIGHT_TEXTAREA_MAX);
						tfvalTAHeight.setText(appProperties.getTextAreaHeight()+"");
						//return;
						MainUIFrame.setStatusMessage("Application Configuration NOT updated.",MessageLevel.warn);
						warnLabel.setVisible(true);
						return;
					}
				}

				logger.debug("Save config");
				if(appProperties!=null)
					appProperties.setElementTooltipType(AppConstants.ElementTooltipOptions.valueOf(cbElementType.getSelectedItem().toString()));

				appProperties.setStartLastSize(cbAppSizePos.isSelected());
				appProperties.setEnableAutoComplete(cbAutocomplete.isSelected());
				appProperties.setEnableImpliesFeature(cbImpliesFeature.isSelected());
				appProperties.setDisplayTextArea(cbDisplayTextArea.isSelected());
				appProperties.setDistinguishHelperTables(cbDistHelperTable.isSelected());
				if(MainUIFrame.getUserLevel().equals(UserLevels.Admin))
				{
					appProperties.setDisplayEditDescriptionMenu(cbDispEditDescMenu.isSelected());
				}
				if(MainUIFrame.getUserLevel().equals(UserLevels.Admin)||
						MainUIFrame.getUserLevel().equals(UserLevels.Editor))
				{
					logger.error(" Save delete button");
					appProperties.setDisplayDeleteOption(cbDisplayDeleteButton.isSelected());
				}

				try {
					appProperties.setValueElementLength(l);
					appProperties.setTextAreaHeight(lta);
				} catch (DBException e1) {
				}
				warnLabel.setVisible(false);
				MainUIFrame.setStatusMessage("Application Configuration updated.");
			}
		});
		appConfigPanel.add(butUpdate);

		// change password config panel
		if(mainapp.isUserAccountUsed())
		{
			final String username=appProperties.getAppUserName();
			JPanel dbConfigPanel= new JPanel();
			TitledBorder b1=BorderFactory.createTitledBorder("Password Configuration");
			b1.setTitleFont(AppConstants.APP_FONT_FIELDS_BOLD);
			dbConfigPanel.setBorder(b1);
			dbConfigPanel.setLayout(new BoxLayout(dbConfigPanel, BoxLayout.Y_AXIS));
			midPanel.add(dbConfigPanel);

			JLabel infolabel = new JLabel("Change your password. User name: '"+username+"'");
			infolabel.setFont(AppConstants.APP_FONT_MENU);
			dbConfigPanel.add(infolabel);

			ConfigItemPanel oldPwdConfigPanel = new ConfigItemPanel();
			JLabel oldpwd = new JLabel("Old Password: ");
			oldpwd.setPreferredSize(new Dimension(110,22));
			oldpwd.setFont(AppConstants.APP_FONT_FIELDS_SM);

			oldPwdConfigPanel.add(oldpwd);

			oldPwdField = new JPasswordField();
			oldPwdField.setPreferredSize(new Dimension(180,22));
			oldPwdConfigPanel.add(oldPwdField);
			dbConfigPanel.add(oldPwdConfigPanel);

			ConfigItemPanel newPwdConfigPanel = new ConfigItemPanel();
			JLabel newpwd1 = new JLabel("New Password: ");
			newpwd1.setPreferredSize(new Dimension(110,22));
			newpwd1.setFont(AppConstants.APP_FONT_FIELDS_SM);

			newPwdConfigPanel.add(newpwd1);

			newPwdField1 = new JPasswordField();
			newPwdField1.setPreferredSize(new Dimension(180,22));
			newPwdConfigPanel.add(newPwdField1);
			dbConfigPanel.add(newPwdConfigPanel);

			ConfigItemPanel newPwdConfigPanel2 = new ConfigItemPanel();
			JLabel newpwd2 = new JLabel("Confirm New: ");
			newpwd2.setFont(AppConstants.APP_FONT_FIELDS_SM);
			newpwd2.setPreferredSize(new Dimension(110,22));
			newPwdConfigPanel2.add(newpwd2);

			newPwdField2 = new JPasswordField();
			newPwdField2.setPreferredSize(new Dimension(180,22));
			newPwdConfigPanel2.add(newPwdField2);

			dbConfigPanel.add(newPwdConfigPanel2);


			butChangePassword=new JButton("Change Password");
			butChangePassword.setFont(AppConstants.APP_FONT_FIELDS);
			//changePwdConfigPanel.add(butChangePassword);

			dbConfigPanel.add(butChangePassword);

			butChangePassword.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					logger.debug("Update password");
					//verify if all fields are specified
					if(oldPwdField.getPassword().length==0||
							newPwdField1.getPassword().length==0||
							newPwdField2.getPassword().length==0)
					{
						JOptionPane.showMessageDialog(null, "<html>Not all fields specified.<br>" +
								"Please specify all fields and try again.</html>","Error changing password",JOptionPane.ERROR_MESSAGE);
						return;
					}
					logger.error("Check two new passwords");

					if(!Arrays.equals(newPwdField1.getPassword(),newPwdField2.getPassword()))
					{
						JOptionPane.showMessageDialog(null, "<html>New passwords not same.<br>" +
								"Please type new password and try again.</html>","Error changing password",JOptionPane.ERROR_MESSAGE);
						return;
					}
					logger.error("Verify if old password is correct");
					UserManager um;

					um = new UserManager(username,String.valueOf(oldPwdField.getPassword()));
					try {
						if(!um.pwdIsValid())
						{
							JOptionPane.showMessageDialog(null, "<html>Existing password is not correct.<br>" +
									"Please existing password correctly and try again.</html>","Error changing password",JOptionPane.ERROR_MESSAGE);
							return;
						}

						String sql="UPDATE _dbmappuser SET pwd='"+UserManager.hashPassword(String.valueOf(newPwdField1.getPassword()))+"' WHERE username='"+username+"'";
						logger.error("Update password "+sql);
						try {
							DBSqlHandler.getInstance().executeQuery(sql);
							JOptionPane.showMessageDialog(null, "<html>Password is changed.<br>" +
							"Please use this new password to login next time." );
						} catch (Exception e1) {
							logger.warn("Error changing password: "+e1.getMessage());
							MainUIFrame.setStatusMessage("Error changing password: "+e1.getMessage());
						}
					} catch (HeadlessException e1) {
						logger.warn("Error changing password: "+e1.getMessage());
						MainUIFrame.setStatusMessage("Error changing password: "+e1.getMessage());
					} catch (DBException e1) {
						// TODO Auto-generated catch block
						logger.warn("Error changing password: "+e1.getMessage());
						MainUIFrame.setStatusMessage("Error changing password: "+e1.getMessage());
					}
				}
			});
		}
	}

	private class ConfigItemPanel extends JPanel
	{
		public ConfigItemPanel()
		{
			this.setLayout(new FlowLayout(FlowLayout.LEFT,2,0));
			this.setMaximumSize(new Dimension(1000,25));
			this.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
	}
}
