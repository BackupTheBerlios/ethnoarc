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
package de.fhg.fokus.se.ethnoarc.zti_import;


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

public class ZTI_Import {
    static int import_mode=3;  // mode = 2 -> new import style with wl references
	static String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_zti";
	static String DBUSERNAME ="admin";
	static String DBPASSWORD ="adminPW";

	static Logger logger = Logger.getLogger(ZTI_Import.class.getName());

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
		sFile = "c:/ethnoArc/ExportZTI/";
		if(import_mode==2) DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_zti2";
		if(import_mode==3) DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_zti2";
		Logger.getLogger(ZTI_Import.class.getName());
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

		// parse input files - first go through the word lists
		File checkFile = new File(sFile);
		if (!checkFile.isDirectory()) {
			Process_ZTI_Export_File(checkFile.getAbsolutePath(),1);
		} else {
			Process_ZTI_Export_Directory(checkFile.getAbsolutePath(),1);
		}
		// parse input files - now parse actual data
	   checkFile = new File(sFile);
		if (!checkFile.isDirectory()) {
			Process_ZTI_Export_File(checkFile.getAbsolutePath(),2);
		} else {
			Process_ZTI_Export_Directory(checkFile.getAbsolutePath(),2);
		}		
		
		if(import_mode==2)CreateReferenceLinks();
		if(import_mode==3)CreateReferenceLinks();
		//BuildConnectingLinks("c_095PlaceOfColl","wl_095Locality","c_095PlaceOfColl_Contains_wl_095Locality");
		//BuildConnectingLinks("cc_095Collector","wl_095Person","cc_095Collector_Contains_wl_095Person");
		//BuildConnectingLinks("cc_095Role","wl_095CollRole","cc_095Role_Contains_wl_095CollRole");
		//BuildConnectingLinks("c_095Presenters","wl_095Person","c_095Presenters_Contains_wl_095Person");
		//BuildConnectingLinks("s_095Presenters","wl_095Person","s_095Presenters_Contains_wl_095Person");
		//BuildConnectingLinks("st_095TuneSystem","wl_095TuneSystem","st_095TuneSystem_Contains_wl_095TuneSystem");
		//BuildConnectingLinks("st_095TuneTone","wl_095Tones","st_095TuneTone_Contains_wl_095Tones");
		//BuildConnectingLinks("st_095TuneType","wl_095TuneTypes","st_095TuneType_Contains_wl_095TuneTypes");
		//BuildConnectingLinks("st_095Cadence","wl_095Cadences","st_095Cadence_Contains_wl_095Cadences");
		//BuildConnectingLinks("st_095Syllables","wl_095Syllables","st_095Syllables_Contains_wl_095Syllables");

