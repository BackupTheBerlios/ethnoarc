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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * $Id: DBSqlHandler.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * @author fokus
 */
public class DBSqlHandler {
	// -------- LOGGING -----
	static Logger logger = Logger.getLogger(DBSqlHandler.class.getName());

	private static DBSqlHandler dbSqlHandler;

	private static Connection connection;
	private static String DBDRIVER;
	private static String DBURL;
	private static String DBUSERNAME;
	private static String DBPASSWORD;
	private static Class DBclass = null;
	private static boolean VIEW_EXISTS = false;
	private static String VIEW_NAME = "";

	public DBSqlHandler(String DBDriver, String DBURL, String DBUSERNAME, String DBPASSWORD) 
	throws DBException {

		this.DBDRIVER = DBDriver;
		this.DBURL = DBURL;
		this.DBUSERNAME = DBUSERNAME;
		this.DBPASSWORD = DBPASSWORD;
		setConnection();
	}

	public static DBSqlHandler getInstance(String DBDriver, String DBURL, String DBUSERNAME, String DBPASSWORD) throws DBException
	{
		try {
			if(dbSqlHandler==null)
				dbSqlHandler = new DBSqlHandler(DBDriver, DBURL, DBUSERNAME,DBPASSWORD);
			return dbSqlHandler;
		} catch (DBException e) {
			throw e;
		}
	}

	public static DBSqlHandler getInstance() throws DBException
	{
		try {
			if(dbSqlHandler==null)
				dbSqlHandler = new DBSqlHandler(DBDRIVER,DBURL,DBUSERNAME,DBPASSWORD);

			return dbSqlHandler;
		} catch (DBException e) {
			throw e;
		}
	}

	private static void setConnection()throws  DBException{
		try {
			logger.debug("Setting up connection to the MySQL dabatase: \r\n"+
					"\tUrl:"+DBURL+"\r\n"+
					"\tUserName:"+DBUSERNAME+"\r\n"+
					"\tPassword:"+DBPASSWORD);
			DBclass = Class.forName(DBDRIVER);
			connection = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
		} catch (ClassNotFoundException e) {
			//logger.error(e.toString() + " not found");
			throw  new DBException(DBException.DB_CONNECTION_FAILED,"Class not found");
		} catch (SQLException e) {
			//logger.error("Connection failed: "+e.getErrorCode()+"::"+e.getMessage());
			if(e.getErrorCode()==0)
			{
				throw new DBException(DBException.DB_CONNECTION_UNKNOWN_HOST);
			}
			else if (e.getErrorCode()==1049)
				throw new DBException(DBException.DB_CONNECTION_UNKNOWN_DB);
			else if (e.getErrorCode()==1045)
				throw new DBException(DBException.DB_CONNECTION_ACCESS_DENIED);
			else
				throw new DBException(DBException.DB_CONNECTION_FAILED);
		}
		if (DBclass != null) {
			if (connection == null) {
				logger.error("Sorry, could not connect to DB.");
				throw new DBException(DBException.DB_CONNECTION_FAILED);
			}
		}
		logger.debug("DB connection created.");
	}

