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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JColorChooser;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.util.Iterator;
import java.util.Set;

public class ArchiveInformationDialog implements ActionListener, ListSelectionListener {
	

	JTextField nameField,computerField, portField, userField, databaseField;
    JLabel nameLabel,computerLabel, portLabel, databaseLabel, userLabel, passwordLabel, colorLabel;
	JButton colorButton;
    JPasswordField passwordField;
    JFrame frame;
    JList list;
    JCheckBox useRMIquery ;
    JCheckBox useLocalLanguage ;
    
    public  void start() {
        // define access parameters for an archive
    	frame = new JFrame("Archive access information");
        frame.setSize(600,250);
        
        JPanel infoPanel = new JPanel();
        JButton buttonCancel, buttonStore, buttonDelete;
        int dist=20;
        SpringLayout layout = new SpringLayout();
        Dimension dimension = new Dimension(700,20);
        DefaultListModel listModel = new DefaultListModel();

      
        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //list.addListSelectionListener(this);
        //list.setVisibleRowCount(99);
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setMaximumSize(new Dimension(1000,1000));
        listScrollPane.setMinimumSize(new Dimension(100,100));
        listScrollPane.setPreferredSize(new Dimension(150,210));
        infoPanel.add(listScrollPane);        
        layout.putConstraint(SpringLayout.NORTH, listScrollPane, dist,SpringLayout.NORTH, infoPanel);
        layout.putConstraint(SpringLayout.WEST, listScrollPane, dist,SpringLayout.WEST, infoPanel);
//        layout.putConstraint(SpringLayout.SOUTH, listScrollPane, dist,SpringLayout.SOUTH, infoPanel);
        
        nameField=new JTextField();
        nameField.setColumns(20);
        nameField.setMaximumSize(dimension);
        nameLabel=new JLabel("Name:");
        infoPanel.add(nameField);
        infoPanel.add(nameLabel);
        layout.putConstraint(SpringLayout.NORTH, nameLabel, dist,SpringLayout.NORTH, infoPanel);
        layout.putConstraint(SpringLayout.NORTH, nameField, 0,SpringLayout.NORTH, nameLabel);
        layout.putConstraint(SpringLayout.WEST, nameLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, nameField, dist,SpringLayout.EAST, nameLabel);
        layout.putConstraint(SpringLayout.EAST, nameField, -dist,SpringLayout.EAST, infoPanel);
        computerField=new JTextField();               
        computerField.setColumns(20);
        computerField.setMaximumSize(dimension);
        computerLabel=new JLabel("Computer:");
        infoPanel.add(computerField);
        infoPanel.add(computerLabel);
        layout.putConstraint(SpringLayout.NORTH, computerLabel, dist,SpringLayout.SOUTH, nameLabel);
        layout.putConstraint(SpringLayout.NORTH, computerField, 0,SpringLayout.NORTH, computerLabel);
        layout.putConstraint(SpringLayout.WEST, computerLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, computerField, dist,SpringLayout.EAST, nameLabel);
        layout.putConstraint(SpringLayout.EAST, computerField, -dist,SpringLayout.EAST, infoPanel);
        portField=new JTextField("3306");
        portField.setColumns(20);
        portField.setMaximumSize(dimension);
        portLabel=new JLabel("Port:");
        infoPanel.add(portField);
        infoPanel.add(portLabel);
        layout.putConstraint(SpringLayout.NORTH, portLabel, dist,SpringLayout.SOUTH, computerLabel);
        layout.putConstraint(SpringLayout.NORTH, portField, 0,SpringLayout.NORTH, portLabel);
        layout.putConstraint(SpringLayout.WEST, portLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, portField, dist,SpringLayout.EAST, portLabel);
        layout.putConstraint(SpringLayout.EAST, portField, -dist,SpringLayout.EAST, infoPanel);
        databaseField=new JTextField("ethnoArc");
        databaseField.setColumns(20);
        databaseField.setMaximumSize(dimension);
        databaseLabel=new JLabel("Database:");
        infoPanel.add(databaseField);
        infoPanel.add(databaseLabel);
        layout.putConstraint(SpringLayout.NORTH, databaseLabel, dist,SpringLayout.SOUTH, portLabel);
        layout.putConstraint(SpringLayout.NORTH, databaseField, 0,SpringLayout.NORTH, databaseLabel);
        layout.putConstraint(SpringLayout.WEST,databaseLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, databaseField, dist,SpringLayout.EAST, databaseLabel);
        layout.putConstraint(SpringLayout.EAST, databaseField, -dist,SpringLayout.EAST, infoPanel);
        
        useRMIquery = new JCheckBox("Use RMI query");
        infoPanel.add(useRMIquery);
        useRMIquery.setMaximumSize(dimension);        
        layout.putConstraint(SpringLayout.NORTH, useRMIquery, dist,SpringLayout.SOUTH, databaseLabel);
        layout.putConstraint(SpringLayout.WEST, useRMIquery, dist,SpringLayout.EAST, listScrollPane);

        useRMIquery.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		passwordField.setEnabled(!useRMIquery.isSelected());
            		userField.setEnabled(!useRMIquery.isSelected());
            		userLabel.setEnabled(!useRMIquery.isSelected());
            		passwordLabel.setEnabled(!useRMIquery.isSelected());
            }
        });    
        
        userField=new JTextField();
        userField.setColumns(20);
        userField.setMaximumSize(dimension);
        userLabel=new JLabel("User:");
        infoPanel.add(userField);
        infoPanel.add(userLabel);
        layout.putConstraint(SpringLayout.NORTH, userLabel, dist,SpringLayout.SOUTH, useRMIquery);
        layout.putConstraint(SpringLayout.NORTH, userField, 0,SpringLayout.NORTH, userLabel);
        layout.putConstraint(SpringLayout.WEST, userLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, userField, dist,SpringLayout.EAST, userLabel);
        layout.putConstraint(SpringLayout.EAST, userField, -dist,SpringLayout.EAST, infoPanel);
        passwordField=new JPasswordField();
        passwordField.setColumns(20);
        passwordField.setMaximumSize(dimension);
        passwordLabel=new JLabel("Password:");
        infoPanel.add(passwordField);
        infoPanel.add(passwordLabel);
        layout.putConstraint(SpringLayout.NORTH, passwordLabel, dist,SpringLayout.SOUTH, userLabel);
        layout.putConstraint(SpringLayout.NORTH, passwordField, 0,SpringLayout.NORTH, passwordLabel);
        layout.putConstraint(SpringLayout.WEST, passwordLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, passwordField, dist,SpringLayout.EAST, passwordLabel);
        layout.putConstraint(SpringLayout.EAST, passwordField, -dist,SpringLayout.EAST, infoPanel);

        colorButton=new JButton();
       // colorButton.setColumns(20);
        colorButton.setMaximumSize(new Dimension(20,20));
        colorButton.setPreferredSize(new Dimension(20,20));
        colorButton.setBackground(new Color(0,0,0));
        colorButton.setForeground(new Color(0,0,0));
        colorButton.addActionListener(this);
        colorButton.setActionCommand("Color");
        colorLabel=new JLabel("Color:");
        infoPanel.add(colorButton);
        infoPanel.add(colorLabel);
        layout.putConstraint(SpringLayout.NORTH, colorLabel, dist,SpringLayout.SOUTH, passwordLabel);
        layout.putConstraint(SpringLayout.NORTH, colorButton, 0,SpringLayout.NORTH, colorLabel);
        layout.putConstraint(SpringLayout.WEST, colorLabel, dist,SpringLayout.EAST, listScrollPane);
        layout.putConstraint(SpringLayout.WEST, colorButton, dist,SpringLayout.WEST, passwordField);
       // layout.putConstraint(SpringLayout.EAST, colorButton, -dist,SpringLayout.EAST, infoPanel);
        
        useLocalLanguage = new JCheckBox("Use local archive language for descriptions");
        infoPanel.add(useLocalLanguage);
        useLocalLanguage.setMaximumSize(dimension);        
        layout.putConstraint(SpringLayout.NORTH, useLocalLanguage, dist,SpringLayout.SOUTH, colorLabel);
        layout.putConstraint(SpringLayout.WEST, useLocalLanguage, dist,SpringLayout.EAST, listScrollPane);

        useRMIquery.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		passwordField.setEnabled(!useRMIquery.isSelected());
            		userField.setEnabled(!useRMIquery.isSelected());
            		userLabel.setEnabled(!useRMIquery.isSelected());
            		passwordLabel.setEnabled(!useRMIquery.isSelected());
            }
        });    
        
        
        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(this);
        infoPanel.add(buttonCancel);
        layout.putConstraint(SpringLayout.NORTH, buttonCancel, dist,SpringLayout.SOUTH, useLocalLanguage);
        layout.putConstraint(SpringLayout.WEST, buttonCancel, dist,SpringLayout.EAST, listScrollPane);
        buttonDelete = new JButton("Delete");
        buttonDelete.addActionListener(this);
        infoPanel.add(buttonDelete);
        layout.putConstraint(SpringLayout.NORTH, buttonDelete, dist,SpringLayout.SOUTH, useLocalLanguage);
        layout.putConstraint(SpringLayout.WEST, buttonDelete, dist,SpringLayout.EAST, buttonCancel);
        buttonStore = new JButton("Store");
        buttonStore.addActionListener(this);
        infoPanel.add(buttonStore);
        layout.putConstraint(SpringLayout.NORTH, buttonStore, dist,SpringLayout.SOUTH, useLocalLanguage);
        layout.putConstraint(SpringLayout.WEST, buttonStore, dist*3,SpringLayout.EAST, buttonDelete);
        //layout.putConstraint(SpringLayout.EAST, buttonStore, -dist,SpringLayout.EAST, infoPanel);

        
        layout.putConstraint(SpringLayout.EAST, infoPanel,dist,SpringLayout.EAST, buttonStore);
        layout.putConstraint(SpringLayout.SOUTH, infoPanel,dist,SpringLayout.SOUTH, buttonCancel);

        infoPanel.setLayout(layout);
        frame.getContentPane().add(infoPanel, BorderLayout.NORTH);
             
        // add the list of existing entries, if any              
	    Set keys = ControlFrame.archives.keySet();	    
	    Iterator iter = keys.iterator();
	    int nFirst=1;
	    while (iter.hasNext())
	    {
	    	Object key = iter.next();
	    	listModel.addElement((String)key);
	    	if(nFirst==1){
	    		nFirst=0;
	    		ArchiveInfo archiveInfo=(ArchiveInfo)ControlFrame.archives.get((String)key);
	    		nameField.setText(new String(archiveInfo.name));
	    		computerField.setText(new String(archiveInfo.computer));
	    		portField.setText(new String(archiveInfo.port));
	    		databaseField.setText(new String(archiveInfo.database));
	    		userField.setText(new String(archiveInfo.user));
	    		passwordField.setText(new String(archiveInfo.password));
	    		useRMIquery.setSelected(archiveInfo.useRMIquery);
	    		useLocalLanguage.setSelected(archiveInfo.useLocalLanguage);
	            colorButton.setBackground(archiveInfo.color);
	            colorButton.setForeground(archiveInfo.color);
        		passwordField.setEnabled(!archiveInfo.useRMIquery);
        		userField.setEnabled(!archiveInfo.useRMIquery);
        		userLabel.setEnabled(!archiveInfo.useRMIquery);
        		passwordLabel.setEnabled(!archiveInfo.useRMIquery);        		
	    	}
	    }
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);

        //Display the window.
        frame.pack();
        frame.setVisible(true);

        }
        
        public void actionPerformed(ActionEvent e) {
        	if(e.getActionCommand().equals("Store")){
         	   //System.out.println(nameField.getText());
         	   if(nameField.getText().length()!=0){
// true needs to be replaced by value of rmi tag         		   
         		ControlFrame.addArchiveInfo(nameField.getText(),computerField.getText() , portField.getText(), databaseField.getText(), userField.getText(), String.valueOf(passwordField.getPassword()), useRMIquery.isSelected(),useLocalLanguage.isSelected(),colorButton.getForeground());         		
         	   }        	
         	   frame.dispose();
         	}
        	if(e.getActionCommand().equals("Cancel")){
           	   frame.dispose();
           	}
        	if(e.getActionCommand().equals("Color")){                
                Color newColor = JColorChooser.showDialog(
                		null,
                        "Choose archive color",
                        null);
                colorButton.setForeground(newColor);
                colorButton.setBackground(newColor);
        	}
          	
        	if(e.getActionCommand().equals("Delete")){
        		if(nameField.getText().length()!=0)
         		  ControlFrame.deleteArchiveInfo(nameField.getText());        		
          	   frame.dispose();
          	}
        }
        
        //This method is required by ListSelectionListener.
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                if (list.getSelectedIndex() != -1) {
    	    		ArchiveInfo archiveInfo=(ArchiveInfo)ControlFrame.archives.get(list.getSelectedValue());
    	    		if(archiveInfo!=null){
    	    		 nameField.setText(new String(archiveInfo.name));
    	    		 computerField.setText(new String(archiveInfo.computer));
    	    		 portField.setText(new String(archiveInfo.port));
    	    		 databaseField.setText(new String(archiveInfo.database));
    	    		 userField.setText(new String(archiveInfo.user));
    	    		 passwordField.setText(new String(archiveInfo.password));
    	    		 useRMIquery.setSelected(archiveInfo.useRMIquery);
    	    		 useLocalLanguage.setSelected(archiveInfo.useLocalLanguage);    
    		         colorButton.setBackground(archiveInfo.color);
    		         colorButton.setForeground(archiveInfo.color);
    	        		passwordField.setEnabled(!archiveInfo.useRMIquery);
    	        		userField.setEnabled(!archiveInfo.useRMIquery);
    	        		userLabel.setEnabled(!archiveInfo.useRMIquery);
    	        		passwordLabel.setEnabled(!archiveInfo.useRMIquery);   		         
    	    		}
                }
            }
        }

}




