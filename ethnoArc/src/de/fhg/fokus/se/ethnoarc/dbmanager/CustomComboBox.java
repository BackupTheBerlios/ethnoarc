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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
public class CustomComboBox extends JComboBox	implements JComboBox.KeySelectionManager,FocusListener
{
	private String searchFor;
	private long lap;
	private boolean autoComplete=true;
	private boolean initImply=false;
	private Color bg;
	public class CBDocument extends PlainDocument
	{
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
		{
			if (str==null) return;
			super.insertString(offset, str, a);
			if(!isPopupVisible() && str.length() != 0) fireActionEvent();
		}
	}
	public CustomComboBox(Object[] items,Color bg)
	{
		super(items);
		this.bg=bg;
		initui();
	}
	public CustomComboBox(Color bg,boolean autoComplete)
	{
		super();
		this.bg=bg;
		this.autoComplete=autoComplete;
		initui();
	}
	private void initui()
	{
		
		this.setFont(AppConstants.APP_FONT_FIELDS);
		((JTextField)this.getEditor().getEditorComponent())
        .setDisabledTextColor(AppConstants.APP_COLOR_FONT_DISABLED);
		
		((JTextField)this.getEditor().getEditorComponent()).addFocusListener(this);
		((JTextField)this.getEditor().getEditorComponent()).setBorder(new LineBorder(AppConstants.APP_COLOR_FIELD_BORDER,1));
		this.setBorder(new LineBorder(AppConstants.APP_COLOR_FIELD_BORDER,0));
		//if(autoComplete)
		//	initAutoComplete();
	}
	public void setEditor(ComboBoxEditor cbeditor)
	{
		
		//((JTextField)this.getEditor().getEditorComponent()).removeFocusListener(this);
		super.setEditor(cbeditor);
		((JTextField)this.getEditor().getEditorComponent()).addFocusListener(this);
	}
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		if(enabled)
		{
			this.getEditor().getEditorComponent().setBackground(Color.white);
		}
		else
		{
			this.getEditor().getEditorComponent().setBackground(bg);
			
		}
	}
	public  void focusGained(FocusEvent arg0) {
		((JTextField)this.getEditor().getEditorComponent()).selectAll();
		if(initImply)
			setOrgText();
	}
	public void focusLost(FocusEvent arg0) {
		if(initImply)
		{
			elpanel.updateImpliesElements();
		initImply=false;
		}
	}
	public void setInitImply(boolean initImply)
	{
		this.initImply=initImply;
	}
	private String orgText="";
	private ElementPanel elpanel;
	public void setElementPanel(ElementPanel elpanel)
	{
		this.elpanel=elpanel;
	}
	public void setOrgText()
	{
		orgText=this.getSelectedItem().toString().trim();
	}
	/*public void addItem(Object anObject)
	{
		System.out.println(" +++ "+anObject);
		super.addItem(anObject);
	}*/
	
	/// AUTO COMPLETE PART
	public void initAutoComplete()
	{
		lap = new java.util.Date().getTime();
		setKeySelectionManager(this);
		JTextField tf;
		if(getEditor() != null)
		{
			tf = (JTextField)getEditor().getEditorComponent();
			if(tf != null)
			{
				tf.setDocument(new CBDocument());
				addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						JTextField tf = (JTextField)getEditor().getEditorComponent();
						String text = tf.getText();
						ComboBoxModel aModel = getModel();
						String current;
						for(int i = 0; i < aModel.getSize(); i++)
						{
							current = aModel.getElementAt(i).toString();
							if(current.toLowerCase().startsWith(text.toLowerCase()))
							{
								tf.setText(current);
								tf.setSelectionStart(text.length());
								tf.setSelectionEnd(current.length());
								break;
							}
						}
					}
				});
			}
		}
	}
	public int selectionForKey(char aKey, ComboBoxModel aModel)
	{
		long now = new java.util.Date().getTime();
		if (searchFor!=null && aKey==KeyEvent.VK_BACK_SPACE &&	searchFor.length()>0)
		{
			searchFor = searchFor.substring(0, searchFor.length() -1);
		}
		else
		{
			//	System.out.println(lap);
			// Kam nie hier vorbei.
			if(lap + 1000 < now)
				searchFor = "" + aKey;
			else
				searchFor = searchFor + aKey;
		}
		lap = now;
		String current;
		for(int i = 0; i < aModel.getSize(); i++)
		{
			current = aModel.getElementAt(i).toString().toLowerCase();
			if (current.toLowerCase().startsWith(searchFor.toLowerCase())) return i;
		}
		return -1;
	}
	public void fireActionEvent()
	{
		super.fireActionEvent();
	}
}