	public void closeDBConnection()throws Exception{
		try {
			if(connection!=null&&!connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			logger.error(e.toString() + " connection clould not be closed.");
			throw e;
		}
	}

	public ResultSet executeQuery(String query)throws Exception{
		logger.debug("Executing Query: '"+query+"'");
		Statement stmt = null;
		ResultSet rs = null;
		// If the connection does not exist the set up connection
		if(connection==null||connection.isClosed())
			setConnection();

		try{
			stmt = connection.createStatement();
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();

				return rs;
			}
			else
			{
				//TODO: CHECK
				return null;
			}
		}catch (SQLException e) {
			/*if(logger.isDebugEnabled())
				logger.error(e.toString() + " connection failed."+query,e);
			else
				logger.error(e.toString() + " connection failed.");*/
			try{
				if(rs!=null)
				rs.close();
				stmt.close();
			}catch(SQLException sqlEx){
				rs = null;
				stmt = null;
				//TODO: Create error codes.
				//ignore throw sqlEx;
			}
			throw new Exception( "SQLException while executing query: "+e.getMessage()+" - \r\n"+query);
		}
	}
	public void executeTransactionQuery(List<String> queryList) throws Exception
	{
//		logger.debug("Executing Query: '"+query+"'");
		Statement stmt = null;
		ResultSet rs = null;
		// If the connection does not exist the set up connection
		if(connection==null||connection.isClosed())
			setConnection();

		try{
			connection.setAutoCommit(false);
			stmt = connection.createStatement();
			
			for (String query : queryList) {
				logger.debug(" Send Query:: "+query);
				stmt.executeUpdate(query);
			}
			// all queries performed correctly
			logger.debug(" Query OK ... do commit.");
			connection.commit();
		}catch (SQLException e) {
			try{
				logger.info("Do rollback");
				connection.rollback();
				if(rs!=null)
				rs.close();
				stmt.close();
			}catch(SQLException sqlEx){
				connection.rollback();
				rs = null;
				stmt = null;
			}
			throw new Exception( "SQLException while executing query: "+e.getMessage());
		}
	}
	public String executeGetValue(String query) throws Exception
	{
		String val=null;
		ResultSet rs = executeQuery(query);
		while( rs.next() )
		{
			val = rs.getString(1);
		}
		return val;
	}
	public List<String> getGetStringList(String query) throws Exception
	{
		List<String> idList = new ArrayList<String>();
		ResultSet rs = null;

		try {
			rs = executeQuery(query);

			while( rs.next() ) {
				try {
					//set the ID
					idList.add(rs.getString(DBConstants.FIELD_CONTENT));
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
			return idList;
		}catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}

	public List<Integer> getGetIntegerList(String query) throws Exception
	{
		//logger.debug("Getting IDs of table "+tableName);
		List<Integer> idList = new ArrayList<Integer>();
		ResultSet rs = null;
		//String sqlStatement = "SELECT ID FROM " +tableName;

		try {
			rs = executeQuery(query);

			while( rs.next() ) {
				try {
					//set the ID
					idList.add(rs.getInt(DBConstants.FIELD_ID));
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
			//logger.info("Table ids list created.");
			//connection.close();
			return idList;

		}catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	public List<Integer> getGetIntegerList(String query,String columnName) throws Exception
	{
		//logger.debug("Getting IDs of table "+tableName);
		List<Integer> idList = new ArrayList<Integer>();
		ResultSet rs = null;
		//String sqlStatement = "SELECT ID FROM " +tableName;

		try {
			rs = executeQuery(query);

			while( rs.next() ) {
				try {
					//set the ID
					idList.add(rs.getInt(columnName));
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}
			}
			//logger.info("Table ids list created.");
			//connection.close();
			return idList;

		}catch (SQLException e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	/**
	 * Gets the list of data fields of a table.
	 * @param query The SQL statement.
	 * @return The list of data fields of a table.
	 * @throws Exception
	 */
	public LinkedHashMap<String, DBField> getDataFields(String query,String tableName) throws Exception
	{
		LinkedHashMap<String, DBField> dataFields = new LinkedHashMap<String, DBField>();

		int id;
		String content,creationdate,isPublicString;
		Boolean isPublic=true;

		ResultSet rs = null;

		try {

			rs = executeQuery(query);

			while( rs.next() ) {
				try {
					//get the ID
					id=rs.getInt(DBConstants.FIELD_ID);

					//get creation Date
					creationdate=rs.getString(DBConstants.FIELD_CREATION_DATE);
					if(creationdate!=null)
					{
						//get Content
						content=rs.getString(DBConstants.FIELD_CONTENT);
						//get isPublic
						isPublicString=rs.getString(DBConstants.FIELD_PUBLIC);


						if(isPublicString.equals(DBConstants.VAL_IS_PRIVATE))
							isPublic=false;
						else
							isPublic=true;
						DBField dbField=new DBField(id,creationdate,content,isPublic,tableName);

						dataFields.put(String.valueOf(dbField.getID()), dbField);
						//dataFieldsList.add(dbField);
						if(logger.isDebugEnabled())
							logger.debug("****  Data: "+dbField.toString());
					}

				} catch (Exception e) {
					/*if(logger.isDebugEnabled())
						logger.error("Exception getting data fields:"+e.getMessage()+"::"+query+":"+e);
					else
						logger.error("Exception getting data fields:"+e.getMessage());*/
				}
			}
			return dataFields;
		}catch (SQLException e) {
			//logger.error("SQLException getting data fields:"+e.getMessage());
			throw e;
		}
		catch (Exception e) {
			//if(logger.isDebugEnabled())
				//logger.error("Exception getting data fields:"+e.getMessage()+"---"+query);
			//else
			//	logger.error("Exception getting data fields:"+e.getMessage());
			throw e;
		}
	}
	/**
	 * Gets the list of data fields of a table.
	 * @param query The SQL statement.
	 * @return The the data field of a table.
	 * @throws Exception
	 */
	public DBField getDataField(String query,String tableName) throws Exception
	{
		DBField dataField=null;

		int id;
		String content,creationdate,isPublicString;
		Boolean isPublic=true;

		ResultSet rs = null;

		try {
			rs = executeQuery(query);

			while( rs.next() ) {
				try {
					//get the ID
					id=rs.getInt(DBConstants.FIELD_ID);
					//get creation Date
					creationdate=rs.getString(DBConstants.FIELD_CREATION_DATE);
					//get Content
					content=rs.getString(DBConstants.FIELD_CONTENT);
					//get isPublic
					isPublicString=rs.getString(DBConstants.FIELD_PUBLIC);
					//format=rs.getString(DBConstants.FIELD_FORMAT);
					if(isPublicString.equals(DBConstants.VAL_IS_PRIVATE))
						isPublic=false;
					else
						isPublic=true;
					dataField=new DBField(id,creationdate,content,isPublic,tableName);
				} catch (Exception e) {
					logger.error("Exception getting data fields:"+e.getMessage());
				}
			}
			return dataField;
		}catch (SQLException e) {
			logger.error("SQLException getting data fields:"+e.getMessage());
			throw e;
		}
		catch (Exception e) {
			logger.error("Exception getting data fields:"+e.getMessage());
			logger.error("::"+query);
			throw e;
		}
	}
	public List<DBField> getDataFieldList(String query,String tableName) throws Exception
	{
		List<DBField> dataFields=new ArrayList<DBField>();

		int id;
		String content,creationdate,isPublicString;
		Boolean isPublic=true;

		ResultSet rs = null;

		try {
			rs = executeQuery(query);
			
			while( rs.next() ) {
				try {
					//get the ID
					id=rs.getInt(DBConstants.FIELD_ID);
					//get creation Date
					creationdate=rs.getString(DBConstants.FIELD_CREATION_DATE);
					//get Content
					content=rs.getString(DBConstants.FIELD_CONTENT);
					//get isPublic
					isPublicString=rs.getString(DBConstants.FIELD_PUBLIC);
					//format=rs.getString(DBConstants.FIELD_FORMAT);
					if(isPublicString.equals(DBConstants.VAL_IS_PRIVATE))
						isPublic=false;
					else
						isPublic=true;
					DBField dataField=new DBField(id,creationdate,content,isPublic,tableName);
					dataFields.add(dataField);
				} catch (Exception e) {
					logger.error("Error sql: "+query);
					logger.error("Exception getting data fields:"+e.getMessage());
				}
			}
			return dataFields;
		}catch (SQLException e) {
			logger.error("SQLException getting data fields:"+e.getMessage());
			throw e;
		}
		catch (Exception e) {
			logger.error("Exception getting data fields:"+e.getMessage());
			throw e;
		}
	}


	public String parseDotNotation(DBStructure dbStructure, Vector<String> fieldList, String expectedViewName){
		String viewSQL = "";
		Vector<String> PARAM_TABLES = new Vector<String>();
		Vector<String> PARAM_ELEMENTS = new Vector<String>();
		Vector<String> LEFT_OUTER_JOIN_ELEMENTS = new Vector<String>();
//		Vector<String> WHERE_ELEMENTS = new Vector<String>();
		for(int counterFieldList=0;counterFieldList<fieldList.size();++counterFieldList){
			String[] listParams = fieldList.get(counterFieldList).split("\\.");
			//first add all tabels to tablelist
			String contains ="";
			String tempJOIN  ="";
//			String tempWHERE  ="";
			for(int counterListParams=0; counterListParams < listParams.length; ++counterListParams){
				if(counterListParams<listParams.length-1){//add all tables
					//System.out.println("listParams[counterListParams]="+listParams[counterListParams]);
					if(!PARAM_TABLES.contains(listParams[counterListParams]))
					{
						PARAM_TABLES.add(listParams[counterListParams]);
					}
					//contains = listParams[counterListParams] +"_contains_"+listParams[counterListParams+1];
					contains = dbStructure.getShortContainsTableName(listParams[counterListParams],listParams[counterListParams+1]);
					tempJOIN = " LEFT OUTER JOIN " + contains + " ON " + listParams[counterListParams]+".ID="+contains+".ID1";
					tempJOIN += " LEFT OUTER JOIN " + listParams[counterListParams+1] + " ON " + listParams[counterListParams+1] +".ID=" + contains+".ID2";
					if(!LEFT_OUTER_JOIN_ELEMENTS.contains(tempJOIN))
						LEFT_OUTER_JOIN_ELEMENTS.add(tempJOIN);

//					tempWHERE = listParams[counterListParams]+".ID="+contains+".ID1 AND " +listParams[counterListParams+1]+".ID="+contains+".ID2";
//					if(!WHERE_ELEMENTS.contains(tempWHERE))
//					WHERE_ELEMENTS.add(tempWHERE);
//					tempWHERE = listParams[counterListParams]+".Public=1";
//					if(!WHERE_ELEMENTS.contains(tempWHERE))
//					WHERE_ELEMENTS.add(tempWHERE);
				}
				else{//add last element


					//check if its a table or element
					try {
						//System.out.println("Typedef von="+listParams[counterListParams]);
						String type = dbStructure.getDbElementType(listParams[counterListParams]).toString();
						if(type.equals("Table")){
							if(!PARAM_TABLES.contains(listParams[counterListParams])){
								PARAM_TABLES.add(listParams[counterListParams]);
							}
						}else if(type.equals("TableElement")){
							if(!PARAM_ELEMENTS.contains(listParams[counterListParams])){
								PARAM_ELEMENTS.add(listParams[counterListParams]);
//								tempWHERE = listParams[counterListParams]+".Public=1";
//								if(!WHERE_ELEMENTS.contains(tempWHERE))
//								WHERE_ELEMENTS.add(tempWHERE);
							}
						}
						//System.out.println("Type="+dbStructure.getDbElementType(listParams[counterListParams]));
					} catch (DBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}
//					NOT NEEDED ????
//					contains = listParams[counterListParams-1] +"_contains_"+listParams[counterListParams];
//					tempJOIN = " LEFT OUTER JOIN " + listParams[counterListParams]+" ON " + listParams[counterListParams]+ ".ID="+contains+".ID2"; 
//					if(!LEFT_OUTER_JOIN_ELEMENTS.contains(tempJOIN))
//					LEFT_OUTER_JOIN_ELEMENTS.add(tempJOIN);
				}
			}


		}
		//check if a view with these request parameters already exists
		VIEW_NAME = checkIfViewExists(PARAM_ELEMENTS,expectedViewName);
		//VIEW_NAME = expectedViewName;
		VIEW_EXISTS= false;
		if(!VIEW_EXISTS){	
			viewSQL = "CREATE OR REPLACE VIEW ";
			viewSQL += VIEW_NAME;
			//add alias Fieldnames
			viewSQL += " (";
			for(int i=0;i<PARAM_ELEMENTS.size();++i){
				viewSQL += PARAM_ELEMENTS.get(i);
				viewSQL += ", ";
//				viewSQL += PARAM_ELEMENTS.get(i)+"_id";
//				viewSQL += ", ";
//				viewSQL += PARAM_ELEMENTS.get(i)+"_Public";
//				viewSQL += ", ";
			}
			for(int i=0;i<PARAM_TABLES.size();++i){
				viewSQL += PARAM_TABLES.get(i)+"_id";
//				viewSQL += ", ";
//				viewSQL += PARAM_TABLES.get(i)+"_Public";
				if(!(i+1==PARAM_TABLES.size()))
					viewSQL += ", ";
			}
			viewSQL += ")";
			//add searchFields
			viewSQL += " AS SELECT ";
			for(int i=0;i<PARAM_ELEMENTS.size();++i){
				viewSQL += PARAM_ELEMENTS.get(i) + ".content";
				viewSQL += ", ";
//				viewSQL += PARAM_ELEMENTS.get(i) + ".id";
//				viewSQL += ", ";
//				viewSQL += PARAM_ELEMENTS.get(i) + ".public";
//				viewSQL += ", ";
			}
			for(int i=0;i<PARAM_TABLES.size();++i){
				viewSQL += PARAM_TABLES.get(i)+".id";
//				viewSQL += ", ";
//				viewSQL += PARAM_TABLES.get(i)+".public";
				if(!(i+1==PARAM_TABLES.size()))
					viewSQL += ", ";
			}
			//add FROM elements
			viewSQL += " FROM " + PARAM_TABLES.get(0);
			for(int i=0; i<LEFT_OUTER_JOIN_ELEMENTS.size();++i){
				viewSQL += LEFT_OUTER_JOIN_ELEMENTS.get(i);
			}
			//add WHERE elements
//			viewSQL += " WHERE ";
//			for(int i=0;i<WHERE_ELEMENTS.size();++i){
//			viewSQL += WHERE_ELEMENTS.get(i);
//			if(!(i+1==WHERE_ELEMENTS.size()))
//			viewSQL += " AND ";
//			}

		}
		return viewSQL;
	}

	public String getViewName(DBStructure dbStructure, Vector<String> fieldList)
	{
		if(fieldList.size()==0)
			return null;
		//name fo the created view
		String viewSQL = "";
		//check if dot-notation
		if(fieldList.get(0).contains(".")){
			viewSQL =  parseDotNotation(dbStructure, fieldList, "");
		}
		else{
			//vectors with elemnts from eahc part of the SQL statement needed for the view
			Vector<String> aliasFieldNames = new Vector<String>();
			Vector<String> searchFieldsElements = new Vector<String>();
			Vector<String> fromElements = new Vector<String>();
			Vector<String> whereClauseElements = new Vector<String>();
			//list of tables which must be included in the SQl but are not part of the serach fields only due combined table references
			Vector <String> parentList = new Vector <String>();
			//check if a view with these request parameters already exists
			VIEW_NAME = checkIfViewExists(fieldList,"");
			if(!VIEW_EXISTS){
				for(int i=0;i<fieldList.size();++i){
					if(!aliasFieldNames.contains(fieldList.get(i)))
						aliasFieldNames.add(fieldList.get(i));
					if(!searchFieldsElements.contains(fieldList.get(i)))
						searchFieldsElements.add(fieldList.get(i));
					if(!fromElements.contains(fieldList.get(i)))
						fromElements.add(fieldList.get(i));

					Vector <String> parentContainsTableName = new Vector<String>();
					String parentTableName="";
					parentContainsTableName = dbStructure.getParentCombinedTable(fieldList.get(i));

					for (int counter=0; counter < parentContainsTableName.size();++counter){
						if(!parentContainsTableName.get(counter).contains("TakesValueFrom") && !parentContainsTableName.get(counter).contains("Implies")){
							//adding non search fields to the FROM vector
							if(!fromElements.contains(parentContainsTableName.get(counter)))
								fromElements.add(parentContainsTableName.get(counter));

							//find the parent table to the search field table needed for the WHERE clause
							int pos = parentContainsTableName.get(counter).indexOf("_");
							parentTableName = parentContainsTableName.get(counter).substring(0, pos);
							//adding non search fields to the FROM vector
							if(!fromElements.contains(parentTableName))
								fromElements.add(parentTableName);

							String dummy = parentTableName + ".id = " + parentContainsTableName.get(counter) + ".id1 AND " + fieldList.get(i) + ".id = " +  parentContainsTableName.get(counter) + ".id2";						
							if(!whereClauseElements.contains(dummy))
								whereClauseElements.add(dummy);
							//add the public/ hidden part to where clause
							dummy = parentTableName + ".Public=1";
							if(!whereClauseElements.contains(dummy))
								whereClauseElements.add(dummy);
							dummy = fieldList.get(i) + ".Public=1";
							if(!whereClauseElements.contains(dummy))
								whereClauseElements.add(dummy);
							//add parent to list for later extending WHERE satement of CREATE VIEW SQL
							if(!parentList.contains(parentTableName)){
								parentList.add(parentTableName);
							}
						}
					}
				}	
				//Add additional fields to the FROM and WHERE clause related to the Parent tables of the search fields
				fromElements = checkParentIsContained(dbStructure, parentList, "FROM", fromElements);
				whereClauseElements = checkParentIsContained(dbStructure, parentList, "WHERE", whereClauseElements);

				//1. get root table (hopefully one!) what if more than one?????
				String rootTable = getRootTable(dbStructure, parentList);

				viewSQL = createViewSQL(VIEW_NAME, aliasFieldNames, searchFieldsElements, rootTable, fromElements, whereClauseElements, fieldList);
			}
		}
		//carry out the create view statement
		try{
			//System.out.println(viewSQL);
			executeQuery(viewSQL);
		}catch(Exception e){ System.err.println("SQL Query "+e);}	
		return VIEW_NAME;
	}


	private String createViewSQL(String viewName, Vector <String> aliasFieldNames, Vector <String> searchFieldsElements, String rootTable, Vector <String> fromElements, Vector <String> whereClauseElements, Vector <String> fieldList)
	{
		String viewSQL = "CREATE OR REPLACE VIEW ";
		viewSQL += viewName;
		//add alias Fieldnames
		viewSQL += " (";
		for(int i=0;i<aliasFieldNames.size();++i){
			viewSQL += aliasFieldNames.get(i);
			viewSQL += ", ";
			viewSQL += aliasFieldNames.get(i)+"_id";
			if(!(i+1==aliasFieldNames.size()))
				viewSQL += ", ";
		}
		viewSQL += ")";
		//add searchFields
		viewSQL += " AS SELECT ";
		for(int i=0;i<searchFieldsElements.size();++i){
			viewSQL += searchFieldsElements.get(i) + ".content";
			viewSQL += ", ";
			viewSQL += searchFieldsElements.get(i) + ".id";
			if(!(i+1==searchFieldsElements.size()))
				viewSQL += ", ";
		}
		//add FROM elements
		viewSQL += " FROM ";
		for(int i=0;i<fromElements.size();++i){
			viewSQL += fromElements.get(i);
			if(!(i+1==fromElements.size()))
				viewSQL += ", ";
		}
		//add WHERE elements
		viewSQL += " WHERE ";
		for(int i=0;i<whereClauseElements.size();++i){
			viewSQL += whereClauseElements.get(i);
			if(!(i+1==whereClauseElements.size()))
				viewSQL += " AND ";

		}		
		return viewSQL;
	}

	private Vector<String> checkParentIsContained(DBStructure dbStructure, Vector<String> parentList, String SQLClause, Vector<String> toAddItems){
		for(int i=0; i < parentList.size(); ++i){
			for(int ii=0; ii < parentList.size();++ii){
				String relationTableName = dbStructure.getRelationTableName(parentList.get(i), parentList.get(ii));
				String parentTableName="";
				if(relationTableName!=null){
					if(SQLClause.equalsIgnoreCase("WHERE")){
						int pos = relationTableName.indexOf("_");
						parentTableName = relationTableName.substring(0, pos);
						String dummy = parentTableName + ".id = " + relationTableName + ".id1 AND " + parentList.get(ii) + ".id = " +  relationTableName + ".id2";
						if(!toAddItems.contains(dummy))
							toAddItems.add(dummy);
					}else if(SQLClause.equalsIgnoreCase("FROM")){
						if(!toAddItems.contains(relationTableName))
							toAddItems.add(relationTableName);
						if(!toAddItems.contains(parentList.get(i)))
							toAddItems.add(parentList.get(i));
						if(!toAddItems.contains(parentList.get(ii)))
							toAddItems.add(parentList.get(ii));
					}
				}
			}
		}
		return toAddItems;
	}

	private String checkIfViewExists(Vector<String> fieldList, String expectedViewName)
	{
		//DBConstants.VIEW_NAME
		//1. get all tables in DB
		Vector<String> viewNames = new Vector<String>();
		Vector<String> viewFields = new Vector<String>();
		int maxViewNumber=0;
		try{
			if(expectedViewName.equals("")){
				ResultSet rsTables = executeQuery("SHOW TABLES");
				while( rsTables.next() ) {
					//2. to parse the names for the views like ethnoarc_dyn_view_1
					String dummy = rsTables.getString(1);
					if (dummy.startsWith(DBConstants.VIEW_NAME)){
						viewNames.add(dummy);
						int dummyValue = Integer.valueOf( dummy.replace(DBConstants.VIEW_NAME, "") ).intValue();
						if (dummyValue > maxViewNumber)
							maxViewNumber = dummyValue;
					}
				}
				//3. use the parsed named to compared if the view already exists
				for(int i=0; i < viewNames.size(); ++i){
					ResultSet rsFields = executeQuery("DESC "+ viewNames.get(i));
					while( rsFields.next() ) {
						viewFields.add(rsFields.getString(1));
					}
					//first check size of fields
					if(viewFields.size()==fieldList.size()){
						//now compare content if true return name and set VIEW_EXISTS = true
						if(viewFields.containsAll(fieldList))
							VIEW_EXISTS = true;
						return viewNames.get(0);
					}
				}
				//4. View not found return name and set VIEW_EXISTS = false
				VIEW_EXISTS = false;
				return DBConstants.VIEW_NAME + (maxViewNumber+1);
			}else{//used not for dynamic views only for first time generated ones
				ResultSet rsTables = executeQuery("SHOW TABLES");
				while( rsTables.next() ) {
					//search if viewname exists
					String dummy = rsTables.getString(1);
					if (dummy.equals(expectedViewName)){
						//search if the foudn view has all fields
						ResultSet rsFields = executeQuery("DESC "+ expectedViewName);
						while( rsFields.next() ) {
							viewFields.add(rsFields.getString(1));
						}
						if(viewFields.containsAll(fieldList))
							VIEW_EXISTS = true;
						return expectedViewName;
					}
				}
				//view not found
				VIEW_EXISTS = false;
				return expectedViewName;
			}
		}catch(Exception e){ System.err.println("SQL Query "+e);}		
		return null;
	}

	//TODO: change that a vector will be returned if more than one root table
	private String getRootTable(DBStructure dbStructure, Vector<String> parentList){
		if (parentList.size()==1)
			return parentList.get(0);
		else if(parentList.size()==0)
			return null;
		for(int i=0; i < parentList.size(); ++i){
			for(int ii=0; ii < parentList.size();++ii){
				if(!(ii+1>=parentList.size()))
					if(dbStructure.getRelationTableName(parentList.get(i),parentList.get(ii))!=null)
					{
						//remove the child from the vector
						parentList.remove(ii);
						//recursive call to eliminate the next child
						getRootTable(dbStructure, parentList);
						return parentList.get(0);
					}		
			}
		}
		//maybe this can only be reached by not removing anything but still more than 1 content
		return null;
	}

	//only for debug purposes!!!
	public void createMaxTable(){
		String createTable="CREATE TABLE maxtable (";
		for(int i=1;i<1000;++i){
			createTable += " a" +i+ " char(2),";
		}
		createTable += " last char(2))";
		try{
			executeQuery(createTable);
		}catch(Exception e)
		{ System.err.println("SQL Query "+e);}
	}

	public SearchObject getSearchObject(DBStructure dbStructure, Vector <String> fields){
		SearchObject searchObject = new SearchObject();
		try {
			for(int i=0; i < fields.size();++i){
				List<String> list = dbStructure.getParentStructure(fields.get(i));
				if(list.size()==1){//only one path found
					String realList = removeMultipleFields(list.get(0));
					searchObject.addSearchField(realList);
				}else{//more than one path found
					//check if more than one shortest path exist
					int shortesPathNumber=100000;
					Vector <String> possiblePathes = new Vector();
					for(int count=0;count<list.size();++count){
						String realList = removeMultipleFields(list.get(count));
						String dummy = realList;
						String[] dummyCount = dummy.split(".");
						if(dummyCount.length < shortesPathNumber){//look for the shortest path
							shortesPathNumber = dummyCount.length;
							possiblePathes.clear(); //path cleared for even shorter one
							possiblePathes.add(realList);
						}else if(dummyCount.length == shortesPathNumber){
							possiblePathes.add(realList); //more than one shortest path found
						}
					}
					//if possible more than one shortest path are found
					if(possiblePathes.size()==1){//no problem just add it
						searchObject.addSearchField(possiblePathes.get(0));
					}else{
						searchObject.setMultiplePath(true);
						searchObject.addMultiplePathElements(fields.get(i),possiblePathes);//add name of element as key and possible paths
					}
				}
			}

		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Hashtable<String, Vector <String>> test = searchObject.getMultiplePathTable();
		for (Vector<String> vc : test.values()) {
			for (String string : vc) {
				logger.info("XXXXX '"+string+"'");
			}
		}
		
		return searchObject;
	}
	
	//used for removing multiple entries of the same field
	private String removeMultipleFields(String checkString){
		int posDot =0;
		int posWord =0;
		String searchString="";
		posDot = checkString.indexOf("."); 
		if(posDot>0){
		searchString = checkString.substring(0, posDot);//find Root table
		posWord = checkString.indexOf(searchString+".", 1);//check if root table is included more than once       
        // check also for special case if first root is also the end of the string
		if(posWord<=0){
			if(checkString.endsWith("."+searchString))
				posWord = checkString.indexOf(searchString, 1);
		}
		}
		if(posWord>0){
			checkString = checkString.substring(posWord);
			return checkString;
		}else{
			return checkString;
		}
	}

}
