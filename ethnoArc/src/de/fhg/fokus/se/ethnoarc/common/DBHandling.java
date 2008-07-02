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

import hypergraph.graphApi.Edge;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fhg.fokus.se.ethnoarc.common.DBConstants.DBElementTypes;

/**
 * $Id: DBHandling.java,v 1.4 2008/07/02 09:58:40 fchristian Exp $ 
 * @author fokus
 */
public class DBHandling {
	// -------- LOGGING -----
	static Logger logger = Logger.getLogger(DBHandling.class.getName());

	protected DBStructure dbStructure;
	private DBSqlHandler sqlHandler;
	private boolean parseContent=false;
	private Vector <String> CHECKLIST = new Vector <String>();
	
	public DBHandling( String DBURL, String DBUSERNAME, String DBPASSWORD) throws DBException, Exception{
		this(DBURL,DBUSERNAME,DBPASSWORD,false);
	}
	
	public DBHandling( String DBURL, String DBUSERNAME, String DBPASSWORD, boolean parseContent) 
	throws DBException,Exception {
		//initLog();
		if(logger.isDebugEnabled())
			logger.debug("'"+DBURL+"' '"+DBUSERNAME+"' '"+DBPASSWORD+"'");
		//Initialise SQL Handler
		sqlHandler=new DBSqlHandler(DBConstants.DBDRIVER, DBURL, DBUSERNAME, DBPASSWORD);
		this.parseContent =parseContent;
//		Parse DB
		dbStructure = parseDB();

		/*for (EADBDescription de : dbStructure.getTables().values()) {
			//if(de.getTablename().equals("s_046Title"))
			//logger.debug(de.getNameDB()+":"+de.getDBElementType()+":::#"+de.getParentStructure());
		}*/
	}
	
	private void initLog()
	{
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		logger.debug("LOGGER INITIALISED");
	}

	public DBStructure getDBStructure()
	{
		return dbStructure;
	}

