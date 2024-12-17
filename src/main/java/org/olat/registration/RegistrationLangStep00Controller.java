/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.registration;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.i18n.I18nManager;

/**
 * Initial date: Okt 25, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationLangStep00Controller extends StepFormBasicController {


	private final StepsRunContext runContext;
	private final LanguageChooserController languageChooserForm;

	public RegistrationLangStep00Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;
		this.languageChooserForm = new LanguageChooserController(ureq, wControl, true, rootForm);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(languageChooserForm.getInitialFormItem());
	}

	@Override
	public FormItem getStepFormItem() {
		return languageChooserForm.getInitialFormItem();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof SingleSelection se && se.isOneSelected()) {
			Locale localeOrDefault = I18nManager.getInstance().getLocaleOrDefault(se.getSelectedKey());
			ureq.getUserSession().setLocale(localeOrDefault);
			fireEvent(ureq, StepsEvent.RELOAD);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		runContext.put(RegWizardConstants.CHOSEN_LANG, languageChooserForm.getSelectedLanguage());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
