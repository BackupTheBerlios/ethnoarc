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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileFilter;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.Dimension;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;


public class ControlFrame implements ActionListener, WindowListener, KeyListener, KeyEventDispatcher{
	public static JMenu menuDatabaseStructure; 
	public static Hashtable<String,ArchiveInfo> archives;
	public static ControlFrame thisFrame;
	public static JFrame thisJFrame;
    public static SearchAppPropertyManager appPropertyManager; 
    public static QueryBuildManager QBM; 
    public static String saveName=null; 
    private static int colorNum=0;  
    public static int initialX=0;  
    public static int initialY=0;  
    public static JButton searchButton;
    public static boolean warnBeforeExit=true;
    
    public  void init() {
    // prepare archive access table
        archives=new Hashtable<String,ArchiveInfo>();
    // read properties
    try{
    	appPropertyManager=SearchAppPropertyManager.getDBPropertyManagerInstant();
     } catch (Exception e) {}

    thisFrame = this;
    // create main application window
	JFrame frame = new JFrame("ethnoArc Multi Archive Search");
	thisJFrame=frame;
    frame.setSize(200,100);
    frame.setLocation(initialX, initialY);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//    Where the GUI is created:
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;
//    Create the menu bar.
    menuBar = new JMenuBar();
//    Build the first menu.
    menu = new JMenu("File");
    menu.getPopupMenu().setLightWeightPopupEnabled(false);
    menuBar.add(menu); 
    menuItem = new JMenuItem("New");    
    menuItem.setActionCommand("\1New");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuItem = new JMenuItem("Open File...");    
    menuItem.setActionCommand("\1Load");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuItem = new JMenuItem("Save");    
    menuItem.setActionCommand("\1Save");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuItem = new JMenuItem("Save As...");    
    menuItem.setActionCommand("\1SaveAs");
    menuItem.addActionListener(this);
    menu.add(menuItem);   
    menuItem = new JMenuItem("Options");    
    menuItem.setActionCommand("\1Options");
    menuItem.addActionListener(this);
    menu.add(menuItem);   
    menuItem = new JMenuItem("Quit");    
    menuItem.setActionCommand("\1Quit");
    menuItem.addActionListener(this);
    menu.add(menuItem);    
//  Build database access menu
    menuDatabaseStructure = new JMenu("Archive Structure");
    menuBar.add(menuDatabaseStructure);
    RebuildArchiveMenu();
//  Help/About menu    
    menu = new JMenu("Help");
    menu.getPopupMenu().setLightWeightPopupEnabled(false);
    menuBar.add(menu); 
    menuItem = new JMenuItem("About");    
    menuItem.setActionCommand("\1About");
    menuItem.addActionListener(this);
    menu.add(menuItem);

    //  Build search menu button
    menuBar.add(Box.createHorizontalGlue());
    searchButton = new JButton("Perform Search");
    menuBar.add(searchButton);
    searchButton.setActionCommand("\1Search");
    searchButton.addActionListener(this);	
    
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);               

    QBM = new QueryBuildManager ();
    JScrollPane scrollPane = new JScrollPane(  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
    scrollPane.setPreferredSize( new Dimension(QBM.getQueryCanvas().getWidth(),QBM.getQueryCanvas().getHeight()));
    scrollPane.setSize( new Dimension(QBM.getQueryCanvas().getWidth(),QBM.getQueryCanvas().getHeight()));
    //QBM.setScrollPane(scrollPane);
    //frame.getContentPane().add(QBM.getQueryCanvas());
    frame.setContentPane(QBM.getQueryCanvas());
    frame.setBackground( new Color(164, 159, 153  ));
        
    frame.setPreferredSize( new Dimension(QBM.getQueryCanvas().getWidth()+40,QBM.getQueryCanvas().getHeight()));
    frame.setSize( new Dimension(QBM.getQueryCanvas().getWidth()+40,QBM.getQueryCanvas().getHeight()));    
    frame.setJMenuBar(menuBar);
    frame.pack();
    frame.setVisible( true );    

  //  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    
    frame.addWindowListener(this);
    
    //searchButton.addKeyListener(this);
        
    (new Thread( QBM )).start();      
    }
    
	public static void addArchiveInfo(String name, String computer, String port, String database, String user, String password, boolean useRMIquery, boolean useLocalLanguage, Color color){
		ArchiveInfo archiveInfo = new ArchiveInfo();
		archiveInfo.name=new String(name);
		archiveInfo.computer=new String(computer);
		archiveInfo.port=new String(port);
		archiveInfo.database=new String(database);
		archiveInfo.user=new String(user);
		archiveInfo.password=new String(password);
		archiveInfo.useRMIquery=useRMIquery;
		archiveInfo.useLocalLanguage=useLocalLanguage;
		// cycle between multiple colours if none has been given
		// maybe we make this user selectable later
		if(color==null)	{
			if(colorNum>7)colorNum=0;
			if(colorNum==0)archiveInfo.color=new Color(0,0,128);
			if(colorNum==1)archiveInfo.color=new Color(150,0,56);
			if(colorNum==2)archiveInfo.color=new Color(0,128,0);
			if(colorNum==3)archiveInfo.color=new Color(128,0,0);
			if(colorNum==4)archiveInfo.color=new Color(194,83,56);
			if(colorNum==5)archiveInfo.color=new Color(128,0,128);
			if(colorNum==6)archiveInfo.color=new Color(191,56,0);
			if(colorNum==7)archiveInfo.color=new Color(114,128,148);
			colorNum++;
		}
		else archiveInfo.color=color;
		if(archives.containsKey(name))archives.remove(name);
		archives.put(name,archiveInfo);		
		QBM.UpdateElementColours("jdbc:mysql://"+archiveInfo.computer+":"+archiveInfo.port+"/"+archiveInfo.database,archiveInfo.color);
		RebuildArchiveMenu();
	}
	
