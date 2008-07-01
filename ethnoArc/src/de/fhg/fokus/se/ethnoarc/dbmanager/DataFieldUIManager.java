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

import de.fhg.fokus.se.ethnoarc.common.DBField;

/**
 * $Id: DataFieldUIManager.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $ 
 * @author fokus
 */
public class DataFieldUIManager  {
	private DBField dbField;
	private String format="";
	/**
	 * Specifies what kind of update was done. <br> <b><code>None</code></b>: The data has not been updated.<br> <b><code>Updated</code></b>: The data has been updated.<br> <b><code>NewData</code></b>: The data is new.<br> <b><code>accessTypeUpdated</code></b>: The access type is modified.
	 */
	public static enum updateType {
		None,   			
		Updated,			
		NewData 			
	}
	public DataFieldUIManager(DBField dbField,String tableDspNames,String format)
	{
		this.format=format;
		if(dbField!=null)
			this.dbField=dbField;
	}
	private String getOriginalval()
	{
		if(dbField!=null)
			return dbField.getContent();
		else
			return "";
	}
	
	public updateType getUpdateType(String currentData)throws Exception
	{
		String orgVal=getOriginalval();
		if(orgVal==null)
			return updateType.Updated;
		if(orgVal.equals("currentData")&&!currentData.equals("")&&dbField==null)
			return updateType.NewData;
		else if(orgVal==null||!orgVal.equals(currentData))
			return updateType.Updated;
		else
			return updateType.None;
	}
	public void updateField(Boolean isPublic,String currentData)throws Exception
	{
		if(dbField!=null)
		{
			dbField.updateDBField(currentData, isPublic);
			MainUIFrame.setStatusMessage("Data updated! from '"+dbField.getContent()+"' to '"+currentData+"'");
		}
		else 
			throw new Exception("dbField not specified.");
	}
	
	public void updateReferenceField(Boolean isPublic,int newReferenceID,String refTableName)throws Exception
	{
		if(dbField!=null)
		{
			dbField.updateDBFieldReference(newReferenceID, isPublic,refTableName);
			MainUIFrame.setStatusMessage("Data updated! from '"+dbField.getContent()+"' to '"+newReferenceID+"'");
		}
		else 
			throw new Exception("dbField not specified.");
	}
	public void removeDataField()
	{
		dbField=null;
	}
}
