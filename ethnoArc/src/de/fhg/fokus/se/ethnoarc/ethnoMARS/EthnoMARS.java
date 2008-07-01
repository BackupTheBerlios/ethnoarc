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
import hypergraph.graphApi.GraphSystem;
import hypergraph.graphApi.GraphSystemFactory;
import hypergraph.graphApi.Node;
import hypergraph.graphApi.Group;
import hypergraph.graphApi.Edge;
import hypergraph.graphApi.algorithms.GraphUtilities;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import hypergraph.visualnet.GraphSelectionEvent;
import hypergraph.visualnet.GraphSelectionListener;
import hypergraph.visualnet.GraphSelectionModel;

import java.rmi.Naming;

import java.util.Hashtable;
import java.util.Iterator;

import java.awt.Color;
//import java.awt.Image;
//import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.ImageIcon;

import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.ethnoMARS.GraphPanel;
import de.fhg.fokus.se.ethnoarc.queryServer.QueryServerInterface;

public class EthnoMARS implements GraphSelectionListener {
	
    public  static  String Version = "V2.108"; 
    
	public void valueChanged(GraphSelectionEvent e) {
		GraphSelectionModel gsm = (GraphSelectionModel) e.getSource();
		Iterator i = gsm.getSelectionElementIterator();
		if (i.hasNext()) {
			//Node node = (Node) i.next();
			//AttributeManager attrMgr = graph.getAttributeManager();
//			System.out.println(href);
		}
	}
	public static void main(String[] args) {
		ControlFrame controlFrame = new ControlFrame();

		Logger.getLogger(EthnoMARS.class.getName());
		PropertyConfigurator.configure("log/ethnoarcLog.properties");
		controlFrame.init();

	}
	public void go(String DBURL, String DBGIVENNAME, String DBNAME, String DBUSERNAME, String DBPASSWORD, boolean useRMI, boolean useLocalLanguage, Color DBCOL, String DBMENUNAME, String DBCENTER) {
		JFrame frame = new JFrame();
		Node TreeNode = null;
		Node InitalCenter = null;
		Hashtable <String,EADBDescription> originalTables = null;
		DBStructure dbStructure=null;
		DBHandling dbHandle=null;	 
		Graph graph;		    

		UIManager.put("ModelPanelUI","hypergraph.hyperbolic.ModelPanelUI");
		//UIManager.put("ModelPanelUI","hypergraph.hyperbolic.HalfPlanePanelUI");
		GraphSystem graphSystem = null;
		try {
			graphSystem = GraphSystemFactory.createGraphSystem("hypergraph.graph.GraphSystemImpl",null);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(8);
		}
		if(useRMI){
			try{
				QueryServerInterface QSI = (QueryServerInterface)Naming.lookup(DBURL);			
				dbStructure=QSI.getDBStructure(DBNAME);
				originalTables = dbStructure.getTables();
			}
			catch(Exception ex)
			{
				System.out.println("Remote query server access exception = " + ex ) ;
				ex.printStackTrace();
				ControlFrame.QBM.showOberlayMessage("Can't connect to "+DBURL,10000);
				return;								
			}

		}
		else { // use direct SQL connection
			try {			
				dbHandle = new DBHandling(DBURL, DBUSERNAME, DBPASSWORD, true);			
				dbStructure = dbHandle.getDBStructure();					
				originalTables = dbStructure.getTables();		
			} catch (Exception e) {
				e.printStackTrace();
				ControlFrame.QBM.showOberlayMessage("Can't connect to "+DBURL,10000);					
				return;
			}
		}
		graph = GraphUtilities.createTree(graphSystem,0,0);			
		graph.removeAll();
		Group gRoot=null;
		Group gNoValue=null;
		Group gRootEdges=null;
		Group gContains=null;
		Group gAlternative=null;
		Group gReference=null;
		Group gAlternativeLanguage=null;
		Group gSelected = null;

		try{
			gRoot = graph.createGroup( "RootNodes");
			gNoValue = graph.createGroup( "NoValueNodes");
			gRootEdges = graph.createGroup( "RootEdges");
			gContains = graph.createGroup( "ContainsEdges");
			gAlternative = graph.createGroup( "AlternativeEdges");
			gReference = graph.createGroup( "ReferenceEdges");
			gAlternativeLanguage = graph.createGroup( "AlternativeLanguageEdges");
			gSelected = graph.createGroup( "Selected");        		
			TreeNode = graph.createNode("");
		}catch(Exception e){System.out.println(e);}



		Hashtable <String, Node>ethnoArcNodes = new Hashtable<String, Node>();
		Hashtable ethnoArcRootNodes = determineRootNodes(originalTables);

		// create nodes				
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				try {
					Node node = graph.createNode(originalTable.getNameDB());					
					if(originalTable.getNameDB().equals(DBCENTER)) InitalCenter=node;

//					System.out.println("Creating node: "+originalTable.getNameDB());		
					node.setLabel(originalTable.getDisplayname());
					if(originalTable.getEnglishDescription()!=null){
						graph.getAttributeManager().setAttribute("EDescription", node, originalTable.getEnglishDescription());
						graph.getAttributeManager().setAttribute("ShowDescription", node, originalTable.getEnglishDescription());
					}
					if(originalTable.getDescription()!=null){
						graph.getAttributeManager().setAttribute("Description", node, originalTable.getDescription());
						if(useLocalLanguage)graph.getAttributeManager().setAttribute("ShowDescription", node, originalTable.getDescription());
					}											
					
					if(originalTable.getFirstContent()!=null)graph.getAttributeManager().setAttribute("Example", node, originalTable.getFirstContent());
					// figure out if this is a root node...
					if((ethnoArcRootNodes.get(originalTable.getNameDB())).equals("R")){
						node.setGroup(gRoot);							
						Edge edge = graph.createEdge( TreeNode, node);
						edge.setGroup(gRootEdges);						
					}
					if(originalTable.getNoValue()){
						node.setGroup(gNoValue);
						graph.getAttributeManager().setAttribute("NoValue", node, "true");			    			
					}
					else graph.getAttributeManager().setAttribute("NoValue", node, "false");

					ethnoArcNodes.put(originalTable.getNameDB(),node);
					//System.out.println(node.toString());						
				}catch(Exception e){System.out.println("Node creation failure");}
			}
		}
		// create edges				
		for (EADBDescription originalTable : originalTables.values()) {
			if( (originalTable.getType().equals("Contains"))||
					(originalTable.getType().equals("AlternativeLanguage"))||
					(originalTable.getType().equals("TakesReferenceFrom"))||
					(originalTable.getType().equals("Alternative")))
				try{
					Node node1 =(Node) ethnoArcNodes.get(originalTable.getFirstTable());
					Node node2 = (Node) ethnoArcNodes.get(originalTable.getSecondTable());					
					if(!(node1.getName().equals(node2.getName()))){ // hypergraph doesn't like self-references, event though we allow this in the database							                    
						Edge edge =  graph.createEdge( originalTable.getNameDB(),node1, node2);
//						System.out.println("Creating edge: "+node1.getLabel()+" "+node2.getLabel());		

						edge.setLabel(originalTable.getType());
						if(originalTable.getEnglishDescription()!=null)graph.getAttributeManager().setAttribute("Relation", edge, node1.getLabel()+"\n"+originalTable.getType()+"\n"+node2.getLabel());
						if(originalTable.getType().equals("Contains"))edge.setGroup(gContains);
						if(originalTable.getType().equals("AlternativeLanguage"))edge.setGroup(gAlternativeLanguage);
						if(originalTable.getType().equals("Alternative"))edge.setGroup(gAlternative);
						if(originalTable.getType().equals("TakesReferenceFrom"))edge.setGroup(gReference);
					}
					//System.out.println(edge.toString());						
				}catch(Exception e){System.err.println("Edge creation error: "+e);}
		}

		// finf description if available
		String DBdescription="";
		String DBdescription_english="";
		if(dbStructure.getTableByTableName("introduction")!=null)
			DBdescription=dbStructure.getTableByTableName("introduction").getDescription();
		if(dbStructure.getTableByTableName("englishintroduction")!=null)
			DBdescription_english=dbStructure.getTableByTableName("englishintroduction").getDescription();

		GraphPanel graphPanel = new GraphPanel(graph, frame);
		
		if(InitalCenter==null)
				graphPanel.setCenterNode(TreeNode);
		else 
			graphPanel.setCenterNode(InitalCenter);
		
		graphPanel.setSaveName(DBURL);	    	
		graphPanel.setDBinfo(DBURL, DBNAME, DBUSERNAME, DBPASSWORD, DBMENUNAME, DBdescription, DBdescription_english, useRMI, DBCOL, DBCENTER);    	
		graphPanel.setDatabaseTables(originalTables, dbStructure.getTableByTableName("language").getDescription());
		graphPanel.setNodes(ethnoArcNodes);
		graphPanel.setSelectedGroup(gSelected);
		ImageIcon imi = new javax.swing.ImageIcon("res/images/ethnoarc_logo.png");
		graphPanel.setLogo(imi.getImage());
		graphPanel.setSmallLogo(imi.getImage());

		graph.getAttributeManager().setAttribute(GraphPanel.NODE_BACKGROUND, gSelected,new Color(190,190,190));

		graph.getAttributeManager().setAttribute(GraphPanel.NODE_FOREGROUND, gRoot,new Color(114,146,71));
		graph.getAttributeManager().setAttribute(GraphPanel.NODE_FOREGROUND, gNoValue,new Color(200,100,100));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINEWIDTH, graph, new Float(2));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, graph, new Color(200,20,20));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, gContains, new Color(200,200,200));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, gAlternative, new Color(200,100,200));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, gReference, new Color(100,200,100));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, gAlternativeLanguage, new Color(200,200,100));

		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINEWIDTH, gRootEdges, new Float(0));
		graph.getAttributeManager().setAttribute(GraphPanel.EDGE_LINECOLOR, gRootEdges, Color.WHITE);

		graph.getAttributeManager().setAttribute(GraphPanel.NODE_FOREGROUND, TreeNode,Color.WHITE);
		graph.getAttributeManager().setAttribute(GraphPanel.NODE_BACKGROUND, TreeNode,Color.WHITE);

