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
package de.fhg.fokus.se.ethnoarc.webSearch;

import java.net.*;
import java.util.*;
import java.io.*;
import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;

import java.sql.ResultSet;


public class WebSearch {

	/**
	 * The parsed DB structure.
	 */
	 static DBStructure dbStructure;
	/**
	 * The DB handler object to manage database communication.
	 */
	 static DBHandling dbHandle;
	 
	 static DBSqlHandler sqlHandler;
	
	static int socketUsed = 6452;
	static ServerSocket serverSocket = null;
	static long         clients      = 0;
	static int         MAXSLEEP = 100; // maximum wait before timeout in 1/10 seconds
	static String DBURL ="jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_test";
	static String DBUSERNAME ="ethnoarc";
	static String DBPASSWORD ="ethnoarc";
	static String PROXYNAME ="proxy.phtml";
	static String BASEDIR ="E:/Apache/Apache2/htdocs/";
	
	static Hashtable <String,DBTable> combinedTables;
	static Hashtable <String,EADBDescription> originalTables;	
	
	public static void main(String[] args) {

			    Socket clientSocket = null;
			    readProperties();
			    parseDB();
			    try {
			      serverSocket = new ServerSocket(socketUsed);
			    } catch (java.io.IOException e) {
			      System.err.println("Exception: " + e);
			      return;
			    }
			    System.out.println("WebSearch is online!");
			    try {
			      while ((clientSocket = serverSocket.accept()) != null) {
			        ClientThread clientThread = new ClientThread(clientSocket);
			        clientThread.start();
			      }
			    } catch (java.io.IOException e) {
			      System.err.println(e);
			      return;
			    }
			  }
	
	private static void readProperties() {
		java.util.Properties props = new java.util.Properties();
		try {
			props.load(  new FileInputStream ( new File ( "WebSearch.prop" )  ) );
		} catch (Exception e) {
			try {
			props.load(  new FileInputStream ( new File ( "properties/WebSearch.prop" )  ) );
			} catch (Exception e1) {return;}
		}
		// seems we have a property file, so let's see what's in there
        if (props.getProperty("DBPassword") != null)
        	DBPASSWORD = props.getProperty("DBPassword");
        if (props.getProperty("DBUserName") != null)
        	DBUSERNAME = props.getProperty("DBUserName");
        if (props.getProperty("DBURL") != null)
        	DBURL = props.getProperty("DBURL");
        if (props.getProperty("ProxyName") != null)
        	PROXYNAME = props.getProperty("ProxyName");
        if (props.getProperty("DocBaseDir") != null)
        	BASEDIR = props.getProperty("DocBaseDir");
        if (props.getProperty("Socket") != null)
        	socketUsed = Integer.parseInt(props.getProperty("Socket"));
	}
    
