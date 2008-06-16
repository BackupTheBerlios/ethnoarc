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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.fhg.fokus.se.ethnoarc.common.DBConstants.DBElementTypes;

/**
 * This class provides attributes and methods necessary to get basic properties or description
 * of a ethnoArc db elements. An ethnoArc db element could be a table (e.g., 'Adresse'), a table element (e.g., 'Stadt') or 
 * an element describing the relationship between two db elements (e.g, 'adresse_Contains_Stadt'). <p> 
 * <b>Note:</b> Essencially, all above mentioned elements are specified as separate tables in the actual ethnoArc database. 
 * But for ease of understanding the database strucutre, ethnoArc db elements are parsed to create a strucuture as that of a conventional database
 * (tables & elements) and named accordingly. 
 * <p>
 * $Id: EADBDescription.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * @author fokus
 */
public class EADBDescription implements Serializable{

	/** The ID of the DB element. */
	private int ID;
	/** 
	 * The type of the DB element. <p>
	 * 
	 * <code>object</code>, <code>contains</code>, <code>TakesValueFrom</code>, 
	 * <code>Implies</code>, <code>Alternative</code>, or <code>AlternativeLanguage</code>. 
	 */
	private String Type="";
	
	/**
	 * The element type of the DB element.
	 * @see DBElementTypes
	 */
	private DBElementTypes DbElementType=DBElementTypes.NonSpecified;
	/** The name of the element. */
	private String Name="";
	/**
	 * The name used to store this element in the database. Each element is 
	 * stored as a table in the db. This is actually a table name. 
	 */
	private String nameDB="";
	/**
	 * The name to be displayed.
	 */
	private String Displayname="";
	/** Standard description of the db element. */
	private String Description="";
	/**  Description o fhte db element in english. */
	private String EnglishDescription="";
	/** The format of values of the db element. */
	private String Format="";
	
	private boolean NoValue=false;
	private boolean Multiple=false;
	private int OrderNumber=0;
	private String FirstTable="";
	private String SecondTable="";
	private boolean IsMandatory=false;
	/**
	 * Specifies the first content if present of this element.
	 */
	private String firstContent="";
	/**
	 * Indicates if the value of the table and its child elements are public
	 * by default. If <code>true</code> then its values are made public. Otherwise,
	 * its values, including value of its child elements are private and are not
	 * visible for search.
	 */
	private boolean IsPublicDefault=false;
	private List<String> parentStructure =new ArrayList <String>();
	
	/**
	 * Specifies if the relatedTable refers to one or more other tables.
	 * A table may refer to other tables with following type definitions:
	 *  <li><code>TakesValueFrom</code>
	 *  <li><code>Alternative</code>
	 *  <li><code>AlternativeLanguage</code> and
	 *  <li><code>Implies</code>. <br>
	 *  <code>true</code>: The relatedTable refers to one or more other tables. <br>
	 *  <code>false</code>: The relatedTable does NOT refer other tables.
	 */
	private boolean hasReferenceTable=false;
	
	/**
	 * List of all referred table.
	 */
	private List<TableReference> referedTableList = new ArrayList<TableReference>();
	
	/**
	 * Specifies if other elements implies the value of this element.
	 */
	private boolean isImpliedBy=false;
	
	
	//	SET Methods
	public void setID(int ID){
		this.ID = ID;
	}
	public void setNoValue(int NoValue){
		if(NoValue==0){
			this.NoValue = false;
		}else{
			this.NoValue = true;
		}
	}	
	public void setMultiple(int Multiple){
		if(Multiple==0){
			this.Multiple = false;
		}else{
			this.Multiple = true;
		}
	}	
	public void setOrderNumber(int OrderNumber){
		this.OrderNumber = OrderNumber;
	}
	public void setType(String Type){
		this.Type = Type;
	}
	/**
	 * Sets the db element type of the database element.
	 * @param dbElementType The db element type of this db element.
	 * @see DBConstants.DBElementTypes
	 */
	public void setDBElementType(DBConstants.DBElementTypes dbElementType)
	{
		this.DbElementType=dbElementType;
	}
	public void setName(String Name){
		this.Name = Name;
	}	
	public void setNameDB(String Tablename){
		this.nameDB = Tablename;
	}
	public void setDisplayname(String Displayname){
		this.Displayname = Displayname;
	}
	public void setDescription(String Description){
		this.Description = Description;
	}	
	public void setEnglishDescription(String EnglishDescription){
		this.EnglishDescription = EnglishDescription;
	}
	public void setFormat(String format){
		this.Format= format;
	}
	public void setFirstTable(String FirstTable){
		this.FirstTable = FirstTable;
	}	
	public void setSecondTable(String SecondTable){
		this.SecondTable = SecondTable;
	}
	public void setIsMandatory(Boolean isMandatory)
	{
		this.IsMandatory=isMandatory;
	}

