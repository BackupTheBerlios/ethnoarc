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

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.Timer;

public class QueryBuildManager extends Object implements Runnable, Serializable {

	private static final long serialVersionUID = -1308461357661531639L;

	public class SpiderWatch extends Thread {
		Thread watchme;

		JLabel label;

		public SpiderWatch(Thread watchme, JLabel l) {
			this.watchme = watchme;
			this.label = l;
		}

		public void run() {
			while (watchme.isAlive())
				try {
					if (!label.isDisplayable())
						watchme.stop();
					Thread.sleep(500);
				} catch (Exception e) {
				}
			ControlFrame.searchButton.setEnabled(true);
		}
	}

	public class DrawingCanvas extends JComponent {
		private static final long serialVersionUID = 1L;

		QueryBuildManager QBM;

		public DrawingCanvas(QueryBuildManager QBM) {
			this.QBM = QBM;
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			QBM.redrawCanvas(g);
		}
	}

	private static int GLOBAL_UPDATE_INTERVAL = 1000;

	private static int WARNING_TIMER = 3000;

	private static int DEFAULT_QUERYCANVAS_WIDTH = 400;

	private static int DEFAULT_QUERYCANVAS_HEIGHT = 600;

	private static int ACTION_IDLE = 0;

	private static int ACTION_DRAGGING = 1;

	private static int ACTION_CONNECTING = 2;

	private static int IO_NONE = 0;

	private static int IO_INPUT = 1;

	private static int IO_OUTPUT = 2;

	private Color connectorLeftColor;

	private Color connectorRightColor;

	public static Color viewportBkgColor;

	private DrawingCanvas queryCanvas;

	private Hashtable<String, QueryElement> queryElements;

	private boolean updateEnabled;

	private boolean fullRedrawNeeded;

	public String selectedElement = "";

	private String selectedInput = "";

	private String selectedOutput = "";

	private String overlayMessage = "";

	private long messageTime;

	private boolean mouseDownLeft;

	private int selectionX, selectionY, selectionWidth, selectionHeight;

	private int prevSelectionX, prevSelectionY;

	private boolean selectionDrawn;

	private int selectionOffsetX, selectionOffsetY;

	private int action;

	private int entryCount = 1;

	private int resultCount = 1;

	private int orCount = 1;

	private int searchCount = 1;

	public static int REPLACE_WINDOW = 1;

	public static int MULTIPLE_WINDOWS = 2;

	public static int TABBED_WINDOW = 3;

	public static int SQL_STYLE = 1;

	public static int WINDOWS_STYLE = 2;

	public static int resultDisplay = MULTIPLE_WINDOWS;

	public static int wildcardStyle = SQL_STYLE;

	public static char CSVseparator = ',';

	public static int nCanvasWidth=DEFAULT_QUERYCANVAS_WIDTH;
	public static int nCanvasHeight=DEFAULT_QUERYCANVAS_HEIGHT;

	public static int tooltipDelay = 4000;

	
	private int selectedIO1Type;

	private int selectedIO2Type;

	private int tmpselectedIOType;

	private String selectedIO1Element = "";

	private String selectedIO2Element = "";

	private String selectedIO1Port = "";

	private String selectedIO2Port = "";

	private String tmpSelectedIOElement = "";

	private String tmpSelectedIOPort = "";

	private JPopupMenu freeSpaceMenu;

	private JPopupMenu queryElementMenu;

	//private JScrollPane 			scrollPane;
	private JFrame frame = null;

	private JTabbedPane tabbedPane = null;

	public Image input1;

	public Image input2;

	public Image input3;

	public Image output1;

	public Image output2;

	public Image input1s;

	public Image input2s;

	public Image input3s;

	public Image output1s;

	public Image output2s;

	private int tableSelectedRow = 0;

	private int tableSelectedCol = 0;

