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

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import de.fhg.fokus.se.ethnoarc.common.DBConstants;
import de.fhg.fokus.se.ethnoarc.common.DBHandling;
import de.fhg.fokus.se.ethnoarc.common.DBSqlHandler;
import de.fhg.fokus.se.ethnoarc.common.DBStructure;
import de.fhg.fokus.se.ethnoarc.common.SearchManager;
import de.fhg.fokus.se.ethnoarc.common.SearchObject;
import de.fhg.fokus.se.ethnoarc.common.SearchResult;
import de.fhg.fokus.se.ethnoarc.queryServer.QueryServerInterface;

public class DatabaseSpider extends Thread {
	String DBref;

	String DBname;

	Hashtable<String, QueryElement> queryElements;

	Hashtable<String, String> resultFields;

	Hashtable<String, IntObj> resultFieldOrder;

	Vector<String> resultRow = new Vector<String>();

	Vector<String> orConnectedEntryFields = new Vector<String>();

	Vector<String> orConnectedDBFields = new Vector<String>();

	Boolean bMultipleDB;

	DefaultTableModel model;

	JLabel label;

	Iterator itb;

	int nChoice = 0;

	public DatabaseSpider(Hashtable<String, QueryElement> queryElements,
			Hashtable<String, String> resultFields,
			Hashtable<String, IntObj> resultFieldOrder, Boolean bMultipleDB,
			DefaultTableModel model, JLabel label, Iterator itb) {
		this.queryElements = queryElements;
		this.resultFields = resultFields;
		this.bMultipleDB = bMultipleDB;
		this.model = model;
		this.label = label;
		this.resultFieldOrder = resultFieldOrder;
		this.itb = itb;
		nChoice = 0;
	}

	public void run() {
		ControlFrame.searchButton.setEnabled(false);
		label.setText("<Search active> Current Results : " + 0);

		while (itb.hasNext()) {
			this.DBref = (String) itb.next();
			startSearch();
		}
		label.setText("Final Results : " + model.getRowCount());

	}

