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

import org.olat.core.gui.components.badge.Badge;
import org.olat.core.util.nodes.GenericNode;

/**
 * @author Mike Stock
 */
public class GenericTreeNode extends GenericNode implements TreeNode {

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
	private Badge badge;

	public GenericTreeNode() {
		//
	}
	
	public GenericTreeNode(String ident) {
		super(ident);
	}

	public GenericTreeNode(String title, Object userObject) {
		this.title = title;
		this.userObject = userObject;
	}
	
	public GenericTreeNode(String ident, String title, Object userObject) {
		super(ident);
		this.title = title;
		this.userObject = userObject;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	@Override
	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	@Override
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	public TreeNode getDelegate() {
		return delegate;
	}

	/**
	 * Sets the delegate node which will be activated when this node is clicked.
	 * 
	 * @param delegate
	 */
	public void setDelegate(TreeNode delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String string) {
		cssClass = string;
	}

	@Override
	public String getIconCssClass() {
		return iconCssClass;
	}
	
	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

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

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	@Override
	public Badge getBadge() {
		return badge;
	}
	
	public void setBadge(String message, Badge.Level level) {
		if(badge == null) {
			badge = new Badge(getIdent() + "_BADGE");
		}
		badge.setMessage(message);
		badge.setLevel(level);
	}
	
	public void removeBadge() {
		badge = null;
	}
}