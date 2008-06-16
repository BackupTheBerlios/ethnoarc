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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import de.fhg.fokus.se.ethnoarc.common.DBException;
import de.fhg.fokus.se.ethnoarc.common.DBConstants.TableReferenceTypes;
import de.fhg.fokus.se.ethnoarc.dbmanager.AppConstants.UserLevels;
import de.fhg.fokus.se.ethnoarc.dbmanager.MainUIFrame.MessageLevel;

/**
 * $Id: NavigationPanel.java,v 1.1 2008/06/16 08:24:06 fchristian Exp $ 
 * @author fokus
 */
public class NavigationPanel extends JPanel implements ActionListener {
	static Logger logger = Logger.getLogger(NavigationPanel.class.getName());

	//private String tableName;
	private TablePanel tablePanel;
	private Boolean editMode=false;
	private Boolean newData=false;
	private Boolean multipleMode=true;
	private UserLevels userLevel=UserLevels.Browser;
	private TableReferenceTypes parentReferenceType;

	private Color bgColor;

	/**
	 * Button commands
	 */
	private static enum COMMANDS {
		Next,
		Previous,
		First,
		Last,
		ChangeDataIndex,
		New,
		Save,
		Cancel,
		Edit,
		Expand,
		Delete
	}

	public NavigationPanel(String tableName,TablePanel tablePanel)
	{
		//this.tableName=tableName;
		this.tablePanel=tablePanel;
		this.multipleMode=tablePanel.isMultipleMode();
		bgColor=tablePanel.getBackground();
		userLevel=MainUIFrame.getUserLevel();
		this.parentReferenceType=tablePanel.getParentReferenceType();
		initNavigationPanel();
		
		this.setBackground(bgColor);
		this.updateUI();
	}
	public void actionPerformed(ActionEvent e) {
		logger.debug(e.getActionCommand()+" command button PRESSED.");
		switch (COMMANDS.valueOf(e.getActionCommand())) {
		case Next:
			MainUIFrame.setIsPerformingTask(true);
			MainUIFrame.setStatusMessage("Getting next data...");
			new Thread()
			{
				public void run(){
					tablePanel.changeDataIndx(1);
					MainUIFrame.setIsPerformingTask(false);
					MainUIFrame.setStatusMessage("Data retrieved.");
				}
			}.start();
			
			break;
		case Previous:
			MainUIFrame.setIsPerformingTask(true);
			MainUIFrame.setStatusMessage("Getting previous data...");
			new Thread()
			{
				public void run(){
					tablePanel.changeDataIndx(-1);
					MainUIFrame.setIsPerformingTask(false);
					MainUIFrame.setStatusMessage("Data retrieved.");
				}
			}.start();
			break;
		case First:
			if(tablePanel.getCurrentDataIndex()!=0)
			{
				tablePanel.changeDataIndex(0);
			}
			break;
		case Last:
			int lastDataIndx = tablePanel.getDataSize()-1;
			if(tablePanel.getCurrentDataIndex()!=lastDataIndx)
			{
				tablePanel.changeDataIndex(lastDataIndx);
			}
			break;
		case ChangeDataIndex:
			MainUIFrame.setIsPerformingTask(true);
			MainUIFrame.setStatusMessage("Getting specified data...");
			new Thread()
			{
				public void run(){
					try{
						int newIndx=Integer.valueOf(tfCurrentData.getText());
						if(newIndx > 0 && newIndx <= tablePanel.getDataSize())
						{
							tfCurrentData.setBackground(Color.white);
							tablePanel.changeDataIndex(newIndx-1);
							MainUIFrame.setIsPerformingTask(false);
							MainUIFrame.setStatusMessage("Data retrieved.");
						}
						else
						{
							
							tfCurrentData.setBackground(Color.yellow);
							MainUIFrame.setIsPerformingTask(false);
							MainUIFrame.setStatusMessage("Specified val not within range. ",MessageLevel.error);
						}
					}catch(Exception ex)
					{
						MainUIFrame.setIsPerformingTask(false);
						MainUIFrame.setStatusMessage("Specified val not correct. ",MessageLevel.error);
						tfCurrentData.setBackground(Color.yellow);
					}
				}
			}.start();
			break;
		case New:
			setEditMode(true);
			newData=true;
			navForNewData();
			tablePanel.freezeSubTableNavControls();
			tablePanel.freezeParentTableNavControls();
			break;
		case Save:
			MainUIFrame.setIsPerformingTask(true);
			MainUIFrame.setStatusMessage("Saving data...");
			new Thread()
			{
				public void run(){

					try {
						tablePanel.saveData(newData);
						butSave.setEnabled(false);
						
						MainUIFrame.setStatusMessage("Data saved. updating ui...");
						logger.info("Data Saved");
					} catch (DBException e1) {
						logger.info(e1.getDetailedMsg());
						MainUIFrame.setIsPerformingTask(false);
						MainUIFrame.setStatusMessage("Data not saved: "+e1.getDetailedMsg(),MainUIFrame.MessageLevel.warn);
						return;
					} catch (Exception e1) {
						logger.error("+++"+e1);
						e1.printStackTrace();
						MainUIFrame.setIsPerformingTask(false);
						if(logger.isDebugEnabled())
						{
							MainUIFrame.setStatusMessage("Data not saved: "+e1.getMessage());
						}
						else
						{
							logger.error(e1.getMessage());
							MainUIFrame.setStatusMessage("Data not saved: ");
						}
						return;
					}
					setEditMode(false);
					newData=false;
					tablePanel.modeChanged(editMode);
					tablePanel.updateParentControls();
					tablePanel.updateSubTableNavControls();
					butSave.setEnabled(false);
					butCancel.setEnabled(false);
					if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
						butNew.setEnabled(true);

					cbPublicPrivate.setEnabled(false);
					updateControls();
					MainUIFrame.setIsPerformingTask(false);
					MainUIFrame.setStatusMessage("Data saved.");
				}
			}.start();
			break;
		case Cancel:
			setEditMode(false);
			newData=false;
			tablePanel.modeChanged(editMode);
			tablePanel.updateParentControls();
			tablePanel.updateSubTableNavControls();

			tablePanel.setTableData();
			updateControls();

			butSave.setEnabled(false);
			butCancel.setEnabled(false);
			if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
				butNew.setEnabled(true);
			//if(!tablePanel.getIsPublicDefault())
				cbPublicPrivate.setEnabled(false);
			break;
		case Edit:
			setEditMode(!editMode);
			butSave.setEnabled(true);
			butCancel.setEnabled(true);
			butNew.setEnabled(false);
			butEdit.setEnabled(false);

			cbPublicPrivate.setEnabled(true);
			updateControls();

			tablePanel.freezeSubTableNavControls();
			tablePanel.freezeParentTableNavControls();
			tablePanel.modeChanged(editMode);
			break;
		case Delete:
			tablePanel.deleteData();
			break;
		case Expand:
			if(butExpand.isSelected())
			{
				butExpand.setToolTipText("<html><body style=\"background-color:#f8f8f8\">Expand the panel.</html></body>");
				freezeNavControls();
			}
			else
			{
				butExpand.setToolTipText("<html><body style=\"background-color:#f8f8f8\">Collapse the panel.</html></body>");
				updateControls();
			}
			tablePanel.expandShrinkContentPanel(butExpand.isSelected());
		default:
			break;
		}
	}
	private void setEditMode(boolean editMode)
	{
		this.editMode=editMode;
		MainUIFrame.setUiEditMode(editMode);
	}
	public void setIsPublic(boolean isPublicDefault, boolean isPublic)
	{
		if(!isPublicDefault)
		{
			cbPublicPrivate.setSelected(false);
			cbPublicPrivate.setEnabled(false);
		}
		cbPublicPrivate.setSelected(isPublic);
	}
	public Boolean getIsPublic()
	{
		return cbPublicPrivate.isSelected();
	}
	public void navForNewData()
	{
		tablePanel.refreshTableElement();

		labDataCounter.setText("New");

		butPrevious.setEnabled(false);
		butNext.setEnabled(false);
		butEdit.setEnabled(false);
		butSave.setEnabled(true);
		butNew.setEnabled(false);
		butExpand.setEnabled(false);
		butCancel.setEnabled(true);
		
		if(tablePanel.getIsPublicDefault())
			cbPublicPrivate.setSelected(true);
		else
			cbPublicPrivate.setSelected(false);
		
		cbPublicPrivate.setEnabled(true);
		tfCurrentData.setEnabled(false);
		
		if(butDelete!=null)
			butDelete.setEnabled(false);
	}

