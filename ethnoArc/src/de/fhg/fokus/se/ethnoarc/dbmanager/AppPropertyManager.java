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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import de.fhg.fokus.se.ethnoarc.common.DBException;

/**
 * $Id: AppPropertyManager.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $
 * Manages the application properties. 
 * @author fokus
 */
public class AppPropertyManager {
	
	/** The instance of this class. */
	private static AppPropertyManager ethnoarcAppProps;
	/** All application properties */
	private static Properties properties;
	/** The location of the properties. */
	private final String propertyFile="properties/ethnoarcApp.prop"; 
	
	private MainUIFrame mainApp;
	
	/**
	 * Private constructor of the class. Reads the application properties.
	 * @throws IOException Specified property file cannot be found.
	 */
	private AppPropertyManager() throws DBException
	{
		properties = new Properties();
		
		readPropertyFile();
	}
	/**
	 * Gets the instance of the class.
	 * @return The instance of the class.
	 * @throws IOException 
	 */
	public static AppPropertyManager getDBPropertyManagerInstant() throws DBException
	{
		if(ethnoarcAppProps==null)
		{
			ethnoarcAppProps=new AppPropertyManager();
		}
		return ethnoarcAppProps;
	}
	public void setMainApp(MainUIFrame mainApp)
	{
		this.mainApp=mainApp;
	}
	
	/** 
	 * Reads property file.
	 */
	private void readPropertyFile() throws DBException
	{
		try {
			properties.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
			throw new DBException(DBException.APP_PROPERTY_FILE_NOTFOUND);
		}
	}

	/**
	 * Writes property files.
	 */
	public void writePropertyFile()
	{
		try {
			String propComments =
				"ethnoArc DB Manager Application Properties \r\n"+
				"Application and DB connection properties.\r\n\r\n"+
				"ElementTooltip: The property of the element to be used as the tooltip.\r\n"+
				"\tOptions: Description,EnglishDescription,Description_detail,EnglishDescription_detail\r\n"+
				"\tdefault: Description\r\n"+
				"ShowLog: Displays log output in the application UI.\r\n"+
				"\tOptions: true or false. Default: false\r\n"+
				"showDBConfPanel: Displays the DB Configuration panel always at startup.\r\n"+
				"\tOptions: true or false. Default: false\r\n"+
				"separateTableColor: Displays separate color for consequetive child tables.\r\n"+
				"\tOptions: true or false. Default: true\r\n";
			
			properties.store(new FileOutputStream(propertyFile),propComments);
		} catch (IOException e) {
		}
	}
	/**
	 * Gets the value of the specified property.
	 * @param propName The name of the property.
	 * @return The value of the property.
	 */
	public String getPropertyValue(String propName)
	{
		return properties.getProperty(propName);
	}
	
	/**
	 * Gets the value of the DBUrl
	 * @return The database url
	 */
	public String getDBUrl()
	{
		return properties.getProperty(AppConstants.PROP_DBURL);
	}
	/**
	 * Gets the value of the DB User name
	 * @return The database user name.
	 */
	public String getDBUserName()
	{
		return properties.getProperty(AppConstants.PROP_DBUSER_NAME);
	}
	/**
	 * Gets the value of the DB password
	 * @return The database password.
	 */
	public String getDBPassword()
	{
		//return UserManager.
		return properties.getProperty(AppConstants.PROP_DBUSER_PWD);
	}
	/**
	 * Gets the value of the DB Manager application User name
	 * @return The application user name.
	 */
	public String getAppUserName()
	{
		return properties.getProperty(AppConstants.PROP_APPUSER_NAME);
	}
	/**
	 * Sets the value of the DB URL property.
	 * @param The value of the DB URL property.
	 */
	public void setDBUrl(String value)
	{
		properties.setProperty(AppConstants.PROP_DBURL, value);
	}
	/**
	 * Sets the value of the DB Username property.
	 * @param The value of the DB Username property.
	 */
	public void setDBUsername(String value)
	{
		properties.setProperty(AppConstants.PROP_DBUSER_NAME, value);
	}
	/**
	 * Sets the value of the DB password property.
	 * @param The value of the DB password property.
	 */
	public void setDBPassword(String value)
	{
		properties.setProperty(AppConstants.PROP_DBUSER_PWD, value);
	}
	/**
	 * Sets the value of the application Username property.
	 * @param The value of the application Username property.
	 */
	public void setAppUsername(String value)
	{
		properties.setProperty(AppConstants.PROP_APPUSER_NAME, value);
	}
	
