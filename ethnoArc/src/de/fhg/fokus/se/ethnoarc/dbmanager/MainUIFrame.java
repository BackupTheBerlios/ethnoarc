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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.DBElementTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.ApplicationModes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;

/**
 * $Id: MainUIFrame.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $
 * 
 * @author fokus
 */
public class MainUIFrame extends javax.swing.JFrame implements ActionListener {

	private static MainUIFrame appFrame;

	/** The logger */
	static Logger logger = Logger.getLogger(MainUIFrame.class.getName());

	private JPanel infoPanel;

	private JPanel menuPanel;

	private JPanel mainPanel;

	private static JTabbedPane mainTabbedPane;

	private JPanel statusPanel;

	private JLabel prjLabels;

	private static JLabel statusBarLabel;

	private AppPropertyManager appPropertyManager;

	private LogPanel logpanel;

	private DBConnectionPanel dbConnPanel;

	private UserManagerPanel userPanel;

	private ConfigurationPanel configPanel;

	/**
	 * The current mode of the application.
	 * 
	 * @see ApplicationModes
	 */
	private static ApplicationModes appMode = ApplicationModes.DataEntry;

	private static ApplicationModes lastAppMode;

	/**
	 * The flag indicating if the UI is in Edit mode.
	 */
	private static boolean uiEditMode = false;

	/**
	 * List of tablePanels. Each table including child tables has its own
	 * tablePanel.
	 */
	Hashtable<String, TablePanel> tablePanels = new Hashtable<String, TablePanel>();

	/**
	 * Stores all parent structure of all elements(e.g., '<code>Adressenliste.Adresse.Land</code>', '<code>Adressenliste.Adresse.Person.Vorname</code>')
	 * including the elements of the child tables. Used when searching for a
	 * data.
	 */
	private Hashtable<String, Vector<String>> topLevelTableElements = new Hashtable<String, Vector<String>>();

	private Hashtable<String, ElementPanel> dbElements = new Hashtable<String, ElementPanel>();

	/**
	 * Stores <code>FROM</code> part of the sql string for search purpose.
	 * Example: <br>
	 * <code>FROM v_Adressenliste, v_Person, v_Adresse </code>
	 */
	private static Hashtable<String, String> searchStringPartFrom = new Hashtable<String, String>();

	/**
	 * Stores <code>WHERE</code> part of the sql string for search purpose.
	 * Example: <br>
	 * <code>WHERE  v_Adresse.PersonID=v_Person.ID AND v_Adressenliste.AdresseID=v_Adresse.ID</code>
	 */
	private static Hashtable<String, String> searchStringPartWhere = new Hashtable<String, String>();

	/**
	 * List of search parameters. The key of the hashtable specifies the element
	 * name (in DB) and the value specifies the value to be searched. E.g.,
	 * <code>Stadt</code>:<code>Berlin</code>.
	 */
	private Hashtable<String, SearchParams> searchList = new Hashtable<String, SearchParams>();

	/**
	 * The user level of the logged-in user.
	 */
	private static UserLevels userLevel = UserLevels.Browser;

	/**
	 * If the data base is accessible.
	 */
	private boolean dbAccessible;

	/**
	 * Specifies if the user account was used or general database account was used for
	 * application login.
	 */
	private boolean userLogin=false;