	public void setImpliesTable(String tableName, EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.Implies,table));
		//System.out.println(nameDB+"--- "+tableName);
		table.setImpliedByTable(tableName, this);
	
	}
	public void setImpliedByTable(String tableName, EADBDescription table)
	{
		if(!isImpliedBy)
			isImpliedBy=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.ImpliedBy,table));
	}
	public boolean isImpliedBy()
	{
		return isImpliedBy;
	}

	public void setTakesValueFromTable(EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.TakesValueFrom,table));
	}
	public void setExclusiveTakesValueFromTable(EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.ExclusiveTakesValueFrom,table));
	}
	
	public void setAlternativeTable(EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.Alternative,table));
	}
	
	public void setTakesReferenceFromTable(EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.TakesReferenceFrom,table));
	}
		
	public void setAlternativeLanguageTable(EADBDescription table){
		if(!hasReferenceTable)
			hasReferenceTable=true;
		referedTableList.add(new TableReference(DBConstants.TableReferenceTypes.AlternativeLanguage,table));
	}
	
	//Geneva Meeting additions
	public void setPublicDefault(Boolean PuplicDefault){
		this.IsPublicDefault=PuplicDefault;
	}	

	/**
	 * Sets the parent structure of the element.
	 * @param parentStructure The parent structure.E.g., <code>Adressenliste.Adresse</code>.
	 */
	public void addParentStructure(String parentStructure)
	{
		this.parentStructure.add(parentStructure);
	}

	//GET Methods
	
	public String toString()
	{
		return Displayname+":\r\n"+
			"\tnameDB: "+nameDB+"\r\n"+
			"\tparentStructure: "+parentStructure;
	}
	public int getID(){
		return this.ID;
	}
	public boolean getNoValue(){
		return this.NoValue;
	}
	public boolean getMultiple(){
		return this.Multiple;
	}	
	public int getOrderNumber(){
		return this.OrderNumber;
	}
	public String getType(){
		return this.Type;
	}	
	public String getName(){
		return this.Name;
	}	
	/**
	 * Gets the db element type of this db element.
	 * @return The DB element type.
	 * @see DBConstants.DBElementTypes
	 */
	public DBElementTypes getDBElementType()
	{
		/*if(this.DbElementType==null)
		{
			if(getParentStructure().equals(getNameDB()))
				return DBElementTypes.Table;
			else
				return DBElementTypes.Others;
		}*/
		return this.DbElementType;
	}
	public String getNameDB(){
		return this.nameDB;
	}
	public String getDisplayname(){
		return this.Displayname;
	}
	public String getDescription(){
		return this.Description;
	}	
	public String getEnglishDescription(){
		return this.EnglishDescription;
	}
	public String getFormat(){
		return this.Format;
	}
	public String getFirstTable(){
		return this.FirstTable;
	}	
	public String getSecondTable(){
		return this.SecondTable;
	}	

	public Boolean getIsMandatory()
	{
		return IsMandatory;
	}
	
	//Geneva Meeting addition
	public Boolean getIsPublicDefault()
	{
		return IsPublicDefault;
	}
	/**
	 * Gets boolean flag if the table has atleast one referenced table.
	 * @return <code>true</code>: The table has atleast one referenced table.<br>
	 * <code>false</code>: The table has NO referenced table.
	 */
	public Boolean hasReferenceTable()
	{
		return hasReferenceTable;
	}
	/**
	 * Returns the list of all referred tables.
	 * @return The list of all referred tables.
	 */
	public List<TableReference> getReferredTables()
	{
		return referedTableList;
	}
	/**
	 * Returns the list of all alternate langauage table.
	 * @return List of alternate language tables. Empty table if 
	 * the table does not contain any alternate language tables.
	 */
	public List<EADBDescription> getAlternateLanguageTables()
	{
		List<EADBDescription> alLangTables=new ArrayList<EADBDescription>();
		
		for (TableReference referredTable : referedTableList) {
			if(referredTable.referenceType.equals(DBConstants.TableReferenceTypes.AlternativeLanguage))
				alLangTables.add(referredTable.getReferencedTable());
		}
		
		return alLangTables;
	}
	
	public class TableReference implements Serializable
	{
		private DBConstants.TableReferenceTypes referenceType;
		private EADBDescription referencedTable;
		public TableReference(DBConstants.TableReferenceTypes referenceType, EADBDescription referencedTable)
		{
			this.referenceType=referenceType;
			this.referencedTable=referencedTable;
		}
		public DBConstants.TableReferenceTypes getReferenceType()
		{
			return referenceType;
		}
		public EADBDescription getReferencedTable()
		{
			return referencedTable;
		}
	}
	/**
	 * gets the parent structure of the element.
	 * @return The parent structure. E.g., <code>Adressenliste.Adresse</code>.
	 */
	public List<String> getParentStructure()
	{
		if(parentStructure.size()>0)
		return parentStructure;
		else
		{
			//parentStructure.add(nameDB);
			List<String> l = new ArrayList<String>();
			l.add(this.getNameDB());
			return l;
		}
	}
	 
	public void setFirstContent(String content){
		firstContent = content;
	}
	public String getFirstContent(){
		return firstContent;
	}
}
