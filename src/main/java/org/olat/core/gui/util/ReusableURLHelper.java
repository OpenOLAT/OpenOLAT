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
package org.olat.core.gui.util;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.util.component.ComponentTraverser;
import org.olat.core.util.component.ComponentVisitor;

/**
 * @author Felix Jost, http://www.goodsolutions.ch
 *
 */
public class ReusableURLHelper {
	/**
	 * Used only for load-test-mode identification of the correct component when the full path matching failed.
	 * @param childName
	 * @param searchRoot
	 * @return a List of Components which are added in parent using the name childName, that is, where something like container.put(childName,...) has taken place
	 */
	public static List<Component> findComponentsWithChildName(final String childName, Component searchRoot) {
		final List<Component> founds = new ArrayList<Component>();
		ComponentTraverser ct = new ComponentTraverser(new ComponentVisitor(){
			public boolean visit(Component comp, UserRequest ureq) {
				if(comp.getParent()==null){
					return true;
				}
				if (comp.getParent().getComponent(childName) == comp) {
					founds.add(comp);
				}
				return true;
			}}, searchRoot, true);
		ct.visitAll(null);
		return founds;
	}
}
