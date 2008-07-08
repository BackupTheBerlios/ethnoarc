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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TextField;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Font;
import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;


public class QueryElement implements Serializable  {

	private static final long serialVersionUID = 1L;
	protected static int    TITLEBAR_VMARGIN                                                = 3;
	protected static int    DEFAULT_WIDTH                                                   = 215;
	protected static int    DEFAULT_HEIGHT                                                  = 25;

	public static int              TYPE_ELEMENT = 0;
	public static int              TYPE_RESULT = 1;
	public static int              TYPE_ENTRY = 2;
	public static int              TYPE_OR= 3;

	public static int              OR_TYPE_NEUTRAL= 0;
	public static int              OR_TYPE_TEXT= 1;
	public static int              OR_TYPE_DATA= 2;

	protected int                   left;
	protected int                   top;
	protected int                   width;
	protected int                   height;
	protected int                   type = 0;      
	protected int                   or_type = 0;      

	protected String                name;      
	protected String                label;      
	protected String                description;      
	protected String                englishDescription;      
	protected String                exampleContent;      
	protected String                DBURL;      
	protected String                DBuser;      
	protected String                DBpassword;      
	protected String                DBname;      
	protected String                DBnameSQL;      
	protected String                DBtablename;      
	protected boolean				DBrmi;

	protected TextField 			entry; 

	protected Map <String,Point>	inputmap_insertionpoint;
	protected Map <String,Point>   	outputmap_insertionpoint;
	protected Map <String,Rectangle> inputmap_extents;
	protected Map <String,Rectangle> outputmap_extents;
	protected Map <String,Boolean>	inputmap_selected;
	protected Map <String,Boolean>	outputmap_selected;
	protected Map <String,String>	inputmap;
	protected Map <String,String>   outputmap;
	protected Hashtable <String,String> connections;

	protected Map                   properties;

	protected boolean               redrawneeded;
	protected boolean               initialLabel=true;

	protected Color                 color_bkg;
	protected Color                 color_border;
	protected Color                 color_textfield;
	protected Color                 color_title;
	protected Color                 color_inputticks;
	protected Color                 color_outputticks;
	protected Color                 color_inputlabels;
	protected Color                 color_outputlabels;
	protected Color                 color_selectedinputticks;
	protected Color                 color_selectedoutputticks;

	protected Font                  font_title;

	public QueryElement(String name  ) {

		this.name=name;
		this.label=name;

		left=0;
		top=0;


		color_bkg                                = ControlFrame.queryElementBG;
		color_textfield                         = ControlFrame.queryElementTextarea;
		color_border                            = ControlFrame.queryElementBorder;
		color_title                             = ControlFrame.queryElementText;
			    
		color_inputticks                        = new Color( 0, 0, 0 );
		color_outputticks                       = new Color( 0, 0, 0 );
		color_inputlabels                       = new Color( 0, 0, 0 );
		color_outputlabels                      = new Color( 0, 0, 0 );
		color_selectedinputticks        = new Color( 201, 66, 133 );
		color_selectedoutputticks       = new Color( 201, 66, 133 );

		font_title                                      = new Font( "Dialog", Font.PLAIN, 12 );

		description="";      
		englishDescription="";  
		exampleContent="";  
		// database access info might be changed once we sue the web proxy instead
		DBURL="";      
		DBuser="";    
		DBpassword="";   
		DBname="";  
		DBtablename="";  
		DBrmi=false;

		inputmap_insertionpoint         = new HashMap<String,Point>();
		outputmap_insertionpoint        = new HashMap<String,Point>();
		inputmap_extents                = new HashMap<String,Rectangle>();
		outputmap_extents               = new HashMap<String,Rectangle>();
		inputmap_selected               = new HashMap<String,Boolean>	();
		outputmap_selected              = new HashMap<String,Boolean>	();
		inputmap                     	= new HashMap<String,String>();
		outputmap                      	= new HashMap<String,String>();
		connections                     = new Hashtable<String,String>();



		entry= new TextField("");
		entry.setBackground(color_textfield);
		entry.setForeground(Color.BLACK);
		entry.setBounds(0,0,10,10);
		entry.setVisible(false);
		ControlFrame.QBM.getQueryCanvas().add(entry);

		safeSetSize( DEFAULT_WIDTH, DEFAULT_HEIGHT );



	}

	public void setSize( int width, int height ) {
		safeSetSize( width, height );
	}


	private void safeSetSize( int width, int height ) { 
		this.width = width;
		this.height = height;		
		buildDefaultInputMap();
		buildDefaultOutputMap();
		redrawTo();
	}

