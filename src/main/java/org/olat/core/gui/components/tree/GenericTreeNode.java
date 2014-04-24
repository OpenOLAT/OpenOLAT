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

package org.olat.core.gui.components.tree;

import java.io.Serializable;

import org.olat.core.util.nodes.GenericNode;

/**
 * @author Mike Stock
 */
public class GenericTreeNode extends GenericNode implements TreeNode, Serializable {

	private static final long serialVersionUID = -2381133733726739228L;
	
	private Object userObject;
	private String title;
	private String altText;
	private boolean accessible = true; // can be clicked?
	private boolean selected = false;
	private TreeNode delegate; // if this node is clicked, delegate to that
	private String cssClass;
	private String iconCssClass;
	private String iconDecoratorCssClass;
	
	/**
	 * 
	 */
	public GenericTreeNode() {
		//
	}
	
	/**
	 * Fix identifier for state-less behavior
	 * @param ident
	 */
	public GenericTreeNode(String ident) {
		super(ident);
	}

	/**
	 * @param title
	 * @param userObject
	 */
	public GenericTreeNode(String title, Object userObject) {
		this.title = title;
		this.userObject = userObject;
	}
	
	/**
	 * @param id A fix identification for state-less behavior, must be unique
	 * @param title
	 * @param userObject
	 */
	public GenericTreeNode(String ident, String title, Object userObject) {
		super(ident);
		this.title = title;
		this.userObject = userObject;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getAltText()
	 */
	public String getAltText() {
		return altText;
	}

	/**
	 * @param altText
	 */
	public void setAltText(String altText) {
		this.altText = altText;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#isAccessible()
	 */
	public boolean isAccessible() {
		return accessible;
	}

	/**
	 * @param accessible
	 */
	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getUserObject()
	 */
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * @param userObject
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * @return TreeNode
	 */
	public TreeNode getDelegate() {
		return delegate;
	}

	/**
	 * Sets the delegate.
	 * 
	 * @param delegate The delegate to set
	 */
	public void setDelegate(TreeNode delegate) {
		this.delegate = delegate;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getCssClass()
	 */
	@Override
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * @param string
	 */
	public void setCssClass(String string) {
		cssClass = string;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getIconCssClass()
	 */
	@Override
	public String getIconCssClass() {
		return iconCssClass;
	}
	
	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

	/**
	 * @see org.olat.core.gui.components.tree.TreeNode#getIconDecorator1CssClass()
	 */
	@Override
	public String getIconDecorator1CssClass() {
		return iconDecoratorCssClass;
	}
	
	public void setIconDecorator1CssClass(String iconDecoratorCssClass) {
		this.iconDecoratorCssClass = iconDecoratorCssClass;
	}

	@Override
	public String getIconDecorator2CssClass() {
		return null;
	}

	@Override
	public String getIconDecorator3CssClass() {
		return null;
	}

	@Override
	public String getIconDecorator4CssClass() {
		return null;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}