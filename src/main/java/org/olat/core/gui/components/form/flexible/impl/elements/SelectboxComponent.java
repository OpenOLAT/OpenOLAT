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

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 26.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SelectboxComponent extends FormBaseComponentImpl {


	private static final ComponentRenderer RENDERER = new SelectboxRenderer();
	private SelectboxSelectionImpl selectionWrapper;
	private String[] values;
	private String[] options;
	private String[] cssClasses;
	private boolean escapeHtml = true;

	/**
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 */
	public SelectboxComponent(String id, String name, Translator translator, SelectboxSelectionImpl selectionWrapper) {
		super(id, name, translator);
		this.selectionWrapper = selectionWrapper;
	}

	@Override
	public SelectboxSelectionImpl getFormItem() {
		return selectionWrapper;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public String getGroupingName() {
		return getComponentName();
	}

	public String[] getOptions() {
		return options;
	}

	public String[] getValues() {
		return values;
	}

	public String[] getCssClasses() {
		return cssClasses;
	}
	
	public void setOptionsAndValues(String[] options, String[] values, String[] cssClasses) {
		this.options = options;
		this.values = values;
		this.cssClasses = cssClasses;
	}
	
	public boolean isEscapeHtml() {
		return escapeHtml;
	}

	public void setEscapeHtml(boolean escapeHtml) {
		this.escapeHtml = escapeHtml;
	}

	public boolean isSelected(String key) {
		return key != null && selectionWrapper.isOneSelected() && key.equals(selectionWrapper.getSelectedKey());
	}

	/**
	 * wheter this select box allows multiple values to be selected or not
	 * @param isMultiSelect
	 */
	public boolean isMultiSelect() {
		return selectionWrapper.isMultiselect();
	}

	public Form getRootForm() {
		return selectionWrapper.getRootForm();
	}

	public int getAction() {
		return selectionWrapper.getAction();
	}

	public String getSelectionElementFormDisId(){
		return selectionWrapper.getFormDispatchId();
	}
	
	public boolean isInlineValidationOn() {
		return selectionWrapper.isInlineValidationOn();
	}
	
}