	public QueryBuildManager() {

		queryElements = new Hashtable<String, QueryElement>();

		action = ACTION_IDLE;

		selectedElement = "";
		selectedInput = "";
		selectedOutput = "";

		queryCanvas = new DrawingCanvas(this);
		queryCanvas.setDoubleBuffered(false);
		//queryCanvas.setOpaque( true );
		queryCanvas.setBackground(new Color(255, 5, 2));
		queryCanvas.setSize(nCanvasWidth,nCanvasHeight);
		
		queryCanvas.setVisible(true);
		queryCanvas.setEnabled(true);
		updateEnabled = true;
		fullRedrawNeeded = true;

		connectorRightColor = new Color(122, 153, 122);
		connectorLeftColor = new Color(112, 133, 165);
		viewportBkgColor = ControlFrame.frameBG;
		//queryCanvas.setBackground(viewportBkgColor);

		ToolTipManager ttm = ToolTipManager.sharedInstance();
		QueryBuildManager.tooltipDelay = ttm.getDismissDelay();

		try {
			input1 = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input1"),
					"file:res/images/SearchIcons/input1.gif"));
			input2 = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input2"),
					"file:res/images/SearchIcons/input2.gif"));
			input3 = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input2"),
					"file:res/images/SearchIcons/input3.gif"));
			output1 = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("output1"),
					"file:res/images/SearchIcons/output1.gif"));
			output2 = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("output2"),
					"file:res/images/SearchIcons/output2.gif"));
			input1s = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input1-selected"),
					"file:res/images/SearchIcons/input1-selected.gif"));
			input2s = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input2-selected"),
					"file:res/images/SearchIcons/input2-selected.gif"));
			input3s = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("input2-selected"),
					"file:res/images/SearchIcons/input3-selected.gif"));
			output1s = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("output1-selected"),
					"file:res/images/SearchIcons/output1-selected.gif"));
			output2s = javax.imageio.ImageIO.read(new java.net.URL(getClass()
					.getResource("output2-selected"),
					"file:res/images/SearchIcons/output2-selected.gif"));
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		queryCanvas
				.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
					public void mouseMoved(java.awt.event.MouseEvent evt) {
						mouseMovedEvent((int) evt.getX(), (int) evt.getY());
					}

					public void mouseDragged(java.awt.event.MouseEvent evt) {
						mouseMovedEvent((int) evt.getX(), (int) evt.getY());
					}
				});

		queryCanvas.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
					mousePressedEventLeft((int) evt.getX(), (int) evt.getY());
				else if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
					mousePressedEventRight((int) evt.getX(), (int) evt.getY());
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
					mouseReleasedEventLeft((int) evt.getX(), (int) evt.getY());
				else if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
					mouseReleasedEventRight((int) evt.getX(), (int) evt.getY());
			}
		});

		freeSpaceMenu = new JPopupMenu();
		freeSpaceMenu.setLightWeightPopupEnabled(false);
		JMenuItem menuItem = new JMenuItem("Add entry field");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryElement queryElement = new QueryElement("Entry "
						+ entryCount++);
				queryElement.setType(QueryElement.TYPE_ENTRY);
				queryElement.addOutput("Query string");
				queryElement.setLabel("Entryfield");
				queryElement
						.setEnglishDescription("Entryfield to provide search strings for queries");
							//Point point = getGoodLocation(queryElement, 0, 0);
				Point point = queryCanvas.getMousePosition(true);
				if(point==null)  point =getGoodLocation(queryElement, 0, 0);

				queryElement.setLocation(point.x, point.y);
				addQueryElement(queryElement);
			}
		});
		freeSpaceMenu.add(menuItem);
		menuItem = new JMenuItem("Add result element");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryElement queryElement = new QueryElement("Result "
						+ resultCount++);
				queryElement.addInput("Result list");
				queryElement.setType(QueryElement.TYPE_RESULT);
				queryElement
						.setEnglishDescription("Result fields determine which fields results will appear in the result matrix.");
				//Point point = getGoodLocation(queryElement, 0, 0);
				Point point = queryCanvas.getMousePosition(true);
				if(point==null)  point =getGoodLocation(queryElement, 0, 0);
				queryElement.setLocation(point.x, point.y);
				addQueryElement(queryElement);
			}
		});
		freeSpaceMenu.add(menuItem);
		menuItem = new JMenuItem("Add OR");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryElement queryElement = new QueryElement("OR " + orCount++);
				queryElement.addInput("Input");
				queryElement.setLabel("OR");
				queryElement.setType(QueryElement.TYPE_OR);
				queryElement
						.setEnglishDescription("Allows an 'OR' connection for two objects or entry fields.");
				//Point point = getGoodLocation(queryElement, 0, 0);
				Point point = queryCanvas.getMousePosition(true);
				if(point==null)  point =getGoodLocation(queryElement, 0, 0);
				queryElement.setLocation(point.x, point.y);
				addQueryElement(queryElement);
			}
		});
		redrawCanvas();
		freeSpaceMenu.add(menuItem);

	}

	//public void setScrollPane ( JScrollPane scrollPane ) {
	//	this.scrollPane = scrollPane;
	//}

	public DrawingCanvas getQueryCanvas() {
		return queryCanvas;
	}

	public int getQueryCanvasWidth() {
		return queryCanvas.getWidth();
	}
	public int getQueryCanvasHeight() {
		return queryCanvas.getHeight();
	}

	public void run() {
		while (true) {
			if (updateEnabled) {
				if (!fullRedrawNeeded) {
					refreshCanvas();
				} else {
					redrawCanvas();
					fullRedrawNeeded = false;
				}
				try {
					Thread.sleep(GLOBAL_UPDATE_INTERVAL);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void addQueryElement(QueryElement qe) {
		queryElements.put(qe.getName(), qe);
		redrawCanvas();
	}

	public void fullRedraw() {
		fullRedrawNeeded = true;
	}

	public void showOberlayMessage(String message, int time) {
		overlayMessage = message;
		messageTime = System.currentTimeMillis();
		new Timer(time, redrawTimer).start();
		action = ACTION_IDLE;
		redrawCanvas();
		return;
	}

	protected void connectIOs(String outputelement, String outputport,
			String inputelement, String inputport) {
		synchronized (queryElements) {
			QueryElement queryElement = (QueryElement) queryElements
					.get(outputelement);
			QueryElement targetElement = (QueryElement) queryElements
					.get(inputelement);
			if ((queryElement.getType() == QueryElement.TYPE_ENTRY)
					&& (targetElement.getType() == QueryElement.TYPE_RESULT)) {
				showOberlayMessage("Can't connect blue output to green input",
						WARNING_TIMER);
				return;
			}
			if ((queryElement.getType() == QueryElement.TYPE_ELEMENT)
					&& (targetElement.getType() == QueryElement.TYPE_ELEMENT)) {
				showOberlayMessage("Can't connect green output to blue input",
						WARNING_TIMER);
				return;
			}
			if ((queryElement.getType() == QueryElement.TYPE_ENTRY)
					&& (targetElement.getType() == QueryElement.TYPE_OR)
					&& (targetElement.getOrType() == QueryElement.OR_TYPE_DATA)) {
				showOberlayMessage(
						"Can't mix entry field and element field outputs",
						WARNING_TIMER);
				return;
			}
			if ((queryElement.getType() == QueryElement.TYPE_ELEMENT)
					&& (targetElement.getType() == QueryElement.TYPE_OR)
					&& (targetElement.getOrType() == QueryElement.OR_TYPE_TEXT)) {
				showOberlayMessage(
						"Can't mix entry field and element field outputs",
						WARNING_TIMER);
				return;
			}
			// Note - we  only handle elements with a single output, so we don't
			// have to care about the outputport information
			try {
				queryElement.addOutputConnection(inputelement, inputport);
				// perform re-labeling if we connect an element to a result field
				if (queryElement.getType() == QueryElement.TYPE_ELEMENT) {
					if ((targetElement.getType() == QueryElement.TYPE_RESULT)
							&& (targetElement.initialLabel)) {
						if (queryElement.getLabel().contains(":"))
							targetElement.setNewLabel(queryElement.getLabel()
									.substring(
											queryElement.getLabel()
													.indexOf(':') + 2));
						else
							targetElement.setNewLabel(queryElement.getLabel());
					}
				}
				// perform re-labeling if we connect an entry field to an element
				if (queryElement.getType() == QueryElement.TYPE_ENTRY) {
					if ((targetElement.getType() == QueryElement.TYPE_ELEMENT)
							&& (queryElement.initialLabel)) {
						if (targetElement.getLabel().contains(":"))
							queryElement.setNewLabel(targetElement.getLabel()
									.substring(
											targetElement.getLabel().indexOf(
													':') + 2));
						else
							queryElement.setNewLabel(targetElement.getLabel());
					}
				}
				// set element type if we connect to neutral 'or' element
				if (targetElement.getType() == QueryElement.TYPE_OR) {
					if (queryElement.getType() == QueryElement.TYPE_ELEMENT)
						targetElement.setOrType(QueryElement.OR_TYPE_DATA);
					if (queryElement.getType() == QueryElement.TYPE_ENTRY)
						targetElement.setOrType(QueryElement.OR_TYPE_TEXT);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			//System.out.println( "Connecting: "+queryElement.getName()+"."+outputport+"  to  "+inputelement+"."+inputport );
			fullRedraw();
			return;
		}
	}

	ActionListener redrawTimer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			Timer t = (Timer) evt.getSource();
			t.stop();
			redrawCanvas();
		}
	};

	public void redrawCanvas() {
		redrawCanvas(queryCanvas.getGraphics());
	}

	public void redrawCanvas(Graphics g) {
		if (g != null) {
			// clear viewport 			
			g.setColor(viewportBkgColor);
			g.fillRect(0, 0, queryCanvas.getWidth(), queryCanvas.getHeight());
			redrawConnectors(g);
			// force each element  to redraw itself
			Iterator iqe = queryElements.keySet().iterator();
			while (iqe.hasNext())
				((QueryElement) queryElements.get(iqe.next())).redrawTo(g);
			redrawMessage(g);
		}
	}

	public void redrawMessage(Graphics g) {
		// draw timed overlay message if available	
		if (g == null)
			return;
		if (overlayMessage.length() == 0)
			return;
		if (System.currentTimeMillis() - messageTime > 1000) {
			overlayMessage = "";
			return;
		}
		g.setFont(new Font("Dialog", Font.PLAIN, 12));
		int w = g.getFontMetrics().stringWidth(overlayMessage);
		int h = g.getFontMetrics().getHeight();
		int x = queryCanvas.getWidth() / 2 - w / 2;
		int y = queryCanvas.getHeight() / 2 - h / 2;
		g.setColor(new Color(240, 128, 128));
		g.fillRect(x - 5, y - 5, w + 10, h + 10);
		g.setColor(new Color(0, 0, 0));
		g.drawRoundRect(x - 5, y - 5, w + 10, h + 10, 5, 5);
		g.drawString(overlayMessage, x, y + h - 4);
	}

	public void refreshCanvas() {
		Graphics g;
		g = queryCanvas.getGraphics();
		if (updateEnabled) {
			Iterator iqe = queryElements.keySet().iterator();
			while (iqe.hasNext()) {
				QueryElement queryElement = (QueryElement) queryElements
						.get(iqe.next());
				if (queryElement.needRedraw()) {
					if (g != null) {
						queryElement.redrawTo();
						queryElement.redrawn();
					}
				}
			}
		}
		redrawMessage(g);
	}

	private void redrawConnectors(Graphics g) {
		int x1, x2, y1, y2;
		if (g == null)
			return;
		Iterator iqe = queryElements.keySet().iterator();
		while (iqe.hasNext()) {
			QueryElement queryElement = (QueryElement) queryElements.get(iqe
					.next());
			Iterator ito = queryElement.getOutputs().values().iterator();
			while (ito.hasNext()) {
				String OutputName = (String) ito.next();
				x1 = (int) (queryElement.getLeft() + queryElement
						.getOutputInsertionPoint(OutputName).getX());
				y1 = (int) (queryElement.getTop() + queryElement
						.getOutputInsertionPoint(OutputName).getY());
				Iterator itc = queryElement.getConnections().keySet()
						.iterator();
				while (itc.hasNext()) {
					String connection = (String) (itc.next());
					String connectionName = (String) queryElement
							.getConnections().get(connection);
					QueryElement inElement = null;
					try {
						inElement = (QueryElement) queryElements
								.get(connection);
						String connectedInputName = connectionName;
						x2 = (int) (inElement.getLeft() + inElement
								.getInputInsertionPoint(connectedInputName)
								.getX());
						y2 = (int) (inElement.getTop() + inElement
								.getInputInsertionPoint(connectedInputName)
								.getY());

						//						//draw GeneralPath (polygon)
						GeneralPath path = new GeneralPath();
						path.moveTo(x1, y1);
						if (queryElement.getOutputInsertionPoint(OutputName)
								.getX() == 0) {
							// draw on left side...
							g.setColor(connectorLeftColor);
							path.curveTo(x1 - 50, y1, x2 - 50, y2, x2, y2);
						} else {
							// draw on right side...
							g.setColor(connectorRightColor);
							path.curveTo(x1 + 50, y1, x2 + 50, y2, x2, y2);
						}
						Graphics2D g2 = (Graphics2D) g;
						Stroke oldStroke = g2.getStroke();
						g2.setStroke(new BasicStroke(2));
						g2.draw(path);
						g2.setStroke(oldStroke);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e.getMessage());
					}
				}
			}
		}
	}

	public void repaintElements(){
		Iterator iqe = queryElements.keySet().iterator();
		while (iqe.hasNext()) {
			QueryElement queryElement = (QueryElement) queryElements
					.get(iqe.next());
			queryElement.setColours(ControlFrame.queryElementBG, 
					ControlFrame.queryElementBorder, 
					ControlFrame.queryElementTextarea,
					ControlFrame.queryElementText);		
					queryElement.redrawTo();
					queryElement.redrawn();
				}
			}
		
	
	
	private void mouseMovedEvent(int x, int y) {
		Graphics g;

		if (mouseDownLeft == true) {
			// if something selected, draw selection
			if (!selectedElement.equals("")) {

				if (action == ACTION_DRAGGING) {
					g = queryCanvas.getGraphics();
					if (g != null) {
						g.setColor(new Color(255, 255, 255));
						g.setXORMode(new Color(0, 0, 0));
						g.drawRect(selectionX, selectionY, selectionWidth,
								selectionHeight);

						if (selectionDrawn) {
							g.drawRect(prevSelectionX, prevSelectionY,
									selectionWidth, selectionHeight);
						} else {
							selectionDrawn = true;
						}
						g.setPaintMode();
					}
					prevSelectionX = selectionX;
					prevSelectionY = selectionY;
					selectionX = x - selectionOffsetX;
					selectionY = y - selectionOffsetY;
				}
			}
		}
	}

	private void mousePressedEventLeft(int x, int y) {
		QueryElement queryElement = null;

		mouseDownLeft = true;
		selectedElement = "";

		unselectIOelement();

		// look whether we selected an element 
		Iterator iqe = queryElements.keySet().iterator();
		while (iqe.hasNext()) {
			queryElement = (QueryElement) queryElements.get(iqe.next());
			if (x >= queryElement.getLeft()
					&& x <= (queryElement.getLeft() + queryElement.getWidth())
					&& y >= queryElement.getTop()
					&& y <= (queryElement.getTop() + queryElement.getHeight())) {
				selectedElement = queryElement.getName();
			}
		}

		if (selectedElement.equals(""))
			action = ACTION_IDLE;

		if (!selectedElement.equals("")) {
			queryElement = (QueryElement) queryElements.get(selectedElement);

			tmpselectedIOType = IO_NONE;

			//find out if any input is under pointer and store reference if any
			selectedInput = queryElement.getNameOfInputAt(x
					- queryElement.getLeft(), y - queryElement.getTop());
			if (!selectedInput.equals("")) {
				tmpselectedIOType = IO_INPUT;
				tmpSelectedIOPort = selectedInput;
				tmpSelectedIOElement = selectedElement;
			}

			//find out if any output is under pointer and store reference if any
			selectedOutput = queryElement.getNameOfOutputAt(x
					- queryElement.getLeft(), y - queryElement.getTop());
			if (!selectedOutput.equals("")) {
				tmpselectedIOType = IO_OUTPUT;
				tmpSelectedIOPort = selectedOutput;
				tmpSelectedIOElement = selectedElement;
			}

			if (tmpselectedIOType != IO_NONE) {
				if (action == ACTION_CONNECTING) {
					selectedIO2Type = tmpselectedIOType;
					selectedIO2Port = tmpSelectedIOPort;
					selectedIO2Element = tmpSelectedIOElement;
					if ((selectedIO2Port.equals(selectedIO1Port))
							&& (selectedIO2Element.equals(selectedIO1Element)))
						return;

					if ((selectedIO1Type == IO_INPUT)
							&& (selectedIO2Type == IO_INPUT)) {
						showOberlayMessage("Cannot connect two inputs!",
								WARNING_TIMER);
						return;
					}
					if ((selectedIO1Type == IO_OUTPUT)
							&& (selectedIO2Type == IO_OUTPUT)) {
						showOberlayMessage("Cannot connect two outputs!",
								WARNING_TIMER);
						return;
					}
					if (selectedIO1Element.equals(selectedIO2Element)) {
						if (!selectedIO2Port.equals(selectedIO1Port)) {
							//System.out.println( "ELement can not connect to itself");
							showOberlayMessage(
									"Element can not connect to itself",
									WARNING_TIMER);
						}
					}
					if (selectedIO1Type == IO_OUTPUT) {
						connectIOs(selectedIO1Element, selectedIO1Port,
								selectedIO2Element, selectedIO2Port);
						redrawCanvas();
					} else {
						connectIOs(selectedIO2Element, selectedIO2Port,
								selectedIO1Element, selectedIO1Port);
						redrawCanvas();
						redrawCanvas();
					}
					action = ACTION_IDLE;
					return;
				} else {
					selectedIO1Type = tmpselectedIOType;
					selectedIO1Port = tmpSelectedIOPort;
					selectedIO1Element = tmpSelectedIOElement;
					action = ACTION_CONNECTING;
					if (selectedIO1Type == IO_INPUT)
						queryElement.selectInput(selectedIO1Port);
					else
						queryElement.selectOutput(selectedIO1Port);
					queryElement.redraw();
					redrawCanvas();
					return;
				}
			}
			action = ACTION_IDLE;
		}
		if (!selectedElement.equals("")) {
			updateEnabled = false;
			action = ACTION_DRAGGING;
			selectionOffsetX = x - queryElement.getLeft();
			selectionOffsetY = y - queryElement.getTop();
			prevSelectionX = x - selectionOffsetX;
			prevSelectionY = y - selectionOffsetY;
			selectionX = x - selectionOffsetX;
			selectionY = y - selectionOffsetY;
			selectionWidth = queryElement.getWidth();
			selectionHeight = queryElement.getHeight();
		}
	}

	private void mousePressedEventRight(int x, int y) {
		unselectIOelement();
		action = ACTION_IDLE;
	}

	private void mouseReleasedEventLeft(int x, int y) {
		QueryElement queryElement = null;

		// reposition & resize selected block if any
		if (!selectedElement.equals("")) {
			try {
				queryElement = (QueryElement) queryElements
						.get(selectedElement);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			if (action == ACTION_DRAGGING) {
				if (selectionX > queryCanvas.getWidth() - 30)
					selectionX = queryCanvas.getWidth() - 30;
				if (selectionY > queryCanvas.getHeight() - 10)
					selectionY = queryCanvas.getHeight() - 10;
				if (selectionX < 0)
					selectionX = 0;
				if (selectionY < 0)
					selectionY = 0;
				queryElement.setLocation(selectionX, selectionY);
				updateEnabled = true;
				redrawCanvas();
				action = ACTION_IDLE;
			}
		}

		mouseDownLeft = false;
		updateEnabled = true;
	}

	private void mouseReleasedEventRight(int x, int y) {
		QueryElement queryElement = null;

		// look whether we selected an element 
		selectedElement = "";
		Iterator iqe = queryElements.keySet().iterator();
		while (iqe.hasNext()) {
			queryElement = (QueryElement) queryElements.get(iqe.next());
			if (x >= queryElement.getLeft()
					&& x <= (queryElement.getLeft() + queryElement.getWidth())
					&& y >= queryElement.getTop()
					&& y <= (queryElement.getTop() + queryElement.getHeight())) {
				selectedElement = queryElement.getName();
			}
		}

		if (!selectedElement.equals("")) {
			queryElement = (QueryElement) queryElements.get(selectedElement);
			buildQueryElementMenu(queryElement);
			popMenu(queryElementMenu, x, y);
			queryElement.redraw();
			redrawCanvas();
		} else {
			popMenu(freeSpaceMenu, x, y);
			redrawCanvas();
		}
	}

	public void buildQueryElementMenu(QueryElement queryElement) {
		JMenuItem menuItem;
		queryElementMenu = new JPopupMenu();
		queryElementMenu.setLightWeightPopupEnabled(false);
		menuItem = new JMenuItem("Change label");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryElement queryElement = (QueryElement) queryElements
						.get(selectedElement);
				String s = (String) JOptionPane.showInputDialog(queryCanvas,
						"Enter new label", "Customized Dialog",
						JOptionPane.PLAIN_MESSAGE, null, null, queryElement
								.getLabel());

				//If a string was returned, say so.
				if ((s != null) && (s.length() > 0)) {
					queryElement.setNewLabel(s);
					redrawCanvas();
				}
			}
		});
		if (queryElement.getType() != QueryElement.TYPE_OR)
			queryElementMenu.add(menuItem);

		menuItem = new JMenuItem("Show properties");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				QueryElement queryElement = (QueryElement) queryElements
						.get(selectedElement);
				String strProperty = "";
				if (queryElement.getDBname().length() > 0)
					strProperty = strProperty + "From archive: "
							+ queryElement.getDBname() + "\n";
				if (queryElement.getEnglishDescription().length() > 0)
					strProperty = strProperty + "English Description:\n  "
							+ queryElement.getEnglishDescription() + "\n";
				if (queryElement.getDescription().length() > 0)
					strProperty = strProperty + "Original Description:\n  "
							+ queryElement.getDescription() + "\n";
				if (queryElement.getExampleContent().length() > 0)
					strProperty = strProperty + "Example Content:\n  "
							+ queryElement.getExampleContent() + "\n";
				JOptionPane.showMessageDialog(queryCanvas, strProperty,
						"Properties for " + queryElement.getLabel(),
						JOptionPane.PLAIN_MESSAGE);
			}
		});
		queryElementMenu.add(menuItem);
		menuItem = new JMenuItem("Remove this element");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedElement();
			}
		});
		queryElementMenu.add(menuItem);

		// add cut option for incoming ports
		Iterator itb = queryElements.keySet().iterator();
		Iterator itc;
		while (itb.hasNext()) {
			QueryElement sourceElement = (QueryElement) queryElements.get(itb
					.next());
			itc = sourceElement.getConnections().keySet().iterator();
			while (itc.hasNext()) {
				String connection = (String) (itc.next());
				if (connection.equals(queryElement.getName())) {
					menuItem = new JMenuItem("Cut incoming connection from \""
							+ sourceElement.getLabel() + "\"");
					menuItem.setActionCommand("I" + sourceElement.getName());
					queryElementMenu.add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							cutConnectionAction(e.getActionCommand());
						}
					});
				}
			}
		}
		// add cut option for outgoing ports
		itc = queryElement.getConnections().keySet().iterator();
		while (itc.hasNext()) {
			String connection = (String) (itc.next());
			QueryElement targetElement = (QueryElement) queryElements
					.get(connection);
			menuItem = new JMenuItem("Cut outgoing connection to \""
					+ targetElement.getLabel() + "\"");
			menuItem.setActionCommand("O" + targetElement.getName());
			queryElementMenu.add(menuItem);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cutConnectionAction(e.getActionCommand());
				}
			});
		}

		// the following options only apply to query elements of the type element 
		if (queryElement.getType() != QueryElement.TYPE_ELEMENT)
			return;

		menuItem = new JMenuItem("Add connected entry field");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddConnectedEntryField();
			}
		});
		queryElementMenu.add(menuItem);

		menuItem = new JMenuItem("Add connected result field");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddConnectedResultField();
			}
		});
		queryElementMenu.add(menuItem);

	}

	public void AddConnectedField() {
		// add result field on keypress (or entry field if we already have a result field)
		if (selectedElement==null)return;
		QueryElement selectedQueryElement = queryElements.get(selectedElement);
		if(selectedQueryElement.getType()!= QueryElement.TYPE_ELEMENT)return;
		
		// do we have outgoing connections?	
		Iterator itc;
		itc = selectedQueryElement.getConnections().keySet().iterator();
		if(!itc.hasNext()) {AddConnectedResultField();return;}
		// do we have incoming connections?	
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			QueryElement sourceElement = (QueryElement) queryElements.get(itb
					.next());
			itc = sourceElement.getConnections().keySet().iterator();
			while (itc.hasNext()) {
				String connection = (String) (itc.next());
				if (connection.equals(selectedQueryElement.getName())) return;
			}
		}
		// no incoming connections - so let's add an entry field
		AddConnectedEntryField();
		
	}
		
	public void AddConnectedResultField() {
		QueryElement selectedQueryElement = queryElements.get(selectedElement);		
		QueryElement resultElement = new QueryElement("Result " + resultCount++);
		resultElement.addInput("Result list");
		resultElement.setType(QueryElement.TYPE_RESULT);
		resultElement
				.setEnglishDescription("Result fields determine which fields results will appear in the result matrix.");
		
		Point point = getGoodLocation(resultElement, selectedQueryElement
				.getLeft(), selectedQueryElement.getTop()
				+ selectedQueryElement.getHeight() + 20);
		resultElement.setLocation(point.x, point.y);
		addQueryElement(resultElement);
		connectIOs(selectedElement, "O", resultElement.getName(), "Result list");
	}

	public void AddConnectedEntryField() {
		QueryElement entryElement = new QueryElement("Entry " + entryCount++);
		entryElement.setType(QueryElement.TYPE_ENTRY);
		entryElement.addOutput("Query string");
		entryElement.setLabel("Entryfield");
		entryElement
				.setEnglishDescription("Entryfield to provide search strings for queries");
		QueryElement selectedQueryElement = queryElements.get(selectedElement);
		Point point = getGoodLocation(entryElement, selectedQueryElement
				.getLeft(), 0);
		entryElement.setLocation(point.x, point.y);
		addQueryElement(entryElement);
		connectIOs(entryElement.getName(), "Query string", selectedElement, "I");
	}

	public void cutConnectionAction(String connectionCommand) {
		String connectionType = connectionCommand.substring(0, 1);
		String connectionNodeName = connectionCommand.substring(1);

		if (connectionType.equals("O")) {
			QueryElement queryElement = (QueryElement) queryElements
					.get(selectedElement);
			queryElement.removeConnection(connectionNodeName);
		} else {
			QueryElement queryElement = (QueryElement) queryElements
					.get(connectionNodeName);
			queryElement.removeConnection(selectedElement);
		}
		checkOrElements();
		redrawCanvas();
	}

	public void checkOrElements() {
		// checking whether the connection removal left any 'OR' elements
		// without incoming connections - revert those elements to neutral
		QueryElement queryElement = null;
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			if (queryElement.getType() == QueryElement.TYPE_OR) {
				Boolean bHasInPointer = false;
				Iterator itb2 = queryElements.keySet().iterator();
				while (itb2.hasNext()) {
					QueryElement pointingElement = (QueryElement) queryElements
							.get(itb2.next());
					Iterator ito = pointingElement.getConnections().keySet()
							.iterator();
					while (ito.hasNext()) {
						String ConnectionName = (String) ito.next();
						if (ConnectionName.equals(queryElement.getName())) {
							bHasInPointer = true;
						}
					}
				}
				if (!bHasInPointer)
					queryElement.setOrType(QueryElement.OR_TYPE_NEUTRAL);
			}
		}
	}

	public void removeSelectedElement() {
		if (selectedElement == null)
			return;
		if (selectedElement.equals(""))
			return;
		// remove connection to this element from all other elements
		QueryElement queryElement = null;
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			Iterator ito = queryElement.getConnections().keySet().iterator();
			while (ito.hasNext()) {
				String ConnectionName = (String) ito.next();
				if (ConnectionName.equals(selectedElement)) {
					queryElement.getConnections().remove(ConnectionName);
				}
			}
		}
		queryElements.get(selectedElement).removeEntryfield();
		queryElements.remove(selectedElement);
		selectedElement = "";
		selectedInput = "";
		selectedOutput = "";
		redrawCanvas();
	}

	public Point getGoodLocation(QueryElement queryElement, int xstart,
			int ystart) {
		Point bestPoint = new Point(xstart, ystart);
		Rectangle queryRect, existRect, overlapRect;
		QueryElement existingElement;
		int overlap, minOverlap;

		minOverlap = 999999999;
		for (int x = xstart; x < (queryCanvas.getWidth() - queryElement
				.getWidth()); x = x + 10)
			for (int y = ystart; y < (queryCanvas.getHeight() - queryElement
					.getHeight()); y = y + 10) {
				queryRect = new Rectangle(x, y, queryElement.getWidth(),
						queryElement.getHeight());
				overlap = 0;
				// calculate overlap with existing blocks
				Iterator it = queryElements.keySet().iterator();
				while (it.hasNext()) {
					existingElement = (QueryElement) queryElements.get(it
							.next());
					existRect = new Rectangle(existingElement.getLeft(),
							existingElement.getTop(), existingElement
									.getWidth(), existingElement.getHeight());
					if (queryRect.intersects(existRect)) {
						overlapRect = queryRect.intersection(existRect);
						overlap = overlap
								+ (overlapRect.height * overlapRect.width);
					}
				}
				if (overlap < minOverlap) {
					bestPoint.x = x;
					bestPoint.y = y;
					if (overlap == 0)
						return bestPoint;
					minOverlap = overlap;
				}
			}
		return bestPoint;
	}

	public void popMenu(JPopupMenu menu, int x, int y) {
		menu.show(queryCanvas, x, y);
	}

	public void clear() {
		// remove fields from canvas
		QueryElement queryElement = null;
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			queryElement.removeEntryfield();
		}
		queryElements.clear();
		selectedElement = "";
		queryElements = new Hashtable<String, QueryElement>();
		redrawCanvas();
	}

	public void writeQueryElements(ObjectOutputStream out) {
		try {

			out.writeInt(entryCount);
			out.writeInt(resultCount);
			out.writeInt(orCount);
			out.writeObject(queryElements);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	public void readQueryElements(ObjectInputStream in) {
		// remove fields from canvas
		QueryElement queryElement = null;
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			queryElement.removeEntryfield();
		}
		try {
			entryCount = in.readInt();
			resultCount = in.readInt();
			orCount = in.readInt();
			queryElements = (Hashtable<String, QueryElement>) in.readObject();
			redrawCanvas();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
		// re-register entry fields with canvas
		queryElement = null;
		itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			queryElement.reregisterEntryfield();
		}

	}

	public Boolean elementPointsTo(String resultName) {
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			QueryElement queryElement = (QueryElement) queryElements.get(itb
					.next());
			if (queryElement.getType() == QueryElement.TYPE_ELEMENT) {
				Iterator outit = queryElement.getConnections().keySet()
						.iterator();
				while (outit.hasNext()) {
					String OutputName = (String) outit.next();
					if (OutputName.equals(resultName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void UpdateElementColours(String archiveURL, Color color) {
		QueryElement queryElement = null;
		Boolean changeMade = false;

		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			if (queryElement.getType() == QueryElement.TYPE_ELEMENT) {
				if (queryElement.getDBURL().equalsIgnoreCase(archiveURL)) {
					queryElement.setColor(color);
					changeMade = true;
				}
			}
		}
		if (changeMade)
			redrawCanvas();
	}

	public void performSearch() {
		Boolean bHasEntry = false, bHasResult = false, bMultipleDB = false;
		Hashtable<String, String> dbReferences = new Hashtable<String, String>();
		Hashtable<String, String> resultFields = new Hashtable<String, String>();
		Hashtable<String, IntObj> resultFieldOrder = new Hashtable<String, IntObj>();
		String firstDBname = "";
		QueryElement queryElement = null;
		DefaultTableModel model;

		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			if (queryElement.getType() == QueryElement.TYPE_ELEMENT) {
				dbReferences.put(queryElement.DBURL + queryElement.DBname,
						queryElement.DBname);
				// and, while we're at it, check whether we deal with multiple Databases
				if (firstDBname.length() == 0)
					firstDBname = new String(queryElement.getDBname());
				if (!(firstDBname.equals(queryElement.getDBname()))) {
					bMultipleDB = true;
				}

			}
			// some basic checking whether we have input and output fields
			// (though we don't check at this point whether they are actually connected to anything)
			if (queryElement.getType() == QueryElement.TYPE_ENTRY)
				bHasEntry = true;
			if (queryElement.getType() == QueryElement.TYPE_RESULT)
				bHasResult = true;
			// and count result fields that are referenced
			if (queryElement.getType() == QueryElement.TYPE_RESULT) {
				if (elementPointsTo(queryElement.getName())) {
					resultFields.put((String) queryElement.getName(),
							(String) queryElement.getLabel());
				}
			}
		}
		if (!bHasEntry) {
			JOptionPane.showMessageDialog(queryCanvas,
					"A search needs to contain at least on entry field.",
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (!bHasResult) {
			JOptionPane.showMessageDialog(queryCanvas,
					"A search needs to contain at least on result field.",
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (resultFields.size() == 0) {
			JOptionPane.showMessageDialog(queryCanvas,
					"A search needs be connected to at least on result field.",
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (dbReferences.isEmpty()) {
			JOptionPane.showMessageDialog(queryCanvas,
					"A search needs to contain at least one database element.",
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		// figure out what result fields are required
		// create table for result display
		if (resultDisplay == MULTIPLE_WINDOWS)
			frame = new JFrame("Search Result");
		if (resultDisplay == REPLACE_WINDOW) {
			if (frame == null)
				frame = new JFrame("Search Result");
			else if (!frame.isDisplayable())
				frame = new JFrame("Search Result");
			else
				; // keep using same frame
		}

		if (resultDisplay == TABBED_WINDOW) {
			if (frame == null) {
				frame = new JFrame("Search Result");
				tabbedPane = null;
				searchCount = 1;
			} else if (!frame.isDisplayable()) {
				frame = new JFrame("Search Result");
				tabbedPane = null;
				searchCount = 1;
			} else
				; // keep using same frame

		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		if ((resultDisplay == TABBED_WINDOW) && (tabbedPane == null))
			tabbedPane = new JTabbedPane();

		JPanel p = new JPanel(new BorderLayout());

		if (resultDisplay == TABBED_WINDOW) {
			tabbedPane.addTab("Result " + searchCount, null, p);
			tabbedPane.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() != 3)
						return;
					JPopupMenu deleteTabMenu = new JPopupMenu();
					deleteTabMenu.setLightWeightPopupEnabled(false);
					JMenuItem menuItem = new JMenuItem("Delete selected tab");
					deleteTabMenu.add(menuItem);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (tabbedPane.getTabCount() == 1) { // if we only got one tab, close the window
								frame.dispose();
							} else {
								tabbedPane.removeTabAt(tabbedPane
										.getSelectedIndex());
							}
						}
					});
					deleteTabMenu.show(tabbedPane, e.getX(), e.getY());
				}

				public void mouseReleased(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mousePressed(MouseEvent e) {
				}
			});
			tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Result "
					+ searchCount));
			searchCount++;
		}

		p.setOpaque(true); //content panes must be opaque
		if (resultDisplay == TABBED_WINDOW)
			frame.setContentPane(tabbedPane);
		else
			frame.setContentPane(p);

		JLabel l = new JLabel("Search Results : 0");
		p.add(l, BorderLayout.NORTH);

		frame.setSize(new Dimension(800, 600));

		model = new DefaultTableModel();
		final JTable table = new JTable(model) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};

		if (bMultipleDB) {
			model.addColumn("Source");
		}

		// add colums to result model, based on left/right position
		int leftmost;
		int orderPos = 0;
		int prevleftmost = -10000;
		boolean valueAdded = true;
		while (valueAdded) {
			// find leftmost result field
			valueAdded = false;
			itb = resultFields.keySet().iterator();
			leftmost = 100000;
			while (itb.hasNext()) {
				String key = (String) itb.next();
				queryElement = (QueryElement) queryElements.get(key);
				if ((queryElement.getLeft() < leftmost)
						&& (queryElement.getLeft() > prevleftmost))
					leftmost = queryElement.getLeft();
			}
			prevleftmost = leftmost;
			itb = resultFields.keySet().iterator();
			while (itb.hasNext()) {
				String key = (String) itb.next();
				queryElement = (QueryElement) queryElements.get(key);
				if (queryElement.getLeft() == prevleftmost) {
					resultFieldOrder.put(key, new IntObj(orderPos));
					orderPos++;
					model.addColumn(resultFields.get(key));
					valueAdded = true;
				}
			}
		}
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true);
		/* Note about sorting tables 
		 * the setAutoCreateRowSorter function is only available
		 * from Java 1.6 onwards - we are trying to keep the code
		 * compatible with Java 1.4, so we don't use the function.
		 * If you are working in a Java 1.6 environment, feel free to
		 * uncomment the next statement.
		 */
		//		table.setAutoCreateRowSorter(true); 
		
		
		table.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent arg0) {
				if (arg0.getClickCount() != 2)
					return; // only handle double clicks
				DefaultTableModel dtm = (DefaultTableModel) table.getModel();

				//System.err.println(dtm.getValueAt(tableSelectedRow,						tableSelectedCol).toString());
				try {
					String myURL = dtm.getValueAt(tableSelectedRow,
							tableSelectedCol).toString();
					if(! ( (myURL.toLowerCase().startsWith("http:"))||(myURL.toLowerCase().startsWith("https:"))))return;
					String osName = System.getProperty("os.name");
					if (osName.startsWith("Mac OS")) {
						Class fileMgr;

						fileMgr = Class.forName("com.apple.eio.FileManager");

						Method openURL;
						openURL = fileMgr.getDeclaredMethod("openURL",
								new Class[] { String.class });

						openURL.invoke(null, new Object[] { myURL });
					} else if (osName.startsWith("Windows"))
						Runtime.getRuntime()
								.exec(
										"rundll32 url.dll,FileProtocolHandler "
												+ myURL);
					else { //assume Unix or Linux 
						String[] browsers = { "firefox", "opera", "konqueror",
								"epiphany", "mozilla", "netscape" };
						String browser = null;
						for (int count = 0; count < browsers.length
								&& browser == null; count++)
							if (Runtime.getRuntime().exec(
									new String[] { "which", browsers[count] })
									.waitFor() == 0)
								browser = browsers[count];

						if (browser == null)
							throw new Exception("Could not find web browser");
						else
							Runtime.getRuntime().exec(
									new String[] { browser, myURL });
					}
				}

				catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
		});

		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (!lsm.isSelectionEmpty())
					tableSelectedRow = lsm.getMinSelectionIndex();
			};
		})

		;
		ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
		colSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (!lsm.isSelectionEmpty())
					tableSelectedCol = lsm.getMinSelectionIndex();
			}
		});
		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		//Add the scroll pane to this panel.
		p.add(scrollPane, BorderLayout.CENTER);

		//		Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		//		Create the menu bar.
		menuBar = new JMenuBar();
		//		Build the menu.
		menu = new JMenu("Save result");
		menuBar.add(menu);
		menuItem = new JMenuItem("Save as Excel");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resultDisplay == TABBED_WINDOW) { // get reference to current tab
					SaveSearchResults resultSaver = new SaveSearchResults();
					resultSaver.tabbedExportExcel(tabbedPane, frame,false);
				} else {
					DefaultTableModel dtm = (DefaultTableModel) table
							.getModel();
					SaveSearchResults resultSaver = new SaveSearchResults();
					resultSaver.exportExcel(dtm, frame,false);
				}
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Show in Excel");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String prevSaveName=SaveSearchResults.saveNameExcel;
				if (resultDisplay == TABBED_WINDOW) { // get reference to current tab
					SaveSearchResults resultSaver = new SaveSearchResults();
					resultSaver.tabbedExportExcel(tabbedPane, frame, true);
				} else {
					DefaultTableModel dtm = (DefaultTableModel) table
							.getModel();
					SaveSearchResults resultSaver = new SaveSearchResults();
					resultSaver.exportExcel(dtm, frame, true);
				}
				
				try {
					String osName = System.getProperty("os.name");
					if (osName.startsWith("Mac OS")) {
						Class fileMgr;

						fileMgr = Class.forName("com.apple.eio.FileManager");

						Method openURL;
						openURL = fileMgr.getDeclaredMethod("openURL",
								new Class[] { String.class });

						openURL.invoke(null, new Object[] { SaveSearchResults.savedNameExcel });
					} else if (osName.startsWith("Windows"))
						Runtime.getRuntime()
								.exec(
										"rundll32 url.dll,FileProtocolHandler "
												+ SaveSearchResults.savedNameExcel);
					else { //assume Unix or Linux 
						String[] browsers = { "firefox", "opera", "konqueror",
								"epiphany", "mozilla", "netscape" };
						String browser = null;
						for (int count = 0; count < browsers.length
								&& browser == null; count++)
							if (Runtime.getRuntime().exec(
									new String[] { "which", browsers[count] })
									.waitFor() == 0)
								browser = browsers[count];

						if (browser == null)
							throw new Exception("Could not find web browser");
						else
							Runtime.getRuntime().exec(
									new String[] { browser, SaveSearchResults.savedNameExcel });
					}
				}

				catch (Exception e1) {
					e1.printStackTrace();
				}
				SaveSearchResults.saveNameExcel=prevSaveName;
			}
			
		});
		menu.add(menuItem);

		
		if (resultDisplay == TABBED_WINDOW)
			menuItem = new JMenuItem("Save selected result tab as CSV");
		else
			menuItem = new JMenuItem("Save as CSV");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel dtm = (DefaultTableModel) table.getModel();
				if (resultDisplay == TABBED_WINDOW) { // get reference to current tab
					JPanel panel = (JPanel) tabbedPane.getSelectedComponent();
					JScrollPane sp = (JScrollPane) panel.getComponent(1);
					JViewport jv = (JViewport) sp.getComponent(0);
					JTable jt = (JTable) jv.getComponent(0);
					dtm = (DefaultTableModel) jt.getModel();
				}
				SaveSearchResults resultSaver = new SaveSearchResults();
				resultSaver.exportCSV(dtm, frame,
						QueryBuildManager.CSVseparator);
			}
		});
		menu.add(menuItem);
		frame.setJMenuBar(menuBar);

		//Display the window.
		frame.pack();
		frame.setVisible(true);

		itb = dbReferences.keySet().iterator();
		DatabaseSpider dbSpider = new DatabaseSpider(queryElements,
				resultFields, resultFieldOrder, bMultipleDB, model, l, itb);
		dbSpider.start();
		SpiderWatch sw = new SpiderWatch(dbSpider, l);
		sw.start();

	}

	public void unselectIOelement() {
		QueryElement queryElement;
		if (selectedIO1Element.equals(""))
			return;
		if (action != ACTION_CONNECTING)
			return;
		queryElement = (QueryElement) queryElements.get(selectedIO1Element);
		queryElement.unselectAllOutputs();
		queryElement.unselectAllInputs();
		queryElement.redraw();
	}
}
