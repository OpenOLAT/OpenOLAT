/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.components.date.RelativeDateElement;
import org.olat.core.gui.components.date.RelativeDateSelection;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.ui.component.AutomationContextCellRenderer;
import org.olat.modules.curriculum.ui.component.AutomationTargetStatusCellRenderer;
import org.olat.repository.ExecutionPeriodRelativeDateContext;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, https://www.frentix.com
 */
public class EditCurriculumAutomationController extends FormBasicController {

	private SingleSelection dependingOnEl;
	private FormLayoutContainer executionPeriodCont;
	private RelativeDateElement relativeDateEl;
	private MultipleSelectionElement onlyWhenStatusEl;
	private MultipleSelectionElement dependingOnStatusEl;

	private final CurriculumAutomationRule rule;
	private final boolean implType;
	private final CurriculumElement element;

	public EditCurriculumAutomationController(UserRequest ureq, WindowControl wControl,
			CurriculumAutomationRule rule, boolean implementationType, CurriculumElement element) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(RepositoryEntryStatusEnum.class, getLocale(), getTranslator()));
		this.rule = rule;
		this.element = element;
		AutomationContext context = rule.getContext();
		this.implType = context == AutomationContext.IMPLEMENTATION
				|| (context == AutomationContext.CONTENT && implementationType);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer headerCont = FormLayoutContainer.createCustomFormLayout("headerCont", getTranslator(),
				velocity_root + "/automation_edit_header.html");
		formLayout.add(headerCont);

		StaticTextElement contextEl = uifactory.addStaticTextElement("automation.rule.context", "automation.rule.context",
				new AutomationContextCellRenderer(getTranslator()).render(rule.getContext()), headerCont);
		contextEl.setDomWrapperElement(DomWrapperElement.div);
		uifactory.addStaticTextElement("automation.rule.type", "automation.rule.type",
				translate("automation.type." + rule.getAutomationType().name().toLowerCase()), headerCont);
		if (rule.getTargetStatus() != null) {
			String status = new AutomationTargetStatusCellRenderer(getTranslator(), false).render(rule.getTargetStatus());
			uifactory.addStaticTextElement("automation.rule.target", "automation.rule.target.status",
					status, headerCont);
		}

		FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("configCont", getTranslator());
		configCont.setFormTitle(translate("automation.condition"));
		configCont.setRootForm(mainForm);
		formLayout.add(configCont);

		if (rule.getContext() == AutomationContext.CONTENT) {
			SelectionValues dependingOnSV = new SelectionValues();
			dependingOnSV.add(SelectionValues.entry(AutomationDependingOn.STATUS.name(),
					translate("automation.depending.on.status")));
			dependingOnSV.add(SelectionValues.entry(AutomationDependingOn.EXECUTION_PERIOD.name(),
					translate("automation.depending.on.execution_period")));
			dependingOnEl = uifactory.addButtonGroupSingleSelectHorizontal("automation.depending.on",
					configCont, dependingOnSV);
			dependingOnEl.addActionListener(FormEvent.ONCHANGE);
			if (rule.getDependingOn() != null) {
				dependingOnEl.select(rule.getDependingOn().name(), true);
			} else {
				dependingOnEl.select(AutomationDependingOn.STATUS.name(), true);
			}
		}

		SelectionValues onlyWhenStatusSV = buildOnlyWhenStatusSV();
		onlyWhenStatusEl = uifactory.addCheckboxesButtonGroup("automation.only.when.status",
				"automation.col.status.is", configCont, onlyWhenStatusSV);
		initCheckboxSelection(onlyWhenStatusEl, onlyWhenStatusSV, rule.getOnlyWhenStatus());

		executionPeriodCont = uifactory.addInlineFormLayout("executionPeriod", null, configCont);
		String relativeDateLabelKey = implType
				? "automation.relative.date.implementation"
				: "automation.relative.date.element";
		executionPeriodCont.setLabel(relativeDateLabelKey, null);
		ExecutionPeriodRelativeDateContext relativeDateContext = new ExecutionPeriodRelativeDateContext(
				getTranslator(),
				element != null ? element.getBeginDate() : null,
				element != null ? element.getEndDate() : null);
		relativeDateEl = uifactory.addRelativeDateElement("automation.relative.date", null,
				executionPeriodCont, getWindowControl(), relativeDateContext);
		relativeDateEl.setAriaLabel(translate(relativeDateLabelKey));
		if (rule.getUnit() != null) {
			boolean offsetEnabled = rule.getUnit() != AutomationUnit.SAME_DAY;
			String unitKey = offsetEnabled ? rule.getUnit().name() : null;
			String refKey = rule.getReference() != null ? rule.getReference()
					: (rule.getDirection() == OffsetDirection.BEFORE ? CurriculumAutomationRule.REFERENCE_BEGIN : CurriculumAutomationRule.REFERENCE_END);
			relativeDateEl.setValue(new RelativeDateSelection(refKey, rule.getDirection(),
					unitKey, rule.getValue(), offsetEnabled));
		}

		if (rule.getContext() == AutomationContext.CONTENT) {
			SelectionValues dependingOnStatusSV = buildDependingOnStatusSV();
			dependingOnStatusEl = uifactory.addCheckboxesButtonGroup("automation.on.status",
					"automation.on.status", configCont, dependingOnStatusSV);
			String hintKey = implType
					? "automation.on.status.hint.implementation"
					: "automation.on.status.hint.element";
			dependingOnStatusEl.setHelpTextKey(hintKey, null);
			initCheckboxSelection(dependingOnStatusEl, dependingOnStatusSV, rule.getDependingOnStatus());
		}

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, configCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());

		updateVisibility();
	}

	private void initCheckboxSelection(MultipleSelectionElement el, SelectionValues sv, Set<String> savedValues) {
		if (sv.keys().length == 1) {
			el.select(sv.keys()[0], true);
			el.setEnabled(false);
		} else if (savedValues != null) {
			for (String status : savedValues) {
				if (el.getKeys().contains(status)) {
					el.select(status, true);
				}
			}
		}
	}

	private SelectionValues buildOnlyWhenStatusSV() {
		SelectionValues sv = new SelectionValues();
		AutomationContext context = rule.getContext();
		String targetStatus = rule.getTargetStatus();

		if (context == AutomationContext.IMPLEMENTATION) {
			addStatus(sv, CurriculumElementStatus.preparation);
			addStatus(sv, CurriculumElementStatus.provisional);
			if (CurriculumElementStatus.finished.name().equals(targetStatus)) {
				addStatus(sv, CurriculumElementStatus.confirmed);
			}
		} else if (context == AutomationContext.ELEMENT) {
			addStatus(sv, CurriculumElementStatus.preparation);
			if (CurriculumElementStatus.finished.name().equals(targetStatus)) {
				addStatus(sv, CurriculumElementStatus.active);
			}
		} else {
			if (implType) {
				addStatus(sv, CurriculumElementStatus.preparation);
				addStatus(sv, CurriculumElementStatus.provisional);
				addStatus(sv, CurriculumElementStatus.confirmed);
			} else {
				addStatus(sv, CurriculumElementStatus.preparation);
				addStatus(sv, CurriculumElementStatus.active);
			}
		}
		return sv;
	}

	private SelectionValues buildDependingOnStatusSV() {
		SelectionValues sv = new SelectionValues();
		String targetStatus = rule.getTargetStatus();
		if (RepositoryEntryStatusEnum.closed.name().equals(targetStatus)) {
			addStatus(sv, CurriculumElementStatus.cancelled);
			addStatus(sv, CurriculumElementStatus.finished);
		} else {
			if (implType) {
				addStatus(sv, CurriculumElementStatus.provisional);
				addStatus(sv, CurriculumElementStatus.confirmed);
			} else {
				addStatus(sv, CurriculumElementStatus.active);
			}
		}
		return sv;
	}

	private void addStatus(SelectionValues sv, CurriculumElementStatus status) {
		sv.add(SelectionValues.entry(status.name(), CurriculumUIFactory.translateAutomationStatus(getTranslator(), status.name())));
	}

	private void updateVisibility() {
		boolean isContent = rule.getContext() == AutomationContext.CONTENT;
		if (!isContent) {
			executionPeriodCont.setVisible(true);
			onlyWhenStatusEl.setVisible(true);
		} else {
			boolean statusTrigger = dependingOnEl.isOneSelected()
					&& AutomationDependingOn.STATUS.name().equals(dependingOnEl.getSelectedKey());
			executionPeriodCont.setVisible(!statusTrigger);
			onlyWhenStatusEl.setVisible(!statusTrigger);
			dependingOnStatusEl.setVisible(statusTrigger);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (dependingOnEl == source) {
			updateVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		onlyWhenStatusEl.clearError();
		if (onlyWhenStatusEl.isVisible() && onlyWhenStatusEl.isEnabled()
				&& !onlyWhenStatusEl.isAtLeastSelected(1)) {
			onlyWhenStatusEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		if (dependingOnStatusEl != null) {
			dependingOnStatusEl.clearError();
			if (dependingOnStatusEl.isVisible() && dependingOnStatusEl.isEnabled()
					&& !dependingOnStatusEl.isAtLeastSelected(1)) {
				dependingOnStatusEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}

		if (executionPeriodCont.isVisible()) {
			relativeDateEl.clearError();
			if (relativeDateEl.getValue() == null) {
				relativeDateEl.setErrorKey("form.legende.mandatory");
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (dependingOnEl != null) {
			rule.setDependingOn(AutomationDependingOn.valueOf(dependingOnEl.getSelectedKey()));
		}

		if (executionPeriodCont.isVisible()) {
			RelativeDateSelection sel = relativeDateEl.getValue();
			if (sel != null) {
				rule.setReference(sel.getRefKey());
				rule.setValue(sel.getValue());
				rule.setDirection(sel.getDirection());
				if (sel.getUnitKey() != null) {
					rule.setUnit(AutomationUnit.valueOf(sel.getUnitKey()));
				} else {
					rule.setUnit(AutomationUnit.SAME_DAY);
				}
			}
		}

		if (dependingOnStatusEl != null && dependingOnStatusEl.isVisible()) {
			rule.setDependingOnStatus(new HashSet<>(dependingOnStatusEl.getSelectedKeys()));
		} else {
			rule.setDependingOnStatus(null);
		}

		if (onlyWhenStatusEl.isVisible()) {
			rule.setOnlyWhenStatus(new HashSet<>(onlyWhenStatusEl.getSelectedKeys()));
		} else {
			rule.setOnlyWhenStatus(null);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
