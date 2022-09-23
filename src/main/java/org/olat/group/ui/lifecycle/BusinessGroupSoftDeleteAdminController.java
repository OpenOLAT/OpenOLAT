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
public class BusinessGroupSoftDeleteAdminController extends FormBasicController {

	private SingleSelection enableSoftDeleteEl;
	private TextElement numberOfInactiveDaySoftDeletionEl;
	private TextElement numberOfDayBeforeSoftDeleteMailEl;
	private TextElement copyMailBeforeSoftDeleteEl;
	private TextElement copyMailAfterSoftDeleteEl;
	private SingleSelection enableMailAfterSoftDeleteEl;
	private SpacerElement reactivationMailSpacer;
	
	private int counter = 0;
	private TranslationBundle mailAfterSoftDeleteBundle;
	private TranslationBundle mailBeforeSoftDeleteBundle;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl;
	
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroupSoftDeleteAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.soft.delete.title");
		setFormDescription("admin.soft.delete.description");
		
		// day inactivity
		String daysBefore = Integer.toString(businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete());
		numberOfInactiveDaySoftDeletionEl = uifactory.addTextElement("num.inactive.day.soft.deletion", "num.inactive.day.soft.deletion", 4, daysBefore, formLayout);
		initDays(numberOfInactiveDaySoftDeletionEl, "num.inactive.day.soft.deletion.addon");
		
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(new SelectionValue("auto-wo", translate("mode.soft.deletion.auto.wo.grace"), translate("mode.soft.deletion.auto.wo.grace.desc")));
		modeValues.add(new SelectionValue("auto-with", translate("mode.soft.deletion.auto.with.grace"), translate("mode.soft.deletion.auto.with.grace.desc")));
		modeValues.add(new SelectionValue("manual-wo", translate("mode.soft.deletion.manual.wo.grace"), translate("mode.soft.deletion.manual.wo.grace.desc")));
		modeValues.add(new SelectionValue("manual-with", translate("mode.soft.deletion.manual.with.grace"), translate("mode.soft.deletion.manual.with.grace.desc")));
		enableSoftDeleteEl = uifactory.addCardSingleSelectHorizontal("soft.delete.mode", formLayout, modeValues.keys(), modeValues.values(), modeValues.descriptions(), null);
		enableSoftDeleteEl.setElementCssClass("o_radio_cards_lg");
		enableSoftDeleteEl.addActionListener(FormEvent.ONCHANGE);
		
		boolean automaticEnabled = businessGroupModule.isAutomaticGroupSoftDeleteEnabled();
		int dayBeforeSoftDeleteEmail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		if(automaticEnabled && dayBeforeSoftDeleteEmail <= 0) {
			enableSoftDeleteEl.select("auto-wo", true);
		} else if(automaticEnabled && dayBeforeSoftDeleteEmail > 0) {
			enableSoftDeleteEl.select("auto-with", true);
		} else if(dayBeforeSoftDeleteEmail <= 0) {
			enableSoftDeleteEl.select("manual-wo", true);
		} else if(dayBeforeSoftDeleteEmail > 0) {
			enableSoftDeleteEl.select("manual-with", true);
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
		String daysBeforeMail = Integer.toString(businessGroupModule.getNumberOfDayBeforeSoftDeleteMail());
		numberOfDayBeforeSoftDeleteMailEl = uifactory.addTextElement("num.day.before.mail.soft.delete", "num.day.before.mail.soft.delete", 4, daysBeforeMail, formLayout);
		initDays(numberOfDayBeforeSoftDeleteMailEl, "days");
		
		// subject + content mail
		mailBeforeSoftDeleteBundle = initForm("mail.before.soft.delete.label",
				"notification.mail.before.soft.delete.subject", "notification.mail.before.soft.delete.body", formLayout);
		
		// Copy mail before deactivation 
		List<String> beforeCopyAn = businessGroupModule.getMailCopyBeforeSoftDelete();
		copyMailBeforeSoftDeleteEl = uifactory.addTextElement("copy.mail.before.soft.delete", -1, StringUtils.join(beforeCopyAn, ", "), formLayout);
		copyMailBeforeSoftDeleteEl.setPlaceholderKey("copy.mail.help", null);
	}
	
	private void initMailAfter(FormItemContainer formLayout) {
		uifactory.addSpacerElement("soft-delete-mail-spacer", formLayout, false);

		// enable mail after inactivation
		SelectionValues yesNoValues = new SelectionValues();
		yesNoValues.add(SelectionValues.entry("true", translate("yes")));
		yesNoValues.add(SelectionValues.entry("false", translate("no")));
		enableMailAfterSoftDeleteEl = uifactory.addRadiosHorizontal("enable.mail.before.soft.delete", "enable.mail.before.soft.delete", formLayout,
				yesNoValues.keys(), yesNoValues.values());
		enableMailAfterSoftDeleteEl.addActionListener(FormEvent.ONCHANGE);
		enableMailAfterSoftDeleteEl.select(businessGroupModule.isMailAfterSoftDelete() ? "true" : "false", true);

		mailAfterSoftDeleteBundle = initForm("mail.after.soft.delete.label",
				"notification.mail.after.soft.delete.subject", "notification.mail.after.soft.delete.body", formLayout);
		
		// Copy mail before deactivation 
		List<String> afterCopyAn = businessGroupModule.getMailCopyBeforeSoftDelete();
		copyMailAfterSoftDeleteEl = uifactory.addTextElement("copy.mail.after.soft.delete", -1, StringUtils.join(afterCopyAn, ", "), formLayout);
		copyMailAfterSoftDeleteEl.setPlaceholderKey("copy.mail.help", null);
	}
	
