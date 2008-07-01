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

import java.text.ParseException;

import org.apache.log4j.Logger;

/**
 * $Id: DBField.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $
 * This class defines the structure of a data.
 * @author fokus
 */
public class DBField {
//	-------- LOGGING -----
	static Logger logger = Logger.getLogger(DBField.class.getName());
	// ----------------- ATTRIBUTES ---------------------------------
	/**
	 * The name of the element.
	 */
	private String elementName;

	/**
	 * The unique identifier of the data field.
	 */
	private int id;
	/**
	 * The creation date of the data.
	 */
	private String creationDate;
	/**
	 * The content (value) of data.
	 */
	private String content;
	
	/**
	 * Access type of the data.
	 * <code>true</code>: The data should be <i>public</i>.<br>
	 * <code>false</code>: The data should be <i>private</i>.
	 */
	private Boolean isPublic=true;
	
	// ----------------- CONSTRUCTORS ---------------------------------
	public DBField(int id, String creationDate, String contentVal,Boolean isPublic,String elementName)  throws ParseException 
	{
		this.id=id;
		this.creationDate=creationDate;
		this.content=contentVal;
		this.isPublic=isPublic;
		this.elementName=elementName;
	}
	
	// ----------------- SET METHODS ---------------------------------
	public void setContentVal(String content)
	{
		this.content=content;
	}
	public void setIsPublic(Boolean isPublic)
	{
		this.isPublic=isPublic;
	}
	// ----------------- GET METHODS ---------------------------------
	public int getID()
	{
		return id;
	}
	public String getCreationDate()
	{
		return creationDate;
	}
	public String getContent()
	{
		return content;
	}
	public Boolean getIsPublic()
	{
	   return isPublic;
	}
	public String getRelatedTableName()
	{
		return elementName;
	}
	public String toString()
	{
		return "ID-'"+id+"' DT-'"+creationDate.toString()+"' VAL-'"+content+"' PUBLIC-"+isPublic;
	}
	//	 ----------------- GET METHODS ---------------------------------
	public void updateDBField(String val, boolean isPublic) throws Exception
	{
		StringBuffer sqlStatement = new StringBuffer("UPDATE ");
		sqlStatement.append(elementName); // from part
		sqlStatement.append(" SET ");
		  sqlStatement.append(DBConstants.FIELD_CONTENT);
		  sqlStatement.append("='");
		  sqlStatement.append(val);
		  sqlStatement.append("',");
		  sqlStatement.append(DBConstants.FIELD_CREATION_DATE);
		  sqlStatement.append("=");
		  sqlStatement.append(DBConstants.SQL_TIMESTAMP);
		  sqlStatement.append(",");
		  sqlStatement.append(DBConstants.FIELD_PUBLIC);
		  sqlStatement.append("=");
		  sqlStatement.append(DBConstants.getIsPublicVal(isPublic));
		sqlStatement.append(" WHERE ");
		  sqlStatement.append(DBConstants.FIELD_ID);
		  sqlStatement.append("=");
		  sqlStatement.append(id);
		  logger.debug("UPDATE:'"+sqlStatement.toString()+"'");
		  if(logger.isDebugEnabled())
			  logger.debug("UPDATE:'"+sqlStatement.toString()+"'");
		try {
			DBSqlHandler.getInstance().executeQuery(sqlStatement.toString());
			setContentVal(val);
		} catch (Exception e) {
			logger.error("Error executing update statement."+e.getMessage());
			throw e;
		}
	}
	public void updateDBFieldReference(int newReferenceID,boolean isPublic,String refTableName)throws Exception
	{
		StringBuffer sqlStatement = new StringBuffer("UPDATE ");
		sqlStatement.append(refTableName); // from part
		sqlStatement.append(" SET ID2=");
		  sqlStatement.append(newReferenceID);
		  sqlStatement.append(",");
		  sqlStatement.append(DBConstants.FIELD_CREATION_DATE);
		  sqlStatement.append("=");
		  sqlStatement.append(DBConstants.SQL_TIMESTAMP);
		sqlStatement.append(" WHERE ");
		  sqlStatement.append(DBConstants.FIELD_ID);
		  sqlStatement.append("=");
		  sqlStatement.append(id);
		  if(logger.isDebugEnabled())
			  logger.debug("UPDATE:'"+sqlStatement.toString()+"'");
		try {
			DBSqlHandler.getInstance().executeQuery(sqlStatement.toString());
//			setContentVal(val);
		} catch (Exception e) {
			logger.error("Error executing update statement."+e.getMessage());
			throw e;
		}
	}
}
