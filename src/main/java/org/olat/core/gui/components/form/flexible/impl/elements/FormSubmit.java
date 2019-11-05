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
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;

/**
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

	@Override
	protected void rootFormAvailable(){
		String formItemId = getFormItemId();
		component = new FormButtonComponent(formItemId, this,true);
	}
	
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		if(getRootForm().getAction() == FormEvent.ONCLICK){
			getComponent().setDirty(true);
			getRootForm().submit(ureq);
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(Window.NO_RESPONSE_VALUE_MARKER.equals(ureq.getParameter(Window.NO_RESPONSE_PARAMETER_MARKER))) {
			return; // ignore background request
		}

		// no values with submit to be evaluated
		getComponent().setDirty(true);
	}

	@Override
	public void validate(List<ValidationStatus> statusDescriptinons) {
		// submit is not validating itself
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}
	
	
	/**
	 * for submit renderer only
	 * @return
	 */
	@Override
	String getTranslated(){
		return getTranslator().translate(i18nKey);
	}


}