	public void setLocation( int left, int top ) {
		safeSetLocation( left, top );		
	}

	private void safeSetLocation( int left, int top ) {
		this.left = left;
		this.top = top;
		redrawTo();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int  getLeft() {
		return left;
	}

	public int getTop() {
		return top;
	}

	public String getName() {
		return name;
	}
	public String getDBtablename() {
		return DBtablename;
	}
	public boolean getDBrmi() {
		return DBrmi;
	}	
	public String getLabel() {
		return label;
	}
	public String getDescription() {
		return description;
	}
	public String getEnglishDescription() {
		return englishDescription;
	}
	public String getExampleContent() {
		return exampleContent;
	}
	public void setDescription(String description) {
		this.description=description;
	}
	public void setEnglishDescription(String description) {
		this.englishDescription=description;
	}
	public void setExampleContent(String exampleContent) {
		this.exampleContent=exampleContent;
	}
	public void setDBrmi(boolean DBrmi) {
		this.DBrmi=DBrmi;
	}	
	public String getDBnameSQL() {
		return DBnameSQL;
	}
	public String getDBname() {
		return DBname;
	}
	public String getDBuser() {
		return DBuser;
	}
	public String getDBpassword() {
		return DBpassword;
	}
	public String getDBURL() {
		return DBURL;
	}
	public String getEntryfieldValue() {
		return entry.getText();
	}

	public void setLabel(String label) {
		this.label=label;
		// resize for long labels
		int label_width = ControlFrame.QBM.getQueryCanvas().getFontMetrics(font_title).stringWidth( label );
		if(type==TYPE_ELEMENT)label_width=label_width+55;
		else label_width=label_width+30;
		if(label_width>getWidth())setSize(label_width,getHeight());

	}
	public void setNewLabel(String label) {
		initialLabel=false;
		setLabel(label);
	}
	public Boolean initialLabel() {
		return initialLabel;
	}

	public void setDBinfo(String description, String englishDescription, String exampleContent,String DBURL, String DBnameSQL, String DBuser, String DBpassword, String DBname,String DBtablename, boolean DBrmi, Color DBcolor) {
		this.description=description;      
		this.englishDescription=englishDescription;
		this.exampleContent=exampleContent;

		this.DBURL=DBURL;      
		this.DBnameSQL=DBnameSQL;      
		this.DBuser=DBuser;      
		this.DBpassword=DBpassword;      
		this.DBname=DBname;    
		this.DBtablename=DBtablename;
		this.DBrmi=DBrmi;
		this.color_title=DBcolor;
	}
	public void redrawTo(  ) {
		redrawTo(ControlFrame.QBM.getQueryCanvas().getGraphics());
	}
	public void redrawTo(  Graphics g) {

		// Graphics g = ControlFrame.QBM.getQueryCanvas().getGraphics();
		Graphics2D g2 = (Graphics2D)g;
		int labelleft=3;
		boolean outputSelected=false;
		boolean inputSelected=false;

		Iterator it = this.getOutputs().values().iterator();
		while( it.hasNext() ) {
			String OutputName = (String) it.next(); 
			if( selectedOutput(OutputName) ) outputSelected=true;
		}
		it = this.getInputs().values().iterator();
		while( it.hasNext() ) {
			String InputName = (String) it.next(); 
			if( selectedInput(InputName) ) inputSelected=true;
		}

		g.setColor(color_bkg);
		g.fillRect(left,top, width, height);
		if(type==TYPE_RESULT)
			g.setColor(Color.WHITE);
		else
			g.setColor(color_border);
		((Graphics2D)g).setStroke(new BasicStroke(2)); 
		g.drawRect(left+1,top+1, width-2, height-2);

		if(type==TYPE_ENTRY){
			if(outputSelected)
				g2.drawImage(ControlFrame.QBM.output1s,left+2,top+2,null);
			else
				g2.drawImage(ControlFrame.QBM.output1,left+2,top+2,null);
			labelleft=27;
		}
		if(type==TYPE_ELEMENT){
			if(inputSelected)
				g2.drawImage(ControlFrame.QBM.input1s,left+2,top+2,null);
			else
				g2.drawImage(ControlFrame.QBM.input1,left+2,top+2,null);
			if(outputSelected)
				g2.drawImage(ControlFrame.QBM.output2s,left+width-23,top+2,null);
			else
				g2.drawImage(ControlFrame.QBM.output2,left+width-23,top+2,null);
			labelleft=27;
		}
		if(type==TYPE_OR){
			if(or_type==OR_TYPE_NEUTRAL){
				if(inputSelected)
					g2.drawImage(ControlFrame.QBM.input3s,left+2,top+23,null);
				else
					g2.drawImage(ControlFrame.QBM.input3,left+2,top+23,null);
			}
			else if(or_type==OR_TYPE_TEXT){
				if(inputSelected)
					g2.drawImage(ControlFrame.QBM.input1s,left+2,top+23,null);
				else
					g2.drawImage(ControlFrame.QBM.input1,left+2,top+23,null);
			}
			else {
				if(inputSelected)
					g2.drawImage(ControlFrame.QBM.input2s,left+2,top+23,null);
				else
					g2.drawImage(ControlFrame.QBM.input2,left+2,top+23,null);
			}
			labelleft=4;
		}
		if(type==TYPE_RESULT){
			if(inputSelected)
				g2.drawImage(ControlFrame.QBM.input2s,left+width-23,top+2,null);
			else
				g2.drawImage(ControlFrame.QBM.input2,left+width-23,top+2,null);
		}		 
		if(type==TYPE_ENTRY){
			g.drawLine(left+2, top+25, left+width-2, top+25);
			g.setColor(color_textfield);
			g.fillRect(left+2,top+26, width-4,height-28);
			entry.setBackground(color_textfield);
			entry.setForeground(Color.BLACK);
			entry.setBounds(left+2, top+26, getWidth()-5, 19);
			entry.setVisible(true);

		}
		else  entry.setVisible(false);
		g.setColor(color_title);
		g.drawString( this.getLabel(), left+labelleft, top+17);


	}

	public boolean needRedraw() {
		return redrawneeded;
	}

	public void redraw() {
		redrawneeded = true;
	}

	public void redrawn() {
		redrawneeded = false;
	}
	
	public void setColours(Color bg, Color border, Color textfield, Color title) {
		color_bkg=bg;	
		color_border=border;;
		color_textfield=textfield;
		color_title=title;
		redraw();
	}

	public Point getInputInsertionPoint( String inputName ) {
		return (Point)(inputmap_insertionpoint.get(inputName));
	}

	public Point getOutputInsertionPoint( String outputName ) {
		return (Point)(outputmap_insertionpoint.get(outputName));
	}

	public Rectangle getInputExtents( String inputName ) {
		return (Rectangle)(inputmap_extents.get(inputName));
	}

	public Rectangle getOutputExtents( String outputName ) {
		return (Rectangle)(outputmap_extents.get(outputName));
	}

	public String getNameOfInputAt( int x, int y ) { //local coordinates
		int                     l, t, w, h;

		Iterator it = inputmap_extents.entrySet().iterator();
		while( it.hasNext() ) {
			Map.Entry entry = (Map.Entry) it.next(); 
			Rectangle rect = (Rectangle) entry.getValue();
			w = (int)rect.getWidth();
			h = (int)rect.getHeight();
			l = (int)rect.getX();
			t = (int)rect.getY();
			if( x>=l && x<=(l+w) && y>=t && y<=(t+h) ) {
				return entry.getKey().toString();
			}
		}
		return "";
	}

	public String getNameOfOutputAt( int x, int y ) { //local coordinates
		int                     l, t, w, h;

		Iterator it = outputmap_extents.entrySet().iterator();
		while( it.hasNext() ) {
			Map.Entry entry = (Map.Entry) it.next(); 
			Rectangle rect = (Rectangle) entry.getValue();
			w = (int)rect.getWidth();
			h = (int)rect.getHeight();
			l = (int)rect.getX();
			t = (int)rect.getY();
			if( x>=l && x<=(l+w) && y>=t && y<=(t+h) ) {
				return entry.getKey().toString();
			}
		}
		return "";
	}

	protected void buildDefaultInputMap() {
		int                     x, y;
		int                     l, t, w, h;

		inputmap_insertionpoint.clear();
		inputmap_extents.clear();

		Iterator it = this.getInputs().values().iterator();
		while( it.hasNext() ) {
			String InputName = (String) it.next();
			w= 22;
			h = 22;
			if(type==TYPE_ELEMENT)l=2;
			else if(type==TYPE_OR)l=2;
			else l=width-24;
			if(type==TYPE_OR)t=24;
			else t = 2;
			if(type==TYPE_ELEMENT)x=0;
			else x=width;
			y = 12;		
			inputmap_insertionpoint.put(InputName, new Point(x,y) );		
			inputmap_extents.put( InputName, new Rectangle(l,t,w,h) );	
		}
	}

	protected void buildDefaultOutputMap() {
		int                     x, y;
		int                     l, t, w, h;

		outputmap_insertionpoint.clear();
		outputmap_extents.clear();

		Iterator it = this.getOutputs().values().iterator();
		while( it.hasNext() ) {
			String OutputName = (String) it.next();
			w= 22;
			h = 22;
			if(type==TYPE_ELEMENT)l=width-24;
			else l=2;
			t = 2;
			if(type==TYPE_ELEMENT)x=width;
			else x=0;
			y = 12;
			outputmap_insertionpoint.put( OutputName, new Point(x,y) );
			outputmap_extents.put( OutputName, new Rectangle(l,t,w,h) );
		}
	}

	protected void unselectAllInputs() {
		inputmap_selected.clear();
		Iterator it = this.getInputs().values().iterator();
		while( it.hasNext() ) {
			String InputName = (String) it.next();
			inputmap_selected.put( InputName, new Boolean(false) );
		}
	}

	protected void unselectAllOutputs() {
		outputmap_selected.clear();

		Iterator it = this.getOutputs().values().iterator();
		while( it.hasNext() ) {
			String OutputName = (String) it.next();
			outputmap_selected.put(OutputName, new Boolean(false) );
		}
	}

	protected void selectInput( String inputName ) {
		inputmap_selected.put( inputName, new Boolean(true) );
	}

	protected void selectOutput( String outputName ) {
		outputmap_selected.put( outputName, new Boolean(true) );
	}

	protected void unselectInput( String inputName ) {
		inputmap_selected.put( inputName, new Boolean(false) );
	}

	protected void unselectOutput( String outputName ) {
		outputmap_selected.put( outputName, new Boolean(false) );
	}

	public boolean selectedInput( String inputName ) { 
		return inputmap_selected.get(inputName) != null ? ((Boolean)(inputmap_selected.get(inputName))).booleanValue() : false;
	}

	public boolean selectedOutput( String outputName ) { 
		return outputmap_selected.get(outputName) != null ? ((Boolean)(outputmap_selected.get(outputName))).booleanValue() : false;
	}
	public void  addOutputConnection (String inputelement, String inputport){
		connections.put(inputelement, inputport);
	}
	public Map getInputs() {
		return inputmap;
	}
	public Map getOutputs() {
		return outputmap;
	}
	public Hashtable getConnections() {
		return connections;
	}
	public int getNumConnections() {
		return connections.size();
	}
	public int getNumInputs() {
		return inputmap.size();
	}
	public int getNumOutputs() {
		return outputmap.size();
	}
	public Map getProperties() {
		return properties;
	}
	public void setType(int type) {
		this.type=type;
		if(type==TYPE_ENTRY){
			setSize(getWidth(),47);
			entry.setVisible(true);
			entry.setEnabled(true);
		}
		else if(type==TYPE_OR){
			setSize(26,47);
			buildDefaultInputMap();
		}
		else  {
			setSize(getWidth(),26);
			entry.setVisible(false);
			entry.setEnabled(false);
		}

	}	
	public void setOrType(int type) {
		if(type==this.or_type)return;
		
		String InputName="";
		Iterator it = this.getInputs().values().iterator();
		while( it.hasNext() ) {
			 InputName = (String) it.next();
		}		
		if(type==OR_TYPE_TEXT){
			inputmap_insertionpoint.put(InputName, new Point(0,36) );	
		}
		else if(type==OR_TYPE_DATA){
			inputmap_insertionpoint.put(InputName, new Point(width,36) );	
		}
		this.or_type=type;
	}	
	public int getOrType() {
		return or_type;
	}
	public void setColor(Color DBcolor) {
		this.color_title=DBcolor;
	}		
	public int getType() {
		return type;
	}
	protected void addInput( String inputname ) {
		inputmap.put( inputname, inputname );
		buildDefaultInputMap();
		unselectAllInputs();
		redraw();
	}

	protected void addOutput( String outputname ) {
		outputmap.put( outputname, outputname );
		buildDefaultOutputMap();
		unselectAllOutputs();
	}

	protected void removeInput( String inputname ) {
		inputmap.remove( inputname );
		buildDefaultInputMap();          
		unselectAllInputs();
	}


	protected void removeOutput( String outputname ) {
		outputmap.remove( outputname );
		buildDefaultOutputMap();         
		unselectAllOutputs();
	}
	protected void removeConnection( String connectionputname ) {
		connections.remove( connectionputname );
	}

	public void removeEntryfield( ) {
		ControlFrame.QBM.getQueryCanvas().remove(entry);
	}
	public void reregisterEntryfield( ) {
		ControlFrame.QBM.getQueryCanvas().add(entry);
	}

}
