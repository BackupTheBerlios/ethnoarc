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
 * $Id: DBException.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * @author fokus
 */
public class DBException extends Exception  {
	//	 Error types
	public static final short UNKNOWN_ERROR=0;
	
	public static final short DB_CONNECTION_UNKNOWN_HOST=1;
	public static final short DB_CONNECTION_UNKNOWN_DB=2;
	public static final short DB_CONNECTION_ACCESS_DENIED=3;
	public static final short DB_CONNECTION_FAILED=4;
	public static final short DB_LOGIN_INVALID=5;
	
	
	public static final short DATA_FORMAT_INVALID=10;
	public static final short DATA_MANDATORY_INVALID=20;
	public static final short APP_PROPERTY_FILE_NOTFOUND=30;
	public static final short DB_ELEMENT_UNKNOWN=40;
	
	public static final short DB_STRUCTURE_INVALID=50;
	public static final short DATA_VALUE_INVALID=60;

	public static final short SAVE_UNNECESSARY=90;
	public static final short SQL_EXCEPTION=100;
	
	public static final short FORMAT_TYPE_INVALID=200;
	
	public static final short APPLICATION_ERROR=300;
	public static final short APPLICATION_MEMORY_ERROR=350;
	
	public static final short SEARCH_ERROR=400;
	
	private  short errorCode; 
	private  String errorMsg;
	private String detailErrorMsg;
	
	public DBException(short errorCode)
	{
		super();
		this.errorCode=errorCode;
		detailErrorMsg=errorMsg;
	}
	public DBException(short errorCode,String detailMsg)
	{
		super();
		this.errorCode=errorCode;
		this.detailErrorMsg = detailMsg;
	}
	
	public short getErrorCode()
	{
		return errorCode;
	}
	
	public String getDetailedMsg()
	{
		if(detailErrorMsg!=null)
			return detailErrorMsg;
		else
			return getMessage();
	}
	 
	public String getMessage()
	{	
		switch (errorCode) {
		case UNKNOWN_ERROR:
			errorMsg = "Unknown Error";
			break;
		case DB_CONNECTION_UNKNOWN_HOST:
			errorMsg = "Specified database host unknown.";
			break;
		case DB_CONNECTION_UNKNOWN_DB:
			errorMsg = "Specified database not found.";
			break;
		case DB_CONNECTION_ACCESS_DENIED:
			errorMsg="Access denied for the specified username and password.";
			break;
		case DB_CONNECTION_FAILED:
			errorMsg="Connection failed.";
			break;
		case DB_LOGIN_INVALID:
			errorMsg="Username or password invalid.";
			break;
		case DATA_MANDATORY_INVALID:
			errorMsg =  "Mandatory field not specified.";
			break;
		case DATA_FORMAT_INVALID:
			errorMsg =  "Format of the field not valid.";
			break;
		case APP_PROPERTY_FILE_NOTFOUND:
			errorMsg="Application property file not found.";
			break;
		case DB_ELEMENT_UNKNOWN:
			errorMsg="Specified element does not exist in Database.";
			break;
		case DB_STRUCTURE_INVALID:
			errorMsg="DB structure not valid.";
			break;
		case DATA_VALUE_INVALID:
			errorMsg="value not valid.";
			break;
		case FORMAT_TYPE_INVALID:
			errorMsg= "Specified format type invalid";
			break;
		case APPLICATION_ERROR:
			errorMsg="Application error occured.";
			break;
		case APPLICATION_MEMORY_ERROR:
			errorMsg="Application out of memory occured.";
			break;
		case SEARCH_ERROR:
			errorMsg="Search error.";
			break;
		default:
			errorMsg =  "Unknown Error";
			break;
		}
		return errorMsg;
	}
	public String toString()
	{
		try{
		if(errorMsg.equals(detailErrorMsg))
			return "DBException Code: "+errorCode+" Reason: '"+getMessage()+"'";
		else
			return "DBException Code: "+errorCode+" Reason: '"+getMessage()+"' Detail: '"+detailErrorMsg+"'";
		}catch(NullPointerException e)
		{
			return "";
		}
	}
}