	private TranslationBundle initForm(String labelI18nKey, String subjectI18nKey, String bodyI18nKey, FormItemContainer formLayout) {
		StaticTextElement viewEl = uifactory.addStaticTextElement("view." + counter++, labelI18nKey, "", formLayout);
		viewEl.setElementCssClass("o_omit_margin");
		FormLink translationLink = uifactory.addFormLink("translate." + counter++, "translation.edit", null, formLayout, Link.BUTTON);
		TranslationBundle bundle = new TranslationBundle(labelI18nKey, subjectI18nKey, bodyI18nKey, viewEl, translationLink);
		translationLink.setUserObject(bundle);
		bundle.update(getTranslator());
		return bundle;
	}
	
	private void initDays(TextElement textEl, String addOnKey) {
		textEl.setDisplaySize(6);
		textEl.setMaxLength(6);
		textEl.setElementCssClass("form-inline");
		textEl.setTextAddOn(addOnKey);
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
		if(enableMailAfterSoftDeleteEl == source || enableSoftDeleteEl == source) {
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
		
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfInactiveDaySoftDeletionEl, true);

		String mode = enableSoftDeleteEl.getSelectedKey();
		boolean mailBeforeDeactivation = ("auto-with".equals(mode) || "manual-with".equals(mode));
		allOk &= BusinessGroupLifecycleUIHelper.validateInteger(numberOfDayBeforeSoftDeleteMailEl, mailBeforeDeactivation);
		
		allOk &= BusinessGroupLifecycleUIHelper.validateEmail(copyMailBeforeSoftDeleteEl, false);
		allOk &= BusinessGroupLifecycleUIHelper.validateEmail(copyMailAfterSoftDeleteEl, false);
		
		return allOk;
	}
	
	private void updateUI() {
		String selectedMode = enableSoftDeleteEl.getSelectedKey();
		boolean reactivationPeriodEnabled = "auto-with".equals(selectedMode) || "manual-with".equals(selectedMode);
		numberOfDayBeforeSoftDeleteMailEl.setVisible(reactivationPeriodEnabled);
		copyMailBeforeSoftDeleteEl.setVisible(reactivationPeriodEnabled);
		reactivationMailSpacer.setVisible(reactivationPeriodEnabled);
		mailBeforeSoftDeleteBundle.setVisible(reactivationPeriodEnabled);
		
		boolean mailAfterDeactivation = enableMailAfterSoftDeleteEl.isOneSelected() && "true".equals(enableMailAfterSoftDeleteEl.getSelectedKey());
		copyMailAfterSoftDeleteEl.setVisible(mailAfterDeactivation);
		mailAfterSoftDeleteBundle.setVisible(mailAfterDeactivation);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String mode = enableSoftDeleteEl.getSelectedKey();
		boolean automaticEnabled = "auto-wo".equals(mode) || "auto-with".equals(mode);
		businessGroupModule.setAutomaticGroupSoftDeleteEnabled(automaticEnabled ? "true" : "false");

		String daysBefore = numberOfInactiveDaySoftDeletionEl.getValue();
		businessGroupModule.setNumberOfInactiveDayBeforeSoftDelete(Integer.valueOf(daysBefore));
		
		String dayBeforeMailSoftDelete = numberOfDayBeforeSoftDeleteMailEl.getValue();
		boolean mailBeforeSoftDelete = ("auto-with".equals(mode) || "manual-with".equals(mode)) && StringHelper.isLong(dayBeforeMailSoftDelete);
		businessGroupModule.setMailBeforeSoftDelete(mailBeforeSoftDelete);
		if(mailBeforeSoftDelete) {
			businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(Integer.valueOf(dayBeforeMailSoftDelete));
			businessGroupModule.setMailCopyBeforeSoftDelete(copyMailAfterSoftDeleteEl.getValue());
		} else {
			businessGroupModule.setNumberOfDayBeforeSoftDeleteMail(0);
			businessGroupModule.setMailCopyBeforeSoftDelete(null);
		}
		
		boolean mailAfterSoftDelete = enableMailAfterSoftDeleteEl.isOneSelected() && enableMailAfterSoftDeleteEl.isSelected(0);
		businessGroupModule.setMailAfterSoftDelete(mailAfterSoftDelete);
		if(mailAfterSoftDelete) {
			businessGroupModule.setMailCopyAfterSoftDelete(copyMailAfterSoftDeleteEl.getValue());
		} else {
			businessGroupModule.setMailCopyAfterSoftDelete(null);
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doTranslate(UserRequest ureq, TranslationBundle bundle) {
		if(guardModalController(translatorCtrl)) return;

		String description = MailHelper.getVariableNamesHelp(BGMailTemplate.variableNames(true), getLocale());
		SingleKey subjectKey = new SingleKey(bundle.getSubjectI18nKey(), InputType.TEXT_ELEMENT);
		SingleKey bodyKey = new SingleKey(bundle.getBodyI18nKey(), InputType.TEXT_AREA);
		List<SingleKey> keys = List.of(subjectKey, bodyKey);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys,
				BusinessGroupListController.class, description);
		translatorCtrl.setUserObject(bundle);
		listenTo(translatorCtrl);

		String title = translate("translate.title", translate(bundle.getLabelI18nKey()));
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
