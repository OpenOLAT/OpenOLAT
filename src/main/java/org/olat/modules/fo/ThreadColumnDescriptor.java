/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.fo;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.Formatter;
import org.olat.core.util.traversal.GenericTraversalNode;
import org.olat.core.util.traversal.TreeComparator;
import org.olat.core.util.traversal.TreeWalker;

/**
 * Specialized ColumnDescriptor to display title of Forum messages in a indented
 * (thread-like) way
 * 
 * @author Felix Jost
 */
public class ThreadColumnDescriptor extends DefaultColumnDescriptor {
	private static final String ONEINDENT = "&nbsp;&nbsp;";

	private static final int MAXINDENTS = 20;
	private static final String[] INDENTS;

	private List messages;
	private TreeWalker tw;
	private boolean toIndent;

	static {
		INDENTS = new String[MAXINDENTS];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < MAXINDENTS; i++) {
			INDENTS[i] = sb.toString();
			sb.append(ONEINDENT);
		}
	}

	/**
	 * Description:<BR>
	 * Private class that implements a tree comparator for forum messages 
	 * <P>
	 * Initial Date:  Jan 19, 2005
	 *
	 * @author gnaegi
	 */
	private class MessageTreeComparator implements TreeComparator {
		private class MessageComparator implements Comparator {

			/**
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object a, Object b) {
				boolean asc = true; //ascending;
				GenericTraversalNode ga = (GenericTraversalNode) a;
				GenericTraversalNode gb = (GenericTraversalNode) b;
				Message ma = (Message) ga.getItem();
				Message mb = (Message) gb.getItem();
				Date da = ma.getCreationDate();
				Date db = mb.getCreationDate();
				// TODO:fj: avoid allocation of lots of new Date objects
				// timestamp and date did not compare? recheck.
				// See also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5103041 for the java 1.4/1.5 code bug
				if (da instanceof Timestamp) {
					da = new Date(da.getTime());
				}
				
				if (db instanceof Timestamp) {
					db = new Date(db.getTime());
				}
				
				
				return (asc ? da.compareTo(db) : db.compareTo(da));
			}

		}

		private MessageComparator msgcomp;

		
		private MessageTreeComparator() {
			msgcomp = new MessageComparator();
		}

		/**
		 * @see org.olat.core.util.traversal.TreeComparator#sort(int, java.util.List)
		 */
		public void sort(int depth, List children) {
			/*
			 * if (depth > 1) { msgcomp.setAscending(true); } else {
			 * msgcomp.setAscending(false); }
			 */
			Collections.sort(children, msgcomp);
		}
	}

	/**
	 * @param headerKey
	 * @param dataColumn
	 * @param action
	 */
	public ThreadColumnDescriptor(String headerKey, int dataColumn, String action) {
		super(headerKey, dataColumn, action, null);
	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput, int, org.olat.core.gui.render.Renderer)
	 */
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		StringOutput sbuf = new StringOutput();
		super.renderValue(sbuf, row, renderer);
		String val = sbuf.toString();
		Object m = messages.get(getTable().getSortedRow(row));
		GenericTraversalNode gtn = getTreeWalker().getGenericTraversalNode(m);
		// + "..."+gtn.getDepth()+", vnr:"+gtn.getVisitNumber();
		sb.append("<div style=\"white-space: nowrap;"); // do not wrap titles, looks unsexy
		int indent = gtn.getDepth(); // starts with 1
		if (indent > MAXINDENTS) indent = MAXINDENTS;
		if (toIndent) {
			sb.append("padding-left: ");
			sb.append(Formatter.roundToString(((float)indent-1)/2, 2));
			sb.append("em;");
		}
		sb.append("\">");
		sb.append(Formatter.truncate(val, 50-indent));
		sb.append("</div>");
	}

	/**
	 * 
	 */
	private TreeWalker getTreeWalker() {
		if (tw == null) {
			tw = new TreeWalker(new MessageTreeComparator(), null); // no visitor,
																															// since we just
																															// need the
																															// traversal order
																															// after sorting
			Iterator mit = messages.iterator();
			while (mit.hasNext()) {
				Message m = (Message) mit.next();
				tw.addRelationship(m, m.getParent());
			}
			tw.traverse(); // now the visitednr of each node indicates the sorting
										 // position
		}
		return tw;
	}

	/**
	 * this special columndescriptor can only handle data from the column of the
	 * tablemodel if it is of type Message, since this descriptor is especially
	 * made for the forum
	 */
	public int compareTo(int rowa, int rowb) {
		// we take the treewalker which we built previously and use the visitednr
		// attribute to specify sort order
		Object a = messages.get(rowa);
		Object b = messages.get(rowb); // both are messages; to be compared
		TreeWalker treeWalker = getTreeWalker();
		GenericTraversalNode gtna = treeWalker.getGenericTraversalNode(a);
		GenericTraversalNode gtnb = treeWalker.getGenericTraversalNode(b);
		int sortedposa = gtna.getVisitNumber(); // the visitednumber was determined
																						// by the sorting algorithm,
																						// therefore it is the sorted
																						// position
		int sortedposb = gtnb.getVisitNumber();
		int diff = sortedposa - sortedposb;
		int res = (diff == 0 ? 0 : (diff > 0 ? 1 : -1));
		return res;
	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#modelChanged()
	 */
	public void modelChanged() {
		ForumMessagesTableDataModel ftdm = (ForumMessagesTableDataModel) getTable().getTableDataModel();
		// this specialized columndescriptor only works for the forumtabledatamodel
		messages = ftdm.getObjects();
		tw = null; // we need to build a new TreeWalker (which does the sorting,
							 // since the model has changed
	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#sortingAboutToStart()
	 */
	public void sortingAboutToStart() {
		toIndent = true; // only indent messages if we are sorting this column

	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#otherColumnDescriptorSorted()
	 */
	public void otherColumnDescriptorSorted() {
		toIndent = false;
	}


	public String toString(int rowid) {
		String retVal = super.toString(rowid);
		Message m = (Message)messages.get(getTable().getSortedRow(rowid));
		return retVal+m.getTitle()+m.getCreator().getName();
	}
}