	// -------------- Initialisation ----------------------------------------
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainUIFrame();
			}
		});
	}

	public MainUIFrame() {

		super();
		appFrame = this;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// for ui purpoes
		addListeners();

		// get application properties
		try {
			appPropertyManager = AppPropertyManager
			.getDBPropertyManagerInstant();
			appPropertyManager.setMainApp(this);
			// logger.error("PROPERTIES "+appProperties.getProperty("DBUrl"));
		} catch (DBException e1) {
			logger.error("Error reading property file: " + e1.getMessage());
			JOptionPane.showMessageDialog(this,
					"Property file not found. Will shutdown.",
					"DB Manager Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		// init logger
		initLog();
		// inti ui
		initGUI();
		// init logger panel
		setLogPanel();

		if (appPropertyManager.getShowDBConfPanelAtStartup())
			displayDBConnPanel(true);
		else
			displayDBConnPanel(false);
		this.setVisible(true);
	}

	private void displayDBConnPanel(boolean dbConnectMode) {
		if (dbConnPanel == null)
			dbConnPanel = new DBConnectionPanel(this, dbConnectMode);

		if (dbConnectMode)
			mainTabbedPane.add("DB Conn", dbConnPanel);
		else
			mainTabbedPane.add("DBM Login", dbConnPanel);

		appMode = ApplicationModes.NoDBConnection;
		menuPanel.setVisible(false);
		MainUIFrame.setStatusMessage("Specify DB Connection properties");
		appModeChanged();
	}

	private void storeProperties() {
		appPropertyManager.setApplicationHeight(this.getHeight());
		appPropertyManager.setApplicationWidth(this.getWidth());
		appPropertyManager.setApplicationX(this.getX());
		appPropertyManager.setApplicationY(this.getY());
		appPropertyManager.writePropertyFile();
	}

	private void addListeners() {
		// Window close listener to close db connection
		// when the application closes.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				logger.debug("Application CLOSING");

				try {
					logger.debug("Closing DB Connection");
					storeProperties();
					// if(DBSqlHandler)
					DBSqlHandler.getInstance().closeDBConnection();
					logger.info("DB Connection closed.");
				} catch (DBException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
				}
				logger.info("Application closed");
				setVisible(false);
				dispose();
			}

			/**
			 * Adds windows opened listener to configure UI for better ease of
			 * use.
			 */
			public void windowOpened(WindowEvent e) {
				logger.debug("Windows OPENED");
				switch (appMode) {
				// if dbconfiguration panel is displayed then set the focus to
				// the db url textfield so that it is easy to change the db url.
				case NoDBConnection:
					dbConnPanel.setFocusUrl();
					break;
					// refresh ui
				case DataEntry:
					refreshUI();
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * Initialises the DB Manager application. Logs in to the DB. If successful
	 * Then verifies the users password and gets the level of the user.
	 */
	public void initDBManager(final String dburl, final String username,
			final String userpwd) {
		MainUIFrame.setIsPerformingTask(true);
		MainUIFrame.setStatusMessage("Conneting ....");
		// perform process in a separate thread.
		new Thread() {
			public void run() {
				boolean validUser = false;
				logger.debug("Trying to access db using login details.");
				MainUIFrame.setStatusMessage("Trying to access db ...");

				dbAccessible = doAccessDB(dburl, username, userpwd);
				// if db is accessible using the specified password then
				// no need to verify user password.. UserLevel=Admin
				if (dbAccessible) {
					MainUIFrame.setStatusMessage("Connected to db ...");
					logger.info("DB is accessible using login details.");
					userLevel = UserLevels.Admin;
					validUser = true;

					// Save in property file
					if (!dburl.equals(appPropertyManager.getDBUrl()))
						appPropertyManager.setDBUrl(dburl);

					if (!username.equals(appPropertyManager.getDBUserName()))
						appPropertyManager.setDBUsername(username);

					if (!userpwd.equals(appPropertyManager.getDBPassword()))
						appPropertyManager.setDBPassword(userpwd);

					if (!username.equals(appPropertyManager.getAppUserName()))
						appPropertyManager.setAppUsername(username);

					appPropertyManager.writePropertyFile();
				}
				// if db is not accessible then try accessing using the
				// password saved in the property file.
				else {
					if (dburl.equals(appPropertyManager.getDBUrl())) {
						logger.debug("Try DB access using saved passwords.");
						dbAccessible = doAccessDB(
								appPropertyManager.getDBUrl(),
								appPropertyManager.getDBUserName(),
								appPropertyManager.getDBPassword());
						logger.info("DB is accessible:" + dbAccessible);
						if (!dbAccessible) {

							displayDBConnPanel(true);
							MainUIFrame.setIsPerformingTask(false);
							MainUIFrame
							.setStatusMessage("DB Connection failed");
							return;
						}
					} else
						return;
				}

				// If user level is not Admin then verify password
				if (!userLevel.equals(UserLevels.Admin)) {
					logger
					.debug("Checking user password and getting user level");
					validUser = doDBLogin(username, userpwd);
					logger.info("user is valid: " + validUser);
				}

				// Try getting user password
				try {

					if (validUser) {

						MainUIFrame.setStatusMessage("User validated. level: "
								+ userLevel + "...");
						logger.debug("User validated. level: " + userLevel
								+ "...");
						if (!username.equals(appPropertyManager
								.getAppUserName())) {
							appPropertyManager.setAppUsername(username);
							appPropertyManager.writePropertyFile();
						}
						if (!userLevel.equals(UserLevels.Admin))
							rbutUsers.setVisible(false);

						appMode = ApplicationModes.DataEntry;
						//userLogin=true;
						appModeChanged();
						MainUIFrame.setStatusMessage("Getting db structure...");

						initDBStructure();

						prjLabels
						.setText("<html><body><font size=\"3\">ethnoArc Database Manager - "
								+ "</font><font size=\"2\"><i>"
								+ userLevel
								+ " Mode</i></font><br><font size=\"2\">DB URL: "
								+ appPropertyManager.getDBUrl()
								+ "</font><body></html>");

						setIsPerformingTask(true);
						MainUIFrame.setStatusMessage("Building UI...");
						initDBStructureUI();

						if (appMode.equals(ApplicationModes.DataEntry)) {
							mainTabbedPane.remove(dbConnPanel);
						}

						if (userLevel.equals(UserLevels.Browser)
								|| userLevel.equals(UserLevels.BrowserFull)) {
							rbutBrowser.setText("Browse");
							rbutBrowser.setToolTipText("Browse database");
						}

						refreshUI();
						MainUIFrame.setIsPerformingTask(false);
						MainUIFrame
						.setStatusMessage("Database retrieved & UI initialised");
					}
				} catch (DBException e) {
					if (logger.isDebugEnabled())
						logger.error("DB Login: " + e.getDetailedMsg(), e);
					else
						logger.error("DB Login: " + e.getDetailedMsg());
					if (dbConnPanel == null)
						dbConnPanel = new DBConnectionPanel(e, appFrame, false);
					else
						dbConnPanel.setError(e, false);

					mainTabbedPane.add("DB Conn", dbConnPanel);
					appMode = ApplicationModes.NoDBConnection;
					MainUIFrame.setIsPerformingTask(false);
					setStatusMessage("Error getting structure",
							MessageLevel.error);
					appModeChanged();
				} catch (Exception e) {
					if (logger.isDebugEnabled())
						logger.error("DB Login: " + e.getMessage(), e);
					else
						logger.error("DB Login: " + e.getMessage());
					logger.error(".........צצצצ"+e);
					e.printStackTrace();
					MainUIFrame.setIsPerformingTask(false);
					setStatusMessage("Error initialising", MessageLevel.error);
				}
				MainUIFrame.setIsPerformingTask(false);
			}
		}.start();

	}

	public boolean isDBAccessible() {
		return dbAccessible;
	}

	/**
	 * Accesses the database.
	 * 
	 * @return If success or not.
	 * @throws DBException
	 * @throws Exception
	 */
	private boolean doAccessDB(String dburl, String username, String userpwd) 
	{
		try {
			dbHandler = new DBHandling(dburl, username, userpwd);
		} catch (DBException e) {
			// Display dbconnection panel
			setIsPerformingTask(false);
			setStatusMessage("db connection failed:" + e.getDetailedMsg(),
					MessageLevel.error);

			if (logger.isDebugEnabled())
				logger.error("DB Login: " + e.getDetailedMsg(), e);
			else
				logger.error("INIT DB STRUCTURE: " + e.getDetailedMsg());
			if (dbConnPanel == null)
				dbConnPanel = new DBConnectionPanel(e, this, true);
			else
				dbConnPanel.setError(e, true);

			return false;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.error("Error DB Access: " + e.getMessage(), e);
			else
				logger.error("Error DB Access: " + e.getMessage());
			setIsPerformingTask(false);
			setStatusMessage("db connection failed:" + e.getMessage(),
					MessageLevel.error);

			return false;
		}
		return true;
	}

	/**
	 * Logins to the DB. If successful verifies the user password and gets the
	 * user level.
	 * 
	 * @return <code>true</code>: User password is correct.<br>
	 *         <code>false</code>: user password is not correct.
	 */
	private boolean doDBLogin(String username, String userpwd) {
		// verify user password
		UserManager um = new UserManager(username, userpwd);
		boolean validUser;
		try {
			validUser = um.pwdIsValid();
			userLogin=true;
		} catch (DBException e) {

			setStatusMessage("DB Access error: " + e.getDetailedMsg(),
					MessageLevel.error);
			return false;
		}
		if (validUser) {
			userLevel = um.getUserLevel();
		}
		return validUser;
		// return false;
	}

	/**
	 * Gets the user level of the logged in user.
	 * 
	 * @return the user level of the application user.
	 * @see UserLevels
	 */
	public static UserLevels getUserLevel() {
		return userLevel;
	}
	public static boolean displayDeleteButton()
	{
		try {
			return (userLevel.equals(UserLevels.Admin)||
			userLevel.equals(UserLevels.Editor))&&
			AppPropertyManager.getDBPropertyManagerInstant().getDisplayDeleteOption();
		} catch (DBException e) {
			return false;
		}
	}

	// -------------- GET DATA ----------------------------------------
	/**
	 * The parsed DB structure.
	 */
	private DBStructure dbStructure;

	/**
	 * The DB handler object to manage database communication.
	 */
	private DBHandling dbHandler;

	/**
	 * Gets the database structure from the MySql Database.
	 */
	private void initDBStructure() throws DBException, Exception {
		dbStructure = dbHandler.getDBStructure();
		//printCombinedTables(dbStructure);
	}

	/**
	 * Gets the DB structure and initiates dynamic UI based on the DB structure.
	 */
	private void initDBStructureUI() {
		if (dbStructure != null) {
			// Get the top level tables.
			Hashtable<String, DBTable> topLevelTables = dbStructure
			.getCombinedTables();

			int i = 0;
			// Create a tab for each top level tables.
			for (DBTable table : topLevelTables.values()) {
				try {
					topLevelTableElements.put(table.getTableName(),
							new Vector<String>());
					addTablePanel(table, i++);
					searchStringPartFrom.put(table.getTableName(), searchFrom);
					searchStringPartWhere
					.put(table.getTableName(), searchWhere);
					searchFrom = "";
					searchWhere = "";
				} catch (Exception e) {
					logger.error("Error adding tab for table:"
							+ table.getTableName(), e);
					setStatusMessage("Error adding tab for table:"
							+ table.getTableName());
				}
			}
		} else
			statusBarLabel.setText("Error getting DB Structure!");
		/*
		 * for (String tPanelName : tablePanels.keySet()) {
		 * logger.error(tPanelName+":----- FROM
		 * '"+searchStringPartFrom.get(tPanelName)+"'::'"+searchStringPartWhere.get(tPanelName)+"'"); }
		 */
	}
//	class PopupListener extends MouseAdapter {
//	    public void mousePressed(MouseEvent e) {
//	        maybeShowPopup(e);
//	    }
//
//	    public void mouseReleased(MouseEvent e) {
//	        maybeShowPopup(e);
//	    }
//
//	    private void maybeShowPopup(MouseEvent e) {
//	        if (e.isPopupTrigger()) {
//	            popup.show(e.getComponent(),
//	                       e.getX(), e.getY());
//	        }
//	    }
//	}
	//JPopupMenu popup;
	/**
	 * Creates a tab panel for the given top level table and initiates creation
	 * of UI for elements of the table.
	 * 
	 * @param table
	 *            The top level table to create dynamic UI.
	 * @throws Exception
	 */
	private void addTablePanel(DBTable table, int tableIndx) throws Exception {
		String ctbName = table.getTableName();
//		if (logger.isDebugEnabled())
//			logger.error("Creating combined table tab panels:"+table.getTableName()+":"+table.getParentConnectingTable().getType());

		// Create a table panel
		TablePanel aTablePanel = new TablePanel(table, 0, null,
				AppConstants.APP_COLOR_DEFAULT, appMode,null);

		mainTabbedPane.add(aTablePanel, tableIndx);

		if (topLevelTableElements.size() <= 9) {
			int shortcut = 49;
			mainTabbedPane.setMnemonicAt(tableIndx, shortcut + tableIndx);
		}
		if(aTablePanel.isHelperTable())
		{
			if(appPropertyManager.getDistinguishHelperTables())
				mainTabbedPane.setTitleAt(tableIndx, table.getTableDisplayName()+"*");
			else
				mainTabbedPane.setTitleAt(tableIndx, table.getTableDisplayName());
		}
		else
			mainTabbedPane.setTitleAt(tableIndx, table.getTableDisplayName());
		mainTabbedPane.setToolTipTextAt(tableIndx, getElementTooltip(table));
		
		//if the user is admin show the popup menu to change table details.
		if(userLevel.equals(UserLevels.Admin)&&appPropertyManager.getDisplayEditDescriptionMenu())
		{
			EADBDescription tabledes= table.getTableProperties();

			mainTabbedPane.addMouseListener(new DBDescriptionManager(
					ctbName,tabledes.getDisplayname(),tabledes.getDescription(),tabledes.getEnglishDescription()));
		}

		addTableElement(table, aTablePanel, table.getTableName());

		logger.info("Tab panel for the table '" + ctbName + "' created.");
		MainUIFrame.setStatusMessage("Tab panel for the table '" + ctbName
				+ "' created.");
		tablePanels.put(table.getTableName(), aTablePanel);
		if(searchTables.size()>61)
		{
			logger.warn("Search will not work... too many tables:"+searchTables.size());
		}
	}

	/**
	 * Updates the tooltip of tables and elements when the tooltip type is
	 * updated through configuration panel.
	 */
	public void updateElementTooltip() {
		logger.error("Update tooltip");

		for (TablePanel tPanel : tablePanels.values()) {
			tPanel.updateElementTooltip();
			for (int i = 0; i < mainTabbedPane.getComponentCount(); i++) {
				try {
					TablePanel t = (TablePanel) mainTabbedPane
					.getComponentAt(i);
					if (t.getTableName().equals(tPanel.getTableName())) {
						mainTabbedPane.setToolTipTextAt(i, getElementTooltip(t
								.getAssociateTable()));
						break;
					}
				} catch (java.lang.ClassCastException e) {

				}
			}
		}
	}

	private String getElementTooltip(DBTable table) {
		AppConstants.ElementTooltipOptions tooltipType;
		String tooltip = "";
		// set tooltip
		try {
			tooltipType = AppPropertyManager.getDBPropertyManagerInstant()
			.getElementTooltipType();
		} catch (DBException e) {
			logger
			.warn("Error getting tooltip type from the property manager: "
					+ e.getMessage());
			// default
			tooltipType = AppConstants.ElementTooltipOptions.Description;
		}
		switch (tooltipType) {
		case Description:
			tooltip = "<b>" + table.getTableDisplayName() + "</b><br>"
			+ table.getTableProperties().getDescription();
			break;
		case EnglishDescription:
			tooltip = "<b>" + table.getTableDisplayName() + "</b><br>"
			+ table.getTableProperties().getEnglishDescription();
			break;
		case Description_detail:
			tooltip = "<b>" + table.getTableDisplayName() + "</b><br>"
			+ table.getTableProperties().getDescription();
			
			break;
		case EnglishDescription_detail:
			tooltip = "<b>" + table.getTableDisplayName() + "</b><br>"
			+ table.getTableProperties().getEnglishDescription();
			break;
		default:
			break;
		}
		if(logger.isDebugEnabled())
		{
			tooltip += "<br>Str:" + table.getParentStructure();
		}
		return MainUIFrame.getToolTipString(tooltip);
	}

	String searchWhere = "";

	String searchFrom = "";
	private List<String> searchTables = new ArrayList<String>();
	/**
	 * Adds table elements in the UI.
	 * 
	 * @param aTable
	 *            The database table.
	 * @param parentPanel
	 *            The parent panel.
	 */
	private void addTableElement(DBTable aTable, TablePanel parentPanel,
			String topLevelTableName) {
		
		// logger.error("------------
		// "+aTable.getTableName()+"_"+parentPanel.getTableName());
		// get list of all sorted child elements of the table.
		
		List<DBTable.TableElement> tables = aTable.getSortedTableElements();

		//	logger.error("This table contains value:-"+aTable.getTableName());
		for (DBTable.TableElement childTableElement : tables) {
			
//			if (logger.isDebugEnabled())
//				logger.debug("####### "+aTable.getTableName()+" Child Element:"
//						+ childTableElement.getElementName() + " Type: "
//						+ childTableElement.getElementType()+":"+childTableElement.getParentReferenceType());

			switch (childTableElement.getElementType()) {

			case TABLE: // The child element is a combined table.
//				logger.error("...... "+)
				addTablePanelElement(aTable,parentPanel,topLevelTableName,childTableElement);
				break;
			case ELEMENT: // the child element is an element (does not have an
				// child element itself)
				addElementPanelElement(aTable,parentPanel,topLevelTableName,childTableElement);
				break;
			default:
				break;
			}
		}
		parentPanel.setTableData();
	}
	public void addTablePanelElement(DBTable aTable, TablePanel parentPanel,
			String topLevelTableName,DBTable.TableElement childTableElement)
	{
		try {
//			logger.error("הההההההה "+childTableElement.getCombinedTable().getParentTableName()+":"+ childTableElement.getElementType());
			int parentLevel = parentPanel.getParentLevel() + 1;
			//if(!aTable.getTableProperties().getNoValue())
			//logger.error("This table contains value:-"+aTable.getTableName());
			Color panelBGColor = AppConstants.APP_COLOR_DEFAULT;
			if (appPropertyManager.separateConsequetivePanelColor()) {
				if (parentLevel % 2 != 0)
					panelBGColor = AppConstants.APP_COLOR_LIGHT;
			}

			TablePanel tablePanel = new TablePanel(childTableElement
					.getCombinedTable(), parentLevel, parentPanel,
					panelBGColor, appMode,childTableElement.getParentReferenceType());

			//add panel for this table to the parent panel
			parentPanel.addTablePanel(tablePanel);

			//get child tables and create table panels for them
			String parentTableName = aTable.getTableName();
			String parentConnTableName = childTableElement
			.getCombinedTable().getParentConnectingTableName();
			String tName = childTableElement.getCombinedTable()
			.getTableName();
			
			if(searchTables.contains(tName))
			{
				//logger.error(" #######"+tName);
				//logger.info("------------- already added "+tName);
				if (searchFrom.equals(""))
					searchFrom = parentTableName;


				if(!searchTables.contains(parentConnTableName))
					searchTables.add(parentConnTableName);

				searchFrom += " LEFT OUTER JOIN " + parentConnTableName
				+ " ON (" + parentTableName + ".ID="
				+ parentConnTableName + ".ID1 "
				//+ " LEFT OUTER JOIN " + tName + " ON " + 
				+"AND "+ tName + ".ID=" + parentConnTableName + ".ID2) ";
			}
			else
			{
				searchTables.add(tName);
				// /searchFrom+=
				// ","+childTable.getCombinedTable().getTableName()+","+childTable.getCombinedTable().getParentConnectingTableName();
				if (searchFrom.equals(""))
					searchFrom = parentTableName;

				if(!searchTables.contains(parentConnTableName))
					searchTables.add(parentConnTableName);

				searchFrom += " LEFT OUTER JOIN " + parentConnTableName
				+ " ON " + parentTableName + ".ID="
				+ parentConnTableName + ".ID1"
				+ " LEFT OUTER JOIN " + tName + " ON " + tName
				+ ".ID=" + parentConnTableName + ".ID2";
			}

			addTableElement(childTableElement.getCombinedTable(),
					tablePanel, topLevelTableName);

//			check if it has alt language tables
			List<EADBDescription> altLangTables = childTableElement.getCombinedTable().getTableProperties().getAlternateLanguageTables();
			if(altLangTables.size()>0)
			{
				for (EADBDescription alLangTable : altLangTables) {
					if(logger.isDebugEnabled())
						logger.debug("###### "+alLangTable.getNameDB()+":"+childTableElement.getCombinedTable().getTableName());
					
					DBTable altable=dbStructure.getCombinedTableByName(alLangTable.getNameDB());
					alLangTable.setDBElementType(DBElementTypes.Table);
					if(altable==null)
					{
						altable=createDBTable(childTableElement.getCombinedTable(), alLangTable);
					}
					//Color alPanelBGColor = AppConstants.APP_COLOR_ALT;
					Color alPanelBGColor = AppConstants.APP_COLOR_DEFAULT;
					if (appPropertyManager.separateConsequetivePanelColor()) {
						if ((parentLevel+1) % 2 != 0)
							alPanelBGColor = AppConstants.APP_COLOR_LIGHT;
					}
					TablePanel alLangTablePanel = new TablePanel(altable,parentLevel+1,tablePanel,alPanelBGColor,appMode,true);
					tablePanel.addTablePanel(alLangTablePanel);

					addTableElement(altable,alLangTablePanel,topLevelTableName);
					String altablename=altable.getTableName();
					String altableparentname=altable.getParentTable().getTableName();
					String altableparentrelname=altable.getParentConnectingTableName();
					if(searchTables.contains(altablename))
					{
						//logger.info("-------------2 already added "+tName);
						if (searchFrom.equals(""))
							searchFrom = altableparentname;
						if(searchTables.contains(altableparentrelname))
						{
							//logger.info("-----3 rel EXISTS ");
							//searchFrom += " LEFT OUTER JOIN " + altableparentrelname
							searchFrom = searchFrom.substring(0,searchFrom.lastIndexOf(")"));
							searchFrom +=  " AND " + altableparentname + ".ID="
							+ altableparentrelname + ".ID1 "
							+"AND "+ altablename + ".ID=" + altableparentrelname + ".ID2) ";
						}
						else
						{
							searchTables.add(altableparentrelname);
							searchFrom += " LEFT OUTER JOIN " + altableparentrelname
							+ " ON (" + altableparentname + ".ID="
							+ altableparentrelname + ".ID1 "
							+"AND "+ altablename + ".ID=" + altableparentrelname + ".ID2) ";
						}
					}
					else
					{
						searchTables.add(altablename);

						// /searchFrom+=
						// ","+childTable.getCombinedTable().getTableName()+","+childTable.getCombinedTable().getParentConnectingTableName();
						if (searchFrom.equals(""))
							searchFrom = parentTableName;

						if(!searchTables.contains(altableparentrelname))
							searchTables.add(altableparentrelname);
						//else
						//	logger.info("-----+ NOT ADDED");


						searchFrom += " LEFT OUTER JOIN " + altableparentrelname
						+ " ON " + altableparentname + ".ID="
						+ altableparentrelname + ".ID1"
						+ " LEFT OUTER JOIN " + altablename + " ON " + altablename
						+ ".ID=" + altableparentrelname + ".ID2";
					}
				}
			}

			//aTable.getSortedTableElements();
		} catch (Exception e) {
			logger.error("Error adding Data Panel for "
					+ aTable.getTableName(), e);
			e.printStackTrace();
		}
	}
	public void addElementPanelElement(DBTable aTable, TablePanel parentPanel,
			String topLevelTableName,DBTable.TableElement childTableElement)
	{
		/*int parentLevel = parentPanel.getParentLevel();
		Color panelBGColor = AppConstants.APP_COLOR_DEFAULT;
		if (appPropertyManager.separateConsequetivePanelColor()) {
			if (parentLevel % 2 != 0)
				panelBGColor = AppConstants.APP_COLOR_LIGHT;
		}*/

		ElementPanel lab = new ElementPanel(childTableElement
				.getChildTableElement(), parentPanel.getBGColor(), this,parentPanel);
		parentPanel.addElementUI(childTableElement
				.getElementTableName(), lab);

		dbElements.put(childTableElement.getElementTableName(), lab);
		// Add parent structure of the element in the list of elements
		// of the top level table for search.
		Vector<String> elementList = topLevelTableElements
		.get(topLevelTableName);

		elementList.add(childTableElement.getChildTableElement()
				.getParentStructure());
		topLevelTableElements.remove(topLevelTableName);
		topLevelTableElements.put(topLevelTableName, elementList);
	}
	private DBTable createDBTable(DBTable parentTable,EADBDescription tableDesc)
	{
		DBTable newTable=new DBTable(tableDesc);
		//add parent table
		newTable.setParentTable(parentTable);

		//add parent connecting table
		String conTableName = dbStructure.getRelationTableName(parentTable.getTableName(), tableDesc.getNameDB());
		if(conTableName!=null)
		{
			//logger.debug(":::::: "+tableDesc.getNameDB());
			newTable.setParentConnectingTable(dbStructure.getTableByTableName(conTableName));
		}
		else
		{
			//TODO: ERROR
			//logger.error("llllllllllllll");
		}
		//look for related child tables
		String query = "SELECT * FROM databasedescription where firstTable='"+tableDesc.getNameDB()+"'";
		try {
			ResultSet rs= DBSqlHandler.getInstance().executeQuery(query);
			while( rs.next() ) {
				String conTablen=rs.getString(DBConstants.FIELD_TABLENAME);
				String reltablename=rs.getString(DBConstants.FIELD_SECONDTABLE);
				if(logger.isDebugEnabled())
					logger.debug("---- "+tableDesc.getNameDB()+":"+ conTablen+":"+reltablename);

				EADBDescription elDesc = dbStructure.getTableByTableName(reltablename);
				EADBDescription elRelDesc = dbStructure.getTableByTableName(conTablen);
				DBTableElement tableElement = new DBTableElement(elDesc,elRelDesc,tableDesc.getNameDB(),true);
				//logger.error("------ "+newTable.getTableName()+" ---- "+tableElement.getNameDB());
				newTable.addRelatedTable(tableElement);
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newTable;
	}

	public ElementPanel getImpliesElement(String elementName) {
		return dbElements.get(elementName);
	}
	public String getConnectingTableByName(String firstTableName, String secondTableName)
	{
		return dbStructure.getRelationTableName(firstTableName, secondTableName);
	}

	private static void printCombinedTables(DBStructure dbStructure) {
		Hashtable<String, DBTable> combinedTables = dbStructure
		.getCombinedTables();
		for (DBTable combinedTable : combinedTables.values()) {
			logger
			.debug("----------------------------------------------------------------");
			logger.debug("CombinedTableName=" + combinedTable.getTableName());

			Hashtable<String, DBTableElement> subTables = combinedTable
			.getRelatedTables();
			for (DBTableElement table : subTables.values()) {
				logger.debug("\tSubtableTableName=" + table.getNameDB()
						+ "Type:" + table.getRelationType());
			}
		}
	}

	// -------------- Initialisation GUI Parts
	// ----------------------------------------
	private void initGUI() {
		try {
			this.setIconImage(new ImageIcon("res\\images\\ethnoarc_logo.png")
			.getImage());
			this.setTitle("ethnoArc Database Manager");

			initInfoPanel();

			initMainPanel();

			initBottomPanel();
			pack();
			int wdth = 700;
			int hght = 900;
			if (appPropertyManager.getStartLastSize()) {
				if (appPropertyManager.getApplicationWidth() != -1)
					wdth = appPropertyManager.getApplicationWidth();
				if (appPropertyManager.getApplicationHeight() != -1)
					hght = appPropertyManager.getApplicationHeight();
				setLocation(appPropertyManager.getApplicationX(),
						appPropertyManager.getApplicationY());
			}

			setMinimumSize(new Dimension(580, 650));
			setSize(wdth, hght);

			setStatusMessage("DB UI Initialised");

			JButton butAbout = new JButton("");
			butAbout.setBorderPainted(true);
			butAbout.setBorder(new LineBorder(Color.red));
			butAbout.setFocusPainted(false);
			butAbout.setPreferredSize(new Dimension(0, 0));
			butAbout.setMaximumSize(new Dimension(0, 0));
			butAbout.setSize(new Dimension(0, 0));
			butAbout.setMnemonic(KeyEvent.VK_A);
			butAbout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null,
							"<html><body><b>ethnoArc Database Manager</b>"
							+ "<font size=\"2\"><p><p>Version:"
							+ appPropertyManager
							.getApplicationVersion()
							+ "<p>Build: "
							+ appPropertyManager
							.getApplicationBuildDate()
							+ "</font></body></html>",
							"About DB Manager",
							JOptionPane.INFORMATION_MESSAGE, new ImageIcon(
							"res/images/ethnoarc_logo.png"));
				}
			});
			statusPanel.add(butAbout);

		} catch (Exception e) {
			logger.error("Error Initialising UI:", e);
		}
	}

	private enum MenuCommands {
		Search, Manage, Design, Users, Configure, Exit
	}

	public void actionPerformed(ActionEvent e) {
		setIsPerformingTask(true);

		switch (MenuCommands.valueOf(e.getActionCommand())) {
		case Manage:
			appMode = AppConstants.ApplicationModes.DataEntry;
			setStatusMessage("Changing to Browse mode...");
			appModeChanged();
			break;
		case Search:
			appMode = AppConstants.ApplicationModes.Search;
			setStatusMessage("Changing to Search mode...");
			appModeChanged();
			break;
		case Design:
			JOptionPane.showMessageDialog(this, "Sorry Not Implemented Yet!",
					"UI Warning", JOptionPane.WARNING_MESSAGE);
			// appMode=AppConstants.ApplicationModes.DBUpdate;
			// appModeChanged();
			break;
		case Users:
			setStatusMessage("Changing to user management mode...");
			// verify user level
			if (!userLevel.equals(UserLevels.Admin)) {
				JOptionPane.showMessageDialog(this,
						"Sorry you must have Admin right to manage users.",
						"Access Right Problem", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (userPanel == null)
				userPanel = new UserManagerPanel(this,
						AppConstants.APP_COLOR_DEFAULT);
			mainTabbedPane.add("Users", userPanel);
		
			mainTabbedPane.setSelectedComponent(userPanel);
			lastAppMode = appMode;
			appMode = AppConstants.ApplicationModes.UserManagement;

			// disable table tabs
			for (int i = 0; i < mainTabbedPane.getComponentCount() - 1; i++) {
				mainTabbedPane.setEnabledAt(i, false);
			}
			appModeChanged();
			break;
		case Configure:
			setStatusMessage("Changing to configuration mode...");

			if (configPanel == null)
				configPanel = new ConfigurationPanel(this,
						AppConstants.APP_COLOR_DEFAULT);
			mainTabbedPane.add(configPanel, "Config");
			mainTabbedPane.setSelectedComponent(configPanel);
			lastAppMode = appMode;
			appMode = AppConstants.ApplicationModes.Configuration;

			// disable table tabs
			for (int i = 0; i < mainTabbedPane.getComponentCount() - 1; i++) {
				mainTabbedPane.setEnabledAt(i, false);
			}
			appModeChanged();

			// setIsPerformingTask(false);
			// this.setVisible(true);
			break;
		case Exit:
			setVisible(false);
			dispose();
			break;
		default:
			break;
		}
	}

	public void closeUserManagerPanel() {
		if (userPanel != null) {
			logger.debug("Close user manager panel " + selectedTabIndx);
			userPanel.setVisible(false);

			mainTabbedPane.remove(userPanel);
			// mainTabbedPane.setSelectedIndex(0);
			// mainTabbedPane.updateUI();
			appMode = lastAppMode;
			// enable table tabs
			for (int i = 0; i < tablePanels.size(); i++) {
				mainTabbedPane.setEnabledAt(i, true);
			}
			appModeChanged();
		}
		// else
		// logger.error("Close user manager panel?????");
	}

	public void closeConfigPanel() {
		if (configPanel != null) {
			logger.debug("Close config manager panel " + selectedTabIndx);
			configPanel.setVisible(false);

			mainTabbedPane.remove(configPanel);
			// mainTabbedPane.setSelectedIndex(0);
			// mainTabbedPane.updateUI();
			appMode = lastAppMode;
			// enable table tabs
			for (int i = 0; i < tablePanels.size(); i++) {
				mainTabbedPane.setEnabledAt(i, true);
			}
			appModeChanged();
		}
		// else
		// logger.error("Close user manager panel?????");
	}

	/**
	 * Returns the flag indicating if the user account stored in the database
	 * is used to login.
	 * @return <code>true</code>: User account stored in the database is used.<br>
	 * <code>false</code>: DB account is used to login.
	 */
	public boolean isUserAccountUsed()
	{
		return userLogin;
	}

	public static ApplicationModes getApplicationMode() {
		return appMode;
	}

	private void appModeChanged() {
		switch (appMode) {
		case DataEntry:
			menuPanel.setVisible(true);
			// infoPanel.setBackground(AppConstants.APP_COLOR_DEFAULT);
			if (butSearch != null)
				butSearch.setVisible(false);
			if (userLevel.equals(UserLevels.Admin)) {
				rbutUsers.setEnabled(true);
				rbutUsers.setSelected(false);
			}
			rbutBrowser.setEnabled(false);
			rbutSearch.setEnabled(true);
			rbutBrowser.setSelected(true);
			rbutSearch.setSelected(false);
			searchPanel.setVisible(false);
			rbutConfigure.setSelected(false);
			rbutConfigure.setEnabled(true);
			if (tabListener != null)
				mainTabbedPane.removeChangeListener(tabListener);
			break;
		case Search:
			logger.debug("App mode is Search");
			searchList.clear();
			// infoPanel.setBackground(AppConstants.APP_COLOR_SEARCH);
			if (butSearch == null)
				createSearchButton();
			else
				butSearch.setVisible(true);
			if (userLevel.equals(UserLevels.Admin)) {
				rbutUsers.setEnabled(true);
				rbutUsers.setSelected(false);
			}
			rbutSearch.setEnabled(false);
			rbutBrowser.setEnabled(true);
			rbutSearch.setSelected(true);
			rbutBrowser.setSelected(false);
			rbutConfigure.setSelected(false);
			rbutConfigure.setEnabled(true);
			rootPane.setDefaultButton(butSearch);
			searchPanel.setVisible(true);
			if (tabListener == null)
				tabListener = new TabChangeListener();
			mainTabbedPane.addChangeListener(tabListener);
			break;
		case DBUpdate:
			/*
			 * infoPanel.setBackground(AppConstants.APP_COLOR_DESIGN);
			 * if(butSearch!=null) butSearch.setVisible(false);
			 * if(userLevel.equals(UserLevels.Admin)) {
			 * rbutUsers.setEnabled(true); } rbutSearch.setEnabled(false);
			 * rbutSearch.setFont(AppConstants.APP_FONT_FIELDS_BOLD);
			 * rbutBrowser.setEnabled(true);
			 */
			break;
		case NoDBConnection:
			menuPanel.setVisible(false);
			if (butSearch != null)
				butSearch.setVisible(false);
			// rbutBrowser.setEnabled(false);
			// rbutSearch.setEnabled(false);
			// rbutUsers.setEnabled(false);
			searchPanel.setVisible(false);

			break;
		case UserManagement:
			if (butSearch != null)
				butSearch.setVisible(false);
			rbutBrowser.setSelected(false);
			rbutSearch.setSelected(false);
			rbutUsers.setSelected(true);
			rbutBrowser.setEnabled(false);
			rbutSearch.setEnabled(false);
			rbutUsers.setEnabled(false);
			searchPanel.setVisible(false);
			rbutConfigure.setSelected(false);
			rbutConfigure.setEnabled(false);
			if (tabListener != null)
				mainTabbedPane.removeChangeListener(tabListener);
			break;
		case Configuration:
			if (butSearch != null)
				butSearch.setVisible(false);
			rbutBrowser.setSelected(false);
			rbutSearch.setSelected(false);
			rbutUsers.setSelected(false);
			rbutUsers.setEnabled(false);
			rbutBrowser.setEnabled(false);
			rbutSearch.setEnabled(false);
			rbutUsers.setEnabled(false);
			searchPanel.setVisible(false);
			rbutConfigure.setSelected(true);
			rbutConfigure.setEnabled(false);
			if (tabListener != null)
				mainTabbedPane.removeChangeListener(tabListener);
			break;
		default:
			break;
		}

		// perform process in a separate thread.
		setStatusMessage("");
		new Thread() {
			public void run() {
				for (TablePanel tPanel : tablePanels.values()) {
					try {
						setStatusMessage("Updating " + tPanel.getTableName()
								+ "...");
						tPanel.appModeChanged(appMode);
					} catch (Exception e) {
						setIsPerformingTask(false);
						setStatusMessage("Error:" + e.getMessage(),
								MessageLevel.error);
					}
				}
				setIsPerformingTask(false);
				setStatusMessage(appMode + " mode set.");
			}
		}.start();
	}

	/**
	 * Initialises the Main Panel
	 */
	private void initMainPanel() {
		mainPanel = new JPanel();
		BorderLayout mainPanelLayout = new BorderLayout();
		mainPanel.setLayout(mainPanelLayout);

		getContentPane().add(mainPanel, BorderLayout.CENTER);

		mainTabbedPane = new JTabbedPane();
		mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
	}

	private CustomRadioButton rbutBrowser, rbutSearch, rbutUsers, rbutDBUpdate,
	rbutConfigure;
	public AppPropertyManager getAppProperties()
	{
		return appPropertyManager;
	}
	private void initInfoPanel() {
		// Info Panel
		infoPanel = new JPanel();
		infoPanel.setBackground(AppConstants.APP_COLOR_DARK);
		BorderLayout infoPanelLayout = new BorderLayout();
		infoPanel.setLayout(infoPanelLayout);
		getContentPane().add(infoPanel, BorderLayout.NORTH);
		infoPanel.setPreferredSize(new java.awt.Dimension(384, 70));

		// Info Panel Contents
		prjLabels = new JLabel();

		prjLabels.setIcon(new ImageIcon("res\\images\\ethnoarc_logo.png"));

		infoPanel.add(prjLabels, BorderLayout.CENTER);

		menuPanel = new JPanel(new BorderLayout(0, 0));
		// menuPanel.setAlignmentY(Component.LEFT_ALIGNMENT);
		menuPanel.setBackground(AppConstants.APP_COLOR_DARK);

		FlowLayout mainMenuLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);
		mainMenuLayout.setAlignment(FlowLayout.LEFT);

		JPanel mainMenuPanel = new JPanel(mainMenuLayout);
		mainMenuPanel.setBackground(AppConstants.APP_COLOR_DARK);
		menuPanel.add(mainMenuPanel, BorderLayout.WEST);

		// rbutBrowser = new CustomRadioButton("Browse / Manage",
		// new ImageIcon("res/images/browse.png"),new
		// ImageIcon("res/images/browse-dis.png"),new
		// ImageIcon("res/images/browse-r.png"));
		rbutBrowser = new CustomRadioButton("", new ImageIcon(
		"res/images/browse.gif"), new ImageIcon(
		"res/images/browse-act.gif"), new ImageIcon(
		"res/images/browse-dis.gif"), new ImageIcon(
		"res/images/browse-r.gif"));
		rbutBrowser.setMnemonic(KeyEvent.VK_B);
		rbutBrowser.setSelected(true);
		rbutBrowser.addActionListener(this);
		rbutBrowser.setActionCommand(MenuCommands.Manage.toString());
		rbutBrowser.setToolTipText("Browse/edit database");
		mainMenuPanel.add(rbutBrowser);

		// rbutSearch = new CustomRadioButton("Search",
		// new ImageIcon("res/images/search.png"),new
		// ImageIcon("res/images/search-dis.png"),new
		// ImageIcon("res/images/search-r.png"));
		rbutSearch = new CustomRadioButton("", new ImageIcon(
		"res/images/search.gif"), new ImageIcon(
		"res/images/search-act.gif"), new ImageIcon(
		"res/images/search-dis.gif"), new ImageIcon(
		"res/images/search-r.gif"));
		rbutSearch.setMnemonic(KeyEvent.VK_S);
		rbutSearch.addActionListener(this);
		rbutSearch.setActionCommand(MenuCommands.Search.toString());
		rbutSearch.setToolTipText("Search database");
		mainMenuPanel.add(rbutSearch);

		FlowLayout optMenuLayout = new FlowLayout(FlowLayout.CENTER, 5, 0);
		optMenuLayout.setAlignment(FlowLayout.RIGHT);
		JPanel optMenuPanel = new JPanel(optMenuLayout);
		optMenuPanel.setBackground(AppConstants.APP_COLOR_DARK);

		menuPanel.add(optMenuPanel, BorderLayout.EAST);
		// rbutUsers = new CustomRadioButton("Manage Users",
		// new ImageIcon("res/images/usermanage.png"),new
		// ImageIcon("res/images/usermanage-act.png"),new
		// ImageIcon("res/images/usermanage-dis.png"),new
		// ImageIcon("res/images/usermanage-r.png"));
		rbutUsers = new CustomRadioButton("", new ImageIcon(
		"res/images/usermanage.gif"), new ImageIcon(
		"res/images/usermanage-act.gif"), new ImageIcon(
		"res/images/usermanage-dis.gif"), new ImageIcon(
		"res/images/usermanage-r.gif"));
		rbutUsers.setMnemonic(KeyEvent.VK_U);
		rbutUsers.setToolTipText("Manage user accounts");
		rbutUsers.addActionListener(this);
		rbutUsers.setActionCommand(MenuCommands.Users.toString());
		optMenuPanel.add(rbutUsers);

		rbutConfigure = new CustomRadioButton("", new ImageIcon(
		"res/images/config.gif"), new ImageIcon(
		"res/images/config-act.gif"), new ImageIcon(
		"res/images/config-dis.gif"), new ImageIcon(
		"res/images/config-r.gif"));
		rbutConfigure.setMnemonic(KeyEvent.VK_C);
		rbutConfigure.setToolTipText("Configure DB Manager application");
		rbutConfigure.addActionListener(this);
		rbutConfigure.setActionCommand(MenuCommands.Configure.toString());
		optMenuPanel.add(rbutConfigure);

		ButtonGroup modegroup = new ButtonGroup();
		modegroup.add(rbutBrowser);
		modegroup.add(rbutSearch);
		modegroup.add(rbutUsers);
		modegroup.add(rbutConfigure);
		infoPanel.add(menuPanel, BorderLayout.SOUTH);
	}

	private int selectedTabIndx = -1;

	private JToggleButton showLogBut;

	private JButton butSearch;

	private void setLogPanel() {
		if (logpanel == null)
			logpanel = new LogPanel();

		if (appMode.equals(AppConstants.ApplicationModes.Search))
			createSearchButton();

		if (appPropertyManager.getShowLog()) {
			// Init log panel if necessary
			showLogBut = new JToggleButton("ShowLog");
			showLogBut.setSize(0, 0);
			showLogBut.setPreferredSize(new Dimension(0, 0));
			showLogBut.setMnemonic(KeyEvent.VK_L);
			showLogBut.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JToggleButton tbut = (JToggleButton) e.getSource();

					if (tbut.isSelected()) {
						showLogBut.setText("HideLog");
						selectedTabIndx = mainTabbedPane.getSelectedIndex();
						if (logpanel != null) {
							logger.debug("Add log panel.");
							logpanel.setVisible(true);
							mainTabbedPane.add("Log", logpanel);
							mainTabbedPane.setSelectedIndex(mainTabbedPane
									.getTabCount() - 1);
						}
					} else {
						removeLogPanelFromTab();
						showLogBut.setText("ShowLog");
					}
				}
			});
			infoPanel.add(showLogBut, BorderLayout.NORTH);
		}
	}

	private static String fieldWhere = "";

	public static String getSearchString(String tName) {
		return fieldWhere;
	}

	private void createSearchButton() {
		butSearch = new JButton("Search");
		butSearch.setMnemonic(KeyEvent.VK_S);

		butSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TablePanel tp = (TablePanel) mainTabbedPane
				.getSelectedComponent();
				if (butSearch.getText().equals("Search")) {

					fieldWhere = "";
					MainUIFrame.setStatusMessage("SEARCH " + tp.getTableName());
					final String topLevelTableName = tp.getTableName();
					getSearchParam(topLevelTableName);

					if (searchList.size() > 0) {
						MainUIFrame.setIsPerformingTask(true);
						MainUIFrame.setStatusMessage("Searching ...");
						new Thread() {
							public void run() {
								// String fieldWhere="";
								searchFrom = searchStringPartFrom
								.get(topLevelTableName);
								searchWhere = searchStringPartWhere
								.get(topLevelTableName);

								for (SearchParams aSearchParam : searchList
										.values()) {
									
									// add table name in from part
									if (cbWildCard.isSelected()
											&& (aSearchParam.value
													.contains("%") || aSearchParam.value
													.contains("_"))) {

										String prefix = " ";
										if (!fieldWhere.trim().equals(""))
											prefix = " AND ";
										fieldWhere += prefix;

										if(!aSearchParam.isTableValue)
										{
											searchFrom += ","
												+ aSearchParam.elementName
												+ ","
												+ aSearchParam.connectingtableName;

											fieldWhere+= aSearchParam.tableName
											+ ".ID="
											+ aSearchParam.connectingtableName
											+ ".ID1"
											+ " AND "
											+ aSearchParam.elementName
											+ ".ID="
											+ aSearchParam.connectingtableName
											+ ".ID2";
											fieldWhere += " AND ";
										}
										
										fieldWhere += aSearchParam.elementName
											+ ".content LIKE '"
											+ aSearchParam.value + "'";
									} else {
										String prefix = " ";
										if (!fieldWhere.trim().equals(""))
											prefix = " AND ";
										fieldWhere += prefix;
										if(!aSearchParam.isTableValue)
										{
											searchFrom += ","
												+ aSearchParam.elementName
												+ ","
												+ aSearchParam.connectingtableName;

											fieldWhere+= aSearchParam.tableName
											+ ".ID="
											+ aSearchParam.connectingtableName
											+ ".ID1"
											+ " AND "
											+ aSearchParam.elementName
											+ ".ID="
											+ aSearchParam.connectingtableName
											+ ".ID2";
											fieldWhere += " AND ";
										}
										
										fieldWhere += aSearchParam.elementName
											+ ".content = '"
											+ aSearchParam.value + "'";
									}
								}

								try {

									if(searchFrom.equals(""))
										searchFrom=topLevelTableName;
									fieldWhere = " FROM " + searchFrom
									+ " WHERE " + searchWhere
									+ fieldWhere;
									TablePanel tPanel = tablePanels
									.get(topLevelTableName);
									tPanel.searchStarted();
									butSearch.setText("New Search");
									MainUIFrame.setIsPerformingTask(false);
									setStatusMessage("Searched data retrieved");
								} catch (Exception e1) {
									String msg = "Error doing search:";
									if (logger.isDebugEnabled()) {
										logger.error(msg, e1);
										MainUIFrame.setIsPerformingTask(false);
										setStatusMessage(msg + ":"
												+ e1.getMessage());
									} else {
										logger.error(msg+e1.getMessage()+"::"+fieldWhere);
										MainUIFrame.setIsPerformingTask(false);
										setStatusMessage(msg);
									}
									e1.printStackTrace();
								}
							}
						}.start();
					} else
						setStatusMessage("No Search specified",
								MessageLevel.warn);
				}
				// new search
				else {
					try {
						tp.appModeChanged(appMode);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					butSearch.setText("Search");
				}
			}
		});
		butSearch.setPreferredSize(new Dimension(120, 20));
		butSearch.setFont(AppConstants.APP_FONT_MENU);
		searchPanel.add(butSearch);
	}

	private void getSearchParam(String topLevelTableName) {
		TablePanel topLevelPanel = tablePanels.get(topLevelTableName);
		searchList.clear();

		searchList = topLevelPanel.getSearchParams(searchList);
	}

	public static class SearchParams {
		String tableName, tableViewName, elementName, connectingtableName,
		value;
		boolean isTableValue=false;
		public SearchParams(String tableName, String tableViewName,
				String elementName, String connectingtableName, String value,boolean isTableValue) {
			this.tableName = tableName;
			this.tableViewName = tableViewName;
			this.elementName = elementName;
			this.connectingtableName = connectingtableName;
			this.value = value;
			this.isTableValue=isTableValue;
		}
	}

	/**
	 * Removes the search parameter from the search list.
	 * 
	 * @param elementName
	 *            The name of the element to be removed.
	 */
	public void removeSearchParam(String elementName) {
		searchList.remove(elementName);
	}

	/**
	 * Removes all items from the search list.
	 */
	public void clearSearchParam() {
		searchList.clear();
	}

	private void removeLogPanelFromTab() {
		if (logpanel != null) {
			logpanel.setVisible(false);
			mainTabbedPane.remove(mainTabbedPane.getTabCount() - 1);
			if (selectedTabIndx > -1)
				mainTabbedPane.setSelectedIndex(selectedTabIndx);
		}
	}

	private TabChangeListener tabListener;

	private class TabChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			try {
				TablePanel tp = (TablePanel) mainTabbedPane
				.getSelectedComponent();
				// logger.error("####
				// "+tp.getTableName()+":"+tp.getSearchState());
				if (tp.getSearchState())
					butSearch.setText("New Search");
				else
					butSearch.setText("Search");
			} catch (Exception ex) {
			}
		}
	}

	/** Default status message color. */
	private static Color colorDefault = Color.BLACK;

	/** Warning messages color */
	private static Color colorWarn = Color.RED;

	/** Color of the status messages when the applicaton is performing task. */
	private static Color colorWait = Color.blue;

	/**
	 * Indicates if the application is performing certain task. E.g.,getting
	 * data from the database. This is used in displaying status messages in
	 * specific format based on this value. The idea is to indicate users to
	 * wait before the application completes the task it is doing.
	 */
	private static boolean performingTask = false;

	/**
	 * Sets the value to indicate that the application is performing a task.<br>
	 * <code>true</code>: The application is performing a task. Disable the
	 * whole application UI and displays status mesage in specific way to
	 * indiate users to wait. <br>
	 * <code>true</code>: The application finished performing a task.
	 * 
	 * @param performingTask
	 *            The value to indicate that the application is performing a
	 *            task.
	 */
	public static void setIsPerformingTask(boolean performingTask) {
		Cursor crsr;
		if(performingTask)
			crsr = new Cursor(Cursor.WAIT_CURSOR);
		else
			crsr = new Cursor(Cursor.DEFAULT_CURSOR);
		appFrame.setCursor(crsr);
		appFrame.setEnabled(!performingTask);
		MainUIFrame.performingTask = performingTask;
	}

	public static enum MessageLevel {
		info, warn, error
	}

	public static void setStatusMessage(final String msg) {

		setStatusMessage(msg, MessageLevel.info);
	}

	public static void setStatusMessage(String msg, MessageLevel msgLevel) {
		if (performingTask) {
			
			statusBarLabel.setIcon(new ImageIcon("res/images/wait.gif"));
			// statusBarLabel.setHorizontalAlignment(JLabel.LEFT);
//			statusLabelPanel.setBackground(Color.yellow);
			statusBarLabel.setForeground(colorWait);
		} else {
			
//			statusLabelPanel.setBackground(AppConstants.APP_COLOR_DARK);
			statusBarLabel.setIcon(null);
			switch (msgLevel) {
			case info:
				statusBarLabel.setForeground(colorDefault);
				break;
			case warn:
				statusBarLabel.setForeground(colorWarn);
				break;
			case error:
				statusBarLabel.setForeground(colorWarn);
				break;
			default:
				break;
			}
		}
		statusBarLabel.setText(msg);
	}

	private JPanel searchPanel;

	private JCheckBox cbWildCard;
	private static JPanel statusLabelPanel;
	private void initBottomPanel() {
		statusPanel = new JPanel(new BorderLayout());
		// Search Panel
		searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		cbWildCard = new JCheckBox("Accept Wild Cards", true);
		cbWildCard.setMnemonic(KeyEvent.VK_W);
		cbWildCard.setFont(AppConstants.APP_FONT_FIELDS);
		searchPanel.add(cbWildCard);

		statusPanel.add(searchPanel, BorderLayout.NORTH);

		// status Label Panel
		 statusLabelPanel = new JPanel();
		FlowLayout statusPanelLayout = new FlowLayout();
		statusPanelLayout.setAlignment(FlowLayout.LEFT);
		statusLabelPanel.setLayout(statusPanelLayout);
		statusPanel.add(statusLabelPanel, BorderLayout.SOUTH);

		getContentPane().add(statusPanel, BorderLayout.SOUTH);

		statusPanel.setToolTipText("Status Bar");
		statusPanel.setBackground(new java.awt.Color(192, 192, 192));

		statusBarLabel = new JLabel();

		statusBarLabel.setFont(AppConstants.APP_FONT_DEFAULT);
		statusLabelPanel.setBackground(AppConstants.APP_COLOR_DARK);
		statusLabelPanel.add(statusBarLabel);

		statusBarLabel.setText("Status");

		statusBarLabel.setSize(31, 20);
		//statusPanel.setPreferredSize(new Dimension(statusPanel.WIDTH, 22));
	}

	public static String getToolTipString(String tooltip) {
		int descSize = tooltip.length() - tooltip.lastIndexOf("<br>");
		descSize = 0;
		String[] toolsplit = tooltip.split("<br>");
		for (String string : toolsplit) {
			if (string.length() > descSize)
				descSize = string.length();
		}

		String tableHeader = "<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0>";
		if (descSize > 100)
			tableHeader = "<TABLE BORDER=0 CELLSPACING=0 CELLPADDING=0 WIDTH=150>";
		return "<HTML><BODY STYLE=\"background-color:#FFEFBE\">" + tableHeader
		+ "<TR>" + "<TD>" + tooltip + "</TD>" + "</TR></TABLE>"
		+ "</BODY></HTML>";
	}
	public static String getToolTipStringN(String tooltip) {
		
		return "<HTML><BODY STYLE=\"background-color:#FFEFBE\">" + tooltip
		+ "</TABLE>"
		+ "</BODY></HTML>";
	}


	public static boolean getUiEditMode() {
		return uiEditMode;
	}

	public static void setUiEditMode(boolean editMode) {
		uiEditMode = editMode;
	}

	/**
	 * Initialises the logger.
	 */
	private void initLog() {
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		logger.debug("LOGGER INITIALISED");
	}

	boolean uiFinalized = false;

	private void refreshUI() {
		if (!uiFinalized) {
			for (TablePanel tPanel : tablePanels.values()) {
				tPanel.finalizeUI();
				tPanel.f();
			}
			uiFinalized = true;
		}
	}
}
