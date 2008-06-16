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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.fhg.fokus.se.ethnoarc.dbmanager.helper.TextPaneAppender;
/**
 * $Id: LogPanel.java,v 1.2 2008/06/16 14:21:58 fchristian Exp $ 
 * The panel to display the log output.
 * @author fokus
 */
public class LogPanel extends JPanel implements ActionListener{
	static Logger logger = Logger.getLogger(LogPanel.class.getName());
	private JPanel controlPanel;
	private JPanel mainPanel;
	private JTextPane logOutputPane;
	private JButton butClear;
	
	/**
	 * @author  rva
	 */
	private enum COMMANDS{
		Clear,
		Debug,
		Info,
		Warn,
		Error,
		Off
	}
	private Level iniLogLevel;
	public LogPanel()
	{
		iniLogLevel= Logger.getRootLogger().getLevel();
		initGui();
	}
	
	private void setLogPane()
	{
		//PatternLayout myConsoleLayout = ;
		TextPaneAppender tpa = new TextPaneAppender(new PatternLayout("%p_%d{HH:mm:ss} - %m%n"), "aAppender");
		tpa.setTextPane(logOutputPane);
		//tpa.setThreshold(Logger.getRootLogger().getLevel());
		Logger.getRootLogger().addAppender(tpa);
	}
	public void actionPerformed(ActionEvent e) {
		switch (COMMANDS.valueOf(e.getActionCommand())) {
		case Clear:
			setText("");
			logger.info("All log messages in the log panel cleared");
			break;
		case Debug:
			Logger.getRootLogger().setLevel(Level.DEBUG);
			logger.info("Log level changed to DEBUG.");
			break;
		case Info:
			Logger.getRootLogger().setLevel(Level.INFO);
			logger.info("Log level changed to INFO.");
			break;
		case Warn:
			Logger.getRootLogger().setLevel(Level.WARN);
			logger.info("Log level changed to WARN.");
			break;
		case Error:
			Logger.getRootLogger().setLevel(Level.ERROR);
			logger.info("Log level changed to ERROR.");
			break;
		case Off:
			Logger.getRootLogger().setLevel(Level.OFF);
			logger.info("Logging turned OFF.");
			break;
		default:
			break;
		}
	}
	public void setText(String str)
	{
		logOutputPane.setText(str);
	}
	
	private void initGui()
	{
		this.setLayout(new BorderLayout());
		//top panel
		controlPanel = new JPanel();
		
		// add log level buttons
		JRadioButton butDebug=new JRadioButton("Debug");
		butDebug.setMnemonic(KeyEvent.VK_D);
		butDebug.setActionCommand(COMMANDS.Debug.toString());
		butDebug.addActionListener(this);
		if(iniLogLevel.equals(Level.DEBUG))
			butDebug.setSelected(true);
		controlPanel.add(butDebug);
		
		JRadioButton butInfo=new JRadioButton("Info");
		butInfo.setMnemonic(KeyEvent.VK_I);
		butInfo.setActionCommand(COMMANDS.Info.toString());
		butInfo.addActionListener(this);
		if(iniLogLevel.equals(Level.INFO))
			butInfo.setSelected(true);
		controlPanel.add(butInfo);
		
		JRadioButton butWarn=new JRadioButton("Warn");
		butWarn.setMnemonic(KeyEvent.VK_W);
		butWarn.setActionCommand(COMMANDS.Warn.toString());
		butWarn.addActionListener(this);
		if(iniLogLevel.equals(Level.WARN))
			butWarn.setSelected(true);
		controlPanel.add(butWarn);
		
		JRadioButton butError=new JRadioButton("Error");
		butError.setMnemonic(KeyEvent.VK_E);
		butError.setActionCommand(COMMANDS.Error.toString());
		butError.addActionListener(this);
		if(iniLogLevel.equals(Level.ERROR))
			butError.setSelected(true);
		controlPanel.add(butError);
		
		JRadioButton butNone=new JRadioButton("Off");
		butNone.setMnemonic(KeyEvent.VK_O);
		butNone.setActionCommand(COMMANDS.Off.toString());
		butNone.addActionListener(this);
		if(iniLogLevel.equals(Level.OFF))
			butNone.setSelected(true);
		controlPanel.add(butNone);
		
		ButtonGroup logLevelGroup = new ButtonGroup();
		logLevelGroup.add(butDebug);
		logLevelGroup.add(butInfo);
		logLevelGroup.add(butWarn);
		logLevelGroup.add(butError);
		logLevelGroup.add(butNone);
		
		//button Clear
		butClear = new JButton("Clear");
		butClear.setActionCommand(COMMANDS.Clear.toString());
		butClear.addActionListener(this);
		controlPanel.add(butClear);
		
		this.add(controlPanel,BorderLayout.NORTH);
		
		mainPanel = new JPanel(new BorderLayout());
		
		logOutputPane = new JTextPane();
		logOutputPane.setEditable(false);
		setLogPane();
		
		mainPanel.add(logOutputPane,BorderLayout.CENTER);
		JScrollPane scrollPanelContent = new JScrollPane(mainPanel);
		scrollPanelContent.setBorder(null);
		this.add(scrollPanelContent, BorderLayout.CENTER);
	}
}
