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
package de.fhg.fokus.se.ethnoarc.ief_import;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;

public class IEF_Import {

	static String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_ief4";
	static String DBUSERNAME ="admin";
	static String DBPASSWORD ="adminPW";

	static Logger logger = Logger.getLogger(IEF_Import.class.getName());

	static Hashtable <String,EADBDescription> originalTables = null;
	static DBStructure dbStructure=null;
	static DBHandling dbHandle=null;	

	static Connection cn = null;
	static	Statement st = null;
	static	Statement st2 = null;

	static String strLastConnector=null;
	static int nLastConnectorID=-1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sFile = "";
		sFile = "c:/ethnoArc/ExportIEF/";

		Logger.getLogger(IEF_Import.class.getName());
		PropertyConfigurator.configure("log/ethnoarcLog.properties");

		// get database structure information
		try {
			dbHandle = new DBHandling(DBURL, DBUSERNAME, DBPASSWORD);
			dbStructure = dbHandle.getDBStructure();							
			originalTables = dbStructure.getTables();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// create database connection

		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection(DBURL, DBUSERNAME, DBPASSWORD);
			st = cn.createStatement();
			st2 = cn.createStatement();
		} catch (Exception ex) {
			System.err.println("Can't connect to database server at "
					+ DBURL);
			System.exit(-1);
		}

		// parse input files
		for(int pass=1;pass<=1;pass++){  // just one pass now.
			File checkFile = new File(sFile);
			if (!checkFile.isDirectory()) {
				Process_IEF_Export_File(checkFile.getAbsolutePath(),pass);
			} else {
				Process_IEF_Export_Directory(checkFile.getAbsolutePath(),pass);
			}
		}
		CheckRelations();

