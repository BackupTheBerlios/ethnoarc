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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This class provides necessary methods to perform database search.
 * @TODO Check how to block private data. 
 * @author fokus
 */
public class SearchManager {
	/** The logger */
	static Logger logger = Logger.getLogger(SearchManager.class.getName());

	/**
	 * Specifies what kind of result is expected. The default mode is <code>value</code>.<br>
	 * <li><code>value</code>: Values of specified outputParams are returned.
	 * <li><code>id</code>: IDs of specified outputParams are returned.
	 */
	private enum returntype{
		value,
		id
	}

	private String dbUrl;
	private String dbUserName;
	private String dbPassword;
	private DBStructure dbStructure;
	private DBSqlHandler sqlHandler;

	private Hashtable<String,SearchParameter> inputParams=new Hashtable<String,SearchParameter>();
	private Hashtable<String,SearchParameter>outputParams=new Hashtable<String,SearchParameter>();
	private String searchRootTableName="";

	/** Specifies if to return private data or note. */
	private boolean returnPrivateData=false;
	/**
	 * Stores <code>FROM</code> part of the sql string for search purpose. Example: <br>
	 * <code>FROM v_Adressenliste, v_Person, v_Adresse </code>
	 */
	private static Hashtable<String,SearchTableParameter> searchStringPartFrom = new Hashtable<String, SearchTableParameter>();

	private String sqlWhereString;


	public SearchManager(DBSqlHandler sqlHandler,DBStructure dbStructure)throws DBException
	{
		initLog();
		if(dbStructure==null)
			throw new DBException(DBException.DB_STRUCTURE_INVALID);
		this.sqlHandler=sqlHandler;
		this.dbStructure=dbStructure;

		logger.debug("SearchManager Initialised");
	}
	public SearchManager(String dbUrl, String dbUserName, String dbPassword) throws DBException
	{
		initLog();
		this.dbUrl=dbUrl;
		this.dbUserName=dbUserName;
		this.dbPassword=dbPassword;
		logger.debug("URL: "+dbUrl+" usern: "+dbUserName+" pwd: "+dbPassword);
		getDBStructure();
		logger.debug("DB Structure parsed");
		//	init sql handler
		try {
			sqlHandler=DBSqlHandler.getInstance();
		} catch (DBException e) {
			throw e;
		}
	}
	public SearchManager(String dbUrl, String dbUserName, String dbPassword,DBStructure dbStructure) throws DBException
	{
		initLog();
		logger.debug("URL: "+dbUrl+" usern: "+dbUserName+" pwd: "+dbPassword);

		if(dbStructure==null)
			throw new DBException(DBException.DB_STRUCTURE_INVALID);

		this.dbUrl=dbUrl;
		this.dbUserName=dbUserName;
		this.dbPassword=dbPassword;
		this.dbStructure=dbStructure;

		//	init sql handler
		try {
			sqlHandler = new DBSqlHandler(DBConstants.DBDRIVER,dbUrl,dbUserName,dbPassword).getInstance();
		} catch (DBException e) {
			throw e;
		}
	}

