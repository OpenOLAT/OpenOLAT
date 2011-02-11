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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ParentChildMapper {

	private Map vLToV = new HashMap();

	/**
	 * 
	 */
	public ParentChildMapper() {
		super();
	}

	/**
	 * @param parent
	 * @param child
	 */
	public void addRelationship(Object parent, Object child) {
		Set children = (Set) vLToV.get(parent);
		if (children == null) {
			children = new HashSet();
			vLToV.put(parent, children);
		}
		children.add(child);
	}

	/**
	 * @return set of parents
	 */
	public Set getParents() {
		return vLToV.keySet();
	}

	/**
	 * @param parent
	 * @return set of children
	 */
	public Set getChildren(Object parent) {
		Set children = (Set) vLToV.get(parent);
		return children;
	}

}