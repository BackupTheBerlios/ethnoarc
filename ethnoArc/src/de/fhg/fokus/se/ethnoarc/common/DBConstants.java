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
/**
 * $Id: DBConstants.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus
 */
public class DBConstants {
	/** The name of the database table where the description of the database is located. */
	public static final String MASTERTABLE = "databasedescription";
	/** The database driver */
	public static final String DBDRIVER = "com.mysql.jdbc.Driver";
	/** The URL of the database to use. */
	public static final String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_test";

	/** The username of the database. */
	public static final String DBUSERNAME = "admin";
	/** The password to access the database. */
	public static final String DBPASSWORD = "adminPW";
	
	public static final String SQL_SHOW_FIELDS = "SHOW FIELDS FROM ";
	public static final String SQL_SHOW_TABLES = "SHOW TABLES";
	/**  */
	public static final String SQL_TIMESTAMP = "CURRENT_TIMESTAMP";
	
	public static final String VAL_IS_PUBLIC = "1";
	public static final String VAL_IS_PRIVATE = "0";
	
	public static String getIsPublicVal(Boolean isPublic)
	{
		if(isPublic)
			return DBConstants.VAL_IS_PUBLIC;
		else
			return DBConstants.VAL_IS_PRIVATE;
	}
	
	//fieldnames of database tables with type 'Object'
	public static final String FIELD_CREATION_DATE = "CreationDate";
	public static final String FIELD_CONTENT = "Content";
	//fieldnames of mastertable 'databasedescription'
	public static final String FIELD_ID = "ID";
	public static final String FIELD_TYPE = "Type";
	public static final String FIELD_NAME = "Name";
	public static final String FIELD_PUBLIC = "Public";
	public static final String FIELD_TABLENAME = "Tablename";
	public static final String FIELD_DISPLAYNAME = "Displayname";
	public static final String FIELD_DESCRIPTION = "Description";
	public static final String FIELD_ENGLISHDESCRIPTION = "EnglishDescription";
	public static final String FIELD_FORMAT = "Format";
	public static final String FIELD_NOVALUE = "NoValue";
	public static final String FIELD_MULTIPLE = "Multiple";
	public static final String FIELD_ORDERNUMBER = "OrderNumber";
	public static final String FIELD_FIRSTTABLE = "FirstTable";
	public static final String FIELD_SECONDTABLE = "SecondTable";
	public static final String FIELD_MANDATORY = "Mandatory";
	public static final String FIELD_PUBLICDEFAULT = "PublicDefault";
	
	public static final String VIEW_NAME = "ethnoarc_dyn_view_";
	
	/**
	 * Defines the types of db elements.
	 * <li>Table: The element is a table. E.g. <code>Adresse</code>.
	 * <li>TableElement: The element is an element of a table. E.g. <code>Stadt</code> which belongs to a table <code>Adresse</code>.
	 * <li>RelationElement: The element is a DB element describing the relation between <code>Table<->Table</code> or 
	 * <code>Table<->TableElement</code>. E.g. 	<code>Adresse_Contains_Stadt</code> that describes the relationship between the table 
	 * <code>Adresse</code> and its table element <code>Stadt</code>.
	 * <li>Others: Other types of elements other than obove mentioned types. 
	 */
	public static enum DBElementTypes{
		/** The element is a table. E.g. <code>Adresse</code>.*/
		Table,
		/** The element is an element of a table. E.g. <code>Stadt</code> which belongs to a table <code>Adresse</code>. */
		TableElement,
		/** 
		 * The element is a DB element describing the relation between <code>Table<->Table</code> or 
		 * <code>Table<->TableElement</code>. E.g. 	<code>Adresse_Contains_Stadt</code> that describes the relationship between the table 
		 * <code>Adresse</code> and its table element <code>Stadt</code>.*/
		RelationElement,
		HelperTable,
		/**Other types of elements other than above mentioned types.*/
		Others,
		NonSpecified;
	}
	
	public static enum TableReferenceTypes {
		Contains,
		TakesValueFrom,
		ExclusiveTakesValueFrom,
		Alternative,
		AlternativeLanguage,
		Implies,
		ImpliedBy,
		TakesReferenceFrom
	}
	public static  enum ELEMENT_TYPE {
		ELEMENT,
		TABLE}
	/**
	 * 
	 * 
	 */
	public static enum USER_LEVELS{
		Manage,
		Edit,
		Browse,
		BrowseRestrict
	}
}
