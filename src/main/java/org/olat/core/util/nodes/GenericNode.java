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
* <p>
*/ 

package org.olat.core.util.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class GenericNode implements INode, Serializable {

	private static final int INITIAL_CHILD_SIZE = 3;

	private String ident; // generated automatically
	private INode parent = null;
	private List<INode> children = null; // lazy init

	/**
	 * 
	 */
	protected GenericNode() {
		//FIXME:fj: add a flag in param whether forever unique id or just session unique
		this.ident = String.valueOf(CodeHelper.getForeverUniqueID());
	}
	
	protected GenericNode(String ident) {
		this.ident = ident;
	}

	/**
	 * @see org.olat.core.util.nodes.INode#getIdent()
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * @param ident
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	/**
	 * @see org.olat.core.util.nodes.INode#getParent()
	 */
	public INode getParent() {
		return parent;
	}

	/**
	 * @see org.olat.core.util.nodes.INode#setParent(org.olat.core.util.nodes.INode)
	 */
	public void setParent(INode newParent) {
		this.parent = newParent;
	}

	/**
	 * @see org.olat.core.util.nodes.INode#getChildCount()
	 */
	public int getChildCount() {
		if (children == null) return 0;
		return children.size();
	}

	/**
	 * @see org.olat.core.util.nodes.INode#getChildAt(int)
	 */
	public INode getChildAt(int childIndex) {
		if (children == null) children = new ArrayList<INode>(INITIAL_CHILD_SIZE);
		return children.get(childIndex);
	}

	/**
	 * @see org.olat.core.util.nodes.INode#getPosition()
	 */
	public int getPosition() {
		if (parent == null) return -1;
		for (int i = 0; i < parent.getChildCount(); i++) {
			INode child = parent.getChildAt(i);
			if (child.getIdent().equals(getIdent())) return i;
		}
		throw new AssertException("Corrupt tree structure.");
	}

	/**
	 * @see org.olat.core.util.nodes.INode#removeFromParent()
	 */
	public void removeFromParent() {
		INode myParent = getParent();
		if (myParent != null) {
			myParent.remove(this);
		}
	}

	@Override
	public void insert(INode newChild, int index) {
		if (isNodeAncestor(newChild)) throw new IllegalArgumentException("new child is an ancestor");
		newChild.removeFromParent();
		newChild.setParent(this);
		if (children == null) {
			children = new ArrayList<INode>(INITIAL_CHILD_SIZE);
		}
		if(index > children.size()) {
			children.add(newChild);
		} else {
			children.add(index, newChild);
		}
	}

	/**
	 * @see org.olat.core.util.nodes.INode#removeAllChildren()
	 */
	public void removeAllChildren() {
		if (children != null) children.clear();
	}

	/**
	 * @see org.olat.core.util.nodes.INode#remove(org.olat.core.util.nodes.INode)
	 */
	public void remove(INode node) {
		if (children == null) return;
		children.remove(node);
	}

	private boolean isNodeAncestor(INode node) {
		INode ancestor = this;
		do {
			if (ancestor == node) return true;
		} while ((ancestor = ancestor.getParent()) != null);
		return false;
	}

	/**
	 * @see org.olat.core.util.nodes.INode#addChild(org.olat.core.util.nodes.INode)
	 */
	@Override
	public void addChild(INode newChild) {
		if (children == null) children = new ArrayList<INode>(INITIAL_CHILD_SIZE);
		insert(newChild, children.size());
	}
	
	public void sort(Comparator<INode> comparator) {
		if(children != null && children.size() > 1) {
			Collections.sort(children, comparator);
		}
	}

	/**
	 * @return top-level root of node tree
	 */
	public INode findRoot() {
		INode cur = this;
		INode latest;
		do {
			latest = cur;
			cur = cur.getParent();
		} while (cur != null);
		return latest;
	}

}