	public void freezeNavControls()
	{
		butPrevious.setEnabled(false);
		butNext.setEnabled(false);
		butEdit.setEnabled(false);
		butSave.setEnabled(false);
		butNew.setEnabled(false);
		butCancel.setEnabled(false);
		tfCurrentData.setEnabled(false);
		if(butDelete!=null)
			butDelete.setEnabled(false);
	}
	/**
	 * Sets the data counter label.
	 */
	public void setDataCounterLabel()
	{
		int countVal=tablePanel.getCurrentDataIndex()+1;
		if(countVal!=0)
		{
			labDataCounter.setText(" of "+tablePanel.getDataSize());
			if(tablePanel.getDataSize()==0)
				tfCurrentData.setText("0");
			else
				tfCurrentData.setText(countVal+"");
		}
		else
		{
			labDataCounter.setText("No Data!");
			tfCurrentData.setText("0");
		}
		//labDataID.setText(tablePanel.getAssociateTable().getCurrentDataID()+"");
	}

	public void updateControls()
	{
		int currentDataIndex =-1;
		switch (tablePanel.getApplicationMode()) {
		case DataEntry:
			currentDataIndex = tablePanel.getCurrentDataIndex();

			//		in edit mode disable browse controls 
			if(MainUIFrame.getUiEditMode()) 
			{
				butPrevious.setEnabled(false);
				butNext.setEnabled(false);
				butExpand.setEnabled(false);
				tfCurrentData.setEnabled(false);
				if(butDelete!=null)
					butDelete.setEnabled(false);
				return;
			}
			butExpand.setEnabled(true);
			//if the table can have more than one value.
			if(tablePanel.isMultipleMode())
			{
				int datasize=tablePanel.getDataSize();
				labDataCounter.setEnabled(true);
				if(datasize<2) //Data size is less than 2
				{
					butPrevious.setEnabled(false);
					butNext.setEnabled(false);
					tfCurrentData.setEnabled(false);
				}
				else // Data size is more than one
				{
					tfCurrentData.setEnabled(true);
					if(currentDataIndex==0)
					{
						butPrevious.setEnabled(false);
						butNext.setEnabled(true);
					}
					else if(currentDataIndex==(datasize-1))
					{
						butPrevious.setEnabled(true);
						butNext.setEnabled(false);
					}
					else
					{
						butPrevious.setEnabled(true);
						butNext.setEnabled(true);
					}
				}
				if(currentDataIndex>-1)
				{
//					if(parentReferenceType==null || !parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butEdit.setEnabled(true);
				}
				else
				{
					butEdit.setEnabled(false);
					tablePanel.freezeSubTableNavControls();
				}

				if(tablePanel.getDataSize()<1)
				{
					if(tablePanel.getParentPanel()==null)
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
					if( tablePanel.getParentPanel()!=null&&tablePanel.getParentPanel().getDataSize()>0) 
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
				}
				else
				{
					if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
						butNew.setEnabled(true);
				}

				tablePanel.modeChanged(editMode);
			}
			else //if not multiple
			{
				if(tablePanel.getDataSize()==0)
				{
					butNew.setVisible(true);

					butEdit.setVisible(false);

					if(tablePanel.getParentPanel().getDataSize()>0)
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
					else
						butNew.setEnabled(false);
				}
				else
				{
					butNew.setVisible(false);
					butEdit.setVisible(true);

//					if(parentReferenceType==null || !parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor)|| tablePanel.getParentPanel().getDataSize()>0)
							butEdit.setEnabled(true);
						else
							butEdit.setEnabled(false);
				}
			}
			if(tablePanel.getDataSize()>0)
			{
				if(MainUIFrame.displayDeleteButton()&&butDelete!=null)
					butDelete.setEnabled(true);
			}
			else
			{
				if(butDelete!=null)
					butDelete.setEnabled(false);
			}
			break;
		case Search:
			currentDataIndex = tablePanel.getCurrentDataIndex();

			//		in edit mode disable browse controls 
			if(MainUIFrame.getUiEditMode()) 
			{
				butPrevious.setEnabled(false);
				butNext.setEnabled(false);
				butExpand.setEnabled(false);
				tfCurrentData.setEnabled(false);
				return;
			}
			butExpand.setEnabled(true);
			//if the table can have more than one value.
			if(tablePanel.isMultipleMode())
			{
				int datasize=tablePanel.getDataSize();
				labDataCounter.setEnabled(true);
				if(datasize<2) //Data size is less than 2
				{
					butPrevious.setEnabled(false);
					butNext.setEnabled(false);
					tfCurrentData.setEnabled(false);
				}
				else // Data size is more than one
				{
					tfCurrentData.setEnabled(true);
					if(currentDataIndex==0)
					{
						butPrevious.setEnabled(false);
						butNext.setEnabled(true);
					}
					else if(currentDataIndex==(datasize-1))
					{
						butPrevious.setEnabled(true);
						butNext.setEnabled(false);
					}
					else
					{
						butPrevious.setEnabled(true);
						butNext.setEnabled(true);
					}
				}
				if(currentDataIndex>-1)
				{
//					if(parentReferenceType==null || !parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
						if((userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))&& tablePanel.getDataSize()>0)
							butEdit.setEnabled(true);
					
					if(MainUIFrame.displayDeleteButton()&& butDelete!=null)
						butDelete.setEnabled(true);
				}
				else
				{
					butEdit.setEnabled(false);
					tablePanel.freezeSubTableNavControls();
				}

				if(tablePanel.getDataSize()<1)
				{
					if(tablePanel.getParentPanel()==null)
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
					if( tablePanel.getParentPanel()!=null&&tablePanel.getParentPanel().getDataSize()>0) 
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
				}
				else
				{
					if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
						butNew.setEnabled(true);
				}

				//tablePanel.modeChanged(editMode);
			}
			else //if not multiple
			{
				if(tablePanel.getDataSize()==0)
				{
					butNew.setVisible(true);

					butEdit.setVisible(false);

					if(tablePanel.getParentPanel().getDataSize()>0)
					{
						if(userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))
							butNew.setEnabled(true);
					}
					else
						butNew.setEnabled(false);
				}
				else
				{
					butNew.setVisible(false);
					butEdit.setVisible(true);

//					if(parentReferenceType==null || !parentReferenceType.equals(TableReferenceTypes.TakesReferenceFrom))
						if((userLevel.equals(UserLevels.Admin)||userLevel.equals(UserLevels.Editor))&& tablePanel.getDataSize()>0)
						{
							butEdit.setEnabled(true);
						}
						else
							butEdit.setEnabled(false);
				}
			}
			//butEdit.setEnabled(false);
			butSave.setEnabled(false);
			butNew.setEnabled(false);
			butCancel.setEnabled(false);
			//tfCurrentData.setEnabled(false);
			break;
		default:
			break;
		}

	}
