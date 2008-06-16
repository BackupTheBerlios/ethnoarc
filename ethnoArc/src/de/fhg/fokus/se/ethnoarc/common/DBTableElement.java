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
import java.util.List;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame;

/**
 * 
 * This class contains properties and operation related to the elements of a table.
 * For example, element 'FirstName' of the table 'Person'.
 * 
 * This class provides operation to get the properties of the elements along with 
 * methods to get values of the element. <p>
 * 
 * $Id: DBTableElement.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus

 */
public class DBTableElement implements Serializable{

	//	-------- LOGGING -----
	//static Logger logger = Logger.getLogger(DBTableElement.class.getName());
	//---------------- ATTRIBUTES --------------------------

	/**
	 * Related description of the element.
	 */
	private EADBDescription elementDescription;

	/**
	 * The description of this element and the table.
	 */
	private EADBDescription relationDescriptionTable;
	/**
	 * The name of the table to which this element is belongs to.
	 */
	 String tableName;
	
	/**
	 * Indicates if the value of the table element is public
	 * by default. If <code>true</code> then its value is made public. Otherwise,
	 * its value is not 
	 */
	private boolean isPublicDefault=true;
	
	// SQL related
	private String sqlPartFrom;
	private String sqlPartWhere;
	private String sqlStatementGet;
	 
	/**
	 * SQL statement to get all (ID, creation date, isPublic, content) values.
	 */
	private String sqlStatementGetValues;
	
	private boolean isTableValue=false;
	/**
	 * Specifies if parent or grandparent is of type reference.
	 */
	private boolean parentTypeReferenced=false;

	//	---------------- CONSTRUCTOR --------------------------
	/**
	 * 
	 */
	public DBTableElement(EADBDescription elementDescription, 
			EADBDescription relationDescriptionTable, 
			String tableName,
			boolean isPublicDefault)
	throws Exception
	{
		if(elementDescription==null||tableName==null)
			throw new Exception("One of the specified parameter is null");

		this.elementDescription=elementDescription;
		this.relationDescriptionTable=relationDescriptionTable;
		this.tableName=tableName;
		this.isPublicDefault=isPublicDefault;
		//create sql statement
		String elementNameDB = elementDescription.getNameDB();
		String relDescrTableName;
		//if the relation description table is specified
		if(relationDescriptionTable!=null)
		{
			relDescrTableName = relationDescriptionTable.getNameDB();

			//create from part

			sqlPartFrom = tableName+","+ elementDescription.getNameDB()+","+relDescrTableName;
			sqlPartWhere = " WHERE " + tableName+".ID="+relDescrTableName+".ID1 AND "+
			elementNameDB+".ID="+relDescrTableName+".ID2";

			sqlStatementGet = "SELECT "+elementNameDB+".content "+elementNameDB+
			" FROM "+sqlPartFrom+sqlPartWhere;

			sqlStatementGetValues="SELECT "+
			elementNameDB+"."+DBConstants.FIELD_ID+", "+
			elementNameDB+"."+DBConstants.FIELD_CREATION_DATE+", "+
			elementNameDB+"."+DBConstants.FIELD_PUBLIC+", "+
			elementNameDB+"."+DBConstants.FIELD_CONTENT+
			" FROM "+sqlPartFrom+sqlPartWhere;
		}
		else // the table contains value
		{
			isTableValue=true;
			sqlStatementGet = "SELECT "+elementNameDB+".content "+elementNameDB+" FROM "+elementNameDB;
			sqlStatementGetValues="SELECT "+
			elementNameDB+"."+DBConstants.FIELD_ID+", "+
			elementNameDB+"."+DBConstants.FIELD_CREATION_DATE+", "+
			elementNameDB+"."+DBConstants.FIELD_PUBLIC+", "+
			elementNameDB+"."+DBConstants.FIELD_CONTENT+" FROM "+elementNameDB;
		}
			
		
		//if(logger.isDebugEnabled())
		//logger.debug("SQL: "+tableName+"_"+elementNameDB+"_"+sqlStatementGet);
		//logger.info("SQL: "+tableName+"_"+elementNameDB+"_"+sqlStatementGet+"\r\n"+sqlStatementGetValues);
	}
	public void setIsTableValue(boolean isTableValue)
	{
		this.isTableValue=isTableValue;
	}
	public void setIsParentTypeReferenced(boolean parentTypeReferenced)
	{
		this.parentTypeReferenced=parentTypeReferenced;
	}
	public boolean isParentTypeReferenced()
	{
		return parentTypeReferenced;
	}
	//	---------------- GET METHODS --------------------------
	