		System.out.println("Finished.");
	}	
	public static void Process_IEF_Export_Directory(String inputDirectory, int pass){

		File directoryFile = new File(inputDirectory);
		String[] nameList = directoryFile.list();
		for (int i = 0; i < nameList.length; i++) {
			File singleFile = new File(inputDirectory + '/' + nameList[i]);
			if (singleFile.isDirectory())
				Process_IEF_Export_Directory(singleFile.getAbsolutePath(),pass);
			else if (singleFile.getAbsolutePath().toLowerCase()
					.endsWith(".xml"))			
				Process_IEF_Export_File(singleFile.getAbsolutePath(),pass);
		}
	}
	public static void Process_IEF_Export_File(String inputFile, int pass){
		DOMParser parser = new DOMParser();
		Document doc = null;
		ResultSet rs = null;
		String statement = null;
		String strNodeName = null;
		String strTableName = null;
		String strAttributeNodeName = null;
		String strAttributeNodeValue = null;
		String strReferenceValue = null;
		String strAttributeContent = null;
		String strAttributeID = null;
		String strAttributeName = null;

		String strFromName = null;
		String strFromID = null;
		String strToName = null;
		String strToID = null;

		int nObjectsProcessed=0;


		System.out.println("Processing: "+inputFile);

		try {
			parser.parse("file:/"+inputFile);		
			doc = parser.getDocument();		
		} catch (IOException ioe) {
			System.err.println("Error: " + ioe.getMessage());
		} catch (SAXException saxe) {
			System.err.println("Error: " + saxe.getMessage());
		}
		// get root node
		NodeList root = doc.getChildNodes();
		for (int i = 0; i < root.getLength(); i++) {

			// check whether this is a know entity
			if( !root.item(i).getLocalName().equals("ethnoArcDefinition")){
				System.err.println("Unexpected root node name "+root.item(i).getLocalName()+" in file "+inputFile+" - skipping!");
				return;				
			}
		}
		NodeList elements = root.item(0).getChildNodes();
		// parse element nodes
		for (int i = 0; i < elements.getLength(); i++) {
			try{
				// check whether this item name exist somewhere in the structure
				int isReference=0;
				strTableName=null;
				strNodeName=new String(elements.item(i).getNodeName());

				nObjectsProcessed++;
				//    if(nObjectsProcessed>100)return; // to speed up testing, we sometime only use the first 100 items
				if( strNodeName.equals("Object"));
				else if(strNodeName.equals("#text"));
				else if(strNodeName.equals("ObjectData")){
					strAttributeID=null;
					strAttributeName=null;
					strAttributeContent=null;
					isReference=0;
					if(elements.item(i).getAttributes().getLength()!=0){
						for(int j=0;j<elements.item(i).getAttributes().getLength();j++){
							strAttributeNodeValue=new String(elements.item(i).getAttributes().item(j).getNodeValue());
							strAttributeNodeName=new String(elements.item(i).getAttributes().item(j).getNodeName());
							if(strAttributeNodeName.equals("name"))strAttributeID=new String(strAttributeNodeValue);
							else if(strAttributeNodeName.equals("reference")){strReferenceValue=new String(strAttributeNodeValue);isReference=1;} //strAttributeID=new String(strAttributeNodeValue);
							else if (strAttributeNodeName.equals("type"))strAttributeName=new String(strAttributeNodeValue);
							else if (strAttributeNodeName.equals("value"))strAttributeContent=new String(strAttributeNodeValue);
							else System.err.println("Unexpected attribute: "+strAttributeNodeName);
							//System.err.println("Attribute "+strAttributeName+" Value: "+strAttributeContent);
						}
					}
					if((isReference==1)&&(strReferenceValue.length()>0)){// on pass 2 we only resolve the reference links
						strFromName=strAttributeID.substring(0,strAttributeID.lastIndexOf('.'));
						strFromID=strAttributeID.substring(strAttributeID.lastIndexOf('.')+1);
						strToName=strReferenceValue.substring(0,strReferenceValue.lastIndexOf('.'));
						strToID=strReferenceValue.substring(strReferenceValue.lastIndexOf('.')+1);
						CreateReferenceLink(strFromName,strFromID,strToName,strToID);						
					}
					if((isReference==0)){


						if(strAttributeID==null)System.err.println("No attribute ID found");
						//if(strAttributeContent==null)System.err.println("No attribute content found "+strAttributeID);
						if(strAttributeName==null)System.err.println("No attribute name found");
						if(!strAttributeID.startsWith(strAttributeName))System.err.println("ID "+strAttributeID+" does not start with "+strAttributeName);
						strAttributeID=strAttributeID.substring(strAttributeName.length()+1);
						if(strAttributeName.endsWith("._ID")){
							strAttributeName=strAttributeName.substring(0, strAttributeName.length()-4);
						}
						if(strAttributeName.endsWith(".ID")){
							strAttributeName=strAttributeName.substring(0, strAttributeName.length()-3);
						}
						if(strAttributeID.contains(".")){
							System.err.println("Attribute ID is "+strAttributeID);
							strAttributeID=strAttributeID.substring(strAttributeID.lastIndexOf("."));
							System.err.println("Attribute ID is now "+strAttributeID);
							System.exit(0);
						}		
					}
				}
						else System.err.println("Unexpected node name: "+strNodeName);
			

				if(isReference==0)
				{ 
					for (EADBDescription originalTable : originalTables.values()) {
						if(originalTable.getType().equals("object")){
							//System.out.println("Check "+originalTable.getName()+" against "+strNodeName);
							if(originalTable.getName().equalsIgnoreCase(strAttributeName)){
								strTableName=originalTable.getNameDB();
							}
						}
					}
					if(strTableName==null){
						if(!strNodeName.equals("#text"))
							if(!strNodeName.equals("Object"))		
								if(strAttributeContent.length()>0){
									if(strAttributeName.equals("Culegeri.ID_Campanii"))
										BuildRelation("Campanii",strAttributeContent,"Culegeri",strAttributeID);
									else if(strAttributeName.equals("Informatori_Fise.ID_Informatori"))
										BuildRelation("Informatori",strAttributeContent,"Informatori_Fise",strAttributeID);
									else if(strAttributeName.equals("Foto_Filme.ID_Foto_Tipuri"))
										BuildRelation("Foto_Tipuri",strAttributeContent,"Foto_Filme",strAttributeID);
									else if(strAttributeName.equals("Piese_Copii.ID_Piese")) // this relation does not actually exist in the database
										;//BuildRelation("Piese",strAttributeContent,"Piese_Diviziuni",strAttributeID);
									else if(strAttributeName.equals("Piese_Diviziuni.ID_Piese"))
										BuildRelation("Piese",strAttributeContent,"Piese_Diviziuni",strAttributeID);

									else if(strAttributeName.equals("Diviziuni.ID_Piese")) ; // does not appear in structure, just in export
									else if(strAttributeName.equals("Diviziuni.ID_Suport")); // does not appear in structure, just in export
									else if(strAttributeName.equals("Diviziuni_Copii_Digitale.ID_Diviziuni")); // does not appear in structure, just in export
									else if(strAttributeName.equals("Diviziuni_Copii_Digitale.ID_Suport_Tipuri")); // does not appear in structure, just in export
									else if(strAttributeName.equals("Diviziuni_Evaluari.ID_Diviziuni")); // does not appear in structure, just in export
									else if(strAttributeName.equals("Piese_Evaluari.ID_Piese")); // does not appear in structure, just in export
									else if(strAttributeName.equals("Suport.ID_Suport_Tipuri")); // does not appear in structure, just in export

									else if(strAttributeName.equals("Piese.ID_Culegeri"))
										BuildRelation("Culegeri",strAttributeContent,"Piese",strAttributeID);
									else if(strAttributeName.equals("Suport_Evaluari.ID_Suport"))
										BuildRelation("Suport",strAttributeContent,"Suport_Evaluari",strAttributeID);
									else 
										System.err.println("Name not matching match for "+strAttributeID+ " "+strAttributeName+" with value "+strAttributeContent);
								}
					}
					else {
						if(strAttributeID.contains(".")){
							strAttributeID=strAttributeID.substring(strAttributeID.lastIndexOf('.')+1);
						}
						
						// check whether we know this ID
						rs = st.executeQuery("SELECT ID FROM "
								+ strTableName
								+ " where ID='"+MakeSQLsafe(strAttributeID)+"'");

						if(rs.first()){ // value already there - don't insert it again
							;
						}	
						else
						{					
							if(strAttributeContent==null)
								statement = "insert into " + strTableName
								+ " (CreationDate, ID) VALUES "
								+ "(CURRENT_TIMESTAMP,'"
								+ MakeSQLsafe(strAttributeID)
								+"');";
							else 
								statement = "insert into " + strTableName
								+ " (CreationDate,ID,Content) VALUES "
								+ "(CURRENT_TIMESTAMP,'"
								+ MakeSQLsafe(strAttributeID)
								+ "','"
								+ MakeSQLsafe(strAttributeContent) 
								+ "')" 
								;									
							st.executeUpdate(statement);
						}


						// build connection to any children of this node 
						NodeList children = elements.item(i).getChildNodes();
						for (int j = 0; j < children.getLength(); j++) {
							if(strNodeName.equals("ObjectData"))	
								Process_Child_Node(strTableName,strAttributeID, children.item(j));
						}		
					}
				}
			}catch (Exception e){
				System.err.println("statement is "+statement);
				System.err.println("SQL error "+e.getMessage());e.printStackTrace();}

		}



	}

	public static void ConnectReference(){}

	public static String MakeSQLsafe(String anyString) {
		return ((anyString.replaceAll("'", "''")).trim());
	}
	public static void Process_Child_Node(String parentTable1,String parentID1, Node childNode){
		ResultSet rs = null;
		String statement = null;
		String strAttributeContent= null;
		String strAttributeName= null;
		String childTable = null;
		String relationTable = null;
		String childName = null;
		String childID = null;
		String parentTable = null;
		String parentID = null;
		boolean switchTables=false;
		boolean noProperName=false;

		parentTable=parentTable1;
		parentID=parentID1;
		
		if(childNode.getNodeName().equals("#text"))return;
		if(childNode.getAttributes().getLength()==0)return;
		// find proper table for this object
		// check whether this item name exist somewhere in the structure
		if(childNode.getAttributes().getLength()!=0){
			for(int i=0;i<childNode.getAttributes().getLength();i++){
				strAttributeContent=new String(childNode.getAttributes().item(i).getNodeValue());
				strAttributeName=new String(childNode.getAttributes().item(i).getNodeName());
				if(!strAttributeName.equals("reference")){
					System.err.println("Expected attribute reference, got: " +strAttributeName);
					return;
				}
				if(strAttributeContent.equals(parentTable+".ID."+parentID)){
					// Ignoring self referential IDs
					return;
				}
				if(strAttributeContent.equals(parentTable+"._ID."+parentID)){
					// Ignoring self referential IDs
					return;
				}
			}
		}

		// separate name from ID
		int dotpos=0;
		for(int i=0;i<strAttributeContent.length();i++)
			if (strAttributeContent.charAt(i)=='.')dotpos=i;
		childName = strAttributeContent.substring(0, dotpos);
		childID = strAttributeContent.substring(dotpos+1);

		//System.err.println("Child name "+childName+" child id "+childID);


		//if(childName.equals("Culegeri.ID_Campanii")){childName="Campanii";switchTables=true;}
		//if(childName.equals("Informatori_Fise.ID_Etnii")){childName="Etnii";switchTables=true;}
		//if(childName.equals("Informatori_Fise.ID_Informatori")){childName="Informatori";switchTables=true;}
		//if(childName.equals("Foto_Filme.ID_Foto_Tipuri")){childName="Foto_Tipuri";switchTables=true;}
		//if(childName.equals("Piese.ID_Culegeri")){childName="Culegeri";switchTables=true;}
		//if(childName.equals("Piese_Diviziuni.ID_Piese")){childName="Piese";switchTables=true;}
		//if(childName.equals("Piese_Diviziuni.ID_Suport")){childName="Suport";switchTables=true;}
		//if(childName.equals("Suport_Evaluari.ID_Suport")){childName="Suport";switchTables=true;}
		//if(childName.equals("Piese_Copii.ID_Piese")){childName="Piese";switchTables=true;}
		//if(childName.equals("Culegeri.ID_Campanii"))return;
		//if(childName.equals("Informatori_Fise.ID_Etnii"))return;
		//if(childName.equals("Informatori_Fise.ID_Informatori"))return;
		//if(childName.equals("Foto_Filme.ID_Foto_Tipuri"))return;
		if(childName.equals("Piese.ID_Culegeri"))return;
		//if(childName.equals("Piese_Diviziuni.ID_Piese"))return;
		//if(childName.equals("Piese_Diviziuni.ID_Suport"))return;
		//if(childName.equals("Suport_Evaluari.ID_Suport"))return;
		//if(childName.equals("Piese_Copii.ID_Piese"))return; // relation does not actually exist
		if(childName.equals("Cuvinte_Cheie.ID"))return;
		if(childName.equals("Informatori_Fise.ID"))return;
		if(childName.equals("Foto_Filme.ID"))return;
		if(childName.equals("Foto_Tipuri.ID"))return;
		//if(childName.equals("Inventar_Tipuri._ID"))return;
		//if(childName.equals("Suport_Tipuri.ID"))return;
		//if(childName.equals("Suport_Tipuri_noi.ID"))return;
		//if(childName.equals("Piese_Diviziuni.ID"))return;
		//if(childName.equals("Piese_Copii.ID"))return;
		//if(childName.equals("Suport_Evaluari.ID"))return;

		//if(childName.equals("Diviziuni.ID_Piese"))return; // does exit in export, but not in actual database
		//if(childName.equals("Diviziuni.ID_Suport"))return;// does exit in export, but not in actual database
		//if(childName.equals("Diviziuni_Copii_Digitale.ID_Diviziuni"))return; // does exit in export, but not in actual database
		if(childName.equals("Diviziuni_Copii_Digitale.ID"))return;// does exit in export, but not in actual database
		//if(childName.equals("Diviziuni_Copii_Digitale.ID_Suport_Tipuri"))return;// does exit in export, but not in actual database
		if(childName.equals("Diviziuni_Evaluari.ID"))return;// does exit in export, but not in actual database
		//if(childName.equals("Diviziuni_Evaluari.ID_Diviziuni"))return;// does exit in export, but not in actual database
		//if(childName.equals("Piese_Evaluari.ID_Piese"))return;// does exit in export, but not in actual database
		if(childName.equals("Piese_Evaluari.ID"))return;// does exit in export, but not in actual database
		//if(childName.equals("Suport.ID_Suport_Tipuri"))return;// does exit in export, but not in actual database
		if(childName.equals("Suport_Tipuri._ID"))return;// does exit in export, but not in actual database



		// find table for child
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				if(originalTable.getName().equalsIgnoreCase(childName)){
					childTable=originalTable.getNameDB();
				}
			}
		}
		if(childTable==null){
			// not a proper table name - maybe we can deconstruct it a bit
			//System.err.println("Didn't find second table for child relation: "+childName); return;
			if(childName.contains(".ID_")){
				childName=childName.substring(childName.lastIndexOf(".ID_")+4);
				switchTables=true;
				// now try again
				for (EADBDescription originalTable : originalTables.values()) {
					if(originalTable.getType().equals("object")){
						if(originalTable.getName().equalsIgnoreCase(childName)){
							childTable=originalTable.getNameDB();
						}
					}
				}				
			}
		}

   
		if(childTable==null){
			System.err.println("Didn't find second table for child relation: "+childName); return;}
			
		
		if(switchTables){
			parentTable=childTable;			
			parentID=childID;
			childTable=parentTable1;
			childID=parentID1;
		}

		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("Contains")||originalTable.getType().equals("TakesReferenceFrom")){
				if((originalTable.getFirstTable().equals(parentTable))&&(originalTable.getSecondTable().equals(childTable))){
					relationTable=originalTable.getNameDB();
				}
			}
		}		
		if(relationTable==null){System.err.println("Didn't find relation table for relation: "+parentTable+" "+childName); return;}


			// check whether we already have this entry
		try {
			rs = st.executeQuery("SELECT ID FROM "
					+ relationTable
					+ " where ID1='"+MakeSQLsafe(parentID)+"' AND ID2='"+MakeSQLsafe(childID)+"'");

			if(rs.first()){ // value already there - don't insert it again
				;
			}	
			else
			{					
				statement = "insert into " + relationTable
				+ " (CreationDate, ID1, ID2) VALUES "
				+ "(CURRENT_TIMESTAMP,'"
				+ MakeSQLsafe(parentID)
				+ "','"
				+ MakeSQLsafe(childID) 
				+ "')" ;
				st.executeUpdate(statement);
			}
		}catch(Exception e){
			System.err.println("Build relation insert error: " + e.getMessage());  
			System.exit(-1);			
		}


	}




	public static void BuildRelation(String parentName,String parentID, String childName,String childID){
		ResultSet rs = null;
		String statement = null;
		String parentTable=null;
		String childTable=null;
		String relationTable=null;

		

	//System.err.println("Building relation "+parentName+"("+parentID+") to "+childName+"("+childID+")");

		
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				//System.out.println("Check "+originalTable.getName()+" against "+strNodeName);
				if(originalTable.getName().equalsIgnoreCase(parentName)){
					parentTable=originalTable.getNameDB();
				}
				if(originalTable.getName().equalsIgnoreCase(childName)){
					childTable=originalTable.getNameDB();
				}
			}
		}
		if(parentTable==null){System.err.println("Didn't find first table for relation: "+parentName);return;} 
		if(childTable==null){System.err.println("Didn't find second table for relation: "+childName); return;}
		for (EADBDescription originalTable : originalTables.values()) {
				if(originalTable.getType().equals("Contains")||originalTable.getType().equals("TakesReferenceFrom")){									
				if((originalTable.getFirstTable().equals(parentTable))&&(originalTable.getSecondTable().equals(childTable))){
					relationTable=originalTable.getNameDB();
				}
			}
		}		
		if(relationTable==null){System.err.println("Didn't find relation table for relation: "+parentName+" "+childName); return;}

		// check whether we already have this entry
		try {
			rs = st.executeQuery("SELECT ID FROM "
					+ relationTable
					+ " where ID1='"+MakeSQLsafe(parentID)+"' AND ID2='"+MakeSQLsafe(childID)+"'");

			if(rs.first()){ // value already there - don't insert it again
				;
			}	
			else
			{					
				statement = "insert into " + relationTable
				+ " (CreationDate, ID1, ID2) VALUES "
				+ "(CURRENT_TIMESTAMP,'"
				+ MakeSQLsafe(parentID)
				+ "','"
				+ MakeSQLsafe(childID) 
				+ "')" ;
				st.executeUpdate(statement);			
			}
		}catch(Exception e){
			System.err.println("Build relation insert error: " + e.getMessage());  
			System.exit(-1);			
		}

	}



	public static void CheckRelations(){
		ResultSet rs = null;
		ResultSet rs1 = null;
		String statement = null;
		String tableName1=null;
		String tableName2=null;
		String relationTable=null;
		int nValid=1;

		for (EADBDescription originalTable : originalTables.values()) {
				if(originalTable.getType().equals("Contains")||originalTable.getType().equals("TakesReferenceFrom")){								
				relationTable=originalTable.getNameDB();
				tableName1=originalTable.getFirstTable();
				tableName2=originalTable.getSecondTable();
				// read all value pairs
				try {

						rs = st.executeQuery("SELECT ID1, ID2, ID FROM "
								+ relationTable);

						if(rs.first()){ // values available
							while (rs.next()) {
								nValid=1;
								rs1=st2.executeQuery("SELECT ID FROM "
										+ tableName1
										+ " where ID='"+rs.getString(1)+"'");
								if(!rs1.first())System.err.println("Reference to non-existing ID1 "+rs.getString(1)+" in table "+relationTable+" removed.");
								if(!rs1.first())nValid=0;
								rs1=st2.executeQuery("SELECT ID FROM "
										+ tableName2
										+ " where ID='"+rs.getString(2)+"'");
								if(!rs1.first())System.err.println("Reference to non-existing ID2 "+rs.getString(2)+" in table "+relationTable+" removed.");
								if(!rs1.first())nValid=0;
								if(nValid==0){ // remove entry
									statement = "DELETE FROM " + relationTable
									+ " WHERE ID='"+rs.getString(3)+"'";																 															
									st2.executeUpdate(statement);
								}														
							}					
						}	
					
				}catch(Exception e){
					System.err.println("Check relation table error: " + e.getMessage());  
					System.exit(-1);			
				}			
				}
		}
		}

	public static void CreateReferenceLink(String fromName,String fromID,String toName,String toID){
		String strReferenceTable=null;
		String statement = null;
		ResultSet rs = null;
		
		for (EADBDescription originalTable : originalTables.values()) {
				//System.out.println("Check "+originalTable.getName()+" against "+fromName+"_TakesReferenceFrom_"+toName);
				if(originalTable.getName().equalsIgnoreCase(fromName+"_TakesReferenceFrom_"+toName)){
					strReferenceTable=originalTable.getNameDB();
			}
		}
		if(strReferenceTable==null){
		System.err.println("No connecting reference table found for: "+fromName+" to "+toName);
		}
		else { //create reference link
			// check whether we already have this entry
			try {
				rs = st.executeQuery("SELECT ID FROM "
						+ strReferenceTable
						+ " where ID1='"+MakeSQLsafe(fromID)+"' AND ID2='"+MakeSQLsafe(toID)+"'");

				if(rs.first()){ // value already there - don't insert it again
					;
				}	
				else
				{					
					statement = "insert into " + strReferenceTable
					+ " (CreationDate, ID1, ID2) VALUES "
					+ "(CURRENT_TIMESTAMP,'"
					+ MakeSQLsafe(fromID)
					+ "','"
					+ MakeSQLsafe(toID) 
					+ "')" ;
					st.executeUpdate(statement);
				}
			}catch(Exception e){
				System.err.println("Build relation insert error: " + e.getMessage());  
				System.exit(-1);			
			}		
		}			
	}

}

