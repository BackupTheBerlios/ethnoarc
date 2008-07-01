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
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBField;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.DBElementTypes;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.TableReferenceTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.ApplicationModes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;

/**
 * $Id: TablePanel.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $ 
 * @author fokus
 */
public class TablePanel extends javax.swing.JPanel{

	static Logger logger = Logger.getLogger(TablePanel.class.getName());

	private ApplicationModes currentAppMode=ApplicationModes.DataEntry;

	private int currentDataIndex = 0;
	private int dataSize=0;
	/**
	 * Specifies the number of parents. <br> 
	 * E.g. <code>0</code>: No parents <br>
	 * <code>1</code>: 1 parent <br>
	 * <code>2</code>: 2 parents <br>
	 */
	private int parentLevel=-1;
	/**
	 * The parent table panel
	 */
	private TablePanel parentPanel;
	/**
	 * Specifies if the UI is in data browing mode or in data insert/update mode.
	 */
	private Boolean isMultipleMode=true;

	/**
	 * The name of the table.
	 */
	private String tableName;
	/**
	 * The display name of the table.
	 */
	private String tabledisplayName;
	/**
	 * The table object.
	 */
	private DBTable table;

	private Hashtable<String,TablePanel> SubTablePanels=new Hashtable<String, TablePanel>();

	// ---- Content Panel Controls
	private JScrollPane scrollPanelContent;
	private JPanel contentPanel;
	private Hashtable<String,ElementPanel> tableElements=new Hashtable<String,ElementPanel>(); 
	private boolean isAltLangTable=false;

	// ---- Navigation Panel Controls
	private NavigationPanel navPanel;
	private Color bgColor;
	/**
	 * The type of reference between this table and its parent.
	 * @see TableReferenceTypes
	 */
	private TableReferenceTypes parentReferenceType;

