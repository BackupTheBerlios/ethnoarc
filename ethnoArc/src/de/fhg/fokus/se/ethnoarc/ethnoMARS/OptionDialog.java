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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;


import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;
import javax.swing.text.MaskFormatter;

public class OptionDialog {
	JFrame frame;  
	JRadioButton[] radioButtons ;
	JRadioButton[] wildcardButtons ;
	JRadioButton[] csvButtons ;
	JRadioButton[] closeButtons ;
	JFormattedTextField 		tooltipEntry; 
	public  void start() {


		radioButtons = new JRadioButton[3];
		ButtonGroup group = new ButtonGroup();
		radioButtons[0] = new JRadioButton("replace earlier results");
		radioButtons[1] = new JRadioButton("open a new window");
		radioButtons[2] = new JRadioButton("appear as tabs");
		for (int i = 0; i < 3; i++) {
			group.add(radioButtons[i]);
		}

		if(QueryBuildManager.resultDisplay==QueryBuildManager.REPLACE_WINDOW)radioButtons[0].setSelected(true);
		if(QueryBuildManager.resultDisplay==QueryBuildManager.MULTIPLE_WINDOWS)radioButtons[1].setSelected(true);
		if(QueryBuildManager.resultDisplay==QueryBuildManager.TABBED_WINDOW)radioButtons[2].setSelected(true);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(radioButtons[0].isSelected())QueryBuildManager.resultDisplay=QueryBuildManager.REPLACE_WINDOW;
				if(radioButtons[1].isSelected())QueryBuildManager.resultDisplay=QueryBuildManager.MULTIPLE_WINDOWS;
				if(radioButtons[2].isSelected())QueryBuildManager.resultDisplay=QueryBuildManager.TABBED_WINDOW;
				ControlFrame.appPropertyManager.writePropertyFile();
				frame.dispose();     
			}
		});    

		JPanel resultsPanel =createPane("Additional search results should:",
				radioButtons,
				okButton);


		wildcardButtons = new JRadioButton[2];
		ButtonGroup wildcardGroup = new ButtonGroup();
		wildcardButtons[0] = new JRadioButton("SQL style (% and _)");
		wildcardButtons[1] = new JRadioButton("Windows style (* and ?)");
		for (int i = 0; i < 2; i++) {
			wildcardGroup.add(wildcardButtons[i]);
		}

		if(QueryBuildManager.wildcardStyle==QueryBuildManager.SQL_STYLE)wildcardButtons[0].setSelected(true);
		if(QueryBuildManager.wildcardStyle==QueryBuildManager.WINDOWS_STYLE)wildcardButtons[1].setSelected(true);

		JButton okWildcardButton = new JButton("OK");
		okWildcardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(wildcardButtons[0].isSelected())QueryBuildManager.wildcardStyle=QueryBuildManager.SQL_STYLE;
				if(wildcardButtons[1].isSelected())QueryBuildManager.wildcardStyle=QueryBuildManager.WINDOWS_STYLE;
				ControlFrame.appPropertyManager.writePropertyFile();
				frame.dispose();     
			}
		});    


		JPanel wildcardPanel =createPane("Wildcards are written in:",
				wildcardButtons,
				okWildcardButton);


		csvButtons = new JRadioButton[3];
		ButtonGroup csvGroup = new ButtonGroup();
		csvButtons[0] = new JRadioButton("Comma ");
		csvButtons[1] = new JRadioButton("Semicolon ");
		csvButtons[2] = new JRadioButton("Tab ");
		for (int i = 0; i < 3; i++) {
			csvGroup.add(csvButtons[i]);
		}

		if(QueryBuildManager.CSVseparator==',')csvButtons[0].setSelected(true);
		if(QueryBuildManager.CSVseparator==';')csvButtons[1].setSelected(true);
		if(QueryBuildManager.CSVseparator=='\t')csvButtons[2].setSelected(true);

		JButton okCSVButton = new JButton("OK");
		okCSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(csvButtons[0].isSelected())QueryBuildManager.CSVseparator=',';
				if(csvButtons[1].isSelected())QueryBuildManager.CSVseparator=';';
				if(csvButtons[1].isSelected())QueryBuildManager.CSVseparator='\t';
				ControlFrame.appPropertyManager.writePropertyFile();
				frame.dispose();     
			}
		});    

		JPanel csvPanel =createPane("CSV separator is:",
				csvButtons,
				okCSVButton);


		// tooltipEntry.setBounds(0,0,10,10);
		//tooltipEntry.setVisible(true);

		try{
			MaskFormatter numFormatter = new MaskFormatter();
			numFormatter.setValidCharacters(" 1234567890");
			numFormatter.setMask("******");
			numFormatter.setAllowsInvalid(false);		
			tooltipEntry = new JFormattedTextField(numFormatter);
		}catch (Exception e){System.err.println(e.getMessage());}
		tooltipEntry.setValue(""+QueryBuildManager.tooltipDelay);
		tooltipEntry.setColumns(6);
		tooltipEntry.setPreferredSize(new Dimension(100,20));
		tooltipEntry.setMaximumSize(new Dimension(100,20));

		JButton okTooltipButton = new JButton("OK");
		okTooltipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String strVal=(String)tooltipEntry.getText();
				int val=0;
				for(int i=0;i<strVal.length();i++)
					if(strVal.charAt(i)!=' ')val=val*10+(strVal.charAt(i)-'0');
				if(val<10)val=10;
				QueryBuildManager.tooltipDelay=val;
				ControlFrame.appPropertyManager.writePropertyFile();
				frame.dispose();     
			}
		});    

		JPanel tooltipPanel =createPane("Tooltip visibility in milliseconds:",
				tooltipEntry,
				okTooltipButton);

		

		closeButtons = new JRadioButton[2];
		ButtonGroup closeGroup = new ButtonGroup();
		closeButtons[0] = new JRadioButton("Confirm quit ");
		closeButtons[1] = new JRadioButton("Quit without confirmation ");

		for (int i = 0; i < 2; i++) {
			closeGroup.add(closeButtons[i]);
		}

		if(ControlFrame.warnBeforeExit)closeButtons[0].setSelected(true);
		else closeButtons[1].setSelected(true);

		JButton okCloseButton = new JButton("OK");
		okCloseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(closeButtons[0].isSelected())ControlFrame.warnBeforeExit=true;
				if(closeButtons[1].isSelected())ControlFrame.warnBeforeExit=false;
				ControlFrame.appPropertyManager.writePropertyFile();
				frame.dispose();     
			}
		});    

		JPanel closePanel =createPane("On closing the main window:",
				closeButtons,
				okCloseButton);
		
		//Create and set up the window.
		frame = new JFrame("Options");
		frame.setMinimumSize(new Dimension(350,190));
		frame.setPreferredSize(new Dimension(350,190));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Results", null,
				resultsPanel,
		"Result display options");
		tabbedPane.addTab("Wildcards", null,
				wildcardPanel,
		"Set wildcard style");
		tabbedPane.addTab("CSV", null,
				csvPanel,
		"Set CSV separator");        
		tabbedPane.addTab("Tooltip", null,
				tooltipPanel,
		"Set tooltip duration");        
		tabbedPane.addTab("Quitting", null,
				closePanel,
		"Set closing confirmation");        

		tabbedPane.setOpaque(true);
		frame.setContentPane(tabbedPane);
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private JPanel createPane(String description,
			JRadioButton[] radioButtons,
			JButton showButton) {

		int numChoices = radioButtons.length;
		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);
		for (int i = 0; i < numChoices; i++) {
			box.add(radioButtons[i]);
		}
		JPanel pane = new JPanel(new BorderLayout());
		pane.add(box, BorderLayout.PAGE_START);
		pane.add(showButton, BorderLayout.PAGE_END);
		return pane;
	}    
	private JPanel createPane(String description,
			JFormattedTextField entryField,
			JButton showButton) {

		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);
		box.add(entryField);

		JPanel pane = new JPanel(new BorderLayout());
		pane.add(box, BorderLayout.PAGE_START);
		pane.add(showButton, BorderLayout.PAGE_END);
		return pane;
	}    
}
