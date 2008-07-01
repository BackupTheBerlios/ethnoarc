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
import java.awt.Font;

/**
 * $Id: AppConstants.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $
 * 
 * EthnoArc DB application related contants.
 */
public class AppConstants {
	/** The name of the property database URL. */
	public static final String PROP_DBURL = "DBUrl";
	/** The name of the property database user name. */
	public static final String PROP_DBUSER_NAME = "DBUserName";
	/** The name of the property database password. */
	public static final String PROP_DBUSER_PWD = "DBPassword";
	/**  The name of the table with users details. */
	public static final String DB_USER_LEVEL_TABLE_NAME="_dbmappuser";
	
	/** The name of the property application username. */
	public static final String PROP_APPUSER_NAME = "AppUserName";
	
	public static final String VIEW_PREFIX="view_";
	
	/** 
	 * The name of the property element tooltip to specify which property to
	 * be shown as the property. 
	 */
	public static final String PROP_ELEMENT_TOOLTIP = "ElementTooltip";
	
	/**
	 * The name of the property to specify if auto complete feature in
	 * Combobox is enabled.
	 */
	public static final String PROP_AUTO_COMPLETE = "autocomplete";
	
	/**
	 * The name of the property to specify if enable implies feature.
	 */
	public static final String PROP_IMPLIES_FEATURE = "enableImpliesFeature";
	
	/**
	 * The name of the property to specify if the application to be started
	 * with the last known size and position.
	 */
	public static final String PROP_START_LAST_SIZE = "startLastSize";
	
	/** 
	 * The name of the property to specify if to display log in the UI or not. 
	 */
	public static final String PROP_ELEMENT_SHOW_LOG="ShowLog";
	/** 
	 * The name of the property to specify if to display DB
	 * config pananel at startup or not. 
	 */
	public static final String PROP_ELEMENT_SHOW_DBCONFIG_PANEL="showDBConfPanel";
	
	/**
	 * The name of the property to display tables in different colors in UI.
	 */
	public static final String PROP_SEPARATE_TABLE_COLORS="separateTableColor";
	
	/**
	 * The name of the property to display delete icon to delete values.
	 */
	public static final String PROP_DISPLAY_DELETE_ICON="displayDeleteIcon";

	
	/**
	 * The name of the property to store the width of the application.
	 */
	public static final String PROP_APP_WIDTH="width";
	
	/**
	 * The name of the property to store the heigth of the application.
	 */
	public static final String PROP_APP_HEIGHT="height";
	
	/**
	 * The horizontal position of the top left part of the application in the desktop.
	 */
	public static final String PROP_APP_POSITION_X="appX";
	
	/**
	 * The vertical position of the top left part of the application in the desktop.
	 */
	public static final String PROP_APP_POSITION_Y="appY";
	
	/**
	 * The name of the property to store the version of the application.
	 */
	public static final String PROP_APP_VERSION="version";
	
	/**
	 * The name of the property to store the build date of the application.
	 */
	public static final String PROP_APP_BUILD_DATE="builddate";
	
	/**
	 * The name of the property to store the length of the element value control field.
	 */
	public static final String PROP_ELEMENT_VALUE_LENGTH="valuelength";
	
	/**
	 * The name of the property to use Text Area for elements with long values.
	 */
	public static final String PROP_ELEMENT_VALUE_SHOW_TEXTAREA="displaytextarea";
	
	/**
	 * The name of the property to specify if to display popup menu to display element 
	 * value in popup text area.
	 */
	public static final String PROP_ELEMENT_VALUE_SHOW_NEW_TEXTAREA="displaypopupta";
	/**
	 * The name of the property to specify if to differentiate helper tables like tables containing
	 * lists from others.
	 */
	public static final String PROP_DISTINGUISH_HELPER_TABLES="distinguishHelperTables";
	
	/**
	 * The name of the property to specify if to display the popup menu to update description of an element
	 * in the database.
	 */
	public static final String PROP_DISPLAY_EDIT_DESCRIPTION_MENU="displayEditDescription";
	
	
	/**
	 * The default value of the length of the element value control field. 
	 */
	public static final int ELEMENT_VALUE_LENGTH_DEFAULT=250;
	/**
	 * The minimum value of the length of the element value control field. 
	 */
	public static final int ELEMENT_VALUE_LENGTH_MIN=200;
	
	/**
	 * The maximum value of the length of the element value control field. 
	 */
	public static final int ELEMENT_VALUE_LENGTH_MAX=499;
	