	private static void parseDB() {
		try {
			dbHandle = new DBHandling(DBURL, DBUSERNAME, DBPASSWORD);
			dbStructure = dbHandle.getDBStructure();
			getTables(dbStructure);
			sqlHandler=DBSqlHandler.getInstance(DBConstants.DBDRIVER, DBURL, DBUSERNAME, DBPASSWORD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getTables(DBStructure dbStructure) {
		combinedTables = dbStructure.getCombinedTables();
		originalTables = dbStructure.getTables();
		//paseThroughCBTables(combinedTables);
	//	justCheckingTables(originalTables);
	}
	private static void justCheckingTables(Hashtable <String,EADBDescription> originalTables ){
		for (EADBDescription originalTable : originalTables.values()) {
		 // System.out.println("TableName: "+originalTable.getDescription());
		  System.out.println("TableName: "+originalTable.getName());
		}		
	}		
	static class ClientThread
	    extends Thread {

	    Socket clientSocket = null;
	 
		static String MultiResultHTML ="";
		static String SingleResultHTML ="";
		static String NoResultHTML ="";
		static String ViewName ="";
		static int ResultLimit=-1;

	    public ClientThread(Socket socket) {
	      clientSocket = socket;
	    }
	    public void run() {

	        String data = "";
	        String key = "";
	        String value = "";
	        String tableName = "";
	        int sleeps = 0;
	        int nData = 0;
	        int parametersExpected = -1;
	        int parametersReceived = 0;
	        char c;
	        Vector allSearchObjects= new Vector();
	        Vector usedSearchObjects= new Vector();
	        Vector searchStrings= new Vector();
	        boolean bUseAndSearch=true;
	        char    cSearchType='s';
	        boolean bShowSinglePage=false;

	       
	        
	        try {
	          java.io.DataOutputStream out = new java.io.DataOutputStream(clientSocket.getOutputStream());
	          java.io.DataInputStream  in  = new java.io.DataInputStream(clientSocket.getInputStream());
	          while((parametersExpected==-1)||(parametersReceived<parametersExpected)){
	        	  while(in.available()==0){
	        		  try{Thread.sleep(100);}catch (Exception e) { }
	        		  sleeps++;  
	        		  if(sleeps>MAXSLEEP){ 
	        			  out.writeBytes("Timeout reached<br />");	  
	    	              clientSocket.close();
	    	              return;
	        			 
	        		  }
	        	  }
	        	  for(int i=0;i<in.available();i++){
	        		  c=(char)in.readUnsignedByte();
	        		  if(parametersExpected==-1){
	        			  if(c=='\n')parametersExpected=nData;
	        			  else nData=nData*10+(c-'0');
	        		  }
	        		  else {
	        			  if(c=='\1'){
	        				  key=new String(data);
	        				  data="";
	        			  }
	        			  else if(c=='\2'){
	        				  value=new String(data);
	        				  data="";
	        				  parametersReceived++;
	        				  
	        				  if(key.equals("ethnoArcResultpage")){	        				  
		        				  	MultiResultHTML=new String(value);
		        				  	tableName="";
		        				  }
	        				  else if(key.equals("ethnoArcView")){	        				  
	        					  ViewName=new String(value);
	        					  bShowSinglePage=true;
		        				  tableName="";
		        				  }
	        				  else if(key.equals("ethnoArcSinglePage")){	        				  
	        					    SingleResultHTML=new String(value);
	        					    bShowSinglePage=true;
		        				  	tableName="";
		        				  }
	        				  else if(key.equals("ethnoArcAndOr")){
		        					if(value.equalsIgnoreCase("and"))	
		        				  		bUseAndSearch=true;        	
		        					if(value.equalsIgnoreCase("or"))	
		        				  		bUseAndSearch=false;        	
		        				  	tableName="";
		        				  }
	        				  else if(key.equals("ethnoArcSearchType")){
		        					if(value.equalsIgnoreCase("C"))	
		        						cSearchType='c';        	
		        					else if(value.equalsIgnoreCase("S"))	
		        						cSearchType='s';          	
		        					else if(value.equalsIgnoreCase("M"))	
		        						cSearchType='m';     
		        				  	tableName="";
		        				  }
	        				  else {
	        				  	// check whether object with that name actually exists
	        				  	if(bShowSinglePage){
	        				  		// we already know that these objects exist, so we just add them
		        			        allSearchObjects.add(key);
		        			        if(value.length()>0){
		        			        	usedSearchObjects.add(key);
		        			        	searchStrings.add(value);	
		        			        }
	        				  	}
	        				  	else
	        				  	{ // check whether these are proper object names
	        				  		boolean bFoundObject=false;
	        				 	  for (EADBDescription originalTable : originalTables.values()) {
									if(key.equals(originalTable.getName())){
										bFoundObject=true;
										tableName=originalTable.getNameDB();
										break;
									}
							  	}
							  	if(!bFoundObject)out.writeBytes("Incorrect query for database object "+key+"<br />\n");
							  	else {
		        			        allSearchObjects.add(tableName);
		        			        if(value.length()>0){
		        			        	usedSearchObjects.add(tableName);
		        			        	searchStrings.add(value);		        			        		        			        
		        			        }
							  	}
	        				  	}
							  }				
	        				  //System.out.println("Parameter "+parametersReceived+" has key "+key+"("+tableName+") and value "+value);
	        			  }
	        			  else data=data+c;	        		    
	        		  }
	        	  }
	          }
	          // if this is the call for a single page, handle this separately
			  if(bShowSinglePage){
				  returnResultSinglePage(out,ViewName,SingleResultHTML,usedSearchObjects,searchStrings);					    
		          clientSocket.close();
		          return;	              
			  }
			  
	          // got all query parameters, so check what result values we need...
	          Vector returnObjects=parseResultPage(out);

	          // at this point we should parse the 'one result' page to find whether additional info is needed
	          Vector singleReturnObjects=parseSingleResultPage(SingleResultHTML);
	          // * not implemented yet *
	          
	          // collect all objects needed for view
	          Vector allViewObjects=new Vector();
			  for(int i=0;i<returnObjects.size();i++)
					  allViewObjects.add(returnObjects.elementAt(i));		
			  for(int i=0;i<allSearchObjects.size();i++){
				  boolean bAlreadyThere=false;
				  for(int i1=0;i1<allViewObjects.size();i1++){
					  if(allViewObjects.elementAt(i1).equals(allSearchObjects.elementAt(i)))bAlreadyThere=true;
				  }
				  if(!bAlreadyThere){
					  allViewObjects.add(allSearchObjects.elementAt(i));
				  }
			  }
			  for(int i=0;i<singleReturnObjects.size();i++){
				  boolean bAlreadyThere=false;
				  for(int i1=0;i1<allViewObjects.size();i1++){
					  if(allViewObjects.elementAt(i1).equals(singleReturnObjects.elementAt(i)))bAlreadyThere=true;
				  }
				  if(!bAlreadyThere){
					  allViewObjects.add(singleReturnObjects.elementAt(i));
				  }
			  }
			  
	
	          // create view for these objects   
			  ViewName= sqlHandler.getViewName(dbStructure, allViewObjects);

	          // create query statement on this view
			  String query=CreateQueryStatement(ViewName,allViewObjects,usedSearchObjects,searchStrings,bUseAndSearch,cSearchType);
              // need to perform database query here
			  ResultSet rs=null;
			  try{		   
			   rs= sqlHandler.executeQuery(query);

			  }catch(Exception e){ System.err.println("SQL Query "+e);	}	
			  returnResultPage(rs,out,allViewObjects);				               
	          clientSocket.close();
	          return;	              
	      } catch (java.io.IOException e) {
	        System.err.println("Read exception "+e);	    
	        //                clientSocket.close();
	        return;
	      }
	    }
	    

		private static String CreateQueryStatement(String view, Vector allViewObjects,Vector usedSearchObjects,Vector searchStrings, boolean bUseAndSearch, char cSearchType ){
			String result="Select ";
			for (int i = 0; i<allViewObjects.size();i++){
				result=result+allViewObjects.elementAt(i);
				if(i!=allViewObjects.size()-1)result=result+",";
			}
			result=result+" from "+view;
			if(usedSearchObjects.size()>0){
				result=result+" where ";
				for (int i = 0; i<usedSearchObjects.size();i++){
					result=result+usedSearchObjects.elementAt(i)+" like '";
					if(cSearchType=='c')result=result+'%';
					result=result+searchStrings.elementAt(i);
					if(cSearchType!='m')result=result+'%';
					result=result+"' ";
					if(i!=usedSearchObjects.size()-1){
						if(bUseAndSearch)result=result+"AND ";
						else result=result+"OR ";
					}
				}				
			}
			result=result+" ORDER BY "+allViewObjects.elementAt(0);
			if(ResultLimit!=-1)result=result+" LIMIT 0,"+(ResultLimit+1);
			result=result+";";
			//System.out.println("Query is: "+result);
			return result;					
		}				
		
		
		 private static Vector parseResultPage(DataOutputStream out){
			Vector returnObjects=new Vector(); 
		    SingleResultHTML="";
		    NoResultHTML="";
			String line;
		  File file = new File(BASEDIR+MultiResultHTML);
		  FileInputStream fis = null;
		  BufferedInputStream bis = null;
		  DataInputStream dis = null;
		  try {
			  fis = new FileInputStream(file);
			  bis = new BufferedInputStream(fis);
			  dis = new DataInputStream(bis);
			  while (dis.available() != 0) {
				  line=dis.readLine();
				  if(line.contains("<!--"))returnObjects=parseCommentLine(line,dis, out);
			  }
			  //for(int i=0;i<returnObjects.size();i++)
			  //  System.out.println(returnObjects.elementAt(i));
		      fis.close();
		      bis.close();
		      dis.close();
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }		
		    return returnObjects;
		}

		private static Vector parseSingleCommentLine(String line,DataInputStream dis){
			Vector returnVector=new Vector();
			
			// first, get line to closing comment, replacing \n with spaces
			try {
				while(!line.contains("-->"))line=line+dis.readChar();
			}catch(IOException e) {}
			line.replace('\n', ' ');
			// check whether line contains <!--, ethnoArcObject, % and --> in that order
            int i1,i2,i3,i4;
            i1=line.indexOf("<!--");
            i2=line.indexOf("ethnoArcObject");
            i3=line.indexOf("%");
            i4=line.indexOf("-->");
            if((i1>-1)&&(i2>-1)&&(i3>-1)&&(i4>-1)&&(i1<i2)&&(i2<i3)&&(i3<i4)){
            	 String objectName=line.substring(line.indexOf("%")+1);
            	 if(objectName.indexOf("%")!=-1){
            		 objectName=objectName.substring(0,objectName.indexOf("%"));
            		 // check name against object table to find table name for object.
				  	  boolean bFoundObject=false;
  				 	  for (EADBDescription originalTable : originalTables.values()) {
							if(objectName.equals(originalTable.getName())){
								bFoundObject=true;
								returnVector.add(originalTable.getNameDB());
								break;
							}
					  	}
            	 }
            }
            // line might contain multiple objects, so check the rest of the line as well            
            line=line.substring(line.indexOf("-->")+2);
            if(line.contains("<!--")){
            	Vector moreContent=parseSingleCommentLine(line,dis);
            	for(int i=0;i<moreContent.size();i++)returnVector.add(moreContent.elementAt(i));
            }
			return returnVector;
		}
		
		
		 private static Vector parseSingleResultPage(String FileName){
				Vector returnSingleObjects=new Vector(); 
				String line;
			  File file = new File(BASEDIR+FileName);
			  FileInputStream fis = null;
			  BufferedInputStream bis = null;
			  DataInputStream dis = null;
			  try {
				  fis = new FileInputStream(file);
				  bis = new BufferedInputStream(fis);
				  dis = new DataInputStream(bis);
				  while (dis.available() != 0) {
					  line=dis.readLine();
					  if(line.contains("<!--")){
						  Vector returnSingleObjectQuery=parseSingleCommentLine(line,dis);
						  // add new entries to list
						  for(int i=0;i<returnSingleObjectQuery.size();i++){
							  boolean bAlreadyThere=false;
							  for(int i1=0;i1<returnSingleObjects.size();i1++){
								  if(returnSingleObjects.elementAt(i1).equals(returnSingleObjectQuery.elementAt(i)))bAlreadyThere=true;
							  }
							  if(!bAlreadyThere)returnSingleObjects.add((String)(returnSingleObjectQuery.elementAt(i)));							  
						  }
					  }
				  }
			      fis.close();
			      bis.close();
			      dis.close();
			    } catch (FileNotFoundException e) {
			      e.printStackTrace();
			    } catch (IOException e) {
			      e.printStackTrace();
			    }		
			    return returnSingleObjects;
			}
		 
		 
		private static Vector parseCommentLine(String line,DataInputStream dis,DataOutputStream out){
			Vector returnObjects=new Vector();
		    String databaseObject;
		    String tableName="";
			// first, get line to closing comment, replacing \n with spaces
			try {
				while(!line.contains("-->"))line=line+dis.readChar();
			}catch(IOException e) {}
			line.replace('\n', ' ');
			if(!line.contains("ethnoArcResultList"))return returnObjects;
			line=line.substring(line.indexOf("ethnoArcResultList")+18);
			if(line.contains("-->"))line=line.substring(0, line.indexOf("-->"));
			line=line.trim();
			// get name of single result file
			if(!line.contains(";"))return returnObjects;
			// find answer page, no answer page, maxcount
			String parameterLine=new String((line.substring(0,line.indexOf(';'))).trim());
			ResultLimit=-1;
			if(parameterLine.indexOf(",")==-1){
				SingleResultHTML=new String((line.substring(0,line.indexOf(';'))).trim());
				NoResultHTML=new String(MultiResultHTML);
			}
			else {
				SingleResultHTML=new String((line.substring(0,line.indexOf(','))).trim());
				parameterLine=parameterLine.substring(parameterLine.indexOf(",")+1).trim();
				if(parameterLine.indexOf(",")==-1){
					NoResultHTML=new String(parameterLine);
				}
				else {
					NoResultHTML=new String((parameterLine.substring(0,parameterLine.indexOf(','))).trim());
					parameterLine=parameterLine.substring(parameterLine.indexOf(",")+1).trim();
					// get number of replies...
					ResultLimit=0;
					for(int i=0;i<parameterLine.length();i++)
						ResultLimit=ResultLimit*10+(parameterLine.charAt(i)-'0');
				}			
			}
			line=line.substring(line.indexOf(';')+1,line.length()).trim();
			// get database objects to be shown	  
			while(line.contains(",")){
				databaseObject=new String((line.substring(0,line.indexOf(','))).trim());
				line=line.substring(line.indexOf(',')+1,line.length()).trim();
				// check whether object with that name actually exists
	        	boolean bFoundObject=false;
	        	for (EADBDescription originalTable : originalTables.values()) {
					if(databaseObject.equals(originalTable.getName())){
					bFoundObject=true;
					tableName=originalTable.getNameDB();
					break;
					}
				}
				if(!bFoundObject){
				 try{
				 	out.writeBytes("Internal HTML scan error - database object "+databaseObject+" not available.<br />");
				 }catch(Exception e){};
				}
				else returnObjects.add(new String(tableName));				  
			}
				// check for final object in line
	        	boolean bFoundObject=false;
	        	for (EADBDescription originalTable : originalTables.values()) {
					if(line.equals(originalTable.getName())){
					bFoundObject=true;
					tableName=originalTable.getNameDB();
					break;
					}
				}
				if(!bFoundObject){
				 try{
				 	out.writeBytes("Internal HTML scan error - database object "+line+" not available.<br />");
				 }catch(Exception e){};
				}
				else returnObjects.add(new String(tableName));
				        
				return returnObjects;
		}
		
		private static void returnResultAtCommentLine(ResultSet rs,String line,DataInputStream dis, DataOutputStream out,Vector allViewObjects ){
			String trail="";
			String databaseObject="";
			Vector objectTable=new Vector();
			Vector objectDisplayName=new Vector();
			// first, get line to closing comment, replacing \n with spaces
			try {
				while(!line.contains("-->"))line=line+dis.readChar();
			}catch(IOException e) {}
			line.replace('\n', ' ');
			// if this is not the comment we're looking for, just print the line and leave
			if(!line.contains("ethnoArcResultList")){
				try{
				  out.writeBytes(line);
				  out.writeBytes("\n");
				}catch(Exception e){}
				 return;
			}
			// this is an ethnoarc result line, so work on this.
			if(line.indexOf("<!--")!=0){
				try{
				 out.writeBytes(line.substring(0,line.indexOf("<!--")));
			     out.writeBytes("\n");
				}catch(Exception e){}
				line=line.substring(line.indexOf("<!--"));
			}			
			if(line.indexOf("-->")!=line.length()-3){
				trail=line.substring(line.indexOf("-->")+3);
				line=line.substring(0,line.indexOf("-->")+3);
			}
			if(rs==null){
				try{
					out.writeBytes("No matching results found \n");
					if(trail.length()>0)out.writeBytes(trail+"\n");
				}catch(Exception e){e.printStackTrace();}
				return;
			}
			// parse line and figure out what objects we need and what to call them
			line=line.substring(line.indexOf(';')+1,line.length()).trim();
			// get database objects to be shown	  
			while(line.contains(",")){
				databaseObject=new String((line.substring(0,line.indexOf(','))).trim());
				line=line.substring(line.indexOf(',')+1,line.length()).trim();
				// check whether object with that name actually exists
	        	for (EADBDescription originalTable : originalTables.values()) {
					if(databaseObject.equals(originalTable.getName())){												
						objectTable.add(originalTable.getNameDB());
						objectDisplayName.add(originalTable.getDisplayname());						
					break;
					}
				}
			}
				// check for final object in line
			    if(line.contains("-->"))line=line.substring(0, line.indexOf("-->"));
			    line=line.trim();
	        	for (EADBDescription originalTable : originalTables.values()) {
					if(line.equals(originalTable.getName())){
						objectTable.add(originalTable.getNameDB());
						objectDisplayName.add(originalTable.getDisplayname());						
					break;
					}
				}

            // now we know the table and display names - find out where these are located in the SQL query result
	         int [] objectNumber = new int[objectTable.size()+1];
	        for(int i=0;i<objectTable.size();i++){
	        	for(int i1=0;i1<allViewObjects.size();i1++)
	        		if(objectTable.elementAt(i).equals(allViewObjects.elementAt(i1))){
	        				objectNumber[i]=i1+1;	        					      
	        		}
	        }
	        	
			try{
				// now output table header
				out.writeBytes("<table>\n");
				out.writeBytes("<tr>\n");
				out.writeBytes("<td> </td>\n");			
				for(int i=0;i<objectTable.size();i++){
					out.writeBytes("<td><b>\n");
					out.writeBytes((String)objectDisplayName.elementAt(i));
					out.writeBytes("</b></td>\n");					
				}
				out.writeBytes("</tr>\n");
			// and now the table content								
			rs.beforeFirst();
			int ResultsReturned=0;
			while( rs.next() )
			{
				out.writeBytes("<tr>\n");
				// create hidden form for link to allow user to select a specific entry
				out.writeBytes("<td> \n");
				out.writeBytes("<FORM method=\"POST\" action=\""+PROXYNAME+"\">\n");
				out.writeBytes("<input type=\"hidden\" name=\"ethnoArcSinglePage\" value=\""+ SingleResultHTML+ "\">\n");
				out.writeBytes("<input type=\"hidden\" name=\"ethnoArcView\" value=\""+ ViewName+ "\">\n");
				for(int i=0;i<objectTable.size();i++){
					out.writeBytes(" <input type=\"hidden\" name=\""+(String)objectTable.elementAt(i)+
							"\" value=\""+rs.getString(objectNumber[i])+"\">\n");
				}		
				
				out.writeBytes("<input type=\"submit\" value=\"+\">\n");
				out.writeBytes("&nbsp;</FORM>\n");                
				out.writeBytes("</td>\n");
				// write table data for users to read
				for(int i=0;i<objectTable.size();i++){
					out.writeBytes("<td>\n");
					out.writeBytes( rs.getString(objectNumber[i]));
					out.writeBytes("</td>\n");					
				}
				out.writeBytes("</tr>\n");
				ResultsReturned++;
				if((ResultsReturned>=ResultLimit)&&(ResultLimit>-1))break;				
			}				
			// have we got more results than we want to return?
			if((ResultsReturned>=ResultLimit)&&(ResultLimit>-1)){
  			  if(rs.next()){
  				  // more results than requested - give the user a hint of this
  				out.writeBytes("<tr>\n");
				out.writeBytes("<td> </td>\n");					
				for(int i=0;i<objectTable.size();i++){
					out.writeBytes("<td>\n");
					out.writeBytes("...");
					out.writeBytes("</td>\n");					
				}
				out.writeBytes("</tr>\n");
			  }				
			}
			
			out.writeBytes("</table>\n");
			if(trail.length()>0)out.writeBytes(trail+"\n");
			}catch(Exception e){e.printStackTrace();}			
		}
		
		private static void returnResultPage(ResultSet rs,DataOutputStream out,Vector allViewObjects ){
		  File file = null;	 
		  FileInputStream fis = null;
		  BufferedInputStream bis = null;
		  DataInputStream dis = null;
		  String line;


		  try {
			  
			  rs.beforeFirst();
			  if(rs.next())file = new File(BASEDIR+MultiResultHTML);
			  else file = new File(BASEDIR+NoResultHTML);
			  fis = new FileInputStream(file);
			  bis = new BufferedInputStream(fis);
			  dis = new DataInputStream(bis);
			  while (dis.available() != 0) {
				  line=dis.readLine();
				  if(line.contains("<!--"))returnResultAtCommentLine(rs,line,dis, out, allViewObjects);
				  else out.writeBytes(line);
				  out.writeBytes("\n");
			  }
		      fis.close();
		      bis.close();
		      dis.close();
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
			      e.printStackTrace();	  
	    	} catch (Exception e) {
		      e.printStackTrace();
		    }			  
		}	
		
		
		private static void returnSingleResultAtCommentLine(ResultSet rs, String line,DataInputStream dis,DataOutputStream out,Vector returnObjects){

			// first, get line to closing comment, replacing \n with spaces
			try {
				while(!line.contains("-->"))line=line+dis.readChar();
			}catch(IOException e) {}
			line.replace('\n', ' ');
			// check whether line contains <!--, ethnoArcObject, % and --> in that order
            int i1,i2,i3,i4;
            i1=line.indexOf("<!--");
            i2=line.indexOf("ethnoArcObject");
            i3=line.indexOf("%");
            i4=line.indexOf("-->");
            if((i1>-1)&&(i2>-1)&&(i3>-1)&&(i4>-1)&&(i1<i2)&&(i2<i3)&&(i3<i4)){
            	try{
            		out.writeBytes(line.substring(0,i4+3));
            		//out.writeBytes("\n");
            	}catch(Exception e){}
            	
            	 String objectName=line.substring(line.indexOf("%")+1);
            	 if(objectName.indexOf("%")!=-1){
            		 objectName=objectName.substring(0,objectName.indexOf("%"));
            		 // check name against object table to find table name for object.
  				 	  for (EADBDescription originalTable : originalTables.values()) {
							if(objectName.equals(originalTable.getName())){
								 try{ rs.first(); 
								 }catch(Exception e){System.err.println(e);}
								 
								  // find position of this object in list of returned objects
								for(int i=0;i<returnObjects.size();i++){
									if(returnObjects.elementAt(i).equals(originalTable.getNameDB())){
										try{
										  //System.err.println("Value for "+originalTable.getName()+" is "+rs.getString(i+1));
											out.writeBytes(rs.getString(i+1));
											//out.writeBytes("\n");
										}catch(Exception e){System.err.println(e);}
									}
								}								
								break;
							}
					  	}
            	 }
            }
            else {
            	// not the line we are looking for, so just write it to out and be done with it.
            	try{
            		out.writeBytes(line);
            		//out.writeBytes("\n");
            	}catch(Exception e){}
            	return;
            }
            // now handle the rest of the line    
            line=line.substring(line.indexOf("-->")+3);
            if(line.length()>0){
            	if(line.contains("<!--"))returnSingleResultAtCommentLine(rs,line,dis,out,returnObjects);
            	else {
                	try{
                		out.writeBytes(line);
                		//out.writeBytes("\n");
                	}catch(Exception e){}
            	}
            }
			return;		
		}
			
	private static void returnResultSinglePage(DataOutputStream out, String ViewName, String FileName, Vector usedSearchObjects, Vector searchStrings){
		  File file = null;	 
		  FileInputStream fis = null;
		  BufferedInputStream bis = null;
		  DataInputStream dis = null;
		  String line;

          Vector returnObjects=parseSingleResultPage(FileName);		
          // now create a SELECT that returns all these values for a specific entry

			String result="Select ";
			for (int i = 0; i<returnObjects.size();i++){
				result=result+returnObjects.elementAt(i);
				if(i!=returnObjects.size()-1)result=result+",";
			}
			result=result+" from "+ViewName;
			if(usedSearchObjects.size()>0){
				result=result+" where ";
				for (int i = 0; i<usedSearchObjects.size();i++){
					result=result+usedSearchObjects.elementAt(i)+" = '"+searchStrings.elementAt(i)+"' ";
					if(i!=usedSearchObjects.size()-1)
						result=result+"AND ";
					}			
			}		
			// get data for this entry
			result=result+";";			
			ResultSet rs=null;
			  try{			   
			   rs= sqlHandler.executeQuery(result);
			  }catch(Exception e){ System.err.println("SQL Query "+e);return;}
			  
			  //System.out.println("Query is "+result);

			  try {	  
				  rs.beforeFirst();
				  rs.next();
			  }catch(Exception e){System.err.println("SQL Query "+e);return;}
		  try {
			  
			  file = new File(BASEDIR+FileName);
			  fis = new FileInputStream(file);
			  bis = new BufferedInputStream(fis);
			  dis = new DataInputStream(bis);
			  while (dis.available() != 0) {
				  line=dis.readLine();
				  if(line.contains("<!--"))returnSingleResultAtCommentLine(rs,line,dis, out, returnObjects);
				  else 
					  out.writeBytes(line);
				  out.writeBytes("\n");
			  }
		      fis.close();
		      bis.close();
		      dis.close();
		    } catch (FileNotFoundException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
			      e.printStackTrace();	  
	    	} catch (Exception e) {
		      e.printStackTrace();
		    }			  
		}	
	}
}
	