	/**
	 * Gets if the values associated with the table is public or private.<p>
	 * <b>Note:</b> If the value specified in the db is <code>true</code> but the value of the parent is 
	 * <code>false</code> then publicDefault value of this table is also <code>false</code>.  
	 * @return <code>true</code>: public; <code>false</code>: private.
	 */
	public boolean getIsPublicDefault()
	{
		return isPublicDefault;
	}

	/**
	 * Gets the name of the element in DB. 
	 * @return The name of th element in DB.
	 */
	public String getNameDB()
	{
		return elementDescription.getNameDB();
	}
	/**
	 * Gets the display name.
	 * @return The user friendly name to be displayed.
	 */
	public String getDisplayName()
	{
		return elementDescription.getDisplayname();
	}
	/**
	 * Gets the description (properties) of the element.
	 * @return the description of the element.
	 */
	public EADBDescription getElementDescription()
	{
		return elementDescription;
	}
	/**
	 * gets the description of the db object specifying the relation
	 * between this element with its parent table. 
	 * @return The description of the db object specifying the relation
	 * between this element with its parent table.
	 */
	public EADBDescription getRelationDescriptionTable()
	{
		return relationDescriptionTable;
	}
	/**
	 * Gets the name of the db element describing the relationship between
	 * this element with its parent table.
	 * @return The name of the db element describing the relationship between
	 * this element with its parent table.
	 */
	public String getRelationDescrTableName()
	{
		if(relationDescriptionTable!=null)
			return relationDescriptionTable.getNameDB();
		else
			return null;
	}
	/**
	 * Gets the type of the relation between the combined table and the related table. 
	 */
	public String getRelationType()
	{
		if(relationDescriptionTable==null)
			return relationDescriptionTable.getType();
		else
			return "";	
	}
	public boolean isTableValueElement()
	{
		return isTableValue;
	}
	/**
	 * Gets the name of the parent table.
	 * @return The parent table name.
	 */
	public String getTableName()
	{
		return tableName;
	}
	/**
	 * Gets value if the value of the related table is mandatory (must be specified) or not.
	 * @return <code>true</code>: The value is mandatory. <br>
	 * <code>false</code>: The value is <i>not</i> mandatory.
	 */
	public Boolean getIsMandatory()
	{
		if(relationDescriptionTable!=null)
			return relationDescriptionTable.getIsMandatory();
		else
			return false;
	}
	/**
	 * Gets value if the multiple value to the related table can be specified.
	 * @return <code>true</code>: Multiple value can be given. <br>
	 * <code>false</code>: Only single value is allowed.
	 */
	public Boolean getIsMultiple()
	{
		if(relationDescriptionTable!=null)
			return relationDescriptionTable.getMultiple();
		else
			return false;
	}
	/**
	 * The flag specifying if the element has a specific format.
	 * @return <code>true</code>: The element has a specific format. <br>
	 * <code>false</code>: The element does not have any format.
	 */
	public Boolean getHasFormat()
	{
		return getElementDescription().getFormat().length()>0;
	}
	/**
	 * Gets the format of the element.
	 * @return The format of the element.
	 */
	public String getFormat()
	{
		return getElementDescription().getFormat();
	}
	/**
	 * The order number of the related table. Used as an order number to display the related
	 * table as an elements of the table.
	 * @return The order number of the related table.
	 */
	public int getOrderNumber()
	{
		if(isTableValue)
			return 0;
		if(relationDescriptionTable!=null)
			return relationDescriptionTable.getOrderNumber();
		else
			return 0;
	}
	
