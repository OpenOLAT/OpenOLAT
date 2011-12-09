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

package org.olat.core.util.traversal;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * 
 * @author Felix Jost
 */

public class GenericTraversalNode {
	private Object item;
	private int depth;
	private List children;
	private int visitNumber;

	/**
	 * @param item
	 */
	public GenericTraversalNode(Object item) {
		children = new ArrayList();
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
	public List getChildren() {
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
	 * Sets the children.
	 * 
	 * @param children The children to set
	 */
	public void setChildren(List children) {
		this.children = children;
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
	 * Sets the item.
	 * 
	 * @param item The item to set
	 */
	public void setItem(Object item) {
		this.item = item;
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