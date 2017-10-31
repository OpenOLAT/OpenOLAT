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

import java.util.Comparator;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public interface INode {
	
	/**
	 * @return node identifier
	 */
	public String getIdent();

	/**
	 * @return parent of this node
	 */
	public INode getParent();

	/**
	 * @param newParent
	 */
	public void setParent(INode newParent);

	/**
	 * 
	 */
	public void removeFromParent();

	/**
	 * 
	 */
	public void removeAllChildren();

	/**
	 * @return number of children attached to this node
	 */
	public int getChildCount();

	/**
	 * @param childIndex
	 * @return node at index childIndex
	 */
	public INode getChildAt(int childIndex);

	/**
	 * @return position of this node
	 */
	public int getPosition();

	/**
	 * Adds a child to the current node. IMPORTANT: Sets the parent of the child
	 * to this node. I.e. detach from old parent.
	 * 
	 * @param newChild
	 */
	public void addChild(INode newChild);

	/**
	 * @param newChild
	 * @param index
	 */
	public void insert(INode newChild, int index);

	/**
	 * @param node
	 */
	public void remove(INode node);
	
	/**
	 * Sort the children of this node
	 * 
	 * @param comparator
	 */
	public void sort(Comparator<INode> comparator);

}