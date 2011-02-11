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

package org.olat.core.util.component;

import java.util.Iterator;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Container;

/**
 * Description: <br>
 * 
 * 
 * @author Felix Jost
 */
public class ComponentTraverser {

	private ComponentVisitor v;
	private Component root;
	private boolean visitChildrenFirst;

	/**
	 * @param v
	 * @param root
	 * @param visitChildrenFirst
	 */
	public ComponentTraverser(ComponentVisitor v, Component root, boolean visitChildrenFirst) {
		this.v = v;
		this.root = root;
		this.visitChildrenFirst = visitChildrenFirst;
	}

	/**
   * do visit with specified root component at ComponentTraverser creation time
	 * @param ureq
	 */
	public void visitAll(UserRequest ureq) {
		doVisit(root, ureq);
	}

	private void doVisit(Component node, UserRequest ureq) {
		if (!visitChildrenFirst) {
			if (!v.visit(node, ureq)) return;
		}
		if (node instanceof Container) { // visit children
			Container co = (Container) node;
			Map children = co.getComponents();
			for (Iterator iter = children.values().iterator(); iter.hasNext();) {
				Component child = (Component) iter.next();
				doVisit(child, ureq);
			}
		}
		if (visitChildrenFirst) {
			v.visit(node, ureq);
		}
	}
}