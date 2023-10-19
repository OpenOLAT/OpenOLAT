/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.admin.lifecycle;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.mail.MailHelper;
import org.olat.group.ui.lifecycle.TranslationBundle;
import org.olat.user.UserModule;
import org.olat.user.manager.lifecycle.LifecycleMailTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserAdminLifecycleConfigurationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableDeactivationEl;
	private TextElement numberOfInactiveDayDeactivationEl;
	private TextElement numberOfDayBeforeDeactivationMailEl;
	private TextElement copyMailBeforeDeactivationEl;
	private TextElement copyMailAfterDeactivationEl;
	private MultipleSelectionElement enableMailBeforeDeactivationEl;
	private MultipleSelectionElement enableMailAfterDeactivationEl;
	private TranslationBundle mailBeforeDeactivationBundle;
	private TranslationBundle mailAfterDeactivationBundle;
	
	private TextElement numberOfDayBeforeExpirationMailEl;
	private TextElement copyMailBeforeExpirationEl;
	private TextElement copyMailAfterExpirationEl;
	private MultipleSelectionElement enableMailBeforeExpirationEl;
	private MultipleSelectionElement enableMailAfterExpirationEl;
	private TranslationBundle mailBeforeExpirationBundle;
	private TranslationBundle mailAfterExpirationBundle;
	
	private MultipleSelectionElement enableDeletionEl;
	private TextElement numberOfInactiveDayDeletionEl;
	private TextElement numberOfDayBeforeDeletionMailEl;
	private TextElement copyMailBeforeDeletionEl;
	private TextElement copyMailAfterDeletionEl;
	private MultipleSelectionElement enableMailBeforeDeletionEl;
	private MultipleSelectionElement enableMailAfterDeletionEl;
	private TranslationBundle mailBeforeDeletionBundle;
	private TranslationBundle mailAfterDeletionBundle;
	
	private int counter = 0;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private UserModule userModule;
	
	public UserAdminLifecycleConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.configuration.title");
		
		initExpirationForm(formLayout);
		uifactory.addSpacerElement("del-exp", formLayout, false);
		initDeactivationForm(formLayout);
		uifactory.addSpacerElement("del-deac", formLayout, false);
		initDeletionForm(formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
	}
	

	protected void initExpirationForm(FormItemContainer formLayout) {
		String[] onValues = new String[] { translate("enabled") };
		enableMailBeforeExpirationEl = uifactory.addCheckboxesHorizontal("enable.mail.before.expiration", "enable.mail.before.expiration", formLayout, onKeys, onValues);
		enableMailBeforeExpirationEl.addActionListener(FormEvent.ONCHANGE);
		enableMailBeforeExpirationEl.select(onKeys[0], userModule.isMailBeforeExpiration());
		
		// day before expiration
		String daysBefore = Integer.toString(userModule.getNumberOfDayBeforeExpirationMail());
		numberOfDayBeforeExpirationMailEl = uifactory.addTextElement("num.day.before.mail.expiration", "num.day.before.mail.expiration", 4, daysBefore, formLayout);
		initDays(numberOfDayBeforeExpirationMailEl);
		
		// Copy mail before expiration 
		copyMailBeforeExpirationEl = uifactory.addTextElement("copy.mail.before.expiration", -1, StringUtils.join(userModule.getMailCopyBeforeExpiration(), ", "), formLayout);
		copyMailBeforeExpirationEl.setHelpTextKey("copy.mail.help", null);

		String[] bodyParams = new String[] { daysBefore };
		// subject + content mail
		mailBeforeExpirationBundle = initForm("mail.before.expiration.body.label",
				"mail.before.expiration.subject", null, "mail.before.expiration.body", bodyParams, formLayout);
		
		// enable mail after
		enableMailAfterExpirationEl = uifactory.addCheckboxesHorizontal("enable.mail.after.expiration", "enable.mail.after.expiration", formLayout, onKeys, onValues);
		enableMailAfterExpirationEl.addActionListener(FormEvent.ONCHANGE);
		enableMailAfterExpirationEl.select(onKeys[0], userModule.isMailAfterExpiration());
		
		// Copy mail after expiration 
		copyMailAfterExpirationEl = uifactory.addTextElement("copy.mail.after.expiration", -1, StringUtils.join(userModule.getMailCopyAfterExpiration(), ", "), formLayout);
		copyMailAfterExpirationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailAfterExpirationBundle = initForm("mail.after.expiration.body.label",
				"mail.after.expiration.subject", null, "mail.after.expiration.body", bodyParams, formLayout);
	}

	protected void initDeactivationForm(FormItemContainer formLayout) {
		String[] onValues = new String[] { translate("enabled") };
		enableDeactivationEl = uifactory.addCheckboxesHorizontal("enable.deactivation", "enable.deactivation", formLayout, onKeys, onValues);
		enableDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		enableDeactivationEl.select(onKeys[0], userModule.isUserAutomaticDeactivation());
		
		// day inactivity
		String daysBefore = Integer.toString(userModule.getNumberOfInactiveDayBeforeDeactivation());
		numberOfInactiveDayDeactivationEl = uifactory.addTextElement("num.inactive.day.deactivation", "num.inactive.day.deactivation", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDayDeactivationEl);

		// enable mail before
		enableMailBeforeDeactivationEl = uifactory.addCheckboxesHorizontal("enable.mail.before.deactivation", "enable.mail.before.deactivation", formLayout, onKeys, onValues);
		enableMailBeforeDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		enableMailBeforeDeactivationEl.select(onKeys[0], userModule.isMailBeforeDeactivation());
		
		// day before
		String daysBeforeMail = Integer.toString(userModule.getNumberOfDayBeforeDeactivationMail());
		numberOfDayBeforeDeactivationMailEl = uifactory.addTextElement("num.day.before.mail.deactivation", "num.day.before.mail.deactivation", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeDeactivationMailEl);
		
		// Copy mail before deactivation 
		copyMailBeforeDeactivationEl = uifactory.addTextElement("copy.mail.before.deactivation", -1, StringUtils.join(userModule.getMailCopyBeforeDeactivation(), ", "), formLayout);
		copyMailBeforeDeactivationEl.setHelpTextKey("copy.mail.help", null);

		String[] bodyParams = new String[] { daysBeforeMail };
		// subject + content mail
		mailBeforeDeactivationBundle = initForm("mail.before.deactivation.body.label",
				"mail.before.deactivation.subject", null, "mail.before.deactivation.body", bodyParams, formLayout);

		// enable mail after
		enableMailAfterDeactivationEl = uifactory.addCheckboxesHorizontal("enable.mail.after.deactivation", "enable.mail.after.deactivation", formLayout, onKeys, onValues);
		enableMailAfterDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		enableMailAfterDeactivationEl.select(onKeys[0], userModule.isMailAfterDeactivation());
		
		// Copy mail after deactivation 
		copyMailAfterDeactivationEl = uifactory.addTextElement("copy.mail.after.deactivation", -1, StringUtils.join(userModule.getMailCopyAfterDeactivation(), ", "), formLayout);
		copyMailAfterDeactivationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailAfterDeactivationBundle = initForm("mail.after.deactivation.body.label",
				"mail.after.deactivation.subject", null, "mail.after.deactivation.body", bodyParams, formLayout);
	}

	protected void initDeletionForm(FormItemContainer formLayout) {
		String[] onValues = new String[] { translate("enabled") };
		enableDeletionEl = uifactory.addCheckboxesHorizontal("enable.deletion", "enable.deletion", formLayout, onKeys, onValues);
		enableDeletionEl.addActionListener(FormEvent.ONCHANGE);
		enableDeletionEl.select(onKeys[0], userModule.isUserAutomaticDeletion());
		
		String daysBefore = Integer.toString(userModule.getNumberOfInactiveDayBeforeDeletion());
		numberOfInactiveDayDeletionEl = uifactory.addTextElement("num.inactive.day.deletion", "num.inactive.day.deletion", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDayDeletionEl);
		
		// enable mail before
		enableMailBeforeDeletionEl = uifactory.addCheckboxesHorizontal("enable.mail.before.deletion", "enable.mail.before.deletion", formLayout, onKeys, onValues);
		enableMailBeforeDeletionEl.addActionListener(FormEvent.ONCHANGE);
		enableMailBeforeDeletionEl.select(onKeys[0], userModule.isMailBeforeDeletion());
		
		// day before
		String daysBeforeMail = Integer.toString(userModule.getNumberOfDayBeforeDeletionMail());
		numberOfDayBeforeDeletionMailEl = uifactory.addTextElement("num.day.before.mail.deletion", "num.day.before.mail.deletion", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeDeletionMailEl);
		
		// Copy mail before deletion 
		copyMailBeforeDeletionEl = uifactory.addTextElement("copy.mail.before.deletion", -1, StringUtils.join(userModule.getMailCopyBeforeDeletion(), ", "), formLayout);
		copyMailBeforeDeletionEl.setHelpTextKey("copy.mail.help", null);

		String[] bodyParams = new String[] { daysBeforeMail };
		// subject + content mail
		mailBeforeDeletionBundle = initForm("mail.before.deletion.body.label",
				"mail.before.deletion.subject", null, "mail.before.deletion.body", bodyParams, formLayout);
		
		// enable mail after
		enableMailAfterDeletionEl = uifactory.addCheckboxesHorizontal("enable.mail.after.deletion", "enable.mail.after.deletion", formLayout, onKeys, onValues);
		enableMailAfterDeletionEl.addActionListener(FormEvent.ONCHANGE);
		enableMailAfterDeletionEl.select(onKeys[0], userModule.isMailAfterDeletion());
		
		// Copy mail after deletion 
		copyMailAfterDeletionEl = uifactory.addTextElement("copy.mail.after.deletion", -1, StringUtils.join(userModule.getMailCopyAfterDeletion(), ", "), formLayout);
		copyMailAfterDeletionEl.setHelpTextKey("copy.mail.help", null);
		
		// subject + content mail
		mailAfterDeletionBundle = initForm("mail.after.deletion.body.label",
				"mail.after.deletion.subject", null, "mail.after.deletion.body", bodyParams, formLayout);
		
	}

	private TranslationBundle initForm(String labelI18nKey, String subjectI18nKey, String[] subjectParams,
									   String bodyI18nKey, String[] bodyParams, FormItemContainer formLayout) {
		StaticTextElement viewEl = uifactory.addStaticTextElement("view." + counter++, labelI18nKey, "", formLayout);
		viewEl.setElementCssClass("o_omit_margin");
		FormLink translationLink = uifactory.addFormLink("translate." + counter++, "translation.edit", null, formLayout, Link.BUTTON);
		TranslationBundle bundle = new TranslationBundle(labelI18nKey, subjectI18nKey, subjectParams, bodyI18nKey, bodyParams, viewEl, translationLink);
		translationLink.setUserObject(bundle);
		bundle.update(getTranslator());
		return bundle;
	}
	
	private void initDays(TextElement textEl) {
		textEl.setDisplaySize(6);
		textEl.setMaxLength(6);
		textEl.setElementCssClass("form-inline");
		textEl.setTextAddOn("days");
	}
	
	private void updateUI() {
		// expiration
		boolean enableMailBeforeExpiration = enableMailBeforeExpirationEl.isAtLeastSelected(1);
		numberOfDayBeforeExpirationMailEl.setVisible(enableMailBeforeExpiration);
		copyMailBeforeExpirationEl.setVisible(enableMailBeforeExpiration);
		mailBeforeExpirationBundle.setVisible(enableMailBeforeExpiration);
		boolean enableMailAfterExpiration = enableMailAfterExpirationEl.isAtLeastSelected(1);
		copyMailAfterExpirationEl.setVisible(enableMailAfterExpiration);
		mailAfterExpirationBundle.setVisible(enableMailAfterExpiration);

		// deactivation
		boolean enableDeactivation = enableDeactivationEl.isAtLeastSelected(1);
		enableMailBeforeDeactivationEl.setVisible(enableDeactivation);
		boolean enableMailBeforeDeactiviation = enableDeactivation && enableMailBeforeDeactivationEl.isAtLeastSelected(1);
		numberOfDayBeforeDeactivationMailEl.setVisible(enableMailBeforeDeactiviation);
		copyMailBeforeDeactivationEl.setVisible(enableMailBeforeDeactiviation);
		mailBeforeDeactivationBundle.setVisible(enableMailBeforeDeactiviation);
		enableMailAfterDeactivationEl.setVisible(enableDeactivation);
		boolean enableMailAfterDeactiviation = enableDeactivation && enableMailAfterDeactivationEl.isAtLeastSelected(1);
		copyMailAfterDeactivationEl.setVisible(enableMailAfterDeactiviation);
		mailAfterDeactivationBundle.setVisible(enableMailAfterDeactiviation);
		
		// deletion
		boolean enableDeletion = enableDeletionEl.isAtLeastSelected(1);
		enableMailBeforeDeletionEl.setVisible(enableDeletion);
		boolean enableMailBeforeDeletion = enableDeletion && enableMailBeforeDeletionEl.isAtLeastSelected(1);
		numberOfDayBeforeDeletionMailEl.setVisible(enableMailBeforeDeletion);
		copyMailBeforeDeletionEl.setVisible(enableMailBeforeDeletion);
		mailBeforeDeletionBundle.setVisible(enableMailBeforeDeletion);
		enableMailAfterDeletionEl.setVisible(enableDeletion);
		boolean enableMailAfterDeletion = enableDeletion && enableMailAfterDeletionEl.isAtLeastSelected(1);
		copyMailAfterDeletionEl.setVisible(enableMailAfterDeletion);
		mailAfterDeletionBundle.setVisible(enableMailAfterDeletion);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(translatorCtrl == source) {
			((TranslationBundle)translatorCtrl.getUserObject()).update(getTranslator());
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		translatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableDeactivationEl == source || enableDeletionEl == source
				|| enableMailBeforeDeactivationEl == source || enableMailAfterDeactivationEl == source
				|| enableMailBeforeDeletionEl == source || enableMailAfterDeletionEl == source
				|| enableMailBeforeExpirationEl == source || enableMailAfterExpirationEl == source) {
			updateUI();
		} else if(source instanceof FormLink) {
			if(source.getUserObject() instanceof TranslationBundle translationBundle) {
				doTranslate(ureq, translationBundle);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateInteger(numberOfDayBeforeExpirationMailEl);
		allOk &= validateInteger(numberOfInactiveDayDeactivationEl);
		allOk &= validateInteger(numberOfDayBeforeDeactivationMailEl);
		allOk &= validateInteger(numberOfInactiveDayDeletionEl);
		allOk &= validateInteger(numberOfDayBeforeDeletionMailEl);
		allOk &= validateEmail(copyMailAfterDeactivationEl);
		allOk &= validateEmail(copyMailBeforeDeactivationEl);
		allOk &= validateEmail(copyMailAfterDeletionEl);
		allOk &= validateEmail(copyMailBeforeDeletionEl);
		allOk &= validateEmail(copyMailAfterExpirationEl);
		allOk &= validateEmail(copyMailBeforeExpirationEl);
		return allOk;
	}
	
	private boolean validateInteger(TextElement element) {
		boolean allOk = true;
		
		element.clearError();
		if(element.isVisible()) {
			if(StringHelper.containsNonWhitespace(element.getValue())) {
				try {
					int value = Integer.parseInt(element.getValue());
					if(value < 1) {
						element.setErrorKey("form.error.nointeger");
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					element.setErrorKey("form.error.nointeger");
					allOk &= false;
				}
			} else {
				element.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateEmail(TextElement element) {
		boolean allOk = true;
		
		element.clearError();
		if (element.isVisible() && formatAndCheckMails(element.getValue()) == null) {
			allOk &= false;
			element.setErrorKey("form.error.nomail");
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// expiration
		boolean enableMailBeforeExpiration = enableMailBeforeExpirationEl.isAtLeastSelected(1);
		userModule.setMailBeforeExpiration(enableMailBeforeExpiration);
		if(enableMailBeforeExpiration) {
			int daysBeforeExpiration = Integer.parseInt(numberOfDayBeforeExpirationMailEl.getValue());
			userModule.setNumberOfDayBeforeExpirationMail(daysBeforeExpiration);
		}
		userModule.setMailAfterExpiration(enableMailAfterExpirationEl.isAtLeastSelected(1)); 
		userModule.setMailCopyAfterExpiration(formatAndCheckMails(copyMailAfterExpirationEl.getValue()));
		userModule.setMailCopyBeforeExpiration(formatAndCheckMails(copyMailBeforeExpirationEl.getValue()));
		
		// deactivation
		boolean automaticDeactivation = enableDeactivationEl.isAtLeastSelected(1);
		userModule.setUserAutomaticDeactivation(automaticDeactivation);
		int daysBeforeInactivation = Integer.parseInt(numberOfInactiveDayDeactivationEl.getValue());
		userModule.setNumberOfInactiveDayBeforeDeactivation(daysBeforeInactivation);
		if(automaticDeactivation) {
			userModule.setMailBeforeDeactivation(enableMailBeforeDeactivationEl.isAtLeastSelected(1));
			if(enableMailBeforeDeactivationEl.isAtLeastSelected(1)) {
				int daysBeforeMail = Integer.parseInt(numberOfDayBeforeDeactivationMailEl.getValue());
				userModule.setNumberOfDayBeforeDeactivationMail(daysBeforeMail);
			}
			userModule.setMailAfterDeactivation(enableMailAfterDeactivationEl.isAtLeastSelected(1));
		}
		userModule.setMailCopyAfterDeactivation(formatAndCheckMails(copyMailAfterDeactivationEl.getValue()));
		userModule.setMailCopyBeforeDeactivation(formatAndCheckMails(copyMailBeforeDeactivationEl.getValue()));
		
		// deletion
		boolean automaticDeletion = enableDeletionEl.isAtLeastSelected(1);
		userModule.setUserAutomaticDeletion(automaticDeletion);
		int daysBeforeDeletion = Integer.parseInt(numberOfInactiveDayDeletionEl.getValue());
		userModule.setNumberOfInactiveDayBeforeDeletion(daysBeforeDeletion);
		if(automaticDeletion) {
			userModule.setMailBeforeDeletion(enableMailBeforeDeletionEl.isAtLeastSelected(1));
			if(enableMailBeforeDeletionEl.isAtLeastSelected(1)) {
				int daysBeforeMail = Integer.parseInt(numberOfDayBeforeDeletionMailEl.getValue());
				userModule.setNumberOfDayBeforeDeletionMail(daysBeforeMail);
			}
			userModule.setMailAfterDeletion(enableMailAfterDeletionEl.isAtLeastSelected(1));
		}
		userModule.setMailCopyAfterDeletion(formatAndCheckMails(copyMailAfterDeletionEl.getValue()));
		userModule.setMailCopyBeforeDeletion(formatAndCheckMails(copyMailBeforeDeletionEl.getValue()));
	}
	
	/**
	 * Checks a comma separated list of mails for validity
	 * Returns null if there is an error
	 * 
	 * @param mails
	 * @return
	 */
	private String formatAndCheckMails(String mailString) {
		if (!StringHelper.containsNonWhitespace(mailString)) {
			return "";
		}
		
		// Remove any whitespaces
		mailString = mailString.replace(" ", "");
		
		// Convert to list for easier handling
		List<String> mails = Arrays.asList(mailString.split(","));
		
		for (String mail : mails) {
			if (!MailHelper.isValidEmailAddress(mail)) {
				return null;
			}
		}
		
		return mailString;
	}

	private void doTranslate(UserRequest ureq, org.olat.group.ui.lifecycle.TranslationBundle bundle) {
		if(guardModalController(translatorCtrl)) return;

		String description = MailHelper.getVariableNamesHelp(LifecycleMailTemplate.variableNames(), getLocale());
		SingleKeyTranslatorController.SingleKey subjectKey = new SingleKeyTranslatorController.SingleKey(bundle.getSubjectI18nKey(), SingleKeyTranslatorController.InputType.TEXT_ELEMENT);
		SingleKeyTranslatorController.SingleKey bodyKey = new SingleKeyTranslatorController.SingleKey(bundle.getBodyI18nKey(), SingleKeyTranslatorController.InputType.TEXT_AREA);
		List<SingleKeyTranslatorController.SingleKey> keys = List.of(subjectKey, bodyKey);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys,
				UserAdminLifecycleConfigurationController.class, description);
		translatorCtrl.setUserObject(bundle);
		listenTo(translatorCtrl);

		String title = translate("translate.title", translate(bundle.getLabelI18nKey()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