	private DBTable fillCBTables(DBStructure dbStructure, DBTable cbTable) 
	{
		ResultSet rs, rs2 = null;
		

		try {
			//check if the root table may contain value 
			if(cbTable.getParentStructure().indexOf(".")<0)
			{
				if(!cbTable.getTableProperties().getNoValue())
				{
					DBTableElement tableElement=new DBTableElement(cbTable.getTableProperties(),null,cbTable.getTableName(),cbTable.getIsPublicDefault());
					cbTable.addRelatedTable(tableElement);
				}
			}
			//get any tables assocated with this combined table
			String dummy = "SELECT DISTINCT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + "='" + cbTable.getTableName()+ "' AND ("+ DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.Contains + "' OR " + DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.TakesReferenceFrom + "')";
			//String dummy = "SELECT DISTINCT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + "='" + cbTable.getTableName()+ "' AND ("+ DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.Contains + "' OR " + DBConstants.FIELD_TYPE + " LIKE '" + DBConstants.TableReferenceTypes.Alternative + "%')";
			//logger.info(dummy);
			rs = sqlHandler.executeQuery(dummy);
			while( rs.next() ) {
				//check if associated table is CB table or not
				String dummy2 = "SELECT DISTINCT " + DBConstants.FIELD_FIRSTTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + "='" + rs.getString(DBConstants.FIELD_SECONDTABLE)+ "'AND ("+ DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.Contains + "' OR " + DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.TakesReferenceFrom + "')";
				//String dummy2 = "SELECT DISTINCT " + DBConstants.FIELD_FIRSTTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + "='" + rs.getString(DBConstants.FIELD_SECONDTABLE)+ "'AND ("+ DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.Contains + "' OR " + DBConstants.FIELD_TYPE + " LIKE '" + DBConstants.TableReferenceTypes.Alternative + "%')";
				rs2 = sqlHandler.executeQuery(dummy2);
				//logger.info(dummy2);
				rs2.next();
					if(rs2.getRow()==0){//table not found in Firstable and so it is a normal table, get table from structure and add it to CBtable
						
						//logger.error(cbTable.getTableName()+"--------- "+rs.getString(DBConstants.FIELD_SECONDTABLE));
						//logger.info(rs.getString(DBConstants.FIELD_SECONDTABLE));
						EADBDescription tableElementDesc = dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_SECONDTABLE));
												
						if(tableElementDesc!=null)
						{
							//Add connecting table name
							String tbName = dbStructure.getRelationTableName(cbTable.getTableName(), tableElementDesc.getNameDB());
							//logger.info("Name="+tbName);
							//if the name cannot be acquired from the db structure then get it from the database 
							if(tbName==null)
							{
								String query="SELECT Tablename FROM databasedescription where firsttable='"+cbTable.getTableName()+"' and secondtable='"+tableElementDesc.getNameDB()+"'";
								tbName=DBSqlHandler.getInstance().executeGetValue(query);
								//logger.error("###### ---- "+tbName);
							}
							//logger.error(cbTable.getTableName()+":"+table.getTablename()+"########## "+tbName);
							EADBDescription connectingTable = dbStructure.getTableByTableName(tbName);
							boolean elemIsPublicDefault=tableElementDesc.getIsPublicDefault();
							//check isPublicDefault of the parent is it is public
							if(elemIsPublicDefault)
							{
								elemIsPublicDefault=cbTable.getIsPublicDefault();
							}
							DBTableElement tableElement=new DBTableElement(tableElementDesc,connectingTable,cbTable.getTableName(),elemIsPublicDefault);
							cbTable.addRelatedTable(tableElement);
							
							//Set the parent structure
							if(tableElement.getTableName().startsWith("Engl"))
								logger.debug("‹‹‹‹‹ "+tableElement.getTableName());
							tableElementDesc.addParentStructure(tableElement.parentStructure);
							tableElementDesc.setDBElementType(DBElementTypes.TableElement);
							
							//logger.error(rs.getString(DBConstants.FIELD_SECONDTABLE)+"-------"+dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_SECONDTABLE)).getParentStructure());
						}
					}else{//is CBTable and must be called again
						
						
						//check against existing connections
						String test = rs.getString(DBConstants.FIELD_SECONDTABLE)+"#"+cbTable.getTableName()+"#"+cbTable.getParentTableName();
						if(CHECKLIST.contains(test)){
							//System.out.println("Sollte aufhˆren");
							break;
						}else{
							CHECKLIST.add(test);
						}
//						don't create CB of same CD table
						//System.out.println("EXIST="+rs.getString(DBConstants.FIELD_SECONDTABLE) + " *** "+cbTable.getTableName()+ " Parentname="+cbTable.getParentTableName());
						if(rs.getString(DBConstants.FIELD_SECONDTABLE).equals(cbTable.getTableName()) || rs.getString(DBConstants.FIELD_SECONDTABLE).equals(cbTable.getParentTableName()))
							continue;

						//1) create new CBtable
						String tablename = rs.getString(DBConstants.FIELD_SECONDTABLE);
						EADBDescription tableDesc;
						tableDesc = dbStructure.getTableByTableName(tablename);
						
						DBTable newTable = new DBTable(tableDesc);				
						//2) add this CB as parent table
						newTable.setParentTable(cbTable);
						//3) set the connecting table name
						EADBDescription connectingTable=dbStructure.getRelationTable(cbTable.getTableName(), rs.getString(DBConstants.FIELD_SECONDTABLE));
						newTable.setParentConnectingTable(connectingTable);
						//4) add new table as child table to this CB table
						cbTable.addChildCBTable(newTable);
					
						//set parent structure
						if(newTable.getTableName().startsWith("Engl"))
							logger.debug("‹‹‹‹‹ 2 "+newTable.getTableName());
						tableDesc.addParentStructure(newTable.getParentStructure());
						
						tableDesc.setDBElementType(DBElementTypes.Table);
						//check if the table can contain value
						if(!tableDesc.getNoValue())
						{
							if(logger.isDebugEnabled())
								logger.debug("..... table contains value: "+tableDesc.getNameDB()+"_"+cbTable.getTableName());
							DBTableElement tableElement=new DBTableElement(tableDesc,connectingTable,cbTable.getTableName(),tableDesc.getIsPublicDefault());
							tableElement.setIsTableValue(true);
							newTable.addRelatedTable(tableElement);
						}
						//5) call fillCBTables again with newCBtable
						fillCBTables (dbStructure,  newTable);
						//logger.error(tablename+"::::::"+dbStructure.getTableByTableName(tablename).getParentStructure());
					}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cbTable;
	}

