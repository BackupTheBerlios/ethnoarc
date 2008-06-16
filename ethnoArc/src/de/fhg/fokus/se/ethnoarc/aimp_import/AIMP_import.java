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
package de.fhg.fokus.se.ethnoarc.aimp_import;

import jxl.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame;

public class AIMP_import {
	static String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_aimp";
	static String DBUSERNAME ="admin";
	static String DBPASSWORD ="adminPW";
	
	static Hashtable <String,EADBDescription> originalTables = null;
	static Hashtable tablePrimaryNames = null;
	static Hashtable tableSecondaryNames = null;
	static DBStructure dbStructure=null;
	static DBHandling dbHandle=null;	
	static String AIMP_support_item_table;
	static String BD_plage_table;
	static String AIMP_BD_relation_table;
	static Connection cn = null;
	static	Statement st = null;
	
	
	
	static Logger logger = Logger.getLogger(MainUIFrame.class.getName());
	
	public static void main(String[] args) {
		ResultSet rs = null;
		String statement = null;
		Workbook workbook_disk=null;
		Workbook workbook_tracks=null;
		// get both workbooks
		try{
			workbook_disk = Workbook.getWorkbook(new File("C:\\ethnoArc\\ExportAIMP\\HR-78rpm-AIMPdatabase.xls"));
		}catch (Exception e){System.err.println("Excel file open error: "+e);}
		try{
		    workbook_tracks = Workbook.getWorkbook(new File("C:\\ethnoArc\\ExportAIMP\\HR-78rpm-AIMPtracksDB.xls"));
		}catch (Exception e){System.err.println("Excel file open error: "+e);}

		// get database info
		try {
			dbHandle = new DBHandling(DBURL, DBUSERNAME, DBPASSWORD);
			dbStructure = dbHandle.getDBStructure();							
			originalTables = dbStructure.getTables();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// perform column mapping
		tablePrimaryNames= new Hashtable();
		Sheet sheet = workbook_disk.getSheet(0);
		MapPrimaryColumnNamesToTables(sheet);
		tableSecondaryNames= new Hashtable();
		Sheet sheet_track = workbook_tracks.getSheet(0);
		MapSecondaryColumnNamesToTables(sheet_track);
		GetMainTableNames();
		// 
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
        //		
		sheet = workbook_disk.getSheet(0);
		int AIMP_support_item_ID;
		try {
		for(int row=1;row<sheet.getRows();row++){
		//	System.err.println(sheet.getCell(0,row).getContents());
			// create AIMP_support_item entry
			statement = "insert into " + AIMP_support_item_table
			+ " (CreationDate) VALUES "
			+ "(CURRENT_TIMESTAMP);";
			st.executeUpdate(statement);			
			rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
							+ AIMP_support_item_table
							+ " where ID=LAST_INSERT_ID()");
			rs.next();
			AIMP_support_item_ID = rs.getInt(1);
			System.out.println("Processing row "+row+" of "+(sheet.getRows()-1));
			// now add information from columns
			for(int col=0;col<sheet.getColumns();col++){
				if(col!=4){
					if(sheet.getCell(col,row).getContents().length()>0){
						// insert cell value into database
						String tableName=(String)tablePrimaryNames.get(col);
						statement = "insert into " + tableName
						+ " (CreationDate,Content) VALUES "
						+ "(CURRENT_TIMESTAMP,'"
						+ MakeSQLsafe(sheet.getCell(col,row).getContents()) 
						+ "')" 
						;			  
						st.executeUpdate(statement);
						rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
								+ tableName
								+ " where ID=LAST_INSERT_ID()");
						rs.next();
						int entryID = rs.getInt(1);	
						// find table that connects this to the AIMP support item
						String relationTable=null;
					    for (EADBDescription originalTable : originalTables.values()) {
							   if((originalTable.getType().equalsIgnoreCase("Contains"))&&
							      (originalTable.getSecondTable().equalsIgnoreCase(tableName))&&
								  (originalTable.getFirstTable().equalsIgnoreCase(AIMP_support_item_table))  )
								   relationTable=originalTable.getNameDB();
					    }
					    if(relationTable==null)System.err.println("Relation table not found for "+tableName);
					    // insert data for the relation
						statement = "insert into " + relationTable
						+ " (CreationDate,ID1,ID2) VALUES "
						+ "(CURRENT_TIMESTAMP," + AIMP_support_item_ID + "," + entryID
						+ ")";
						st.executeUpdate(statement);
					}
					} // end of if(col!=4){
				} // end of for(int col=1;col<sheet.getColumns()){
						// now add BD_plage entry if we find a matching ones
						for(int track_row=1;track_row<sheet_track.getRows();track_row++){
							if(sheet_track.getCell(0, track_row).getContents().equalsIgnoreCase(sheet.getCell(4,row).getContents())){
								// we got a matching entry, so create a BD_plage entry
								statement = "insert into " + BD_plage_table
								+ " (CreationDate) VALUES "
								+ "(CURRENT_TIMESTAMP);";
								st.executeUpdate(statement);
								rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
										+ BD_plage_table
										+ " where ID=LAST_INSERT_ID()");
								rs.next();
								int BD_plage_ID = rs.getInt(1);	
								// create relation with AIMP Support item
								statement = "insert into " + AIMP_BD_relation_table
								+ " (CreationDate,ID1,ID2) VALUES "
								+ "(CURRENT_TIMESTAMP," + AIMP_support_item_ID + "," + BD_plage_ID
								+ ")";
								st.executeUpdate(statement);
							
								// now handle the columns with the values for this BD_plage
								for(int track_col=1;track_col<sheet_track.getColumns()-1;track_col++){
										if(sheet_track.getCell(track_col,track_row).getContents().length()>0){
											// insert cell value into database
											String tableBDName=(String)tableSecondaryNames.get(track_col);
											statement = "insert into " + tableBDName
											+ " (CreationDate,Content) VALUES "
											+ "(CURRENT_TIMESTAMP,'"
											+ MakeSQLsafe(sheet_track.getCell(track_col,track_row).getContents()) 
											+ "')" 
											;			  
											st.executeUpdate(statement);
											rs = st.executeQuery("SELECT LAST_INSERT_ID() FROM "
													+ tableBDName
													+ " where ID=LAST_INSERT_ID()");
											rs.next();
											int entryBD_ID = rs.getInt(1);	
											// find table that connects this to the AIMP support item
											String relationBDTable=null;
										    for (EADBDescription originalTable : originalTables.values()) {
												   if((originalTable.getType().equalsIgnoreCase("Contains"))&&
												      (originalTable.getSecondTable().equalsIgnoreCase(tableBDName))&&
													  (originalTable.getFirstTable().equalsIgnoreCase(BD_plage_table))  )
													   relationBDTable=originalTable.getNameDB();
										    }
										    if(relationBDTable==null)System.err.println("Secondary relation table not found for "+tableBDName);
										    // insert data for the relation
											statement = "insert into " + relationBDTable
											+ " (CreationDate,ID1,ID2) VALUES "
											+ "(CURRENT_TIMESTAMP," + BD_plage_ID + "," + entryBD_ID
											+ ")";
											st.executeUpdate(statement);
										}
							}
						} // end for(int track_row=1;track_row<sheet_track.getRows();track_row++){
					}					

		} // end of for(int row=1;row<sheet.getRows();row++){
		}// end of 	try {
		catch(Exception e){
			System.err.println("Data insert error "+e);		
			e.printStackTrace();
		}
		
		workbook_tracks.close();
		workbook_disk.close();

	}
	public static String MakeSQLsafe(String anyString) {
		return ((anyString.replaceAll("'", "''")).trim());
	}
	static void MapPrimaryColumnNamesToTables(Sheet sheet){
		String colName;
		//System.out.println("Number of rows: "+sheet.getRows());
		//System.out.println("Number of cols: "+sheet.getColumns());
		for(int col=0;col<sheet.getColumns();col++){
			colName=sheet.getCell(col,0).getContents();
			// replace names where needed (most of them)
			if(colName.equalsIgnoreCase("cote générale"))colName="cote_generale";
			if(colName.equalsIgnoreCase("cote générale - no item"))colName="NoTable";
			if(colName.equalsIgnoreCase("no item"))colName="no_item";
			if(colName.equalsIgnoreCase("nb item"))colName="nb_item";
			if(colName.equalsIgnoreCase("ancien numéro"))colName="ancien_numero";
			if(colName.equalsIgnoreCase("cote générale - no item"));
			if(colName.equalsIgnoreCase("correspondance DAT"))colName="correspondance_DAT";
			if(colName.equalsIgnoreCase("no matrice"))colName="no_matrice";
			if(colName.equalsIgnoreCase("région"))colName="region";
			if(colName.equalsIgnoreCase("localité"))colName="localite";
			if(colName.equalsIgnoreCase("sub-continent"))colName="sub_continent";
			if(colName.equalsIgnoreCase("autres pays"))colName="autres_pays";
			if(colName.equalsIgnoreCase("sous-titre"))colName="sous_titre";
			if(colName.equalsIgnoreCase("interprètes"))colName="interpretes";
			if(colName.equalsIgnoreCase("genre, occasion"))colName="genre_occasion";
			if(colName.equalsIgnoreCase("édition"))colName="edition";
			if(colName.equalsIgnoreCase("année")&&(col==25))colName="annee_edition";
			if(colName.equalsIgnoreCase("année")&&(col==29))colName="annee_production";
			if(colName.equalsIgnoreCase("lieu")&&(col==26))colName="lieu_edition";
			if(colName.equalsIgnoreCase("lieu")&&(col==30))colName="lieu_production";
			if(colName.equalsIgnoreCase("livret : auteur"))colName="livret_auteur";
			//System.out.println(colName);
			// try to find the name in the database structure
		    String strTableName = null;
		    for (EADBDescription originalTable : originalTables.values()) {
			   if((originalTable.getName().equalsIgnoreCase(colName)))
			   {
			     strTableName=originalTable.getNameDB();
			     }
			}			
		    if(colName.equals("NoTable")){
				// this is just a conencting table that does not appear in the database - ignore it
			}
			else if(strTableName==null){
				System.err.println("Column "+col+": No matching table found for: "+colName);
			}
			else 
				tablePrimaryNames.put(col, strTableName);
		}
	}
	static void MapSecondaryColumnNamesToTables(Sheet sheet){
		String colName;
		//System.out.println("Number of rows: "+sheet.getRows());
		//System.out.println("Number of cols: "+sheet.getColumns());
		for(int col=0;col<sheet.getColumns();col++){
			colName=sheet.getCell(col,0).getContents();
			// replace names where needed (most of them)
			if(colName.equalsIgnoreCase("nb item"))colName="ref_item";
			if(colName.equalsIgnoreCase("no de plage"))colName="no_plage";
			if(colName.equalsIgnoreCase("durée (m.ss)"))colName="duree";
			if(colName.equalsIgnoreCase("titre"))colName="titre_responsabilite";
			if(colName.equalsIgnoreCase(""))colName="NoTable";
			if(colName.equalsIgnoreCase("cote générale - no item"))colName="NoTable";
			//System.out.println(colName);
			// try to find the name in the database structure
		    String strTableName = null;
		    for (EADBDescription originalTable : originalTables.values()) {
			   if((originalTable.getName().equalsIgnoreCase(colName)))
			   {
			     strTableName=originalTable.getNameDB();
			     }
			}			
		    if(colName.equals("NoTable")){
				// this is just a conencting table that does not appear in the database - ignore it
			}
			else if(strTableName==null){
				System.err.println("Column "+col+": No matching table found for: "+colName);
			}
			else 
				tableSecondaryNames.put(col, strTableName);
		}
	}

	static void GetMainTableNames(){

		    for (EADBDescription originalTable : originalTables.values()) {
				   if((originalTable.getName().equalsIgnoreCase("BD_plage")))
					   BD_plage_table=originalTable.getNameDB();
				   if((originalTable.getName().equalsIgnoreCase("AIMP_support_item")))
					   AIMP_support_item_table=originalTable.getNameDB();
			}
		    for (EADBDescription originalTable : originalTables.values()) {
				   if((originalTable.getType().equalsIgnoreCase("Contains"))&&
							  (originalTable.getFirstTable().equalsIgnoreCase(AIMP_support_item_table))&&
							  (originalTable.getSecondTable().equalsIgnoreCase(BD_plage_table)))
					   AIMP_BD_relation_table=originalTable.getNameDB();
			}
	}
}
