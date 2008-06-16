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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBField;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;

public class AlternateDetails {

	static Logger logger = Logger.getLogger(AlternateDetails.class.getName());

	/**
	 * The type of the alternate.
	 * @see AlternateType
	 */
	private DBConstants.TableReferenceTypes altType;

	/**
	 * The name of the element.
	 */
	private String elementName;
	/**
	 * The ID of the element.
	 */
	private int elementID;
	
	/** If the alternate value could be multiple. */
	private boolean isMultiple=false;
	/**
	 * The alternative element.
	 */
	private EADBDescription altElement;

	/** Array of existng values */
	private String[] valueArray;

	/** 
	 * DBField hashtable. If the alternate relation is specified as multiple
	 * then value fields could be more than one.
	 */
	private Hashtable<Integer,DBField> altElementDBFieldList;

	/**
	 * The name of the connecting element table (ethnoArc DB element) connecting the alt
	 * element to the element. 
	 */
	private String altConnectingElementName;

	/**
	 * @param elementName The name of the element.
	 * @param altType The type of the alternate.
	 * @param altElementName The alternative element.
	 */
	public AlternateDetails(String elementName, DBConstants.TableReferenceTypes altType,EADBDescription altElement)
	{
		this.altType=altType;
		this.altElement=altElement;
		this.elementName=elementName;
		this.altConnectingElementName=elementName+"_"+altType.toString().toLowerCase()+"_"+altElement.getNameDB();
	}
	public EADBDescription getAlternateElement()
	{
		return altElement;
	}
	public DBConstants.TableReferenceTypes getAlternateType()
	{
		return altType;
	}
	public String getAlternateTypeString()
	{
		return altType.toString();
	}
	public String getAlternateElementNameDB()
	{
		return altElement.getNameDB();
	}
	public String getAlternateConnectingNameDB()
	{
		return altConnectingElementName;
	}
	public String getAlternateElementDisplayName()
	{
		return altElement.getDisplayname();
	}
	public void setAlternateValue(List<DBField> altVal)
	{
		if(altVal!=null)
		{
			valueArray = new String[altVal.size()];

			//convert list to hashtable
			int i=0;
			//if(altElementDBFieldList==null)
				altElementDBFieldList= new Hashtable<Integer, DBField>();
			for (DBField field : altVal) {
				altElementDBFieldList.put(field.getID(),field);
				valueArray[i++]=field.getContent();
			}
			//logger.error(elementName+" ÄÄÄÄÄÄ "+altVal.size()+"::"+altElementDBFieldList.size());
		}
	}
	public void setAlternateValue(int id, DBField val)
	{
		altElementDBFieldList.remove(id);
		altElementDBFieldList.put(id, val);
	}
	public List<DBField> getAlternateValueFieldList()
	{
		List<DBField> s =new ArrayList<DBField>(altElementDBFieldList.values());
		return s;
	}
	public Hashtable<Integer,DBField> getAlternateValueFieldTable()
	{
		return altElementDBFieldList;
	}
	
	public DBField getAlternateValue(int id)
	{
		return altElementDBFieldList.get(id);
	}
	public String[] getAlternateValueString()
	{
		return valueArray;
	}
	public int getValueCount()
	{
		return valueArray.length;
	}
	
