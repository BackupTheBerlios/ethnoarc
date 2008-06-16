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
package de.fhg.fokus.se.ethnoarc.queryServer;

import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject ;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.SearchManager;
import de.fhg.fokus.se.ethnoarc.common.SearchObject;
import de.fhg.fokus.se.ethnoarc.common.SearchResult;

public class QueryServerInterfaceImplementation  //extends UnicastRemoteObject
 implements QueryServerInterface, Serializable{
	
	private static final long serialVersionUID = 1L;

	   //public QueryServerInterfaceImplementation() throws RemoteException
	   //{	   }
	   
	   protected QueryServerInterfaceImplementation() throws RemoteException
	   {
	   }

	   protected QueryServerInterfaceImplementation(int port) throws RemoteException
	   {
	   }
	   
	public DBStructure getDBStructure(String dbname) throws RemoteException{
		DBStructure dbs=null;

		System.err.println("Remote getDBStructure for archive "+dbname+" starts");
		QueryServerArchiveInfo archive=QueryServer.archives.get(dbname);
		if(archive!=null)
		try {
			DBHandling dbHandle = new DBHandling("jdbc:mysql://"+archive.computer+":"+archive.port+"/"+archive.database, archive.user, archive.password, true);
			dbs = dbHandle.getDBStructure();	
		} catch (Exception e) {
	        System.out.println("Remote query server access exception = " + e.getMessage() + e.toString()) ;			
			e.printStackTrace();
		}		
			
		System.err.println("Remote getDBStructure done");
		return dbs;
	}
	public SearchObject getSearchObject(String dbname, Vector <String> fields ) throws RemoteException{
		SearchObject searchObject=null;

		System.err.println("Remote getSearchObject for archive "+dbname+" starts");
		QueryServerArchiveInfo archive=QueryServer.archives.get(dbname);
		if(archive!=null)
		try {
			DBHandling dbHandle=null;	
			DBStructure dbStructure=null;	
			dbHandle = new  DBHandling("jdbc:mysql://"+archive.computer+":"+archive.port+"/"+archive.database, archive.user, archive.password, true);			
			dbStructure = dbHandle.getDBStructure();	
			DBSqlHandler sqlHandler=new DBSqlHandler(DBConstants.DBDRIVER, "jdbc:mysql://"+archive.computer+":"+archive.port+"/"+archive.database, archive.user, archive.password);
			searchObject = sqlHandler.getSearchObject(dbStructure, fields);						
		} catch (Exception e) {
	        System.out.println("Remote query server access exception = " + e.getMessage() + e.toString()) ;			
			e.printStackTrace();
		}					
		System.err.println("Remote getSearchObject done");
		return searchObject;
	}
	
	public SearchResult getSearchResult(String dbname, List<String> inputParams, List<String> outputParams, String sqlWhereString)throws RemoteException{
		SearchResult searchResult=null;

		System.err.println("Remote getSearchResult for archive "+dbname+" starts");
		QueryServerArchiveInfo archive=QueryServer.archives.get(dbname);
		if(archive!=null)
		try {
			SearchManager sm= new SearchManager("jdbc:mysql://"+archive.computer+":"+archive.port+"/"+archive.database, archive.user, archive.password);
			searchResult =sm.search(inputParams, outputParams, sqlWhereString);
					
		} catch (Exception e) {
	        System.out.println("Remote query server access exception = " + e.getMessage() + e.toString()) ;			
			e.printStackTrace();
		}					
		System.err.println("Remote getSearchResult done");
		return searchResult;
	}

	public Vector<String> getTablesByContent(String dbname, String searchString) throws RemoteException {
		SearchResult searchResult=null;

		System.err.println("Remote getTablesByContent for archive "+dbname+" starts");
		QueryServerArchiveInfo archive=QueryServer.archives.get(dbname);
		if(archive!=null)
		try {
			DBHandling dbHandle=null;	
			DBStructure dbStructure=null;	
			dbHandle = new  DBHandling("jdbc:mysql://"+archive.computer+":"+archive.port+"/"+archive.database, archive.user, archive.password, true);			
			dbStructure = dbHandle.getDBStructure();
			System.err.println("Remote getTablesByContent done");
			return dbStructure.getTablesByContent(searchString);					
		} catch (Exception e) {
	        System.out.println("Remote query server access exception = " + e.getMessage() + e.toString()) ;			
			e.printStackTrace();
		}					
		System.err.println("Remote getTablesByContent done");

		return null;
	}

	
}
