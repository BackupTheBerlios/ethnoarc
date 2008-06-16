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
package de.fhg.fokus.se.ethnoarc.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBConstants.TableReferenceTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.ApplicationModes;

/**
 * $Id: DBTable.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $
 * 
 * @author fokus
 */
public class DBTable implements Serializable{

	private static final long serialVersionUID = -5754224179623392012L;

	// ------------- ATTRIBUTES ----------------------------
//	Logger logger = Logger.getLogger(MainUIFrame.class.getName());

	/**
	 * List of related tables (elements).
	 */
	private Hashtable<String, DBTableElement> tableElementList = new Hashtable<String, DBTableElement>();

	/**
	 * List of related tables which are itself a combined table.
	 */
	private Hashtable<String, DBTable> tableList = new Hashtable<String, DBTable>();

	/**
	 * List of child element names to sort the child elements. This list is used
	 * only for internal purpose.
	 */
	private Hashtable<Integer, TableElement> sortedElementsList = new Hashtable<Integer, TableElement>();

	/**
	 * The tablename of the combined table.
	 */
	private String tableName = "";

	/**
	 * The name of the combined table.
	 */
	private String displayName = "";

	/**
	 * Indicates if the value of the table and its child elements are public
	 * by default. If <code>true</code> then its values are made public. Otherwise,
	 * its values, including value of its child elements are private and are not
	 * visible for search.
	 */
	private boolean isPublicDefault=true;

	/**
	 * The parent combined table. <code>Null</code> if no parent exist.
	 */
	private DBTable parentTable = null;

	/**
	 * The name of the connecting table describing the relationship between the
	 * parent and this table.
	 */
	private EADBDescription connectingTable = null;

	/**
	 * Properties of the table.
	 */
	private EADBDescription tableProperties=null;

	/**
	 * Specifies if the access mode of the current data made public or private.<br>
	 * <code>true</code>: The data should be <i>public</i>.<br>
	 * <code>false</code>: The data should be <i>private</i>.
	 */
	private Boolean isPublic = true;
	
	private Boolean isRootTable = false;

	/**
	 * Select statement part of the SQL query statement for search purpose.
	 */
	private String searchSelectString;

	public DBTable(EADBDescription tableProperties)
	{
		this.tableProperties=tableProperties;
		tableName=this.tableProperties.getNameDB();
		displayName=this.tableProperties.getDisplayname();

		searchSelectString ="SELECT DISTINCT "+ tableName+".ID,"+tableName+".CreationDate,"+tableName+".Public,"+tableName+".Content";
	}
	public EADBDescription getTableProperties()
	{
		return tableProperties;
	}
	/**
	 * Sets if the values associated with the table is public or private.<p>
	 * <b>Note:</b> If the value specified in the db is <code>true</code> but the value of the parent is 
	 * <code>false</code> then publicDefault value of this table is set to <code>false</code>.  
	 */
	public void setIsPublicDefault()
	{
		isPublicDefault=tableProperties.getIsPublicDefault();
		if(isPublicDefault)//check the public default of the parent
		{
			if(parentTable!=null &&!parentTable.getIsPublicDefault())
				isPublicDefault=false;
		}		
		isPublic=isPublicDefault;
	}
	private boolean isPubdefIdentified=false;
	/**
	 * Gets if the values associated with the table is public or private.<p>
	 * <b>Note:</b> If the value specified in the db is <code>true</code> but the value of the parent is 
	 * <code>false</code> then publicDefault value of this table is also <code>false</code>.  
	 * @return <code>true</code>: public; <code>false</code>: private.
	 */
	public boolean getIsPublicDefault()
	{
		if(!isPubdefIdentified)
		{
			setIsPublicDefault();
			isPubdefIdentified=true;
		}
		return isPublicDefault;
	}

	// ------------- TABLES METHODS ----------------------------
	public void addRelatedTable(DBTableElement tableElement) {
//		logger.error(" ####### "+tableElement.tableName+":"+tableElement.getElementDescription().getNameDB()+"-"+ tableElement.getRelationDescrTableName());
		TableReferenceTypes refType=null;
		if(tableElement.getRelationDescrTableName()!=null)
			refType= TableReferenceTypes.valueOf(tableElement.getRelationDescriptionTable().getType());
		tableElementList.put(tableElement.getNameDB(), tableElement);
		sortedElementsList
		.put(Integer.decode(tableElement.getOrderNumber() + ""),
				new TableElement(DBConstants.ELEMENT_TYPE.ELEMENT,
						tableElement,refType));
		setChildTableParentStructure(tableElement);
		//if(tableElement.getNameDB().equals("wl_095Locality_046DefExpression"))
		//if(tableElement.getTableName().equals("wl_095Locality"))
		//	logger.error("HIPPP "+tableElement.getOrderNumber()+":"+tableElement.getNameDB()+":"+sortedElementsList.size());
	}