	public static void setCenterNode(String URL, String DB, String nodeName){
	    Set keys = archives.keySet();	    
	    Iterator iter = keys.iterator();
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();
	    	ArchiveInfo archInf=archives.get((String)key);
	    	String dburl;
	    	if(archInf.useRMIquery)
	    		dburl="rmi://"+archInf.computer+":"+archInf.port+"/QueryServer";
	    	else
	    		dburl="jdbc:mysql://"+archInf.computer+":"+archInf.port+"/"+archInf.database;
	        if(dburl.equals(URL)&&archInf.database.equals(DB))
	        	archInf.centerNodeName=new String(nodeName);	    	
	    }
	}
		
	public static void deleteArchiveInfo(String name){
		if(archives.containsKey(name))archives.remove(name);
		RebuildArchiveMenu();
	}
	public static void RebuildArchiveMenu(){
//	  Build database access menu
		JMenuItem menuItem;
		menuDatabaseStructure.removeAll();		
	    
	    Set keys = archives.keySet();	    
	    Iterator iter = keys.iterator();
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();
	    	menuItem = new JMenuItem((String)key);
	    	ArchiveInfo archInf=archives.get((String)key);
	    	menuItem.setForeground(archInf.color);
		    menuDatabaseStructure.add(menuItem);
		    menuItem.addActionListener(thisFrame);
	    }
	    menuItem = new JMenuItem("Configure Archives...");
	    menuItem.setActionCommand("\1Add Archive...");
	    menuItem.addActionListener(thisFrame);
	    menuDatabaseStructure.add(menuItem);
	    menuDatabaseStructure.getPopupMenu().setLightWeightPopupEnabled(false);
	    appPropertyManager.writePropertyFile();
	}
	
	 private class QueryFilter extends FileFilter {

	        //Accept all directories and Query files.
	        public boolean accept(File f) {
	            if (f.isDirectory()) {
	                return true;
	            }
	            String extension = getExtension(f);
	            if (extension != null) {
	                if (extension.equalsIgnoreCase("query"))
	                        return true;
	                else {
	                    return false;
	                }
	            }
	            return false;	   
	        }

	        //The description of this filter
	        public String getDescription() {
	            return "Query files";
	        }
	        public  String getExtension(File f) {
	            String ext = null;
	            String s = f.getName();
	            int i = s.lastIndexOf('.');

	            if (i > 0 &&  i < s.length() - 1) {
	                ext = s.substring(i+1).toLowerCase();
	            }
	            return ext;
	        }
	 }

	 
	public static void saveArchiveInfoWithQuery(){
		File file;
		if(saveName==null)file = new File("ethnoArc.query");
		else file = new File( saveName);
	    JFileChooser fc = new JFileChooser();
		fc.setSelectedFile( file );
		fc.addChoosableFileFilter(thisFrame.new QueryFilter());
		int returnVal = fc.showSaveDialog(thisJFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	saveName=fc.getSelectedFile().getAbsolutePath();
        	saveArchiveInfo();
        }
	}
	public static void saveArchiveInfo(){
		//System.out.println("save data to "+saveName);
		// check whether this filename has some sort of extension
		// if it's missing, add ".query"
		String withoutPath;
		int nSPos=saveName.lastIndexOf("\\");
		if(saveName.indexOf("/")>nSPos)nSPos=saveName.lastIndexOf("/");
		if(nSPos>-1)withoutPath=saveName.substring(nSPos);
		else withoutPath=saveName;		
		if(withoutPath.indexOf(".")<=0)saveName=saveName+".query";
		// write the data to the save file
	 try{
	    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveName));
	    QBM.writeQueryElements(out);
	    out.flush();
	    out.close();
     } catch (Exception e) {}
	}
	
	public static void loadArchiveInfo(){
		File file;
		if(saveName==null)file = new File("ethnoArc.query");
		else file = new File( saveName);		
	    JFileChooser fc = new JFileChooser();
		fc.setSelectedFile( file );
		fc.addChoosableFileFilter(thisFrame.new QueryFilter());
		int returnVal = fc.showOpenDialog(thisJFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	saveName=fc.getSelectedFile().getAbsolutePath();
        }
        else return;       
		 try{
			 FileInputStream f = new FileInputStream(saveName);
			 ObjectInputStream in = new ObjectInputStream(f);
			 QBM.readQueryElements(in);
			 in.close();			 
		 } catch (Exception e) {System.err.println("Error reading file "+saveName+":"+e.getMessage());}
		
	}
	public void showAboutDialog (){
		 JOptionPane.showMessageDialog(null, 
				 "ethnoMARS "+EthnoMARS.Version+"\nA Multi-Archive Search tool for ethnomusicological archives\n(C)  FhI FOKUS 2007, 2008\n\n"+
				 "Will become available as OpenSource software in June 2008.\nMore about ethnoArc project at www.ethnoarc.org", 
				 "About ethnoMARS", JOptionPane.PLAIN_MESSAGE);
		 }

    public void actionPerformed(ActionEvent e) {
    	//System.err.println(e.getActionCommand());    	
    	if(e.getActionCommand().equals("\1Quit")){
    		appPropertyManager.writePropertyFile();
    		if(warnBeforeExit){
    			int n = JOptionPane.showConfirmDialog(
    				    null,
    				    "Do you really want to exit the application?",
    				    "Exit confirmation",
    				    JOptionPane.YES_NO_OPTION);
    			if(n==0)System.exit(0);    	
    			else return;
    			
    		}
    	   System.exit(0);    	
    	}
    	else if (e.getActionCommand().equals("\1Search")){
    		QBM.performSearch();
    	}
    	else if (e.getActionCommand().equals("\1New")){
    		QBM.clear();
    	}
    	else if (e.getActionCommand().equals("\1Save")){
    		if(saveName==null)saveArchiveInfoWithQuery();
    		else saveArchiveInfo();
    	}
    	else if (e.getActionCommand().equals("\1SaveAs")){
    		saveArchiveInfoWithQuery();
    	}
    	else if (e.getActionCommand().equals("\1Load")){
    		loadArchiveInfo();
    	}    	
    	else if (e.getActionCommand().equals("\1Options")){
    		OptionDialog  optionDialog = new OptionDialog();
    		optionDialog.start();      
    	}    	
        else if (e.getActionCommand().equals("\1Add Archive...")){
    		ArchiveInformationDialog  archiveInformationDialog = new ArchiveInformationDialog();
    		archiveInformationDialog.start();    		
    	}
    	else if (e.getActionCommand().equals("\1About")){
    		showAboutDialog();
    	}
    	else {
    		// check whether this is one of the available archives
    		if(archives.containsKey(e.getActionCommand())){
    			// retrieve and display archive structure
	    		ArchiveInfo archiveInfo=(ArchiveInfo)ControlFrame.archives.get(e.getActionCommand());
	    		String DBconnect;
	    		if(archiveInfo.useRMIquery)
	    			 DBconnect="rmi://"+archiveInfo.computer+":"+archiveInfo.port+"/QueryServer";
	    		else
	    			 DBconnect="jdbc:mysql://"+archiveInfo.computer+":"+archiveInfo.port+"/"+archiveInfo.database;
    			EthnoMARS dbStructure = new EthnoMARS();
    			
    			dbStructure.go(DBconnect, archiveInfo.name, archiveInfo.database,archiveInfo.user,archiveInfo.password,archiveInfo.useRMIquery,archiveInfo.useLocalLanguage, archiveInfo.color,e.getActionCommand(),archiveInfo.centerNodeName);    			
    		}
    	}
    }

	public void keyPressed(KeyEvent arg0) {
		System.err.println("keypressed "+arg0.getKeyCode());
		
	}

	public void keyReleased(KeyEvent arg0) {
		System.err.println("keyReleased "+arg0.getKeyCode());
		
	}

	public void keyTyped(KeyEvent arg0) {
		System.err.println("keyTyped "+arg0.getKeyCode());
		
	}
    // WindowListener routines - only needed to write property file on exit 
	public void windowActivated(WindowEvent arg0) {	}
	public void windowClosed(WindowEvent arg0) {}
	public void windowClosing(WindowEvent arg0) {
		appPropertyManager.writePropertyFile();
		if(warnBeforeExit){
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "Do you really want to exit the application?",
				    "Exit confirmation",
				    JOptionPane.YES_NO_OPTION);
			if(n==0)System.exit(0);    	
			else return;			
		}
		System.exit(0);
	}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}

	// key dispatcher to handle 'enter' to perform search
	 public boolean dispatchKeyEvent(KeyEvent ke) {		
		 // perform search on ENTER
		 if((ke.getKeyCode()==10)&&(ke.getID()==401)&&thisJFrame.isFocused()){   
			 QBM.performSearch();
			 return true;		 
		 }
		 // delete current item on CTRL-DEL
		 if((ke.getKeyCode()==127)&&(ke.getID()==401)&&(ke.getModifiers()==2)&&thisJFrame.isFocused()){   
			 QBM.removeSelectedElement();
			 return true;		 
		 }
		 // add connecting result field on CTRL-INS
		 if((ke.getKeyCode()==155)&&(ke.getID()==401)&&(ke.getModifiers()==2)&&thisJFrame.isFocused()){   
			 QBM.AddConnectedField();
			 return true;		 
		 }

		 //System.err.println(ke.getKeyCode()+ " "+ke.getID()+" "+ke.getModifiers());
		return false;		 
	 }
}

