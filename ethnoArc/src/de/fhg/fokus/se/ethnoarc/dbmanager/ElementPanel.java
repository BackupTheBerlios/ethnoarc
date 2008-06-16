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
package de.fhg.fokus.se.ethnoarc.dbmanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBField;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.TableReferenceTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
import de.fhg.fokus.se.ethnoarc.dbmanager.DataFieldUI.fieldUITypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.helper.Utils;

/**
 * $Id: ElementPanel.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author FOKUS
 */
public class ElementPanel extends javax.swing.JPanel implements ActionListener 
{
	/** The corresponding {@link DBTableElement} of this panel.	 */
	private DBTableElement tableElement;
	/**  */
	private JLabel labElement;
	private JPanel valueElementPanel;
	private String elementDisplayString;
	private int labElementHeight=20;
	private boolean hasAlternate=false;

	private List<ValueElements> valueElementList = new ArrayList<ValueElements>();

	private List<AlternateDetails> alternateElementList = new ArrayList<AlternateDetails>();

	private List<String> impliesList=new ArrayList<String>();
	private Hashtable<String,DataFieldUI> impliesControlList;//=new Hashtable<String,DataFieldUI>();
	private String impliesSql;
	private boolean doImply=true;

	private JCheckBox tfIsPublic;
	private NaviButtonBase butMultiple;
	private NaviButtonBase butDelete;

	private String elementName;
	private Boolean originalIsPublic;
	private AppPropertyManager appProperty;

	/**
	 * Command to add value to an element that could have multiple values.
	 */
	private final String CMD_SAVE="ADD_VALUE";
	private final String CMD_EDIT_ALTERNATE="EDIT_ALTERNATE";
	private final String CMD_DELETE="DELETE_VALUE";

	static Logger logger = Logger.getLogger(ElementPanel.class.getName());
	private Color bgColor;
	private MainUIFrame mainPanel;
	/**
	 * The type of reference between this table and its parent.
	 * @see TableReferenceTypes
	 */
	private TableReferenceTypes parentReferenceType;
	/**
	 * Panel of the parent table.
	 */
	private TablePanel parentTablePanel;

	public ElementPanel(DBTableElement tableElement,Color bgColor,MainUIFrame mainPanel,TablePanel parentTablePanel)
	{
		super();
		EADBDescription relDescTable = tableElement.getRelationDescriptionTable();
		if(relDescTable!=null)
			parentReferenceType= TableReferenceTypes.valueOf(relDescTable.getType());

		this.mainPanel=mainPanel;
		this.bgColor=bgColor;
		this.setBackground(bgColor);
		this.tableElement=tableElement;
		this.setName(tableElement.getNameDB());
		this.elementName = tableElement.getDisplayName();
		this.originalIsPublic=tableElement.getIsPublicDefault();
		this.parentTablePanel=parentTablePanel;

		appProperty=mainPanel.getAppProperties();

		//get the list of alternate or alternativeLang tables
		getReferencedElements();

		initGUI();

		setElementTooltip();
		setPopup();

		this.setAlignmentY(SwingConstants.TOP);
	}
	public boolean isTableValueElement()
	{
		return tableElement.isTableValueElement();
	}
	private void getReferencedElements()
	{
		for (EADBDescription.TableReference referencedTable : tableElement.getElementDescription().getReferredTables()) {
			switch (referencedTable.getReferenceType()) {
			case Alternative:
				alternateElementList.add(new AlternateDetails(tableElement.getNameDB(),DBConstants.TableReferenceTypes.Alternative,referencedTable.getReferencedTable()));
				break;
			case AlternativeLanguage:
				alternateElementList.add(new AlternateDetails(tableElement.getNameDB(),DBConstants.TableReferenceTypes.AlternativeLanguage,referencedTable.getReferencedTable()));
				break;
			case TakesReferenceFrom:
				alternateElementList.add(new AlternateDetails(tableElement.getNameDB(),DBConstants.TableReferenceTypes.TakesReferenceFrom,referencedTable.getReferencedTable()));
				break;
			case Implies:
				//logger.error("IMPLIES "+appProperty.getEnableImpliesFeature());
				if(appProperty.getEnableImpliesFeature())
					impliesList.add(referencedTable.getReferencedTable().getNameDB());
				break;
			default:
				break;
			}
		}
		if(alternateElementList.size()>0)
			hasAlternate=true;
	}
//	public EADBDescription getElementProperty()
//	{
//	return tableElement.getElementDescription();
//	}
	public List<AlternateDetails> getAlternateDetails()
	{
		return alternateElementList;
	}
	public String getParentConnectingTableName()
	{
		return tableElement.getRelationDescrTableName();
	}
	public String getParentTableName()
	{
		return tableElement.getTableName();
	}
	public String getNameDB()
	{
		return tableElement.getNameDB();
	}

