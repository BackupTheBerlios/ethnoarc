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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBField;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBTableElement;
import de.fhg.fokus.se.ethnoarc.common.EADBDescription;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.TableReferenceTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
import de.fhg.fokus.se.ethnoarc.dbmanager.DataFieldUIManager.updateType;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;
import de.fhg.fokus.se.ethnoarc.dbmanager.helper.TextAreaEditor;

/**
 * $Id: DataFieldUI.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $
 * The user interface to display a data field. Different types of UI is used to
 * display different types of data fields specified by the list of table references
 * the field has. <br><br>
 * <i>Funtionalities:</i>
 * <li> Displays the content of the data field in appropriate way.
 * <li> The class also updates the value of the field. 
 *  <dl></dl>
 * @see EADBDescription.TableReference
 * @author rva
 */
public class DataFieldUI   implements ItemListener {
	static Logger logger = Logger.getLogger(DataFieldUI.class.getName());

	private final UserLevels userLevel=MainUIFrame.getUserLevel();

	private boolean isPublicDefault;
	/**
	 * The format of the field if specified. Empty string if the format is not specified.
	 */
	private String formatString="";
	/**
	 * The data field ui manager.
	 */
	private DataFieldUIManager dfmanager;

	/**
	 * The database field (data) of this element.
	 */
	private DBField dbField;
	/**
	 * The text field component. Used if the field does not have TakesValueFrom reference table and does not have
	 * any format specified.
	 */
	private JTextField tField;
	/**
	 * The combobox component. Used if the field have TakesValueFrom reference table.
	 */
	private CustomComboBox cBox;
	/**
	 * The formatted text field. Used if the field has the format specified.
	 */
	private InputFormatableTextField formattedTField;
	/**
	 * The name of the <code>TakesValueFrom</code> reference table. <code>null</code> 
	 * if the field does not have <code>TakesValueFrom</code> reference table.
	 */
	private String takesValueFromTableName;
//	private String takesReferenceFromTableName;
	private boolean isExclusiveTakesValueFrom=false;
	private CustomPasswordField  passField;
	private CustomTextArea textArea;
	private JScrollPane tAreaScrollPane;
	private AppPropertyManager appProperties;
	/**
	 * List of referenced elements.
	 * @see EADBDescription.TableReference
	 */
	List<EADBDescription.TableReference> referencedElementsList;
	private boolean isImpliedBy=false;
	/**
	 * Types of UI specified for the field. One of the field type is used based on the properties (referencedElementsList) of the table.
	 */
	public enum fieldUITypes{
		/** The text field type UI. */
		TextField, 
		/** Field to display long text */
		TextArea,
		/** Formatted text field type UI. */
		FormattedTextField,
		/** The combobox type UI. */
		ComboBox,
		/** The formatted Comboboxtype UI. */
		FormattedComboBox,
		/** Field that hides the value */
		PasswordField,
	}
	/**
	 * The type of UI specified for the field.
	 * @see fieldUITypes
	 */
	private fieldUITypes uiType;

	/**
	 * Possible values of the field to modify. The values are the values of the <code>TakesValueFrom</code> reference table.
	 * These values are added in the values of the combobox. <br>
	 * Is <code>null</code> if the field does not have <code>TakesValueFrom</code> reference table. 
	 */
	//private List<String> possibleValues;

	private int selectedItemInx=-1;

	private Color bg=AppConstants.APP_COLOR_DEFAULT;

	private String tableDspName;
	private boolean valLongerThanField=false;
	private DBTableElement tableElement;
	/**
	 * The type of reference between this table and its parent.
	 * @see TableReferenceTypes
	 */
	private TableReferenceTypes parentReferenceType;
	/**
	 * Panel of the parent table.
	 */
	private ElementPanel parentElementPanel;

