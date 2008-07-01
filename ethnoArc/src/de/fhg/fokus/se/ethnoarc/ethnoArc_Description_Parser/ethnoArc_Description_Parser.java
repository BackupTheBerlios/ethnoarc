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
package de.fhg.fokus.se.ethnoarc.ethnoArc_Description_Parser;

import java.util.Set;
import java.util.HashSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import java.io.File;
import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ethnoArc_Description_Parser {

	static Connection cn = null;

	static Set Namelist = new HashSet();

	static boolean printInfo = false;

	static boolean useDB = true; 

	static int index_number=0;
	static int object_order_number=1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String sDbUrl = null, sTable = null, sUsr = "", sPwd = "", sFile = "", sDB = "ethnoArc";

		Statement st = null;
		ResultSet rs = null;

		sTable = "content";
		sUsr = "user";
		sPwd = "password";
		sDbUrl = "jdbc:mysql://127.0.0.1:3306/";
		sFile = "Enter XML file or directory with -xml option.";

		sUsr = "username";
		sPwd = "password";
		//sDbUrl = "jdbc:mysql://localhost:3306/";

		sDbUrl = "jdbc:mysql://bruce.fokus.fraunhofer.de:3306/";
		//sFile = "q:/ethnoarc/scheme/Address_Reference_Example";
		//sDB = "ethnoArc";

		sFile = "c:/ethnoarc/DemoArchiv/";
		sDB = "ethnoArc_demo";
		
		//sFile = "c:/ethnoarc/emem/";
		//sDB = "ethnoArc_emem";

		//sFile = "c:/ethnoarc/adresse2/";
		//sDB = "ethnoArc_adresse2";



		/* parse arguments */
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-user")) {
				i++;
				sUsr = new String(args[i]);
			}
			if (args[i].equalsIgnoreCase("-password")) {
				i++;
				sPwd = new String(args[i]);
			}
			if (args[i].equalsIgnoreCase("-database")) {
				i++;
				sDB = new String(args[i]);
			}
			if (args[i].equalsIgnoreCase("-db")) {
				i++;
				sDB = new String(args[i]);
			}
			if (args[i].equalsIgnoreCase("-xml")) {
				i++;
				sFile = new String(args[i]);
			}
			if (args[i].equalsIgnoreCase("-verbose")) {
				printInfo = true;
			}
			if (args[i].equalsIgnoreCase("-IP")) {
				i++;
				sDbUrl = new String("jdbc:mysql://" + args[i] + "/");
			}
			if (args[i].equalsIgnoreCase("-noDB")) {
				useDB = false;
			}
			if (args[i].equalsIgnoreCase("-help")) {
				PrintInfo();
				return;
			}
		}

		if (null != sTable && 0 < sTable.length()) {
			try {
				// Select fitting database driver and connect:
				if (useDB) {
					Class.forName("com.mysql.jdbc.Driver");
					if (printInfo)
						System.out.println("Driver loaded!");
					if (printInfo)
						System.out.println("Trying to connect to SQL.");

					try {
						cn = DriverManager.getConnection(sDbUrl, sUsr, sPwd);
						if (printInfo)
							System.out.println("Got connection to server.");
					} catch (Exception ex) {
						System.err
						.println("Can't connect to database server at "
								+ sDbUrl);
						System.exit(-1);
					}
					// If we reach this, at least we can connect to the database
					// server
					// trying to reach the ethnoArc database now...
					try {
						cn = DriverManager.getConnection(sDbUrl + sDB, sUsr,
								sPwd);
						if (printInfo)
							System.out.println("Got connection to " + sDB
									+ " database.");
					} catch (Exception ex) {
						if (printInfo)
							System.out
							.println("Can't connect to ethnoArc database "
									+ sDbUrl + sDB);
						if (printInfo)
							System.out.println("Creating database");
						st = cn.createStatement();
						st.executeUpdate("CREATE DATABASE " + sDB);
						cn = DriverManager.getConnection(sDbUrl + sDB, sUsr,
								sPwd);
						if (printInfo)
							System.out
							.println("Created connection to ethnoArc database.");
					}
					// Figure out if we got the DatabaseDescription table
					// available
					try {
						st = cn.createStatement();
						rs = st
						.executeQuery("select * from DatabaseDescription where ID = 10");
					} catch (Exception ex) {
						if (printInfo)
							System.out
							.println("Creating DatabaseDescription table");
						st
						.executeUpdate(" create table DatabaseDescription("
								+ "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
								+ "Type text NOT NULL, "
								+ "Name text NOT NULL, Tablename text NOT NULL, Displayname text ,"
								+ "Description text NOT NULL, EnglishDescription text NOT NULL,"
								+ "Format text , "
								+ "PublicDefault boolean , "
								+ "NoValue boolean , "
								+ "Multiple boolean , "
								+ "Mandatory boolean , "
								+ "OrderNumber int , "
								+ "FirstTable text, SecondTable text"
								+ " );");
					}
				}
				// Figure out if we got the _dbmappuser table
				// available
				try {
					st = cn.createStatement();
					rs = st
					.executeQuery("select * from _dbmappuser where ID = 10");
				} catch (Exception ex) {
					if (printInfo)
						System.out
						.println("Creating _dbmappuser table");
					st
					.executeUpdate(" create table _dbmappuser("
							+ "id int(10) unsigned NOT NULL auto_increment,"
							+ "username varchar(45) NOT NULL,"
							+ "pwd varchar(100) NOT NULL,"
							+ "userlevel varchar(45) NOT NULL,"
							+   "PRIMARY KEY  (id)"
							+  ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");							
				}


				File checkFile = new File(sFile);
				if (!checkFile.isDirectory()) {
					ParseXMLDescriptionFile(checkFile.getAbsolutePath(), 1);
					ParseXMLDescriptionFile(checkFile.getAbsolutePath(), 2);
					ParseXMLDescriptionFile(checkFile.getAbsolutePath(), 3);
				} else {
					ParseXMLDescriptionDirectory(checkFile.getAbsolutePath(), 1);
					ParseXMLDescriptionDirectory(checkFile.getAbsolutePath(), 2);
					ParseXMLDescriptionDirectory(checkFile.getAbsolutePath(), 3);
				}

				//	if (useDB)
				//		ShowTable("DatabaseDescription");

			} catch (Exception ex) {
				System.out.println(ex);
			} finally {
				try {
					if (null != rs)
						rs.close();
				} catch (Exception ex) {
				}
				try {
					if (null != st)
						st.close();
				} catch (Exception ex) {
				}
				try {
					if (null != cn)
						cn.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	// Extend String to length of 14 characters
	private static final String extendStringTo14(String s) {
		if (null == s)
			s = "";
		final String sFillStrWithWantLen = "              ";
		final int iWantLen = sFillStrWithWantLen.length();
		final int iActLen = s.length();
		if (iActLen < iWantLen)
			return (s + sFillStrWithWantLen).substring(0, iWantLen);
		if (iActLen > 2 * iWantLen)
			return s.substring(0, 2 * iWantLen);
		return s;
	}

	public static void ParseXMLDescriptionDirectory(String directoryName,
			int pass) {
		File directoryFile = new File(directoryName);
		String[] nameList = directoryFile.list();
		for (int i = 0; i < nameList.length; i++) {
			File singleFile = new File(directoryName + '/' + nameList[i]);
			if (singleFile.isDirectory())
				ParseXMLDescriptionDirectory(singleFile.getAbsolutePath(), pass);
			else if (singleFile.getAbsolutePath().toLowerCase()
					.endsWith(".xml"))			
				ParseXMLDescriptionFile(singleFile.getAbsolutePath(), pass);
		}
	}

	public static void ShowTable(String tableName) {

		Statement st = null;
		ResultSet rs = null;
		try {
			st = cn.createStatement();
			rs = st.executeQuery("select * from " + tableName);
			// Get meta data:
			ResultSetMetaData rsmd = rs.getMetaData();
			int i, n = rsmd.getColumnCount();
			// Print table content:
			for (i = 0; i < n; i++)
				System.out.print("+---------------");
			System.out.println("+");
			for (i = 1; i <= n; i++)
				// Attention: first column with 1 instead of 0
				System.out
				.print("| " + extendStringTo14(rsmd.getColumnName(i)));
			System.out.println("|");
			for (i = 0; i < n; i++)
				System.out.print("+---------------");
			System.out.println("+");
			while (rs.next()) {
				for (i = 1; i <= n; i++)
					// Attention: first column with 1 instead of 0
					System.out.print("| " + extendStringTo14(rs.getString(i)));
				System.out.println("|");
			}
			for (i = 0; i < n; i++)
				System.out.print("+---------------");
			System.out.println("+");
		} catch (Exception ex) {
			System.out.println(ex);
		} finally {
			try {
				if (null != rs)
					rs.close();
			} catch (Exception ex) {
			}
			try {
				if (null != st)
					st.close();
			} catch (Exception ex) {
			}
		}
	}

	public static void PrepareDatabase() {
	}

	public static void ParseXMLDescriptionFile(String xmlName, int pass) {
		if (printInfo)
			System.out.println("XML Name is " + xmlName);
		DOMParser parser = new DOMParser();
		Document doc = null;
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		try {
			parser.parse("file:/"+xmlName);		
			doc = parser.getDocument();		
		} catch (IOException ioe) {
			System.err.println("Error: " + ioe.getMessage());
		} catch (SAXException saxe) {
			System.err.println("Error: " + saxe.getMessage());
		}
		// get root layout
		NodeList elements = doc.getElementsByTagName("ethnoArcDefinition");
		NodeList children;
		for (int i = 0; i < elements.getLength(); i++) {
			children = elements.item(i).getChildNodes();
			NamedNodeMap nnm = elements.item(i).getAttributes();
			for (int i1 = 0; i1 < nnm.getLength(); i1++) {
				if (nnm.item(i1).getNodeName().equals("language")) {
					// check whether we already have the default language set
					if (useDB) {
						try {
							st = cn.createStatement();
							rs = st
							.executeQuery("select * from DatabaseDescription where name = 'language' and type='default'");
							if (!rs.next()) {
								statement = "insert into DatabaseDescription (Type,Name,Description,Tablename,EnglishDescription) VALUES "
									+ "('default','language','"
									+ MakeSQLsafe(nnm.item(i1)
											.getNodeValue()) + "','','')";
								st.executeUpdate(statement);
							} else {
								statement = "Update DatabaseDescription set Description='"
									+ MakeSQLsafe(nnm.item(i1)
											.getNodeValue())
											+ "' where name = 'language' and type='default'";
								st.executeUpdate(statement);
							}
						} catch (Exception ex) {
							System.err
							.println("Error on searching language in DatabaseDescription table");
						}
					}
				}
			}
			children = elements.item(i).getChildNodes();
			for (int i1 = 0; i1 < children.getLength(); i1++) {
				// System.out.println("Children name is " +
				// children.item(i1).getNodeName());
				if (children.item(i1).getNodeName().equals("Object")) {
					ProcessObjectNode(children.item(i1), pass);
				} else if (children.item(i1).getNodeName().equals("Value")) {
					if (pass == 3)
						ProcessValueNode(children.item(i1));
				} else if (children.item(i1).getNodeName().equals("Introduction")) {
					if (pass == 1)
						ProcessIntroductionNode(children.item(i1),0);
				} else if (children.item(i1).getNodeName().equals("EnglishIntroduction")) {
					if (pass == 1)
						ProcessIntroductionNode(children.item(i1),1);
				} else if (children.item(i1).getNodeName().equals("#text")) {
					; // no need to process this
				} else if (children.item(i1).getNodeName().equals("#comment")) {
					; // no need to process this
				} else
					System.err.println("Unexpected XML child name is "
							+ children.item(i1).getNodeName());
			}
		}
	}

	public static void ProcessObjectNode(Node objectNode, int pass) {

		NamedNodeMap nnmc = objectNode.getAttributes();
		String objectName = "";
		String objectTableName;
		String objectDisplayName="";
		String objectDescription = "";
		String objectFormat = "";
		String objectEnglishDescription = "";
		String noValue = "false";
		String publicDefault= "true";

		// get name and novalue information
		if (nnmc != null)
			for (int i2 = 0; i2 < nnmc.getLength(); i2++) {
				if (nnmc.item(i2).getNodeName().equals("name")) {
					objectName = new String(nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("format")) {
					objectFormat = new String(nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("displayname")) {
					objectDisplayName = new String(nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("novalue")) {
					if (nnmc.item(i2).getNodeValue().equals("true"))
						noValue = "true";
				} else if (nnmc.item(i2).getNodeName().equals("public")) {
					if (nnmc.item(i2).getNodeValue().equals("false"))
						publicDefault = "false";
				} else
					System.err.println("Unexpected attribute for object "
							+ objectName + " found: "
							+ nnmc.item(i2).getNodeValue());
			}
		if (Namelist.contains(objectName) && (pass == 1)) {
			System.err.println("Object name '" + objectName
					+ "' has already been used - ignoring second occurence.");
			return;
		}
		Namelist.add(objectName);

		// parse children for descriptions and relations
		NodeList children = objectNode.getChildNodes();
		int RelationOrderNumber = 1;
		for (int i2 = 0; i2 < children.getLength(); i2++) {
			if (children.item(i2).getNodeName().equals("Description")) {
				objectDescription = new String(children.item(i2)
						.getTextContent());
			} else if (children.item(i2).getNodeName().equals(
			"EnglishDescription")) {
				objectEnglishDescription = new String(children.item(i2)
						.getTextContent());
			} else if (children.item(i2).getNodeName().equals("#text")) {
				; // we can ignore this
			} else if (children.item(i2).getNodeName().equals("#comment")) {
				; // no need to process this
			} else if (children.item(i2).getNodeName().equals("Relation")) {
				if (pass == 2) {
					// only process relation info on second pass
					ProcessRelationNode(children.item(i2), objectName,
							RelationOrderNumber++);
				}
			} else
				System.err.println("Unexpected child for object " + objectName
						+ " found: " + children.item(i2).getNodeName());
		}
		objectTableName = new String(CreateTableName(objectName));
		
		// whitespace handling for descriptions
		objectDescription=objectDescription.replace('\t', ' ');
		objectDescription=objectDescription.replace('\r', ' ');
		objectDescription=objectDescription.replace('\n', ' ');
		objectEnglishDescription=objectEnglishDescription.replace('\t', ' ');
		objectEnglishDescription=objectEnglishDescription.replace('\r', ' ');
		objectEnglishDescription=objectEnglishDescription.replace('\n', ' ');
		while (objectDescription.contains("  ")) 
			objectDescription=objectDescription.replace("  ", " ");
		while (objectEnglishDescription.contains("  ")) 
			objectEnglishDescription=objectEnglishDescription.replace("  ", " ");		
		if (printInfo) {
			System.out.println("Object name is " + objectName);
			System.out.println("  Table name is " + objectTableName);
			System.out.println("  Description " + objectDescription);
			System.out.println("  EDescription " + objectEnglishDescription);
			System.out.println("  NoValue is " + noValue);
		}

		if(objectDisplayName.length()==0)objectDisplayName=new String(objectName);
		// does this entry already exist?
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		if (useDB) {
			try {
				st = cn.createStatement();
				rs = st
				.executeQuery("select * from DatabaseDescription where name = '"
						+ MakeSQLsafe(objectName) + "'");
				// insert entry if not already available
				if (!rs.next()) {
					statement = "insert into DatabaseDescription (Type,Name,Tablename,Displayname,Description,EnglishDescription,Format,PublicDefault,NoValue,OrderNumber) VALUES "
						+ "('object','"
						+ MakeSQLsafe(objectName)
						+ "','"
						+ objectTableName
						+ "','"
						+ objectDisplayName
						+ "','"
						+ MakeSQLsafe(objectDescription)
						+ "','"
						+ MakeSQLsafe(objectEnglishDescription)
						+ "','"
						+ MakeSQLsafe(objectFormat) + "'," +publicDefault +","+ noValue +","+(object_order_number++) + ")";
					st.executeUpdate(statement);			
					// and create appropriate table for actual content
					st.executeUpdate(" create table " + objectTableName + " ("
							+ "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
							+ "CreationDate datetime NOT NULL, "
							+ "Public boolean NOT NULL DEFAULT '1', "
							+ "Content text " + " );");
					st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
							" on " +objectTableName + " (ID);");
					st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
							" on " +objectTableName + " (Content(10));");
					st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
							" on " +objectTableName + " (Public);");
				}
				// update entry if already available
				else {
					statement = "update DatabaseDescription set "
						+ "Type='object',Tablename='" + objectTableName
						+ "',Description='"
						+ MakeSQLsafe(objectDescription)
						+ "',Displayname='"
						+ MakeSQLsafe(objectDisplayName)
						+ "',EnglishDescription='"
						+ MakeSQLsafe(objectEnglishDescription)
						+ "',Format='" + MakeSQLsafe(objectFormat)
						+ "',NoValue=" + noValue + ",PublicDefault="+publicDefault+ " where Name='"
						+ MakeSQLsafe(objectName) + "'";							
					st.executeUpdate(statement);
				}
			} catch (Exception ex) {
				System.err
				.println("Error on searching for DatabaseDescription table entry "
						+ objectTableName);
			}
		}

	}

	public static String MakeSQLsafe(String anyString) {
		return ((anyString.replaceAll("'", "''")).trim());
	}

	public static String CreateTableName(String objectName) {
		String TableName = "";
		for (int i = 0; i < objectName.length(); i++) {
			if ((objectName.charAt(i) >= '0') && (objectName.charAt(i) <= '9'))
				TableName = TableName + objectName.charAt(i);
			else if ((objectName.charAt(i) >= 'A')
					&& (objectName.charAt(i) <= 'Z'))
				TableName = TableName + objectName.charAt(i);
			else if ((objectName.charAt(i) >= 'a')
					&& (objectName.charAt(i) <= 'z'))
				TableName = TableName + objectName.charAt(i);
			else {
				// everything else is added as three digit code
				int ccon = (int) objectName.charAt(i);
				TableName = TableName + "_" + ccon / 100;
				ccon = ccon - (ccon / 100) * 100;
				TableName = TableName + ccon / 10;
				ccon = ccon - (ccon / 10) * 10;
				TableName = TableName + ccon;
			}
		}	
		if(TableName.length()>60){
			// SQL handles table names only up to a length of 64 characters,
			// so we shorten long names 
			int nCharSum=0;
			for (int i = 0; i < TableName.length(); i++) 
				nCharSum= nCharSum+(int) TableName.charAt(i);
			TableName=TableName.substring(0,58)+"_"+nCharSum;		 
		}
		return TableName;
	}

	public static void ProcessRelationNode(Node relationNode,
			String parentName, int OrderNumber) {
		NamedNodeMap nnmc = relationNode.getAttributes();
		String relationName = "";
		String relationDestName = "";
		String relationTableName = "";
		String relationType = "";
		String relationMultiple = "true";
		String relationMandatory = "false";
		String relationDescription = "";
		String relationEnglishDescription = "";

		// get name, type and multipleinformation
		if (nnmc != null)
			for (int i2 = 0; i2 < nnmc.getLength(); i2++) {
				if (nnmc.item(i2).getNodeName().equals("name")) {
					relationDestName = new String(nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("type")) {
					if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"CONTAINS"))
						relationType = "Contains";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"HASTOCONTAIN")){
						relationType = "Contains";
						relationMandatory = "true";
					}
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"ALTERNATIVE"))
						relationType = "Alternative";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"ALTERNATIVELANGUAGE"))
						relationType = "AlternativeLanguage";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"TAKESVALUEFROM"))
						relationType = "TakesValueFrom";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"TAKESREFERENCEFROM"))
						relationType = "TakesReferenceFrom";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"EXCLUSIVELYTAKESVALUEFROM")){
						relationType = "TakesValueFrom";
						relationMandatory = "true";
					}
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"IMPLIES"))
						relationType = "Implies";
					else
						System.err
						.println("Unexpected type for relation in object "
								+ parentName
								+ ":"
								+ nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("multiple")) {
					if (nnmc.item(i2).getNodeValue().equals("false"))
						relationMultiple = "false";
				} else
					System.err.println("Unexpected attribute for relation "
							+ relationName + " in object " + parentName + ":"
							+ nnmc.item(i2).getNodeValue());
			}

		relationName = parentName + "_" + relationType + "_" + relationDestName;

		if (!Namelist.contains(relationDestName)) {
			System.err.println("Object '" + parentName + "' has the relation '"
					+ relationType + "' with object '" + relationDestName
					+ "', which is not defined.");
			return;
		}

		if (Namelist.contains(relationName)) {
			System.err.println("Relation name '" + relationName
					+ "' has already been used - ignoring second occurence.");
			return;
		}
		Namelist.add(relationName);

		// parse children for descriptions
		NodeList children = relationNode.getChildNodes();
		for (int i2 = 0; i2 < children.getLength(); i2++) {
			if (children.item(i2).getNodeName().equals("description")) {
				relationDescription = new String(children.item(i2)
						.getTextContent());
			} else if (children.item(i2).getNodeName().equals(
			"EnglishDescription")) {
				relationEnglishDescription = new String(children.item(i2)
						.getTextContent());
			} else if (children.item(i2).getNodeName().equals("#text")) {
				; // we can ignore this
			} else
				System.err.println("Unexpected child for relation "
						+ relationName + " found: "
						+ children.item(i2).getNodeName());
		}

		relationTableName = new String(CreateTableName(parentName) + "_"
				+ relationType + "_" + CreateTableName(relationDestName));

		if(relationTableName.length()>60){
			// SQL handles table names only up to a length of 64 characters,
			// so we shorten long names 
			int nCharSum=0;
			for (int i = 0; i < relationTableName.length(); i++) 
				nCharSum= nCharSum+(int) relationTableName.charAt(i);
			relationTableName=relationTableName.substring(0,58)+"_"+nCharSum;		 
		}

		if (printInfo) {
			System.out.println("Relation name is " + relationName);
			System.out.println("  Table name is " + relationTableName);
			System.out.println("  Type name is " + relationType);
			System.out.println("  Description " + relationDescription);
			System.out.println("  EDescription " + relationEnglishDescription);
			System.out.println("  Multiple is " + relationMultiple);
			System.out.println("  Mandatory is " + relationMandatory);
		}

		if (relationType.equals("Implies")) {
			// special case for implies, which does not have its own associated
			// table
			relationTableName = "";
		}
		// does this entry already exist?
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		if (useDB) {
			try {
				st = cn.createStatement();		
				rs = st
				.executeQuery("select * from DatabaseDescription where name = '"
						+ MakeSQLsafe(relationName) + "'");
				// insert entry if not already available
				if (!rs.next()) {
					statement = "insert into DatabaseDescription (Type,Name,Tablename,Description,EnglishDescription,Multiple,Mandatory,OrderNumber,FirstTable,SecondTable) VALUES "
						+ "('"
						+ relationType
						+ "','"
						+ MakeSQLsafe(relationName)
						+ "','"
						+ relationTableName
						+ "','"
						+ MakeSQLsafe(relationDescription)
						+ "','"
						+ MakeSQLsafe(relationEnglishDescription)
						+ "',"
						+ relationMultiple
						+ ","
						+ relationMandatory
						+ ","
						+ OrderNumber
						+ ",'"
						+ CreateTableName(parentName)
						+ "','"
						+ CreateTableName(relationDestName) + "')";
					st.executeUpdate(statement);

					// and create appropriate table for actual content
					if (!relationTableName.equals("")) {
						st.executeUpdate(" create table " + relationTableName
								+ " ("
								+ "ID int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
								+ "CreationDate datetime NOT NULL, "
								+ "ID1 int NOT NULL, ID2 int NOT NULL );");
						st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
								" on " +relationTableName + " (ID);");
						st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
								" on " +relationTableName + " (ID1);");
						st.executeUpdate(" create index ethnoArc_Index_" + index_number++ +
								" on " +relationTableName + " (ID2);");

					}
				}
				// update entry if already available
				else {
					statement = "update DatabaseDescription set " + "Type='"
					+ relationType + "',Tablename='"
					+ relationTableName + "',Description='"
					+ MakeSQLsafe(relationDescription)
					+ "',EnglishDescription='"
					+ MakeSQLsafe(relationEnglishDescription)
					+ "',Multiple=" + relationMultiple
					+ ",Mandatory=" + relationMandatory
					+ ",OrderNumber=" + OrderNumber + ",FirstTable='"
					+ CreateTableName(parentName) + "',SecondTable='"
					+ CreateTableName(relationDestName) + "'"
					+ " where Name='" + MakeSQLsafe(relationName) + "'";							
					st.executeUpdate(statement);
				}
			} catch (Exception ex) {
				System.err
				.println("Error "+ex.toString()+" on searching for DatabaseDescription table entry "
						+ relationTableName);
			}
		}

	}

	public static void ProcessValueNode(Node valueNode) {
		NamedNodeMap nnmc = valueNode.getAttributes();
		String valueDestName = "";
		String valueDestTableName = "";
		String valueContent = "";
		int valueID = -1;
		// get name, type and multipleinformation
		if (nnmc != null)
			for (int i2 = 0; i2 < nnmc.getLength(); i2++) {
				if (nnmc.item(i2).getNodeName().equals("name")) {
					valueDestName = new String(nnmc.item(i2).getNodeValue());
				} else
					System.err
					.println("Unexpected attribute for value statement "
							+ valueDestName
							+ " found: "
							+ nnmc.item(i2).getNodeName());
			}

		// parse children for text content
		NodeList children = valueNode.getChildNodes();
		for (int i2 = 0; i2 < children.getLength(); i2++)
			if ((children.item(i2).getNodeName().equals("#text"))
					& (valueContent.equals("")))
				valueContent = new String(children.item(i2).getTextContent());

		valueContent = new String(valueContent.trim());

		valueDestTableName = new String(CreateTableName(valueDestName));
		if (printInfo) {
			System.out.println("Value for " + valueDestName);
			System.out.println("  Table name is " + valueDestTableName);
			System.out.println("  Content is " + valueContent);
		}

		if (!Namelist.contains(valueDestName)) {
			System.err.println("  Value statement found for object name "
					+ valueDestName + " which has not been defined.");
			return;
		}

		// add content to table if not already there
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		if (useDB) {
			try {
				st = cn.createStatement();
				rs = st.executeQuery("select * from " + valueDestTableName
						+ " where content = '" + MakeSQLsafe(valueContent)
						+ "'");
				// insert entry if not already available
				if (!rs.next()) {
					statement = "insert into " + valueDestTableName
					+ " (CreationDate,Content) VALUES "
					+ "(CURRENT_TIMESTAMP,'"
					+ MakeSQLsafe(valueContent) + "')";
					st.executeUpdate(statement);
					// read newly created object ID
					rs = st
					.executeQuery("SELECT LAST_INSERT_ID() FROM "
							+ valueDestTableName
							+ " where ID=LAST_INSERT_ID()");
					rs.next();
					valueID = rs.getInt(1);
				} else {
					;// System.out.println("Value was already stored - do
					// nothing.");
				}
			} catch (Exception ex) {
				System.err.println("Error on searching for "
						+ valueDestTableName + " table entry " + valueContent);
			}
		}
		if (!useDB)
			valueID = 1;

		// parse children for relation values
		for (int i2 = 0; i2 < children.getLength(); i2++) {
			if (children.item(i2).getNodeName().equals("RelationValue")) {
				// handle relation value
				if (valueID != -1)
					ProcessRelationValueNode(children.item(i2), valueDestName,
							valueDestTableName, valueID);
			} else if (children.item(i2).getNodeName().equals("#text")) {
				; // we can ignore this
			} else
				System.err.println("Unexpected child for value "
						+ valueDestName + " found: "
						+ children.item(i2).getTextContent());
		}
	}

	public static void ProcessRelationValueNode(Node relationvalueNode,
			String objectName, String tableName1, int ID1) {
		NamedNodeMap nnmc = relationvalueNode.getAttributes();
		String relationType = "";
		String relationTableName = "";
		String relationName = "";
		String valueName = "";
		String valueContent = "";
		String valueTableName = "";
		int valueID2 = -1;

		// get name of relation
		if (nnmc != null)
			for (int i2 = 0; i2 < nnmc.getLength(); i2++) {
				if (nnmc.item(i2).getNodeName().equals("name")) {
					valueName = new String(nnmc.item(i2).getNodeValue());
				} else if (nnmc.item(i2).getNodeName().equals("type")) {
					if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"CONTAINS"))
						relationType = "Contains";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"HASTOCONTAIN"))
						relationType = "Contains";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"ALTERNATIVE"))
						relationType = "Alternative";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"ALTERNATIVELANGUAGE"))
						relationType = "AlternativeLanguage";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"TAKESVALUEFROM"))
						relationType = "TakesValueFrom";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"EXCLUSIVELYTAKESVALUEFROM"))
						relationType = "TakesValueFrom";
					else if (nnmc.item(i2).getNodeValue().toUpperCase().equals(
					"IMPLIES"))
						relationType = "Implies";
					else
						System.err
						.println("Unexpected type for relation in relation value statement "
								+ valueName
								+ ":"
								+ nnmc.item(i2).getNodeValue());
				} else
					System.err
					.println("Unexpected attribute for relationvalue statement "
							+ relationType
							+ "/"
							+ valueName
							+ " found: " + nnmc.item(i2).getNodeName());
			}

		valueContent = new String(relationvalueNode.getTextContent());
		valueTableName = new String(CreateTableName(valueName));
		relationTableName = new String(tableName1 + "_" + relationType + "_"
				+ valueTableName);
		relationName = new String(objectName + "_" + relationType + "_"
				+ valueName);

		if(relationTableName.length()>60){
			// SQL handles table names only up to a length of 64 characters,
			// so we shorten long names 
			int nCharSum=0;
			for (int i = 0; i < relationTableName.length(); i++) 
				nCharSum= nCharSum+(int) relationTableName.charAt(i);
			relationTableName=relationTableName.substring(0,58)+"_"+nCharSum;		 
		}

		if (!Namelist.contains(relationName)) {
			System.err
			.println("RelationValue found for object '"
					+ objectName
					+ "' and relation type '"
					+ relationType
					+ "' that references object '"
					+ valueName
					+ "'.\nThe corresponding relation does not exist for that object.");
			return;
		}

		// add content to table if not already there
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		if (useDB) {
			try {
				st = cn.createStatement();
				rs = st.executeQuery("select * from " + valueTableName
						+ " where content = '" + MakeSQLsafe(valueContent)
						+ "'");
				// insert entry if not already available
				if (!rs.next()) {
					statement = "insert into " + valueTableName
					+ " (CreationDate,Content) VALUES "
					+ "(CURRENT_TIMESTAMP,'"
					+ MakeSQLsafe(valueContent) + "')";
					st.executeUpdate(statement);
					// read newly created object ID
					rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
							+ valueTableName + " where ID=LAST_INSERT_ID()");
					rs.next();
					valueID2 = rs.getInt(1);
				} else {
					// value was already there, read it
					valueID2 = rs.getInt(1);
				}
			} catch (Exception ex) {
				System.err.println("Error on searching for " + valueTableName
						+ " table entry " + valueContent);
			}
		}
		// now add IDs of both objects to relation table, if not already there
		if (useDB) {
			if ((ID1 != -1) && (valueID2 != -1)) {
				try {
					st = cn.createStatement();
					rs = st.executeQuery("select * from " + relationTableName
							+ " where ID1=" + ID1 + " and ID2=" + valueID2);
					// insert entry if not already available
					if (!rs.next()) {
						statement = "insert into " + relationTableName
						+ " (CreationDate,ID1,ID2) VALUES "
						+ "(CURRENT_TIMESTAMP," + ID1 + "," + valueID2
						+ ")";
						st.executeUpdate(statement);
					}
				} catch (Exception ex) {
					System.err.println("Error on inserting relation value for "
							+ relationType + " relation and " + valueName
							+ " with value " + valueContent);
				}
			}
		}

	}

	public static void ProcessIntroductionNode(Node objectNode, int lang) {
		Statement st = null;
		ResultSet rs = null;
		String statement = null;
		if(objectNode.getTextContent()==null)return;
		//System.err.println(objectNode.getTextContent());
		// check whether we already have the introduction set
		if (useDB) {
			try {
				st = cn.createStatement();
				if(lang==0)
					rs = st.executeQuery("select * from DatabaseDescription where name = 'introduction' and type='default'");
				else 
					rs = st.executeQuery("select * from DatabaseDescription where name = 'englishintroduction' and type='default'");
				if (!rs.next()) {
					if(lang==0)
					  statement = "insert into DatabaseDescription (Type,Name,Description,Tablename,EnglishDescription) VALUES "
						+ "('default','introduction','"+ MakeSQLsafe(objectNode.getTextContent()) + "','','')";
					else
					  statement = "insert into DatabaseDescription (Type,Name,Description,Tablename,EnglishDescription) VALUES "
						+ "('default','englishintroduction','"+ MakeSQLsafe(objectNode.getTextContent()) + "','','')";
					st.executeUpdate(statement);
				} else {
					if(lang==0)
					statement = "Update DatabaseDescription set Description='"
						+ MakeSQLsafe(objectNode.getTextContent())
								+ "' where name = 'introduction' and type='default'";
					else
						statement = "Update DatabaseDescription set Description='"
							+ MakeSQLsafe(objectNode.getTextContent())
									+ "' where name = 'englishintroduction' and type='default'";
						
					st.executeUpdate(statement);
				}
			} catch (Exception ex) {
				System.err
				.println("Error on searching language in DatabaseDescription table");
			}
		}
	}


	public static void PrintInfo() {
		System.out.println("XML parser and database creator for ethnoArc");
		System.out.println("Possible parameters:");
		System.out.println(" -help              Print this information.");
		System.out
		.println(" -xml <source>      Specify single XML file to read as input or");
		System.out
		.println("                    directory name (reads all XML files in that directory.");
		System.out
		.println(" -user <username>   Specify user for SQL database.");
		System.out
		.println(" -password <pw>     Specify password for SQL database.");
		System.out
		.println(" -database <dbname> Specify name for database to be created (default: ethnoArc).");
		System.out
		.println(" -db <dbname>       Specify name for database to be created (same as -database).");
		System.out
		.println(" -IP <ip>           Specify IP address and port of SQL server.");
		System.out
		.println("                    Example: 192.168.1.2:3306 or server.ethnoarc.org");
		System.out
		.println(" -verbose           Print more information to stdout.");
		System.out
		.println(" -noDB              Parse XML, but do not create or modify database.");
	}

}