	public void setElementTooltip()
	{
		AppConstants.ElementTooltipOptions tooltipType;
		String tooltip="";
//		set tooltip
		try {
			tooltipType=AppPropertyManager.getDBPropertyManagerInstant().getElementTooltipType();
		} catch (DBException e) {
			logger.warn("Error getting tooltip type from the property manager: "+e.getMessage());
			//default
			tooltipType= AppConstants.ElementTooltipOptions.Description;
		}
		tooltip="<B><U>"+tableElement.getDisplayName()+"</U></B>";
		switch (tooltipType) {
		case Description:
			tooltip+="<BR/>"+tableElement.getElementDescription().getDescription();
			break;
		case EnglishDescription:
			tooltip+="<BR/>"+tableElement.getElementDescription().getEnglishDescription();
			break;
		case Description_detail:
			tooltip+="<BR/>"+tableElement.getElementDescription().getDescription();
			tooltip+=getDetailTooltip();
			break;
		case EnglishDescription_detail:
			tooltip+="<BR/>"+tableElement.getElementDescription().getEnglishDescription();
			tooltip+=getDetailTooltip();
			break;
		default:
			break;
		}
		if(logger.isDebugEnabled())
		{
			tooltip+="<br>STR:"+tableElement.getParentStructure();
			tooltip+=getDetailTooltip()+"<BR/>TN:"+tableElement.getNameDB();
		}
		tooltip =MainUIFrame.getToolTipStringN(tooltip);
		if(popupmenu!=null)
			popupmenu.updateTooltip(tooltip);

		this.setToolTipText(tooltip);
	}
	private String getDetailTooltip()
	{
		String detailTooltip="";
		if(tableElement.getElementDescription().hasReferenceTable())
		{
			for (EADBDescription.TableReference referencedTable : tableElement.getElementDescription().getReferredTables()) {
				detailTooltip += "<br><b>"+referencedTable.getReferenceType()+"</b>-"+referencedTable.getReferencedTable().getDisplayname();
			}
		}
		if(tableElement.getIsMandatory())
			detailTooltip+="<br>Is Mandatory";
		if(tableElement.getIsMultiple())
			detailTooltip+="<br>Is Multiple";
		if(tableElement.getHasFormat())
			detailTooltip+="<br><b>Format</b>: "+tableElement.getFormat();
		return detailTooltip;
	}
	DBDescriptionManager popupmenu;
	private void setPopup()
	{
		if(MainUIFrame.getUserLevel().equals(UserLevels.Admin)&&appProperty.getDisplayEditDescriptionMenu())
		{
			EADBDescription tabledes=tableElement.getElementDescription();
//			logger.error(tabledes.getDisplayname()+".."+this.getToolTipText());
			if(popupmenu==null)
				popupmenu = new DBDescriptionManager(tabledes.getNameDB(),tabledes.getDisplayname(),
						tabledes.getDescription(),tabledes.getEnglishDescription(),this.getToolTipText());
			labElement.addMouseListener(popupmenu);
		}
	}
	private void removePopup()
	{
		if(popupmenu!=null)
			labElement.removeMouseListener(popupmenu);
	}
	public void setValue(List<DBField> dataFields)
	{
//		remove existing
		removeValueTextField();
		if(dataFields.size()==0)
		{
			addValueTextField(null);
		}
		else
		{
			//add text field;
			for (DBField dataField : dataFields) {
				addValueTextField(dataField);
				logger.debug(" +++++ "+tableElement.getElementDescription().getName()+":"+dataField.toString());
			}
		}
	}

	public void resetValue()
	{
		removeValueTextField();
		addValueTextField(null);
	}

	/**
	 * Gets the current access type (If the element is public or private.). 
	 * @return 
	 * <code>true</code>: The element/object is public. <br>
	 * <code>false</code>: The element/object is private.
	 */
	public Boolean isPublics()
	{
		return tfIsPublic.isSelected();
	}
	public void updateTableData(int tableData) throws Exception
	{
		String query="UPDATE "+tableElement.getNameDB()+" SET content='"+getValue()+"' WHERE ID="+tableData;
		DBSqlHandler.getInstance().executeQuery(query);
	}

	/**
	 * ID of the updated reference. Used when editing element with
	 * TakesReferenceFrom property.
	 */
	private int referenceUpdateID=-1;

	public void setReferenceUpdateID(int referenceUpdateID)
	{
		logger.error("----- "+referenceUpdateID);
		if(isTableValueElement())
			parentTablePanel.setReferenceUpdateID(referenceUpdateID);
		else
			this.referenceUpdateID=referenceUpdateID;
	}

