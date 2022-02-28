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
package org.olat.modules.edubase.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.edubase.EdubaseModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 11.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseAdminController extends FormBasicController {

	private static final String[] enabledKeys = new String[]{"on"};

	private MultipleSelectionElement edubaseEnabledEl;
	private TextElement edubaseOauthKeyEl;
	private TextElement edubaseOauthSecretEl;
	private TextElement edubaseLtiLaunchUrlEl;
	private TextElement edubaseReaderUrlEl;
	private MultipleSelectionElement edubaseReaderUrlUniqueEl;
	private TextElement edubaseInfoverUrlEl;
	private TextElement edubaseCoverUrlEl;

	@Autowired
	private EdubaseModule edubaseModule;

	public EdubaseAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_settings");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Edubase
		FormLayoutContainer edubaseCont = FormLayoutContainer.createDefaultFormLayout("edubase_admin", getTranslator());
		edubaseCont.setFormTitle(translate("admin.edubase.title"));
		edubaseCont.setFormContextHelp("manual_user/course_elements/Knowledge_Transfer/#edubase");
		edubaseCont.setFormDescription(translate("admin.edubase.description"));
		edubaseCont.setRootForm(mainForm);
		formLayout.add("edubase", edubaseCont);

		String[] enableValues = new String[]{ translate("on") };
		edubaseEnabledEl = uifactory.addCheckboxesHorizontal("admin.edubase.enabled", edubaseCont, enabledKeys, enableValues);
		edubaseEnabledEl.select(enabledKeys[0], edubaseModule.isEnabled());

		String edubaseOauthKey = edubaseModule.getOauthKey();
		edubaseOauthKeyEl = uifactory.addTextElement("admin.edubase.oauth.key", "admin.edubase.oauth.key", 128, edubaseOauthKey, edubaseCont);
		edubaseOauthKeyEl.setMandatory(true);

		String edubaseOauthSecret = edubaseModule.getOauthSecret();
		edubaseOauthSecretEl = uifactory.addTextElement("admin.edubase.oauth.secret", "admin.edubase.oauth.secret", 128, edubaseOauthSecret, edubaseCont);
		edubaseOauthSecretEl.setMandatory(true);

		uifactory.addSpacerElement("Spacer", edubaseCont, false);
		uifactory.addStaticTextElement("admin.expert.settings", null, edubaseCont);

		String edubaseLtiLaunchUrl = edubaseModule.getLtiLaunchUrl();
		edubaseLtiLaunchUrlEl = uifactory.addTextElement("admin.edubase.lti.launch.url", "admin.edubase.lti.launch.url", 128, edubaseLtiLaunchUrl, edubaseCont);
		edubaseLtiLaunchUrlEl.setMandatory(true);

		String edubaseReaderUrl = edubaseModule.getReaderUrl();
		edubaseReaderUrlEl = uifactory.addTextElement("admin.edubase.reader.url", "admin.edubase.reader.url", 128, edubaseReaderUrl, edubaseCont);
		edubaseReaderUrlEl.setMandatory(true);
		
		edubaseReaderUrlUniqueEl = uifactory.addCheckboxesHorizontal("admin.edubase.reader.url.unique", edubaseCont, enabledKeys, enableValues);
		edubaseReaderUrlUniqueEl.setHelpTextKey("admin.edubase.reader.url.unique.help", null);
		edubaseReaderUrlUniqueEl.select(enabledKeys[0], edubaseModule.isReaderUrlUnique());

		String edubaseInfoverUrl = edubaseModule.getInfoverUrl();
		edubaseInfoverUrlEl = uifactory.addTextElement("admin.edubase.infover.url", "admin.edubase.infover.url", 128, edubaseInfoverUrl, edubaseCont);
		edubaseInfoverUrlEl.setMandatory(true);
		
		String edubaseCoverUrl = edubaseModule.getCoverUrl();
		edubaseCoverUrlEl = uifactory.addTextElement("admin.edubase.cover.url", "admin.edubase.cover.url", 128, edubaseCoverUrl, edubaseCont);
		edubaseCoverUrlEl.setMandatory(true);

		// Edubook
		FormLayoutContainer edubookCont = FormLayoutContainer.createDefaultFormLayout("edubook_admin", getTranslator());
		edubaseCont.setRootForm(mainForm);
		formLayout.add("edubook", edubookCont);

		// Buttons
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsWrapperCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		edubaseModule.setEnabled(edubaseEnabledEl.isAtLeastSelected(1));
		edubaseModule.setOauthKey(edubaseOauthKeyEl.getValue());
		edubaseModule.setOauthSecret(edubaseOauthSecretEl.getValue());
		edubaseModule.setLtiLaunchUrl(edubaseLtiLaunchUrlEl.getValue());
		edubaseModule.setReaderUrl(edubaseReaderUrlEl.getValue());
		edubaseModule.setReaderUrlUnique(edubaseReaderUrlUniqueEl.isAtLeastSelected(1));
		edubaseModule.setInfoverUrl(edubaseInfoverUrlEl.getValue());
		edubaseModule.setCoverUrl(edubaseCoverUrlEl.getValue());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// Validate Edubase fields only if enabled
		if((edubaseEnabledEl.isAtLeastSelected(1))) {
			allOk &= validateIsMandatory(edubaseOauthKeyEl);
			allOk &= validateIsMandatory(edubaseOauthSecretEl);
			allOk &= validateIsMandatory(edubaseLtiLaunchUrlEl);
			allOk &= validateIsMandatory(edubaseReaderUrlEl);
			allOk &= validateIsMandatory(edubaseInfoverUrlEl);
			allOk &= validateIsMandatory(edubaseCoverUrlEl);
		}

		return allOk;
	}

	private boolean validateIsMandatory(TextElement textElement) {
		boolean allOk = true;

		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}
}
