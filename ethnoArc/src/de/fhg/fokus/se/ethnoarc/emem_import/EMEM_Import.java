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
package de.fhg.fokus.se.ethnoarc.emem_import;


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
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame;
import de.fhg.fokus.se.ethnoarc.ethnoMARS.EthnoMARS;

public class EMEM_Import {

	static int LAS_Version=20;
	
	static String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_emem";
	static String DBUSERNAME ="admin";
	static String DBPASSWORD ="adminPW";
	
	static Logger logger = Logger.getLogger(MainUIFrame.class.getName());
	
	static Hashtable <String,EADBDescription> originalTables = null;
	static DBStructure dbStructure=null;
	static DBHandling dbHandle=null;	
	
	static Connection cn = null;
	static	Statement st = null;
	
	static String strLastConnector=null;
	static int nLastConnectorID=-1;

	static int nLastAudioID=-1;
	static int nLastAudio2ID=-1;
	static int nLastFilmID=-1;
	static int nLastFilm2ID=-1;
	static int nLastSammelID=-1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sFile = "";
		
		Logger.getLogger(EMEM_Import.class.getName());
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		
		if(LAS_Version==20)DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_emem";

		sFile = "c:/ethnoArc/ExportEMEM/";
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
			} catch (Exception ex) {
			  System.err.println("Can't connect to database server at "
										+ DBURL);
			System.exit(-1);
			}
	 
	  // parse input files
		for(int pass=1;pass<=2;pass++){	
		File checkFile = new File(sFile);
		if (!checkFile.isDirectory()) {
			Process_EMEM_Export_File(checkFile.getAbsolutePath(),pass);
		} else {
			Process_EMEM_Export_Directory(checkFile.getAbsolutePath(),pass);
		}
		}
     System.out.println("Finished.");
	}	
	public static void Process_EMEM_Export_Directory(String inputDirectory, int pass){

			File directoryFile = new File(inputDirectory);
			String[] nameList = directoryFile.list();
			for (int i = 0; i < nameList.length; i++) {
				File singleFile = new File(inputDirectory + '/' + nameList[i]);
				if (singleFile.isDirectory())
					Process_EMEM_Export_Directory(singleFile.getAbsolutePath(),pass);
				else if (singleFile.getAbsolutePath().toLowerCase()
						.endsWith(".xml"))			
					Process_EMEM_Export_File(singleFile.getAbsolutePath(),pass);
			}
	}
	public static void Process_EMEM_Export_File(String inputFile, int pass){
			DOMParser parser = new DOMParser();
			Document doc = null;
			ResultSet rs = null;
			String statement = null;
			String strNodeName = null;
			String strTableName = null;
			int valueID=-1;
			int isPersonenKoerperschaft=0;


			if (pass==1){
				if(inputFile.endsWith("allePersonen.eAPerKör.060807.xml"))return;
			}
			else
				if(!inputFile.endsWith("allePersonen.eAPerKör.060807.xml"))return;
					
			
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
				if( root.item(i).getLocalName().equals("EAememPerKör"))isPersonenKoerperschaft=1;
				else isPersonenKoerperschaft=0;
				
				// check whether this is a know entity
				if( !root.item(i).getLocalName().equals("EAemem")&&!root.item(i).getLocalName().equals("EAememPerKör")){
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

			    strLastConnector=null;
				nLastConnectorID=-1;
			    
                if((strNodeName.equalsIgnoreCase("Record"))&&(isPersonenKoerperschaft==1))
                	strNodeName="PersonKörperschaft";
                if((strNodeName.equalsIgnoreCase("Record"))&&(isPersonenKoerperschaft==0))
                	strNodeName="Sammlungsobjekt";
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
				  // got appropriate table - for current export it is always 'record' - so let's create an entry
				try {
					statement = "insert into " + strTableName
							+ " (CreationDate) VALUES "
							+ "(CURRENT_TIMESTAMP);";
					st.executeUpdate(statement);
					// read newly created object ID
					
					rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
									+ strTableName
									+ " where ID=LAST_INSERT_ID()");
					rs.next();
					valueID = rs.getInt(1);
					// build connection to any children of this node 
					NodeList children = elements.item(i).getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Process_Child_Node(strTableName,valueID, children.item(j));
					}		
				} catch (Exception e) {
				System.err.println("DB Input error: " + e.getMessage());  
				System.exit(-1);			
				}
          }	
          }						
	}
	public static String MakeSQLsafe(String anyString) {
		// whitespace handling for descriptions
		String stringReturn=anyString.replace('\t', ' ');
		stringReturn=stringReturn.replace('\r', ' ');
		stringReturn=stringReturn.replace('\n', ' ');
		while (stringReturn.contains("  ")) 
			stringReturn=stringReturn.replace("  ", " ");			
		return ((stringReturn.replaceAll("'", "''")).trim());
	}
	public static void Process_Child_Node(String parentTable,int parentID, Node childNode){
			ResultSet rs = null;
			String statement = null;
			String strNodeName = null;
			String strTableName = null;
			int valueID=-1;
			
			
            if(childNode.getNodeName().equals("#text"))return;
            // find proper table for this object
			    // check whether this item name exist somewhere in the structure
			    strTableName=null;
			    strNodeName=new String(childNode.getNodeName());
			    // some mapping for variant names between structure and export
			    if(strNodeName.equals("PersonenKörperschaftenKomplex"))
			    	strNodeName=new String("PersonKörperschaftKomplex");
			 //   if(strNodeName.equals("PerKör.ObjektKomplex"))
			 //   	strNodeName=new String("PerKör.ObjekteKomplex");
			 if(LAS_Version<20){
			    if(strNodeName.equals("bemerkungSammlung"))
			    	strNodeName=new String("BemerkungenSammlung");
			 }
			    if(strNodeName.equals("bemerkungen"))
			    	strNodeName=new String("PerKör.Bemerkungen");
			    if(strNodeName.equals("titelStand"))
			    	strNodeName=new String("PerKör.TitelStand");
			    if(strNodeName.equals("artKörpersch"))
			    	strNodeName=new String("PerKör.ArtKörperschaft");
			    if(strNodeName.equals("Film.verfübareFormate"))
			    	strNodeName=new String("Film.verfügbareFormate");
			    if(strNodeName.equals("handling"))
			    	strNodeName=new String("HandlingVerpackungTransport");
			    if(strNodeName.equals("maßangabe"))
			    	strNodeName=new String("MaßangabeMaßangabe");
			    if(strNodeName.equals("PKKArtDesBezuges"))
			    	strNodeName=new String("PKKArtDesBezugs");
			    if(strNodeName.equals("PerKer.BearbDat"))
			    	strNodeName=new String("PerKör.BearbDat");
			    if(strNodeName.equals("Audio.KategorieGenre"))
			    	strNodeName=new String("Audio.Musikgattung");			    

			    // now perform the actual check whether this table exists			    
			    for (EADBDescription originalTable : originalTables.values()) {
				if(originalTable.getType().equals("object")){
				//System.out.println("Check "+originalTable.getName()+" against "+strNodeName);
				   if(originalTable.getName().equalsIgnoreCase(strNodeName)){
				        strTableName=originalTable.getNameDB();
				     }
					}
				}

			    if(strNodeName.equals("MaßangabeMaßangabe")&&
			    		parentTable.equals("Sammlungsobjekt")){		    	
			    		int massangabekomplexID=CreateConnectedSubNode(parentTable, parentID, "Ma_223angabeKomplex");
			    		Process_Child_Node("Ma_223angabeKomplex",massangabekomplexID, childNode);
			    		return;			    		
			    }
			    if(strNodeName.equals("MaßangabeMaßangabe")){
			    	// Special Case: Unlike all other elements, "Maßangaben"
			    	// has the type as an attribute, not an element value.
			    	// we need extra handling for this.
			    	
			    	if(childNode.getAttributes().getLength()!=0)
			    		Process_Massangaben_Type(parentTable, parentID,childNode.getAttributes().item(0).getNodeValue());			    	
			    }
			    
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("PerKör.ObjektTitel"))){
					  // known issue
//							System.err.println("Known issue: Name does not match any table for "+strNodeName+" with parent "+parentTable);
						    return;				  
					}
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("PerKör.KueId"))){

					statement = "Update "+parentTable+ " set CONTENT='"
					+ MakeSQLsafe(childNode.getTextContent())
					+ "' where ID = '"+parentID+"'";

				try{
				st.executeUpdate(statement);		
					    return;			
				} catch (Exception e) {
					System.err.println("DB insert error: " + e.getMessage());  
					System.exit(-1);			
					}			
				}			
	
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("PerKör.ObjektKomplex"))){
					  // this is the complicated case - we need to build the references
					 // between persons and objects - we handle this in a separate routine
					 Process_Person_Link( parentTable, parentID,  childNode);
						    return;				  
					}				
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("ObjId"))){
					// special case - ObjId used to be a sub-element of "Sammlungsobjekt", but now 
					// the value is a direct part of "Sammlungsobjekt"
					
					statement = "Update "+parentTable+ " set CONTENT='"
						+ MakeSQLsafe(childNode.getTextContent())
						+ "' where ID = '"+parentID+"'";

					try{
					st.executeUpdate(statement);		
						    return;			
					} catch (Exception e) {
						System.err.println("DB insert error: " + e.getMessage());  
						System.exit(-1);			
						}			
					}
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("PKKPersonKörperschaft"))){
					// special case - PKKPersonKörperschaft used to be a sub-element of "PersonKörperschaftKomplex", but now 
					// the value is a direct part of "PersonKörperschaftKomplex"
					statement = "Update "+parentTable+ " set CONTENT='"
					+ MakeSQLsafe(childNode.getTextContent())
					+ "' where ID = '"+parentID+"'";

					try{
					st.executeUpdate(statement);		
						    return;			
					} catch (Exception e) {
						System.err.println("DB insert error: " + e.getMessage());  
						System.exit(-1);			
						}			
					}				
				if((strTableName==null)&&(strNodeName.equalsIgnoreCase("SWDKomplex"))){
					  // known issue - SWD Komplex no longer used
						    return;				  
					}
				
				if(strTableName==null){
						System.err.println("Name does not match any table for "+strNodeName+" with parent "+parentTable);
					    System.exit(-1);
				 }
				try {
                 int nHasTextContent=1;		
                  if(childNode.getTextContent()==null)	nHasTextContent=0;
                  else if(childNode.getTextContent().length()==0)	nHasTextContent=0;
                  else if ((int)childNode.getTextContent().charAt(0)==10)	nHasTextContent=0;
				 
				  if(nHasTextContent==0)				  
					statement = "insert into " + strTableName
							+ " (CreationDate) VALUES "
							+ "(CURRENT_TIMESTAMP);";
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

				public static void Process_Person_Link(String parentTable,int parentID, Node complexNode){
					String statement = null;
					String strIdent="";
					ResultSet rs = null;
					int ObjectID;
					int KomplexID;
					try{
					NodeList elements = complexNode.getChildNodes();
					// parse element nodes
					for (int i = 0; i < elements.getLength(); i++) {
					    String strNode=new String(elements.item(i).getNodeName());
					    // we only need the ident, the rest is available in the object description anyway
					 //   if(strNode.equalsIgnoreCase("PerKör.ObjektTitel"))strTitel=new String(elements.item(i).getTextContent());
					    if(strNode.equalsIgnoreCase("PerKör.ObjektIdent"))strIdent=new String(elements.item(i).getTextContent());
					 //   if(strNode.equalsIgnoreCase("PerKör.ObjektSachbegriff"))strSachbegriff=new String(elements.item(i).getTextContent());
					}					
					// now find 'sachmittel' with this ident
					statement="SELECT Sammlungsobjekt.ID FROM Sammlungsobjekt,Sammlungsobjekt_Contains_IdentNrKomplex,IdentNrKomplex_Contains_IdentNrInhalt,IdentNrInhalt "+
					  		 	"WHERE Sammlungsobjekt.ID=Sammlungsobjekt_Contains_IdentNrKomplex.ID1 AND "+
					  		 	"Sammlungsobjekt_Contains_IdentNrKomplex.ID2=IdentNrKomplex_Contains_IdentNrInhalt.ID1 AND "+
							    "IdentNrKomplex_Contains_IdentNrInhalt.ID2=IdentNrInhalt.ID AND "+
							    "IdentNrInhalt.Content='"+strIdent+"'";
					 rs = st.executeQuery(statement);
					 rs.next();
					 if(!rs.isFirst())return; // object does not match any existing id
					 ObjectID = rs.getInt(1);
					 // now we need to find the right Person reference in the object
						statement="SELECT PersonK_246rperschaftKomplex.ID FROM PersonK_246rperschaftKomplex,Sammlungsobjekt,PerK_246r_046Name_046Inhalt,Sammlungsobjekt_Contains_PersonK_246rperschaftKomplex,PerK_246r_046NameKomplex_Contains_PerK_246r_046Name_046Inh_5352,PerK_246r_046Namensangaben_Contains_PerK_246r_046NameKomplex,PersonK_246rperschaft_Contains_PerK_246r_046Namensangaben "+
			  		 	"WHERE PersonK_246rperschaftKomplex.Content=PerK_246r_046Name_046Inhalt.Content AND " +
			  		 	"PersonK_246rperschaftKomplex.ID=Sammlungsobjekt_Contains_PersonK_246rperschaftKomplex.ID2 AND "+
			  		 	"Sammlungsobjekt_Contains_PersonK_246rperschaftKomplex.ID1='"+ObjectID+"' AND "+
			  		 	"PerK_246r_046Name_046Inhalt.ID=PerK_246r_046NameKomplex_Contains_PerK_246r_046Name_046Inh_5352.ID2 AND "+
			  		 	"PerK_246r_046NameKomplex_Contains_PerK_246r_046Name_046Inh_5352.ID1=PerK_246r_046Namensangaben_Contains_PerK_246r_046NameKomplex.ID2 AND " +
			  		 	"PerK_246r_046Namensangaben_Contains_PerK_246r_046NameKomplex.ID1=PersonK_246rperschaft_Contains_PerK_246r_046Namensangaben.ID2 AND "+
			  		 	"PersonK_246rperschaft_Contains_PerK_246r_046Namensangaben.ID1='"+parentID+"'";				
						 rs = st.executeQuery(statement);
						 rs.next();
						 if(!rs.isFirst())return; // object does not match any existing id
						 KomplexID=rs.getInt(1);
						 // now er got all the relevant information, let's create the references
							statement = "insert into " + "PersonK_246rperschaft_TakesReferenceFrom_Sammlungsobjekt"
							+ " (CreationDate,ID1,ID2) VALUES "
							+ "(CURRENT_TIMESTAMP," + parentID + "," + ObjectID
							+ ")";
							st.executeUpdate(statement);
							statement = "insert into " + "PersonK_246rperschaftKomplex_TakesReferenceFrom_PersonK_24_6807"
							+ " (CreationDate,ID1,ID2) VALUES "
							+ "(CURRENT_TIMESTAMP," + KomplexID + "," + parentID
							+ ")";
							st.executeUpdate(statement);			  		 	
					 
					    
		
					} catch (Exception e) {
						System.err.println("DB insert error: " + e.getMessage());  
						System.exit(-1);			
						}		
				}

	public static void Process_Massangaben_Type(String parentTable,int parentID, String strTyp){
		String strTableName="ma_223angabetyp";
		int valueID=-1;
		ResultSet rs = null;
		String statement = null;		
        try{
		statement = "insert into " + strTableName
						+ " (CreationDate,Content) VALUES "
						+ "(CURRENT_TIMESTAMP,'"
						+ MakeSQLsafe(strTyp) 
						+ "')" 
						;
			  
				st.executeUpdate(statement);
				// read newly created object ID
				rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
								+ strTableName
								+ " where ID=LAST_INSERT_ID()");
				rs.next();
				valueID = rs.getInt(1);
				//System.err.println("Inserted as id: " + valueID);
				// find relation between parent node and this child
				Process_Parent_Child_Relation( parentTable, parentID, strTableName, valueID);											 
			} catch (Exception e) {
			System.err.println("DB insert error (Maßangabetyp): " + e.getMessage());  
			System.exit(-1);			
			}			
}
	