	/**
	 * Saves the data
	 * @param parentDataId The ID of the parent table.
	 * @throws Exception Exception saving the data.
	 */
	public void saveDataField(int parentDataId,boolean isNew)throws Exception
	{
		if(parentReferenceType!=null&&parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
			saveReferenceData(parentDataId,isNew);
		else		
			saveNormalData(parentDataId);
	}
	private void saveNormalData(int parentDataId)throws Exception
	{
//		logger.error("Save Data Field : "+tableElement.getNameDB());
		int i = 0;
		for (ValueElements vElement : valueElementList) {
			DataFieldUI tField = vElement.getValueElement();
			switch (tField.getUpdateType()) {
			case Updated:
				//check if mandatory
				if(tableElement.getIsMultiple()&&tField.getText().trim().equals(""))
				{
					//Delete value
					tableElement.removeContent(parentDataId, tField.getDBField().getID());
				}else
					try{
						tField.updateField(tfIsPublic.isSelected());
					}catch(Exception e)
					{
						tableElement.addContent(tField.getText().trim(), tfIsPublic.isSelected(),parentDataId);
					}
					break;
			case NewData:
				tableElement.addContent(tField.getText().trim(), tfIsPublic.isSelected(),parentDataId);
				if(hasAlternate)
				{
//					Get ID of the inserted row
					String sqlStatement ="SELECT *  FROM "+tableElement.getElementDescription().getNameDB()+" order by ID DESC limit 1";
					try {
						DBField dbfield = DBSqlHandler.getInstance().getDataField(sqlStatement, tableElement.getElementDescription().getNameDB());
						vElement.getAltButton().setElementDBField(dbfield);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case None:

				if(!originalIsPublic.equals(tfIsPublic.isSelected()))
				{
					logger.debug("Public private changed.");
					tField.updateField(tfIsPublic.isSelected());
				}
			default:
				break;
			}
			i++;
		}
	}

	private void saveReferenceData(int parentDataId,boolean isNew)throws Exception
	{
//		logger.error("1111 "+referenceUpdateID);
		for (ValueElements vElement : valueElementList) {
			DataFieldUI tField = vElement.getValueElement();
			// Update existing data.
			if(!isNew)
			{
				logger.error(" UPDATE "+elementName+"--"+parentDataId+"_"+referenceUpdateID);
				//check if mandatory
				if(tableElement.getIsMultiple()&&tField.getText().trim().equals(""))
				{
					//Delete value
					tableElement.removeContent(parentDataId, tField.getDBField().getID());
				}else
					try{
						tField.updateField(tfIsPublic.isSelected());
					}catch(Exception e)
					{
						tableElement.addContent(tField.getText().trim(), tfIsPublic.isSelected(),parentDataId);
					}
			}
			//save new data
			else
			{
				logger.error(" SAVE NEW "+elementName+"--"+parentDataId+"_"+referenceUpdateID);

				String query="INSERT INTO "+tableElement.getRelationDescrTableName()+" (CreationDate,ID1,ID2)"+ 
				" VALUES ("+DBConstants.SQL_TIMESTAMP+","+parentDataId+","+referenceUpdateID+")";
				logger.error("SQL "+query);

				DBSqlHandler.getInstance().executeQuery(query);
			}
			referenceUpdateID=-1;
		}
	}
	/**
	 * Gets the value of the first data element.
	 * @return The value of the first element.
	 */
	public String getValue()
	{
		return valueElementList.get(0).getValueElement().getText().trim();
	}
	public List<String> getValueList()
	{
		List<String> vals=new ArrayList<String>();
		for (ValueElements vel : valueElementList) {
			vals.add(vel.getValueElement().getText());
		}
		return vals;
	}
	public Boolean isSaveNecessary() throws DBException, Exception
	{ 
		Boolean isValid=false;
		Boolean saveNecessary=false;

		for (ValueElements vElement : valueElementList) {
			DataFieldUI tField=vElement.getValueElement();
			if(!saveNecessary) // if the data has not been updated then check
				if(!tField.getUpdateType().equals(DataFieldUIManager.updateType.None))
					saveNecessary=true;
			if(!saveNecessary)
				if(!originalIsPublic.equals(tfIsPublic.isSelected()))
					saveNecessary=true;

			//check if mandatory at least one field is specified
			isValid=isMandatoryValid(tField);
			//TODO: check format
		}
		if(tableElement.getIsMandatory())
		{
			if(isValid)
				return saveNecessary;
			else
				throw new DBException(DBException.DATA_MANDATORY_INVALID, "Mandatory field '"+this.elementName+"' NOT specified.");
		}
		else
			return saveNecessary;
	}
	private Boolean isMandatoryValid(DataFieldUI tField)
	{  
		if(tableElement.getIsMandatory())
		{
			return tField.getText().trim().length()>0;//if atleast one is specified then it is valid
		}
		else
			return true;
	}

	/**
	 * Initialises the GUI.
	 */
	private void initGUI() {
		try {
			FlowLayout panelLayout = new FlowLayout();
			panelLayout.setAlignment(FlowLayout.LEFT);

			this.setAlignmentY(Component.TOP_ALIGNMENT);
			this.setLayout(panelLayout);


			if(tableElement.getHasFormat())
			{
				String fmt = tableElement.getElementDescription().getFormat();
				elementName=Utils.getShortendText(elementName,25-(fmt.length()+2));
				elementDisplayString=elementName+" ("+fmt+")";
			}
			else
			{
				elementDisplayString=Utils.getShortendText(elementName,25);
			}

			//if takes reference from
			if(parentReferenceType!=null && 
					parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom)&&
					!tableElement.isTableValueElement())
			{
				labElement = new JLabel(new ImageIcon("res/images/reference.gif"),JLabel.LEFT);
			}
			else
				// Data Element Label
				labElement = new JLabel();

			labElement.setFont(AppConstants.APP_FONT_DEFAULT);

			labElement.setPreferredSize(new Dimension(150,labElementHeight));

			labElement.setHorizontalAlignment(SwingConstants.RIGHT);

			this.add(labElement);
			labElement.setText(elementDisplayString);

			// Mandatory
			if(tableElement.getIsMandatory())
				labElement.setText("<html><body>"+labElement.getText()+"<b> *</b>"+"</body></html>");

			if(logger.isDebugEnabled())
				labElement.setText(tableElement.getRelationDescriptionTable().getOrderNumber()+":"+labElement.getText());
			else
				labElement.setText(labElement.getText());

			//Create value element panel
			valueElementPanel = new JPanel();
			valueElementPanel.setBackground(this.getBackground());

			this.add(valueElementPanel);

			// Add isPublic checkBox
			tfIsPublic=new JCheckBox();
			tfIsPublic.setBackground(bgColor);
			tfIsPublic.setRolloverEnabled(true);
			if(tableElement.getIsPublicDefault())
			{
				tfIsPublic.setIcon(new ImageIcon("res/images/lock.gif"));
				tfIsPublic.setSelectedIcon(new ImageIcon("res/images/lock-open.gif"));
				tfIsPublic.setDisabledIcon(new ImageIcon("res/images/lock-dis.gif"));
				tfIsPublic.setDisabledSelectedIcon(new ImageIcon("res/images/lock-open-dis.gif"));
				tfIsPublic.setRolloverIcon(new ImageIcon("res/images/lock-r.gif"));
				tfIsPublic.setRolloverSelectedIcon(new ImageIcon("res/images/lock-open-r.gif"));
			}else
			{
				tfIsPublic.setIcon(new ImageIcon("res/images/lockperm.png"));
				tfIsPublic.setSelectedIcon(new ImageIcon("res/images/lockperm-open.png"));
				tfIsPublic.setDisabledIcon(new ImageIcon("res/images/lockperm-dis.png"));
				tfIsPublic.setDisabledSelectedIcon(new ImageIcon("res/images/lockperm-open-dis.png"));
				tfIsPublic.setRolloverIcon(new ImageIcon("res/images/lockperm-r.png"));
				tfIsPublic.setRolloverSelectedIcon(new ImageIcon("res/images/lockperm-open-r.png"));
			}
			tfIsPublic.setSelected(originalIsPublic);

			tfIsPublic.setEnabled(false);
			tfIsPublic.setFocusable(true);

			tfIsPublic.setVerticalAlignment(SwingConstants.TOP);
			this.add(tfIsPublic);

			//	Is Multiple
			if(tableElement.getIsMultiple())
			{
				butMultiple=new NaviButtonBase("add","add.gif","add-dis.gif","add-r.gif");

				butMultiple.setToolTipText("Click to add multiple value");
				butMultiple.setFocusable(false);
				butMultiple.setActionCommand(CMD_SAVE);
				butMultiple.addActionListener(this);
				butMultiple.setVerticalAlignment(SwingConstants.TOP);
				butMultiple.setBackground(bgColor);
				this.add(butMultiple);
			}
			if(MainUIFrame.displayDeleteButton()&&!tableElement.getIsMandatory())
			{
				butDelete = new NaviButtonBase("delete","delete.gif","delete-dis.gif","delete-r.gif");
				butDelete.setToolTipText("Click to delete value");
				butDelete.setFocusable(false);
				butDelete.setActionCommand(CMD_DELETE);
				butDelete.addActionListener(this);
				butDelete.setVerticalAlignment(SwingConstants.TOP);
				butDelete.setBackground(bgColor);
				this.add(butDelete);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(CMD_SAVE))
		{
			addValueTextField(null);
			setMode(true);
			this.updateUI();
		}
		else if(e.getActionCommand().equals(CMD_DELETE))
		{
			deleteElementValue();
		}
	}

	/**
	 * Gets list of all necessary queries to remove the element data.
	 * @return List of queries necessary to remov the element data.
	 */
	protected List<String> getDeleteElementQueries()
	{
		List<String> queryList = new ArrayList<String>();
		// Delete in table,reference table, alternates
		for (ValueElements vel : valueElementList) {
			if(vel.getValueElement().getDBField()!=null)
			{
				int valID=vel.getValueElement().getDBField().getID();
				//add delete query to remove data from the element table
				queryList.add("DELETE FROM "+tableElement.getNameDB()+" WHERE ID="+valID);
				// add delete query to remove data from the connecting table
				if(tableElement.getRelationDescriptionTable()!=null)
					queryList.add("DELETE FROM "+tableElement.getRelationDescrTableName()+" WHERE ID2="+valID);

				//logger.error("Query 1"+delFromTable+"\r\nQuery 2"+delFromConnTable);
				for (AlternateDetails alts : vel.altElementList) {
					for (DBField altval : alts.getAlternateValueFieldList()) {
						//add delete query to remove alternate value from its table
						queryList.add("DELETE FROM "+alts.getAlternateElementNameDB()+" WHERE ID="+altval.getID());
						//add delete query to remove alternate value from the connecting table
						if(alts.getAlternateConnectingNameDB()!=null)
							queryList.add("DELETE FROM "+alts.getAlternateConnectingNameDB()+" WHERE ID2="+altval.getID());
					}
				}
			}
		}
		return queryList;
	}
	private void deleteElementValue()
	{
		// display warning
		String warnmsg = "<html>Do you really want to delete?";
		warnmsg+= "<br>You are deleting <b>"+valueElementList.size()+"</b> data.";
		if(getValueCount()>0)
			warnmsg+= "<br> And also <b>"+valueElementList.size()+"</b> alternate data.";
		warnmsg+="</html>";
		int res = JOptionPane.showConfirmDialog(null, warnmsg, "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,new ImageIcon("res/images/warn.gif"));
		if(res==0)
		{
			List<String> queryList = getDeleteElementQueries();

			for (String query : queryList) {
				logger.error("Delete qury: "+query);
			}
			try {
				DBSqlHandler.getInstance().executeTransactionQuery(queryList);
				MainUIFrame.setStatusMessage("Data Deleted!");
				//refresh field.
				setValue(new ArrayList<DBField>());

			} catch (DBException e) {
				MainUIFrame.setStatusMessage("Data not deleted", MainUIFrame.MessageLevel.warn);
			} catch (Exception e) {
				MainUIFrame.setStatusMessage("Data not deleted", MainUIFrame.MessageLevel.warn);
			}
		}
	}
	/**
	 * Number of alternate values.
	 * @return
	 */
	private int getValueCount()
	{
		int vcount=0;
		if(alternateElementList==null&&alternateElementList.size()>0)
			return vcount;
		else
		{
			for (AlternateDetails altdetails : alternateElementList) {
				if(altdetails.hasAlternateValues())
				{
					vcount+= altdetails.getAlternateValueString().length;
				}
			}
			return vcount;
		}
	}
	/**
	 * Removes all text fields for values.
	 */
	private void removeValueTextField()
	{
		for (ValueElements vElement : valueElementList) {

			valueElementPanel.remove(vElement.getValueElement().getJComponent());
			if(hasAlternate)
				valueElementPanel.remove(vElement.getAltButton().getButton());
		}

		valueElementList.clear();
	}
	private void addValueTextField(DBField dbField)
	{
		// If to display text area then check if the length of the value
		// is longer than normal text field length
		boolean valLongerThanField=false;
		if(appProperty.getDisplayTextArea())
		{
			if(checkValLength)
			{
				valLongerThanField=valLongerThanFieldLength();
				checkValLength=true;
			}
		}

		// create data field
		DataFieldUI tfElement = new DataFieldUI(dbField,
				valLongerThanField, 
				tableElement,this,
				this.getBackground()
		);

		//Create the value element based on the data field and add it to the list
		final ValueElements vElem = new ValueElements(tfElement,alternateElementList);
		valueElementList.add(vElem);

		// add the data field to the value element panel
		valueElementPanel.add(tfElement.getJComponent());

		//add alternate button if exist 
		if(hasAlternate)
		{
			valueElementPanel.add(vElem.getAltButton().getButton());
		}

		// set the value of the icon public/private
		if(dbField!=null)
		{
			originalIsPublic=dbField.getIsPublic();
			tfIsPublic.setSelected(originalIsPublic);
		}
		else
		{
			originalIsPublic=tableElement.getIsPublicDefault();
			tfIsPublic.setSelected(tableElement.getIsPublicDefault());
		}

		// if the UI is in data entry mode and if the property enable implies is true
		// then check if the field implies value of other field(s) then get implied value
		// and set it to the corresponding fields.
		if(mainPanel.getApplicationMode().equals(AppConstants.ApplicationModes.DataEntry)&& appProperty.getEnableImpliesFeature())
		{
			//logger.error("1111 "+doImply+"--"+impliesSql);
			// check if imply process is necessary
			if(doImply && impliesSql==null)
			{
				if(impliesList.size()>0)
				{
					//add focus listener if the field type is not combobox
					if(!vElem.getValueElement().getFieldUIType().equals(fieldUITypes.ComboBox)&&
							!vElem.getValueElement().getFieldUIType().equals(fieldUITypes.FormattedComboBox))
					{
						//logger.error("üüüüü111 "+vElem.getValueElement().getFieldUIType());
						vElem.getValueElement().getJComponent().addFocusListener(new FocusListener(){

							public void focusGained(FocusEvent e) {
								vElem.getValueElement().setOrgText();
							}

							public void focusLost(FocusEvent e) {
								if(vElem.getValueElement().isUpdated())
								{
									updateImpliesElements();
									if(!doImply)
										vElem.getValueElement().getJComponent().removeFocusListener(this);
								}
							}
						});
					}
					//field type is combobox
					else {
						((CustomComboBox)vElem.getValueElement().getJComponent()).setInitImply(true);
						((CustomComboBox)vElem.getValueElement().getJComponent()).setElementPanel(this);
					}
				}
			}
		}
		//logger.error(elementDisplayString+"##### "+ getValueComponent().getFieldUIType());
		// Set size of this element field.
		if(!getValueComponent().getFieldUIType().equals(fieldUITypes.TextArea))
			this.setMaximumSize(new Dimension(30000,AppConstants.ELEMENT_HEIGHT_NORMAL+15));
		else
			this.setMaximumSize(new Dimension(30000,appProperty.getTextAreaHeight()+15));
		//if(logger.isDebugEnabled())
		//this.setBorder(new LineBorder(Color.cyan));
	}
	/** 
	 * The flag to determine if checking the length of the value is longer than the normal text field.
	 * Initially the value is <code>true</code>. After checking this value is set to false to indicate
	 * that it is not necessary to check. 
	 */
	private boolean checkValLength=true;
	/**
	 * Checks if the maximum length of all values of the element and determines
	 * if the length of the value is longer than the specified text field.
	 * @return <code>true</code>: The length of the largest value of the element is longer than the
	 * specified length of the text field. <br>
	 * <code>false</code>: The length of the largest value of the element is shorter than the 
	 * specified length of the text field.
	 */
	private boolean valLongerThanFieldLength()
	{
		boolean isLong=false;
		String query = "select max(length(content)) from "+tableElement.getNameDB();
		try {
			String val =DBSqlHandler.getInstance().executeGetValue(query);

			//logger.info(tableDspName+ "MAX VAL LENGTH: "+elementDBName+"-"+val+appProperties.getValueElementLength()+"::"+appProperties.getValueElementLength()/8);
			int intval=Integer.valueOf(val).intValue();
			if(intval>appProperty.getValueElementLength()/8)
				isLong=true;
		} catch (DBException e) {
			//logger.warn("Cannot get maximum value length of "+tableElement.getNameDB()+":"+e.getDetailedMsg());
		} catch (Exception e) {
			//logger.warn("Cannot get maximum value length of "+tableElement.getNameDB()+":"+e.getMessage());
		}
		return isLong;
	}

	/**
	 * If this element implies values of other elements then gets corresponding values
	 * and sets the values.
	 */
	public void updateImpliesElements()
	{
		//set sql and implies controls list and sql statements if not set yet.
		if(impliesControlList==null)
		{
			if(impliesTableSame())
			{
				setImpliesInfo();
			}
			else
			{
				doImply=false;
				logger.warn("Parent tables are not SAME!!! Implies will not work.");
			}
		}
		// now get and set the implied data
		if(doImply)
			setImpliedData();
		//logger.error("SQL "+impliesSql);
	}
	/**
	 * Gets and sets the implied data.
	 */
	private void setImpliedData()
	{

		try {
			//Query data
			String query = impliesSql+" WHERE "+tableElement.getNameDB()+".content='"+getValueComponent().getText()+"'";
			ResultSet rs = DBSqlHandler.getInstance().executeQuery(query);

			Hashtable<String,List<String>> impliedVals= new Hashtable<String, List<String>>();
			for (String impref : impliesList) {
				impliedVals.put(impref,new ArrayList<String>());
			}
			while( rs.next() ) {
				try {
					String val="";
					for (String impref : impliesList) {
						val=rs.getString(impref);
						if(val!=null&&!val.equals(""))
						{
							List<String> r = impliedVals.get(impref);
							if(!r.contains(val))
								r.add(val);
							impliedVals.put(impref,r);
						}
					}
				} catch (Exception e) {
					logger.error("Exception getting data fields:"+e.getMessage());
				}
			}
			//Display data
			for (String reskey : impliedVals.keySet()) {
				List<String> vals = impliedVals.get(reskey);
				//get ui
				DataFieldUI cnt = impliesControlList.get(reskey);
				if(cnt!=null)
					cnt.addImpliedValues(vals);
			}
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Prepares the information necessary to set implied values. 
	 * <li> Sets controls of the implied elements to update when necessary.
	 * <li> Sets sql statements to get corresponding values.
	 * <br>Example sql statement:
	 * <code><br><b>SELECT DISTINCT</b> Stadt.Content Stadt,Land.Content Land <br> 
	 * <b>FROM</b> Adresse <br>
	 * <dd>LEFT OUTER JOIN Adresse_Contains_Postleitzahl <br>
	 * <dd><dd>ON Adresse.id=Adresse_Contains_Postleitzahl.id1 <br>
	 * <dd>LEFT OUTER JOIN Postleitzahl <br>
	 * <dd><dd>ON Postleitzahl.id=Adresse_Contains_Postleitzahl.id2 <br>
	 * <dd>LEFT OUTER JOIN Adresse_Contains_Stadt <br>
	 * <dd><dd>ON Adresse.id=Adresse_Contains_Stadt.id1 <br>
	 * <dd>LEFT OUTER JOIN Stadt <br>
	 * <dd><dd>ON Stadt.id=Adresse_Contains_Stadt.id2 <br>
	 * <dd>LEFT OUTER JOIN Adresse_Contains_Land <br>
	 * <dd><dd>ON Adresse.id=Adresse_Contains_Land.id1 <br>
	 * <dd>LEFT OUTER JOIN Land <br>
	 * <dd><dd>ON Land.id=Adresse_Contains_Land.id2<br> 
	 * <b>WHERE</b> Postleitzahl.content='66903'
	 * </code>
	 */
	private void setImpliesInfo()
	{
		String sqlSelect="";

		String sqlFrom=" FROM "+tableElement.getTableName() +" LEFT OUTER JOIN "+tableElement.getRelationDescrTableName()+
		" ON "+tableElement.getTableName()+".id="+tableElement.getRelationDescrTableName()+".id1 "+
		" LEFT OUTER JOIN "+tableElement.getNameDB()+
		" ON "+tableElement.getNameDB()+".id="+tableElement.getRelationDescrTableName()+".id2";

		impliesControlList=new Hashtable<String,DataFieldUI>();

		for (String impliesReference : impliesList) {
			ElementPanel imprefpanel=mainPanel.getImpliesElement(impliesReference);
			impliesControlList.put(impliesReference, imprefpanel.getValueComponent());

			//prepare sql statement
			if(sqlSelect.equals(""))
				sqlSelect= "SELECT DISTINCT "+ imprefpanel.getNameDB()+ ".Content "+imprefpanel.getNameDB();
			else
				sqlSelect+=","+imprefpanel.getNameDB()+ ".Content "+imprefpanel.getNameDB();

			sqlFrom+= " LEFT OUTER JOIN "+imprefpanel.getParentConnectingTableName()+
			" ON "+imprefpanel.getParentTableName()+".id="+imprefpanel.getParentConnectingTableName()+".id1"+
			" LEFT OUTER JOIN "+imprefpanel.getNameDB()+
			" ON "+imprefpanel.getNameDB()+".id="+imprefpanel.getParentConnectingTableName()+".id2";
		}
		impliesSql = sqlSelect +sqlFrom;
	}

	/**
	 * Verifies if element that implies and affected element belongs to the same table.
	 * @return <code>true</code>: belong to the same table<br>
	 * <code>false</code>: does not belong to the same table
	 */
	private boolean impliesTableSame()
	{
		for (String impliesReference : impliesList) {
			ElementPanel imprefpanel=mainPanel.getImpliesElement(impliesReference);
			if(!imprefpanel.getParentTableName().equals(tableElement.getTableName()))
				return false;
		}
		return true;
	}


	public DataFieldUI getValueComponent()
	{
		return valueElementList.get(0).getValueElement();
	}

	private List<DBField> getAlternateElementValue(String alternateTableName,int dataID,String alternateType)
	{
		String elementName=tableElement.getNameDB();
		String relElementName=mainPanel.getConnectingTableByName(elementName, alternateTableName);
		//String relElementName=elementName+"_"+alternateType+"_"+alternateTableName;
		//String sql ="SELECT "+alternateTableName+".content "+
		String sql ="SELECT "+alternateTableName+".ID,"+alternateTableName+".creationdate,"+alternateTableName+".public,"+alternateTableName+".content "+
		"FROM "+elementName+", "+alternateTableName+", "+relElementName+
		" WHERE "+elementName+".id="+relElementName+".id1 AND "+alternateTableName+".id="+relElementName+".id2 AND "+
		elementName+".id="+dataID;
		if(logger.isDebugEnabled())
			logger.debug(dataID+" :SQL: "+sql);
		try {
			return DBSqlHandler.getInstance().getDataFieldList(sql, alternateTableName);
//			return DBSqlHandler.getInstance().getDataField(sql, alternateTableName);
		} catch (DBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public void setMode(boolean editMode)
	{
		setMode(editMode,editMode);	
	}
	public void setMode(boolean editModeElements,boolean editModeProp)
	{
		if(!tableElement.isParentTypeReferenced())
		{
			for (ValueElements vElement : valueElementList) {
				vElement.setMode(editModeElements);
			}
			tfIsPublic.setEnabled(editModeProp);

			if(butMultiple!=null)
				butMultiple.setEnabled(editModeProp);
			if(butDelete!=null&&!this.getValue().trim().equals(""))
				butDelete.setEnabled(editModeProp);
		}
		else
		{
			boolean doEnable=false;
			if(parentReferenceType!=null && 
					parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom)&&
					tableElement.isTableValueElement()&&
					editModeElements)
				doEnable=true;

			for (ValueElements vElement : valueElementList) {
				vElement.setMode(doEnable);
			}
			tfIsPublic.setEnabled(false);

			if(butMultiple!=null)
				butMultiple.setEnabled(false);
			if(butDelete!=null&&!this.getValue().trim().equals(""))
				butDelete.setEnabled(false);

		}

	}
	private class AltButton implements ActionListener{
		private NaviButtonBase altButton;

		private List<AlternateDetails> altElementList;
		private DBField elementDBField;
		private ValueElements vElement;

		public AltButton(List<AlternateDetails> altElementList,DBField elementDBField,ValueElements vElement)
		{
			this.altElementList =altElementList;
			this.elementDBField=elementDBField;
			this.vElement=vElement;

			if(!hasValue())
				altButton=new NaviButtonBase("alt","alt.gif","alt-dis.gif","alt-r.gif");
			else
				altButton=new NaviButtonBase("alt","alt-withval.gif","alt-withval-dis.gif","alt-r.gif");
			setTooltip();
			altButton.setBackground(bgColor);
			altButton.setBorder(new LineBorder(Color.red));
			altButton.setActionCommand(CMD_EDIT_ALTERNATE);
			altButton.addActionListener(this);
			setDialogueElements();
		}

		public NaviButtonBase getButton()
		{
			return altButton;
		}
		public void setEnabled(Boolean editMode)
		{
			if(elementDBField!=null)
				altButton.setEnabled(editMode);
			else
				altButton.setEnabled(false);
		}
		public void setElementDBField(DBField elementDBField)
		{
			this.elementDBField=elementDBField;
			setTooltip();
		}
		private void setTooltip()
		{
			boolean hasvalue=false;
			
			if(elementDBField==null&&alternateElementList==null&&alternateElementList.size()>0)
				altButton.setToolTipText("Edit Alternate");
			else
			{
				String tooltip="Edit Alternate";
				if(elementDBField!=null)
				for (AlternateDetails altdetails : alternateElementList) {
					if(altdetails.hasAlternateValues())
					{
						hasvalue=true;
						tooltip+="<br>"+altdetails.toString();
					}
				}
				
				altButton.setToolTipText(MainUIFrame.getToolTipString(tooltip));
			}
		}
		private boolean hasValue()
		{
			if(elementDBField==null&&alternateElementList==null&&alternateElementList.size()>0)
				return false;
			else
			{
				for (AlternateDetails altdetails : alternateElementList) {
					if(altdetails.hasAlternateValues())
					{
						return true;
					}
				}
				return false;
			}
		}
		/**
		 * Number of alternate values.
		 * @return
		 */
		private int getValueCount()
		{
			int vcount=0;
			if(elementDBField==null&&alternateElementList==null&&alternateElementList.size()>0)
				return vcount;
			else
			{
				for (AlternateDetails altdetails : alternateElementList) {
					if(altdetails.hasAlternateValues())
					{
						vcount+= altdetails.getAlternateValueString().length;
					}
				}
				return vcount;
			}
		}
		private Hashtable<String,AlternateDetails> getAlternateDetailsHast()
		{
			Hashtable<String,AlternateDetails> adh = new Hashtable<String, AlternateDetails>();
			for (AlternateDetails ad : alternateElementList) {
				adh.put(ad.getAlternateElementNameDB(),ad);
			}
			return adh;
		}

		private JTextField[] dialogTextFields;

		private void setDialogueElements()
		{
			int altSize = altElementList.size();

			int valcount=getValueCount();
			//logger.error(" VVVVVVV "+elementDisplayString+":"+valcount);
			if(valcount==0)
			{
				dialogTextFields=new JTextField[1];
				dialogTextFields[0]=new JTextField();
			}
			else	
				dialogTextFields=new JTextField[valcount];

			//Create labels
			for (AlternateDetails altDetails : altElementList) {
				int j=0;
				String[] altArray=altDetails.getAlternateValueString();
				if(altArray!=null)
					for (String altv : altDetails.getAlternateValueString()) {
						//logger.error(elementDisplayString+"------ "+getValueCount()+":::"+j++);
						dialogTextFields[j++]=new JTextField(altv);
					}
			}
			//logger.error(" SET DIALOGUE ELEMENTS "+elementDisplayString+"::"+dialogTextFields.length);
		}

		/**
		 * Alternate button is clicked.
		 */
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals(CMD_EDIT_ALTERNATE))
			{
				//The panel covering the whole frame.
				JPanel overalP =new JPanel();
				overalP.setFont(AppConstants.APP_FONT_DEFAULT);
				BoxLayout bl=new BoxLayout(overalP,BoxLayout.PAGE_AXIS);
				overalP.setAlignmentX(Component.LEFT_ALIGNMENT);
				//mainP.set
				overalP.setLayout(bl);

				//Panels for alternate types
				JPanel[] altTypesPanel = new JPanel[altElementList.size()];

				//JPanel dataP = new JPanel(new GridLayout(dialogTextFields.length,2));
				int i=0;
				for (AlternateDetails altDetails : altElementList) 
				{
					//Create label
					altTypesPanel[i]= new JPanel(new GridLayout(altDetails.getValueCount(),2));
					JLabel elementLabel=new JLabel("<html><FONT size=2>Edit "+
							altDetails.getAlternateTypeString()+":</FONT>"+ altDetails.getAlternateElementDisplayName()+
							"<br><FONT size=2>Orginal Val: </FONT><b>"+elementDBField.getContent()+"</b></html>");
					overalP.add(elementLabel);

					//Create text field for each value.
					int j=0;
					Hashtable<Integer,DBField> vt =altDetails.getAlternateValueFieldTable();
					if(vt.size()>0)
						for (DBField df : vt.values()) {
							JLabel l = new JLabel((j+1) + "");
							l.setHorizontalAlignment(SwingConstants.RIGHT);
							altTypesPanel[i].add(l);
							//logger.error(vt.size()+" SET DIALOGUE ELEMENTS22 "+elementDisplayString+".."+dialogTextFields.length+"..l.."+j);
							dialogTextFields[j]=new JTextField(df.getContent());
							dialogTextFields[j].setName(altDetails.getAlternateElementNameDB()+":"+ df.getID());
							altTypesPanel[i].add(dialogTextFields[j]);
							j++;
						}
					else
					{
						altTypesPanel[i].add(new JLabel((j+1) + ""));
						dialogTextFields[j]=new JTextField();
						dialogTextFields[j].setName(altDetails.getAlternateElementNameDB()+":null");
						altTypesPanel[i].add(dialogTextFields[j]);
					}

					overalP.add(altTypesPanel[i]);
					i++;
				}

				Object[] options = {"Save","Cancel"};
				int res=JOptionPane.showOptionDialog(null, overalP, "Alternate: "+labElement.getText(),JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon("res/images/ethnoarc_logo.png"), options, options[0]);
				Hashtable<String,AlternateDetails> adh = getAlternateDetailsHast();
				// save: Check if data has been modified.
				if(res==0)
				{
					for (JTextField tf : dialogTextFields) {
						String[] valuepair=tf.getName().split(":");
						AlternateDetails altelm = adh.get(valuepair[0]);

						// if alternate detail item found
						if(altelm!=null)
						{
							//check if save is necessary:
							if(!valuepair[1].equalsIgnoreCase("null"))
							{
								int altelmID = Integer.parseInt(valuepair[1]);

								if(logger.isDebugEnabled())
									logger.debug("----- "+valuepair[0]+"--"+valuepair[1]+" val: "+tf.getText()+" orgval: "+adh.get(valuepair[0]).getAlternateValue(altelmID));

								DBField orgvalfield = altelm.getAlternateValue(altelmID);

								if(orgvalfield!=null)
								{
									//compare original and new value

									if(!tf.getText().trim().equals(orgvalfield.getContent()))
									{
										if(logger.isDebugEnabled())
											logger.debug(altelm.getAlternateElementNameDB()+"---- SAVEEE "+altelmID+"-"+orgvalfield.getContent()+" new val: "+tf.getText().trim());
										altelm.saveAlternate(tf.getText().trim(), altelmID);
									}
								}
								else
									logger.warn("ID to update not found");
							}
							else
							{
								if(!tf.getText().trim().equals(""))
								{
									logger.debug("SAVE NEW ALTERNATE ");
									altelm.saveAlternate(tf.getText().trim(), -1);
								}
							}
						}//end if alternate detail object found

					}
				}
			}
		}
	}
	/**
	 * Class containing controls and objects related to the value of the elements.
	 * @author rva
	 */
	private class ValueElements{
		/**
		 * Data value field. E.g., textfield, combobox to display and update the value of the elements.
		 */
		private DataFieldUI valueElement;

		/**
		 * List of alternate elements.
		 */
		private List<AlternateDetails> altElementList = new ArrayList<AlternateDetails>();

		/**
		 * Control button related to update alternatives.
		 */
		private AltButton altButton;

		private boolean hasAlternate=false;

		public ValueElements(DataFieldUI valueElement, List<AlternateDetails> altElementList)
		{
			this.valueElement=valueElement;
			//if(!tableElement.isTableValueElement())
			this.altElementList=altElementList;
			//logger.error("....."+tableElement.getNameDB()+":"+altElementList.size());
			if(altElementList.size()>0)
			{
				hasAlternate=true;
				setValues();

				//create altbutton
				altButton = new AltButton(this.altElementList,valueElement.getDBField(),this);
			}
		}
		private void setValues()
		{
			DBField dbField = valueElement.getDBField();
			List<DBField> altVal;

			for (AlternateDetails altDetails : altElementList) {
				if(dbField!=null)
				{	
					altVal=getAlternateElementValue(altDetails.getAlternateElement().getNameDB(), dbField.getID(), altDetails.getAlternateTypeString());
					altDetails.setAlternateValue(altVal);
					altDetails.setElementID(dbField.getID());
				}
				else
				{
					altDetails.setAlternateValue(null);
					altDetails.setElementID(-1);
				}
			}
		}

		public void setAltElementList(List<AlternateDetails> altElementList)
		{
			//if(!tableElement.isTableValueElement())
			this.altElementList=altElementList;

		}
		public void setAltButtons(AltButton alternateButton)
		{
			this.altButton=alternateButton;
		}
		public void setMode(boolean editMode)
		{
			//valueElement.getJComponent().setEnabled(editMode);
			valueElement.setEnabled(editMode);
			if(hasAlternate)
				altButton.setEnabled(editMode);
		}
		public DataFieldUI getValueElement()
		{
			return valueElement;
		}
		public AltButton getAltButton()
		{
			//if(hasAlternate)
			return altButton;
		}
		public String getAltValue(int inx)
		{
			////	return altElementList.get(inx).getAlternateValueString();
			return "";
		}
	}
}

