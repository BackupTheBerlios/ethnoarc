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
package de.fhg.fokus.se.ethnoarc.ethnoMARS;

import java.util.Hashtable;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import de.fhg.fokus.se.ethnoarc.common.EADBDescription;

public class ExportDatabaseStructure {
	public  void export(File f, Hashtable  <String,EADBDescription> dbTables, String DBdescription_original, String DBdescription_english, String country){
		int mincount,prevcount,maxcount=-1;
		int wroteSomething=1;
		try{
			FileWriter fstream = new FileWriter(f.getAbsoluteFile());
			BufferedWriter out = new BufferedWriter(fstream);	
			out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			out.write("<!DOCTYPE ethnoArcDefinition SYSTEM \"http://www.ethnoarc.org/scheme/ENTITIES.DTD\" >\n");
			out.write("\n\n");
			out.write("<ethnoArcDefinition\n");
			out.write("xmlns=\"http://www.ethnoarc.org/scheme\"\n"); 
			out.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); 
			out.write("xsi:schemaLocation=\"http://www.ethnoarc.org/scheme/ethnoArcDefinition.xsd\"\n"); 
			out.write("language=\""+country+"\">\n\n");
			if(DBdescription_original.length()>0){
				out.write("<Introduction>\n");
				out.write(DBdescription_original.replaceAll("&", "&amp;").replaceAll("<", "&lt;")+"\n");
				out.write("</Introduction>\n");
			}
			if(DBdescription_english.length()>0){
				out.write("<EnglishIntroduction>\n");
				out.write(DBdescription_english.replaceAll("&", "&amp;").replaceAll("<", "&lt;")+"\n");
				out.write("</EnglishIntroduction>\n");
			}
			for (EADBDescription originalTable : dbTables.values()) {
				// find language code for this database
				if(originalTable.getType().equals("default")){
					// no longer used - read at an earlier phase
				}
				// and while we're at it, look for the maximum order number
				if(originalTable.getOrderNumber()>maxcount)maxcount=originalTable.getOrderNumber();
			}	
			maxcount++;
			// now write all objects (in ascending order) to file
			prevcount=-1;
			while (wroteSomething==1){
				wroteSomething=0;
				mincount=maxcount;
				// determine minmum object number that is higher than the previous one
				for (EADBDescription originalTable : dbTables.values()) {
					if(originalTable.getType().equals("object")){
						if((originalTable.getOrderNumber()>prevcount)&&(originalTable.getOrderNumber()<mincount))
							mincount=originalTable.getOrderNumber();
					}
				}
				prevcount=mincount;
				for (EADBDescription originalTable : dbTables.values()) {
					if((originalTable.getType().equals("object"))&&(originalTable.getOrderNumber()==mincount)){
						wroteSomething=1;
						out.write("<Object name=\""+originalTable.getName()+"\" ");	
						if(!originalTable.getDisplayname().equals(originalTable.getName())){
							out.write("displayname=\""+originalTable.getDisplayname()+"\" ");	
						}
						if(originalTable.getNoValue())
							out.write("novalue=\"true\" ");
						if(originalTable.getFormat()!=null)
							if(originalTable.getFormat().length()>0)
								out.write("format=\""+originalTable.getFormat()+"\" ");
						if(!originalTable.getIsPublicDefault())
							out.write("public=\"false\" ");
						out.write(">\n");
						if(originalTable.getDescription()!=null)
							out.write("  <Description>"+originalTable.getDescription()+"</Description>\n");
						else
							out.write("  <Description>"+"</Description>\n");
						if(originalTable.getEnglishDescription()!=null)
							out.write("  <EnglishDescription>"+originalTable.getEnglishDescription()+"</EnglishDescription>\n");
						else
							out.write("  <EnglishDescription>"+"</EnglishDescription>\n");
						/* start write relations */
						/* first check max relation count for this object */
						int rel_mincount,rel_prevcount,rel_maxcount=-1;
						for (EADBDescription relTable : dbTables.values()) {
							if( (relTable.getType().equals("Contains"))||
									(relTable.getType().equals("AlternativeLanguage"))||
									(relTable.getType().equals("Alternative"))||
									(relTable.getType().equals("TakesValueFrom"))||
									(relTable.getType().equals("Implies"))){
								if(originalTable.getNameDB().equals(relTable.getFirstTable()))
									if(relTable.getOrderNumber()>rel_maxcount)
										rel_maxcount=relTable.getOrderNumber();
							}
						}
						rel_maxcount++;
						int rel_wroteSomething=1;
						// now write all relations (in ascending order) to file
						rel_prevcount=-1;
						while (rel_wroteSomething==1){
							rel_mincount=rel_maxcount;
							// determine minmum object number that is higher than the previous one
							for (EADBDescription relTable : dbTables.values()) {
								if( (relTable.getType().equals("Contains"))||
										(relTable.getType().equals("AlternativeLanguage"))||
										(relTable.getType().equals("Alternative"))||
										(relTable.getType().equals("TakesValueFrom"))||
										(relTable.getType().equals("Implies"))){
									if(originalTable.getNameDB().equals(relTable.getFirstTable()))		
										if((relTable.getOrderNumber()>rel_prevcount)&&(relTable.getOrderNumber()<rel_mincount))
											rel_mincount=relTable.getOrderNumber();
								}
							}
							// now find relations with this  order value
							rel_prevcount=rel_mincount;
							rel_wroteSomething=0;
							for (EADBDescription relTable : dbTables.values()) {
								if( (relTable.getType().equals("Contains"))||
										(relTable.getType().equals("AlternativeLanguage"))||
										(relTable.getType().equals("Alternative"))||
										(relTable.getType().equals("TakesValueFrom"))||
										(relTable.getType().equals("Implies"))){
									if(originalTable.getNameDB().equals(relTable.getFirstTable()))			    			     			
										if(relTable.getOrderNumber()==rel_mincount){
											rel_wroteSomething=1;
											out.write("    <Relation type=\"");
											if(relTable.getType().equals("AlternativeLanguage"))
												out.write("AlternativeLanguage");
											if(relTable.getType().equals("Alternative"))
												out.write("Alternative");
											if(relTable.getType().equals("Implies"))
												out.write("Implies");
											if((relTable.getType().equals("Contains"))&&(relTable.getIsMandatory()))
												out.write("HasToContain");
											if((relTable.getType().equals("Contains"))&&(!relTable.getIsMandatory()))
												out.write("Contains");
											if((relTable.getType().equals("TakesValueFrom"))&&(relTable.getIsMandatory()))
												out.write("ExclusivelyTakesValueFrom");
											if((relTable.getType().equals("TakesValueFrom"))&&(!relTable.getIsMandatory()))
												out.write("TakesValueFrom");
											out.write("\" name=\"");
											// need to determine name of second half of relation from table name
											for (EADBDescription objectTable : dbTables.values()) {
												if(objectTable.getNameDB().equals(relTable.getSecondTable()))
													out.write(objectTable.getName());
											}								     				
											out.write("\" ");
											if(relTable.getMultiple())
												out.write("multiple=\"true\" ");
											else
												out.write("multiple=\"false\" ");

											out.write(" />\n");

										}
								}
							}			    			     		
						}

						/* end write relations for this object  */
						out.write("</Object>\n\n");
					}
				}

			}
			out.write("</ethnoArcDefinition>\n");
			out.close();
		}catch(Exception e){}
	}	  
}
