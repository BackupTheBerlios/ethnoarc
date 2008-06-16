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

import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WritableFont;
import jxl.write.WritableCellFormat;
import jxl.Workbook;
import jxl.write.Label;

public class SaveSearchResults {
	static String saveNameExcel="Results.xls";
	static String saveNameCSV="Results.csv";

	public  void exportExcel(DefaultTableModel   model, JFrame frame){
		Label label;
		File file = new File( saveNameExcel);
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile( file );
		fc.addChoosableFileFilter(new XLSFilter());
		if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
		file = fc.getSelectedFile();
		saveNameExcel=new String(file.getName());
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			WritableSheet resultSheet = workbook.createSheet("ethnoArc Search Result", 0);
			// write headers
			WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, true);
			WritableCellFormat headerFormat = new WritableCellFormat (headerFont);			
			for(int nCol=0;nCol<model.getColumnCount();nCol++){
				label = new Label(nCol, 0, model.getColumnName(nCol),headerFormat); 
				resultSheet.addCell(label);
			}
			// write data
			for(int nRow=0;nRow<model.getRowCount();nRow++){
				for(int nCol=0;nCol<model.getColumnCount();nCol++){
					label = new Label(nCol, nRow+1, model.getValueAt(nRow,nCol).toString()); 
					resultSheet.addCell(label);
				}
			}
			workbook.write();
			workbook.close();
		} catch (Exception e) {System.err.println("Error saving file :"+e.getMessage());return;}

	}

	public  void tabbedExportExcel(JTabbedPane tabbedPane, JFrame frame){
		Label label;
		File file = new File( saveNameExcel);
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile( file );
		fc.addChoosableFileFilter(new XLSFilter());
		if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
		file = fc.getSelectedFile();
		saveNameExcel=new String(file.getName());
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			for(int tab=0; tab<tabbedPane.getComponentCount(); tab++){
				tabbedPane.getTitleAt(tab);
				JPanel panel=(JPanel)tabbedPane.getComponent(tab);
				JScrollPane sp = (JScrollPane) panel.getComponent(1);
				JViewport jv = (JViewport) sp.getComponent(0);
				JTable jt=(JTable) jv.getComponent(0);
				DefaultTableModel model=(DefaultTableModel)jt.getModel();		

				WritableSheet resultSheet = workbook.createSheet(tabbedPane.getTitleAt(tab),tab);
				// write headers
				WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD, true);
				WritableCellFormat headerFormat = new WritableCellFormat (headerFont);			
				for(int nCol=0;nCol<model.getColumnCount();nCol++){
					label = new Label(nCol, 0, model.getColumnName(nCol),headerFormat); 
					resultSheet.addCell(label);
				}
				// write data
				for(int nRow=0;nRow<model.getRowCount();nRow++){
					for(int nCol=0;nCol<model.getColumnCount();nCol++){
						label = new Label(nCol, nRow+1, model.getValueAt(nRow,nCol).toString()); 
						resultSheet.addCell(label);
					}
				}
			}
			workbook.write();
			workbook.close();
		} catch (Exception e) {System.err.println("Error saving file :"+e.getMessage());return;}

	}


	private class XLSFilter extends FileFilter {

		//Accept all directories and XLS files.
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("xls"))
					return true;
				else {
					return false;
				}
			}
			return false;

		}

		//The description of this filter
		public String getDescription() {
			return "XLS (Excel) files";
		}
		public  String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}
	}
	private class CSVFilter extends FileFilter {

		//Accept all directories and CSV files.
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("csv"))
					return true;
				else {
					return false;
				}
			}
			return false;

		}

		//The description of this filter
		public String getDescription() {
			return "CSV  files";
		}
		public  String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}
	}
	public  void exportCSV(DefaultTableModel   model, JFrame frame, char separator){

		try
		{
			File file = new File( saveNameCSV);
			JFileChooser fc = new JFileChooser();
			fc.setSelectedFile( file );
			fc.addChoosableFileFilter(new CSVFilter());
			if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
			file = fc.getSelectedFile();
			saveNameCSV=new String(file.getName());

			FileWriter fwCSVwriter = new FileWriter(file);
			// write header
			for(int nCol=0;nCol<model.getColumnCount();nCol++){
				String outstring=new String(model.getColumnName(nCol));
				fwCSVwriter.append("\"" +outstring.replaceAll("\"","\"\"") + "\""); 
				if(nCol < model.getColumnCount() - 1)
					fwCSVwriter.append(separator);
			}
			fwCSVwriter.append('\n');

			// write data
			for(int nRow=0;nRow<model.getRowCount();nRow++){
				for(int nCol=0;nCol<model.getColumnCount();nCol++){
					String outstring=new String(model.getValueAt(nRow,nCol).toString());
					fwCSVwriter.append("\"" +outstring.replaceAll("\"","\"\"") + "\""); 
					if(nCol < model.getColumnCount() - 1)
						fwCSVwriter.append(separator);
				}
				fwCSVwriter.append('\n');
			}
			fwCSVwriter.flush();
			fwCSVwriter.close();
		} catch (Exception e) {System.err.println("Error saving file :"+e.getMessage());return;}

	}
}
