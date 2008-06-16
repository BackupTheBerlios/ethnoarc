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
package de.fhg.fokus.se.ethnoarc.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.common.SearchObject;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription.TableReference;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame;

/**
 * $Id: DisplayDBStructureGUI.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus
 */
public class DisplayDBStructureGUI extends JFrame implements MouseInputListener{

	/** The logger */
	static Logger logger = Logger.getLogger(DisplayDBStructureGUI.class.getName());
	/**
	 * Initialises the logger.
	 */
	private static void initLog()
	{
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		logger.debug("LOGGER INITIALISED");
	}
	
	private static DisplayDBStructureGUI shell;
	private static Container contentPane;
	private static JFrame frame;
	private static DBHandling dbHandle;
	private static DBSqlHandler dbSQLHandle;
	private static DBStructure dbStructure;
	private static int START_X = 10;
	private static int START_Y = 10;
	private static int ROW_HEIGHT = 16;
	private static int HEADER_HEIGHT = 24;
	private static int WINDOWS_HEADLINE_HEIGHT = 30;
	
	public DisplayDBStructureGUI(){
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
	}
	
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
    	initLog();
		try {
			UIManager.setLookAndFeel(
				"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
		}
        //Create and set up the window.
        shell.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        shell.setSize(600,400);
        shell.setTitle("DB Display");
        contentPane = shell.getContentPane();
        contentPane.setLayout(null);
        //Display the window.
        //frame.pack();
        shell.setVisible(true);
    }
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DrawingArea shell = new DrawingArea();
		//init expert interface
		shell = new DisplayDBStructureGUI();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        parseDB();
	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
        Object sourceO = arg0.getSource();
        JScrollPane source = null;
        if (!(sourceO instanceof JScrollPane)) {
        	if(sourceO instanceof JTableHeader){
        		JTableHeader tableHeader = (JTableHeader)sourceO;
        		for(int i=0;i<contentPane.getComponentCount();++i){
        			Component c = contentPane.getComponent(i);
        			if(c.getName().equals(tableHeader.getName())){
        				source = (JScrollPane) c;
        			}
        		}     		
        	}else{
        		return;
        	}
        }else{
        	source = (JScrollPane) sourceO;
        }
        setNewLocation(source, source.getX()+arg0.getX(),source.getY()+arg0.getY());
	}

	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	private void setNewLocation(JScrollPane source, int newX, int newY){
		//System.out.println("Full heigh="+this.getHeight() +" newYPos="+(newY+source.getHeight()));
		 if( newX<=0 || newY<=0 || ( (newY+source.getHeight())>=(this.getHeight()- WINDOWS_HEADLINE_HEIGHT)) || (newX+source.getWidth()>=this.getWidth()) ){
			 return;
		 }
		 source.setLocation(newX,newY);
	}
	
	private static void parseDB() {
		try {
//			dbHandle = new DBHandling(DBConstants.DBURL,
//					DBConstants.DBUSERNAME, DBConstants.DBPASSWORD);
//			dbSQLHandle = new DBSqlHandler(DBConstants.DBDRIVER, DBConstants.DBURL, DBConstants.DBUSERNAME, DBConstants.DBPASSWORD);
//			dbStructure = dbHandle.getDBStructure();
			
			DBHandling dummy = new DBHandling("jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_ief2", DBConstants.DBUSERNAME, DBConstants.DBPASSWORD);
//			DBHandling dummy = new DBHandling("jdbc:mysql://bruce.fokus.fraunhofer.de:3306/ethnoarc_ief", DBConstants.DBUSERNAME, DBConstants.DBPASSWORD);
			DBStructure dummyStrc = dummy.getDBStructure();		
			getCombinedTables(dummyStrc);
			
			
//			EADBDescription testTable = dummyStrc.getTableByTableName("Anrede");
//			List<TableReference> liste = testTable.getReferredTables();
//			for (TableReference reference : liste) {
//				System.out.println(testTable.getNameDB()+" ERGEBNISS:"+reference.getReferencedTable().getNameDB());
//			
//			}
			//TEST
//			Vector <String> fields = new Vector<String>();
//			fields.add("Adressenliste.Adresse.Person.Nachname");
//			fields.add("Adressenliste.Adresse.Person.Vorname");
//			fields.add("Adressenliste.Adresse.Postleitzahl");
//			fields.add("Adressenliste.Adresse.Strasse_032und_032Hausnummer");
//			fields.add("Adressenliste.Adresse.Stadt");
//			fields.add("Adressenliste.Adresse.Person.Anrede");
//			fields.add("Adressenliste.Adresse.Person.Titel");
//			fields.add("Nachname");
//			fields.add("Vorname");
//			fields.add("Postleitzahl");
//			fields.add("Strasse_032und_032Hausnummer");
//			fields.add("Stadt");
//			fields.add("Anrede");
//			fields.add("Titel");
//			SearchObject searchObject = dbSQLHandle.getSearchObject(dbStructure, fields);
//			for(int i = 0; i < searchObject.getSearchFields().size();++i){
//				System.out.println(searchObject.getSearchFields().get(i));
//			}
//			fields.add("Person");
//			fields.add("Adressenliste");

//			dbSQLHandle.getViewName(dbStructure, fields);

//			dbSQLHandle.createMaxTable();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private static void getCombinedTables(DBStructure dbStructure) {
		Hashtable <String,DBTable> combinedTables = dbStructure.getCombinedTables();
		paseThroughCBTables(combinedTables);

	}
	
	private static void paseThroughCBTables(Hashtable <String,DBTable> combinedTables ){
		for (DBTable combinedTable : combinedTables.values()) {
			Vector columnName = new Vector();
			columnName.add(combinedTable.getTableName());
			Hashtable <String,DBTableElement> subtables  = combinedTable.getRelatedTables();
			Vector rowData = new Vector();

			//special CB table fields
			for(DBTable cbtable : combinedTable.getChildCBTableList().values()){
				Vector data = new Vector();
				data.add(cbtable.getTableName());
				rowData.add(data);
			}
			//fields which contains content
			for(DBTableElement subtable : subtables.values()){
				Vector data = new Vector();
				data.add(subtable.getNameDB());
				rowData.add(data);
			}
			paintTable(columnName,rowData);
			
			//parse ChildCbtables if available
			if(combinedTable.getChildCBTableList().size() > 0){
				paseThroughCBTables(combinedTable.getChildCBTableList());
			}
		}
		System.out.println("Display finished");
	}
	
	private static void paintTable(Vector columnName, Vector rowData){ 
        JTable table = new JTable( rowData, columnName );
        JScrollPane scrollpane = new JScrollPane(table);
        int X_SPACE = 10;
        scrollpane.setBounds(START_X,START_Y,100,ROW_HEIGHT*rowData.size()+HEADER_HEIGHT);
        JTableHeader header = table.getTableHeader();
        header.addMouseMotionListener(shell);
        header.addMouseListener(shell);
        table.getTableHeader().setReorderingAllowed( false );
        table.getTableHeader().setResizingAllowed( false );
        table.getTableHeader().setName((String)columnName.get(0));
        table.setName((String)columnName.get(0));
        scrollpane.addMouseListener(shell);
        scrollpane.addMouseMotionListener(shell);
        scrollpane.setName((String)columnName.get(0));
        START_X += scrollpane.getWidth() + X_SPACE;
        contentPane.add(scrollpane);
        //START_Y += ROW_HEIGHT*rowData.size()+HEADER_HEIGHT;
	}
}
