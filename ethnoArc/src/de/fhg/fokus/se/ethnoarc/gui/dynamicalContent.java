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

import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.table.JTableHeader;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.DBTable;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;

/**
 * $Id: dynamicalContent.java,v 1.4 2008/07/02 09:58:40 fchristian Exp $ 
 * @author fokus
 */
public class dynamicalContent{
	
	private static DBHandling dbHandle;
	private static DBStructure dbStructure;
	private static int START_X = 10;
	private static int START_Y = 10;
	private static int ROW_HEIGHT = 16;
	private static int HEADER_HEIGHT = 24;
	private static JScrollPane jScrollPane;
	private static NewJFrame parent;
	
	static public void getOtherStuff(NewJFrame org, JScrollPane jScrollPane1){
		try{
			jScrollPane = jScrollPane1;
			parent = org;
//			jScrollPane = jScrollPane1;
//			JButton jButton1 = new JButton();
//			jButton1.setBounds(10, 10, 30, 30);
//			jScrollPane.add(jButton1);
//			jButton1.setText("jButton1");
			parseDB();
		}catch (Exception e) {
			
		}

	}
	private static void parseDB() {
		try {
			dbHandle = new DBHandling( DBConstants.DBURL,
					DBConstants.DBUSERNAME, DBConstants.DBPASSWORD);
			dbStructure = dbHandle.getDBStructure();
			getCombinedTables(dbStructure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getCombinedTables(DBStructure dbStructure) {
		Hashtable <String,DBTable> combinedTables = dbStructure.getCombinedTables();
		
		for (DBTable combinedTable : combinedTables.values()) {
			Vector columnName = new Vector();
			columnName.add(combinedTable.getTableName());
			Hashtable <String,DBTableElement> subtables  = combinedTable.getRelatedTables();
			Vector rowData = new Vector();
			for(DBTableElement subtable : subtables.values()){
				Vector data = new Vector();
				data.add(subtable.getNameDB());
				rowData.add(data);
			}
			paintTable(columnName,rowData);
		}
	}
	
	
	private static void paintTable(Vector columnName, Vector rowData){ 
        JTable table = new JTable( rowData, columnName );
        JScrollPane scrollpane = new JScrollPane(table);
        int X_SPACE = 10;
        scrollpane.setBounds(START_X,START_Y,100,ROW_HEIGHT*rowData.size()+HEADER_HEIGHT);
        JTableHeader header = table.getTableHeader();
        header.addMouseMotionListener(parent);
        header.addMouseListener(parent);
        table.getTableHeader().setReorderingAllowed( false );
        table.getTableHeader().setResizingAllowed( false );
        table.getTableHeader().setName((String)columnName.get(0));
        table.setName((String)columnName.get(0));

        scrollpane.addMouseListener(parent);
        scrollpane.addMouseMotionListener(parent);
        scrollpane.setName((String)columnName.get(0));
        START_X += scrollpane.getWidth() + X_SPACE;
        jScrollPane.add(scrollpane);
        //START_Y += ROW_HEIGHT*rowData.size()+HEADER_HEIGHT;
	}

}
