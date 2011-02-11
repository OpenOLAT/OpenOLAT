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

package org.olat.core.util.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class NodeHelper {

	/**
	 * @param node
	 * @return children as list
	 */
	public static List getChildrenAsList(INode node) {
		List<INode> li = new ArrayList<INode>(5);
		int cnt = node.getChildCount();
		for (int i = 0; i < cnt; i++) {
			li.add(node.getChildAt(i));
		}
		return li;
	}

	/**
	 * Search for a node with the given nodeIdent in all children and grand
	 * children of the given parentNode. The search is done in a depth-first
	 * manner
	 * 
	 * @param nodeIdent
	 * @param parentNode
	 * @return INode or NULL if not found
	 */
	public static INode findNodeInTree(String nodeIdent, INode parentNode) {
		if (parentNode.getIdent().equals(nodeIdent)) {
			return parentNode;
		}
		int cnt = parentNode.getChildCount();
		for (int i = 0; i < cnt; i++) {
			INode child = parentNode.getChildAt(i);
			// call recursion in this child
			INode foundNode = findNodeInTree(nodeIdent, child);
			if (foundNode != null) {
				return foundNode;
			}
		}
		return null;
	}

}