		System.out.println("Finished.");
	}	
	public static void Process_ZTI_Export_Directory(String inputDirectory, int pass){

		File directoryFile = new File(inputDirectory);
		String[] nameList = directoryFile.list();
		for (int i = 0; i < nameList.length; i++) {
			File singleFile = new File(inputDirectory + '/' + nameList[i]);
			if (singleFile.isDirectory())
				Process_ZTI_Export_Directory(singleFile.getAbsolutePath(),pass);
			else if (singleFile.getAbsolutePath().toLowerCase()
					.endsWith(".xml"))			
				Process_ZTI_Export_File(singleFile.getAbsolutePath(),pass);
		}
	}
	public static void Process_ZTI_Export_File(String inputFile, int pass){
		DOMParser parser = new DOMParser();
		Document doc = null;
		ResultSet rs = null;
		String statement = null;
		String strNodeName = null;
		String strTableName = null;
		String strAttributeContent = null;
		String strAttributeName = null;
		String attributeTableName=null;
		String strNodeContent=null;
		String strRemark = null;
		int valueID=-1;
		Boolean isIDfield;
		Boolean bIDFieldFound=true;
		int nObjectsProcessed=0;

		if(inputFile.contains("_wl_")){
			if(pass==2)return;
		}
		else {
			if(pass==1)return;
		}
			
		//if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_Syllables.xml"))return;

	
		/*if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_110_Collection.xml"))return;			
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_111_Collectors.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_115_MediaParts.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_130_Segments.xml"))return;			
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_143_Tunes.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_Cadences.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_Locality.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_CollRole.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_Person.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_Tones.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_TuneSystem.xml"))return;
		if(inputFile.equals("c:\\ethnoArc\\ExportZTI\\EA_wl_TuneTypes.xml"))return;
		*/
		 
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
			if( !root.item(i).getLocalName().equals("ZTI_data")){
				System.err.println("Unexpected root node name "+root.item(i).getLocalName()+" in file "+inputFile+" - skipping!");
				return;				
			}
		}
		NodeList elements = root.item(0).getChildNodes();
		// parse element nodes
		for (int i = 0; i < elements.getLength(); i++) {
			// check whether this item name exist somewhere in the structure
			strTableName=null;
			strNodeName=new String(elements.item(i).getNodeName());

			nObjectsProcessed++;
			//    if(nObjectsProcessed>100)return; // to speed up testing, we sometime only use the first 100 items

			strLastConnector=null;
			nLastConnectorID=-1;

			for (EADBDescription originalTable : originalTables.values()) {
				if(originalTable.getType().equals("object")){
					//System.out.println("Check "+originalTable.getName()+" against "+strNodeName);
					if(originalTable.getName().equalsIgnoreCase(strNodeName)){
						strTableName=originalTable.getNameDB();
					}
				}
			}
			if(strTableName==null){
				if(!strNodeName.equals("#text")){
					System.err.println("Name not matching match for "+strNodeName);
				}
			}
			else {

				try {
					strNodeContent=null;
					strRemark=null;
					isIDfield=false;
					if(elements.item(i).getAttributes().getLength()!=0){

						for(int j=0;j<elements.item(i).getAttributes().getLength();j++){
							if(elements.item(i).getAttributes().item(j).getNodeName().equals("c__WlItemID")) 
								strNodeContent=new String(elements.item(i).getAttributes().item(j).getNodeValue());
							else {
								strAttributeContent=new String(elements.item(i).getAttributes().item(j).getNodeValue());
								strAttributeName=new String(elements.item(i).getAttributes().item(j).getNodeName());
								isIDfield=true;

								if(elements.item(i).getAttributes().item(j).getNodeName().equals("c__ID"));
								else if(elements.item(i).getAttributes().item(j).getNodeName().equals("cc__ID")); // collectors ID
								else if(elements.item(i).getAttributes().item(j).getNodeName().equals("cm__ID")) ;// media ID
								else if(elements.item(i).getAttributes().item(j).getNodeName().equals("s__ID")); // segment ID
								else if(elements.item(i).getAttributes().item(j).getNodeName().equals("st__ID")) ;// tune system ID
								else if(elements.item(i).getAttributes().item(j).getNodeName().equals("s_Remarks")){
									isIDfield=false;
									strRemark=strAttributeContent;
								}// straight forward sub element - easy to handle
								else {
									System.err.println("Unexpected attribute in main element: ");
									System.err.println(elements.item(i).getAttributes().item(j).getNodeValue());
									System.err.println(elements.item(i).getAttributes().item(j).getNodeName());
								}
							}
						}
					}			
					if(elements.item(i).getTextContent()==null)System.err.println("Has text content"+elements.item(i).getTextContent());

					valueID=-1;
					bIDFieldFound=false;
					// handling for ID fields - we need to check whether it's already there...
					// it is slightly tricky, since we don't look for the content of the base object,
					// but for the content of a contained object and the need to determine the
					// root table, if we got one.
					if(isIDfield){
						if(strAttributeName.equals("c__ID")&&strTableName.equals("Collection")){
							attributeTableName="collection";
						}
						else if(strAttributeName.equals("cc__ID")&&strTableName.equals("c_095Collectors")){
							attributeTableName="c_095collectors";
						}
						else if(strAttributeName.equals("cm__ID")&&strTableName.equals("c_095MediaParts")){
							attributeTableName="c_095mediaparts";
						}
						else if(strAttributeName.equals("s__ID")&&strTableName.equals("Segments")){
							attributeTableName="segments";
						}
						else if(strAttributeName.equals("st__ID")&&strTableName.equals("s_095TuneDetails")){
							attributeTableName="s_095tunedetails";
						}													
						else System.err.println("Main attribute not yet handled: "+strAttributeName);

						rs = st.executeQuery("SELECT ID FROM "
								+ attributeTableName
								+ " where CONTENT='"+MakeSQLsafe(strAttributeContent)+"'");
						if(rs.first()){						
							valueID = rs.getInt(1);
									bIDFieldFound=true;
						}
						else 	bIDFieldFound=false;
					}

					String strContent=strNodeContent;
					if(strContent==null)strContent=strAttributeContent;
					
					if(valueID==-1){					
						if(strContent==null)
							statement = "insert into " + strTableName
							+ " (CreationDate) VALUES "
							+ "(CURRENT_TIMESTAMP);";
						else 
							statement = "insert into " + strTableName
							+ " (CreationDate,Content) VALUES "
							+ "(CURRENT_TIMESTAMP,'"
							+ MakeSQLsafe(strContent) 
							+ "')" 
							;						
						st.executeUpdate(statement);
						// read newly created object ID

						rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
								+ strTableName
								+ " where ID=LAST_INSERT_ID()");
						rs.next();
						valueID = rs.getInt(1);
					}

					// build connection to any children of this node 
					NodeList children = elements.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Process_Child_Node(strTableName,valueID, children.item(j));						
					}		


					// special handling for s_Remarks attribute value
					if(strRemark!=null){
						statement = "insert into " + "s_095Remarks"
						+ " (CreationDate,Content) VALUES "
						+ "(CURRENT_TIMESTAMP,'"
						+ MakeSQLsafe(strRemark) 
						+ "')" ;		
						st.executeUpdate(statement);
						rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
								+ "s_095Remarks"
								+ " where ID=LAST_INSERT_ID()");
						rs.next();
						int remarkID = rs.getInt(1);		
						statement = "insert into " + "Segments_Contains_s_095Remarks"
						+ " (CreationDate,ID1,ID2) VALUES "
						+ "(CURRENT_TIMESTAMP," + valueID + "," + remarkID
						+ ")";
						st.executeUpdate(statement);						
					}
				} catch (Exception e) {
					System.err.println("DB Input error: " + e.getMessage());  
					e.printStackTrace();
					System.exit(-1);			
				}
			}	
		}


	}
	public static String MakeSQLsafe(String anyString) {
		return ((anyString.replaceAll("'", "''")).trim());
	}
	public static void Process_Child_Node(String parentTable,int parentID, Node childNode){
		ResultSet rs = null;
		String statement = null;
		String strNodeName = null;
		String strTableName = null;
		String strAttributeContent= null;
		String strAttributeName= null;
		String attributeTableName= null;
		Boolean  isIDfield=false;
		int valueID=-1;
		Boolean isIDentity;

		if(childNode.getNodeName().equals("#text"))return;
		// find proper table for this object
		// check whether this item name exist somewhere in the structure
		strTableName=null;
		strNodeName=new String(childNode.getNodeName());
		// some mapping for variant names between structure and export
		if(strNodeName.equals("C_Media"))
			strNodeName=new String("c_MediaParts");
		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095Cadences")))
			strNodeName=new String("wl_095Cadences");
		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095CollRole")))
			strNodeName=new String("wl_095CollRole");
		if(strNodeName.equals("LangVersion")&&(parentTable.equals("wl_095CollRole")))
			strNodeName=new String("wl_095CollRole");
		if(strNodeName.equals("LangExpression")&&(parentTable.equals("wl_095CollRole_046LangVersion")))
			strNodeName=new String("wl_CollRole.LangExpression");
		if(strNodeName.equals("LangCode")&&(parentTable.equals("wl_095CollRole_046LangVersion")))
			strNodeName=new String("wl_CollRole.LangCode");

		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095Locality")))
			strNodeName=new String("wl_095Locality");
		if(strNodeName.equals("LangVersion")&&(parentTable.equals("wl_095Locality")))
			strNodeName=new String("wl_Locality.LangVersion");
		if(strNodeName.equals("LangExpression")&&(parentTable.equals("wl_095Locality_046LangVersion")))
			strNodeName=new String("wl_Locality.LangExpression");
		if(strNodeName.equals("LangCode")&&(parentTable.equals("wl_095Locality_046LangVersion")))
			strNodeName=new String("wl_Locality.LangCode");			    
		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095Person")))
			strNodeName=new String("wl_095Person");
		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095Syllables")))
			strNodeName=new String("wl_095Syllables");
		if(strNodeName.equals("DefExpression")&&(parentTable.equals("wl_095TuneSystem")))
			strNodeName=new String("wl_095TuneSystem");

		// now perform the actual check whether this table exists			    
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				//System.out.println("Check "+originalTable.getName()+" against "+strNodeName);
				if(originalTable.getName().equalsIgnoreCase(strNodeName)){
					strTableName=originalTable.getNameDB();
				}
			}
		}

		isIDfield=false;
		if(childNode.getAttributes().getLength()!=0){

			for(int i=0;i<childNode.getAttributes().getLength();i++){

				strAttributeContent=new String(childNode.getAttributes().item(i).getNodeValue());
				strAttributeName=new String(childNode.getAttributes().item(i).getNodeName());
				isIDfield=true;						
				if(childNode.getAttributes().item(i).getNodeName().equals("cm_MediaJelzet"))isIDfield=false; // redundant information - ignore
				else if(childNode.getAttributes().item(i).getNodeName().equals("cm__ID")) ;
				else if(childNode.getAttributes().item(i).getNodeName().equals("cc__ID")) ;
				else if(childNode.getAttributes().item(i).getNodeName().equals("c__ID")) ;
				else { 
					isIDfield=false;
					System.err.println("Unexpected attribute in child detected:");
					System.err.println(childNode.getAttributes().item(i).getNodeName());
					System.err.println(childNode.getAttributes().item(i).getNodeValue());
				}
			}
		}


		if(strTableName==null){
			if(parentTable.equals(strNodeName)){
				strTableName=parentTable;
				statement = "UPDATE " + parentTable
				+ " SET Content = "
				+ "'"
				+ MakeSQLsafe(childNode.getTextContent()) 
				+ "' WHERE id = "+parentID
				;
				try{
				st.executeUpdate(statement);
				} catch (Exception e) {
					System.err.println("DB update error: " + e.getMessage());  
					System.exit(-1);			
				}			
				return;
			}
			else {
				System.err.println("Name does not match any table for "+strNodeName+" with parent "+parentTable);
				System.exit(-1);
			}
		}
		try {
			int nHasTextContent=1;		
			if(childNode.getTextContent()==null)	nHasTextContent=0;
			else if(childNode.getTextContent().length()==0)	nHasTextContent=0;
			else if ((int)childNode.getTextContent().charAt(0)==10)	nHasTextContent=0;

			if(strNodeName.equals("Segments")){
				// segments are pretty odd, since some of the import files
				// use the id for the segment, while others us the proper s__ID
				strAttributeContent=(childNode.getTextContent());
				strAttributeName="s__ID";
				isIDfield=true;		
			}	                  
			if(strNodeName.equals("s_TuneDetails")){
				// segments are pretty odd, since some of the import files
				// use the id for the segment, while others us the proper s__ID
				strAttributeContent=(childNode.getTextContent());
				strAttributeName="st__ID";
				isIDfield=true;		
			}	                  
			valueID=-1;
			// handling for ID fields - we need to check whether it's already there...
			if(isIDfield){
				if(strAttributeName.equals("cc__ID")&&strTableName.equals("c_095Collectors")){
					attributeTableName="c_095collectors";
				}
				else if(strAttributeName.equals("cm__ID")&&strTableName.equals("c_095MediaParts")){
					attributeTableName="c_095mediaparts";
				}
				else if(strAttributeName.equals("s__ID")&&strTableName.equals("Segments")){
					attributeTableName="segments";
				}
				else if(strAttributeName.equals("st__ID")&&strTableName.equals("s_095TuneDetails")){
					attributeTableName="s_095tunedetails";
				}													
				else System.err.println("Lower level attribute not yet handled: "+strAttributeName);

				rs = st.executeQuery("SELECT ID FROM "
						+ attributeTableName
						+ " where CONTENT='"+MakeSQLsafe(strAttributeContent)+"'");

				if(rs.first()){
					valueID = rs.getInt(1);
				}
			}

			isIDentity=false;		
			//System.err.println(strNodeName);
			if(strNodeName.equals("c_PlaceOfColl"))isIDentity=true;
			if(strNodeName.equals("C_Presenters"))isIDentity=true;
			if(strNodeName.equals("cc_Collector"))isIDentity=true;
			if(strNodeName.equals("cc_Role"))isIDentity=true;
			if(strNodeName.equals("Segments"))isIDentity=true;
			if(strNodeName.equals("st_TuneSystem"))isIDentity=true;
			if(strNodeName.equals("st_Cadence"))isIDentity=true;
			if(strNodeName.equals("s_TuneDetails"))isIDentity=true;
			if(strNodeName.equals("s_Presenters"))isIDentity=true;
			// process entities that serve as ID fields and aren't attributes
			if((valueID==-1)&&(isIDentity)){
				rs = st.executeQuery("SELECT ID FROM "
						+ strTableName
						+ " where CONTENT='"+MakeSQLsafe(childNode.getTextContent())+"'");

				if(rs.first()){
					valueID = rs.getInt(1);
				}
			}
			if(	valueID==-1){								 
				if(nHasTextContent==0){
					if(strAttributeContent==null){
					statement = "insert into " + strTableName
					+ " (CreationDate) VALUES "
					+ "(CURRENT_TIMESTAMP);";
					}
					else {
						statement = "insert into " + strTableName
						+ " (CreationDate,Content) VALUES "
						+ "(CURRENT_TIMESTAMP,'"
						+ MakeSQLsafe(strAttributeContent) 
						+ "')" 
						;
					}
				}
				else 
					statement = "insert into " + strTableName
					+ " (CreationDate,Content) VALUES "
					+ "(CURRENT_TIMESTAMP,'"
					+ MakeSQLsafe(childNode.getTextContent()) 
					+ "')" 
					;
				st.executeUpdate(statement);
				// read newly created object ID
				rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
						+ strTableName
						+ " where ID=LAST_INSERT_ID()");
				rs.next();
				valueID = rs.getInt(1);
			}
			//System.err.println("Inserted as id: " + valueID);
			// find relation between parent node and this child
			Process_Parent_Child_Relation( parentTable, parentID, strTableName, valueID);											 
			// build connection to any children of this node 
			NodeList children = childNode.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Process_Child_Node(strTableName,valueID, children.item(j));
			}		
		} catch (Exception e) {
			System.err.println("DB insert error: " + e.getMessage());  
			System.exit(-1);			
		}
		
	}


	public static void Process_Parent_Child_Relation(String parentTable,int parentID, String childTable,int childID){
		// trying to a direct connection between these tables
		String statement = null;
		String strTableName = null;
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equalsIgnoreCase("contains")||originalTable.getType().equalsIgnoreCase("AlternativeLanguage")){
				if((originalTable.getFirstTable().equalsIgnoreCase(parentTable))&&
						(originalTable.getSecondTable().equalsIgnoreCase(childTable)))
				{
					strTableName=originalTable.getNameDB();
				}
			}
		}
		if(strTableName==null){
			Process_Parent_Child_Indirect_Relation( parentTable, parentID,  childTable, childID);
			//System.exit(-1);			
		}
		else {
			try {
				statement = "insert into " + strTableName
				+ " (CreationDate,ID1,ID2) VALUES "
				+ "(CURRENT_TIMESTAMP," + parentID + "," + childID
				+ ")";
				st.executeUpdate(statement);
				
			} catch (Exception e) {
				System.err.println("DB relation table insert error: " + e.getMessage());  
				System.exit(-1);			
			}			
		}

	}
	public static void Process_Parent_Child_Indirect_Relation(String parentTable,int parentID, String childTable,int childID){
		ResultSet rs = null;
		String statement = null;

		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equalsIgnoreCase("contains")||originalTable.getType().equalsIgnoreCase("AlternativeLanguage")){
				if((originalTable.getFirstTable().equalsIgnoreCase(parentTable))){
					for (EADBDescription originalTable2 : originalTables.values()) {
						if(originalTable2.getType().equalsIgnoreCase("contains")||originalTable.getType().equalsIgnoreCase("AlternativeLanguage")){
							if((originalTable2.getSecondTable().equalsIgnoreCase(childTable))&&
									(originalTable.getSecondTable().equalsIgnoreCase(originalTable2.getFirstTable()))){
								// connection found!
								//System.out.println("Found connection via:"+ originalTable.getName()+" and "+originalTable2.getName()+" and "+originalTable.getSecondTable());
								// check whether the connecting object was already created for this record - in this case re-use it
								// (practically, this means that Maßangaben und MaßangabenTyp gets associated to the same Maßangabenkomplex)
								if((strLastConnector==null)||(!strLastConnector.equals(originalTable2.getFirstTable()))){
									// we need to create the connecting item
									statement = "insert into " + originalTable2.getFirstTable()
									+ " (CreationDate) VALUES "
									+ "(CURRENT_TIMESTAMP);";
									try{
										st.executeUpdate(statement);
										rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
												+ originalTable2.getFirstTable()
												+ " where ID=LAST_INSERT_ID()");
										rs.next();
										nLastConnectorID = rs.getInt(1);
									}
									catch (Exception e) {
										System.err.println("DB connecting table insert error: " + e.getMessage());  
										System.exit(-1);			
									}			
								}
								strLastConnector=originalTable2.getFirstTable();
								// now connect the items with the two relation tables
								try {
									statement = "insert into " + originalTable.getNameDB()
									+ " (CreationDate,ID1,ID2) VALUES "
									+ "(CURRENT_TIMESTAMP," + parentID + "," + nLastConnectorID
									+ ")";
									st.executeUpdate(statement);
									statement = "insert into " + originalTable2.getNameDB()
									+ " (CreationDate,ID1,ID2) VALUES "
									+ "(CURRENT_TIMESTAMP," + nLastConnectorID + "," + childID
									+ ")";
									st.executeUpdate(statement);
									return;
								} catch (Exception e) {
									System.err.println("DB relation table insert error: " + e.getMessage());  
									System.exit(-1);			
								}			
							} 
						}
					}
				}
			}
		}
		System.err.println("Attempting to connect "+ parentTable+" and "+childTable+" failed");

	}

	public static void BuildConnectingLinks(String table1, String table2, String tableConnect){
		// connecting tables using identical ids.
		int valueID1=-1;
		int valueID2=-1;
		ResultSet rs = null;
		ResultSet rs2 = null;
		String statement = null;
		String content = null;

		try{
			rs = st.executeQuery("SELECT ID, CONTENT FROM "
					+ table1);
			if(!rs.first())return;
			while(true){
				valueID1 = rs.getInt(1);
				//System.out.println("Type: "+rs.getType()+rs.getString(2));
				if(rs.getString(2)==null)content="";
				else content = new String(rs.getString(2));
				// look for this in second table
				rs2 = st2.executeQuery("SELECT ID FROM "
						+ table2
						+ " where CONTENT='"+content+"'");

				if(rs2.first()){
					valueID2 = rs2.getInt(1);				
					statement = "insert into " +tableConnect
					+ " (CreationDate,ID1,ID2) VALUES "
					+ "(CURRENT_TIMESTAMP," + valueID1 + "," + valueID2
					+ ")";
					st2.executeUpdate(statement);		
					//System.out.println("Found Content: "+content);
				}
				rs.next();
				if(rs.isAfterLast())return;  	
			}
		}catch (Exception e){System.err.println("Connection error: "+e.getMessage());e.printStackTrace();}

	}
	public static void CreateReferenceLinks(){
		ResultSet rs = null;
		String statement = null;
		String lastID="";
		
		for (EADBDescription originalTable : originalTables.values()) {			
			if(originalTable.getType().equalsIgnoreCase("TakesReferenceFrom")){
				try {
				// look for content in the first table
				rs = st.executeQuery("SELECT  "+originalTable.getFirstTable()+".ID,"+originalTable.getSecondTable()+".ID FROM "
						+ originalTable.getFirstTable()+","+ originalTable.getSecondTable()
						+ " where "+originalTable.getFirstTable()+".CONTENT="+originalTable.getSecondTable()+".CONTENT ");
				lastID="";
				if(rs.first()){
					int cont=1;
					while(cont==1){
						if(!rs.getString(1).equals(lastID)){
					System.out.println("Found pair "+(originalTable.getName())+" "+rs.getString(1)+" "+rs.getString(2));
					statement = "insert into " +originalTable.getNameDB()
					+ " (CreationDate,ID1,ID2) VALUES "
					+ "(CURRENT_TIMESTAMP," + rs.getString(1) + "," + rs.getString(2)
					+ ")";
					st2.executeUpdate(statement);		
					
					
						}
						lastID=rs.getString(1);
					rs.next();
					if(rs.isAfterLast())cont=0;
					}
				}
			}catch (Exception e){System.err.println("Connection error: "+e.getMessage());e.printStackTrace();}
				
			} //if(originalTable.getType().equalsIgnoreCase("TakesReferenceFrom"){
		}
		
	}

}