	/**
	 * Specifies if parent or grandparent is of type reference.
	 */
	private boolean parentTypeReferenced=false;
	/**
	 * User interface for a table. A table can contain 0 or more elements and can also contain
	 * 0 or more child tables. Each child table is an instance of this class as well.
	 * @param table The table.
	 * @param parentLevel Specifies level from the top most table.
	 * @throws Exception Error creating a panel.
	 */
	public TablePanel(DBTable table, 
			int parentLevel,
			TablePanel parentPanel,
			Color bgColor, 
			ApplicationModes currentAppMode,
			TableReferenceTypes parentReferenceType) throws Exception {
		super();
		this.table=table;

		this.parentLevel=parentLevel;
		this.parentPanel=parentPanel;
		this.currentAppMode=currentAppMode;
		this.bgColor=bgColor;
		this.parentReferenceType=parentReferenceType;

		setIsParentReferenceType();

		initPanel();
	}
	public TablePanel(DBTable table, int parentLevel,TablePanel parentPanel,Color bgColor, ApplicationModes currentAppMode,boolean isAltLangTable) throws Exception {

		super();
		this.table=table;
		this.isAltLangTable=isAltLangTable;
		this.parentLevel=parentLevel;
		this.parentPanel=parentPanel;
		this.currentAppMode=currentAppMode;
		this.bgColor=bgColor;


		setIsParentReferenceType();
		initPanel();
	}
	private void setIsParentReferenceType()
	{
		if(parentReferenceType!=null)
		{
			if(parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))

				parentTypeReferenced=true;
			else
				parentTypeReferenced=parentPanel.isParentTypeReferenced();
		}
		for (DBTableElement relTable : table.getRelatedTables().values()) {
			relTable.setIsParentTypeReferenced(parentTypeReferenced);
		}
	}
	public TableReferenceTypes getParentReferenceType()
	{
		return parentReferenceType;
	}
	public boolean isParentTypeReferenced()
	{
		return parentTypeReferenced;
	}
	private void initPanel()throws Exception
	{
		this.tableName=table.getTableName();
		this.tabledisplayName=table.getTableDisplayName();
		this.setName(this.tabledisplayName);

		if(table.getParentConnectingTable()!=null)
			this.isMultipleMode=table.getParentConnectingTable().getMultiple();

		this.setBackground(bgColor);

		initGUI();

		getData();

		navPanel.updateControls();

		// set Border
		setBorder();

		if(MainUIFrame.getUserLevel().equals(UserLevels.Admin)&&
				AppPropertyManager.getDBPropertyManagerInstant().getDisplayEditDescriptionMenu())
		{
			EADBDescription tabledes=table.getTableProperties();
			this.addMouseListener(new DBDescriptionManager(table.getTableName(),tabledes.getDisplayname(),
					tabledes.getDescription(),tabledes.getEnglishDescription()));
		}
	}

	private void setBorder()
	{
		if(parentLevel>0)
		{
			EtchedBorder eb;
			if(isAltLangTable)
				//eb=new EtchedBorder(AppConstants.APP_COLOR_ALTs,AppConstants.APP_COLOR_BUTTON_BORDER);
				eb=new EtchedBorder(Color.red,Color.blue);
			else
				eb= new EtchedBorder();
			TitledBorder panelBorder= new TitledBorder(eb, tabledisplayName);
			panelBorder.setTitleFont(new java.awt.Font("Arial",1,12));

			if(parentReferenceType!=null && parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
			{
//				panelBorder.setTitleFont(new java.awt.Font("Arial",Font.ITALIC,12));

				this.setBorder(
						new TitledBorder(eb,"       "+tabledisplayName) {
							private static final long serialVersionUID = 7526472295622776147L;

							@Override
							public void paintBorder (Component c,
									Graphics g,
									int x,
									int y,
									int width,
									int height) {
								super.paintBorder (c, g, x, y, width, height);
								g.drawImage(new ImageIcon("res/images/reference.gif").getImage(), EDGE_SPACING + LEADING + TEXT_INSET_H, EDGE_SPACING + TEXT_SPACING, null);
							}
						});
				panelBorder.setTitleColor(Color.blue);
			}else
				this.setBorder(panelBorder);
		}
	}
	public boolean getIsPublicDefault()
	{
		return table.getIsPublicDefault();
	}

	private void getData() throws Exception
	{
		getData(false);
	}
	private void getData(boolean refresh)throws Exception 
	{
		dataSize=table.getTableIds(refresh).size();
		currentDataIndex= table.getCurrentDataIndex();
	}
	public ApplicationModes getApplicationMode()
	{
		return currentAppMode;
	}
	public void appModeChanged(ApplicationModes currentAppMode) throws Exception
	{
		this.currentAppMode=currentAppMode;

		switch (currentAppMode) {
		case DataEntry:
			//logger.error("------- "+tableName+":"+currentDataIndex);
			//currentDataIndex=0;
			getData(true);
			if(parentLevel==0)
				navPanel.updateControls();
			setTableData();
			break;
		case Search:
			searchResultState=false;
			dataSize=0;
			currentDataIndex=-1;
			navPanel.updateControls();

			setTableData(true);

			for (ElementPanel tElement : tableElements.values()) {
				tElement.setMode(true,false);
			}
			this.updateUI();
			break;
		default:
			break;
		}
		for (TablePanel tPanel : SubTablePanels.values()) {
			tPanel.appModeChanged(currentAppMode);
		}
	}
	/** 
	 * To indicate if the UI is in new search mode or search result mode. 
	 */
	private boolean searchResultState=false;
	public void searchStarted() throws Exception
	{
		table.setTableIdsFromSearch();
		getData();
		setDataCounterLabel();
		if(parentLevel==0)
		{
			setTableData();
			navPanel.updateControls();
		}
		for (TablePanel tPanel : SubTablePanels.values()) {
			tPanel.searchStarted();
		}
		searchResultState=true;
	}

	public boolean getSearchState()
	{
		return searchResultState;
	}
	public String getTableName()
	{
		return tableName;
	}
	public boolean isHelperTable()
	{
		return table.getTableProperties().getDBElementType().equals(DBElementTypes.HelperTable);
	}
	public DBTable getAssociateTable()
	{
		return table;
	}

	private int cPanelHeight,totalHeight;

	public void finalizeUI()
	{
		if(SubTablePanels.size()>0)
			for (TablePanel subTablePanel:	SubTablePanels.values()) {
				subTablePanel.finalizeUI();
			}
		else
		{
			//get the last component
			Component c = contentPanel.getComponent(contentPanel.getComponentCount()-1);
			cPanelHeight=(c.getY()+c.getHeight());
			totalHeight = cPanelHeight+75;
			this.setMaximumSize(new Dimension(20000,totalHeight));
		}
	}

	public void f()
	{ //this.navPanel.butExpand.doClick();
		if(SubTablePanels.size()>0)
			for (TablePanel subTablePanel:	SubTablePanels.values()) {
				// if(subTablePanel.parentLevel>0)
				{
					// logger.error(subTablePanel.tabledisplayName+"1111:"+subTablePanel.totalHeight);
					subTablePanel.navPanel.butExpand.doClick();
					//this.navPanel.butExpand.doClick();
					this.updateUI();
					subTablePanel.f();
				}
			}
		else
		{
			if(parentLevel>0)
			{
				//logger.error(tabledisplayName+"2222");
				//this.navPanel.butExpand.doClick();
			}
		}
	}

	public int getParentLevel()
	{
		return parentLevel;
	}
	public int getCurrentDataIndex()
	{
		return currentDataIndex;
	}
	public int getCurrentDataID()
	{
		return table.getCurrentDataID();
	}
	public int getDataSize()
	{
		return dataSize;
	}
	public TablePanel getParentPanel()
	{
		return parentPanel;
	}
	public Color getBGColor()
	{
		return bgColor;
	}
	public Boolean isMultipleMode()
	{
		return isMultipleMode;
	}

	//------------------- DATA CONTROL ELEMENTS ---------

	/**
	 * Adds the specified table panel in the content panel.
	 * @param subTablePanel The data panel of the child table.
	 * @param lastTable Specifies if the table to be added is the last element or not.
	 */
	public void addTablePanel(TablePanel subTablePanel)
	{
		contentPanel.add(subTablePanel);

		subTablePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		subTablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
		SubTablePanels.put(subTablePanel.tableName, subTablePanel);
	}

	/**
	 * Adds the data element control in the content panel.
	 * @param elementName The name of the element.
	 * @param elementPanel The element control.
	 */
	public void addElementUI(String elementName, ElementPanel elementPanel)
	{
		tableElements.put(elementName, elementPanel);

		elementPanel.setAlignmentY(SwingConstants.TOP);
		elementPanel.setAlignmentX(SwingConstants.LEFT);
		contentPanel.add(elementPanel);
	}

	//------------------- DATA ACCESS ---------
	/**
	 * Gets the data of each attribute in the table from the DB
	 * and displays them in corresponding controls.
	 */
	public void setTableData()
	{
		setTableData(false);
	}
	public void setTableData(boolean newFields)
	{
		try {
			if(!newFields)
				setDataCounterLabel();
			else
				navPanel.setDataCounterLabel();

			//Set is public value
			navPanel.setIsPublic(table.getIsPublicDefault(), table.getIsPublic());
			for (DBTableElement relTable : table.getRelatedTables().values()) {

				ElementPanel contrl = tableElements.get(relTable.getNameDB());
				if(contrl!=null)
				{
					if(!newFields)
					{
						List<DBField> dataFieldList = relTable.getContentValueList(table.getCurrentDataID());

						contrl.setValue(dataFieldList);
					}
					else
					{
						table.setCurrentDataIndex(-1);
						contrl.setValue(new ArrayList<DBField>());
					}
				}
				else
					logger.error("CONTROL NOT FOUND: "+relTable.getNameDB());

			}				 
			this.updateUI();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	/**
	 * Sets the data counter label.
	 */
	public void setDataCounterLabel()
	{
		currentDataIndex= table.getCurrentDataIndex();

		navPanel.setDataCounterLabel();

		for (TablePanel subTablePanel:	SubTablePanels.values()) {
			try {
				subTablePanel.parentIDChanged(table.getCurrentDataID());
				subTablePanel.navPanel.updateControls();
			} catch (Exception e) {
				MainUIFrame.setStatusMessage("Error Updating data index: "+e.getMessage());
				logger.error("Error Updating data index: ",e);
			}
		}
	}
	int lastParentID=-1;
	public void parentIDChanged(int updatedID) throws Exception
	{
		//if(lastParentID!=updatedID)
		{
			//if(updatedID!=-1)
			table.parentCurrentDataIDChanged(updatedID);

			dataSize=table.getTableIds(true).size();
			setTableData();
			lastParentID=updatedID;
		}
	}

	// ------------ BROWSE MODE ----------
	/**
	 * Takes necessary step when the current data index
	 * is changed in browsing mode.
	 * @param indxMove Moved index step. <br>
	 * <code>+1</code>: Next Data <br>
	 * <code>-1</code>: Previous Data
	 */
	public void changeDataIndx(int indxMove)
	{
		logger.debug("Index moved "+indxMove);
		try {
			int currDataID=table.changeDataIndx(indxMove);
			if(currDataID>-1)
				dataIndexChanged();
		} catch (Exception e) {
			logger.error("Error changing index: ",e);
			MainUIFrame.setStatusMessage("Error changing index: "+e.getMessage());
		}
	}
	private void dataIndexChanged()
	{
		setTableData();

		navPanel.updateControls();
	}
	// ------ DELETE TABLE DATA
	public void deleteData()
	{
		//display warning
		String warnmsg = "<html>This will delete all data in the table including data of all sub tables if exists.<br>" +
		"Do you really want to delete?";
		warnmsg+="</html>";
		int res = JOptionPane.showConfirmDialog(null, warnmsg, "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,new ImageIcon("res/images/warn.gif"));
		if(res==0)
		{
			MainUIFrame.setIsPerformingTask(true);
			MainUIFrame.setStatusMessage("Deleting data...");
			new Thread()
			{
				public void run(){
					List<String> delQueryList;
					try {
						delQueryList = getDeleteQueries(table.getTableName(),table.getCurrentDataID());
						DBSqlHandler.getInstance().executeTransactionQuery(delQueryList);	
						MainUIFrame.setStatusMessage("Data deleted. Refreshing data...");
						getData(true);
						dataIndexChanged();
						navPanel.setDataCounterLabel();

						navPanel.updateControls();

						MainUIFrame.setIsPerformingTask(false);
						MainUIFrame.setStatusMessage("Data deleted.");
					} catch (DBException e) {
						if(e.getErrorCode()==DBException.APPLICATION_MEMORY_ERROR)
						{
							System.gc();
							System.runFinalization();
							System.gc();

							MainUIFrame.setIsPerformingTask(false);
							//display Error message
							JOptionPane.showMessageDialog(null, "<html>You are trying to delete lots of data" +
									"at once. <br>Please try to delete less amount of data first.</html>", "Delete Data Error", JOptionPane.ERROR_MESSAGE);
							MainUIFrame.setStatusMessage("Deleting data failed!",MessageLevel.error);

						}
						else
						{
							MainUIFrame.setIsPerformingTask(false);
							MainUIFrame.setStatusMessage("Deleting data failed!",MessageLevel.error);
						}
					} catch (Exception e) {
						MainUIFrame.setIsPerformingTask(false);
						MainUIFrame.setStatusMessage("Deleting data failed!",MessageLevel.error);
					}
				}
			}.start();
		}
	}

	/**
	 * Returns list of delete queries to delete all relevant data of the table.
	 * @return List of delete queries to delete all relevant data of the table.
	 * @throws Exception 
	 * @throws DBException 
	 */
	public List<String> getDeleteQueries(String delTableName, int parentID) throws DBException, Exception
	{
		List<String> delQueryList;
		try {
			int currID = table.getCurrentDataID();
			String parentConnTableName = table.getParentConnectingTableName();
			delQueryList = new ArrayList<String>();

			int dataID=parentID;
			if(delTableName.equals(table.getTableName()))
			{
				dataID=currID;
			}
//			logger.error(" --- Delete "+table.getTableName());
			//	Get delete query to delete the entry in this table
			delQueryList.add("DELETE FROM "+table.getTableName()+" WHERE ID="+dataID);
			// Query to delete the entry from the parent connecting table
			if(parentConnTableName!=null)
				delQueryList.add("DELETE FROM "+parentConnTableName+" WHERE ID2="+dataID);

			// Get delete queries from table elements
			for (ElementPanel elPanel : tableElements.values()) {
				if(delTableName.equals(table.getTableName()))
				{
//					logger.error(" +++++ "+elPanel.getNameDB());
					delQueryList.addAll(elPanel.getDeleteElementQueries());
				}
				else
				{

					//get ids of element data ids
					String connTableName = elPanel.getParentConnectingTableName();
					String tableN=table.getTableName();
					String q = "SELECT "+connTableName+".ID2"+" FROM "+connTableName+","+tableN+
					" WHERE "+tableN+".ID="+connTableName+".ID1 AND "+tableN+".ID="+dataID;

					//logger.error(" הההההה "+q);
					List<Integer> elementIds = DBSqlHandler.getInstance().getGetIntegerList(q,"ID2");

					List<AlternateDetails> alternates = elPanel.getAlternateDetails();

					for (Integer elDataID : elementIds) {
//						logger.error("+++++000 "+elPanel.getNameDB()+" : "+elDataID);
						//delete element entry 
						delQueryList.add("DELETE FROM "+elPanel.getNameDB()+" WHERE ID="+elDataID);
						//delete parent connection entry
						delQueryList.add("DELETE FROM "+connTableName+" WHERE ID2="+elDataID);

						// get alt list
						for (AlternateDetails altDetails : alternates) {
							String altTableN =altDetails.getAlternateElementNameDB();
							String altTableConn = altDetails.getAlternateConnectingNameDB();
							//logger.error(" ###### "+elPanel.getNameDB()+":"+ altDetails.getAlternateTypeString()+":"+altDetails.getAlternateConnectingNameDB());
							//get ids of the alternates
							String qq = "SELECT "+altTableConn+".ID2 FROM "+altTableN+","+altTableConn+
							" WHERE "+altTableN+".ID="+altTableConn+".ID1 AND "+altTableN+".ID="+elDataID;


							List<Integer> altIds = DBSqlHandler.getInstance().getGetIntegerList(qq,"ID2");
							for (Integer altDataId : altIds) {
//								logger.error(" ###### "+elPanel.getNameDB()+":"+altDataId);
								delQueryList.add("DELETE FROM "+altTableN+" WHERE ID="+altDataId);
								delQueryList.add("DELETE FROM "+altTableConn+" WHERE ID2="+altDataId);
							}

							//delQueryList.add("DELETE FROM "+altTableN+" WHERE ID="+);
						}
						//delQueryList.add("DELETE FROM "+elPanel.getParentConnectingTableName())
					}
				}

				for (TablePanel subTablePanel : SubTablePanels.values()) {
					//get list of IDS of relevant elements
					String q = "SELECT "+subTablePanel.getParentConnectingTableName()+".ID"+
					" FROM "+subTablePanel.getParentConnectingTableName()+","+tableName+
					" WHERE "+tableName+".ID="+subTablePanel.getParentConnectingTableName()+".ID1 AND "+ 
					tableName+".ID="+parentID;
//					logger.error(" ****** "+subTablePanel.getTableName());
					List<Integer> subTableElementIDs = DBSqlHandler.getInstance().getGetIntegerList(q);
					// for each ids
					for (Integer integer : subTableElementIDs) {
						delQueryList.addAll(subTablePanel.getDeleteQueries(delTableName,integer));
					}
				}
			}
			return delQueryList;
		} catch (java.lang.OutOfMemoryError e) {

			throw new DBException(DBException.APPLICATION_MEMORY_ERROR);
		}
		//return new ArrayList<String>();

	}
	public List<String> getElementValuesDeleteQueries(int parentTableID)
	{
		List<String> delQueryList = new ArrayList<String>();

		return delQueryList;
	}
	protected String getParentConnectingTableName()
	{
		return table.getParentConnectingTableName();
	}

	// ----------- UPDATE / NEW DATA --------
	public void saveData(boolean newData) throws DBException,Exception
	{
		logger.error("Saving Changed Data: IS IT NEW "+newData);

		if(parentReferenceType!=null&&parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
			saveReferenceData(newData);
		else		
			saveNormalData(newData);


	}
	private void saveNormalData(boolean newData) throws Exception
	{
		Boolean doSave=false;
		if(tableElements.size()<1)
			doSave=true;

		// check for each element if the element has been modified.
		for (ElementPanel elpanel : tableElements.values()) {
			try {
				Boolean _doSave = elpanel.isSaveNecessary();
				if(!doSave)
					doSave=_doSave;
			} catch (DBException e) {
				logger.warn("Warning checking if the data should be saved: "+e.getDetailedMsg());
				MainUIFrame.setStatusMessage("Data not saved: "+e.getDetailedMsg());
				throw e;
			}
		}

		if(!doSave)
		{
			//	Check if the isPublic value is updated
			doSave=table.getIsPublic()!=navPanel.getIsPublic();
		}

		if(logger.isDebugEnabled())
			logger.debug("DO SAVE DATA "+doSave);
		if(!doSave)
			throw new DBException(DBException.SAVE_UNNECESSARY,"Nothing changed.");
		else
		{
			// if the data is updated
			if(!newData)
			{
				try {
					logger.debug("Updating table.");
					//	Check if the isPublic value is changed of the table
					if(table.getIsPublic()!=navPanel.getIsPublic())
					{
						//Update isPublic value
						String sqlStatement = "UPDATE "+tableName+
						" SET Public='"+ DBConstants.getIsPublicVal(navPanel.getIsPublic())+"'"+
						" WHERE ID="+table.getCurrentDataID();

						if(logger.isDebugEnabled())
							logger.debug("Update isPublic:"+sqlStatement);

						try {
							DBSqlHandler.getInstance().executeQuery(sqlStatement);
							table.setIsPublic(navPanel.getIsPublic());
						} catch (Exception e) {
							throw new DBException(DBException.SQL_EXCEPTION,"Error updating isPublic val of '"+tableName+"': "+e.getMessage());
						}
					}

					saveNewSubTableData(newData);

					// navPanel.updateControls();
				} catch (Exception e) {
					throw new DBException(DBException.SQL_EXCEPTION,"Error updating table:"+e.getMessage());
				}
			}
			else // if the new entry is made
			{
				DBSqlHandler sqlHandler;
				try {
					sqlHandler = DBSqlHandler.getInstance();
				} catch (Exception e2) {
					throw new DBException(DBException.SQL_EXCEPTION,"Error getting DB connection handler: "+e2.getMessage());
				}

				logger.debug("Adding new table data.");

				//Insert Row in table
				String sqlStatement = "INSERT INTO "+tableName+" (CreationDate,Public)"+ 
				"VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+DBConstants.getIsPublicVal(navPanel.getIsPublic())+"')";

				if(logger.isDebugEnabled())
					logger.debug("Insert CT:"+sqlStatement);

				try {
					sqlHandler.executeQuery(sqlStatement);
				} catch (Exception e) {
					throw new DBException(DBException.SQL_EXCEPTION,"Error inserting new data in table '"+tableName+"': "+e.getMessage());
				}

				//Get ID of the inserted row
				sqlStatement ="SELECT ID  FROM "+tableName+" order by ID DESC limit 1";
				String ID1;
				try {
					// get the last added row ID
					ID1 = DBSqlHandler.getInstance().executeGetValue(sqlStatement);

					if(logger.isDebugEnabled())
						logger.debug("GETID '"+ID1+"' ");
				} catch (Exception e) {
					// TODO: Rollback
					throw new DBException(DBException.SQL_EXCEPTION,"Error getting of the newly added data in table '"+tableName+"': "+e.getMessage());
				}
				//add relationhship with the parent table.
				if(table.getParentTable()!=null)
				{
					sqlStatement="INSERT INTO "+table.getParentConnectingTableName()+" (CreationDate,ID1,ID2)"+ 
					"VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+table.getParentCurrentDataID()+"','"+ID1+"')";
					logger.debug("Insert Relationship: "+sqlStatement);
					try {
						sqlHandler.executeQuery(sqlStatement);
					} catch (Exception e1) {
						throw new DBException(DBException.SQL_EXCEPTION,"Error adding parent relationship detail.'"+sqlStatement+"': "+e1.getMessage());
					}
				}


				try{
					table.setTableIdsFromDB();
					//Update the current id of the table to the latest one
					dataSize++;
					currentDataIndex = dataSize-1;
					//get all ids from the DB - to get the newly added id
					table.getTableIds(false);
					table.setCurrentDataIndex(currentDataIndex);

				} catch (Exception e1) {
					logger.error("Error Setting ID.",e1 );
					// TODO: Rollback
					MainUIFrame.setStatusMessage("Data NOT added.");
					return;
				}
				try {

					saveNewSubTableData(newData);

					dataIndexChanged();
				} catch (Exception e) {
					throw e; 
				}
			}
		}
	}
	private void saveReferenceData(boolean newData)throws Exception
	{
		String query;
		//update data
		if(!newData)
		{
//			logger.error("Save takes reference from data");
			StringBuffer sqlStatement = new StringBuffer("UPDATE ");
			sqlStatement.append(table.getParentConnectingTableName()); // from part
			sqlStatement.append(" SET ID2=");
			sqlStatement.append(referenceUpdateID);
			sqlStatement.append(",");
			sqlStatement.append(DBConstants.FIELD_CREATION_DATE);
			sqlStatement.append("=");
			sqlStatement.append(DBConstants.SQL_TIMESTAMP);
			sqlStatement.append(" WHERE ");
			sqlStatement.append(DBConstants.FIELD_ID);
			sqlStatement.append("=");
			sqlStatement.append(parentPanel.getCurrentDataID());
			query=sqlStatement.toString();
		}
		//save new data
		else
		{
			logger.error(" SAVE NEW DATA "+tableName+"_"+parentPanel.lastParentID+"::"+referenceUpdateID+table.getParentConnectingTableName());
			query="INSERT INTO "+table.getParentConnectingTableName()+" (CreationDate,ID1,ID2)"+ 
			" VALUES ("+DBConstants.SQL_TIMESTAMP+","+parentPanel.lastParentID+","+referenceUpdateID+")";
		}
		if(logger.isDebugEnabled())
			logger.debug(query);
		try {
			DBSqlHandler.getInstance().executeQuery(query);
			referenceUpdateID=-1;
			if(newData)
			{
				table.setTableIdsFromDB();
				//Update the current id of the table to the latest one
				dataSize++;
				currentDataIndex = dataSize-1;
				//get all ids from the DB - to get the newly added id
				table.getTableIds(false);
				table.setCurrentDataIndex(currentDataIndex);
				dataIndexChanged();
			}
		} catch (Exception e) {
			logger.error("Error executing update statement."+e.getMessage());
			referenceUpdateID=-1;
			throw e;
		}

	}
	private Boolean saveNewSubTableData(boolean newData) throws Exception
	{
		for (ElementPanel elpanel : tableElements.values()) {
			if(elpanel.isTableValueElement())
				elpanel.updateTableData(table.getCurrentDataID());
			else
				elpanel.saveDataField(table.getCurrentDataID(),newData);
		}
		return true;
	}
	public void refreshTableElement()
	{
		for (ElementPanel elpanel : tableElements.values()) {
			elpanel.resetValue();
			elpanel.setMode(true);
		}
	}
	public void freezeSubTableNavControls()
	{
		for (TablePanel childPanel : SubTablePanels.values()) {
			childPanel.freezeChildNavControl();
		}
	}
	/**
	 * Disables all navigation panels including the navigation panels of the child elements.
	 */
	private void freezeChildNavControl()
	{
		//freeze nav controls
		navPanel.freezeNavControls();
		for (TablePanel childPanel : SubTablePanels.values()) {
			childPanel.freezeChildNavControl();
		}
	}

	public void freezeParentTableNavControls()
	{
		if(parentPanel!=null)
		{
			parentPanel.navPanel.freezeNavControls();
			parentPanel.freezeParentNavControl();
		}
	}

	private void freezeParentNavControl()
	{
		if(parentPanel!=null)
		{
			parentPanel.navPanel.freezeNavControls();
			parentPanel.freezeParentNavControl();
		}
	}
	public void updateSubTableNavControls()
	{
		for (TablePanel childPanel : SubTablePanels.values()) {
			childPanel.navPanel.updateControls();
		}
	}

	// ------------ UPDATE CONTROLS ----------
	public void modeChanged(Boolean editMode)
	{
		for (ElementPanel elpanel : tableElements.values()) {
			elpanel.setMode(editMode);
		}
	}
	public void updateParentControls()
	{
		if(parentPanel!=null)
		{
			parentPanel.navPanel.updateControls();
			parentPanel.updateParentControls();
		}
	}

//	------------------- UI Elements --------------------------
	private JPanel tPanel,blankLeftPanel;
	private void initGUI() {
		try {
			blankLeftPanel = new JPanel();
			blankLeftPanel.setBackground(this.getBackground());

			BorderLayout tablePanelLayout = new BorderLayout();
			this.setLayout(tablePanelLayout);

			tPanel = new JPanel();
			tPanel.setLayout(new BorderLayout());

			this.add(blankLeftPanel,BorderLayout.WEST);
			this.add(tPanel,BorderLayout.CENTER);

			// Init navigation panel
			navPanel=new NavigationPanel(tabledisplayName,this);
			tPanel.add(navPanel, BorderLayout.NORTH);

			// Init Content Panel
			initContentPanel();
			setElementTooltip();
		} catch (Exception e) {
			logger.error("Error Initialising UI.",e);
		}
	}
	private void setElementTooltip()
	{
		AppConstants.ElementTooltipOptions tooltipType;
		String tooltip="";
//		set tooltip
		try {
			tooltipType=AppPropertyManager.getDBPropertyManagerInstant().getElementTooltipType();

		} catch (DBException e) {
			logger.warn("Error getting tooltip type from the property manager: "+e.getMessage());
			//default
			tooltipType= AppConstants.ElementTooltipOptions.Description;
		}
		switch (tooltipType) {
		case Description:
			tooltip="<b>"+tabledisplayName+"</b><br>"+
			table.getTableProperties().getDescription();
			break;
		case EnglishDescription:
			tooltip="<b>"+tabledisplayName+"</b><br>"+
			table.getTableProperties().getEnglishDescription();
			break;
		case Description_detail:
			tooltip="<b>"+tabledisplayName+"</b><br>"+
			table.getTableProperties().getDescription();
			break;
		case EnglishDescription_detail:
			tooltip="<b>"+tabledisplayName+"</b><br>"+
			table.getTableProperties().getEnglishDescription();
			//int 
			if(table.getTableProperties().getAlternateLanguageTables().size()>0)
				tooltip+="<br><b>Alt.Lang:"+table.getTableProperties().getAlternateLanguageTables().size();
			break;
		default:
			break;
		}
		if(logger.isDebugEnabled())
		{
			tooltip+="<br>STR:"+table.getParentStructure();
			tooltip+="<br>TN:"+table.getTableName();
		}
		this.setToolTipText(MainUIFrame.getToolTipString(tooltip));
		contentPanel.setToolTipText(MainUIFrame.getToolTipString(tooltip));
	}
	public void updateElementTooltip()
	{
		//update the tooltip of the table control
		setElementTooltip();
		//update the tooltip of elements
		for (ElementPanel tabElemnt : tableElements.values()) {
			tabElemnt.setElementTooltip();
		}
		for (TablePanel subTPanel : SubTablePanels.values()) {
			subTPanel.updateElementTooltip();
		}
	}
	private void initContentPanel()
	{
		contentPanel = new JPanel();
		contentPanel.setBackground(this.getBackground());
		BoxLayout contentPanelLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);

		contentPanel.setLayout(contentPanelLayout);

		//Add Scroll bar
		scrollPanelContent = new JScrollPane(contentPanel);
		scrollPanelContent.getVerticalScrollBar().setUnitIncrement(100);
		scrollPanelContent.setBorder(null);
		scrollPanelContent.setWheelScrollingEnabled(true);
		tPanel.add(scrollPanelContent, BorderLayout.CENTER);
		
		// contentPanel.setToolTipText(MainUIFrame.getToolTipString("Table: <b>"+tabledisplayName+"</b>"));
	}

	/**
	 * Expands or shrinks the content panel for better viewing of other elemnts.
	 * @param shrink To shrink or to expand the view.
	 */
	public void expandShrinkContentPanel(Boolean shrink)
	{
		if(cPanelHeight!=contentPanel.getHeight())
			cPanelHeight=contentPanel.getHeight();
		totalHeight=cPanelHeight+75;

		contentPanel.setVisible(!shrink);
		if(shrink)
		{
			contentPanel.setSize(new Dimension(parentPanel.getWidth()-blankLeftPanel.getWidth()-100,0));
			this.setMaximumSize(new Dimension(20000,50));
		}
		else
		{
			contentPanel.setSize(new Dimension(parentPanel.getWidth()-blankLeftPanel.getWidth()-100,cPanelHeight));
			this.setMaximumSize(new Dimension(20000,totalHeight));
		}
		tPanel.setPreferredSize(new Dimension(contentPanel.getWidth()+blankLeftPanel.getWidth(),cPanelHeight+navPanel.getHeight()));
		tPanel.setSize(new Dimension(contentPanel.getWidth()+blankLeftPanel.getWidth(),cPanelHeight+navPanel.getHeight()));

		this.updateUI();
		this.updateParentUI();
		this.getTopLevelAncestor().repaint();
		this.getParent().repaint();
	}
	public void updateParentUI()
	{
		if(parentPanel!=null)
		{
			parentPanel.updateUI();
			parentPanel.updateParentUI();
		}
	}

	public Hashtable<String,MainUIFrame.SearchParams> getSearchParams(Hashtable<String,MainUIFrame.SearchParams> searchParams)
	{

		for (ElementPanel elPanel : tableElements.values()) {
			if(!elPanel.getValue().equals(""))
				searchParams.put(elPanel.getName(), new MainUIFrame.SearchParams(tableName,table.getViewName(),elPanel.getName(),elPanel.getParentConnectingTableName(), elPanel.getValue(),elPanel.isTableValueElement()));
		}
		for (TablePanel childPanels : SubTablePanels.values()) {
			childPanels.getSearchParams(searchParams);
		}
		return searchParams;
	}

	//	 ------------ DATA NAVIGATION ----------
	public void changeDataIndex(int index)
	{
		currentDataIndex=index;
		table.setCurrentDataIndex(currentDataIndex);
		dataIndexChanged();
	}
	/**
	 * ID of the updated reference. Used when editing element with
	 * TakesReferenceFrom property.
	 */
	private int referenceUpdateID=-1;

	public void setReferenceUpdateID(int referenceUpdateID)
	{
		this.referenceUpdateID=referenceUpdateID;
		try {
			//Set is public value
			for (DBTableElement relTable : table.getRelatedTables().values()) {
				if(!relTable.getNameDB().equals(this.getTableName()))
				{
					ElementPanel contrl = tableElements.get(relTable.getNameDB());
					if(contrl!=null)
					{
						List<DBField> dataFieldList = relTable.getContentValueList(referenceUpdateID);
						contrl.setValue(dataFieldList);
					}
				}

			}				 
			this.updateUI();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	public int getReferenceUpdateID()
	{
		return referenceUpdateID;
	}
}
