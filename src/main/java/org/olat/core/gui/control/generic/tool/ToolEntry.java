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

package org.olat.core.gui.control.generic.tool;

import org.olat.core.gui.components.Component;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ToolEntry {
	private static final int TYPE_HEADER = 1;
	private static final int TYPE_LINK = 2;
	private static final int TYPE_POPUP_LINK = 3;
	private static final int TYPE_COMPONENT = 4;
	
	private int type;
	private String ident;
	private String headerText, action, linkText;
	private Component component;
	private String componentName;
	private boolean enabled = true;
	private String cssClass = "b_toolbox_link"; // default
	private String elementCssClass;
	private String width;
	private String height;
	private boolean browserMenubarEnabled;

	/**
	 * @param ident
	 * @param headerText
	 * @param cssClass a css class that provides an toolbox title bar image.
	 *          optional, can be null (default image)
	 */
	public ToolEntry(String ident, String headerText, String cssClass) {
		this.type = TYPE_HEADER;
		this.ident = ident;
		this.headerText = headerText;
		if (cssClass == null) {
			// header default;
			this.cssClass = "b_toolbox_head_default";
		}	else {
			this.cssClass = cssClass;
		}
	}

	/**
	 * @param ident
	 * @param action
	 * @param linkText
	 * @param cssClass
	 */
	public ToolEntry(String ident, String action, String linkText, String cssClass, String elementCssClass) {
		this.type = TYPE_LINK;
		this.ident = ident;
		this.action = action;
		this.linkText = linkText;
		if (cssClass != null) this.cssClass = cssClass;
		this.elementCssClass = elementCssClass;
	}

	/**
	 * @param ident
	 * @param componentName
	 * @param component
	 */
	public ToolEntry(String ident, String componentName, Component component) {
		this.type = TYPE_COMPONENT;
		this.ident = ident;
		this.component = component;
		this.componentName = componentName;
	}

	/**
	 * @param ident
	 * @param action
	 * @param linkText
	 * @param cssClass
	 * @param width
	 * @param height
	 * @param browserMenubarEnabled
	 */
	public ToolEntry(String ident, String action, String linkText, String cssClass, String elementCssClass, String width, String height, boolean browserMenubarEnabled) {
		this.type = TYPE_POPUP_LINK;
		this.ident = ident;
		this.action = action;
		this.linkText = linkText;
		if (cssClass != null) this.cssClass = cssClass;
		this.elementCssClass = elementCssClass;
		this.width = width;
		this.height = height;
		this.browserMenubarEnabled = browserMenubarEnabled;
	}

	
	/**
	 * @return Returns the enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled The enabled to set.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return Returns the ident.
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * @return String
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @return Component
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * @return String
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * @return String
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * @return String css class of the enclosing div tag
	 */
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	/**
	 * @return The element CSS class used for Selenium tests.
	 */
	public String getElementCssClass() {
		return elementCssClass;
	}

	/**
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return String
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * @return boolean
	 */
	public boolean isBrowserMenuEnabled() {
		return browserMenubarEnabled;
	}
	/**
	 * @return height
	 */
	public String getHeight() {
		return height;
	}
	/**
	 * @return height
	 */
	public String getWidth() {
		return width;
	}
}