	public void addChildCBTable(DBTable childTable) {
//		logger.error("********** "+childTable.getTableName()+" Parent: "+ childTable.getParentConnectingTable().getType());
		tableList.put(childTable.getTableName(), childTable);	
		sortedElementsList.put(Integer.decode(childTable.getParentConnectingTable()
				.getOrderNumber()
				+ ""), new TableElement(DBConstants.ELEMENT_TYPE.TABLE,
						childTable,TableReferenceTypes.valueOf(childTable.getParentConnectingTable().getType())));
		setChildTableParentStructure(childTable);
	}
	String parentStructure;

	/**
	 * Creates the parent structure of the added child table element. E.g., <code>Adressenliste.Adresse.Stadt</code>.
	 * @param newTableElement Newly added table element.
	 */
	public void setChildTableParentStructure(DBTableElement newTableElement)
	{
		if(this.parentStructure==null)
			parentStructure=tableName;

		newTableElement.parentStructure=parentStructure+"."+newTableElement.getNameDB();
	}
	/**
	 * Creates the parent structure of the added child table. E.g., <code>Adressenliste.Adresse</code>.
	 * @param newCBTable Newly added table.
	 */
	public void setChildTableParentStructure(DBTable newCBTable)
	{
		if(this.parentStructure==null)
			parentStructure=tableName;
		newCBTable.parentStructure=parentStructure+"."+newCBTable.tableName;

	}
	/**
	 * Gets the parent structure of the table.
	 * @return The parent table. E.g., <code>Adressenliste.Adresse</code>.
	 */
	public String getParentStructure()
	{
		if(parentStructure==null)
			return tableName;
		else
			return parentStructure;
	}

	public Hashtable<String, DBTable> getChildCBTableList() {
		return tableList;
	}

	public void setParentTable(DBTable parentCBTable) {
		this.parentTable = parentCBTable;
	}

	public DBTable getParentTable() {
		return parentTable;
	}

	public String getTableName() {
		return this.tableName;
	}

	public String getTableDisplayName() {
		return this.displayName;
	}

	public Hashtable<String, DBTableElement> getRelatedTables() {
		return tableElementList;
	}
	public DBTableElement getElement(String elementName)
	{
		return tableElementList.get(elementName);
	}
	public String getElementConnectionTableName(String elementName)
	{
		DBTableElement tElement = tableElementList.get(elementName);
		if(tElement!=null)
			return tElement.getRelationDescrTableName();
		else
			return null;
	}

	/*{
		List<TableElement> tbElements = new ArrayList<TableElement>();
		Vector<String> v2 = new Vector<String>(tableElementList.keySet());
		//Collections.sort(v2);
		Iterator it2 = v2.iterator();

		while (it2.hasNext()) {
			tbElements.add(tableElementList.get(it2.next()));
		}
		return tbElements;
	}*/
	public List<TableElement> getSortedTableElements() {
		List<TableElement> sortedElements = new ArrayList<TableElement>();

		Vector<Integer> v2 = new Vector<Integer>(sortedElementsList.keySet());
		Collections.sort(v2);
		Iterator it2 = v2.iterator();

		while (it2.hasNext()) {
			sortedElements.add(sortedElementsList.get(it2.next()));
		}

		return sortedElements;
	}

	// ------------- DATA MANAGEMENT ----------------------------
	/**
	 * List of combined table IDs.
	 */
	private List<String> contentIDs;

	private LinkedHashMap<String, DBField> dataFields;

