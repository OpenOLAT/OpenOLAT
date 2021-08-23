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
package org.olat.core.gui.components.form.flexible.impl;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.StringHelper;

/**
 * Initial Date: 06.12.2006 <br>
 * 
 * @author patrickb
 */
public class FormDecorator {

	private final FormItemContainer container;
	
	public FormDecorator(Form theForm) {
		this.container = theForm.getFormItemContainer();
	}

	public FormDecorator(FormItemContainer container) {
		this.container = container;
	}
	
	public boolean isDomReplacementWrapperRequired() {
		return container.isDomReplacementWrapperRequired();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#hasError(java.lang.String)
	 */
	public boolean hasError(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.hasError();
	}
	
	public boolean hasWarning(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.hasWarning();
	}
	

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#hasExample(java.lang.String)
	 */
	public boolean hasExample(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.hasExample();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#hasLabel(java.lang.String)
	 */
	public boolean hasLabel(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.hasLabel();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#isMandatory(java.lang.String)
	 */
	public boolean isMandatory(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.isMandatory();
	}

	/**
	 * Check if there is a help text available
	 * @return true if help is available, false otherwise
	 */
	public boolean hasHelpText(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.getHelpText() != null;
	}

	/**
	 * Get the translated context help text string for this form item
	 * @return The help text or NULL if no help text is available
	 */
	public String getHelpText(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? null : foco.getHelpText();
	}
	
	public String getLabelText(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? null : foco.getLabelText();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#isEnabled(java.lang.String)
	 */
	public boolean isEnabled(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.isEnabled();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#isVisible(java.lang.String)
	 */
	public boolean isVisible(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? false : foco.isVisible();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#getItemId(java.lang.String)
	 */
	public String getItemId(String formItemName) {
		FormItem foco = getFormItem(formItemName);
		return foco == null ? "" : foco.getFormDispatchId();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormDecorator#isSpacerElement(java.lang.String)
	 */
	public boolean isSpacerElement(String formItemName) {
		FormItem item = getFormItem(formItemName);
		if (item == null)
			return false;
		else
			return (item instanceof SpacerElement);
	}
	
	public String ffXHREvent(String key, String value) {
		Form theForm = container.getRootForm();
		String elementId = "o_fi" + container.getComponent().getDispatchID();
		return FormJSHelper.getXHRFnCallFor(theForm, elementId, 1, true, true, new NameValuePair(key, value));
	}
	
	public String openFfXHREvent(boolean dirtyCheck, boolean pushState, boolean submit) {
		Form theForm = container.getRootForm();
		String elementId = "o_fi".concat(container.getComponent().getDispatchID());
		String cmd = FormJSHelper.getXHRFnCallFor(theForm, elementId, 1, dirtyCheck, pushState, submit);
		return cmd.substring(0, cmd.length() - 1); // remove the last (
	}
	
	public String backgroundCommand(String command, String key, String value) {
		Form theForm = container.getRootForm();
		String elementId = "o_fi" + container.getComponent().getDispatchID();
		return FormJSHelper.getXHRNFFnCallFor(theForm, elementId, 1,
				new NameValuePair("fcid", command),
				new NameValuePair(key, value));
	}
	
	public String appendFlexiFormDirty(String id) {
		StringOutput sb = new StringOutput(256);
		FormJSHelper.appendFlexiFormDirty(sb, container.getRootForm(), id);
		return sb.toString();
	}
	
	public String appendFlexiFormDirtyForCheckbox(String id) {
		StringOutput sb = new StringOutput(256);
		FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, container.getRootForm(), id);
		return sb.toString();
	}
	
	public String appendFlexiFormDirtyForClick(String id) {
		StringOutput sb = new StringOutput(256);
		FormJSHelper.appendFlexiFormDirtyForClick(sb, container.getRootForm(), id);
		return sb.toString();
	}
	
	public String getContainerCssClass() {
		if (container != null && StringHelper.containsNonWhitespace(container.getElementCssClass())) {
			return " " + container.getElementCssClass();
		}
		return "";
	}
	
	public String getElementCssClass(String formItemName) {
		FormItem item = getFormItem(formItemName);
		if (item != null && StringHelper.containsNonWhitespace(item.getElementCssClass())) {
			return " " + item.getElementCssClass();
		}
		return "";
	}
	
	public Form getForm() {
		return container.getRootForm();
	}

	/**
	 * Internal helper to get a form item for the given name
	 * 
	 * @param formItemName
	 * @return
	 */
	public FormItem getFormItem(String formItemName) {
		return container.getFormComponent(formItemName);
	}

}
