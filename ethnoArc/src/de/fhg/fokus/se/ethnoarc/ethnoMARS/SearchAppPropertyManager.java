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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.awt.Color;

import javax.swing.ToolTipManager;


public class SearchAppPropertyManager {
	
	/** The instance of this class. */
	private static SearchAppPropertyManager ethnoarcAppProps;
	/** All application properties */
	private static Properties properties;
	/** The location of the properties. */
	private final String propertyFile="properties/ethnoarcSearch.prop"; 
	
	/**
	 * Private constructor of the class. Reads the application properties.
	 * @throws IOException Specified property file cannot be found.
	 */
	private SearchAppPropertyManager() 
	{
		properties = new Properties();
		readPropertyFile();
	}
	/**
	 * Gets the instance of the class.
	 * @return The instance of the class.
	 * @throws IOException 
	 */
	public static SearchAppPropertyManager getDBPropertyManagerInstant() 
	{	
		if(ethnoarcAppProps==null)
		{		
			ethnoarcAppProps=new SearchAppPropertyManager();
		}
		return ethnoarcAppProps;
	}
	
	/** 
	 * Reads property file.
	 */
	private void readPropertyFile() 
	{
		Set keys;	
		Iterator iter;		
		
		try {
			properties.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
		}		
		
		// read archive data and set archive info, if available 
		keys=properties.keySet();
		iter = keys.iterator();
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();
	    	if(((String)key).startsWith("ArchiveName_")){
	    		ArchiveInfo archiveInfo = new ArchiveInfo();
	    		archiveInfo.name=new String(properties.getProperty((String)key));
	    		archiveInfo.computer=new String(properties.getProperty("ArchiveComputer_"+archiveInfo.name));
	    		archiveInfo.port=new String(properties.getProperty("ArchivePort_"+archiveInfo.name));
	    		archiveInfo.database=new String(properties.getProperty("ArchiveDatabase_"+archiveInfo.name));
	    		archiveInfo.user=new String(properties.getProperty("ArchiveUser_"+archiveInfo.name));
	    		archiveInfo.password=new String(properties.getProperty("ArchivePassword_"+archiveInfo.name));
	    		if(properties.containsKey("ArchiveCenterNode_"+archiveInfo.name))
	    		  archiveInfo.centerNodeName=new String(properties.getProperty("ArchiveCenterNode_"+archiveInfo.name));
	    		else archiveInfo.centerNodeName=null;
	    		
				if(properties.getProperty("ArchiveRMI_"+archiveInfo.name)==null)archiveInfo.useRMIquery=false;
				else {
						String RMIstring = properties.getProperty("ArchiveRMI_"+archiveInfo.name);
						if(RMIstring.equalsIgnoreCase("true"))archiveInfo.useRMIquery=true;
						else archiveInfo.useRMIquery=false;
				}
				if(properties.getProperty("ArchiveLocalLanguage_"+archiveInfo.name)==null)archiveInfo.useLocalLanguage=false;
				else {
						String LocalLanguageString = properties.getProperty("ArchiveLocalLanguage_"+archiveInfo.name);
						if(LocalLanguageString.equalsIgnoreCase("true"))archiveInfo.useLocalLanguage=true;
						else archiveInfo.useLocalLanguage=false;
				}
				if(properties.getProperty("ArchiveColor_"+archiveInfo.name)==null)archiveInfo.color=new Color(0,0,0);
				else { // parse color string for RGB values
					String colString = properties.getProperty("ArchiveColor_"+archiveInfo.name);
					int r=-1, g=-1, b=-1, val=0;
					for(int i=0;i<colString.length();i++){
						if((colString.charAt(i)>='0')&&(colString.charAt(i)<='9'))
							val=val*10+(colString.charAt(i)-'0');
						else if (colString.charAt(i)==']') b=val;
						else if (colString.charAt(i)==','){
							if(r==-1)r=val;
							else g=val;
							val=0;
						}
					}
					archiveInfo.color=new Color(r,g,b);
				}

	    		ControlFrame.archives.put(archiveInfo.name, archiveInfo);	    		
	    }
	    	if(((String)key).equals("ResultDisplay")){
	    		String resDis=new String(properties.getProperty("ResultDisplay"));
	    		if(resDis.equalsIgnoreCase("REPLACE_WINDOW"))QueryBuildManager.resultDisplay=QueryBuildManager.REPLACE_WINDOW;
	    		if(resDis.equalsIgnoreCase("MULTIPLE_WINDOWS"))QueryBuildManager.resultDisplay=QueryBuildManager.MULTIPLE_WINDOWS;
	    		if(resDis.equalsIgnoreCase("TABBED_WINDOW"))QueryBuildManager.resultDisplay=QueryBuildManager.TABBED_WINDOW;
	    		}
	    	if(((String)key).equals("WildcardStyle")){
	    		String resDis=new String(properties.getProperty("WildcardStyle"));
	    		if(resDis.equalsIgnoreCase("SQL_STYLE"))QueryBuildManager.wildcardStyle=QueryBuildManager.SQL_STYLE;
	    		if(resDis.equalsIgnoreCase("WINDOWS_STYLE"))QueryBuildManager.wildcardStyle=QueryBuildManager.WINDOWS_STYLE;
	    		}			
	    	if(((String)key).equals("CSVStyle")){
	    		String resDis=new String(properties.getProperty("CSVStyle"));
	    		if(resDis.equalsIgnoreCase("COMMA"))QueryBuildManager.CSVseparator=',';
	    		if(resDis.equalsIgnoreCase("SEMICOLON"))QueryBuildManager.CSVseparator=';';
	    		if(resDis.equalsIgnoreCase("TAB"))QueryBuildManager.CSVseparator='\t';
	    		}				    	
	    	if(((String)key).equals("TooltipDelay")){
	    		String resDel=new String(properties.getProperty("TooltipDelay"));
				int val=0;
				for(int i=0;i<resDel.length();i++)
					if((resDel.charAt(i)>='0')&&(resDel.charAt(i)<='9'))val=val*10+(resDel.charAt(i)-'0');
				if(val<10)val=10;
				QueryBuildManager.tooltipDelay=val;
				  
				ToolTipManager ttm = ToolTipManager.sharedInstance();
				ttm.setDismissDelay(val);					
	    		}				    	
	    	if(((String)key).equals("CanvasSize")){
	    		String resDel=new String(properties.getProperty("CanvasSize"));
				int val=0, count=0;
				for(int i=0;i<resDel.length();i++){
					if((resDel.charAt(i)>='0')&&(resDel.charAt(i)<='9'))val=val*10+(resDel.charAt(i)-'0');
					if(resDel.charAt(i)==','){
						count++;
						if(count==1)ControlFrame.initialX=val;
						if(count==2)ControlFrame.initialY=val;
						if((count==3)&&(val>0))QueryBuildManager.nCanvasWidth=val;
						if((count==4)&&(val>0))QueryBuildManager.nCanvasHeight=val;
						val=0;
					}
				}
				if((count==3)&&(val>0))QueryBuildManager.nCanvasHeight=val;			
	    		}				    	
		}
	}

	/**
	 * Writes property files.
	 */
	public void writePropertyFile()
	{ 
		Set keys;	
		Iterator iter;
		int removing=1;
		// remove all ArchiveConnectionProperties
		while(removing==1){
		keys=properties.keySet();
		iter = keys.iterator();
		removing=0;
	    while (iter.hasNext()&&(removing==0))
	    {
	    	Object key = iter.next();
	    	if(((String)key).startsWith("Archive")){
	    		properties.remove(key);
	    		removing=1;
	    	}
	    	if(((String)key).startsWith("ResultDisplay")){
	    		properties.remove(key);
	    		removing=1;
	    	}
	    }
		}
		
    	if(QueryBuildManager.resultDisplay==QueryBuildManager.REPLACE_WINDOW)setPropertyValue("ResultDisplay", "REPLACE_WINDOW");
    	if(QueryBuildManager.resultDisplay==QueryBuildManager.MULTIPLE_WINDOWS)setPropertyValue("ResultDisplay", "MULTIPLE_WINDOWS");
    	if(QueryBuildManager.resultDisplay==QueryBuildManager.TABBED_WINDOW)setPropertyValue("ResultDisplay", "TABBED_WINDOW");

    	if(QueryBuildManager.wildcardStyle==QueryBuildManager.SQL_STYLE)setPropertyValue("WildcardStyle", "SQL_STYLE");
    	if(QueryBuildManager.wildcardStyle==QueryBuildManager.WINDOWS_STYLE)setPropertyValue("WildcardStyle", "WINDOWS_STYLE");
    	
    	if(QueryBuildManager.CSVseparator==',')setPropertyValue("CSVStyle", "COMMA");
    	if(QueryBuildManager.CSVseparator==';')setPropertyValue("CSVStyle", "SEMICOLON");    	    
    	if(QueryBuildManager.CSVseparator=='\t')setPropertyValue("CSVStyle", "TAB");    	    
    	
    	setPropertyValue("TooltipDelay", ""+QueryBuildManager.tooltipDelay);
    	if(ControlFrame.QBM!=null){
    		int x=ControlFrame.thisJFrame.getX();
    		int y=ControlFrame.thisJFrame.getY();
    		if(x<0)x=0;
    		if(y<0)y=0;
    	setPropertyValue("CanvasSize", ""+x+","+y+","+(ControlFrame.QBM.getQueryCanvasWidth()-32)+","+(ControlFrame.QBM.getQueryCanvasHeight()+55));
    	}
		// create ArchiveConnectionProperties from archives	    
		keys = ControlFrame.archives.keySet();	    
	     iter = keys.iterator();
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();
	    	ArchiveInfo archiveInfo=(ArchiveInfo)ControlFrame.archives.get((String)key);
	    	setPropertyValue("ArchiveName_"+archiveInfo.name,  archiveInfo.name);
	    	setPropertyValue("ArchiveComputer_"+archiveInfo.name,  archiveInfo.computer);
	    	setPropertyValue("ArchivePort_"+archiveInfo.name,  archiveInfo.port);
	    	setPropertyValue("ArchiveDatabase_"+archiveInfo.name,  archiveInfo.database);
	    	setPropertyValue("ArchiveUser_"+archiveInfo.name,  archiveInfo.user);
	    	setPropertyValue("ArchivePassword_"+archiveInfo.name,  archiveInfo.password);
	    	if(archiveInfo.centerNodeName!=null)
	    		if(archiveInfo.centerNodeName.length()>0)
	    			setPropertyValue("ArchiveCenterNode_"+archiveInfo.name,  archiveInfo.centerNodeName);

	    	if(archiveInfo.useRMIquery) setPropertyValue("ArchiveRMI_"+archiveInfo.name,"true");
	    	else setPropertyValue("ArchiveRMI_"+archiveInfo.name,"false");
	    	
	    	if(archiveInfo.useLocalLanguage) setPropertyValue("ArchiveLocalLanguage_"+archiveInfo.name,"true");
	    	else setPropertyValue("ArchiveLocalLanguage_"+archiveInfo.name,"false");
	    	
	    	setPropertyValue("ArchiveColor_"+archiveInfo.name,  archiveInfo.color.toString());	    	
	    }

		try {
			String propComments =
				"ethnoArc Search Application Properties \r\n";
			
			properties.store(new FileOutputStream(propertyFile),propComments);
		} catch (IOException e) {
		}
	}
	/**
	 * Gets the value of the specified property.
	 * @param propName The name of the property.
	 * @return The value of the property.
	 */
	public String getPropertyValue(String propName)
	{
		return properties.getProperty(propName);
	}
	/**
	 * Gets the value of the specified property.
	 * @param propName The name of the property.
	 * @return The value of the property.
	 */
	public void setPropertyValue(String propName, String propValue)
	{
		properties.setProperty(propName, propValue);
	}
	
}
