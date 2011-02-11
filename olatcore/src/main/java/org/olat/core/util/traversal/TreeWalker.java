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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.traversal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 
 * @author Felix Jost
 */
public class TreeWalker {
	private Map nodemap = new HashMap();
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
		List children = node.getChildren();
		treecomp.sort(depth + 1, children);
		Iterator it = children.iterator();
		while (it.hasNext()) {
			GenericTraversalNode c = (GenericTraversalNode) it.next();
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
		GenericTraversalNode n = (GenericTraversalNode) nodemap.get(item);
		if (n == null) { // not existing, so create
			n = new GenericTraversalNode(item);
			nodemap.put(item, n);
		}
		return n;
	}

}