//		graphPanel.getPropertyManager().setProperty("visualnet.layout.useExpander",true);
		//graphPanel.getPropertyManager().setProperty("visualnet.layout.initiallyExpanded",1);
		//graphPanel.getPropertyManager().setProperty("visualnet.GenericMDSLayout.repulsingForce",new Double(0.1));
		//graphPanel.getPropertyManager().setProperty("visualnet.GenericMDSLayout.repulsingForceCutOff",new Double(1.01));
		//graphPanel.getPropertyManager().setProperty("visualnet.GenericMDSLayout.connectedDisparity",new Double(0.01));
		//graphPanel.setGraphLayout(new TreeLayout(graph, graphPanel.getModel(), graphPanel.getPropertyManager()));
		//graphPanel.setGraphLayout(new GenericMDSLayout(null, graphPanel.getModel(), graph, graphPanel.getPropertyManager()));
		graphPanel.getSelectionModel().addSelectionEventListener(this);	    		    	

		frame.getContentPane().add(graphPanel);
		frame.setSize(500,500);
		frame.setTitle(DBGIVENNAME);
		//frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		if(InitalCenter!=null)graphPanel.centerNode(InitalCenter);
	}

	private Hashtable determineRootNodes(Hashtable <String,EADBDescription> originalTables ){
		Hashtable<String,String> resultTable= new Hashtable<String,String>();
		Boolean bIsRoot, bChangeOccured;
		int minVal;
		String minStr;
		// create tables with all nodes  			
		for (EADBDescription originalTable : originalTables.values()) {
			if(originalTable.getType().equals("object")){
				resultTable.put(originalTable.getNameDB()," ");

				// and determine whether this is a true root node (no pointers toward them)
				bIsRoot=true;
				for (EADBDescription searchTable : originalTables.values()) {
					if( ((searchTable.getType().equals("Contains"))||
							(searchTable.getType().equals("TakesReferenceFrom"))||
							(searchTable.getType().equals("AlternativeLanguage"))||
							(searchTable.getType().equals("Alternative")))&&
							searchTable.getSecondTable().equals(originalTable.getNameDB())){
						if(!searchTable.getSecondTable().equals(searchTable.getFirstTable()))
							bIsRoot=false;								
					}
				}
				if(bIsRoot)resultTable.put(originalTable.getNameDB(),"R");
			}
		}
		while(true){
			// now mark all nodes that can be reached by the root nodes
			bChangeOccured=true;
			while(bChangeOccured){
				bChangeOccured=false;
				for (EADBDescription searchTable : originalTables.values()) {
					if( ((searchTable.getType().equals("Contains"))||
							(searchTable.getType().equals("AlternativeLanguage"))||
							(searchTable.getType().equals("TakesReferenceFrom"))||
							(searchTable.getType().equals("Alternative"))))
						if(!searchTable.getSecondTable().equals(searchTable.getFirstTable())){
							if((resultTable.get(searchTable.getSecondTable())).equals(" "))
								if(!(resultTable.get(searchTable.getFirstTable()).equals(" "))){
									resultTable.put(searchTable.getSecondTable(),"C");
									bChangeOccured=true;
								}	       				
						}
				}        		
			}
			// find lowest entry that still has a space (unreachable from current root elements)
			minVal=-1;
			minStr="";
			Iterator it = resultTable.keySet().iterator();
			while( it.hasNext() ) {
				String key= (String)it.next();
				if(resultTable.get(key).equals(" ")){
					EADBDescription unreachedTable=originalTables.get(key);
					if(minVal==-1){
						minVal=unreachedTable.getOrderNumber();
						minStr=new String(key);
					}
					else if (unreachedTable.getOrderNumber()<minVal){
						minVal=unreachedTable.getOrderNumber();
						minStr=new String(key);
					}
				}
			}
			if(minVal==-1)return resultTable;
			resultTable.put(minStr,"R");
		}
	}
	
}