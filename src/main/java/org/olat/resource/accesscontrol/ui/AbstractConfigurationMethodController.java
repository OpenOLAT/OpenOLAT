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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferAccess;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for AbstractConfigurationMethodController
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractConfigurationMethodController extends FormBasicController {
	
	private String name;
	private boolean embbed = false;
	
	public AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	public AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, String pageName) {
		super(ureq, wControl, pageName);
	}

	public AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, String pageName, Translator fallbackTranslator) {
		super(ureq, wControl, pageName, fallbackTranslator);
	}

	protected AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, int layout){
		super(ureq, wControl, layout);
	}

	protected AbstractConfigurationMethodController(UserRequest ureq, WindowControl wControl, int layout, String customLayoutPageName, Form externalMainForm) {
		super(ureq, wControl, layout, customLayoutPageName, externalMainForm);
		embbed = (externalMainForm != null);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!embbed) {
			
			final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			buttonGroupLayout.setRootForm(mainForm);
			formLayout.add(buttonGroupLayout);
			
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public abstract OfferAccess commitChanges();
	
	public abstract AccessMethod getMethod();

	public FormItem getInitialFormItem() {
		return flc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
