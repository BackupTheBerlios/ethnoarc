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

import hypergraph.graphApi.Element;
import hypergraph.graphApi.Graph;
import hypergraph.graphApi.Group;
import hypergraph.graphApi.Node;
import java.util.Hashtable;

import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JToolTip;
import javax.swing.filechooser.FileFilter;

import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;

public class GraphPanel extends hypergraph.visualnet.GraphPanel implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private Graph graph;

	private JFrame frame;

	private Node centerNode;

	private String saveName;

	private Hashtable databaseTables;

	Hashtable<String, Node> ethnoArcNodes;

	private String countryCode;

	private Node lastClickedNode;

	public Node lastSelectedNode = null;

	public Group lastSelectedNodeGroup;

	public Group gSelected;

	private int noNodeClick = 1;

	private String DBURL = "";

	private String DBNAME = "";

	private String DBUSERNAME = "";

	private String DBPASSWORD = "";

	private String DBMENUNAME = "";

	private String DBDESCRIPTION = "";

	private String DBDESCRIPTIONE = "";

	private String DBDESCRIPTIONO = "";

	private String DBCENTER = "";

	private boolean DBRMI = false;

	private Color DBCOLOR = new Color(0, 0, 0);

	public GraphPanel(Graph graph, JFrame frame) {
		super(graph);
		this.graph = graph;
		this.frame = frame;
	}

	public JToolTip createToolTip() {
		return new JMultiLineToolTip();
	}

	public void setCenterNode(Node node) {
		centerNode = node;
		if (centerNode.getName().length() > 0)
			ControlFrame.setCenterNode(DBURL, DBNAME, centerNode.getName());
	}

	public void setDatabaseTables(Hashtable dbTables, String code) {
		databaseTables = dbTables;
		countryCode = code;
	}

	public void setSelectedGroup(Group gSelected) {
		this.gSelected = gSelected;
	}

	public void setNodes(Hashtable<String, Node> nodes) {
		this.ethnoArcNodes = nodes;
	}

	public void setSaveName(String name) {
		saveName = "ethnoArc.xml";
		int i = name.lastIndexOf('/');
		if (name.lastIndexOf('\\') > i)
			i = name.lastIndexOf('\\');
		if (i > 0 && i < name.length() - 1) {
			saveName = name.substring(i + 1).toLowerCase() + ".xml";
		}
	}

	public void setDBinfo(String DBURL, String DBname, String DBusername,
			String DBpassword, String DBmenuname, String DBdescription,
			String DBdescriptionEnglish, boolean DBrmi, Color DBcolor,
			String DBcenter) {
		this.DBURL = DBURL;
		DBUSERNAME = DBusername;
		DBPASSWORD = DBpassword;
		DBMENUNAME = DBmenuname;
		DBNAME = DBname;
		DBRMI = DBrmi;
		DBCOLOR = DBcolor;
		DBDESCRIPTIONO = DBdescription;
		DBDESCRIPTIONE = DBdescriptionEnglish;
		DBDESCRIPTION = DBDESCRIPTIONO;
		DBCENTER = DBcenter;
		// prefer english description, if available
		if (DBDESCRIPTIONE.length() > 0)
			DBDESCRIPTION = DBDESCRIPTIONE;
	}

	public void setHoverElement(Element element, boolean repaint) {
		//System.out.println("Hover element called");
		if (getHoverElement() != element) {
			if (element == null) {
				setToolTipText(null);
			}
			// show a tooltip  if the element is a node
			if (element != null
					&& element.getElementType() == Element.NODE_ELEMENT) {
				String toolTip = (String) graph.getAttributeManager()
						.getAttribute("ShowDescription", element);
				int count = 0;
				for (int i = 0; i < toolTip.length(); i++) {
					count++;
					if ((count > 60) && (toolTip.charAt(i) == ' ')
							&& (i < toolTip.length() - 2)) {
						toolTip = toolTip.substring(0, i) + '\n'
								+ toolTip.substring(i + 1);
						count = 0;
					}
				}
				String exampleContent = (String) graph.getAttributeManager()
						.getAttribute("Example", element);
				if (exampleContent.length() != 0)
					toolTip = toolTip + "\nExample: " + exampleContent;
				setToolTipText(toolTip);
			}
			// do something different if the element is an edge
			if (element != null
					&& element.getElementType() == Element.EDGE_ELEMENT) {
				setToolTipText((String) graph.getAttributeManager()
						.getAttribute("Relation", element));
			}
		}
		super.setHoverElement(element, repaint);
	}

	protected void logoClicked(MouseEvent e) {
		super.logoClicked(e);
	}

	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
	}

	public void nodeClicked(int clickCount, Node node) {
		noNodeClick = 0;
		super.nodeClicked(clickCount, node);
		if (clickCount > 0)
			lastClickedNode = node;

		if (clickCount == 1) {
			//System.err.println("Single click on node: "+node.getLabel());
		}
		if (clickCount == 2) {
			//System.err.println("Double click on node: "+node.getLabel());
		}
	}

	public void mouseClicked(java.awt.event.MouseEvent e) {
		// Hypergraph can determine when we click on a node, but
		// not if we click somewhere else. Since we need to know
		// when there is a right-click somewhere else, we give the
		// click event to Hypergraph. If no nodeClicked is triggered,
		// we're clicking somewhere else and can use set the menu accordingly.
		noNodeClick = 1;
		super.mouseClicked(e);

		if (lastSelectedNode != null) {
			lastSelectedNode.setGroup(lastSelectedNodeGroup);
			lastSelectedNode = null;
		}
		if ((noNodeClick == 1)
				&& (e.getButton() == java.awt.event.MouseEvent.BUTTON3)) {
			// right clicked somewhere outside a node - create mouse popup choice
			JPopupMenu menu = new JPopupMenu();
			// Create and add a menu item
			JMenuItem item = new JMenuItem("Recenter graph");
			item.setActionCommand("\1Center");
			item.addActionListener(this);
			menu.add(item);
			item = new JMenuItem("Search node descriptions");
			item.setActionCommand("\1SearchDescriptions");
			item.addActionListener(this);
			menu.add(item);
			item = new JMenuItem("Search node by value");
			item.setActionCommand("\1SearchByValue");
			item.addActionListener(this);
			menu.add(item);

			if (DBDESCRIPTION.length() > 0) {
				item = new JMenuItem("Show archive description");
				item.setActionCommand("\1Describe");
				item.addActionListener(this);
				menu.add(item);
			}
			item = new JMenuItem("Export structure");
			item.setActionCommand("\1Export");
			item.addActionListener(this);
			menu.add(item);
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
		if ((noNodeClick == 0)
				&& (e.getButton() == java.awt.event.MouseEvent.BUTTON3)) {
			// right clicked somewhere outside a node - create mouse popup choice
			JPopupMenu menu = new JPopupMenu();
			// Create and add a menu item
			String noValue = (String) graph.getAttributeManager().getAttribute(
					"NoValue", lastClickedNode);

			JMenuItem item;
			Boolean disable = false;
			if (noValue == null)
				item = new JMenuItem("Add to work area");
			else {
				if (noValue.equals("false"))
					item = new JMenuItem("Add to work area");
				else {
					item = new JMenuItem(
							"Add to work area - [Disabled: no value associated with this node]");
					disable = true;
				}
			}
			item.setActionCommand("\1WorkArea");
			item.addActionListener(this);
			item.setEnabled(!disable);
			menu.add(item);

			item = new JMenuItem("Set as center node");
			item.setActionCommand("\1CenterNode");
			item.addActionListener(this);
			item.setEnabled(true);
			menu.add(item);

			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void actionPerformed(ActionEvent e) {
		//System.err.println(e.getActionCommand());    	
		if (e.getActionCommand().equals("\1Center")) {
			this.centerNode(centerNode);
		}
		if (e.getActionCommand().equals("\1SearchDescriptions")) {
			//this.centerNode(centerNode);    		
			Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			Dimension screensize = toolkit.getScreenSize();
			int xpos = frame.getWidth() + frame.getX() - 20;
			if (xpos + 200 > screensize.width)
				xpos = screensize.width - 200;
			DescriptionPresenter presenter = new DescriptionPresenter(xpos,
					frame.getY() + 10, graph, this, ethnoArcNodes, DBMENUNAME);
			presenter.run();
		}
		if (e.getActionCommand().equals("\1SearchByValue")) {
			//this.centerNode(centerNode);    		
			Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
			Dimension screensize = toolkit.getScreenSize();
			int xpos = frame.getWidth() + frame.getX() - 20;
			if (xpos + 200 > screensize.width)
				xpos = screensize.width - 200;
			NodeFromContent selector = new NodeFromContent(xpos,
					frame.getY() + 10, graph, this, ethnoArcNodes, DBMENUNAME,DBURL,DBNAME, DBUSERNAME,DBPASSWORD,DBRMI);
			selector.run();
		} else if (e.getActionCommand().equals("\1Describe")) {
			JOptionPane.showMessageDialog(frame, DBDESCRIPTION,
					"Archive description", 1);
		} else if (e.getActionCommand().equals("\1Export")) {
			File file = new File(saveName);
			JFileChooser fc = new JFileChooser();
			fc.setSelectedFile(file);
			fc.addChoosableFileFilter(new XMLFilter());
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				ExportDatabaseStructure exporter = new ExportDatabaseStructure();
				exporter.export(file, databaseTables, DBDESCRIPTIONO,
						DBDESCRIPTIONE, countryCode);
			}
		} else if (e.getActionCommand().equals("\1WorkArea")) {
			QueryElement workElement = new QueryElement(DBURL
					+ lastClickedNode.getName());
			workElement
					.setLabel(DBMENUNAME + ": " + lastClickedNode.getLabel());
			workElement.addInput("I");
			workElement.addOutput("O");
			workElement.setType(QueryElement.TYPE_ELEMENT);
			String EDescription = (String) graph.getAttributeManager()
					.getAttribute("EDescription", lastClickedNode);
			String Description = (String) graph.getAttributeManager()
					.getAttribute("Description", lastClickedNode);
			String Example = (String) graph.getAttributeManager().getAttribute(
					"Example", lastClickedNode);
			workElement.setDBinfo(Description, EDescription, Example, DBURL,
					DBNAME, DBUSERNAME, DBPASSWORD, DBMENUNAME, lastClickedNode
							.getName(), DBRMI, DBCOLOR);
			Point point = ControlFrame.QBM.getGoodLocation(workElement, 0, 0);
			workElement.setLocation(point.x, point.y);
			ControlFrame.QBM.addQueryElement(workElement);

			// code for auto-connecting results and entry fields
			// used for experimenting, but not used in actual
			//String prevSel=ControlFrame.QBM.selectedElement;
			//ControlFrame.QBM.selectedElement=workElement.getName();
			//ControlFrame.QBM.AddConnectedEntryField();
			//ControlFrame.QBM.AddConnectedResultField();
			//ControlFrame.QBM.selectedElement=prevSel;
		} else if (e.getActionCommand().equals("\1CenterNode")) {
			setCenterNode(lastClickedNode);
		}
	}

	private class XMLFilter extends FileFilter {

		//Accept all directories and XML files.
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("xml"))
					return true;
				else {
					return false;
				}
			}
			return false;

		}

		//The description of this filter
		public String getDescription() {
			return "XML files";
		}

		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}
}
