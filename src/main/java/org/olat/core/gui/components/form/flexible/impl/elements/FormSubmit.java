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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormSubmit
 * <P>
 * Initial Date: 24.11.2006 <br>
 * 
 * @author patrickb
 */
public class FormSubmit extends FormButton implements Submit{
	
	private String i18nKey;
	private FormButtonComponent component;

	public FormSubmit(String name, String i18nKey) {
		this(null, name, i18nKey);
	}
	
	public FormSubmit(String id, String name, String i18nKey) {
		super(id, name);
		if(!StringHelper.containsNonWhitespace(i18nKey)){
			throw new AssertException("i18nKey must not be null");
		}
		this.i18nKey = i18nKey;
		this.action = FormEvent.ONCLICK;
	}

	protected void rootFormAvailable(){
		String formItemId = getFormItemId();
		component = new FormButtonComponent(formItemId, this,true);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponentImpl#doDispatchFormRequest(org.olat.core.gui.UserRequest,
	 *      long[], int)
	 */
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		if(getRootForm().getAction() == FormEvent.ONCLICK){
			getComponent().setDirty(true);
			getRootForm().submit(ureq);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponentImpl#rememberFormRequest(org.olat.core.gui.UserRequest,
	 *      long[], int)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		// no values with submit to be evaluated
		getComponent().setDirty(true);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#validate()
	 */
	public void validate(List<ValidationStatus> statusDescriptinons) {
		// submit is not validating itself
	}

	protected Component getFormItemComponent() {
		return component;
	}
	
	
	/**
	 * for submit renderer only
	 * @return
	 */
	String getTranslated(){
		return getTranslator().translate(i18nKey);
	}


}