	public void startSearch() {
		String DBUSERNAME = "", DBPASSWORD = "", DBURL = "", DBnameSQL = "", fullName;
		boolean DBRMI = false;
		QueryElement queryElement;
		SearchObject searchObject = null;
		String strWhere = "";
		Vector<String> fields = new Vector<String>();
		Vector<ArrayList> targetColumns = new Vector<ArrayList>();
		List<String> inp = new ArrayList<String>();
		List<String> outp = new ArrayList<String>();
		Boolean bHasMultipleInterpretations = false;
		// System.out.println("Perform search on DB "+DBref);
		// get required info for database structure retrieval
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			if (DBref
					.equals(queryElement.getDBURL() + queryElement.getDBname())) {
				DBURL = queryElement.getDBURL();
				DBUSERNAME = queryElement.getDBuser();
				DBPASSWORD = queryElement.getDBpassword();
				DBRMI = queryElement.getDBrmi();
				DBname = queryElement.getDBname();
				DBnameSQL = queryElement.getDBnameSQL();
				fields.addElement(queryElement.getDBtablename());
			}
		}
		if (DBRMI) {
			try {
				// System.err.println("This is a RMI based search
				// ("+DBnameSQL+") on "+DBURL);
				QueryServerInterface QSI = (QueryServerInterface) Naming
						.lookup(DBURL);
				searchObject = QSI.getSearchObject(DBnameSQL, fields);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				// System.err.println("This is an SQL based search on "+DBURL);
				DBHandling dbHandle = null;
				DBStructure dbStructure = null;
				dbHandle = new DBHandling(DBURL, DBUSERNAME, DBPASSWORD);

				dbStructure = dbHandle.getDBStructure();
				DBSqlHandler sqlHandler = new DBSqlHandler(
						DBConstants.DBDRIVER, DBURL, DBUSERNAME, DBPASSWORD);
				searchObject = sqlHandler.getSearchObject(dbStructure, fields);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (searchObject.getMultiplePathExists()) {
			System.err
					.println("Multiple possible interpretations of search path - selected:");
			bHasMultipleInterpretations = true;
			Hashtable multi = searchObject.getMultiplePathTable();
			// itb = multi.keySet().iterator();
			// while( itb.hasNext() ) {
			// System.err.println(multi.get(itb.next()));
			// }

			itb = multi.keySet().iterator();
			fields = searchObject.getSearchFields();

			while (itb.hasNext()) {
				Vector<String> vAlternatives = (Vector<String>) multi.get(itb
						.next());
				if (nChoice < vAlternatives.size()) {
					fields.add(vAlternatives.elementAt(nChoice));
				} else
					return;
			}
		}
		// now we got the canonical names of the search fields, so build
		// input/output lists
		else
			fields = searchObject.getSearchFields();
		
		
	    // trying to remove common prefices to minimize search tree
		String strLastPrefix="";
		boolean bChangeMade=true;
		while((fields.size()>0)&&(bChangeMade)&&(fields.get(0).indexOf(".")>0)){
			int nPos=fields.get(0).indexOf(".");
			boolean bFoundDifferent=false;
			for (int i = 0; i < fields.size(); i++){
				int nPos2=fields.get(i).indexOf(".");
				if (nPos2!=nPos)bFoundDifferent=true;
				else 					if(!fields.get(0).substring(0,nPos).equals(fields.get(i).substring(0,nPos)))bFoundDifferent=true;		
			}
			bChangeMade=false;
			if(!bFoundDifferent){
				bChangeMade=true;
				strLastPrefix=fields.get(0).substring(0,nPos);
				for (int i = 0; i < fields.size(); i++){
					fields.set(i, fields.get(i).substring(nPos+1));				
				}
				for (int i = 0; i < fields.size(); i++)
					System.err.println(fields.get(i));
			}
			
		}
		// prefix fields with last common prefic
		for (int i = 0; i < fields.size(); i++)
			fields.set(i, strLastPrefix+"."+fields.get(i));	
			
	//	for (int i = 0; i < fields.size(); i++)
	//		System.err.println(fields.get(i));
		
		// get required info for database structure retrieval

		itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			queryElement = (QueryElement) queryElements.get(itb.next());
			if (DBref
					.equals(queryElement.getDBURL() + queryElement.getDBname())) {
				// get full name for this element
				fullName = "";
				for (int i = 0; i < fields.size(); i++) {
					if (fields.get(i).contains(".")) {
						if (fields.get(i).endsWith(
								"." + queryElement.getDBtablename())) {
							fullName = fields.get(i);
						}
					} else {
						if (fields.get(i).endsWith(
								queryElement.getDBtablename())) {
							fullName = fields.get(i);
						}
					}
				}
				if (fullName.equals("")) {
					System.err.println("Couldn't determine internal name for "
							+ queryElement.getDBtablename()
							+ ". Search aborted.");
					return;
				}
				// check whether any of the entry fields points to this element
				// Note: This creates a query string as a side effect.
				if (entryPointsTo(queryElement.getName())) {
					inp.add(fullName);
					// no longer needed since query string is now built
					// elsewhere
					// if(strWhere.length()==0)strWhere=new String(strQuery);
					// else strWhere=new String(strWhere+" AND "+strQuery);
				}
				// check whether this has any connection to result field
				if (pointsToResult(queryElement)) {
					outp.add(fullName);
				}
			}
		}

		if (inp.size() == 0) {
			JOptionPane.showMessageDialog(ControlFrame.QBM.getQueryCanvas(),
					"No input values for search in DB: " + DBname,
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		if (outp.size() == 0) {
			JOptionPane.showMessageDialog(ControlFrame.QBM.getQueryCanvas(),
					"No result field connected for search in DB: " + DBname,
					"Search failed", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		strWhere = makeQueryWherePart();

		// for(int i=0;i<inp.size();i++)
		// System.err.println("inp["+i+"]="+inp.get(i));
		// for(int i=0;i<outp.size();i++)
		// System.err.println("outp["+i+"]="+outp.get(i));
		// System.err.println("where part is "+strWhere);

		SearchResult rs = null;
		if (DBRMI) {
			try {
				// System.err.println("This is a RMI based result search
				// ("+DBnameSQL+") on "+DBURL);
				QueryServerInterface QSI = (QueryServerInterface) Naming
						.lookup(DBURL);
				rs = QSI.getSearchResult(DBnameSQL, inp, outp, strWhere);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				SearchManager sm = new SearchManager(DBURL, DBUSERNAME,
						DBPASSWORD);
				rs = sm.search(inp, outp, strWhere);
			} catch (Exception e) {
				// System.err.println("Search exception:
				// "+e.getLocalizedMessage());
				// e.printStackTrace();
			}
		}

		if (rs != null) {
			// figure out what result column we need to map to what column
			// in the display table

			for (int i = 0; i < rs.getColumnCount(); i++) {
				// find the proper element for this column
				itb = queryElements.keySet().iterator();
				while (itb.hasNext()) {
					queryElement = (QueryElement) queryElements.get(itb.next());
					if (queryElement.getType() == QueryElement.TYPE_ELEMENT)
						if (DBref.equals(queryElement.getDBURL()
								+ queryElement.getDBname())) {

							// if(rs.getColumnName(i).endsWith(queryElement.getDBtablename())){
							if (rs.getColumnName(i).equals(
									queryElement.getDBtablename())) {
								ArrayList<IntObj> targetCols = new ArrayList<IntObj>();
								// got proper element for this result column,
								// now find the
								// result query elements it points to...
								Iterator outit = queryElement.getConnections()
										.keySet().iterator();
								while (outit.hasNext()) {
									String OutputName = (String) outit.next();
									QueryElement connectionTarget = queryElements
											.get(OutputName);
									if (connectionTarget.getType() == QueryElement.TYPE_RESULT) {
										// find column number for this in
										// results table
										Iterator itr = resultFields.keySet()
												.iterator();
										int colPos;
										while (itr.hasNext()) {
											String key = (String) itr.next();
											if (key.equals(connectionTarget
													.getName())) {
												colPos = resultFieldOrder
														.get(connectionTarget
																.getName()).i;
												if (bMultipleDB)
													colPos++;
												targetCols.add(new IntObj(
														colPos));
											}
										}
									}
								}
								targetColumns.add(targetCols);
							}
						}
				}
			}
			// now we have the mapping from 'rs' columns to display 'columns'
			/*
			 * for(int i=0;i<targetColumns.size();i++){
			 * System.err.print("Column "+i+"("+rs.getColumnName(i)+") points to
			 * "); ArrayList pointers=(ArrayList)targetColumns.get(i); for(int
			 * j=0;j<pointers.size();j++)
			 * System.err.print(pointers.get(j).toString()+" ");
			 * System.err.println(); }
			 */
			// Now determine the order im which we want to
			// read the columns. This is almost completely pointless,
			// but it allows the user to determine the order of insertion
			// if two data columns point to the same result column,
			// i.e. whether last name and first name are shown as
			// "Smith, John" or "John, Smith".
			int[] colMapping = new int[rs.getColumnCount()];
			int leftmost;
			int orderPos = 0;
			int prevleftmost = -10000;
			boolean valueAdded = true;
			while (valueAdded) {
				// find leftmost result field
				valueAdded = false;
				leftmost = 100000;
				// determine new leftmost position
				itb = queryElements.keySet().iterator();
				while (itb.hasNext()) {
					queryElement = (QueryElement) queryElements.get(itb.next());
					if (queryElement.getType() == QueryElement.TYPE_ELEMENT)
						if (DBref.equals(queryElement.getDBURL()
								+ queryElement.getDBname()))
							if ((queryElement.getLeft() < leftmost)
									&& (queryElement.getLeft() > prevleftmost))
								leftmost = queryElement.getLeft();
				}
				prevleftmost = leftmost;
				// find element(s) with this position
				itb = queryElements.keySet().iterator();
				while (itb.hasNext()) {
					String key = (String) itb.next();
					queryElement = (QueryElement) queryElements.get(key);
					if (queryElement.getType() == QueryElement.TYPE_ELEMENT)
						if (DBref.equals(queryElement.getDBURL()
								+ queryElement.getDBname()))
							if (queryElement.getLeft() == prevleftmost) {
								// find column that matches this element
								// add this to mapping list
								int rsCol = -1;
								for (int i = 0; i < rs.getColumnCount(); i++) {
									if (rs.getColumnName(i).equals(
											queryElement.getDBtablename()))
										rsCol = i;
								}
								if ((rsCol != -1)
										&& (orderPos < rs.getColumnCount()))
									colMapping[orderPos++] = rsCol;
								valueAdded = true;
							}
				}

			}

			// let's fill the result table with the results
			for (int nRow = 0; nRow < rs.getRowCount(); nRow++) {
				// create empty result row
				resultRow = new Vector<String>();
				for (int i = 0; i < resultFields.size(); i++)
					resultRow.add(i, "");
				if (bMultipleDB)
					resultRow.add("");
				if (bMultipleDB)
					resultRow.set(0, DBname);
				// and add results
				for (int nColP = 0; nColP < rs.getColumnCount(); nColP++) {
					int nCol = colMapping[nColP];
					ArrayList targets = (ArrayList) targetColumns.get(nCol);
					for (int nTargets = 0; nTargets < targets.size(); nTargets++) {
						int targetCol = ((IntObj) targets.get(nTargets)).i;
						String strVal = (String) rs.getValueAt(nRow, nCol);
						if (strVal != null) {
							if (((String) resultRow.get(targetCol)).length() == 0)
								resultRow.set(targetCol, new String(strVal));
							else
								resultRow.set(targetCol, new String(
										((String) resultRow.get(targetCol))
												+ ", " + strVal));
						}

					}
				}
				model.addRow(resultRow);
			}
			if ((rs.getRowCount() == 0) && (bHasMultipleInterpretations)) {
				nChoice++;
				startSearch();
			} // try with next alternative...
		}
		label.setText("<Search active> Current Results : "
				+ model.getRowCount());
	}

	private Boolean entryPointsTo(String elementName) {
		Boolean bResult = false;
		Iterator itb = queryElements.keySet().iterator();
		while (itb.hasNext()) {
			QueryElement queryElement = (QueryElement) queryElements.get(itb
					.next());
			if (queryElement.getType() == QueryElement.TYPE_ENTRY) {
				Iterator outit = queryElement.getConnections().keySet()
						.iterator();
				while (outit.hasNext()) {
					String OutputName = (String) outit.next();
					if (OutputName.equals(elementName)) {
						if (queryElement.getEntryfieldValue().length() > 0) {
							// no longer needed since query string is built
							// elsewhere now
							// QueryElement searchElement=
							// (QueryElement)queryElements.get(elementName);
							// if(bResult) // handle multiple entries to same
							// element
							// strQuery=strQuery + " AND " +
							// searchElement.getDBtablename() +".content like
							// '"+
							// MakeSQLsafe(queryElement.getEntryfieldValue())+"'";
							// else
							// strQuery=searchElement.getDBtablename()
							// +".content like '"+
							// MakeSQLsafe(queryElement.getEntryfieldValue())+"'";
							bResult = true;
						}
					}
				}
			}
		}
		return bResult;
	}

	private static String MakeSQLsafe(String anyString) {
		return ((anyString.replaceAll("'", "''")).trim());
	}

	private Boolean pointsToResult(QueryElement queryElement) {
		Boolean bResult = false;
		Iterator outit = queryElement.getConnections().keySet().iterator();
		while (outit.hasNext()) {
			String OutputName = (String) outit.next();
			QueryElement connectionTarget = queryElements.get(OutputName);
			if (connectionTarget.getType() == QueryElement.TYPE_RESULT)
				bResult = true;
		}
		return bResult;
	}

	private String makeQueryWherePart() {
		/*
		 * This used to be part of the inp/outp list building, but with the
		 * addition of the 'or' element, the logic gets a bit more complicated,
		 * so this got its own procedure.
		 */
		String strWhere = "";
		Hashtable<String, String> queryParts = new Hashtable<String, String>();
		Vector<String> coveredFields;
		Vector<String> coveredDBElements;
		// first we need to generate the query strings for all individual DB
		// elements
		// (we'll combine them later for the final query)
		// find element(s) with this position
		Iterator itb = queryElements.keySet().iterator();
		QueryElement queryElement;
		QueryElement entryElement;
		while (itb.hasNext()) {
			String key = (String) itb.next();
			queryElement = (QueryElement) queryElements.get(key);
			if (queryElement.getType() == QueryElement.TYPE_ELEMENT)
				if (DBref.equals(queryElement.getDBURL()
						+ queryElement.getDBname())) {
					coveredFields = new Vector<String>();
					// do any entry fields point to this element?
					Iterator ite = queryElements.keySet().iterator();
					while (ite.hasNext()) {
						String keye = (String) ite.next();
						entryElement = (QueryElement) queryElements.get(keye);
						if (entryElement.getType() == QueryElement.TYPE_ENTRY)
							if (!coveredFields.contains(entryElement.getName())) { // only
																					// if
																					// we
																					// haven't
																					// used
																					// this
																					// entry
																					// field
																					// before
								// check whether this field has a connection to
								// the queryelement
								Iterator out = entryElement.getConnections()
										.keySet().iterator();
								while (out.hasNext()) {
									String OutputName = (String) out.next();
									if (OutputName.equals(queryElement
											.getName())) {
										// if this one points to any 'OR'
										// elements, list all the other entry
										// fields that are connected to the same
										// 'OR' or connected via other 'OR'
										orConnectedEntryFields = new Vector<String>();
										BuildOrEntryList(entryElement);
										// add all those entry elements to
										// 'covered fields'.
										for (int i = 0; i < orConnectedEntryFields
												.size(); i++) {
											if (!coveredFields
													.contains(orConnectedEntryFields
															.elementAt(i)))
												coveredFields
														.add(orConnectedEntryFields
																.elementAt(i));
										}
										// now go through all the entries and
										// build queries for the non-empty ones,
										// connecting them with "OR" and
										// building part of the search string
										String orQuery = "";
										for (int i = 0; i < orConnectedEntryFields
												.size(); i++) {
											QueryElement entryFieldElement = queryElements
													.get(orConnectedEntryFields
															.elementAt(i));
											if (entryFieldElement
													.getEntryfieldValue()
													.length() > 0) {
												if (orQuery.length() == 0)
													orQuery = queryElement
															.getDBtablename()
															+ ".content like '"
															+ MakeSQLsafe(entryFieldElement
																	.getEntryfieldValue())
															+ "'";
												else
													orQuery = orQuery
															+ " OR "
															+ queryElement
																	.getDBtablename()
															+ ".content like '"
															+ MakeSQLsafe(entryFieldElement
																	.getEntryfieldValue())
															+ "'";
											}
										}
										if (orQuery.length() > 0) { // add query
																	// to query
																	// string
																	// for this
																	// db
																	// element
											if (strWhere.length() == 0)
												strWhere = "(" + orQuery + ")";
											else
												strWhere = strWhere + " AND ("
														+ orQuery + ")";
										}
									}
								}

							}
					}
					// // we now got the query for this single element - store
					// it.
					if (strWhere.length() > 0)
						queryParts.put(queryElement.getName(), new String(
								strWhere));
					strWhere = "";
				}

		}
		// now find out which of the DB elements are connected with OR elements
		// and combine their queries accordingly
		QueryElement dbElement;
		strWhere = "";
		coveredDBElements = new Vector<String>();
		Iterator it_qp = queryParts.keySet().iterator();
		while (it_qp.hasNext()) {
			String key = (String) it_qp.next();
			if (!coveredDBElements.contains(key)) { // if not yet handled in
													// connection to other
													// element
				dbElement = (QueryElement) queryElements.get(key);
				orConnectedDBFields = new Vector<String>();
				BuildOrDBElementList(dbElement);
				// now go through all the entries and connect the queries with
				// "OR"
				String orQuery = "";
				for (int i = 0; i < orConnectedDBFields.size(); i++) {
					if (orQuery.length() == 0)
						orQuery = new String(queryParts.get(orConnectedDBFields
								.elementAt(i)));
					else
						orQuery = "("
								+ orQuery
								+ " OR ("
								+ new String(queryParts.get(orConnectedDBFields
										.elementAt(i))) + ")) ";
					// add to processed elements
					coveredDBElements.add(orConnectedDBFields.elementAt(i));
				}
				// now add the query for this (and possible connected DB
				// elements) to the overall query
				if (orQuery.length() > 0) { // add query to query string for
											// this db element
					if (strWhere.length() == 0)
						strWhere = orQuery;
					else
						strWhere = strWhere + " AND (" + orQuery + ")";
				}
			}

		}
		// System.err.println("Query is "+strWhere);
		if (QueryBuildManager.wildcardStyle == QueryBuildManager.WINDOWS_STYLE) {
			// replace wildcards with windows wild cards
			strWhere=strWhere.replace('*', '%');
			strWhere=strWhere.replace('?', '_');
		}
		
		
		// if we got an '|' character within the search string,
		// split the corresponding part of the query into two 'OR' parts
		while (strWhere.contains("|")) {
			int pipePos = strWhere.indexOf("|");
			int partStart = 0;
			int quoteStart = -1;
			int quoteEnd = -1;
			for (int i = 0; i < strWhere.length(); i++)
				if (strWhere.charAt(i) == '\'') {
					if (i < pipePos)
						quoteStart = i;
					if ((i > pipePos) && (quoteEnd == -1))
						quoteEnd = i;
				}
			if (quoteStart > -1)
				for (int i = 1; i < quoteStart; i++)
					if (strWhere.charAt(i - 1) == '(')
						partStart = i;
			if ((quoteStart == -1) || (quoteEnd == -1)) {
				strWhere=strWhere.replaceFirst("\\|", " ");
			} else if ((quoteStart == pipePos-1) || (quoteEnd == pipePos+1)) {
				strWhere=strWhere.replaceFirst("\\|", " ");
			} else {
				// perform the actual replacement
				// first, find both partial strings
				String strLeft = strWhere.substring(quoteStart+1, pipePos);
				String strRight = strWhere.substring(pipePos+1, quoteEnd);
				String strFullExpression = strWhere.substring(partStart, quoteEnd+1);
				String strLeftExpression = strWhere.substring(partStart, quoteStart);
				// build replacement expression
				String strNewExpression ="("+strLeftExpression+" '"+strLeft+"') OR ("+strLeftExpression+" '"+strRight+"')";
                // need to escape "|" character to avoid problem in regex							
				strFullExpression=strFullExpression.replaceFirst("\\|","\\\\|");
				
				strWhere=strWhere.replaceFirst(strFullExpression, strNewExpression);
			}

		}
		// if we got an '&' character within the search string,
		// split the corresponding part of the query into two 'OR' parts
		while (strWhere.contains("&")) {
			int pipePos = strWhere.indexOf("&");
			int partStart = 0;
			int quoteStart = -1;
			int quoteEnd = -1;
			for (int i = 0; i < strWhere.length(); i++)
				if (strWhere.charAt(i) == '\'') {
					if (i < pipePos)
						quoteStart = i;
					if ((i > pipePos) && (quoteEnd == -1))
						quoteEnd = i;
				}
			if (quoteStart > -1)
				for (int i = 1; i < quoteStart; i++)
					if (strWhere.charAt(i - 1) == '(')
						partStart = i;
			if ((quoteStart == -1) || (quoteEnd == -1)) {
				strWhere=strWhere.replaceFirst("\\&", " ");
			} else if ((quoteStart == pipePos-1) || (quoteEnd == pipePos+1)) {
				strWhere=strWhere.replaceFirst("\\&", " ");
			} else {
				// perform the actual replacement
				// first, find both partial strings
				String strLeft = strWhere.substring(quoteStart+1, pipePos);
				String strRight = strWhere.substring(pipePos+1, quoteEnd);
				String strFullExpression = strWhere.substring(partStart, quoteEnd+1);
				String strLeftExpression = strWhere.substring(partStart, quoteStart);
				// build replacement expression
				String strNewExpression ="("+strLeftExpression+" '"+strLeft+"') AND ("+strLeftExpression+" '"+strRight+"')";
                // need to escape "|" character to avoid problem in regex							
				strFullExpression=strFullExpression.replaceFirst("\\&","\\\\&");
				
				strWhere=strWhere.replaceFirst(strFullExpression, strNewExpression);
			}

		}

		//System.err.println("Query is " + strWhere);
		
		return strWhere;
	}

	private void BuildOrEntryList(QueryElement inputElement) {
		Boolean changeHappened = true;
		QueryElement entryElement;
		Vector<String> orElements = new Vector<String>();
		orConnectedEntryFields.add(inputElement.getName());
		while (changeHappened) { // repeat until we got all connected fields
			// find all new 'OR' elements referenced by current entry element
			changeHappened = false;
			for (int i = 0; i < orConnectedEntryFields.size(); i++) {
				entryElement = queryElements.get(orConnectedEntryFields.get(i));
				Iterator out = entryElement.getConnections().keySet()
						.iterator();
				while (out.hasNext()) {
					String OutputName = (String) out.next();
					QueryElement targetElement = queryElements.get(OutputName);
					if (targetElement.getType() == QueryElement.TYPE_OR)
						if (!orElements.contains(targetElement.getName())) {
							orElements.add(targetElement.getName());
							changeHappened = true;
						}
				}
			}
			// now find entry elements that point to the available 'OR' elements
			// do any entry fields point to this element?
			Iterator it = queryElements.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				entryElement = (QueryElement) queryElements.get(key);
				if (entryElement.getType() == QueryElement.TYPE_ENTRY) {
					Iterator out = entryElement.getConnections().keySet()
							.iterator();
					while (out.hasNext()) {
						String OutputName = (String) out.next();
						if (orElements.contains(OutputName)) {
							// points to one of the 'or' in the list - if we
							// haven't used this entry, do it now
							if (!orConnectedEntryFields.contains(entryElement
									.getName())) {
								orConnectedEntryFields.add(entryElement
										.getName());
								changeHappened = true;
							}
						}
					}
				}
			}
		}

	}

	private void BuildOrDBElementList(QueryElement inputElement) {
		Boolean changeHappened = true;
		QueryElement entryElement;
		Vector<String> orElements = new Vector<String>();
		orConnectedDBFields.add(inputElement.getName());

		while (changeHappened) { // repeat until we got all connected fields
			// find all new 'OR' elements referenced by current DB element
			changeHappened = false;
			for (int i = 0; i < orConnectedDBFields.size(); i++) {
				entryElement = queryElements.get(orConnectedDBFields.get(i));
				Iterator out = entryElement.getConnections().keySet()
						.iterator();
				while (out.hasNext()) {
					String OutputName = (String) out.next();
					QueryElement targetElement = queryElements.get(OutputName);
					if (targetElement.getType() == QueryElement.TYPE_OR)
						if (!orElements.contains(targetElement.getName())) {
							orElements.add(targetElement.getName());
							changeHappened = true;
						}
				}
			}
			// now find entry elements that point to the available 'OR' elements
			// do any entry fields point to this element?
			Iterator it = queryElements.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				entryElement = (QueryElement) queryElements.get(key);
				if (entryElement.getType() == QueryElement.TYPE_ELEMENT) {
					Iterator out = entryElement.getConnections().keySet()
							.iterator();
					while (out.hasNext()) {
						String OutputName = (String) out.next();
						if (orElements.contains(OutputName)) {
							// points to one of the 'or' in the list - if we haven't used this entry, do it now
							if (!orConnectedDBFields.contains(entryElement
									.getName())) {
								orConnectedDBFields.add(entryElement.getName());
								changeHappened = true;
							}
						}
					}
				}
			}
		}

	}

}