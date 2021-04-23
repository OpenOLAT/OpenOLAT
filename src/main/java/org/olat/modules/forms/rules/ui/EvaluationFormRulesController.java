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
package org.olat.modules.forms.rules.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Action;
import org.olat.modules.forms.model.xml.ChoiceSelectedCondition;
import org.olat.modules.forms.model.xml.Condition;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rule;
import org.olat.modules.forms.model.xml.VisibilityAction;
import org.olat.modules.forms.rules.ActionHandler;
import org.olat.modules.forms.rules.ConditionHandler;
import org.olat.modules.forms.rules.EvaluationFormRuleHandlerProvider;
import org.olat.modules.forms.rules.RuleHandlerProvider;

/**
 * 
 * Initial date: 8 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormRulesController extends FormBasicController {

	private static final String CMD_DELETE = "rule.delete";
	
	private FormLayoutContainer rulesCont;
	private FormLink addRuleLink;
	private final List<RuleElement> ruleEls = new ArrayList<>();
	private FormSubmit saveButton;
	
	private final Form form;
	private final RuleHandlerProvider ruleHandlerProvider;
	private int counter = 0;
	private EmptyState emptyState;
	private EmptyState noRulesPossible;


	public EvaluationFormRulesController(UserRequest ureq, WindowControl wControl, Form form) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.form = form;
		ruleHandlerProvider = new EvaluationFormRuleHandlerProvider();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String rulePage = velocity_root + "/edit_rules.html";
		rulesCont = FormLayoutContainer.createCustomFormLayout("rules", getTranslator(), rulePage);
		rulesCont.setRootForm(mainForm);
		formLayout.add(rulesCont);
		rulesCont.contextPut("rules", ruleEls);
		
		emptyState = EmptyStateFactory.create("empty.state", rulesCont.getFormItemComponent(), this);
		emptyState.setIconCss("o_icon_branch");
		emptyState.setMessageI18nKey("empty.state.message");
		emptyState.setHintI18nKey("empty.state.hint");
		emptyState.setButtonI18nKey("rule.add");
			
		noRulesPossible = EmptyStateFactory.create("no.rules.possible", rulesCont.getFormItemComponent(), this);
		noRulesPossible.setIconCss("o_icon_branch");
		noRulesPossible.setIndicatorIconCss("o_icon_warn");
		noRulesPossible.setMessageI18nKey("no.rules.possible.message");
		noRulesPossible.setHintI18nKey("no.rules.possible.hint");
		
		addRuleLink = uifactory.addFormLink("rule.add", rulesCont, Link.BUTTON);
		addRuleLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		//rules
		for (Rule rule : form.getRules()) {
			Condition condition = rule.getCondition();
			ConditionHandler conditionHandler = getConditionHandler(condition);
			Action action = rule.getAction();
			ActionHandler actionHandler = getActionHandler(action);
			RuleElement ruleEl = initRuleForm(ureq, rule, conditionHandler, condition, actionHandler, action);
			ruleEls.add(ruleEl);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		saveButton = uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateRulesPossibleUI(false);
	}
	
	private ConditionHandler getConditionHandler(Condition condition) {
		for (ConditionHandler conditionHandler : ruleHandlerProvider.getConditionHandlers()) {
			if (conditionHandler.getConditionType().equals(condition.getType())) {
				return conditionHandler;
			}
		}
		return null;
	}
	
	private ActionHandler getActionHandler(Action action) {
		for (ActionHandler actionHandler : ruleHandlerProvider.getActionHandlers()) {
			if (actionHandler.getActionType().equals(action.getType())) {
				return actionHandler;
			}
		}
		return null;
	}
	
	protected RuleElement initRuleForm(UserRequest ureq, Rule rule, ConditionHandler conditionHandler, Condition condition,
			ActionHandler actionHandler, Action action) {
		//Condition
		KeyValues conditionTypeKV = new KeyValues();
		ruleHandlerProvider.getConditionHandlers().stream()
				.forEach(handler -> conditionTypeKV.add(entry(handler.getConditionType(), translate(handler.getI18nKey()))));
		conditionTypeKV.sort(KeyValues.VALUE_ASC);
		
		// This element is not in the velocity template, because it has only one entry.
		// When you add a second ConditionHandler (see CourseReminderEditController):
		// - add the element to the template and adjust the bootstrap classes
		// - implement the logic in formInnerEvent
		// - do the same for actions
		SingleSelection conditionTypeEl = uifactory.addDropdownSingleselect("condition.type." + counter++, null, rulesCont,
				conditionTypeKV.keys(), conditionTypeKV.values(), null);
		conditionTypeEl.addActionListener(FormEvent.ONCHANGE);
		String conditionType = conditionHandler.getConditionType();
		for(String conditionTypeKey : conditionTypeEl.getKeys()) {
			if(conditionType.equals(conditionTypeKey)) {
				conditionTypeEl.select(conditionTypeKey, true);
			}
		}
		
		ConditionEditorFragment conditionEditor = conditionHandler.getEditorFragment(uifactory, condition, form);
		FormItem conditionItem = conditionEditor.initForm(rulesCont, this, ureq);
		
		// Action
		KeyValues actionTypeKV = new KeyValues();
		ruleHandlerProvider.getActionHandlers().stream()
				.forEach(handler -> actionTypeKV.add(entry(handler.getActionType(), translate(handler.getI18nKey()))));
		actionTypeKV.sort(KeyValues.VALUE_ASC);
		
		SingleSelection actionTypeEl = uifactory.addDropdownSingleselect("action.type." + counter++, null, rulesCont,
				actionTypeKV.keys(), actionTypeKV.values(), null);
		actionTypeEl.addActionListener(FormEvent.ONCHANGE);
		String actionType = actionHandler.getActionType();
		for(String actionTypeKey : actionTypeEl.getKeys()) {
			if(actionType.equals(actionTypeKey)) {
				actionTypeEl.select(actionTypeKey, true);
			}
		}
		ActionEditorFragment actionEditor = actionHandler.getEditorFragment(uifactory, action, form);
		FormItem actionItem = actionEditor.initForm(rulesCont, this, ureq);
		
		// Delete
		FormLink deleteRuleButton = uifactory.addFormLink("delete.rule." + counter, CMD_DELETE, "rule.delete", null, rulesCont, Link.BUTTON);
		deleteRuleButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		
		RuleElement ruleEl = new RuleElement(deleteRuleButton, rule, conditionTypeEl, conditionEditor, conditionItem,
				actionTypeEl, actionEditor, actionItem);

		conditionTypeEl.setUserObject(ruleEl);
		deleteRuleButton.setUserObject(ruleEl);
		return ruleEl;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == emptyState && event == EmptyState.EVENT) {
			doAddRule(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addRuleLink) {
			doAddRule(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(CMD_DELETE.equals(cmd)) {
				doDeleteRule((RuleElement)link.getUserObject());
			}
		} else {
			ruleEls.forEach(re -> re.getConditionEditor().formInnerEvent(ureq, source, event));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for (RuleElement ruleEl:ruleEls) {
			allOk &= ruleEl.getConditionEditor().validateFormLogic(ureq);
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<Rule> rules = ruleEls.stream()
				.map(this::toRule)
				.collect(Collectors.toList());
		form.setRules(rules);
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	private Rule toRule(RuleElement ruleElement) {
		Rule rule = ruleElement.getRule();
		if (rule == null) {
			rule = new Rule();
			rule.setId(UUID.randomUUID().toString());
		}
		rule.setCondition(ruleElement.getConditionEditor().getCondition());
		rule.setAction(ruleElement.getActionEditor().getAction());
		return rule;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doAddRule(UserRequest ureq) {
		ConditionHandler conditionHandler = ruleHandlerProvider.getConditionHandlers().stream()
				.filter(handler -> ChoiceSelectedCondition.TYPE.equals(handler.getConditionType()))
				.findFirst().get();
		ActionHandler actionHandler = ruleHandlerProvider.getActionHandlers().stream()
				.filter(handler -> VisibilityAction.TYPE.equals(handler.getActionType()))
				.findFirst().get();
		RuleElement ruleEl = initRuleForm(ureq, null, conditionHandler, null, actionHandler, null);
		ruleEls.add(ruleEl);
	}

	private void doDeleteRule(RuleElement ruleElement) {
		ruleEls.remove(ruleElement);
		updateRulesPossibleUI(true);
	}
	
	private void updateRulesPossibleUI(boolean showSaveButton) {
		boolean rulesPossible = !ruleEls.isEmpty();
		if (!rulesPossible) {
			rulesPossible = conditionsAvailable() && actionsAvailable();
		}
		rulesCont.contextPut("noRulesPossible", Boolean.valueOf(!rulesPossible));
		saveButton.setVisible(showSaveButton || rulesPossible);
	}
	
	private boolean conditionsAvailable() {
		for (ConditionHandler conditionHandler : ruleHandlerProvider.getConditionHandlers()) {
			if (conditionHandler.conditionsAvailable(form)) {
				return true;
			}
		}
		return false;
	}

	private boolean actionsAvailable() {
		for (ActionHandler actionHandler : ruleHandlerProvider.getActionHandlers()) {
			if (actionHandler.actionsAvailable(form)) {
				return true;
			}
		}
		return false;
	}

	public static class RuleElement {
		
		private final FormLink deleteRuleButton;
		private final Rule rule;
		private final SingleSelection conditionTypeEl;
		private final FormItem conditionItem;
		private final ConditionEditorFragment conditionEditor;
		private final SingleSelection actionTypeEl;
		private final FormItem actionItem;
		private final ActionEditorFragment actionEditor;
		
		public RuleElement(FormLink deleteRuleButton, Rule rule, SingleSelection conditionTypeEl,
				ConditionEditorFragment conditionEditor, FormItem conditionItem, SingleSelection actionTypeEl,
				ActionEditorFragment actionEditor, FormItem actionItem) {
			this.deleteRuleButton = deleteRuleButton;
			this.rule = rule;
			this.conditionTypeEl = conditionTypeEl;
			this.conditionEditor = conditionEditor;
			this.conditionItem = conditionItem;
			this.actionTypeEl = actionTypeEl;
			this.actionEditor = actionEditor;
			this.actionItem = actionItem;
		}
		
		public FormLink getDeleteRuleButton() {
			return deleteRuleButton;
		}
		
		public String getDeleteButtonName() {
			return deleteRuleButton.getComponent().getComponentName();
		}
		
		public Rule getRule() {
			return rule;
		}

		public SingleSelection getConditionTypeEl() {
			return conditionTypeEl;
		}
		
		public String getConditionTypeComponentName() {
			return conditionTypeEl.getName();
		}
		
		public ConditionEditorFragment getConditionEditor() {
			return conditionEditor;
		}
		
		public FormItem getConditionItem() {
			return conditionItem;
		}
		
		public String getConditionItemName() {
			return conditionItem.getName();
		}
		
		public String getConditionItemErrorName() {
			return conditionItem.getName() + "_ERROR";
		}
		
		public SingleSelection getActionTypeEl() {
			return actionTypeEl;
		}
		
		public String getActionTypeComponentName() {
			return actionTypeEl.getName();
		}
		
		public ActionEditorFragment getActionEditor() {
			return actionEditor;
		}
		
		public FormItem getActionItem() {
			return actionItem;
		}
		
		public String getActionItemName() {
			return actionItem.getName();
		}
		
		public String getActionItemErrorName() {
			return actionItem.getName() + "_ERROR";
		}
	}

}
