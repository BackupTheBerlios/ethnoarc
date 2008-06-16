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

import hypergraph.graphApi.Graph;
import hypergraph.graphApi.Node;
import java.util.Hashtable;
import java.util.Iterator;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


public class DescriptionPresenter implements ListSelectionListener  {
	Hashtable <String, Node> ethnoArcNodes;
	Graph graph;
	JList list;
	JTextField searchField;
	String searchString ="";
	int posx, posy;
	GraphPanel graphPanel;
	Timer timer;
	JFrame frame;
	String DBMENUNAME= "";
	
	
	public DescriptionPresenter (int posx, int posy, Graph graph,GraphPanel graphPanel,Hashtable <String, Node> nodes, String DBMENUNAME){
		ethnoArcNodes=nodes;
		this.graph=graph;
		this.posx=posx;
		this.posy=posy;
		this.graphPanel=graphPanel;
		this.DBMENUNAME=DBMENUNAME;
	}

	public  void run(){
        String frametitle=DBMENUNAME+": Search node descriptions ";
	    Frame[] frames = Frame.getFrames();
	    
	    // if this frame already exists, don't open a new one
	    for (int i=0; i<frames.length; i++) 
	      if (frames[i].getTitle().equals(frametitle))
	    	  if (frames[i].isDisplayable()){
	    		  frames[i].setState(Frame.NORMAL);
	    	  frames[i].transferFocus();
	    	  return;	    	  
	      }	
		
	    frame = new JFrame(frametitle);
		frame.setLocation(posx,posy);
		frame.setSize(400,400);

		//Create the list and put it in a scroll pane.
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(10);
		list.addListSelectionListener(this);		
		JScrollPane listScrollPane = new JScrollPane(list);

		searchField = new JTextField(10);

		searchField.addActionListener( 
				new ActionListener() {
					public void actionPerformed( ActionEvent e ) {						
						if(!searchString.equals(searchField.getText())){
							searchString = searchField.getText();
							System.err.println("Search string now "+searchString);
							fillList();
						}						 
					}
				}
		);		
		searchField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void insertUpdate( DocumentEvent e ) {update();}
					public void removeUpdate( DocumentEvent e ) {update();}
					public void changedUpdate( DocumentEvent e ) {update();}
					public void update() {
						if(!searchString.equals(searchField.getText())){
							searchString = searchField.getText();
							fillList();
						}		
					}
				}
		);		

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane,
				BoxLayout.LINE_AXIS));
		buttonPane.add(new JLabel ("Search filter: "));
		buttonPane.add(searchField);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		fillList();
		frame.add(listScrollPane, BorderLayout.CENTER);
		frame.add(buttonPane, BorderLayout.PAGE_END);
		//Display the window.
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		frame.requestFocus();
		
	    timer = new Timer(500, null);
		timer.setInitialDelay(200);
		timer.addActionListener(
				// making sure that search window closes once structure window is closed
				new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						if(frame==null){
							  timer.stop();
							  timer=null;
						}
						else if(!frame.isDisplayable()){
							  timer.stop();
							  timer=null;
						}
						if(graphPanel==null){
							  timer.stop();
							  timer=null;
							  frame.dispose();
							}
						else if(!graphPanel.isDisplayable()){
							  timer.stop();
							  timer=null;
							  frame.dispose();
							}
					}
				}
		);		
		timer.start();
	}
	public void fillList(){	       
		DefaultListModel listModel;
		listModel = new DefaultListModel();
		Iterator it = ethnoArcNodes.keySet().iterator();
		while( it.hasNext() ) {
			String key= (String)it.next();
			Node node = ethnoArcNodes.get(key);
			String showDescription= (String)graph.getAttributeManager().getAttribute("ShowDescription",node);
			if(showDescription.toLowerCase().length()>0)
			if(showDescription.toLowerCase().contains(searchString.toLowerCase()))				
				addSortedElement(listModel,showDescription);
		}
		list.setModel(listModel);
	}
	   public void valueChanged(ListSelectionEvent e) {
	        if (e.getValueIsAdjusting() == false) {
                int selIndex=list.getSelectedIndex();
	            if (selIndex != -1) {
	            	// first check for multiple occurences of the same description and
	            	// figure out which one we clicked
	            	int dupliCount=0;
	            	String selectionString=(String)list.getSelectedValue();
	            	for(int i=0;i<list.getSelectedIndex();i++){
	            		if(((String)list.getModel().getElementAt(i)).equals(selectionString))dupliCount++;	            		
	            	}
	            	// parse through the node list to find proper node
	            	Iterator it = ethnoArcNodes.keySet().iterator();
	            	int count=-1;
	        		while( it.hasNext() ) {
	        			String key= (String)it.next();
	        			Node node = ethnoArcNodes.get(key);
	        			String showDescription= (String)graph.getAttributeManager().getAttribute("ShowDescription",node);
	        			if(showDescription.equalsIgnoreCase(selectionString)){
	        				count++;
	        				if(count==dupliCount){
	        					if(graphPanel.lastSelectedNode!=null){
	        						graphPanel.lastSelectedNode.setGroup(graphPanel.lastSelectedNodeGroup);	        				
	        						
	        					}
	        					graphPanel.lastSelectedNode=node;
	        					graphPanel.lastSelectedNodeGroup=node.getGroup();	        					
	        					node.setGroup(graphPanel.gSelected);
	        					graphPanel.centerNode(node);
	        				}
	        			}
	        		}	            	
	            }
	        }
	    }
		public void addSortedElement(DefaultListModel listModel, String description){
			if(listModel.getSize()==0){
				listModel.addElement(description);
				return;
			}
			for(int i=0;i<listModel.getSize();i++){
				if(((String)listModel.get(i)).compareToIgnoreCase(description)>0){
					listModel.insertElementAt(description,i);
					return;
				}
			}
			listModel.addElement(description);
		}

}
