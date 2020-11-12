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
package org.olat.course.reminder.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.manager.CourseReminderTemplate;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.model.SendTime;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderEditController extends FormBasicController {
	
	private TextElement subjectEl;
	private RichTextElement emailEl;
	private TextElement descriptionEl;
	private FormLayoutContainer rulesCont;
	private final List<RuleElement> ruleEls = new ArrayList<>();
	
	private Reminder reminder;
	private final RepositoryEntry entry;
	private int counter = 0;
	
	@Autowired
	private ReminderService reminderManager;
	@Autowired
	private ReminderModule reminderModule;
	
	public CourseReminderEditController(UserRequest ureq, WindowControl wControl, Reminder reminder) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(ReminderAdminController.class, getLocale(), getTranslator()));
		
		this.reminder = reminder;
		this.entry = reminder.getEntry();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Course Reminders");
		formLayout.add(generalCont);

		String key = reminder.getKey() == null ? "" : reminder.getKey().toString();
		uifactory.addStaticTextElement("reminder.id", key, generalCont);
		
		String desc = reminder.getDescription();
		descriptionEl = uifactory.addTextElement("reminder.description", "reminder.description", 128, desc, generalCont);
		descriptionEl.setMandatory(true);
		descriptionEl.setElementCssClass("o_sel_course_reminder_desc");
		
		String sendTime = getSendTimeDescription();
		uifactory.addStaticTextElement("send.time.description.label", sendTime, generalCont);
		
		//rules
		String rulePage = velocity_root + "/edit_rules.html";
		rulesCont = FormLayoutContainer.createCustomFormLayout("rules", getTranslator(), rulePage);
		rulesCont.setRootForm(mainForm);
		formLayout.add(rulesCont);
		rulesCont.contextPut("rules", ruleEls);
		
		String configuration = reminder.getConfiguration();
		if(StringHelper.containsNonWhitespace(configuration)) {
			ReminderRules rules = reminderManager.toRules(configuration);
			if(rules.getRules() != null) {
				for(ReminderRule rule: rules.getRules()) {
					if(rule != null) {
						RuleSPI ruleSpy = reminderModule.getRuleSPIByType(rule.getType());
						RuleElement ruleEl = initRuleForm(ureq, ruleSpy, rule);
						ruleEls.add(ruleEl);
					}
				}
			}
		}
		
		if(ruleEls.isEmpty()) {
			doInitDefaultRule(ureq);
		}
		
		//email content
		FormLayoutContainer contentCont = FormLayoutContainer.createVerticalFormLayout("contents", getTranslator());
		contentCont.setRootForm(mainForm);
		formLayout.add(contentCont);

		//email subject
		String subject = reminder.getEmailSubject();
		subjectEl = uifactory.addTextElement("reminder.subject", "reminder.subject", 128, subject, contentCont);
		subjectEl.setMandatory(true);
		subjectEl.setElementCssClass("o_sel_course_reminder_subject");
		
		String emailContent = reminder == null ? null : reminder.getEmailBody();
		if(!StringHelper.containsNonWhitespace(emailContent)) {
			emailContent = translate("reminder.def.body");
		}
		emailEl = uifactory.addRichTextElementForStringDataMinimalistic("email.content", "email.content", emailContent, 10, 60, contentCont, getWindowControl());
		emailEl.setMandatory(true);
		MailHelper.setVariableNamesAsHelp(emailEl, CourseReminderTemplate.variableNames(), getLocale());
		
		String buttonPage = velocity_root + "/edit_rules_buttons.html";
		FormLayoutContainer buttonLayout = FormLayoutContainer.createCustomFormLayout("buttons", getTranslator(), buttonPage);
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	protected String getSendTimeDescription() {
		String interval = reminderModule.getInterval();
		String desc = translate("interval." + interval);
		String time;
		
		SendTime parsedTime = SendTime.parse(reminderModule.getDefaultSendTime());
		if(parsedTime.isValid()) {
			int hour = parsedTime.getHour();
			int minute = parsedTime.getMinute();
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			time = Formatter.getInstance(getLocale()).formatTimeShort(cal.getTime());
		} else {
			time = "ERROR";
		}
		String descText = translate("send.time.description", new String[] { desc, time} );
		String infoText = "<br /><em class='text-muted'>" + translate("send.time.info") + "</em>";
		return descText + infoText;
	}
	
	protected RuleElement initRuleForm(UserRequest ureq, RuleSPI ruleSpi, ReminderRule rule) {
		KeyValues typeKV = new KeyValues();
		reminderModule.getRuleSPIList().stream()
				.filter(spi -> spi.isEnabled(entry))
				.forEach(spi -> typeKV.add(entry(spi.getClass().getSimpleName(), translate(spi.getLabelI18nKey()))));
		typeKV.sort(KeyValues.VALUE_ASC);
		
		String id = Integer.toString(counter++);
		SingleSelection typeEl = uifactory.addDropdownSingleselect("rule.type.".concat(id), null, rulesCont, typeKV.keys(), typeKV.values(), null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = ruleSpi.getClass().getSimpleName();
		for(String typeKey : typeEl.getKeys()) {
			if(type.equals(typeKey)) {
				typeEl.select(typeKey, true);
			}
		}
		
		FormLink addRuleButton = uifactory.addFormLink("add.rule.".concat(id), "add", "add.rule", null, rulesCont, Link.BUTTON);
		addRuleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addRuleButton.setElementCssClass("o_sel_course_add_rule");
		FormLink deleteRuleButton = uifactory.addFormLink("delete.rule.".concat(id), "delete", "delete.rule", null, rulesCont, Link.BUTTON);
		deleteRuleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteRuleButton.setElementCssClass("o_sel_course_delete_rule");
		
		RuleEditorFragment editor = ruleSpi.getEditorFragment(rule, entry);
		FormItem customItem = editor.initForm(rulesCont, this, ureq);
		
		RuleElement ruleEl = new RuleElement(typeEl, addRuleButton, deleteRuleButton, editor, customItem);

		typeEl.setUserObject(ruleEl);
		addRuleButton.setUserObject(ruleEl);
		deleteRuleButton.setUserObject(ruleEl);
		return ruleEl;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		descriptionEl.clearError();
		if(!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
			descriptionEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		subjectEl.clearError();
		if(!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}		

		emailEl.clearError();
		if(!StringHelper.containsNonWhitespace(emailEl.getValue())) {
			emailEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		for(RuleElement ruleEl:ruleEls) {
			allOk &= ruleEl.getEditor().validateFormLogic(ureq);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof SingleSelection) {
			RuleElement panelToUpdate = null;
			for(RuleElement ruleEl:ruleEls) {
				if(source == ruleEl.getTypeEl()) {
					panelToUpdate = ruleEl;
				}
			}
			if(panelToUpdate != null) {
				SingleSelection typeEl = (SingleSelection)source;
				RuleSPI type = reminderModule.getRuleSPIByType(typeEl.getSelectedKey());
				doUpdateRuleForm(ureq, panelToUpdate, type);
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("add".equals(cmd)) {
				doAddRule(ureq, (RuleElement)button.getUserObject());
			} else if("delete".equals(cmd)) {
				doDeleteRule(ureq, (RuleElement)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doUpdateRuleForm(UserRequest ureq, RuleElement panelToUpdate, RuleSPI ruleSpi) {
		//remove old editor
		rulesCont.remove(panelToUpdate.getCustomItem());
		//add new one
		RuleEditorFragment editor = ruleSpi.getEditorFragment(null, entry);
		FormItem customItem = editor.initForm(rulesCont, this, ureq);
		panelToUpdate.setCustomItem(customItem, editor);
		rulesCont.setDirty(true);
	}
	
	private void doAddRule(UserRequest ureq, RuleElement ruleElement) {
		int index = ruleEls.indexOf(ruleElement) + 1;
		RuleSPI ruleSpi = reminderModule.getRuleSPIByType(DateRuleSPI.class.getSimpleName());
		RuleElement ruleEl = initRuleForm(ureq, ruleSpi, null);
		if(index >= 0 && index < ruleEls.size()) {
			ruleEls.add(index, ruleEl);
		} else {
			ruleEls.add(ruleEl);
		}
	}
	
	private void doDeleteRule(UserRequest ureq, RuleElement ruleElement) {
		rulesCont.remove(ruleElement.getAddRuleButton());
		rulesCont.remove(ruleElement.getDeleteRuleButton());
		rulesCont.remove(ruleElement.getTypeEl());
		rulesCont.remove(ruleElement.getCustomItem());
		
		if(ruleEls.remove(ruleElement)) {
			rulesCont.setDirty(true);
		}
		
		if(ruleEls.isEmpty()) {
			doInitDefaultRule(ureq);
		}
	}
	
	private void doInitDefaultRule(UserRequest ureq) {
		RuleSPI ruleSpi = reminderModule.getRuleSPIByType(DateRuleSPI.class.getSimpleName());
		RuleElement ruleEl = initRuleForm(ureq, ruleSpi, null);
		ruleEls.add(ruleEl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String description = descriptionEl.getValue();
		reminder.setDescription(description);
		
		ReminderRules rules = new ReminderRules();
		
		for(RuleElement ruleEl:ruleEls) {
			ReminderRule rule = ruleEl.getEditor().getConfiguration();
			if(rule != null) {
				rules.getRules().add(rule);
			}
		}
		
		String configuration = reminderManager.toXML(rules);
		reminder.setConfiguration(configuration);

		String emailSubject = subjectEl.getValue();
		reminder.setEmailSubject(emailSubject);

		String emailBody = emailEl.getValue();
		reminder.setEmailBody(emailBody);

		reminder = reminderManager.save(reminder);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class RuleElement {
		
		private SingleSelection typeEl;
		private FormLink addRuleButton;
		private FormLink deleteRuleButton;
		private FormItem customItem;
		private RuleEditorFragment editor;
		
		public RuleElement(SingleSelection typeEl, FormLink addRuleButton, FormLink deleteRuleButton, RuleEditorFragment editor, FormItem customItem) {
			this.typeEl = typeEl;
			this.editor = editor;
			this.addRuleButton = addRuleButton;
			this.deleteRuleButton = deleteRuleButton;
			this.customItem = customItem;
		}
		
		public SingleSelection getTypeEl() {
			return typeEl;
		}
		
		public RuleEditorFragment getEditor() {
			return editor;
		}
		
		public FormLink getAddRuleButton() {
			return addRuleButton;
		}
		
		public FormLink getDeleteRuleButton() {
			return deleteRuleButton;
		}
		
		public String getTypeComponentName() {
			return typeEl.getName();
		}
		
		public String getAddButtonName() {
			return addRuleButton.getComponent().getComponentName();
		}
		
		public String getDeleteButtonName() {
			return deleteRuleButton.getComponent().getComponentName();
		}
		
		public FormItem getCustomItem() {
			return customItem;
		}
		
		public String getCustomItemName() {
			return customItem.getName();
		}
		
		public void setCustomItem(FormItem customItem, RuleEditorFragment editor) {
			this.editor = editor;
			this.customItem = customItem;
		}
	}
}