//	---- Navigation Panel Controls
	public JToggleButton butExpand; 
	private JLabel labDataCounter;
	private CustomTextField tfCurrentData;
	private NaviButtonBase butPrevious;
	private NaviButtonBase butNext;
	private NaviButtonBase butNew;
	private NaviButtonBase butSave;
	private NaviButtonBase butCancel;
	private NaviButtonBase butEdit;
	private NaviButtonBase butDelete;
	private JCheckBox cbPublicPrivate;
	//private JLabel labDataID;

	private void initNavigationPanel()
	{
		FlowLayout navPanelLayout = new FlowLayout(FlowLayout.LEFT,5,0);

		this.setLayout(navPanelLayout);

		this.setAlignmentY(0.0f);
		this.setAutoscrolls(true);

		butExpand = new JToggleButton();
		//do not display the expand button in the first level table.
		if(tablePanel.getParentLevel()>0)
		{
			butExpand.setToolTipText("<html><body style=\"background-color:#f8f8f8\">Collapse the panel.</html></body>");
			butExpand.setSelectedIcon(new ImageIcon("res/images/expand.gif"));
			butExpand.setDisabledSelectedIcon(new ImageIcon("res/images/expand-dis.gif"));
			butExpand.setRolloverEnabled(true);
			butExpand.setRolloverSelectedIcon(new ImageIcon("res/images/expand-r.gif"));
			butExpand.setIcon(new ImageIcon("res/images/colapse.gif"));
			butExpand.setRolloverEnabled(true);
			butExpand.setRolloverIcon(new ImageIcon("res/images/colapse-r.gif"));
			butExpand.setDisabledIcon(new ImageIcon("res/images/colapse-dis.gif"));
			butExpand.setBackground(bgColor);
			butExpand.setActionCommand(COMMANDS.Expand.toString());
			butExpand.addActionListener(this);
			butExpand.setFont(new java.awt.Font("Times New Roman",0,9));
			butExpand.setMargin(new Insets(0,0,0,0));

			butExpand.setBorderPainted(false);
			butExpand.setFocusPainted(false);
			this.add(butExpand);
		}

		// Button - EDIT
		butEdit = new NaviButtonBase(COMMANDS.Edit.toString(),"edit.gif","edit-dis.gif","edit-r.gif");
		this.add(butEdit);
		butEdit.setBackground(bgColor);
		butEdit.setToolTipText("Edit data");
		butEdit.setActionCommand(COMMANDS.Edit.toString());
		butEdit.setEnabled(false);
		butEdit.addActionListener(this);

		//	Button - NEW
		butNew = new NaviButtonBase(COMMANDS.New.toString(),"new.gif","new-dis.gif","new-r.gif");
		butNew.setBackground(bgColor);;
		this.add(butNew);
		butNew.setToolTipText("Create New Data");
		butNew.setActionCommand(COMMANDS.New.toString());
		butNew.setEnabled(false);
		butNew.addActionListener(this);

		//	Button - SAVE
		butSave = new NaviButtonBase(COMMANDS.Save.toString(),"save.gif","save-dis.gif","save-r.gif");
		butSave.setBackground(bgColor);
		//butSave.setDisabledIcon("save-dis.gif");
		this.add(butSave);
		butSave.setToolTipText("Save Data");
		butSave.setActionCommand(COMMANDS.Save.toString());
		butSave.setEnabled(false);
		butSave.addActionListener(this);

		//	Button - CANCEL
		butCancel = new NaviButtonBase(COMMANDS.Cancel.toString(),"clear.gif","clear-dis.gif","clear-r.gif");
		butCancel.setBackground(bgColor);
		this.add(butCancel);
		butCancel.setToolTipText("Cancel");
		butCancel.setActionCommand(COMMANDS.Cancel.toString());
		butCancel.setEnabled(false);
		butCancel.addActionListener(this);

		// Checkbox private/public data
//		Add isPublic checkBox
		cbPublicPrivate=new JCheckBox();
		cbPublicPrivate.setBackground(bgColor);
		
		if(tablePanel.getIsPublicDefault())
		{
			cbPublicPrivate.setIcon(new ImageIcon("res/images/lock.gif"));
			cbPublicPrivate.setSelectedIcon(new ImageIcon("res/images/lock-open.gif"));
			cbPublicPrivate.setDisabledIcon(new ImageIcon("res/images/lock-dis.gif"));
			cbPublicPrivate.setDisabledSelectedIcon(new ImageIcon("res/images/lock-open-dis.gif"));
			cbPublicPrivate.setRolloverIcon(new ImageIcon("res/images/lock-r.gif"));
			cbPublicPrivate.setRolloverSelectedIcon(new ImageIcon("res/images/lock-open-r.gif"));
		}else
		{
			cbPublicPrivate.setIcon(new ImageIcon("res/images/lockperm.png"));
			cbPublicPrivate.setSelectedIcon(new ImageIcon("res/images/lockperm-open.png"));
			cbPublicPrivate.setDisabledIcon(new ImageIcon("res/images/lockperm-dis.png"));
			cbPublicPrivate.setDisabledSelectedIcon(new ImageIcon("res/images/lockperm-open-dis.png"));
			cbPublicPrivate.setRolloverIcon(new ImageIcon("res/images/lockperm-r.png"));
			cbPublicPrivate.setRolloverSelectedIcon(new ImageIcon("res/images/lockperm-open-r.png"));
			cbPublicPrivate.setToolTipText("Data Locked");
		}

		//cbPublicPrivate.setSelectedIcon(new ImageIcon("res/images/lock-open.gif"));

		cbPublicPrivate.setSelected(true);
		cbPublicPrivate.setEnabled(false);
		cbPublicPrivate.setToolTipText("Access control for the data.");
		this.add(cbPublicPrivate);


//		Button Previous
		butPrevious = new NaviButtonBase(COMMANDS.Previous.toString(),"previous.gif","previous-dis.gif","previous-r.gif");
		butPrevious.setBackground(bgColor);
		this.add(butPrevious);
		butPrevious.setToolTipText("Goto previous data");
		butPrevious.setActionCommand(COMMANDS.Previous.toString());
		butPrevious.addActionListener(this);

		//current data text field
		tfCurrentData=new CustomTextField(bgColor);

		// current data index
		tfCurrentData.setPreferredSize(new Dimension(40,15));
		tfCurrentData.setHorizontalAlignment(SwingConstants.RIGHT);
		tfCurrentData.setFont(AppConstants.APP_FONT_DEFAULT);
		tfCurrentData.setActionCommand(COMMANDS.ChangeDataIndex.toString());
		tfCurrentData.addActionListener(this);
		tfCurrentData.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				//do nothing
			}

			public void focusLost(FocusEvent e) {
				setDataCounterLabel();
			}});
		this.add(tfCurrentData);

