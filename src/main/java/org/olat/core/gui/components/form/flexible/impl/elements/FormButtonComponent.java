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
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Initial Date:  08.12.2006 <br>
 * @author patrickb
 */
class FormButtonComponent extends FormBaseComponentImpl {

	private FormButton formButton;
	private boolean isSubmitAndValidate = false;
	private static final ComponentRenderer RENDERER = new FormButtonRenderer();

	FormButtonComponent(String id, FormButton foButton) {
		super(id, foButton.getName());
		this.formButton = foButton;
		setDomReplacementWrapperRequired(false);
		setSpanAsDomReplaceable(true);
	}
	
	/**
	 * needed to have some special js code for "submit And validate" button(s)
	 * @param foButton
	 * @param isSubmitAndValidateButton
	 */
	FormButtonComponent(String id, FormButton foButton, boolean isSubmitAndValidateButton) {
		this(id, foButton);
		this.isSubmitAndValidate = isSubmitAndValidateButton;
	}
	
	@Override
	public FormButton getFormItem(){
		return formButton;
	}
	/**
	 * for special js code in renderer
	 * @return
	 */
	boolean getIsSubmitAndValidate(){
		return isSubmitAndValidate;
	}

	public void setSubmitAndValidate(boolean isSubmitAndValidate) {
		this.isSubmitAndValidate = isSubmitAndValidate;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
