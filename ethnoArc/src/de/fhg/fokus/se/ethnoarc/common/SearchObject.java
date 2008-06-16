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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

public class SearchObject implements Serializable {

	private static final long serialVersionUID = -5021039504273705680L;
	private Vector <String> searchFields = new Vector<String>();
	private Hashtable<String,Vector<String>> multiplePathTable = new Hashtable<String,Vector <String>>();
	private boolean muliplePathExists = false;
	
	public void addSearchField(String dotNotation){
		searchFields.add(dotNotation);
	}

	public Vector <String> getSearchFields(){
		return searchFields;
	}
	
	public void setMultiplePath(boolean newSet){
		muliplePathExists = newSet;
	}
	
	public boolean getMultiplePathExists(){
		return muliplePathExists;
	}
	
	public void addMultiplePathElements(String elementName, Vector <String> mulPath){
		multiplePathTable.put(elementName, mulPath);
	}
	
	public Hashtable<String,Vector<String>> getMultiplePathTable(){
		return multiplePathTable;
	}
	
	public Vector<String> getMultiplePathByElementname(String elementName){
		return multiplePathTable.get(elementName);
	}
	
}