public static void Process_Parent_Child_Relation(String parentTable,int parentID, String childTable,int childID){
			// trying to a direct connection between these tables
			String statement = null;
			
			    String strTableName = null;
			    for (EADBDescription originalTable : originalTables.values()) {
			    if(originalTable.getType().equalsIgnoreCase("contains")){
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
	int valueID=-1;
	ResultSet rs = null;
	String statement = null;

			    String strConnectingTableName = null;
			    for (EADBDescription originalTable : originalTables.values()) {
			    if(originalTable.getType().equalsIgnoreCase("contains")){
				   if((originalTable.getFirstTable().equalsIgnoreCase(parentTable))){
			         for (EADBDescription originalTable2 : originalTables.values()) {
			          if(originalTable2.getType().equalsIgnoreCase("contains")){
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
  // build intermediate connections for new structure (since LAS v.20)
				int ObjekttypID;	    
				int AudioID, FilmID;	    
				if(nLastSammelID!=parentID){
					nLastAudioID=-1;
					nLastAudio2ID=-1;
					nLastFilmID=-1;
					nLastFilm2ID=-1;
				}
				
	
			    if(parentTable.equals("Sammlungsobjekt") && 
			    		( childTable.equals("Audio_046Besetzung")||
			    				childTable.equals("Audio_046Inhalt") ||
			    				childTable.equals("Audio_046Dokumentation") ||
			    				childTable.equals("Audio_046TextOriginal") ||
			    				childTable.equals("Audio_046Musikgattung"))){
			    	if((nLastSammelID==parentID)&&(nLastAudioID!=-1))
			    		AudioID=nLastAudioID;
			    	else {			    	
			    	ObjekttypID=CreateConnectedSubNode(parentTable, parentID, "Objekttyp");
			    	AudioID=CreateConnectedSubNode("Objekttyp", ObjekttypID, "ZusatzAudio1");
			    	}
			    	Process_Parent_Child_Relation("ZusatzAudio1",AudioID,  childTable, childID);
			    	nLastAudioID=AudioID;
			    	nLastSammelID=parentID;
			    	return;
			    }
			    if(parentTable.equals("Sammlungsobjekt") && 
			    		( childTable.equals("Audio_046TechnischeBemerkung")||
			    				childTable.equals("Audio_046Vorlage") ||
			    				childTable.equals("Audio_046Digitalisiert") ||
			    				childTable.equals("Audio_046Nadelschliff") ||
			    				childTable.equals("Audio_046Format"))){
			    	if((nLastSammelID==parentID)&&(nLastAudio2ID!=-1))
			    		AudioID=nLastAudio2ID;
			    	else {			    	
			    	ObjekttypID=CreateConnectedSubNode(parentTable, parentID, "Objekttyp");
			    	AudioID=CreateConnectedSubNode("Objekttyp", ObjekttypID, "ZusatzAudio2");
			    	}
			    	Process_Parent_Child_Relation("ZusatzAudio2",AudioID,  childTable, childID);
			    	nLastAudio2ID=AudioID;
			    	nLastSammelID=parentID;
			    	return;
			    }			    
			    if(parentTable.equals("Sammlungsobjekt") && 
			    		( childTable.equals("Film_046Form")||
			    				childTable.equals("Film_046Instrumente"))){
			    	if((nLastSammelID==parentID)&&(nLastFilmID!=-1))
			    		FilmID=nLastFilmID;
			    	else {			    	
			    	ObjekttypID=CreateConnectedSubNode(parentTable, parentID, "Objekttyp");
			    	FilmID=CreateConnectedSubNode("Objekttyp", ObjekttypID, "ZusatzFilmVideo1");
			    	}
			    	Process_Parent_Child_Relation("ZusatzFilmVideo1",FilmID,  childTable, childID);
			    	nLastFilmID=FilmID;
			    	nLastSammelID=parentID;
			    	return;
			    }			
			    if(parentTable.equals("Sammlungsobjekt") && 
			    		( childTable.equals("Film_046Verf_252gbareFormate")||
			    				childTable.equals("Film_046TV_045Norm") ||
			    				childTable.equals("Film_046Ton") ||
			    				childTable.equals("Film_046Farbe"))){
			    	if((nLastSammelID==parentID)&&(nLastFilm2ID!=-1))
			    		FilmID=nLastFilm2ID;
			    	else {			    	
			    	ObjekttypID=CreateConnectedSubNode(parentTable, parentID, "Objekttyp");
			    	FilmID=CreateConnectedSubNode("Objekttyp", ObjekttypID, "ZusatzFilmVideo2");
			    	}
			    	Process_Parent_Child_Relation("ZusatzFilmVideo2",FilmID,  childTable, childID);
			    	nLastFilm2ID=FilmID;
			    	nLastSammelID=parentID;
			    	return;
			    }						    
  			    
System.err.println("Attempting to connect "+ parentTable+" and "+childTable+" failed");

}

public static int CreateConnectedSubNode(String parentTable,int parentID, String childTable){
	String statement = null;
	ResultSet rs = null;
	int result=-1;
	
	    String strTableName = null;
	    for (EADBDescription originalTable : originalTables.values()) {
	    if(originalTable.getType().equalsIgnoreCase("contains")){
		   if((originalTable.getFirstTable().equalsIgnoreCase(parentTable))&&
		   	  (originalTable.getSecondTable().equalsIgnoreCase(childTable)))
		   {
		     strTableName=originalTable.getNameDB();
		     }
			}
		}
		statement = "insert into " + childTable
		+ " (CreationDate) VALUES "
		+ "(CURRENT_TIMESTAMP);";
		try{
		 st.executeUpdate(statement);
		 rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
				+ childTable
				+ " where ID=LAST_INSERT_ID()");
		 rs.next();
		 result = rs.getInt(1);
		 
				statement = "insert into " + strTableName
						+ " (CreationDate,ID1,ID2) VALUES "
						+ "(CURRENT_TIMESTAMP," + parentID + "," + result
						+ ")";
		st.executeUpdate(statement);
		} catch (Exception e) {
		  System.err.println("DB relation table insert error: " + e.getMessage());  
		  System.exit(-1);			
		}			
		return result;
	    }
		
}