	/**
	 * Gets the list of IDs of the combined table.
	 * 
	 * @return IDs of the combined table.
	 * @throws Exception
	 */
	public List<String> getTableIds(boolean refresh) throws Exception {
		//logger.info("######## "+MainUIFrame.getApplicationMode().equals(ApplicationModes.Search));
		if(MainUIFrame.getApplicationMode().equals(ApplicationModes.Search))
		{	//logger.debug("####### "+tableName+":"+refresh);
			if(!refresh)
			{
				if (contentIDs == null)
				{
					setTableIdsFromSearch();
				}
			}
			else
				setTableIdsFromSearch();
		}
		else{
			if(!refresh)
			{
				if (contentIDs == null)
				{
					setTableIdsFromDB();
				}
			}
			else{
				setTableIdsFromDB();
			}
		}
		return contentIDs;
	}
	/**
	 * Gets the IDs from the DB.
	 * Example sql statement: <br> 
	 * <code><b>SELECT</b> Person.ID, Person.Content, Person.CreationDate, Person.Public <br>
	 * <b>FROM</b> Adresse,Person,Adresse_Contains_Person <br>
	 * <b>WHERE</b> ID1=Adresse.ID AND <br>
	 * ID2=Person.ID AND <br>
	 * Adresse.ID=1 <br>
	 * <b>ORDER BY</b> id</code><p>
	 * Example sql statement for search: <br>
	 * <code><b>SELECT DISTINCT</b> Person.ID,Person.CreationDate,Person.Public,Person.Content <br>
	 * <b>FROM</b> view_Adressenliste, view_Person, view_Adresse,Person <br>
	 * <b>WHERE</b> <br>
	 * view_Adresse.Person_id=view_Person.Person_id AND <br>
	 * view_Adressenliste.Adresse_id=view_Adresse.Adresse_id AND <br>
	 * view_Person.Person_ID=Person.ID  AND <br>
	 * Vorname LIKE 'ho%' AND <br>
	 * view_Adresse.Adresse_ID=3515 <br>
	 * <b>ORDER BY</b> id</code>
	 * 
	 * @throws Exception
	 */
	public void setTableIdsFromDB() throws Exception {
		String query="";
			//logger.debug("SET TABLEIDsFROM DB DEF "+tableName);
		if (parentTable == null) {
			query = "SELECT ID,CreationDate,Public,Content FROM " + tableName
			+ " ORDER BY id";
		} else {
//			if(connectingTable==null)
				//logger.error("öööööööööööö "+tableName);

			query = "SELECT " + tableName + ".ID, " + tableName + "."
			+ DBConstants.FIELD_CONTENT + ", " + tableName + "."
			+ DBConstants.FIELD_CREATION_DATE + ", " + tableName + "."
			+ DBConstants.FIELD_PUBLIC + " FROM "
			+ getParentTableName() + "," + tableName + ","
			+ connectingTable.getNameDB() + " WHERE " + "ID1="
			+ getParentTableName() + ".ID AND " + "ID2=" + tableName
			+ ".ID AND " + getParentTableName() + ".ID="
			+ getParentCurrentDataID() + " ORDER BY id";
		}
		
		setTableIdsFromDB(query);
	}
	public void setTableIdsFromSearch() throws Exception {
		String query="";
		//logger.debug("SET TABLEIDsFROM DB "+tableName);
		String searchFields = MainUIFrame.getSearchString(tableName);
		
		boolean doQuery=true;
		if(!searchFields.equals(""))
		{
			query= searchSelectString+ searchFields;

			if (parentTable != null) {
				int parentID=getParentCurrentDataID();
				if(parentID>-1)
					query+=" AND " + getParentTable().getTableName()+ ".ID="+ parentID + " ORDER BY id";
				else
				{
					doQuery=false;
				}
			}
			else
				query+= " ORDER BY id";

		}
		//logger.debug(tableName+" ###### "+query);

		if(doQuery)
			setTableIdsFromDB(query);
	}
	public void setTableIdsFromDB(String query) throws Exception {
		if(!query.trim().equals(""))
		{
			dataFields = DBSqlHandler.getInstance().getDataFields(query, tableName);

		//	if (logger.isDebugEnabled())
		//		logger.debug("QURY::" + query + " SIZE: " + dataFields.size());

			if (dataFields != null && dataFields.size() > 0) {
				contentIDs = new ArrayList<String>(dataFields.keySet());
				CurrentDataIndex = 0;
			} else {
				contentIDs = new ArrayList<String>();
				CurrentDataIndex = -1;
			}
			setOriginalAccessType();
		}
	}

	/**
	 * The index of the current data id.
	 */
	private int CurrentDataIndex = -1;

	public int getCurrentDataIndex() {
		return CurrentDataIndex;
	}

	public void setCurrentDataIndex(int dataIndex) {
		this.CurrentDataIndex = dataIndex;
		setOriginalAccessType();
	}

	public int changeDataIndx(int indxMove) throws Exception {
		//if(logger.isDebugEnabled())
		//	logger.info(tableProperties.getNameDB()+" change indx:"+indxMove);
		int newDataIndx = CurrentDataIndex + indxMove;

		if (newDataIndx < 0 || newDataIndx > (getTableIds(false).size() - 1))
			return -1;
		else {
			CurrentDataIndex = newDataIndx;
			setOriginalAccessType();
			return getCurrentDataID();
		}
	}

	public int getCurrentDataID() {
		if (contentIDs != null && CurrentDataIndex > -1) { // logger.error("dbc
			// "+CurrentDataIndex);
			int id = Integer.valueOf(contentIDs.get(CurrentDataIndex));
			return id;
		} else
			return -1;
	}

