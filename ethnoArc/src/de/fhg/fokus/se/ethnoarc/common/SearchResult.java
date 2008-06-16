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
package de.fhg.fokus.se.ethnoarc.common;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SearchResult extends AbstractTableModel{
	private Object[] columnNames;
	private String[][] data;

	public SearchResult(Object[] columnNames)
	{
		this.columnNames=columnNames;
	}
	public void setData(String[][] data)
	{
		this.data=data;
	}
	public void setData(List<String[]> datalist)
	{
		if(datalist.size()>0)
		{
			int csize=datalist.get(0).length;
			String[][] d = new String[datalist.size()][csize];
			for (int i = 0; i < d.length; i++) {
				d[i]=new String[csize];
				for (int j = 0; j < csize; j++) {
					d[i][j]=new String();
					d[i][j]=datalist.get(i)[j];
				}
			}
			data=d;
		}
		else
			data=new String[0][columnNames.length];
	}
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public void printDebugData() {
		int numRows = getRowCount();
		int numCols = getColumnCount();

		for (int i=0; i < numRows; i++) {
			System.out.print("    row " + (i+1) + ":");
			for (int j=0; j < numCols; j++) {
				System.out.print("  " + data[i][j]);
			}
			System.out.println();
		}
		System.out.println("--------------------------");
	}
}