	public DataFieldUI(DBField dbField,boolean valLongerThanField,
			DBTableElement tableElement,
//			TablePanel parentTablePanel,
			ElementPanel parentElementPanel,
			Color bgColor)
	{	
		this.tableElement=tableElement;
		this.formatString=tableElement.getElementDescription().getFormat();
		this.dfmanager= new DataFieldUIManager(dbField,tableDspName,formatString);
		this.dbField=dbField;
		this.valLongerThanField=valLongerThanField;
		this.isPublicDefault=tableElement.getIsPublicDefault();
		
//		this.parentTablePanel=parentTablePanel;
		this.parentElementPanel=parentElementPanel;

		this.referencedElementsList=tableElement.getElementDescription().getReferredTables();
		this.tableDspName=tableElement.getElementDescription().getName();
		this.isImpliedBy=tableElement.getElementDescription().isImpliedBy();
		this.bg=bgColor;

		EADBDescription relDescTable = tableElement.getRelationDescriptionTable();
		if(relDescTable!=null)
			parentReferenceType= TableReferenceTypes.valueOf(relDescTable.getType());

		try {
			appProperties= AppPropertyManager.getDBPropertyManagerInstant();
		} catch (DBException e1) {
			logger.warn("Error getting application properties: "+e1.getDetailedMsg());
		}

		//set display type
		setFieldUIType();
		//create ui
		createUI();

		//String contText = Utils.getShortendText(dbField.getContent(),25);
		// set data value 
		if(dbField!=null)
		{
			String val = dbField.getContent();
			switch (uiType) {
			case TextField:
				tField.setText(val);
				break;
			case TextArea:
				textArea.setText(val);
				break;
			case FormattedTextField:
				formattedTField.setText(val);
				break;
			case ComboBox:
				if(isImpliedBy)
					addvalue(val);
				cBox.setSelectedItem(val);
				selectedItemInx=cBox.getSelectedIndex();
				break;
			case FormattedComboBox:
				if(isImpliedBy)
					addvalue(val);
				cBox.setSelectedItem(val);
				break;
			case PasswordField:
				passField.setText(val);
				break;
			default:
				break;
			}

			getJComponent().setName(String.valueOf(dbField.getID()));
		}
		getJComponent().setEnabled(false);

		//get length of the value element control field.
		int length;
		try {
			length = appProperties.getDBPropertyManagerInstant().getValueElementLength();
		} catch (DBException e) {
			length=AppConstants.ELEMENT_VALUE_LENGTH_DEFAULT;
		}
		if(length<0)
		{
			length=AppConstants.ELEMENT_VALUE_LENGTH_DEFAULT;
		}

		if(!uiType.equals(fieldUITypes.TextArea))
			getJComponent().setPreferredSize(new java.awt.Dimension(length, AppConstants.ELEMENT_HEIGHT_NORMAL));
		else
			getJComponent().setPreferredSize(new java.awt.Dimension(length, appProperties.getTextAreaHeight()));

		JComponent c = this.getJComponent();
		if(appProperties.getDisplayPopupTextArea())
		{
			taEditor=new TextAreaEditor(this,false,tableDspName);
			//if(!uiType.equals(uiType.ComboBox)||!uiType.equals(uiType.FormattedComboBox))
			c.addMouseListener(taEditor);
		}
	}
	private TextAreaEditor taEditor;
	public DBField getDBField()
	{
		return dbField;
	}
	private void addvalue(String t)
	{
		boolean isAlreadyAdded=false;
		for (int i = 0; i < cBox.getItemCount(); i++) {
			if(!isAlreadyAdded)
			{
				if(cBox.getItemAt(i).equals(t))
				{
					isAlreadyAdded=true;
				}
				break;
			}
		}
		if(!isAlreadyAdded)
		{
			cBox.addItem(t);
		}
	}
	public boolean getIsExclusiveTakesValueFrom()
	{
		return isExclusiveTakesValueFrom;
	}
	public fieldUITypes getFieldUIType()
	{
		return uiType;
	}
	private boolean isReferenceSetter=false;
	private void setFieldUIType()
	{
		if(tableElement.isTableValueElement())
		{
			if(parentReferenceType!=null)
			{
				switch (parentReferenceType) {
				case TakesReferenceFrom:
					uiType=fieldUITypes.ComboBox;
					isExclusiveTakesValueFrom=true;
					isReferenceSetter=true;
					break;
				default:
					selectUI();
				break;
				}
			}
			else
				selectUI();
		}
		else
		{
			if(tableElement.isParentTypeReferenced())
			{
				if(!valLongerThanField)
					uiType = fieldUITypes.TextField;
				else
					uiType=fieldUITypes.TextArea;
			}
			else
			{
				if(parentReferenceType!=null)
				{
					switch (parentReferenceType) {
					case TakesReferenceFrom:
						uiType=fieldUITypes.ComboBox;
						isExclusiveTakesValueFrom=true;
						isReferenceSetter=true;
						break;
					default:
						selectUI();
					break;
					}

				}
				else
					selectUI();
			}
		}
	}
	private void selectUI()
	{
		if(tableElement.isParentTypeReferenced())
			if(!valLongerThanField)
				uiType = fieldUITypes.TextField;
			else
				uiType=fieldUITypes.TextArea;
		else
		{
			//if(dbField!=null&&userLevel.equals(UserLevels.Browser)&&(!isPublicDefault||!dbField.getIsPublic()))
			if(dbField!=null&&userLevel.equals(UserLevels.Browser)&&(!dbField.getIsPublic()))
			{
				uiType=fieldUITypes.PasswordField;
			}
			else
			{
				//get takes value from table name
				for (EADBDescription.TableReference tabref : referencedElementsList) {
					if(tabref.getReferenceType().equals(TableReferenceTypes.TakesValueFrom)||tabref.getReferenceType().equals(TableReferenceTypes.ExclusiveTakesValueFrom))
						//logger.error("--- "+tableDspName);
						takesValueFromTableName=tabref.getReferencedTable().getNameDB();
					if(tabref.getReferenceType().equals(TableReferenceTypes.ExclusiveTakesValueFrom))
						isExclusiveTakesValueFrom=true;
				}

				if((takesValueFromTableName==null||takesValueFromTableName.equals(""))&&!isImpliedBy)
				{
					if(formatString.equals("")){
						{
							if(!valLongerThanField)
								uiType = fieldUITypes.TextField;
							else
								uiType=fieldUITypes.TextArea;
						}
					}
					else
					{
						uiType = fieldUITypes.FormattedTextField;
					}
				}
				else
				{
					if(formatString.equals(""))
					{
						uiType = fieldUITypes.ComboBox;
					}
					else{
						uiType = fieldUITypes.FormattedComboBox;
					}
				}
			}
		}
	}

