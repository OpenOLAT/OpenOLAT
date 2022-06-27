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

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.CsrfDelegate;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

class FormWrapperContainer extends AbstractComponent implements ComponentCollection, CsrfDelegate {

	// Renderer
	private static final ComponentRenderer RENDERER = new FormWrapperContainerRenderer();
	// associated form knows the choosen layout
	private Form form;

	/**
	 * @param id A fix identifier for the container
	 * @param name
	 * @param translator
	 * @param form
	 */
	public FormWrapperContainer(String id, String name, Translator translator, Form form) {
		super(id, name, translator);
		this.form = form;
	}

	public String getDispatchFieldId() {
		return form.getDispatchFieldId();
	}

	public String getEventFieldId() {
		return form.getEventFieldId();
	}

	public String getFormName() {
		return form.getFormName();
	}
	
	public boolean isCsrfProtected() {
		return form.isCsrfProtection();
	}

	/**
	 * @return
	 */
	ComponentCollection getFormLayout() {
		return form.getFormLayout();
	}
	
	public WindowControl getWindowControl() {
		return form.getWindowControl();
	}

	@Override
	public Component getComponent(String name) {
		if(form.getFormLayout().getComponentName().equals(name)) {
			return form.getFormLayout();
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return Collections.<Component>singletonList(form.getFormLayout());
	}

	/**
	 * @return true: form contains multipart elements; false: form does not contain multipart elements
	 */
	boolean isMultipartEnabled() {
		return form.isMultipartEnabled();
	}
	
	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		form.evalFormRequest(ureq);
	}

	/**
	 * @param ureq
	 * @param ok
	 */
	void fireValidation(UserRequest ureq, boolean ok) {
		fireValidation(ureq, ok, org.olat.core.gui.components.form.Form.EVNT_VALIDATION_OK);
	}
	
	/**
	 * Fire the validation event
	 * @param ureq
	 * @param ok Validation ok or not
	 * @param okEvent Specify the OK event
	 */
	void fireValidation(UserRequest ureq, boolean ok, Event okEvent) {
		if (ok) {
			fireEvent(ureq, okEvent);
		} else {
			fireEvent(ureq, org.olat.core.gui.components.form.Form.EVNT_VALIDATION_NOK);
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public void fireFormEvent(UserRequest ureq, FormEvent event) {
		fireEvent(ureq, event);
	}
}