	/** The parent structure of the elemnt. E.g., <code>Adressenliste.Adresse.Stadt</code>*/
	 String parentStructure;
	/**
	 * Gets the parent structure of the element. 
	 * E.g., <code>Adressenliste.Adresse.Stadt</code>
	 * @return The parent structure of the element.
	 */
	public String getParentStructure()
	{
		if(parentStructure==null)
		{
			return tableName+"."+elementDescription.getNameDB();
		}
		else
		{
			return parentStructure;
		}
	} 
	public String getRootTableName()
	{
		String parentStructure = getParentStructure();
		return parentStructure.substring(0,parentStructure.indexOf("."));
	}

	// -------------- SQL STATEMENTS -------------------
	/**
	 * Gets the SQL string to get all content value.
	 * @return SQL string to get all content value.
	 */
	public String getContentSqlString()
	{
		return sqlStatementGet;
	}
	/**
	 * Gets the SQL string to get the content value with the specified ID.
	 * @param id The unique idenfier of the data.
	 * @return The SQL string to get the content value with the specified ID.
	 */
	public String getContentSqlString(int id)
	{
		return sqlStatementGet + " AND "+tableName+".ID="+id;
	}
	/**
	 * Gets the value of the content specified by the id.
	 * @param id The id of the data.
	 * @return The value of the data. Returns empty string if the value does not exist.
	 */
	public String getContentValue(int id)
	{
		String val;
		String sqlStatement=sqlStatementGet + " AND "+tableName+".ID="+id;
		
		try {
			val = DBSqlHandler.getInstance().executeGetValue(sqlStatement);
			//logger.debug("The Val '"+val+"' "+sqlStatement);
		} catch (Exception e) {
			val="";
		}
		if(val==null)
			val="";
		return val;
	}
	public DBField getContentDataField(int id)
	{
		DBField val=null;
		String sqlStatement=sqlStatementGetValues + " AND "+tableName+".ID="+id;

		try {
			val = DBSqlHandler.getInstance().getDataField(sqlStatement,elementDescription.getNameDB());
		//	if(logger.isDebugEnabled())
		//		logger.debug("The Val '"+val+"' "+sqlStatement);
		} catch (Exception e) {
			//throw e;	
		}
		return val;
	}
	public List<DBField> getContentValueList(int id)
	{
		List<DBField> val=null;
		String sqlStatement;
		if(isTableValue&&parentTypeReferenced)
		{
			sqlStatement="SELECT * FROM "+elementDescription.getNameDB()+" WHERE ID="+id;
		}
		else if(isTableValue)
		{
			sqlStatement="SELECT * FROM "+elementDescription.getNameDB()+" WHERE ID="+id;
		}
		else
		{
			if(relationDescriptionTable!=null)
				sqlStatement=sqlStatementGetValues + " AND "+tableName+".ID="+id;
			else
				sqlStatement=sqlStatementGetValues + " WHERE "+tableName+".ID="+id;
		}
		
		
//		if(elementDescription.getNameDB().equalsIgnoreCase("wl_095person"))
//			logger.error(" BBBBB "+sqlStatement);
		
		try {
			val = DBSqlHandler.getInstance().getDataFieldList(sqlStatement,elementDescription.getNameDB());
		//	if(logger.isDebugEnabled())
		//		logger.debug("The Val '"+val+"' "+sqlStatement);
		} catch (Exception e) {
		//	logger.error(":::::::::::::::::::::"+sqlStatement);
			//throw e;
		}
		return val;
	}
	/**
	 * Removes the value of the table element. Removes data from the connecting table and the table element.
	 * @param parentID The ID of the table.
	 * @param dataID The ID of the data.
	 */
	public boolean removeContent(int parentID, int dataID)
	{
		DBSqlHandler sqlHandler;
		try {
			sqlHandler = DBSqlHandler.getInstance();
		} catch (Exception e1) {
			return false;
		}
		// remove from the connecting table
		String sqlStatement = "DELETE FROM "+getRelationDescrTableName()+ " WHERE ID1='"+parentID+"' AND ID1='"+dataID+"'";
		//if(logger.isDebugEnabled())
		//	logger.debug("Remove data from connecting table: "+sqlStatement);
			try {
				sqlHandler.executeQuery(sqlStatement);
			} catch (Exception e) {
				String msg = "Error removing data from the connecting table: "+sqlStatement;
			//	logger.error(msg+"\r\n\tSQL Statement '"+sqlStatement+"'",e);
			//	if(logger.isDebugEnabled())
			//		MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
			//	else
					MainUIFrame.setStatusMessage(msg);
				
				return false;
			}
			
			// remove from the table element
			sqlStatement = "DELETE FROM "+getNameDB()+ " WHERE ID='"+dataID+"'";
			//if(logger.isDebugEnabled())
			//logger.debug("Remove data from table element: "+sqlStatement);
			
			try {
				sqlHandler.executeQuery(sqlStatement);
			} catch (Exception e) {
				String msg = "Error removing data from the table element: "+sqlStatement;
				//logger.error(msg+"\r\n\tSQL Statement '"+sqlStatement+"'",e);
				//if(logger.isDebugEnabled())
				//	MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
				//else
					MainUIFrame.setStatusMessage(msg);
				
				return false;
			}
		return true;
	}
	public Boolean addContent(String val, Boolean isPublic,int ID1)
	{
		DBSqlHandler sqlHandler;
		try {
			sqlHandler = DBSqlHandler.getInstance();
		} catch (Exception e1) {
			return false;
		} 
		
		//Insert Row in related table
		String sqlStatement = "INSERT INTO "+getNameDB()+" (CreationDate,Public,Content)"+ 
		" VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+DBConstants.getIsPublicVal(isPublic)+"','"+val+"')";
		//if(logger.isDebugEnabled())
		//	logger.debug("INSERT RELT:"+sqlStatement);
		
		try {
			sqlHandler.executeQuery(sqlStatement);
		} catch (Exception e) {
			String msg = "Error adding new row in a table: "+getNameDB();
			//logger.error(msg+"\r\n\tSQL Statement '"+sqlStatement+"'",e);
			//if(logger.isDebugEnabled())
			//	MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
			//else
				MainUIFrame.setStatusMessage(msg);
			//TODO: Rollback
			return false;
		}
		String ID2;
		//Get ID of the inserted row
		sqlStatement ="SELECT ID  FROM "+getNameDB()+" order by ID DESC limit 1";
		try {
			ID2 = sqlHandler.executeGetValue(sqlStatement);
			//if(logger.isDebugEnabled())
			//	logger.debug("GETID '"+ID2+"' "+sqlStatement);
		} catch (Exception e) {
			String msg = "Error getting ID of the newly inserted row in a table: "+getNameDB();
			//logger.error(msg+"\r\n\tSQL Statement '"+sqlStatement+"'",e);
			//if(logger.isDebugEnabled())
			//	MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
			//else
				MainUIFrame.setStatusMessage(msg);
			// TODO Rollback
			return false;
		}
		
		//Insert row in relation description table
		sqlStatement="INSERT INTO "+getRelationDescrTableName()+" (CreationDate,ID1,ID2)"+ 
		"VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+ID1+"','"+ID2+"')";
		//if(logger.isDebugEnabled())
			//logger.debug("INSERT RD:"+sqlStatement);
		try {
			sqlHandler.executeQuery(sqlStatement);
		} catch (Exception e) {
			String msg = "Error adding new row in a relation description table: "+getRelationDescrTableName();
			//logger.error(msg+"\r\n\tSQL Statement '"+sqlStatement+"'",e);
			//if(logger.isDebugEnabled())
			//	MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
			//else
				MainUIFrame.setStatusMessage(msg);
			//TODO: Rollback
			return false;
		}
		return true;
	}
}
