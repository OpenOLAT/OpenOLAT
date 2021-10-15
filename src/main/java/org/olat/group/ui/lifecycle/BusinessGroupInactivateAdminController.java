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
package org.olat.group.ui.lifecycle;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.InputType;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.SingleKey;
import org.olat.core.util.mail.MailHelper;
import org.olat.group.BusinessGroupModule;
import org.olat.group.ui.BGMailHelper.BGMailTemplate;
import org.olat.group.ui.main.BusinessGroupListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupInactivateAdminController extends FormBasicController {

	private SingleSelection enableDeactivationEl;
	private TextElement numberOfInactiveDayDeactivationEl;
	private TextElement numberOfDayReactivationPeriodEl;
	private TextElement numberOfDayBeforeDeactivationMailEl;
	private TextElement copyMailBeforeDeactivationEl;
	private TextElement copyMailAfterDeactivationEl;
	private SingleSelection enableMailAfterDeactivationEl;
	private SpacerElement reactivationMailSpacer;
	
	private int counter = 0;
	private TranslationBundle mailAfterDeactivationBundle;
	private TranslationBundle mailBeforeDeactivationBundle;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupInactivateAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.inactivation.title");
		setFormDescription("admin.inactivation.description");
		
		// day inactivity
		String daysBefore = Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeDeactivation());
		numberOfInactiveDayDeactivationEl = uifactory.addTextElement("num.inactive.day.deactivation", "num.inactive.day.deactivation", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDayDeactivationEl, "num.inactive.day.deactivation.addon");

		int dayReactivationPeriod = businessGroupModule.getNumberOfDayReactivationPeriod();
		String gracePeriod = Integer.toString(dayReactivationPeriod);
		numberOfDayReactivationPeriodEl = uifactory.addTextElement("num.day.reactivation.period", "num.day.reactivation.period", 4, gracePeriod, formLayout);
		initDays(numberOfDayReactivationPeriodEl, "num.day.reactivation.period.addon");
		
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(new SelectionValue("auto-wo", translate("mode.deactivation.auto.wo.grace"), translate("mode.deactivation.auto.wo.grace.desc")));
		modeValues.add(new SelectionValue("auto-with", translate("mode.deactivation.auto.with.grace"), translate("mode.deactivation.auto.with.grace.desc")));
		modeValues.add(new SelectionValue("manual-wo", translate("mode.deactivation.manual.wo.grace"), translate("mode.deactivation.manual.wo.grace.desc")));
		modeValues.add(new SelectionValue("manual-with", translate("mode.deactivation.manual.with.grace"), translate("mode.deactivation.manual.with.grace.desc")));
		enableDeactivationEl = uifactory.addCardSingleSelectHorizontal("inactivation.mode", formLayout, modeValues.keys(), modeValues.values(), modeValues.descriptions(), null);
		enableDeactivationEl.setElementCssClass("o_radio_cards_lg");
		enableDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupInactivationEnabled();
		int dayBeforeDeactivationEmail = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		if(automaticEnabled && dayBeforeDeactivationEmail <= 0) {
			enableDeactivationEl.select("auto-wo", true);
		} else if(automaticEnabled && dayBeforeDeactivationEmail > 0) {
			enableDeactivationEl.select("auto-with", true);
		} else if(dayBeforeDeactivationEmail <= 0) {
			enableDeactivationEl.select("manual-wo", true);
		} else if(dayBeforeDeactivationEmail > 0) {
			enableDeactivationEl.select("manual-with", true);
		}
		
		initReactivationMail(formLayout);
		initMailAfter(formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());	
	}

	private void initReactivationMail(FormItemContainer formLayout) {
		reactivationMailSpacer = uifactory.addSpacerElement("reactivation-spacer", formLayout, false);
		
		// day before
		String daysBeforeMail = Integer.toString(businessGroupModule.getNumberOfDayBeforeDeactivationMail());
		numberOfDayBeforeDeactivationMailEl = uifactory.addTextElement("num.day.before.mail.deactivation", "num.day.before.mail.deactivation", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeDeactivationMailEl, "days");
		
		// subject + content mail
		mailBeforeDeactivationBundle = initForm("mail.before.deactivation.label",
				"notification.mail.before.deactivation.subject", "notification.mail.before.deactivation.body", formLayout);

		// Copy mail before deactivation 
		List<String> beforeCopyAn = businessGroupModule.getMailCopyBeforeDeactivation();
		copyMailBeforeDeactivationEl = uifactory.addTextElement("copy.mail.before.deactivation", -1, StringUtils.join(beforeCopyAn, ", "), formLayout);
		copyMailBeforeDeactivationEl.setPlaceholderKey("copy.mail.help", null);
	}
	
	private void initMailAfter(FormItemContainer formLayout) {
		uifactory.addSpacerElement("inactivation-mail-spacer", formLayout, false);

		// enable mail after inactivation
		SelectionValues yesNoValues = new SelectionValues();
		yesNoValues.add(SelectionValues.entry("true", translate("yes")));
		yesNoValues.add(SelectionValues.entry("false", translate("no")));
		enableMailAfterDeactivationEl = uifactory.addRadiosHorizontal("enable.mail.before.deactivation", "enable.mail.before.deactivation", formLayout,
				yesNoValues.keys(), yesNoValues.values());
		enableMailAfterDeactivationEl.addActionListener(FormEvent.ONCHANGE);
		enableMailAfterDeactivationEl.select(businessGroupModule.isMailAfterDeactivation() ? "true" : "false", true);
		
		mailAfterDeactivationBundle = initForm("mail.after.deactivation.label",
				"notification.mail.after.deactivation.subject", "notification.mail.after.deactivation.body", formLayout);
		
		// Copy mail before deactivation 
		List<String> afterCopyAn = businessGroupModule.getMailCopyBeforeDeactivation();
		copyMailAfterDeactivationEl = uifactory.addTextElement("copy.mail.after.deactivation", -1, StringUtils.join(afterCopyAn, ", "), formLayout);
		copyMailAfterDeactivationEl.setPlaceholderKey("copy.mail.help", null);
	}
	
	private void initDays(TextElement textEl, String addOnKey) {
		textEl.setDisplaySize(6);
		textEl.setMaxLength(6);
		textEl.setElementCssClass("form-inline");
		textEl.setTextAddOn(addOnKey);
	}
	
	private TranslationBundle initForm(String labelI18nKey, String subjectI18nKey, String bodyI18nKey, FormItemContainer formLayout) {
		StaticTextElement viewEl = uifactory.addStaticTextElement("view." + counter++, labelI18nKey, "", formLayout);
		viewEl.setElementCssClass("o_omit_margin");
		FormLink translationLink = uifactory.addFormLink("translate." + counter++, "translation.edit", null, formLayout, Link.BUTTON);
		TranslationBundle bundle = new TranslationBundle(labelI18nKey,  subjectI18nKey, bodyI18nKey, viewEl, translationLink);
		translationLink.setUserObject(bundle);
		bundle.update(getTranslator());
		return bundle;
	}

	@Override
	protected void doDispose() {
		//
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
		if(enableMailAfterDeactivationEl == source || enableDeactivationEl == source) {
			updateUI();
		} else if(source instanceof FormLink) {
			if(source.getUserObject() instanceof TranslationBundle) {
				doTranslate(ureq, (TranslationBundle)source.getUserObject());
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfInactiveDayDeactivationEl, true);
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfDayReactivationPeriodEl, true);

		String mode = enableDeactivationEl.getSelectedKey();
		boolean mailBeforeDeactivation = ("auto-with".equals(mode) || "manual-with".equals(mode));
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfDayBeforeDeactivationMailEl, mailBeforeDeactivation);
		
		allOk &= BusinessGroupLifecycleUIHelper.validateEmail(copyMailBeforeDeactivationEl, false);
		allOk &= BusinessGroupLifecycleUIHelper.validateEmail(copyMailAfterDeactivationEl, false);
		
		return allOk;
	}

	private void updateUI() {
		String selectedMode = enableDeactivationEl.getSelectedKey();
		boolean reactivationPeriodEnabled = "auto-with".equals(selectedMode) || "manual-with".equals(selectedMode);
		numberOfDayBeforeDeactivationMailEl.setVisible(reactivationPeriodEnabled);
		copyMailBeforeDeactivationEl.setVisible(reactivationPeriodEnabled);
		reactivationMailSpacer.setVisible(reactivationPeriodEnabled);
		mailBeforeDeactivationBundle.setVisible(reactivationPeriodEnabled);
		
		boolean mailAfterDeactivation = isSendMailAfterDeactiovation();
		copyMailAfterDeactivationEl.setVisible(mailAfterDeactivation);
		mailAfterDeactivationBundle.setVisible(mailAfterDeactivation);
	}
	
	private boolean isSendMailAfterDeactiovation() {
		return enableMailAfterDeactivationEl.isOneSelected() && "true".equals(enableMailAfterDeactivationEl.getSelectedKey());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String mode = enableDeactivationEl.getSelectedKey();
		boolean automaticEnabled = "auto-wo".equals(mode) || "auto-with".equals(mode);
		businessGroupModule.setAutomaticGroupInactivationEnabled(automaticEnabled ? "enabled" : "disabled");

		String daysBefore = numberOfInactiveDayDeactivationEl.getValue();
		businessGroupModule.setNumberOfInactiveDayBeforeDeactivation(Integer.valueOf(daysBefore));
		
		String dayReactivationPeriod = numberOfDayReactivationPeriodEl.getValue();
		businessGroupModule.setNumberOfDayReactivationPeriod(Integer.valueOf(dayReactivationPeriod));
		
		String dayBeforeMailDeactivation = numberOfDayBeforeDeactivationMailEl.getValue();
		boolean mailBeforeDeactivation = ("auto-with".equals(mode) || "manual-with".equals(mode)) && StringHelper.isLong(dayBeforeMailDeactivation);
		businessGroupModule.setMailBeforeDeactivation(mailBeforeDeactivation);
		if(mailBeforeDeactivation) {
			businessGroupModule.setNumberOfDayBeforeDeactivationMail(Integer.valueOf(dayBeforeMailDeactivation));
			businessGroupModule.setMailCopyBeforeDeactivation(copyMailAfterDeactivationEl.getValue());
		} else {
			businessGroupModule.setNumberOfDayBeforeDeactivationMail(0);
			businessGroupModule.setMailCopyBeforeDeactivation(null);
		}
		
		boolean mailAfterDeactivation = isSendMailAfterDeactiovation();
		businessGroupModule.setMailAfterDeactivation(mailAfterDeactivation);
		if(mailAfterDeactivation) {
			businessGroupModule.setMailCopyAfterDeactivation(copyMailAfterDeactivationEl.getValue());
		} else {
			businessGroupModule.setMailCopyAfterDeactivation(null);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doTranslate(UserRequest ureq, TranslationBundle bundle) {
		if(guardModalController(translatorCtrl)) return;

		String description = MailHelper.getVariableNamesHelp(BGMailTemplate.allVariableNames(), getLocale());
		SingleKey subjectKey = new SingleKey(bundle.getSubjectI18nKey(), InputType.TEXT_ELEMENT);
		SingleKey bodyKey = new SingleKey(bundle.getBodyI18nKey(), InputType.TEXT_AREA);
		List<SingleKey> keys = List.of(subjectKey, bodyKey);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys,
				BusinessGroupListController.class, description);
		translatorCtrl.setUserObject(bundle);
		listenTo(translatorCtrl);

		String title = translate("translate.title", new String[] { translate(bundle.getLabelI18nKey()) });
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