//		Label Table name
		labDataCounter = new JLabel();
		labDataCounter.setFont(AppConstants.APP_FONT_DEFAULT);
		this.add(labDataCounter);
		labDataCounter.setText("NA");

		// Button Next
		butNext = new NaviButtonBase(COMMANDS.Next.toString(),"next.gif","next-dis.gif","next-r.gif");
		butNext.setBackground(bgColor);
		this.add(butNext);
		butNext.setToolTipText("Goto next data");
		butNext.setActionCommand(COMMANDS.Next.toString());
		butNext.addActionListener(this);
		
		// Delete button
		if(MainUIFrame.displayDeleteButton())
		{
			butDelete = new NaviButtonBase(COMMANDS.Next.toString(),"delete.gif","delete-dis.gif","delete-r.gif");
			butDelete.setBackground(bgColor);
			this.add(butDelete);
			butDelete.setToolTipText("Delete Data: "+tablePanel.getName());
			butDelete.setActionCommand(COMMANDS.Delete.toString());
			butDelete.addActionListener(this);
			butDelete.setEnabled(false);
		}

		if(!multipleMode)
		{
			butPrevious.setVisible(false);
			butNext.setVisible(false);
			butNew.setVisible(false);
			labDataCounter.setVisible(false);
			tfCurrentData.setVisible(false);
		}

		// ID label for debugging
		/*if(logger.isDebugEnabled())
		{
			try{
				int orderNum=tablePanel.getAssociateTable().getCurrentDataID();

				labDataID = new JLabel(orderNum+"");

				labDataID.setFont(new java.awt.Font("Times New Roman",1,9));
				this.add(labDataID);
			}catch(NullPointerException e)
			{

			}
		}*/
	}
}