	/**
	 * Returns the current data ID of the parent table.
	 * 
	 * @return The current data ID of the parent table if the parent table
	 *         exists. Else return <code>-1</code>.
	 */
	public int getParentCurrentDataID() {
		if (parentTable != null)
			return parentTable.getCurrentDataID();
		else
			return -1;
	}

	/**
	 * Updates the list of data IDs based on the updated parent ID.
	 * 
	 * @param updatedID
	 *            The current data ID of the parent table.
	 * @throws Exception
	 */
	public void parentCurrentDataIDChanged(int updatedID) throws Exception {
		if(updatedID>-1)
			setTableIdsFromDB();
		else
		{
			CurrentDataIndex=-1;
			contentIDs=new ArrayList<String>();
		}
	}

	/**
	 * Gets the name of the parent table.
	 * 
	 * @return The parent table name. <code>Null</code> if parent table does
	 *         not exist.
	 */
	public String getParentTableName() {
		if (parentTable != null)
			return parentTable.tableName;
		else
			return null;
	}

	/**
	 * Sets the name of the connecting table describing relationship between
	 * this table and the parent table.
	 * 
	 * @param parentConnectingTableName
	 *            The name of the connectingt table describing relationship
	 *            between this table and the parent table.
	 */
	public void setParentConnectingTable(EADBDescription parentConnectingTable) {
		this.connectingTable = parentConnectingTable;
	}

	/**
	 * Gets the name of the connecting table describing relationship between
	 * this table and the parent table.
	 * 
	 * @return The name of the connecting table describing relationship between
	 *         this table and the parent table.
	 */
	public EADBDescription getParentConnectingTable() {
		return connectingTable;
	}

	public String getParentConnectingTableName() {
		if(connectingTable!=null)
			return connectingTable.getNameDB();
		else
			return null;
	}

	// ------- PUBLIC / PRIVATE ------------------------
	/**
	 * Gets the access type of the current data from the DB.
	 */
	public void setOriginalAccessType() {
		if (CurrentDataIndex > -1) {
			isPublic = dataFields.get(String.valueOf(getCurrentDataID()))
			.getIsPublic();
		}
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		dataFields.get(String.valueOf(getCurrentDataID()))
		.setIsPublic(isPublic);
		this.isPublic = isPublic;
	}
	/**
	 * Returns the name of the view created to represent this table. View is created
	 * and used to simplify search process.
	 * @return The name of the view.
	 */
	public String getViewName()
	{
		return AppConstants.VIEW_PREFIX+tableName;
	}
	
	
	public void setIsRootTable(boolean isRoot){
		isRootTable = isRoot;
	}
	
	public boolean getIsRootTable(){
		return isRootTable;
	}

	// ------- SORT ELEMENTS ----
	public class TableElement implements Serializable {

		private DBConstants.ELEMENT_TYPE elementType;

		/**
		 * The name of the element to be displayed.
		 */
		private String elementName;

		/**
		 * The id (table name) of the table to be used in the sql queries
		 */
		private String elementID;

		private DBTableElement childTableElement;

		private DBTable childCombinedTable;
		private TableReferenceTypes referenceType;

		public TableElement(DBConstants.ELEMENT_TYPE elementType,
				DBTableElement childTableElement,TableReferenceTypes referenceType) {
			this.childTableElement = childTableElement;
			this.elementName = childTableElement.getDisplayName();
			this.elementID = childTableElement.getNameDB();
			this.elementType = elementType;
			this.referenceType=referenceType;
//			logger.error("####**** "+childTableElement.getNameDB()+"::"+childTableElement.getRelationDescrTableName()+"-"+referenceType);
		}

		public TableElement(DBConstants.ELEMENT_TYPE elementType,
				DBTable childCombinedTable,TableReferenceTypes referenceType) {
			this.elementType = elementType;
			this.elementName = childCombinedTable.tableName;
			this.childCombinedTable = childCombinedTable;
			this.referenceType=referenceType;
//			if(childCombinedTable!=null)
//				logger.error("####****++++ "+childCombinedTable.getTableName()+"::"+this.elementName+"-"+referenceType);
		}

		public DBConstants.ELEMENT_TYPE getElementType() {
			return elementType;
		}

		public String getElementName() {
			return elementName;
		}

		public String getElementTableName() {
			return elementID;
		}

		public DBTableElement getChildTableElement() {
			return childTableElement;
		}

		public DBTable getCombinedTable() {
			return childCombinedTable;
		}
		public TableReferenceTypes getParentReferenceType()
		{
			return referenceType;
		}
		
	}
}