	private DBStructure createCombinedTables(DBStructure dbStructure)throws Exception{
				
		ResultSet rs, rsTables = null;
		Hashtable<String,String> resultRootTables = new Hashtable<String,String>();
		Hashtable<String,EADBDescription> possibleRootTables = new Hashtable<String,EADBDescription>();
		try{
			//First get list of available tables
			rsTables = sqlHandler.executeQuery("SELECT DISTINCT " + DBConstants.FIELD_FIRSTTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_TYPE + "='" + DBConstants.TableReferenceTypes.Contains + "'");
			while( rsTables.next() ) {
				String secondTableName = rsTables.getString(DBConstants.FIELD_FIRSTTABLE);
				rs = sqlHandler.executeQuery("SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_SECONDTABLE + "='" + secondTableName + "'");
				rs.next();//otherwise counting does not work
				if(rs.getRow()==0){//root table found and added to DBStructure
					String tablename = rsTables.getString(DBConstants.FIELD_FIRSTTABLE);
					EADBDescription tempTable;
					tempTable = dbStructure.getTableByTableName(tablename);
					DBTable cbTable = new DBTable(tempTable);
					//logger.error("------- "+tempTable.getNameDB());
					//logger.error("‹‹‹‹‹‹‹‹‹‹ "+tempTable.getDisplayname()+"::"+tempTable.getNoValue());

					dbStructure.addCombinedTable(fillCBTables(dbStructure, cbTable));
					
					tempTable.setDBElementType(DBElementTypes.Table);
				}else{//add to possible roottable list
					Hashtable <String,EADBDescription> originalTables = dbStructure.getTables();
					Hashtable possibleRoottable = determineRootNodes(originalTables);
		        	for (EADBDescription originalTable : originalTables.values()) {
						if(originalTable.getType().equals("object")){
							if((possibleRoottable.get(originalTable.getNameDB())).equals("R")){
								//System.out.println("**** ROOT FOUND **** = "+originalTable.getNameDB());
								//check if tables already in there...
								DBTable dummy = dbStructure.getCombinedTableByName(originalTable.getNameDB());
								if(dummy==null){
									//System.out.println("NOT IN LIST= "+ originalTable.getNameDB());
									DBTable cbTable = new DBTable(originalTable);
									dbStructure.addCombinedTable(fillCBTables(dbStructure, cbTable));
								}					
							}
						}
		        	}
				}
				
			}
			//find tables which are not contains but TakesValueFrom and TakesReferenceFrom
			rsTables = sqlHandler.executeQuery(
					"SELECT DISTINCT secondtable " +
					"FROM databasedescription " +
					"WHERE firsttable IN " +
						"(SELECT secondtable from databasedescription) AND " +
						"(Type !='Contains' AND Type!='AlternativeLanguage' AND Type!='TakesReferenceFrom' AND Type!='Alternative' AND Type!='Implies') AND " +
						"firsttable!=secondtable");
			
			while( rsTables.next() ) {
				String secondTableName = rsTables.getString(DBConstants.FIELD_SECONDTABLE);
				EADBDescription tempTable;
				tempTable = dbStructure.getTableByTableName(secondTableName);
				DBTable cbTable = new DBTable(tempTable);
				dbStructure.addCombinedTable(fillCBTables(dbStructure, cbTable));
				tempTable.setDBElementType(DBElementTypes.HelperTable);
			}
			return dbStructure;
		}
		catch (SQLException e) {
			logger.error("SQLException Creating combined tables: "+e.getMessage(),e);
			throw e;
		}
		catch (Exception e) {
			logger.error("Exception creating combined table: "+e.getMessage(),e);
			throw e;
		}
	}	
		
	private DBStructure createDBStructure(ResultSet rs){
		DBStructure dbStructure = new DBStructure();
		try {
			while( rs.next() ) {
				EADBDescription newDBElement = new EADBDescription();

				newDBElement.setID(Integer.valueOf(rs.getString(DBConstants.FIELD_ID)).intValue());
				newDBElement.setType(rs.getString(DBConstants.FIELD_TYPE));
				newDBElement.setName(rs.getString(DBConstants.FIELD_NAME));
				newDBElement.setNameDB(rs.getString(DBConstants.FIELD_TABLENAME));
				newDBElement.setDisplayname(rs.getString(DBConstants.FIELD_DISPLAYNAME));
				newDBElement.setDescription(rs.getString(DBConstants.FIELD_DESCRIPTION));
				newDBElement.setEnglishDescription(rs.getString(DBConstants.FIELD_ENGLISHDESCRIPTION));
				newDBElement.setFormat(rs.getString(DBConstants.FIELD_FORMAT));
				newDBElement.setIsMandatory(rs.getBoolean(DBConstants.FIELD_MANDATORY));
				
				if(!newDBElement.getType().toLowerCase().equals("object"))
				{
					if(newDBElement.getType().toLowerCase().equals("default"))
						newDBElement.setDBElementType(DBElementTypes.Others);
					else
					newDBElement.setDBElementType(DBElementTypes.RelationElement);
				}else{//put first found content in DBElement
					if(parseContent==true){
						ResultSet firstElement = null;
						try {
							firstElement = sqlHandler.executeQuery("SELECT " + DBConstants.FIELD_CONTENT + " FROM " + newDBElement.getNameDB() + " WHERE " + DBConstants.FIELD_CONTENT + "!='' AND " + DBConstants.FIELD_PUBLIC + "='1'");
							firstElement.next();
							if(firstElement.getRow()!=0){
								String content = firstElement.getString(DBConstants.FIELD_CONTENT);
								newDBElement.setFirstContent(content);
								//logger.info("Content="+content);
							}else{
								newDBElement.setFirstContent("");
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				if(rs.getString(DBConstants.FIELD_NOVALUE)!=null){
					newDBElement.setNoValue(Integer.valueOf(rs.getString(DBConstants.FIELD_NOVALUE)).intValue());
				}else{
					newDBElement.setNoValue(0);
				}
				if(rs.getString(DBConstants.FIELD_MULTIPLE)!=null){
					newDBElement.setMultiple(Integer.valueOf(rs.getString(DBConstants.FIELD_MULTIPLE)).intValue());
				}else{
					newDBElement.setMultiple(0);
				}
				if(rs.getString(DBConstants.FIELD_ORDERNUMBER)!=null){
					newDBElement.setOrderNumber(Integer.valueOf(rs.getString(DBConstants.FIELD_ORDERNUMBER)).intValue());
				}else{
					newDBElement.setOrderNumber(0);
				}	
				//GENEVA MEETING addition for global protecting fields/objects
				if(rs.getString(DBConstants.FIELD_PUBLICDEFAULT)!=null){
					newDBElement.setPublicDefault(rs.getBoolean(DBConstants.FIELD_PUBLICDEFAULT));
				}else{
					newDBElement.setPublicDefault(true);
				}

				newDBElement.setFirstTable(rs.getString(DBConstants.FIELD_FIRSTTABLE));
				newDBElement.setSecondTable(rs.getString(DBConstants.FIELD_SECONDTABLE));		
				dbStructure.addTable(newDBElement);
			}
			return dbStructure;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private DBStructure parseDB()throws Exception{
		logger.debug("Parsing Database to create ethnoarc db structure.");
		ResultSet rs = null;
		DBStructure dbStructure;

		try {
			rs = sqlHandler.executeQuery("SELECT * FROM " + DBConstants.MASTERTABLE);

			dbStructure = createDBStructure(rs);
			
			dbStructure = createCombinedTables(dbStructure);
			addTableDependencies(dbStructure);
			logger.info("Ethnoarc DB Parsed.");
			//logger.info("------- Stadt_AlternativeLanguage_Andere_032Schreibweise "+dbStructure.getCombinedTableByName("Stadt_AlternativeLanguage_Andere_032Schreibweise"));
			//logger.info("Anrede"+dbStructure.getCombinedTableByName("anrede"));
			return dbStructure;
		} catch (Exception e) {
			throw new Exception("Error creating DB structure: "+e.getMessage());
		}
	}

	public void addTableDependencies(DBStructure dbStructure){

		ResultSet rs = null;
		String dependency = "";
		Vector <String> list;
		try {
			rs = sqlHandler.executeQuery("SELECT DISTINCT " + DBConstants.FIELD_FIRSTTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE  + " !=''");
			while( rs.next() ) {
				EADBDescription tb =dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE));
				//Set Implies
				list = getDependencieList(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.Implies.toString());
				if(!list.isEmpty()){
					for(int i =0; i<list.size(); ++i){
						dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE)).setImpliesTable(list.get(i),dbStructure.getTableByTableName(list.get(i)));
					}
				}
				list.clear();
				//Set Alternative
				dependency = getDependency(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.Alternative);
				if(!dependency.equals("")){
					EADBDescription depTable=dbStructure.getTableByTableName(dependency);
					
					if(depTable.getParentStructure().get(0).equals(depTable.getNameDB()))
					{
						for (String string : tb.getParentStructure()) {
							//logger.error("qqqqdepTable "+depTable.getNameDB()+"::"+string);
							depTable.addParentStructure(string+"."+depTable.getNameDB());
						}
					}
					tb.setAlternativeTable(depTable);
					//dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE)).setAlternativeTable(dbStructure.getTableByTableName(dependency));
					dependency = "";
				}
				//Set Alternative Language
				dependency = getDependency(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.AlternativeLanguage);
				if(!dependency.equals("")){
					
					EADBDescription depTable=dbStructure.getTableByTableName(dependency);
					
					if(depTable.getParentStructure().get(0).equals(depTable.getNameDB()))
					{
						for (String string : tb.getParentStructure()) {
							//logger.error("qqqqdepTable "+depTable.getNameDB()+"::"+string);
							depTable.addParentStructure(string+"."+depTable.getNameDB());
						}
					}
					tb.setAlternativeLanguageTable(depTable);
					dependency = "";
				}
				//Set TakesValuefrom
				dependency = getDependency(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.TakesValueFrom);
				if(!dependency.equals("")){
					dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE)).setTakesValueFromTable(dbStructure.getTableByTableName(dependency));
					dependency = "";
				}
				//Set TakesReferenceFrom
				dependency = getDependency(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.TakesReferenceFrom);
				if(!dependency.equals("")){
					dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE)).setTakesValueFromTable(dbStructure.getTableByTableName(dependency));
					dependency = "";
				}
//				Set Exclusive TakesValuefrom
				dependency = getDependency(rs.getString(DBConstants.FIELD_FIRSTTABLE), DBConstants.TableReferenceTypes.ExclusiveTakesValueFrom);
				//logger.error("........... "+dependency);
				if(!dependency.equals("")){
					dbStructure.getTableByTableName(rs.getString(DBConstants.FIELD_FIRSTTABLE)).setExclusiveTakesValueFromTable(dbStructure.getTableByTableName(dependency));
					dependency = "";
				}
			}

		} catch (Exception e) {
			logger.error("Exception add Table Dependencies: "+e.getMessage(),e);
		}
	}

	//method used if only one dependency is possible
	public String getDependency(String tableName, DBConstants.TableReferenceTypes refType){
		String dependentTable="";
		try {
			ResultSet rs = null;
			String query ="";
			
			query = "SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + " ='" + tableName + "' AND " + DBConstants.FIELD_TYPE + " ='" + refType.toString()+ "'";
			
			switch (refType) {
			case TakesValueFrom:
				query="SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + " ='" + tableName + "' AND " + DBConstants.FIELD_TYPE + " ='" + refType.toString()+ "' AND mandatory='0'";
				break;
			case TakesReferenceFrom:
				query="SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + " ='" + tableName + "' AND " + DBConstants.FIELD_TYPE + " ='" + refType.toString()+ "' AND mandatory='0'";
				break;
			case ExclusiveTakesValueFrom:
				query="SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + " ='" + tableName + "' AND " + DBConstants.FIELD_TYPE + " ='TakesValueFrom' AND mandatory='1'";
				break;
			default:
				break;
			}
				
			
			rs = sqlHandler.executeQuery(query);
			
			while( rs.next() ){
				dependentTable= rs.getString(DBConstants.FIELD_SECONDTABLE);
			}

		} catch (Exception e) {
			logger.error("Exception get Dependenciy: "+e.getMessage(),e);
		}
		return dependentTable;
	}

	//	method used if more than one dependency is possible
	public Vector<String> getDependencieList(String tableName, String type){
		Vector <String> list = new Vector<String>();
		try {
			ResultSet rs = null;
			rs = sqlHandler.executeQuery("SELECT " + DBConstants.FIELD_SECONDTABLE + " FROM " + DBConstants.MASTERTABLE + " WHERE " + DBConstants.FIELD_FIRSTTABLE + " ='" + tableName + "' AND " + DBConstants.FIELD_TYPE + " ='" + type+ "'");
			while( rs.next() ){
				list.add(rs.getString(DBConstants.FIELD_SECONDTABLE));
			}

		} catch (Exception e) {
			logger.error("Exception get DependenieList: "+e.getMessage(),e);
		}

		return list;
	}	
	
	/****************************************************************************************************/
	/* TESTING FUNCTIONS for NxN */
	/****************************************************************************************************/
	
	private Hashtable determineRootNodes(Hashtable <String,EADBDescription> originalTables ){
		Hashtable<String,String> resultTable= new Hashtable<String,String>();
		Boolean bIsRoot, bChangeOccured;
		int minVal;
		String minStr;
		// create tables with all nodes  			
    	for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				resultTable.put(originalTable.getNameDB()," ");
			
			// and determine whether this is a true root node (no pointers toward them)
			bIsRoot=true;
			for (EADBDescription searchTable : originalTables.values()) {
	       		if( ((searchTable.getType().equals("Contains"))||
						(searchTable.getType().equals("AlternativeLanguage"))||
						(searchTable.getType().equals("TakesReferenceFrom"))||
					(searchTable.getType().equals("Alternative")))&&
					searchTable.getSecondTable().equals(originalTable.getNameDB())){
	       			if(!searchTable.getSecondTable().equals(searchTable.getFirstTable()))
	       			bIsRoot=false;								
	       		}
			}
			if(bIsRoot){
				resultTable.put(originalTable.getNameDB(),"R");
				//System.out.println("INSERT R for="+originalTable.getNameDB());
				}
			}
    	}
    	while(true){
    	// now mark all nodes that can be reached by the root nodes
    	bChangeOccured=true;
    	while(bChangeOccured){
    		bChangeOccured=false;
			for (EADBDescription searchTable : originalTables.values()) {
	       		if( ((searchTable.getType().equals("Contains"))||
					(searchTable.getType().equals("AlternativeLanguage"))||
					(searchTable.getType().equals("TakesReferenceFrom"))||
					(searchTable.getType().equals("Alternative"))))
	       			if(!searchTable.getSecondTable().equals(searchTable.getFirstTable())){
	       				if((resultTable.get(searchTable.getSecondTable())).equals(" "))
	       					if(!(resultTable.get(searchTable.getFirstTable()).equals(" "))){
	       						resultTable.put(searchTable.getSecondTable(),"C");
	       						bChangeOccured=true;
	       					}	       				
	       		}
			}        		
    	}
        // find lowest entry that still has a space (unreachable from current root elements)
    	minVal=-1;
    	minStr="";
		Iterator it = resultTable.keySet().iterator();
		while( it.hasNext() ) {
			String key= (String)it.next();
		    if(resultTable.get(key).equals(" ")){
		    	EADBDescription unreachedTable=originalTables.get(key);
		    	if(minVal==-1){
		    		minVal=unreachedTable.getOrderNumber();
		    		minStr=new String(key);
		    	}
		    	else if (unreachedTable.getOrderNumber()<minVal){
		    		minVal=unreachedTable.getOrderNumber();
		    		minStr=new String(key);
		    	}
		    }
		}
		if(minVal==-1)return resultTable;
		resultTable.put(minStr,"R");
		//System.out.println("INSERT R2 for="+resultTable);
    	}
	}
}
