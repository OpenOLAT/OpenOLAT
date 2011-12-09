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

package org.olat.core.util.component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;

/**
 * Description: <br>
 * FIXME:pb discuss "copy pasted -> sign that Container and component should be interfaces"
 * 
 * @author Patrick Brunner
 */
public class FormComponentTraverser {

	private FormComponentVisitor v;
	private FormItem root;
	private boolean visitChildrenFirst;

	/**
	 * @param v
	 * @param root
	 * @param visitChildrenFirst
	 */
	public FormComponentTraverser(FormComponentVisitor v, FormItem root, boolean visitChildrenFirst) {
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

	private void doVisit(FormItem node, UserRequest ureq) {
		if (!visitChildrenFirst) {
			if (!v.visit(node, ureq)) return;
		}
		if (node instanceof FormItemContainer) { // visit children
			FormItemContainer co = (FormItemContainer) node;
			Map<String, FormItem> children = co.getFormComponents();
			Set<FormItem> formItems = new HashSet<FormItem>(children.values());
			for (Iterator<FormItem> iter = formItems.iterator(); iter.hasNext();) {
				doVisit(iter.next(), ureq);
			}
		}
		if (visitChildrenFirst) {
			v.visit(node, ureq);
		}
	}
}