	/**
	 * Searches for data based on specified parameters.
	 * @param inputParams The list of input search parameters. Should contain the complete structure 
	 * (e.g.,<code>Adressenliste.Adresse.Person.Nachname</code>) of the 
	 * elements specified in the <code>sqlWhereString</code>
	 * @param outputParams The list of output search parameters. Should contain the complete structure 
	 * (e.g.,<code>Adressenliste.Adresse.Person.Vorname</code>) of elements.
	 * @param sqlWhereString The where part of the query statement. E.g. <br>
	 * <code>(Nachname.content LIKE 'a%' OR Nachname.content LIKE 'r%') AND titel.content='Dr.'</code>
	 * @see SearchParameter Structure for input and output parameters.
	 * @return The result of the search query. 
	 * @see SearchResult The search result format.
	 * @throws DBException Exception during search.
	 */
	public SearchResult search(List<String> inputParams, List<String> outputParams, String sqlWhereString)throws DBException
	{
		for (String aParam : inputParams) {
			SearchParameter p= new SearchParameter(aParam);
			this.inputParams.put(p.elementName, p);
			if(logger.isDebugEnabled())
				logger.debug("--- "+p.structure+" el:"+p.elementName+" tb:"+p.tableName+" r:"+p.rootTableName+" rel:"+p.connectingTableName);
		}

		for (String aParam : outputParams) {
			SearchParameter p= new SearchParameter(aParam);
			this.outputParams.put(p.elementName, p);
			if(logger.isDebugEnabled())
				logger.debug("--- "+p.structure+" el:"+p.elementName+" tb:"+p.tableName+" r:"+p.rootTableName+" rel:"+p.connectingTableName);
		}

		this.sqlWhereString=sqlWhereString;

		//validate params
		validateSearchParams();
		logger.debug("Specified params are valid.");

		//get roottablename
		int ind=inputParams.get(0).indexOf(".");
		if(ind>0)
			searchRootTableName=inputParams.get(0).substring(0,ind);
		else
			searchRootTableName=inputParams.get(0);
		logger.debug("---- "+searchRootTableName);

		parseSearchParams();
		setSqlFromString();
		setSqlWhereString();

		//displayParamList()
		return getSearchResult();
	}
	
	private void displayParamList()
	{
		//display paramList
		for (Integer key : paramList.keySet()) {
			String m=key+":";

			for (String string : paramList.get(key).keySet()) {
				m+=string+",";
			}
			logger.info("...... "+m);
		}
	}

