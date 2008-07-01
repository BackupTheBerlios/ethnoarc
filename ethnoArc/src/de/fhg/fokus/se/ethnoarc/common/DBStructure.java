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

import java.sql.ResultSet;
import java.util.Hashtable;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBConstants.DBElementTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.TablePanel;

/**
 * $Id: DBStructure.java,v 1.3 2008/07/01 12:08:52 fchristian Exp $ 
 * @author fokus
 */
public class DBStructure implements Serializable {

	static Logger logger = Logger.getLogger(TablePanel.class.getName());
	
	private Hashtable<String,EADBDescription> DB_Tables=new Hashtable<String,EADBDescription>(); 
	private Hashtable<String,DBTable> DB_CombinedTables = new Hashtable<String,DBTable>();

	public void addTable(EADBDescription newTable){
		//logger.error("-------- '"+newTable.getName()+"'");

		if(!newTable.getNameDB().equals(""))
		DB_Tables.put(newTable.getNameDB(), newTable);
		else
			DB_Tables.put(newTable.getName(), newTable);
	}

	public Hashtable<String,EADBDescription> getTables(){
		return DB_Tables;
	}

	public void addCombinedTable(DBTable newTable){
		if(newTable.getTableName().equals("wl_095Locality_046LangVersion"))
			logger.error("#####--wl_095Locality_046LangVersion ADDED");
		DB_CombinedTables.put(newTable.getTableName(), newTable);
	}
	public Hashtable<String,DBTable> getCombinedTables(){
		return DB_CombinedTables;
	}

	public DBTable getCombinedTableByName(String combinedTableName){
		return DB_CombinedTables.get(combinedTableName);
	}

	public EADBDescription getTableByTableName(String tableName){
		if(tableName==null)
			logger.error("+++++++++++++++++ "+tableName);
		return DB_Tables.get(tableName);
	}

	public EADBDescription getRelationTable(String firstTableName, String secondTableName)
	{
		String tbName = getRelationTableNameP(firstTableName, secondTableName);
		if(tbName!=null)
			return DB_Tables.get(tbName);
		else
		{
			String query="SELECT Tablename FROM databasedescription where firsttable='"+firstTableName+"' and secondtable='"+secondTableName+"'";
			try {
				tbName=DBSqlHandler.getInstance().executeGetValue(query);
			} catch (DBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			if(tbName!=null)
				return DB_Tables.get(tbName);
			else
				return null;
		}
	}
	private String getRelationTableNameP(String firstTableName, String secondTableName)
	{
		for (String key : DB_Tables.keySet()) {
			if(key.startsWith(firstTableName+"_"))
			{
				if(key.endsWith("_"+secondTableName))
					return key;
			}
		}
		return null;
	}
	public  String getRelationTableName(String firstTableName, String secondTableName)
	{
		EADBDescription elDesc = getRelationTable(firstTableName,secondTableName);
		if(elDesc!=null)
			return elDesc.getNameDB();
		else
			return null;
	}
	
	public Vector<String> getParentCombinedTable(String childTableName){
		Vector<String> keyList = new Vector <String>();
		for (String key : DB_Tables.keySet()) {
				if(key.endsWith("_"+childTableName)){
					if(!keyList.contains(key))
						keyList.add(key);
				}
		}
		return keyList;
	}
	
	public Vector<String> getChildTables(String parentTableName){
		Vector<String> childList = new Vector <String>();
		for (String key : DB_Tables.keySet()) {
				if(key.startsWith(parentTableName+"_Contains_")){
					EADBDescription table = DB_Tables.get(key);
					String childTablename = table.getSecondTable();
					if(!childList.contains(childTablename))
						childList.add(childTablename);
				}
		}
		return childList;
	}
	
	
	public String getShortContainsTableName(String searchFirstname, String searchSecondname){
		for (String key : DB_Tables.keySet()) {
				EADBDescription table = DB_Tables.get(key);
				String foundFirstname = table.getFirstTable()+"";//to avoid null reponses
				String foundSecondname =  table.getSecondTable()+"";
				if(foundFirstname.equals(searchFirstname) && foundSecondname.equals(searchSecondname)){
					return table.getNameDB();
				}
			}
		return null;
	}
	
	/**
	 * Gets the parent structures of the specified element.
	 * @param dbElementName The name of the db element whose parent structure is requested.
	 * @return The list of parent structure <code>List<String></code>. 
	 * @throws Exception If the specified dbElementName is not valid.
	 */
	public List<String> getParentStructure(String dbElementName) throws DBException
	{
		EADBDescription tb = DB_Tables.get(dbElementName);
		if(tb!=null)
			return tb.getParentStructure();
		else
			throw new DBException(DBException.DB_ELEMENT_UNKNOWN,"DB element with specified name '"+dbElementName+"' not found.");
	}
	/**
	 * Gets the DB element type of the specified element name.
	 * @param dbElementName The name of the db element whose elementType is requested.
	 * @return The db element type.
	 * @throws Exception If the specified dbElementName is not valid.
	 * @see DBElementTypes
	 */
	public DBElementTypes getDbElementType(String dbElementName) throws DBException
	{
		EADBDescription tb = DB_Tables.get(dbElementName);
		if(tb!=null)
			return tb.getDBElementType();
		else
			throw new DBException(DBException.DB_ELEMENT_UNKNOWN, "DB element with specified name '"+dbElementName+"' not found.");
	}
	
	public Vector<String> getTablesByContent(String strSearchContent)
	{
		ResultSet rs,rs1;
		Vector<String> tableList = new Vector <String>();

			String query="SELECT Tablename FROM databasedescription where type='object'";
			try {
				rs=DBSqlHandler.getInstance().executeQuery(query);
				if(rs.first()){ // values available
					while (rs.next()) {
						String query2="SELECT COUNT(*) FROM "+rs.getString(1)+" WHERE content like '"+ ((strSearchContent.replaceAll("'", "''")).trim())+"'";
						rs1=DBSqlHandler.getInstance().executeQuery(query2);
						if(rs1.first()){ // values available
							if(!(rs1.getString(1).startsWith("0")))
								tableList.add(rs.getString(1));
						}
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
				return tableList;
			}
		return tableList;
	}
	
}
