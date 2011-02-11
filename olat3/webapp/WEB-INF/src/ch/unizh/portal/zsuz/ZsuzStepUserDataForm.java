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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.unizh.portal.zsuz;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZsuzStepUserDataForm
 * 
 * <P>
 * Initial Date: 19.06.2008 <br>
 * 
 * @author patrickb
 */
public class ZsuzStepUserDataForm extends StepFormBasicController {

	private final static String FORMIDENTIFIER = ZsuzStepUserDataForm.class.getCanonicalName();
	private List<UserPropertyHandler> userPropertyHandlers;
	private UserManager um;

	public ZsuzStepUserDataForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName) {
		super(ureq, control, rootForm, runContext, layout, customLayoutPageName);
		setBasePackage(this.getClass());
		um = UserManager.getInstance();
		Translator withUserProps =UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()); 
		setTranslator(withUserProps);
		flc.setTranslator(withUserProps);
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//list with property-key-name and value for the MailTemplate in the next step
		List<String[]> propsAndValues = new ArrayList<String[]>(userPropertyHandlers.size());
		//
		BaseSecurity im = BaseSecurityManager.getInstance();
		Identity identity = im.findIdentityByName(getIdentity().getName());
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String propertyName = userPropertyHandler.getName();
			FormItem fi = this.flc.getFormComponent(propertyName);
			String propertyValue = userPropertyHandler.getStringValue(fi);
			//(propertyname, propertyvalue) for mailtemplate
			propsAndValues.add(new String[]{translate(userPropertyHandler.i18nFormElementLabelKey()), propertyValue});
			//set property value
			identity.getUser().setProperty(propertyName, propertyValue);
		}
		//save address information
		um.updateUserFromIdentity(identity);
		// 
		addToRunContext("userproperties", propsAndValues);
		// inform surrounding Step runner to proceed
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);

	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		//check users properties
		
		boolean isValid = true;
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if(userPropertyHandler == null) continue;
			//so far only textelement are supported in address field in this form here!
			String compName = userPropertyHandler.getName();
			TextElement currentTextelement = (TextElement) this.flc.getFormComponent(compName);
			String currentPropValue = currentTextelement.getValue();
			if (currentTextelement.isMandatory() && currentPropValue.trim().equals("")) {
				currentTextelement.setErrorKey("new.form.mandatory", new String[] {});
				isValid = false;
			}
		}
		
		if(isValid){
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		return isValid;
	}

	@Override
	@SuppressWarnings("unused")
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
		// which fields are shown here is defined in
		// olat_userconfig.xml -> search for <entry key="ch.unizh.portal.zsuz.ZsuzStepUserDataForm">
		// validation of fields happens in validateFormLogic(..) and save/update is done
		// in formOK(..)
		//
		userPropertyHandlers = um.getUserPropertyHandlersFor(FORMIDENTIFIER, false);
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			// adds the element to the formLayout
			userPropertyHandler.addFormItem(getLocale(), getIdentity().getUser(), FORMIDENTIFIER, false, formLayout);
		}

	}

}
