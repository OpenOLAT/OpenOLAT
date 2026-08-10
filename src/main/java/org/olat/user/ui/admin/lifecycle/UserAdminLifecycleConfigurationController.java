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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.mail.MailHelper;
import org.olat.group.ui.lifecycle.TranslationBundle;
import org.olat.user.UserLifecycleManager;
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
	
	private static final String[] yesNoKeys = new String[] { "yes", "no" };

	private FormToggle enableDeactivationEl;
	private TextElement numberOfInactiveDayDeactivationEl;
	private TextElement numberOfDayBeforeDeactivationMailEl;
	private TextElement copyMailBeforeDeactivationEl;
	private TextElement copyMailAfterDeactivationEl;
	private SingleSelection enableMailBeforeDeactivationEl;
	private SingleSelection enableMailAfterDeactivationEl;
	private TranslationBundle mailBeforeDeactivationBundle;
	private TranslationBundle mailAfterDeactivationBundle;

	private TextElement numberOfDayBeforeExpirationMailEl;
	private TextElement copyMailBeforeExpirationEl;
	private TextElement copyMailAfterExpirationEl;
	private SingleSelection enableMailBeforeExpirationEl;
	private SingleSelection enableMailAfterExpirationEl;
	private TranslationBundle mailBeforeExpirationBundle;
	private TranslationBundle mailAfterExpirationBundle;

	private FormToggle enableDeletionEl;
	private TextElement numberOfInactiveDayDeletionEl;
	private TextElement numberOfDayBeforeDeletionMailEl;
	private TextElement copyMailBeforeDeletionEl;
	private TextElement copyMailAfterDeletionEl;
	private SingleSelection enableMailBeforeDeletionEl;
	private SingleSelection enableMailAfterDeletionEl;
	private TranslationBundle mailBeforeDeletionBundle;
	private TranslationBundle mailAfterDeletionBundle;
	
	private int counter = 0;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	
	public UserAdminLifecycleConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date nextExecution = userLifecycleManager.getNextExecutionTime();
		String time = nextExecution != null
				? Formatter.getInstance(getLocale()).formatTimeShort(nextExecution)
				: "-";
		
		FormLayoutContainer expirationCont = FormLayoutContainer.createDefaultFormLayout("expiration", getTranslator());
		expirationCont.setFormTitle(translate("legend.account.expiration"));
		expirationCont.setFormInfo(translate("legend.account.expiration.info", time));
		formLayout.add(expirationCont);
		initExpirationForm(expirationCont);
		
		FormLayoutContainer lifecycleCont = FormLayoutContainer.createDefaultFormLayout("lifecycle", getTranslator());
		lifecycleCont.setFormTitle(translate("legend.user.lifecycle"));
		lifecycleCont.setFormInfo(translate("legend.user.lifecycle.info", time));
		formLayout.add(lifecycleCont);
		initDeactivationForm(lifecycleCont);
		uifactory.addSpacerElement("del-deac", lifecycleCont, false);
		initDeletionForm(lifecycleCont);

		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonsLayout);
		uifactory.addFormSubmitButton("save", buttonsLayout);
	}
	
	private SingleSelection addYesNoRadios(String name, boolean value, FormItemContainer formLayout) {
		String[] yesNoValues = new String[] { translate("yes"), translate("no") };
		SingleSelection el = uifactory.addRadiosHorizontal(name, name, formLayout, yesNoKeys, yesNoValues);
		el.addActionListener(FormEvent.ONCHANGE);
		el.select(yesNoKeys[value ? 0 : 1], true);
		return el;
	}

	protected void initExpirationForm(FormItemContainer formLayout) {
		enableMailBeforeExpirationEl = addYesNoRadios("enable.mail.before.expiration", userModule.isMailBeforeExpiration(), formLayout);
		
		// day before expiration
		String daysBefore = Integer.toString(userModule.getNumberOfDayBeforeExpirationMail());
		numberOfDayBeforeExpirationMailEl = uifactory.addTextElement("num.day.before.mail.expiration", "num.day.before.mail.expiration", 4, daysBefore, formLayout);
		initDays(numberOfDayBeforeExpirationMailEl);
		
		// Copy mail before expiration 
		copyMailBeforeExpirationEl = uifactory.addTextElement("copy.mail.before.expiration", -1, StringUtils.join(userModule.getMailCopyBeforeExpiration(), ", "), formLayout);
		copyMailBeforeExpirationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailBeforeExpirationBundle = initForm("mail.before.expiration.body.label",
				"mail.before.expiration.subject", "mail.before.expiration.body", formLayout);
		
		// enable mail after
		enableMailAfterExpirationEl = addYesNoRadios("enable.mail.after.expiration", userModule.isMailAfterExpiration(), formLayout);
		
		// Copy mail after expiration 
		copyMailAfterExpirationEl = uifactory.addTextElement("copy.mail.after.expiration", -1, StringUtils.join(userModule.getMailCopyAfterExpiration(), ", "), formLayout);
		copyMailAfterExpirationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailAfterExpirationBundle = initForm("mail.after.expiration.body.label",
				"mail.after.expiration.subject", "mail.after.expiration.body", formLayout);
	}

	protected void initDeactivationForm(FormItemContainer formLayout) {
		enableDeactivationEl = uifactory.addToggleButton("enable.deactivation", "enable.deactivation",
				translate("on"), translate("off"), formLayout);
		enableDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		enableDeactivationEl.toggle(userModule.isUserAutomaticDeactivation());
		
		// day inactivity
		String daysBefore = Integer.toString(userModule.getNumberOfInactiveDayBeforeDeactivation());
		numberOfInactiveDayDeactivationEl = uifactory.addTextElement("num.inactive.day.deactivation", "num.inactive.day.deactivation", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDayDeactivationEl);

		// enable mail before
		enableMailBeforeDeactivationEl = addYesNoRadios("enable.mail.before.deactivation", userModule.isMailBeforeDeactivation(), formLayout);
		
		// day before
		String daysBeforeMail = Integer.toString(userModule.getNumberOfDayBeforeDeactivationMail());
		numberOfDayBeforeDeactivationMailEl = uifactory.addTextElement("num.day.before.mail.deactivation", "num.day.before.mail.deactivation", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeDeactivationMailEl);
		
		// Copy mail before deactivation 
		copyMailBeforeDeactivationEl = uifactory.addTextElement("copy.mail.before.deactivation", -1, StringUtils.join(userModule.getMailCopyBeforeDeactivation(), ", "), formLayout);
		copyMailBeforeDeactivationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailBeforeDeactivationBundle = initForm("mail.before.deactivation.body.label",
				"mail.before.deactivation.subject", "mail.before.deactivation.body", formLayout);

		// enable mail after
		enableMailAfterDeactivationEl = addYesNoRadios("enable.mail.after.deactivation", userModule.isMailAfterDeactivation(), formLayout);
		
		// Copy mail after deactivation 
		copyMailAfterDeactivationEl = uifactory.addTextElement("copy.mail.after.deactivation", -1, StringUtils.join(userModule.getMailCopyAfterDeactivation(), ", "), formLayout);
		copyMailAfterDeactivationEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailAfterDeactivationBundle = initForm("mail.after.deactivation.body.label",
				"mail.after.deactivation.subject", "mail.after.deactivation.body", formLayout);
	}

	protected void initDeletionForm(FormItemContainer formLayout) {
		enableDeletionEl = uifactory.addToggleButton("enable.deletion", "enable.deletion",
				translate("on"), translate("off"), formLayout);
		enableDeletionEl.addActionListener(FormEvent.ONCHANGE);
		enableDeletionEl.toggle(userModule.isUserAutomaticDeletion());
		
		String daysBefore = Integer.toString(userModule.getNumberOfInactiveDayBeforeDeletion());
		numberOfInactiveDayDeletionEl = uifactory.addTextElement("num.inactive.day.deletion", "num.inactive.day.deletion", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDayDeletionEl);
		
		// enable mail before
		enableMailBeforeDeletionEl = addYesNoRadios("enable.mail.before.deletion", userModule.isMailBeforeDeletion(), formLayout);
		
		// day before
		String daysBeforeMail = Integer.toString(userModule.getNumberOfDayBeforeDeletionMail());
		numberOfDayBeforeDeletionMailEl = uifactory.addTextElement("num.day.before.mail.deletion", "num.day.before.mail.deletion", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeDeletionMailEl);
		
		// Copy mail before deletion 
		copyMailBeforeDeletionEl = uifactory.addTextElement("copy.mail.before.deletion", -1, StringUtils.join(userModule.getMailCopyBeforeDeletion(), ", "), formLayout);
		copyMailBeforeDeletionEl.setHelpTextKey("copy.mail.help", null);

		// subject + content mail
		mailBeforeDeletionBundle = initForm("mail.before.deletion.body.label",
				"mail.before.deletion.subject", "mail.before.deletion.body", formLayout);
		
		// enable mail after
		enableMailAfterDeletionEl = addYesNoRadios("enable.mail.after.deletion", userModule.isMailAfterDeletion(), formLayout);
		
		// Copy mail after deletion 
		copyMailAfterDeletionEl = uifactory.addTextElement("copy.mail.after.deletion", -1, StringUtils.join(userModule.getMailCopyAfterDeletion(), ", "), formLayout);
		copyMailAfterDeletionEl.setHelpTextKey("copy.mail.help", null);
		
		// subject + content mail
		mailAfterDeletionBundle = initForm("mail.after.deletion.body.label",
				"mail.after.deletion.subject", "mail.after.deletion.body", formLayout);
		
	}

	private TranslationBundle initForm(String labelI18nKey, String subjectI18nKey, String bodyI18nKey, FormItemContainer formLayout) {
		StaticTextElement viewEl = uifactory.addStaticTextElement("view." + counter++, labelI18nKey, "", formLayout);
		viewEl.setElementCssClass("o_omit_margin");
		FormLink translationLink = uifactory.addFormLink("translate." + counter++, "translation.edit", null, formLayout, Link.BUTTON_SMALL);
		translationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		translationLink.setElementCssClass("o_button_ghost");
		TranslationBundle bundle = new TranslationBundle(labelI18nKey, subjectI18nKey, null, bodyI18nKey, null, viewEl, translationLink);
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
		boolean enableMailBeforeExpiration = enableMailBeforeExpirationEl.isSelected(0);
		numberOfDayBeforeExpirationMailEl.setVisible(enableMailBeforeExpiration);
		copyMailBeforeExpirationEl.setVisible(enableMailBeforeExpiration);
		mailBeforeExpirationBundle.setVisible(enableMailBeforeExpiration);
		boolean enableMailAfterExpiration = enableMailAfterExpirationEl.isSelected(0);
		copyMailAfterExpirationEl.setVisible(enableMailAfterExpiration);
		mailAfterExpirationBundle.setVisible(enableMailAfterExpiration);

		// deactivation
		boolean enableDeactivation = enableDeactivationEl.isOn();
		enableMailBeforeDeactivationEl.setVisible(enableDeactivation);
		boolean enableMailBeforeDeactiviation = enableDeactivation && enableMailBeforeDeactivationEl.isSelected(0);
		numberOfDayBeforeDeactivationMailEl.setVisible(enableMailBeforeDeactiviation);
		copyMailBeforeDeactivationEl.setVisible(enableMailBeforeDeactiviation);
		mailBeforeDeactivationBundle.setVisible(enableMailBeforeDeactiviation);
		enableMailAfterDeactivationEl.setVisible(enableDeactivation);
		boolean enableMailAfterDeactiviation = enableDeactivation && enableMailAfterDeactivationEl.isSelected(0);
		copyMailAfterDeactivationEl.setVisible(enableMailAfterDeactiviation);
		mailAfterDeactivationBundle.setVisible(enableMailAfterDeactiviation);

		// deletion
		boolean enableDeletion = enableDeletionEl.isOn();
		enableMailBeforeDeletionEl.setVisible(enableDeletion);
		boolean enableMailBeforeDeletion = enableDeletion && enableMailBeforeDeletionEl.isSelected(0);
		numberOfDayBeforeDeletionMailEl.setVisible(enableMailBeforeDeletion);
		copyMailBeforeDeletionEl.setVisible(enableMailBeforeDeletion);
		mailBeforeDeletionBundle.setVisible(enableMailBeforeDeletion);
		enableMailAfterDeletionEl.setVisible(enableDeletion);
		boolean enableMailAfterDeletion = enableDeletion && enableMailAfterDeletionEl.isSelected(0);
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
		boolean enableMailBeforeExpiration = enableMailBeforeExpirationEl.isSelected(0);
		userModule.setMailBeforeExpiration(enableMailBeforeExpiration);
		if(enableMailBeforeExpiration) {
			int daysBeforeExpiration = Integer.parseInt(numberOfDayBeforeExpirationMailEl.getValue());
			userModule.setNumberOfDayBeforeExpirationMail(daysBeforeExpiration);
		}
		userModule.setMailAfterExpiration(enableMailAfterExpirationEl.isSelected(0));
		userModule.setMailCopyAfterExpiration(formatAndCheckMails(copyMailAfterExpirationEl.getValue()));
		userModule.setMailCopyBeforeExpiration(formatAndCheckMails(copyMailBeforeExpirationEl.getValue()));
		
		// deactivation
		boolean automaticDeactivation = enableDeactivationEl.isOn();
		userModule.setUserAutomaticDeactivation(automaticDeactivation);
		int daysBeforeInactivation = Integer.parseInt(numberOfInactiveDayDeactivationEl.getValue());
		userModule.setNumberOfInactiveDayBeforeDeactivation(daysBeforeInactivation);
		if(automaticDeactivation) {
			userModule.setMailBeforeDeactivation(enableMailBeforeDeactivationEl.isSelected(0));
			if(enableMailBeforeDeactivationEl.isSelected(0)) {
				int daysBeforeMail = Integer.parseInt(numberOfDayBeforeDeactivationMailEl.getValue());
				userModule.setNumberOfDayBeforeDeactivationMail(daysBeforeMail);
			}
			userModule.setMailAfterDeactivation(enableMailAfterDeactivationEl.isSelected(0));
		}
		userModule.setMailCopyAfterDeactivation(formatAndCheckMails(copyMailAfterDeactivationEl.getValue()));
		userModule.setMailCopyBeforeDeactivation(formatAndCheckMails(copyMailBeforeDeactivationEl.getValue()));
		
		// deletion
		boolean automaticDeletion = enableDeletionEl.isOn();
		userModule.setUserAutomaticDeletion(automaticDeletion);
		int daysBeforeDeletion = Integer.parseInt(numberOfInactiveDayDeletionEl.getValue());
		userModule.setNumberOfInactiveDayBeforeDeletion(daysBeforeDeletion);
		if(automaticDeletion) {
			userModule.setMailBeforeDeletion(enableMailBeforeDeletionEl.isSelected(0));
			if(enableMailBeforeDeletionEl.isSelected(0)) {
				int daysBeforeMail = Integer.parseInt(numberOfDayBeforeDeletionMailEl.getValue());
				userModule.setNumberOfDayBeforeDeletionMail(daysBeforeMail);
			}
			userModule.setMailAfterDeletion(enableMailAfterDeletionEl.isSelected(0));
		}
		userModule.setMailCopyAfterDeletion(formatAndCheckMails(copyMailAfterDeletionEl.getValue()));
		userModule.setMailCopyBeforeDeletion(formatAndCheckMails(copyMailBeforeDeletionEl.getValue()));
		
		taskExecutorManager.execute(new UpdatePlannedInactivationDates());
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
	
	private static final class UpdatePlannedInactivationDates implements Runnable {

		@Override
		public void run() {
			CoreSpringFactory.getImpl(UserLifecycleManager.class)
				.updatePlannedInactivationDates();
		}
	}
}
