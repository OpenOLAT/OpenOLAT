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
package org.olat.course.nodes.card2brain;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.card2brain.Card2BrainManager;
import org.olat.modules.card2brain.Card2BrainModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.04.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Card2BrainConfigController extends FormBasicController {

	private static final String[] enabledKeys = new String[] { "on" };
	private static final String FORM_MISSING_MANDATORY = "form.legende.mandatory";

	private TextElement flashcardAliasEl;
	private SpacerElement privateLoginSpacer;
	private MultipleSelectionElement enablePrivateLoginEl;
	private TextElement privateKeyEl;
	private TextElement privateSecretEl;
	private FormLink previewButton;
	private LayoutMain3ColsPreviewController previewLayoutCtr;

	private final ModuleConfiguration config;

	@Autowired
	private Card2BrainModule card2BrainModule;
	@Autowired
	private Card2BrainManager card2BrainManager;

	public Card2BrainConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);

		this.config = config;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("Knowledge Transfer#_card2brain");

		String flashcardAlias = config.getStringValue(Card2BrainCourseNode.CONFIG_FLASHCARD_ALIAS);
		flashcardAliasEl = uifactory.addTextElement("edit.flashcard.alias", "edit.flashcard.alias", 128, flashcardAlias,
				formLayout);
		flashcardAliasEl.setMandatory(true);
		flashcardAliasEl.setHelpTextKey("edit.FlashcardHelpText", null);

		previewButton = uifactory.addFormLink("edit.preview", formLayout, "btn btn-default o_xsmall");
		previewButton.setIconLeftCSS("o_icon o_icon_preview");

		privateLoginSpacer = uifactory.addSpacerElement("Spacer", formLayout, false);

		boolean enablePrivateLogin = config.getBooleanSafe(Card2BrainCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN);
		String[] enableValues = new String[] { translate("on") };
		enablePrivateLoginEl = uifactory.addCheckboxesHorizontal("edit.access.enablePrivateLogin", formLayout,
				enabledKeys, enableValues);
		if (enablePrivateLogin) {
			enablePrivateLoginEl.select(enabledKeys[0], true);
		}
		enablePrivateLoginEl.addActionListener(FormEvent.ONCHANGE);

		String privateKey = config.getStringValue(Card2BrainCourseNode.CONFIG_PRIVATE_KEY);
		privateKeyEl = uifactory.addTextElement("edit.access.privateKey", "edit.access.privateKey", 128, privateKey,
				formLayout);
		privateKeyEl.setMandatory(true);
		privateKeyEl.setHelpTextKey("edit.KeyHelpText", null);

		String privateSecret = config.getStringValue(Card2BrainCourseNode.CONFIG_PRIVATE_SECRET);
		privateSecretEl = uifactory.addTextElement("edit.access.privateSecret", "edit.access.privateSecret",
				128, privateSecret, formLayout);
		privateSecretEl.setMandatory(true);
		privateSecretEl.setHelpTextKey("edit.SecretHelpText", null);

		uifactory.addFormSubmitButton("save", formLayout);

		showHidePrivateLoginFields();
		validateLogin();
		
		if (StringHelper.containsNonWhitespace(flashcardAlias)) {
			// Validate at init e.g. if the flashcards still exists.
			validateFormLogic(ureq);
		} else {
			// Don't show the preview button after insert of an new course element
			showHidePreviewButton(false);
		}
	}

	/**
	 * The checkbox of the private login is only visible if the appropriate
	 * option is enabled.
	 */
	private void showHidePrivateLoginFields() {
		privateLoginSpacer.setVisible(card2BrainModule.isPrivateLoginEnabled());
		enablePrivateLoginEl.setVisible(card2BrainModule.isPrivateLoginEnabled());

		privateKeyEl.setVisible(card2BrainModule.isPrivateLoginEnabled() && isPrivateLoginActivated());
		privateSecretEl.setVisible(card2BrainModule.isPrivateLoginEnabled() && isPrivateLoginActivated());
	}

	/**
	 * Show or hide the preview button.
	 */
	private void showHidePreviewButton(boolean show) {
		previewButton.setVisible(show);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateFlashcardAlias(card2BrainManager.parseAlias(flashcardAliasEl.getValue()));
		allOk &= validateLogin();

		// Show the preview button only when the configuration is valid.
		showHidePreviewButton(allOk);

		return allOk;
	}

	private boolean validateFlashcardAlias(String alias) {
		boolean allOk = true;

		if (!StringHelper.containsNonWhitespace(alias)) {
			flashcardAliasEl.setErrorKey(FORM_MISSING_MANDATORY, null);
			allOk &= false;
		} else if (!card2BrainManager.checkSetOfFlashcards(alias)) {
			flashcardAliasEl.setErrorKey("edit.warning.aliasCheckFailed", null);
			allOk &= false;
		}

		return allOk;
	}

	private boolean validateLogin() {
		boolean allOk = true;

		if (isPrivateLoginActivated()) {
			if (!StringHelper.containsNonWhitespace(privateKeyEl.getValue())) {
				privateKeyEl.setErrorKey(FORM_MISSING_MANDATORY, null);
				allOk &= false;
			}
			if (!StringHelper.containsNonWhitespace(privateSecretEl.getValue())) {
				privateSecretEl.setErrorKey(FORM_MISSING_MANDATORY, null);
				allOk &= false;
			}
		}
		
		boolean isEnterpriseLogin = !isPrivateLoginActivated();
		if (!card2BrainModule.isEnterpriseLoginEnabled() && !card2BrainModule.isPrivateLoginEnabled()) {
			setFormWarning("edit.warning.bothLoginDisabled");
			allOk &= false;
		} else if (isEnterpriseLogin && !card2BrainModule.isEnterpriseLoginEnabled()) {
			setFormWarning("edit.warning.enterpriseLoginDisabled");
			allOk &= false;
		} else if (!isEnterpriseLogin && !card2BrainModule.isPrivateLoginEnabled()) {
			setFormWarning("edit.warning.privateLoginDisabled");
			allOk &= false;
		} else {
			setFormWarning(null);
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		setFormWarning(null);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enablePrivateLoginEl) {
			showHidePrivateLoginFields();
		} else if (source == previewButton) {
			Controller card2brainRunCtr = new Card2BrainRunController(ureq, getWindowControl(), config);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
					card2brainRunCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(card2brainRunCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == previewLayoutCtr) {
			removeAsListenerAndDispose(previewLayoutCtr);
		}
		super.event(ureq, source, event);
	}

	protected ModuleConfiguration getUpdatedConfig() {
		config.set(Card2BrainCourseNode.CONFIG_FLASHCARD_ALIAS, card2BrainManager.parseAlias(flashcardAliasEl.getValue()));
		if (isPrivateLoginActivated()) {
			config.set(Card2BrainCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, Boolean.toString(true));
			config.set(Card2BrainCourseNode.CONFIG_PRIVATE_KEY, privateKeyEl.getValue());
			config.set(Card2BrainCourseNode.CONFIG_PRIVATE_SECRET, privateSecretEl.getValue());
		} else {
			config.set(Card2BrainCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, Boolean.toString(false));
			config.set(Card2BrainCourseNode.CONFIG_PRIVATE_KEY, null);
			config.set(Card2BrainCourseNode.CONFIG_PRIVATE_SECRET, null);
		}
		return config;
	}

	/**
	 * 
	 * @return whether the private login is activated by the user.
	 */
	private boolean isPrivateLoginActivated() {
		boolean isPrivateLoginActivated = false;

		if (enablePrivateLoginEl.isAtLeastSelected(1)) {
			isPrivateLoginActivated = true;
		}

		return isPrivateLoginActivated;
	}

}
