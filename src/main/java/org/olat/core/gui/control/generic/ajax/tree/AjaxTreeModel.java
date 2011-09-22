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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.gui.control.generic.ajax.tree;

import java.util.List;

import org.olat.core.logging.LogDelegator;

/**
 * 
 * Description:<br>
 * The ajax tree model provides method for the ajax tree to dynamically build
 * the tree based on user selection. Compared to the standard tree model the
 * tree is not browsed via the child relationship but rather by the
 * getChildrenForm method.
 * 
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public abstract class AjaxTreeModel extends LogDelegator {
	private String customRootIconCssClass;
	String treeModelIdentifyer;

	/**
	 * Constructor
	 * 
	 * @param treeModelIdentifyer
	 *            to identify the the tree, is also used for the root node and
	 *            to store and restore the tree state
	 */
	public AjaxTreeModel(String treeModelIdentifyer) {
		this.treeModelIdentifyer = treeModelIdentifyer;
	}
	
	/**
	 * @return the identifyer for this tree.
	 */
	public final String getTreeModelIdentifyer() {
		return treeModelIdentifyer;
	}

	/**
	 * Set a custom CSS class icon for the root node. The default icon is an
	 * opened and closed folder icon. Use NULL to reset to default.
	 * 
	 * @param rootIconCssClass
	 */
	public final void setCustomRootIconCssClass(String rootIconCssClass) {
		this.customRootIconCssClass = rootIconCssClass;
	}

	/**
	 * Get the custom CSS icon for the root node or NULL if the default icon is
	 * used
	 * 
	 * @return
	 */
	public final String getCustomRootIconCssClass() {
		return customRootIconCssClass;
	}

	/**
	 * Returns the list of children for this node.
	 * 
	 * @param nodeId The ID of the given node. 
	 * @return
	 */
	public abstract List<AjaxTreeNode> getChildrenFor(String nodeId);	

}
