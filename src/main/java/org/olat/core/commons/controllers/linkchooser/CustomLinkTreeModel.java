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

package org.olat.core.commons.controllers.linkchooser;

import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;



/**
 * Model of internal-links. E.g. course-node tree model with gotoNode information.
 * 
 * @author Christian Guretzki
 */
public abstract class CustomLinkTreeModel extends AjaxTreeModel implements TreeModel {

	public CustomLinkTreeModel(String treeModelIdentifyer) {
		super(treeModelIdentifyer);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param nodeId
	 * @return Link URL for a certain node-id e.g. 'javascript:gotoNode(745678661155)'
	 */
	public abstract String  getInternalLinkUrlFor(String nodeId);
}