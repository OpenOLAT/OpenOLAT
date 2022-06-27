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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.form.flexible.impl.Form;

/**
 * Initial Date: 04.01.2007 <br>
 * 
 * @author patrickb
 */
class CheckboxElement {

	private final MultipleSelectionElementImpl selectionWrapper;
	private final int which;
	private final String name;
	private String cssClass;
	
	private boolean enabled = true;
	private boolean visible = true;
	private boolean textOnly;
	private String iconLeftCSS;
	
	

	/**
	 * Constructor for a check box component. Set to private, use the
	 * MultipleSelectionElementImpl or even better the
	 * FormUIFactory.addCheckboxesVertical() or
	 * FormUIFactory.addCheckboxesHorizontal() methods instead
	 * 
	 * @param name
	 * @param selectionWrapper
	 *            The seection wrapper element
	 * @param which
	 *            The position of the checkbox within the selection wrapper
	 * @param cssClass
	 *            Optional css class to be added to the checkbox in a span
	 *            element. Can be NULL
	 */
	CheckboxElement(String name, MultipleSelectionElementImpl selectionWrapper, int which,
			String cssClass, String iconLeftCSS) {
		this.name = name;
		this.selectionWrapper = selectionWrapper;
		this.which = which;
		this.cssClass = cssClass;
		this.iconLeftCSS = iconLeftCSS;
	}
	
	String getName() {
		return name;
	}

	String getGroupingName(){
		return selectionWrapper.getName();
	}
	
	boolean isEnabled() {
		return enabled;
	}

	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	boolean isVisible() {
		return visible;
	}

	void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	boolean isTextOnly() {
		return textOnly;
	}

	void setTextOnly(boolean textOnly) {
		this.textOnly = textOnly;
	}

	int getWhichWeAre(){
		return which;
	}

	String getKey() {
		return selectionWrapper.getKey(which);
	}
	
	public String getFormDispatchId() {
		return selectionWrapper.getFormDispatchId() + "_C_" + which;
	}

	public String getValue() {
		return selectionWrapper.getValue(which);
	}

	public boolean isSelected() {
		return selectionWrapper.isSelected(which);
	}
	
	public String getCssClass() {
		return cssClass;
	}
	
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	
	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	public void setIconLeftCSS(String iconLeftCSS) {
		this.iconLeftCSS = iconLeftCSS;
	}

	public int getAction(){
		return selectionWrapper.getAction();
	}
	
	public Form getRootForm(){
		return selectionWrapper.getRootForm();
	}
	
	public String getSelectionElementFormDispatchId(){
		return selectionWrapper.getFormDispatchId();
	}
	
}
