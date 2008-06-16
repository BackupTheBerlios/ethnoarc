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

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class QueryServer {
	public static Hashtable<String,QueryServerArchiveInfo> archives;
    public static int port, nameport; 
	/** All application properties */
	private static Properties properties;
	/** The location of the properties. */
	private static String propertyFile="properties/ethnoarcQueryServer.prop";     

    
    public static void main(String[] args) {
    	
        try {
	    	Logger.getLogger(QueryServer.class.getName());
	    	PropertyConfigurator.configure("log/ethnoarcLog.properties");
	     	} catch (Exception e) {}
	    	
	        // prepare archive access table
	        archives=new Hashtable<String,QueryServerArchiveInfo>();
	        // 1099 is the default port for RMI
	        port=0;
	        nameport=1099;
		    // read properties	      
	        properties = new Properties();
	        readPropertyFile();
	     try {   
	    	 //QueryServerInterfaceImplementation QSII = new QueryServerInterfaceImplementation();
//
	//    	 LocateRegistry.createRegistry(port);   
	  //  	 Naming.bind("//localhost:"+port+"/"+"QueryServer", QSII);
	    	 
	    	 QueryServerInterfaceImplementation QSII = new QueryServerInterfaceImplementation();
	    	 UnicastRemoteObject.exportObject(QSII, port);
	    	 LocateRegistry.createRegistry(nameport);   
	   	   	 Naming.bind("//localhost:"+nameport+"/"+"QueryServer", QSII);
	    	 
	    	 
            System.out.println("*** Query Server published on port "+nameport+", exported to port "+port+" ***");
        } catch (Exception e) {
            System.err.println("QueryServer exception: "+e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    	
    /** 
	 * Reads property file.
	 */
	private static void readPropertyFile() 
	{
		Set keys;	
		Iterator iter;
		
		try {
			properties.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
			return;
		}
		// read archive data and set archive info, if available 
		keys=properties.keySet();
		iter = keys.iterator();
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();    		
  			    	
	    	if(((String)key).startsWith("ArchiveName_")){
	    		String archiveReference =((String)key).substring(12);   	    		
	    		QueryServerArchiveInfo archiveInfo = new QueryServerArchiveInfo();
	    		archiveInfo.name=new String(properties.getProperty((String)key));
	    		archiveInfo.name=new String(properties.getProperty("ArchiveName_"+archiveReference));
	    		archiveInfo.computer=new String(properties.getProperty("ArchiveComputer_"+archiveReference));
	    		archiveInfo.port=new String(properties.getProperty("ArchivePort_"+archiveReference));
	    		archiveInfo.database=new String(properties.getProperty("ArchiveDatabase_"+archiveReference));
	    		archiveInfo.user=new String(properties.getProperty("ArchiveUser_"+archiveReference));
	    		archiveInfo.password=new String(properties.getProperty("ArchivePassword_"+archiveReference));
	    		QueryServer.archives.put(archiveInfo.name, archiveInfo);   		
				}
	    	if(((String)key).equals("QueryServerNamePort")){	
	    		String strPort=new String(properties.getProperty("QueryServerNamePort"));
				int  val=0;
				for(int i=0;i<strPort.length();i++){
					if((strPort.charAt(i)>='0')&&(strPort.charAt(i)<='9'))
						val=val*10+(strPort.charAt(i)-'0');
	    		}
				nameport=val;
	    	}
	    	if(((String)key).equals("QueryServerPort")){	
	    		String strPort=new String(properties.getProperty("QueryServerPort"));
				int  val=0;
				for(int i=0;i<strPort.length();i++){
					if((strPort.charAt(i)>='0')&&(strPort.charAt(i)<='9'))
						val=val*10+(strPort.charAt(i)-'0');
	    		}
				port=val;
	    	}
	    }
	}
}