	/**
	 * Builds up sql query statement and queries database to get the result. The result is parsed and returned as <code>SearchResult</code>.<br>
	 * Example query statement:<br>
	 * <code><pre><b>SELECT</b> Adressenliste.ID Adressenliste,Adresse.ID Adresse,Person.ID Person
	 *<b>FROM</b> Adressenliste 
	 *   LEFT OUTER JOIN Adressenliste_Contains_Adresse 
	 *      ON Adressenliste.ID=Adressenliste_Contains_Adresse.ID1 
	 *   LEFT OUTER JOIN Adresse 
	 *      ON Adresse.ID=Adressenliste_Contains_Adresse.ID2 
	 *   LEFT OUTER JOIN Adresse_Contains_Person 
	 *      ON Adresse.ID=Adresse_Contains_Person.ID1 
	 *   LEFT OUTER JOIN Person 
	 *      ON Person.ID=Adresse_Contains_Person.ID2,
	 *   Nachname,Person_Contains_Nachname,Titel,Person_Contains_Titel 
	 *<b>WHERE</b>
	 *   Person.ID=Person_Contains_Nachname.ID1 AND 
	 *   Nachname.ID=Person_Contains_Nachname.ID2  AND 
	 *   Nachname.public='1' AND Person.ID=Person_Contains_Titel.ID1 AND 
	 *   Titel.ID=Person_Contains_Titel.ID2  AND Titel.public='1' AND  
	 *   (Nachname.content LIKE 'a%' OR Nachname.content LIKE 'r%') AND 
	 *   titel.content='Dr.' AND Adressenliste.public='1' AND 
	 *   Adresse.public='1' AND Person.public='1'</pre></code>
	 * @return
	 * @throws DBException
	 */
	private SearchResult getSearchResult() throws DBException
	{
		// get table ids
		String query="";
		query = altSqlSelect+ altSqlFrom+searchFrom+" WHERE "+searchWhere+sqlWhereString;
		logger.debug("Sql Statement: "+query);
		/*for (SearchParameter sparam : inputParams.values()) {
			logger.info("----"+sparam.elementName+":"+sparam.sqlGetValue);
		}*/
		try {
			ResultSet rsTableIds= sqlHandler.executeQuery(query);
			if(rsTableIds!=null)
			{
				//Create result object
				SearchResult sr = new SearchResult(outputParams.keySet().toArray());
				//logger.error("----- result column size "+sr.getColumnCount());
				List<String[]> resultVals=new ArrayList<String[]>();

				while( rsTableIds.next() )
				{
					String[] v = new String[outputParams.size()];

					int i=0;
					for (SearchParameter outpParam : outputParams.values()) {
						String vv="";
						String r = rsTableIds.getString(outpParam.tableName);
						String q = "";
						try{
							//if(!outpParam.isTableValue)
							if(outpParam.sqlGetValue.contains(" WHERE "))
								q=outpParam.sqlGetValue+" AND "+outpParam.tableName+".ID="+r;
							else
								q=outpParam.sqlGetValue+" WHERE "+outpParam.tableName+".ID="+r;
							/*else
									q=outpParam.sqlGetValue+" WHERE "+outpParam.tableName+".ID="+r;*/
							//logger.error(outpParam.tableName+"..######"+q);
							if(returnPrivateData)
								vv = sqlHandler.executeGetValue(q);
							else
							{
								//logger.error(outpParam.tableName+"..######"+q);
								ResultSet rsTableVal= sqlHandler.executeQuery(q);
								while(rsTableVal.next())
								{
									String ispublic = rsTableVal.getString("IsPublic");
									if(ispublic.equals("1"))
									{
										if(vv.equals(""))
											vv=rsTableVal.getString(outpParam.elementName);
										else
											vv+=";"+rsTableVal.getString(outpParam.elementName);
									}
									else
									{
										//logger.error(outpParam.elementName+"--------- "+rsTableVal.getString("IsPublic")+"_:_"+rsTableVal.getString(outpParam.elementName));
										vv="*****";
									}
								}
							}
						} catch (Exception e) {
							logger.error("---"+q);
							//e.printStackTrace();
							vv="";
							//throw new DBException(DBException.SEARCH_ERROR,e.getMessage());
						}
						v[i++]=vv;

					}
					resultVals.add(v);
				}
				sr.setData(resultVals);
				return sr;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException(DBException.SEARCH_ERROR,e.getMessage());
		}
	}

	private void setSqlSelectString()
	{

	}
	/**
	 * Creates the missing FROM part of the query statement. It includes the name of the input elements and 
	 * corresponding tables to create complete From part of the sql statement.
	 * Example:<br>
	 * <code>,Nachname,Person_Contains_Nachname,Titel,Person_Contains_Titel</code>
	 */
	private void setSqlFromString()
	{
		logger.debug("Build From statement.");
		searchFrom="";

		for (SearchParameter inputParam : inputParams.values()) {
			//logger.error(inputParam.elementName+"***********"+inputParam.isTableValue+" tot: "+searchTables.size());
			if(!inputParam.isRootTable)
			{
				//check if the table is already added
				if(!searchTables.contains(inputParam.elementName))
				{
					searchFrom+=","+inputParam.elementName;
					searchTables.add(inputParam.elementName);
				}
				if(!searchTables.contains(inputParam.connectingTableName))
				{
					searchFrom+=","+inputParam.connectingTableName;
					searchTables.add(inputParam.elementName);
				}
			}
		}
		//logger.error("SqlFrom: "+searchFrom);
	}

	/**
	 * Creates the missing WHERE part of the query statement. It includes the name of the input elements and 
	 * corresponding tables to create complete Where part of the sql statement.
	 * Example:<br>
	 * <code><pre>Person.ID=Person_Contains_Nachname.ID1 AND 
	 * Nachname.ID=Person_Contains_Nachname.ID2  AND 
	 * Nachname.public='1' AND 
	 * Person.ID=Person_Contains_Titel.ID1 AND 
	 * Titel.ID=Person_Contains_Titel.ID2  AND 
	 * Titel.public='1' AND</code>
	 */

	private void setSqlWhereString()
	{
		searchWhere ="";
		for (SearchParameter inputParam : inputParams.values()) {
			//logger.error("-------"+inputParam.sqlRelPart+"*");
			if(!inputParam.sqlRelPart.trim().equals(""))
				searchWhere+=inputParam.sqlRelPart +" AND ";
			if(!returnPrivateData)
				searchWhere+=inputParam.elementName+".public='1' AND ";
		}
		//logger.error("SqlWhere: "+searchWhere);
	}


	private void validateSearchParams() throws DBException
	{
		//TODO:check if this is necessary and implement if necessary
	}

	//// FOR ALTERNATE SEARCH METHOD

	private Hashtable<Integer, Hashtable<String,TableParameter>> paramList=new Hashtable<Integer, Hashtable<String,TableParameter>>();
	//private List<String> searchElementList  = new ArrayList<String>();
	private String altSqlSelect="",altSqlFrom="";
	private void parseSearchParams()
	{
		for (SearchParameter inputParam : inputParams.values()) {
			int i =1;

			if(!inputParam.isRootTable)
			{
				for (String parentTable : inputParam.parentTableList) {

					if(!paramList.containsKey(i))
					{
						paramList.put(i,new Hashtable<String,TableParameter>());

						paramList.get(i).put(parentTable,new TableParameter(parentTable,inputParam.tableStruc));
					}
					else
					{
						if(!paramList.get(i).contains(parentTable))
							paramList.get(i).put(parentTable,new TableParameter(parentTable,inputParam.tableStruc));
					}
					i++;
				}
			}
			else
			{			
				paramList.put(i,new Hashtable<String,TableParameter>());
				paramList.get(i).put(inputParam.rootTableName,new TableParameter(inputParam.rootTableName,inputParam.rootTableName));
			}
				
		}

		for (SearchParameter outputParam : outputParams.values()) {
			int i =1;
			if(!outputParam.isRootTable)
			{
				for (String parentTable : outputParam.parentTableList) 
				{
					//logger.error("PPPPPP "+parentTable);
					//displayParamList();
					if(!paramList.containsKey(i))
					{
						//logger.error(".. new "+i+" for "+parentTable);
						paramList.put(i,new Hashtable<String,TableParameter>());
						paramList.get(i).put(parentTable,new TableParameter(parentTable,outputParam.tableStruc));
					}
					else
					{
						//logger.error("------‹‹ "+parentTable);
						if(!paramList.get(i).contains(parentTable))
						{
							//logger.error(".. upd "+i+" for "+parentTable);
							paramList.get(i).put(parentTable,new TableParameter(parentTable,outputParam.tableStruc));
						}
					}
					i++;
				}
			}
			else
			{
				if(!paramList.get(i).contains(outputParam.rootTableName))
				{
					paramList.put(i,new Hashtable<String,TableParameter>());
					paramList.get(i).put(outputParam.rootTableName,new TableParameter(outputParam.rootTableName,outputParam.rootTableName));
				}
			}
		}
//displayParamList();
		//create alternate sql from part
		//logger.info("PARAMS "+paramList.size());

		//for (Integer key : paramList.keySet()) {
		for (int i = 0; i < paramList.size(); i++) {
			Integer key=i+1;
			Hashtable<String,TableParameter> p = paramList.get(key);
			//for (TableParameter tp : p.values()) {
			for (String k : p.keySet()) {

				TableParameter tp = p.get(k);
				//logger.error(k+".... "+tp.tableName+" : "+tp.parentTableName+" : "+tp.connectingTableName);
				if(altSqlFrom.equals(""))
				{
					altSqlFrom=" FROM "+tp.tableName;
					altSqlSelect= "SELECT DISTINCT "+k+".ID "+k;
					//logger.error(" ADDED ##### "+tp.tableName);
					searchTables.add(tp.tableName);
				}
				else
				{
					altSqlFrom+=" LEFT OUTER JOIN "+tp.connectingTableName+
					" ON "+tp.parentTableName+".ID="+tp.connectingTableName+".ID1 "+
					" LEFT OUTER JOIN "+tp.tableName+" ON "+tp.tableName+".ID="+tp.connectingTableName+".ID2";
					altSqlSelect+= ","+k+".ID "+k;
					searchTables.add(tp.tableName);
					searchTables.add(tp.connectingTableName);
					
					//logger.error(" ADDED #####2 "+":"+tp.tableName+":"+tp.connectingTableName+":"+tp.parentTableName);
					if(!searchTables.contains(tp.parentTableName))
					searchTables.add(tp.parentTableName);
				}
			}
		}
		//logger.error(":::: "+altSqlSelect+ altSqlFrom+searchFrom+" WHERE "+searchWhere+sqlWhereString);
	}

	private class TableParameter
	{
		private String tableName,parentTableName,connectingTableName;
		private boolean isRootTable=false;
		public TableParameter(String tableName,String tableStruct)
		{
			this.tableName=tableName;
			int ind = tableStruct.indexOf(".");
			if(ind>0)
			{
				String[] tarray=tableStruct.replace(".",":").split(":");
				for (int i = 0; i < tarray.length; i++) {
					if(tarray[i].equals(tableName))
					{
						if(i>0)
							parentTableName=tarray[i-1];
						else
							isRootTable=true;
					}
				}
				if(!isRootTable)
					this.connectingTableName= dbStructure.getRelationTable(parentTableName, tableName).getNameDB();
			}
			else
			{
				tableName=tableStruct;
				isRootTable=true;
			}
			//logger.info("++++++"+ tableName+" struc: "+tableStruct+" parent: "+parentTableName);
		}

	}
////	/
	private String searchFrom="",searchWhere="";

	private List<String> searchTables = new ArrayList<String>();


	/**
	 * Gets the database structure.
	 */
	private void getDBStructure() throws DBException
	{
		try {
			DBHandling dbhandler=new DBHandling(dbUrl,dbUserName,dbPassword);
			dbStructure=dbhandler.getDBStructure();
		} catch (DBException e) {
			throw e;
		} catch (Exception e) {
			throw new DBException(DBException.UNKNOWN_ERROR,e.getMessage());
		}
	}
	private void initLog()
	{
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		logger.debug("LOGGER INITIALISED");
	}
	private class SearchParameter
	{
		/**
		 * The name of the element. E.g., <code>Titel</code>
		 */
		private String elementName;
		/**
		 * The structure of the element. E.g., <code>Adressenliste.Adresse.Person.Titel</code>
		 */
		private String structure;
		/**
		 * The name of the table this element belongs to. E.g., <code>person</code>
		 */
		private String tableName;
		/**
		 * The name of the table describing the relationship between this element
		 * and the table it belongs to. e.g., <code>Person_Contains_Titel</code>.
		 */
		private String connectingTableName;
		/**
		 * The name of the top level table of the element. e.g., <code>Adressenliste</code>
		 */
		private String rootTableName;
		/**
		 * Part of the SQl statement describing the relation between this element and the table
		 * it belongs to.
		 * Example: <br>
		 * <code>Person.ID=Person_Contains_Titel.ID1 AND <br> 
		 * Titel.ID=Person_Contains_Titel.ID2</code>
		 */
		private String sqlRelPart;
		private String[] parentTableList;
		/**
		 * Sql string to get the content of the element. Example:<br>
		 * <code><b>SELECT</b> nachname.content Nachname <br>
		 * <b>FROM</b> nachname,person,person_contains_nachname <br>
		 * <b>WHERE</b> Person.id=Person_Contains_Nachname.id1 AND<br>
		 * Nachname.id=Person_Contains_Nachname.id2</code>.
		 */
		private String sqlGetValue;
		private String tableStruc;
		private boolean isTableValue=false;
		private boolean isRootTable=false;
		/**
		 * @TODO Check how alternate values of a value is parsed.
		 * @param structure
		 */
		public SearchParameter(String structure)
		{
			this.structure=structure;

			int ind = structure.indexOf(".");
			if(ind>0)
			{
				this.rootTableName=structure.substring(0,ind);
				this.elementName=structure.substring(structure.lastIndexOf(".")+1);
				tableStruc=structure.substring(0,structure.lastIndexOf(".")); 

				this.tableName=tableStruc.substring(tableStruc.lastIndexOf(".")+1);
				this.connectingTableName= dbStructure.getRelationTable(tableName, elementName).getNameDB();

				EADBDescription tdes = dbStructure.getTableByTableName(elementName);

				if(!tdes.getNoValue())
					isTableValue=true;

				String st = tableStruc.replace(".", ":");
				//logger.error(tableStruc+"--"+st);
				parentTableList=st.split(":");
				
				sqlRelPart=tableName+".ID="+connectingTableName+".ID1 AND "+
				elementName+".ID="+connectingTableName+".ID2 ";
			}
			else
			{
				this.rootTableName=structure;
				this.elementName=structure;
				this.tableName=structure;
				this.isTableValue=true;
				this.isRootTable=true;
				
				
				sqlRelPart="";
			}
			//logger.error(tableName+"#########--"+isTableValue);
			if(!isTableValue)
			{
				if(returnPrivateData)
					sqlGetValue="SELECT "+elementName+".content "+ elementName+
					" FROM "+tableName;
				else
					sqlGetValue="SELECT "+elementName+".content "+ elementName+","+elementName+".public IsPublic"+
					" FROM "+tableName;
			}
			else
			{
				if(!isRootTable)
				{
					if(returnPrivateData)
						sqlGetValue="SELECT "+elementName+".content "+ elementName+
						" FROM "+elementName+","+tableName+","+connectingTableName+
					" WHERE "+sqlRelPart;
					else
						sqlGetValue="SELECT "+elementName+".content "+ elementName+","+elementName+".public IsPublic"+ 
						" FROM "+elementName+","+tableName+","+connectingTableName+
					" WHERE "+sqlRelPart;
				}
				else
				{
					if(returnPrivateData)
						sqlGetValue="SELECT "+elementName+".content "+ elementName+
						" FROM "+elementName;
					//" WHERE "+sqlRelPart;
					else
						sqlGetValue="SELECT "+elementName+".content "+ elementName+","+elementName+".public IsPublic"+ 
						" FROM "+elementName;
					//" WHERE "+sqlRelPart;
				}
			}
//			logger.error(structure+" Sql get value: "+sqlGetValue);
		}
		private String getElementName(String elementStructure)
		{
			return elementStructure.substring(elementStructure.lastIndexOf(".")+1);
		}
	}

	private class SearchTableParameter
	{
		private String tableName;
		private String fromPart;
		private String selectPart;
		private String wherePublicPart;
		private List<String> childTables=new ArrayList<String>();
		public SearchTableParameter(String tableName,String fromPart, List<String> childTables)
		{
			this.tableName=tableName;
			this.fromPart=fromPart;
			this.childTables=childTables;
			//set selectPart and wherePublicPart
			selectPart=" SELECT "+tableName+".ID "+tableName;
			wherePublicPart=" AND "+tableName+".public='1'";
			for (String aChildTable : childTables) {
				selectPart+=","+aChildTable+".ID "+aChildTable;
				//if(!returnPrivateData)
				//	wherePublicPart+=" AND "+aChildTable+".public='1'";
			}
		}
		public String toString()
		{
			return tableName+"\r\n"+
			"\t"+selectPart+"\r\n"+
			"\t"+fromPart+"\r\n";

		}
	}
}