	/**
	 * Initializes necessary UI for the data field.
	 */
	private void createUI() 
	{

		switch (uiType) {
		case TextField:
			tField = new CustomTextField(bg);
			break;
		case TextArea:
			textArea=new CustomTextArea(bg);
			tAreaScrollPane=new JScrollPane(textArea);
			tAreaScrollPane.setBorder(new LineBorder(AppConstants.APP_COLOR_FIELD_BORDER,0));
			break;
		case FormattedTextField:
			try {
				formattedTField=new InputFormatableTextField(formatString,bg);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case ComboBox:
			try {
				cBox = new CustomComboBox(bg,AppPropertyManager.getDBPropertyManagerInstant().getEnableAutoComplete());
			} catch (DBException e) {
				cBox = new CustomComboBox(bg,true);				
			}
			//logger.error("----- "+tableDspName+":"+isExclusiveTakesValueFrom);
			if(!isExclusiveTakesValueFrom)
				cBox.setEditable(true);
			else
				cBox.setEditable(false);
			if(parentReferenceType!=null&&parentReferenceType.equals(TableReferenceTypes.Contains))
			{
				if(!isImpliedBy)
					setTakesValueFromValues();
			}
			else
			{
				setTakesReferenceFromValues();
			}

			break;
		case FormattedComboBox:
			try {
				cBox = new CustomComboBox(bg,AppPropertyManager.getDBPropertyManagerInstant().getEnableAutoComplete());
			} catch (DBException e) {
				cBox = new CustomComboBox(bg,true);				
			}
			//logger.error("-----2 "+tableDspName+":"+isExclusiveTakesValueFrom);
			if(!isExclusiveTakesValueFrom)
				cBox.setEditable(true);
			cBox.setEditor(new FormattableComboBox(formatString,bg));
			if(!isImpliedBy)
				setTakesValueFromValues();
			break;
		case PasswordField:
			passField=new CustomPasswordField(bg);
			break;
		default:
			break;
		}
	}


	/**
	 * Gets all values of the <code>TakesValueFrom</code> reference table if specified
	 * and puts it in the combobox as items.
	 */
	private void setTakesValueFromValues() 
	{
		try{
			String query="SELECT DISTINCT Content FROM "+takesValueFromTableName+" ORDER BY Content";
			List<String> values= DBSqlHandler.getInstance().getGetStringList(query);
			cBox.removeAllItems();
			cBox.addItem("");
			for (String avalue : values) {
				if(userLevel.equals(UserLevels.Browser)&&!isPublicDefault)
					cBox.addItem("***");
				else
					cBox.addItem(avalue);
			}
			if(AppPropertyManager.getDBPropertyManagerInstant().getEnableAutoComplete())
				cBox.initAutoComplete();
		}catch(Exception e){
			String msg = "Error getting values of the table "+takesValueFromTableName;
			logger.error(msg + tableDspName); 
			MainUIFrame.setStatusMessage(msg, MessageLevel.warn);		
		}
	}
	
	/**
	 * Notification on value change in the combo box. 
	 * Used for TakesReferenceFrom property to change the ID of the parent.
	 */
	public void itemStateChanged(ItemEvent ie) {
		String s = (String)ie.getItem();
		//if new value is selected.
		if(ie.getStateChange()== ItemEvent.SELECTED)
		{
			int selectedindx=  cBox.getSelectedIndex()-1;
			if(selectedindx>-1)
			{
				if(logger.isDebugEnabled())
					logger.debug("NEW "+s+"-"+refDataList.get(cBox.getSelectedIndex()-1).getID()+"::"+refDataList.get(cBox.getSelectedIndex()-1).getContent());
				parentElementPanel.setReferenceUpdateID(refDataList.get(cBox.getSelectedIndex()-1).getID());
			}
		}
//		if(ie.getStateChange()== ItemEvent.DESELECTED)
//		System.out.println("OLD "+s+"-");
	}
	private Hashtable<Integer,DBField> refDataList;
	private void setTakesReferenceFromValues() 
	{
		try{
			String query="SELECT * FROM "+tableElement.getNameDB()+" ORDER BY Content";
			List<DBField> values= DBSqlHandler.getInstance().getDataFieldList(query, tableElement.getNameDB());
			refDataList= new Hashtable<Integer, DBField>();
			cBox.removeAllItems();
			cBox.addItem("");
			int indx=0;
			for (DBField avalue : values) {
				refDataList.put(indx++, avalue);
				cBox.addItem(avalue.getContent());
			}
			if(AppPropertyManager.getDBPropertyManagerInstant().getEnableAutoComplete())
				cBox.initAutoComplete();
		}catch(Exception e){
			String msg = "Error getting values of the table "+tableElement.getNameDB();
			logger.error(msg + tableDspName); 
			MainUIFrame.setStatusMessage(msg, MessageLevel.warn);		
		}
	}
	private List<String> orgValues;
	public void addImpliedValues(List<String> impliedVals)
	{
		if(isImpliedBy)
		{	
			if(orgValues==null)
			{
				orgValues=new ArrayList<String>();
				for (int i = 0; i < cBox.getItemCount(); i++) {
					orgValues.add(cBox.getItemAt(i).toString());
				}
			}
			//remove previously assigned implied values
			List<String> itemstoremove=new ArrayList<String>(); 
			for (int i = 0; i < cBox.getItemCount(); i++) {
				if(!orgValues.contains(cBox.getItemAt(i).toString()))
					itemstoremove.add(cBox.getItemAt(i).toString());
			}
			for (String string : itemstoremove) {
				cBox.removeItem(string);
			}
			//now add new implied items.
			for (String val : impliedVals) {
				//logger.error(tableDspName+" ADD "+val);
				cBox.addItem(val);
			}
			//cBox.updateUI();
		}
	}

	/**
	 * Gets the UI component corresponding to the type of the data field.
	 * @return The component of the data UI.
	 */
	public JComponent getJComponent()
	{
		switch (uiType) {
		case TextField:
			return tField;
		case TextArea:
			//return textArea;
			return tAreaScrollPane;
		case FormattedTextField:
			return formattedTField;
		case ComboBox:
			return cBox;
		case FormattedComboBox:
			return cBox;
		case PasswordField:
			return passField;
		default:
			return null;
		}
	}
	private String orgText="";
	public void setOrgText()
	{
		orgText=getText();
	}
	public boolean isUpdated()
	{
		return !orgText.equals(getText());
	}
	/**
	 * Getts the current text of the data field in the UI.
	 * @return The current text of the data field in the UI.
	 */
	public String getText()
	{
		switch (uiType) {
		case TextField:
			return tField.getText().trim();
		case TextArea:
			return textArea.getText().trim();
		case FormattedTextField:
			return formattedTField.getText().trim();
		case ComboBox:
			try{
				return cBox.getSelectedItem().toString().trim();
			}catch(NullPointerException e)
			{
				return "";
			}
		case FormattedComboBox:
			try{
				return cBox.getSelectedItem().toString().trim();
			}catch(NullPointerException e)
			{
				return "";
			}
		case PasswordField:
			return String.valueOf(passField.getPassword());
		default:
			return "";
		}
	}
	public void setText(String t)
	{
		switch (uiType) {
		case TextField:
			tField.setText(t);
			break;
		case TextArea:
			textArea.setText(t);
			break;
		case FormattedTextField:
			formattedTField.setText(t);
			break;
		case ComboBox:
//			logger.error(tableDspName+ " dddd "+t);
			cBox.addItem(t);
			cBox.setSelectedItem(t);
			break;
		case FormattedComboBox:
//			logger.error(tableDspName+ " eee "+t);
			cBox.addItem(t);
			cBox.setSelectedItem(t);
			break;
		case PasswordField:
			passField.setText(t);
			break;
		default:
			break;
		}
	}

	/**
	 * Enables or disables the component.
	 * @param enabled Boolean value. <br><code>true</code>: Enables the data UI <br>
	 * <code>false</code>: Disables the data UI.
	 */
	public void setEnabled(Boolean enabled)
	{
		if(!uiType.equals(fieldUITypes.TextArea))
			getJComponent().setEnabled(enabled);
		else
			textArea.setEnabled(enabled);

		if(uiType.equals(fieldUITypes.ComboBox))
		{
			if(isReferenceSetter&&enabled)
			{
				cBox.addItemListener(this);
			}
			else
				cBox.removeItemListener(this);
		}
		if(taEditor!=null)
			if(enabled)
				taEditor.setText("Edit in text area.");
			else
				taEditor.setText("View in text area.");
	}


	public updateType getUpdateType()throws Exception
	{
		return dfmanager.getUpdateType(getText()); 
	}
	public void updateField(Boolean isPublic)throws Exception
	{
		
		String selVal=getText();
		
		if(uiType.equals(fieldUITypes.ComboBox)&&
				parentReferenceType!=null&&
				parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
		{
			
			int selValID=refDataList.get(cBox.getSelectedIndex()-1).getID();
			logger.error(" SAVE REFERENCE ID "+tableDspName+"----"+selValID);

			dfmanager.updateReferenceField(isPublic, selValID,tableElement.getRelationDescrTableName());
		}
		else
		{
			logger.error(" SAVE ELEMENT VAL "+tableDspName+"----"+selVal);
			dfmanager.updateField(isPublic, selVal);
		}

		if(uiType.equals(fieldUITypes.ComboBox))
		{
			if(parentReferenceType.equals(TableReferenceTypes.Contains))
				setTakesValueFromValues();
			else
				setTakesReferenceFromValues();
			cBox.setSelectedItem(selVal);
			selectedItemInx=cBox.getSelectedIndex();
		}
	}
}
