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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormSubmitComponent
 * 
 * <P>
 * Initial Date:  08.12.2006 <br>
 * @author patrickb
 */
class FormButtonComponent extends FormBaseComponentImpl {

	private FormButton formButton;
	private boolean isSubmitAndValidate = false;
	private static final ComponentRenderer RENDERER = new FormButtonRenderer();

	FormButtonComponent(FormButton foButton) {
		super(foButton.getName());
		this.formButton = foButton;
	}
	
	/**
	 * needed to have some special js code for "submit And validate" button(s)
	 * @param foButton
	 * @param isSubmitAndValidateButton
	 */
	FormButtonComponent(FormButton foButton, boolean isSubmitAndValidateButton) {
		this(foButton);
		this.isSubmitAndValidate = isSubmitAndValidateButton;
	}
	
	/**
	 * 
	 * @return
	 */
	FormButton getFormButton(){
		return formButton;
	}
	/**
	 * for special js code in renderer
	 * @return
	 */
	boolean getIsSubmitAndValidate(){
		return isSubmitAndValidate;
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