	/**
	 * Regular expression to validate if the specified element length is between
	 * allowed minimum and maximum values.
	 */
	public static final String ELEMENT_VALUE_LENGTH_REGEX="[2-4][0-9]{1,2}";
	
	/**
	 * The name of the property to specify the height of Text Area for elements with long values.
	 */
	public static final String PROP_ELEMENT_HEIGHT_TEXTAREA="taheight";
	/**
	 * The minimum height of the text area to display value of elements.
	 */
	public static final int ELEMENT_HEIGHT_TEXTAREA_MIN=50;
	/**
	 * The maximum height of the text area to display value of elements.
	 */

	public static final int ELEMENT_HEIGHT_TEXTAREA_MAX=400;
	/**
	 * Regular expression to validate if the specified text area height is between
	 * allowed minimum and maximum values.
	 */
	public static final String ELEMENT_TAHEIGHT_REGEX="\\d*";
	/**
	 * The default height of the text area to display value of elements.
	 */
	public static final int ELEMENT_HEIGHT_TEXTAREA_DEFAULT=50;
	/**
	 * The normal height of element fields.
	 */
	public static final int ELEMENT_HEIGHT_NORMAL=20;
	
	/**
	 * Default application background color
	 */
	public static final Color APP_COLOR_DEFAULT=new Color(238,238,238);
	public static final Color APP_COLOR_LIGHT=new Color(250,250,250);
	public static final Color APP_COLOR_DARK=new Color(175,175,175);
	public static final Color APP_COLOR_BUTTON_BORDER=new Color(204,236,254);
	public static final Color APP_COLOR_FONT_DISABLED=Color.gray;
	public static final Color APP_COLOR_FIELD_BORDER=new Color(184,207,229);
	public static final Color APP_COLOR_ALTs=new Color(219,255,247);
	public static final Font APP_FONT_DEFAULT=new java.awt.Font("Dialog",0,10);
	//public static final Font APP_FONT_DEFAULT_BOLD=new java.awt.Font("Dialog",1,10);
	public static final Font APP_FONT_FIELDS=new java.awt.Font("Dialog",0,12);
	public static final Font APP_FONT_FIELDS_BOLD=new java.awt.Font("Dialog",1,11);
	public static final Font APP_FONT_FIELDS_SM=new java.awt.Font("Dialog",0,11);
	public static final Font APP_FONT_MENU=new java.awt.Font("Dialog",0,12);
	public static final Font APP_FONT_MENU_BOLD=new java.awt.Font("Dialog",1,12);
	public static final Font APP_FONT_TITLE=new java.awt.Font("Dialog",1,15);
	
	//public static final Color APP_COLOR_SEARCH=new Color(255,184,178);
	//public static final Color APP_COLOR_DESIGN=new Color(253,229,255);
	
	/**
	 * Options for tooltips.
	 */
	public static enum ElementTooltipOptions
	{
		/** Normal description as the tooltip. */
		Description,
		/** English description as the tooltip. */
		EnglishDescription,
		/** Normal description and other details as the tooltip. */
		Description_detail,
		/** English description and other details as the tooltip. */
		EnglishDescription_detail
	}
	/**
	 * Different modes of the application.<p>
	 * <li><code>NoDBConnection</code>: There is no connection to the database. 
	 * Displays panel to configure DB connection.
	 * <li><code>DataEntry</code>: Configures UI to manage data in the data.
	 * <li><code>Search</code>: Configures UI to query database.
	 * <li><code>DBUpdate</code>: Configures UI to update the database schema.
	 */
	public static enum ApplicationModes
	{
		/** There is no connection to the database. Displays panel to configure DB connection. */
		NoDBConnection,
		/** Configures UI to manage data in the data. */
		DataEntry,
		/** Configures UI to query database. */
		Search,
		/** Configures UI to update the database schema. */
		DBUpdate,
		/** Mode to configure application users. */
		UserManagement,
		/**  Application configuration mode. */
		Configuration
	}
	
	/**
	 * User levels of the DB Manager applications.
	 */
	public static enum UserLevels
	{
		/** 
		 * Has rights as an <code>Editor</code> plus can manage users and 
		 * high level DB management (change <code>publicDefault</code> value, 
		 * change database structure.).
		 */
		Admin,
		/** Can brose and search data like an <code>BrowserFull</code> but also edit data. */
		Editor,
		/** Can browse and search data. Can view locked data.*/
		BrowserFull,
		/** Can browse and search data. Cannot view locked data. */
		Browser
	}
}
