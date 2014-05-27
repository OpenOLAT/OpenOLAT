/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * controller to let the user ensure that he has the copyright on this artefact
 * 
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm02 extends StepFormBasicController {

	private AbstractArtefact artefact;
	private MultipleSelectionElement crCheck;

	public EPCollectStepForm02(UserRequest ureq, WindowControl windowControl, Form form, StepsRunContext stepsRunContext, int layoutDefault,
			String customLayoutPageName, AbstractArtefact artefact) {
		super(ureq, windowControl, form, stepsRunContext, layoutDefault, customLayoutPageName);
		this.artefact = artefact;
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("copyright.intro.text");

		String[] theKeys = new String[] { Boolean.TRUE.toString() };
		String[] theValues = new String[] { translate("copyright.yes") };
		crCheck = uifactory.addCheckboxesHorizontal("copyright.label", null, formLayout, theKeys, theValues);
		if (getFromRunContext("copyright.accepted") != null && (Boolean) getFromRunContext("copyright.accepted")) {
			crCheck.select(Boolean.TRUE.toString(), true);
		}
		//signature > 0 means, collection wizzard can be sure its from OLAT, < 0 means get an approval by user (the target value is the negative one)
		if (artefact.getSignature() > 0 ){
			crCheck.select(Boolean.TRUE.toString(), true);
		}
		crCheck.addActionListener(FormEvent.ONCHANGE);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == crCheck) {
			validateAndSetError();
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return super.validateFormLogic(ureq) && validateAndSetError();
	}

	private boolean validateAndSetError() {
		if (!crCheck.isSelected(0)	|| (getFromRunContext("copyright.accepted") != null && !(Boolean) getFromRunContext("copyright.accepted"))) {
			crCheck.setErrorKey("copyright.error", null);
			return false;
		} else {
			crCheck.showError(false);
			return true;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// its accepted, as form has been validated before
		if (isUsedInStepWizzard()) {
			addToRunContext("copyright.accepted", true);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			// if used outside steps wizzard, persist stuff here
		}		
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}
}
