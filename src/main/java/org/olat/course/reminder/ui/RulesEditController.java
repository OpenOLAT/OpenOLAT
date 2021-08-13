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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.reminder.CourseNodeFragment;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.model.SendTime;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RulesEditController extends StepFormBasicController {
	
	private TextElement descriptionEl;
	private SingleSelection addEl;
	private FormLayoutContainer rulesCont;
	private String[] mainTypeKeys;
	private String[] mainTypeValues;
	private String[] additionalTypeKeys;
	private String[] additionalTypeValues;
	private RuleElement mainRuleEl;
	private final List<RuleElement> additionalRuleEls = new ArrayList<>();
	
	private final Reminder reminder;
	private final CourseNodeReminderProvider reminderProvider;
	private final String warningI18nKey;
	private int counter = 0;
	
	@Autowired
	private ReminderService reminderService;
	@Autowired
	private ReminderModule reminderModule;


	public RulesEditController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, CourseNodeReminderProvider reminderProvider, String warningI18nKey) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		this.reminderProvider = reminderProvider;
		this.warningI18nKey = warningI18nKey;
		setTranslator(Util.createPackageTranslator(ReminderAdminController.class, getLocale(), getTranslator()));
		reminder = (Reminder)runContext.get(RulesEditStep.CONTEXT_KEY);
		initTypeKeyValues();
		initForm(ureq);
	}

	private void initTypeKeyValues() {
		// main
		List<RuleSPI> ruleSPIs = reminderModule.getRuleSPIList().stream()
				.filter(spi -> spi.isEnabled(reminder.getEntry()))
				.filter(this::mainSPIFilter)
				.collect(Collectors.toList());
		RuleSPI mainRuleSPI = getMainRuleSPI();
		if (mainRuleSPI != null && !ruleSPIs.contains(mainRuleSPI)) {
			ruleSPIs.add(mainRuleSPI);
		}
		ruleSPIs.sort(Comparator.comparingInt(RuleSPI::getSortValue));
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		fillKeyValues(ruleSPIs, keys, values);
		mainTypeKeys = keys.toArray(new String[0]);
		mainTypeValues = values.toArray(new String[0]);
		
		// additional 
		ruleSPIs = reminderModule.getRuleSPIList().stream()
				.filter(spi -> spi.isEnabled(reminder.getEntry()))
				.sorted(Comparator.comparingInt(RuleSPI::getSortValue))
				.collect(Collectors.toList());
		keys = new ArrayList<>();
		values = new ArrayList<>();
		fillKeyValues(ruleSPIs, keys, values);
		additionalTypeKeys = keys.toArray(new String[0]);
		additionalTypeValues = values.toArray(new String[0]);
	}

	private boolean mainSPIFilter(RuleSPI ruleSPI) {
		return reminderProvider.getMainRuleSPITypes() == null 
				|| reminderProvider.getMainRuleSPITypes().contains(ruleSPI.getClass().getSimpleName());
	}

	private RuleSPI getMainRuleSPI() {
		String configuration = reminder.getConfiguration();
		if(StringHelper.containsNonWhitespace(configuration)) {
			List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
			if(rules != null && !rules.isEmpty()) {
				ReminderRule mainRule = rules.get(0);
				return reminderModule.getRuleSPIByType(mainRule.getType());
			}
		}
		return null;
	}

	private void fillKeyValues(List<RuleSPI> ruleSPIs, List<String> keys, List<String> values) {
		int lastSeparator = ruleSPIs.get(0).getSortValue() - (ruleSPIs.get(0).getSortValue() % 100);
		for (RuleSPI ruleSPI : ruleSPIs) {
			int currentSeparator = ruleSPI.getSortValue() - (ruleSPI.getSortValue() % 100);
			if (currentSeparator > lastSeparator) {
				keys.add(SingleSelection.SEPARATOR);
				values.add(SingleSelection.SEPARATOR);
				lastSeparator = currentSeparator;
			}
			keys.add(ruleSPI.getClass().getSimpleName());
			values.add(translate(ruleSPI.getLabelI18nKey()));
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		generalCont.setFormContextHelp("Course Reminders");
		generalCont.setFormInfo(getSendTimeDescription());
		if (StringHelper.containsNonWhitespace(warningI18nKey)) {
			generalCont.setFormWarning(translate(warningI18nKey));
		}
		formLayout.add(generalCont);
		
		String desc = reminder.getDescription();
		descriptionEl = uifactory.addTextElement("reminder.description", "reminder.description", 128, desc, generalCont);
		descriptionEl.setMandatory(true);
		descriptionEl.setElementCssClass("o_sel_course_reminder_desc");
		
		//rules
		String rulePage = velocity_root + "/edit_rules.html";
		rulesCont = FormLayoutContainer.createCustomFormLayout("rules", getTranslator(), rulePage);
		rulesCont.setRootForm(mainForm);
		formLayout.add(rulesCont);
		rulesCont.contextPut("additionalRules", additionalRuleEls);
		
		String configuration = reminder.getConfiguration();
		if(StringHelper.containsNonWhitespace(configuration)) {
			List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
			if(rules != null && !rules.isEmpty()) {
				ReminderRule mainRule = rules.get(0);
				RuleSPI ruleSpy = reminderModule.getRuleSPIByType(mainRule.getType());
				mainRuleEl = initRuleForm(ureq, ruleSpy, mainRule, true);
				rulesCont.contextPut("mainRule", mainRuleEl);
				
				for (int i = 1; i < rules.size(); i++) {
					ReminderRule rule = rules.get(i);
					if(rule != null) {
						ruleSpy = reminderModule.getRuleSPIByType(rule.getType());
						RuleElement ruleEl = initRuleForm(ureq, ruleSpy, rule, false);
						additionalRuleEls.add(ruleEl);
					}
					
				}
			}
		}
		
		if(mainRuleEl == null) {
			doInitDefaultRule(ureq);
		}
		
		addEl = uifactory.addDropdownSingleselect("add.rule.type", rulesCont, additionalTypeKeys, additionalTypeValues);
		addEl.enableNoneSelection(translate("add.rule.select"));
		addEl.addActionListener(FormEvent.ONCHANGE);
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
		return "<i class='o_icon o_icon-lg o_icon_time'></i> <b>" + descText + "</b> - " + translate("send.time.info");
	}
	
	protected RuleElement initRuleForm(UserRequest ureq, RuleSPI ruleSpi, ReminderRule rule, boolean mainRule) {
		String id = Integer.toString(counter++);
		String[] typeKeys = mainRule? mainTypeKeys: additionalTypeKeys;
		String[] typeValues = mainRule? mainTypeValues: additionalTypeValues;
		SingleSelection typeEl = uifactory.addDropdownSingleselect("rule.type.".concat(id), null, rulesCont, typeKeys, typeValues, null);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String type = ruleSpi.getClass().getSimpleName();
		for(String typeKey : typeEl.getKeys()) {
			if(type.equals(typeKey)) {
				typeEl.select(typeKey, true);
			}
		}
		
		FormLink deleteRuleButton = uifactory.addFormLink("delete.rule.".concat(id), "delete", "", null, rulesCont, Link.NONTRANSLATED + Link.BUTTON);
		deleteRuleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteRuleButton.setElementCssClass("o_sel_course_delete_rule");
		
		RuleEditorFragment editor = ruleSpi.getEditorFragment(rule, reminder.getEntry());
		FormItem customItem = editor.initForm(rulesCont, this, ureq);
		limitFragmentSelection(editor, mainRule);
		
		RuleElement ruleEl = new RuleElement(typeEl, deleteRuleButton, editor, customItem);
		
		typeEl.setUserObject(ruleEl);
		deleteRuleButton.setUserObject(ruleEl);
		return ruleEl;
	}

	private void limitFragmentSelection(RuleEditorFragment editor, boolean mainRule) {
		if (editor instanceof CourseNodeFragment && StringHelper.containsNonWhitespace(reminderProvider.getCourseNodeIdent())) {
			CourseNodeFragment courseNodeFragment = (CourseNodeFragment)editor;
			if (mainRule) {
				courseNodeFragment.limitSelection(reminderProvider.getCourseNodeIdent());
			}
		}
	}
	
	private void doSelectCourseNode(RuleEditorFragment editor) {
		if (editor instanceof CourseNodeFragment && StringHelper.containsNonWhitespace(reminderProvider.getCourseNodeIdent())) {
			CourseNodeFragment courseNodeFragment = (CourseNodeFragment)editor;
			courseNodeFragment.select(reminderProvider.getCourseNodeIdent());
		}
	}
	
	private void doInitDefaultRule(UserRequest ureq) {
		List<String> mainTypeKeyList = Arrays.asList(mainTypeKeys);
		String defaultType = reminderProvider.getDefaultMainRuleSPIType(mainTypeKeyList);
		if (!StringHelper.containsNonWhitespace(defaultType)) {
			defaultType = mainTypeKeys[0];
		}
		RuleSPI ruleSpi = reminderModule.getRuleSPIByType(defaultType);
		mainRuleEl = initRuleForm(ureq, ruleSpi, null, true);
		doSelectCourseNode(mainRuleEl.getEditor());
		rulesCont.contextPut("mainRule", mainRuleEl);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addEl) {
			doAddRule(ureq);
		} else if(source instanceof SingleSelection) {
			RuleElement panelToUpdate = null;
			if (source == mainRuleEl.getTypeEl()) {
				panelToUpdate = mainRuleEl;
			} else {
				for(RuleElement ruleEl:additionalRuleEls) {
					if(source == ruleEl.getTypeEl()) {
						panelToUpdate = ruleEl;
					}
				}
			}
			if(panelToUpdate != null) {
				SingleSelection typeEl = (SingleSelection)source;
				RuleSPI type = reminderModule.getRuleSPIByType(typeEl.getSelectedKey());
				doUpdateRuleForm(ureq, panelToUpdate, type, panelToUpdate == mainRuleEl);
			}
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("delete".equals(cmd)) {
				doDeleteRule((RuleElement)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		descriptionEl.clearError();
		if(!StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
			descriptionEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		allOk &= mainRuleEl.getEditor().validateFormLogic(ureq);
		
		for(RuleElement ruleEl:additionalRuleEls) {
			allOk &= ruleEl.getEditor().validateFormLogic(ureq);
		}
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		String description = descriptionEl.getValue();
		reminder.setDescription(description);
		
		ReminderRules rules = new ReminderRules();
		rules.getRules().add(mainRuleEl.getEditor().getConfiguration());
		
		for(RuleElement ruleEl:additionalRuleEls) {
			ReminderRule rule = ruleEl.getEditor().getConfiguration();
			if(rule != null) {
				rules.getRules().add(rule);
			}
		}
		
		String configuration = reminderService.toXML(rules);
		reminder.setConfiguration(configuration);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doUpdateRuleForm(UserRequest ureq, RuleElement panelToUpdate, RuleSPI ruleSpi, boolean mainRule) {
		//remove old editor
		rulesCont.remove(panelToUpdate.getCustomItem());
		//add new one
		RuleEditorFragment editor = ruleSpi.getEditorFragment(null, reminder.getEntry());
		FormItem customItem = editor.initForm(rulesCont, this, ureq);
		limitFragmentSelection(editor, mainRule);
		panelToUpdate.setCustomItem(customItem, editor);
		rulesCont.setDirty(true);
	}
	
	private void doAddRule(UserRequest ureq) {
		RuleSPI ruleSpi = reminderModule.getRuleSPIByType(addEl.getSelectedKey());
		RuleElement ruleEl = initRuleForm(ureq, ruleSpi, null, false);
		doSelectCourseNode(ruleEl.getEditor());
		additionalRuleEls.add(ruleEl);
		addEl.select(SingleSelection.NO_SELECTION_KEY, true);
	}
	
	private void doDeleteRule(RuleElement ruleElement) {
		rulesCont.remove(ruleElement.getDeleteRuleButton());
		rulesCont.remove(ruleElement.getTypeEl());
		rulesCont.remove(ruleElement.getCustomItem());
		
		if(additionalRuleEls.remove(ruleElement)) {
			rulesCont.setDirty(true);
		}
	}
	
	public static class RuleElement {
		
		private SingleSelection typeEl;
		private FormLink deleteRuleButton;
		private FormItem customItem;
		private RuleEditorFragment editor;
		
		public RuleElement(SingleSelection typeEl, FormLink deleteRuleButton, RuleEditorFragment editor, FormItem customItem) {
			this.typeEl = typeEl;
			this.editor = editor;
			this.deleteRuleButton = deleteRuleButton;
			this.customItem = customItem;
		}
		
		public SingleSelection getTypeEl() {
			return typeEl;
		}
		
		public RuleEditorFragment getEditor() {
			return editor;
		}
		
		public FormLink getDeleteRuleButton() {
			return deleteRuleButton;
		}
		
		public String getTypeComponentName() {
			return typeEl.getName();
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
