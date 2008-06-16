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
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;
import javax.swing.table.JTableHeader;


public class NewJFrame extends javax.swing.JFrame implements MouseInputListener{
	private JMenuBar jMenuBar1;
	private JMenu jMenu1;
	private JToolBar jToolBar1;
	private JScrollPane jScrollPane1;
	private JButton jButton1;
	private static NewJFrame inst;
	private static int WINDOWS_HEADLINE_HEIGHT = 30;

	public static void main(String[] args) {
		inst = new NewJFrame();
		inst.setVisible(true);
		inst.addMouseListener(inst);
		inst.addMouseMotionListener(inst);
	}
	
	public NewJFrame() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			{
				jToolBar1 = new JToolBar();
				BoxLayout jToolBar1Layout = new BoxLayout(
					jToolBar1,
					javax.swing.BoxLayout.Y_AXIS);
				jToolBar1.setLayout(jToolBar1Layout);
				getContentPane().add(jToolBar1);
				jToolBar1.setBounds(0, 0, 28, 245);
				jToolBar1.setFloatable(false);
				{
					jButton1 = new JButton();
					jToolBar1.add(jButton1);
					jButton1.setText("jButton1");
				}
			}
			{
				jScrollPane1 = new JScrollPane();
				getContentPane().add(jScrollPane1);
				jScrollPane1.setBounds(28, 0, 364, 245);
				jScrollPane1.getVerticalScrollBar().setBounds(0, 0, 0, 0);
				jScrollPane1.getHorizontalScrollBar().setBounds(0, 0, 0, 0);
				jScrollPane1.setLayout(null);
			}
			{
				jMenuBar1 = new JMenuBar();
				setJMenuBar(getJMenuBar1());
				{
					jMenu1 = new JMenu();
					jMenuBar1.add(jMenu1);
					jMenu1.setText("jMenu1");
				}
			}
			dynamicalContent.getOtherStuff(inst, jScrollPane1);
			pack();
			setSize(400, 300);
		} catch (Exception e) {
			
		}
	}
	
	public JMenuBar getJMenuBar1() {
		return jMenuBar1;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
        Object sourceO = arg0.getSource();
        JScrollPane source = null;
        if (!(sourceO instanceof JScrollPane)) {
        	if(sourceO instanceof JTableHeader){
        		JTableHeader tableHeader = (JTableHeader)sourceO;
        		for(int i=0;i<jScrollPane1.getComponentCount();++i){
        			Component c = jScrollPane1.getComponent(i);
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

}
