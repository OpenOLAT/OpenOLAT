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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.Formatter;

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

	private List<Message> messages;
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
	private static class MessageTreeComparator implements TreeComparator {
		private class MessageComparator implements Comparator<GenericTraversalNode> {

			/**
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(GenericTraversalNode ga, GenericTraversalNode gb) {
				boolean asc = true; //ascending;
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

		@Override
		public void sort(int depth, List<GenericTraversalNode> children) {
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
			Iterator<Message> mit = messages.iterator();
			while (mit.hasNext()) {
				Message m = mit.next();
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
	
	private static class TreeWalker {
		private Map<Object,GenericTraversalNode> nodemap = new HashMap<Object,GenericTraversalNode>();
		private TreeComparator treecomp;
		private Visitor v;
		private GenericTraversalNode rootNode;
		private int visitNumber = 0;

		/**
		 * @param treecomp
		 * @param v the visitor, may be null
		 */
		public TreeWalker(TreeComparator treecomp, Visitor v) {
			this.treecomp = treecomp;
			this.v = v;
			rootNode = new GenericTraversalNode(null);
		}

		/**
		 * 
		 */
		public void traverse() {
			doTraverse(rootNode, 0);
		}

		private void doTraverse(GenericTraversalNode node, int depth) {
			if (depth > 0) { // we are not at the artificial root
				node.setDepth(depth);
				node.setVisitNumber(++visitNumber);
				// preorder traversal
				if (v != null) v.visit(node);
			}
			List<GenericTraversalNode> children = node.getChildren();
			treecomp.sort(depth + 1, children);
			for(Iterator<GenericTraversalNode> it = children.iterator(); it.hasNext(); ) {
				GenericTraversalNode c = it.next();
				doTraverse(c, depth + 1);
			}
		}

		/**
		 * @param childitem the child
		 * @param parentitem the parent, may be null if top level
		 */
		public void addRelationship(Object childitem, Object parentitem) {
			GenericTraversalNode gnp;
			GenericTraversalNode gnc = getGenericTraversalNode(childitem);
			if (parentitem != null) {
				gnp = getGenericTraversalNode(parentitem);
			} else {
				gnp = rootNode;
			}
			gnp.addChild(gnc);

		}

		/**
		 * @param item
		 * @return GenericTraversalNode
		 */
		public GenericTraversalNode getGenericTraversalNode(Object item) {
			GenericTraversalNode n = nodemap.get(item);
			if (n == null) { // not existing, so create
				n = new GenericTraversalNode(item);
				nodemap.put(item, n);
			}
			return n;
		}

	}
	
	private static class GenericTraversalNode {
		private Object item;
		private int depth;
		private List<GenericTraversalNode> children;
		private int visitNumber;

		/**
		 * @param item
		 */
		public GenericTraversalNode(Object item) {
			children = new ArrayList<GenericTraversalNode>();
			this.item = item;
		}

		/**
		 * add the child.
		 * 
		 * @param n
		 */
		public void addChild(GenericTraversalNode n) {
			if (!children.add(n)) { throw new RuntimeException("duplicate child in List"); }
		}

		/**
		 * Returns the children.
		 * 
		 * @return List
		 */
		public List<GenericTraversalNode> getChildren() {
			return children;
		}

		/**
		 * Returns the depth.
		 * 
		 * @return int
		 */
		public int getDepth() {
			return depth;
		}

		/**
		 * Returns the item.
		 * 
		 * @return Object
		 */
		public Object getItem() {
			return item;
		}

		/**
		 * Sets the depth.
		 * 
		 * @param depth The depth to set
		 */
		public void setDepth(int depth) {
			this.depth = depth;
		}

		/**
		 * Returns the visitNumber.
		 * 
		 * @return int
		 */
		public int getVisitNumber() {
			return visitNumber;
		}

		/**
		 * Sets the visitNumber.
		 * 
		 * @param visitNumber The visitNumber to set
		 */
		public void setVisitNumber(int visitNumber) {
			this.visitNumber = visitNumber;
		}

	}
	
	public interface Visitor {

		public void visit(GenericTraversalNode node);
	}
	
	public interface TreeComparator {
		/**
		 * sorts the children of a node with depth 'depth' in a tree. depth = 1 = root children
		 * @param depth
		 * @param children
		 */
		public void sort(int depth, List<GenericTraversalNode> children);
	}
}