	public void setElementID(int elementID)
	{
		this.elementID=elementID;
	}
	public String toString()
	{
		String strval = "";
		if(valueArray!=null&&valueArray.length>0)
		{
			for (String val : valueArray) {
				if(strval.equals(""))
					strval="<b>"+altType.name() +": "+altElement.getDisplayname()+"</b>";
				strval+="<br>-"+val;
			}
		}
		return strval;
	}
	public boolean hasAlternateValues()
	{
		if(valueArray!=null&&valueArray.length>0)
			return true;
		else
			return false;
	}
	/**
	 * Updates the alternate value.
	 * @param newVal The new value of the alternate.
	 */
	public boolean saveAlternate(String newVal, int id)
	{
		DBSqlHandler sqlHandler;
		try {
			sqlHandler = DBSqlHandler.getInstance();
		} catch (DBException e1) {
			MainUIFrame.setStatusMessage("Error :"+e1.getMessage(), MessageLevel.error);
			return false;
		} 
		if(id!=-1)
		{
			//logger.debug("Update ID "+altElementDbField.getID());
			//String sql="UPDATE "+altElement.getNameDB()+ " SET content='"+newVal+"' WHERE ID="+altElementDbField.getID();
			String sql="UPDATE "+altElement.getNameDB()+ " SET content='"+newVal+"' WHERE ID="+id;
			logger.debug("SQL SAVE '"+sql+"'");
			try {
				sqlHandler.executeQuery(sql);
				altElementDBFieldList.get(id).setContentVal(newVal);
				logger.info("Alternate value updated");
				return true;
			} catch (DBException e) {
				MainUIFrame.setStatusMessage("Error updating:"+e.getMessage(), MessageLevel.error);

				if(logger.isDebugEnabled())
					logger.error(e);
				else
					logger.error("DBException updating data "+e.getDetailedMsg());
				return false;
			} catch (Exception e) {
				MainUIFrame.setStatusMessage("Error updating:"+e.getMessage(), MessageLevel.error);
				if(logger.isDebugEnabled())
					logger.error(e);
				else
					logger.error("DBException updating data "+e.getMessage());
				return false;
			}
		}
		else
		{
			String elementTableName=altElement.getNameDB();
			//Create new data in altElement
			String sql = "INSERT INTO "+elementTableName+" (CreationDate,Public,Content)"+ 
			" VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+DBConstants.getIsPublicVal(true)+"','"+newVal+"')";

			//if(logger.isDebugEnabled())
			logger.debug("INSERT RELT:"+sql);

			try {
				sqlHandler.executeQuery(sql);
			} catch (Exception e) {
				String msg = "Error adding new row in a table: "+elementTableName;
				logger.error(msg+"\r\n\tSQL Statement '"+sql+"'",e);
				if(logger.isDebugEnabled())
					MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
				else
					MainUIFrame.setStatusMessage(msg);
				return false;
			}

			String ID2="";
			DBField newData;
			//Get ID of the inserted row
			sql ="SELECT *  FROM "+elementTableName+" order by ID DESC limit 1";
			logger.debug("GET ID "+sql);
			try {
				newData = sqlHandler.getDataField(sql,elementTableName);
				if(logger.isDebugEnabled())
					logger.debug("GETID '"+ID2+"' "+sql);
			} catch (Exception e) {
				String msg = "Error getting ID of the newly inserted row in a table: "+elementTableName;
				logger.error(msg+"\r\n\tSQL Statement '"+sql+"'",e);
				if(logger.isDebugEnabled())
					MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
				else
					MainUIFrame.setStatusMessage(msg);
				// TODO Rollback
				return false;
			}
//			Insert row in relation description table
			sql="INSERT INTO "+altConnectingElementName+" (CreationDate,ID1,ID2)"+ 
			" VALUES ("+DBConstants.SQL_TIMESTAMP+",'"+elementID+"','"+newData.getID()+"')";
			if(logger.isDebugEnabled())
				logger.debug("INSERT RD:"+sql);
			try {
				sqlHandler.executeQuery(sql);
				DBField alval=getAlternateValue(id);
				if(alval!=null)
					alval.setContentVal(newVal);
				else
				{
					altElementDBFieldList.remove(id);
					altElementDBFieldList.put(id,newData);
				}
				logger.info("Alternate value updated");
			} catch (Exception e) {
				String msg = "Error adding new row in a relation description table: "+altConnectingElementName;
				logger.error(msg+"\r\n\tSQL Statement '"+sql+"'",e);
				if(logger.isDebugEnabled())
					MainUIFrame.setStatusMessage(msg+" - "+e.getMessage());
				else
					MainUIFrame.setStatusMessage(msg);
				//TODO: Rollback
				return false;
			}
			return true;
		}
	}
}
