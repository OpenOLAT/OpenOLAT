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
package org.olat.modules.quality.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.quality.QualityModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityAdminGeneralController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl;
	private TextElement fromEmailEl;
	private TextElement fromNameEl;
	
	@Autowired
	private QualityModule qualityModule;
	
	public QualityAdminGeneralController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.config.title");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, onKeys, onValues);
		if (qualityModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		fromEmailEl = uifactory.addTextElement("admin.from.email", 500, qualityModule.getFromEmail(), formLayout);
		fromEmailEl.setHelpTextKey("admin.from.email.help", null);
		
		fromNameEl = uifactory.addTextElement("admin.from.name", 500, qualityModule.getFromName(), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fromEmailEl.clearError();
		fromNameEl.clearError();
		if (StringHelper.containsNonWhitespace(fromEmailEl.getValue())) {
			if (!MailHelper.isValidEmailAddress(fromEmailEl.getValue())) {
				fromEmailEl.setErrorKey("error.email.invalid", null);
				allOk = false;
			}
		} else if (StringHelper.containsNonWhitespace(fromNameEl.getValue())) {
			fromNameEl.setErrorKey("error.email.name.no.address", null);
			allOk = false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		qualityModule.setEnabled(enableEl.isAtLeastSelected(1));
		
		qualityModule.setFromEmail(fromEmailEl.getValue());
		qualityModule.setFromName(fromNameEl.getValue());
	}

}