	/**
	 * Gets the value of the element tooltip type.
	 * @return The element tooltip type.
	 * @see AppConstants.ElementTooltipOptions
	 */
	public AppConstants.ElementTooltipOptions getElementTooltipType()
	{
		return AppConstants.ElementTooltipOptions.valueOf(properties.getProperty(AppConstants.PROP_ELEMENT_TOOLTIP,AppConstants.ElementTooltipOptions.Description.toString()));
	}
	
	
	/**
	 * Gets the boolean value indicating if to display the log in the UI or not.<br>
	 * Default:<code>false</code> 
	 * @return <code>true</code>: Display log in the UI.<br>
	 * <code>false</code>: Do not display log in the UI.
	 */
	public boolean getShowLog()
	{
		String show= properties.getProperty(AppConstants.PROP_ELEMENT_SHOW_LOG,"false");
		if(show.toLowerCase().equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Gets the boolean value indicating if to enable autocomplete feature in combobox.
	 * @return <code>true</code>: Enable autocomplete.<br>
	 * <code>false</code>: Do not enable autocomplete.
	 */
	public boolean getEnableAutoComplete()
	{
		String show= properties.getProperty(AppConstants.PROP_AUTO_COMPLETE,"true");
		if(show.toLowerCase().equals("true"))
			return true;
		else
			return false;
	}
	/**
	 * Gets the boolean value indicating if to enable implies feature.
	 * @return <code>true</code>: Enable implies feature.<br>
	 * <code>false</code>: Do not enable implies feature.
	 */
	public boolean getEnableImpliesFeature()
	{
		String show= properties.getProperty(AppConstants.PROP_IMPLIES_FEATURE,"true");
		if(show.toLowerCase().equals("true"))
			return true;
		else
			return false;
	}
	/**
	 * Gets the boolean value indicating if to display delete value option or not.<br>
	 * Default value is <code>false</code>. 
	 * 
	 * @return <code>true</code>: Display delete value option.<br>
	 * <code>false</code>: Do not display delete value option.
	 */
	public boolean getDisplayDeleteOption()
	{
//		String show= properties.getProperty(AppConstants.PROP_DISPLAY_DELETE_ICON,"false");
//		if(show.toLowerCase().equals("true"))
//			return true;
//		else
//			return false;
		return false;
		
	}
	/**
	 * Gets the boolean value indicating if to display the DB configuration panel
	 * at startup of the application or not.
	 * @return <code>true</code>: Display DB configuration panel at startup.<br>
	 * <code>false</code>: Do not display DB configuration panel at startup.
	 */
	public boolean getShowDBConfPanelAtStartup()
	{
		String show= properties.getProperty(AppConstants.PROP_ELEMENT_SHOW_DBCONFIG_PANEL,"false");
		if(show.toLowerCase().equals("true"))
			return true;
		else
			return false;
	}
	/**
	 * Separates the color of child tables for ease of use. 
	 * Default: <code>true</code>.
	 * @return <code>true</code>: Distinguish consequetive child tables with separate colors. <br>
	 * <code>false</code>: Child tables in the UI has the same color.
	 */
	public boolean separateConsequetivePanelColor()
	{
		String show= properties.getProperty(AppConstants.PROP_SEPARATE_TABLE_COLORS,"true");
		if(show.toLowerCase().equals("false"))
			return false;
		else
			return true;
	}
	/**
	 * Gets the width of the application when it was last closed. 
	 * @return The width of the application.
	 */
	public int getApplicationWidth()
	{
		String wdth= properties.getProperty(AppConstants.PROP_APP_WIDTH,"-1");
		try{
			return Integer.parseInt(wdth);
		}catch(Exception e)
		{
			return -1;
		}
	}
	/**
	 * Gets the height of the application when it was last closed. 
	 * @return The height of the application.
	 */
	public int getApplicationHeight()
	{
		String wdth= properties.getProperty(AppConstants.PROP_APP_HEIGHT,"-1");
		try{
			return Integer.parseInt(wdth);
		}catch(Exception e)
		{
			return -1;
		}
	}
	/**
	 * Gets the horizontal position of the application when it was last closed. 
	 * @return The horizontal position of the application.
	 */
	public int getApplicationX()
	{
		String value= properties.getProperty(AppConstants.PROP_APP_POSITION_X,"0");
		try{
			return Integer.parseInt(value);
		}catch(Exception e)
		{
			return -1;
		}
	}
	/**
	 * Gets the vertical position of the application when it was last closed. 
	 * @return The vertical position of the application.
	 */
	public int getApplicationY()
	{
		String value= properties.getProperty(AppConstants.PROP_APP_POSITION_Y,"0");
		try{
			return Integer.parseInt(value);
		}catch(Exception e)
		{
			return -1;
		}
	}
	/**
	 * Gets the flag specifying if the application should be started
	 * with the last known size and position.<br>
	 * Default:<code>true</code>
	 * @return <code>true</code>: Start with the last known size and position.<br>
	 * <code>false</code>: Start with default size(700x900) and position (0x0).
	 */
	public boolean getStartLastSize()
	{
		String size= properties.getProperty(AppConstants.PROP_START_LAST_SIZE,"true");
		if(size.toLowerCase().equals("true"))
			return true;
		else
			return false;
	}
	/**
	 * Gets the length of the element value control field. Default value is <code>250</code>.
	 * @return The value of the element field. If exception then returns <code>-1</code>.
	 */
	public int getValueElementLength()
	{
		String value= properties.getProperty(AppConstants.PROP_ELEMENT_VALUE_LENGTH);
		if(value!=null)
		{
			try{
				return Integer.parseInt(value);
			}catch(Exception e)
			{
				return -1;
			}
		}
		else
		{
			try {
				setValueElementLength(AppConstants.ELEMENT_VALUE_LENGTH_DEFAULT);
			} catch (DBException e) {
				
			}
			return -1;
		}
	}
	/**
	 * Gets the flag specifying if to use JTextArea instead of normal JTextField for elements
	 * That has long values.<br>
	 * Default:<code>false</code>
	 * @return <code>true</code>: Displays JTextArea control for elements that has long values.<br>
	 * <code>false</code>: Displays JTextField control.
	 */
	public boolean getDisplayTextArea()
	{
		String val= properties.getProperty(AppConstants.PROP_ELEMENT_VALUE_SHOW_TEXTAREA);
		if(val!=null)
		{
			if(val.toLowerCase().equals("true"))
				return true;
			else
				return false;
		}
		else
		{
			setDisplayTextArea(false);
			return true;
		}
	}
	/**
	 * Gets the height of the text area control field. Default value is <code>50</code>.
	 * @return The value of the element field. If exception then returns <code>-1</code>.
	 */
	public int getTextAreaHeight()
	{
		String value= properties.getProperty(AppConstants.PROP_ELEMENT_HEIGHT_TEXTAREA);
		if(value!=null)
		{
			try{
				return Integer.parseInt(value);
			}catch(Exception e)
			{
				//return AppConstants.ELEMENT_HEIGHT_TEXTAREA_DEFAULT;
			}
		}
		else
		{
			try {
				setTextAreaHeight(AppConstants.ELEMENT_HEIGHT_TEXTAREA_DEFAULT);
			} catch (DBException e) {
				
			}
			
		}
		return AppConstants.ELEMENT_HEIGHT_TEXTAREA_DEFAULT;
	}
	/**
	 * Gets the flag specifying if if to display popup menu to display element 
	 * value in popup text area.<br>
	 * Default:<code>false</code>
	 * @return <code>true</code>: Display popup menu.<br>
	 * <code>false</code>: Do not display poup menu.
	 */
	public boolean getDisplayPopupTextArea()
	{
		String val= properties.getProperty(AppConstants.PROP_ELEMENT_VALUE_SHOW_NEW_TEXTAREA,"false");
		if(val!=null)
		{
			if(val.toLowerCase().equals("true"))
				return true;
			else
				return false;
		}
		else
		{
			//setDisplayTextArea(false);
			return false;
		}
	}
	/**
	 * Gets the flag specifying if if to differentiate helper tables like tables containing
	 * lists from others.<br>
	 * Default:<code>true</code>
	 * @return <code>true</code>: Distinguish.<br>
	 * <code>false</code>: Do not distinguish.
	 */
	public boolean getDistinguishHelperTables()
	{
		String val= properties.getProperty(AppConstants.PROP_DISTINGUISH_HELPER_TABLES,"true");
		if(val!=null)
		{
			if(val.toLowerCase().equals("false"))
				return false;
			else
				return true;
		}
		else
		{
			//setDisplayTextArea(false);
			return true;
		}
	}
	/**
	 * Gets the flag specifying if if to display the popup menu to update description of an element
	 * in the database.<br>
	 * Default:<code>false</code>
	 * @return <code>true</code>: Display the popup menu.<br>
	 * <code>false</code>: Do not display the popup menu.
	 */
	public boolean getDisplayEditDescriptionMenu()
	{
		String val= properties.getProperty(AppConstants.PROP_DISPLAY_EDIT_DESCRIPTION_MENU,"false");
		if(val!=null)
		{
			if(val.toLowerCase().equals("true"))
				return true;
			else
				return false;
		}
		else
		{
			return true;
		}
	}
	/**
	 * Gets the version of the application.
	 * @return The version of the application.
	 */
	public String getApplicationVersion()
	{
		return properties.getProperty(AppConstants.PROP_APP_VERSION);
	}
	/**
	 * Gets the build date of the application.
	 * @return The build date of the application.
	 */
	public String getApplicationBuildDate()
	{
		return properties.getProperty(AppConstants.PROP_APP_BUILD_DATE);
	}
	
	public void setElementTooltipType(AppConstants.ElementTooltipOptions elmTooltip)
	{
		if(!getElementTooltipType().equals(elmTooltip))
		{
			properties.setProperty(AppConstants.PROP_ELEMENT_TOOLTIP,elmTooltip.toString());
			mainApp.updateElementTooltip();
		}
	}
	/**
	 * Sets the property to store width of the application window.
	 * @param width Width of the application window.
	 */
	public void setApplicationWidth(int width)
	{
		properties.setProperty(AppConstants.PROP_APP_WIDTH, width+"");
	}
	/**
	 * Sets the property to store height of the application window.
	 * @param height Height of the application window.
	 */
	public void setApplicationHeight(int height)
	{
		properties.setProperty(AppConstants.PROP_APP_HEIGHT, height+"");
	}
	/**
	 * Sets the value of the property to start the application with
	 * the last known size or not.
	 * @param doStart <code>true</code>: Start with the last known size and position.<br>
	 * <code>false</code>: Start with default size(700x900) and position (0x0).
	 */
	public void setStartLastSize(boolean doStart)
	{
		properties.setProperty(AppConstants.PROP_START_LAST_SIZE, Boolean.toString(doStart));
	}
	/**
	 * Sets the length of the element value control field. The value must be between 200 and 500.
	 * @param The length of the value element.
	 * @throws DBException Specified value out of range.
	 */
	 
	public void setValueElementLength(int value) throws DBException
	{
		if(value<AppConstants.ELEMENT_VALUE_LENGTH_MIN ||value>AppConstants.ELEMENT_VALUE_LENGTH_MAX)
			throw new DBException(DBException.DATA_VALUE_INVALID);
		properties.setProperty(AppConstants.PROP_ELEMENT_VALUE_LENGTH, value+"");
	}
	/**
	 * Sets the boolean value indicating if to display delete value option or not.
	 * @return <code>true</code>: Display delete value option.<br>
	 * <code>false</code>: Do not display delete value option.
	 */
	public void setDisplayDeleteOption(boolean doDisplay)
	{
		properties.setProperty(AppConstants.PROP_DISPLAY_DELETE_ICON,Boolean.toString(doDisplay));
	}
	
	/**
	 * /**
	 * Sets the flag specifying if to use JTextArea instead of normal JTextField for elements
	 * That has long values.<br>
	 * Default:<code>false</code>
	 * @param displayTextArea <code>true</code>: Displays JTextArea control for elements that has long values.<br>
	 * <code>false</code>: Displays JTextField control.
	 */
	
	public void setDisplayTextArea(boolean displayTextArea)
	{
		properties.setProperty(AppConstants.PROP_ELEMENT_VALUE_SHOW_TEXTAREA,Boolean.toString(displayTextArea));
	}
	/**
	 * Sets the height of text area controls. The value must be between 50 and 400.
	 * @param The height of text area controls.
	 * @throws DBException Specified value out of range.
	 */
	 
	public void setTextAreaHeight(int value) throws DBException
	{
		if(value<AppConstants.ELEMENT_HEIGHT_TEXTAREA_MIN ||value>AppConstants.ELEMENT_HEIGHT_TEXTAREA_MAX)
			throw new DBException(DBException.DATA_VALUE_INVALID);
		properties.setProperty(AppConstants.PROP_ELEMENT_HEIGHT_TEXTAREA, value+"");
	}
	/**
	 * Sets the value of the property to enable autocomplete feature in combobox.
	 * @param doenable <code>true</code>: Enable autocomplete feature.<br>
	 * <code>false</code>: Disable autocomplete feature.
	 */
	public void setEnableAutoComplete(boolean doenable)
	{
		properties.setProperty(AppConstants.PROP_AUTO_COMPLETE, Boolean.toString(doenable));
	}
	/**
	 * Sets the value of the property to enable implies feature.
	 * @param doenable <code>true</code>: Enable implies feature.<br>
	 * <code>false</code>: Disable implies feature.
	 */
	public void setEnableImpliesFeature(boolean doenable)
	{
		properties.setProperty(AppConstants.PROP_IMPLIES_FEATURE, Boolean.toString(doenable));
	}
	/**
	 * Sets the value of the property to differentiate helper tables like tables containing
	 * lists from others.
	 * @param val <code>true</code>: Do distinguish.<br>
	 * <code>false</code>: Do not distinguish.
	 */
	public void setDistinguishHelperTables(boolean val)
	{
		properties.setProperty(AppConstants.PROP_DISTINGUISH_HELPER_TABLES, Boolean.toString(val));
	}
	/**
	 * Sets the value of the property to display the popup menu to update description of an element
	 * in the database..
	 * @param val <code>true</code>: Do display the menu.<br>
	 * <code>false</code>: Do not display the menu.
	 */
	public void setDisplayEditDescriptionMenu(boolean val)
	{
		properties.setProperty(AppConstants.PROP_DISPLAY_EDIT_DESCRIPTION_MENU, Boolean.toString(val));
	}
	/**
	 * Sets the property to store horizontal position of the application window.
	 * @param width The horizontal position of the application window.
	 */
	public void setApplicationX(int value)
	{
		properties.setProperty(AppConstants.PROP_APP_POSITION_X, value+"");
	}
	/**
	 * Sets the property to store vertical position of the application window.
	 * @param width The vertical position of the application window.
	 */
	public void setApplicationY(int value)
	{
		properties.setProperty(AppConstants.PROP_APP_POSITION_Y, value+"");
	}
	
}
