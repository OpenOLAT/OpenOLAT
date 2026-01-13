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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Automation;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.AutomationImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementAutomationController extends FormBasicController {
	
	private FormToggle instantiationEnabledEl;
	private TextElement instantiationValueEl;
	private SingleSelection instantiationUnitEl;
	private FormLayoutContainer instantiationContainer;

	private FormToggle accessForCoachEnabledEl;
	private TextElement accessForCoachValueEl;
	private SingleSelection accessForCoachUnitEl;
	private FormLayoutContainer accessForCoachContainer;
	
	private FormToggle publishedEnabledEl;
	private TextElement publishedValueEl;
	private SingleSelection publishedUnitEl;
	private FormLayoutContainer publishedContainer;
	
	private FormToggle finishedEnabledEl;
	private TextElement finishedValueEl;
	private SingleSelection finishedUnitEl;
	private FormLayoutContainer finishedContainer;

	private final boolean canEdit;
	private CurriculumElement curriculumElement;
	private final SelectionValues unitPK = new SelectionValues();
	
	@Autowired
	private CurriculumService curriculumService;
	
	public EditCurriculumElementAutomationController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.curriculumElement = curriculumElement;

		canEdit = secCallback.canEditCurriculumElementSettings(curriculumElement);
		
		unitPK.add(SelectionValues.entry(AutomationUnit.SAME_DAY.name(), translate("same.day")));
		unitPK.add(SelectionValues.entry(AutomationUnit.DAYS.name(), translate("unit.days")));
		unitPK.add(SelectionValues.entry(AutomationUnit.WEEKS.name(), translate("unit.weeks")));
		unitPK.add(SelectionValues.entry(AutomationUnit.MONTHS.name(), translate("unit.months")));
		unitPK.add(SelectionValues.entry(AutomationUnit.YEARS.name(), translate("unit.years")));
		
		initForm(ureq);
		updateUI();
		updateValuesUI();
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer courseTemplateCont = uifactory.addDefaultFormLayout("courseTemplate", null, formLayout);
		initCourseTemplateForm(courseTemplateCont);
		
		FormLayoutContainer courseAccessCont = uifactory.addDefaultFormLayout("courseAccess", null, formLayout);
		initCourseAccessForm(courseAccessCont);
		
		FormLayoutContainer lastCont = uifactory.addDefaultFormLayout("buttons", null, formLayout);
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, lastCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initCourseTemplateForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("automation.course.templates"));
		formLayout.setFormInfo(translate("automation.course.templates.infos"));
		formLayout.setFormInfoHelp("manual_user/area_modules/Course_Planner_Implementations/#tab_settings");
		
		instantiationEnabledEl = uifactory.addToggleButton("instantiation.enabled", "automation.instantiation.enabled",
				translate("on"), translate("off"), formLayout);
		instantiationEnabledEl.addActionListener(FormEvent.ONCHANGE);
		instantiationEnabledEl.setEnabled(canEdit);
		
		instantiationContainer = uifactory.addInlineFormLayout("instantiationvalues", null, formLayout);
		instantiationValueEl = uifactory.addTextElement("instantiation.value", null, 6, "", instantiationContainer);
		instantiationValueEl.setDisplaySize(6);
		instantiationValueEl.setEnabled(canEdit);
		instantiationUnitEl = uifactory.addDropdownSingleselect("instantiation.unit", null, instantiationContainer, unitPK.keys(), unitPK.values());
		instantiationUnitEl.addActionListener(FormEvent.ONCHANGE);
		instantiationUnitEl.setEnabled(canEdit);
		uifactory.addStaticTextElement("instantiation.infos", null, translate("automation.addon"), instantiationContainer);
		initValueUnit(curriculumElement.getAutoInstantiation(),instantiationEnabledEl, instantiationValueEl, instantiationUnitEl);
	}

	private void initCourseAccessForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("automation.course.access"));
		formLayout.setFormInfo(translate("automation.course.access.infos"));
		formLayout.setFormInfoHelp("manual_user/area_modules/Course_Planner_Implementations/#tab_settings");
		
		uifactory.addStaticTextElement("automation.auto.changes", "automation.auto.changes", "", formLayout);
		
		// Access for coach
		accessForCoachEnabledEl = uifactory.addToggleButton("access.for.coach.enabled", "automation.access.for.coach.enabled",
				translate("on"), translate("off"), formLayout);
		accessForCoachEnabledEl.addActionListener(FormEvent.ONCHANGE);
		accessForCoachEnabledEl.setEnabled(canEdit);
		
		accessForCoachContainer = uifactory.addInlineFormLayout("accessforcoachesvalues", null, formLayout);
		accessForCoachValueEl = uifactory.addTextElement("access.for.coach.value", null, 6, "", accessForCoachContainer);
		accessForCoachValueEl.setDisplaySize(6);
		accessForCoachValueEl.setEnabled(canEdit);
		accessForCoachUnitEl = uifactory.addDropdownSingleselect("access.for.coach.unit", null, accessForCoachContainer, unitPK.keys(), unitPK.values());
		accessForCoachUnitEl.addActionListener(FormEvent.ONCHANGE);
		accessForCoachUnitEl.setEnabled(canEdit);
		uifactory.addStaticTextElement("access.for.coach.infos", null, translate("automation.addon"), accessForCoachContainer);
		initValueUnit(curriculumElement.getAutoAccessForCoach(), accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl);
		
		// Published
		publishedEnabledEl = uifactory.addToggleButton("published.enabled", "automation.published.enabled",
				translate("on"), translate("off"), formLayout);
		publishedEnabledEl.addActionListener(FormEvent.ONCHANGE);
		publishedEnabledEl.setEnabled(canEdit);
		
		publishedContainer = uifactory.addInlineFormLayout("publishedvalues", null, formLayout);
		publishedValueEl = uifactory.addTextElement("published.value", null, 6, "", publishedContainer);
		publishedValueEl.setDisplaySize(6);
		publishedValueEl.setEnabled(canEdit);
		publishedUnitEl = uifactory.addDropdownSingleselect("published.unit", null, publishedContainer, unitPK.keys(), unitPK.values());
		publishedUnitEl.addActionListener(FormEvent.ONCHANGE);
		publishedUnitEl.setEnabled(canEdit);
		uifactory.addStaticTextElement("published.infos", null, translate("automation.addon"), publishedContainer);
		initValueUnit(curriculumElement.getAutoPublished(), publishedEnabledEl, publishedValueEl, publishedUnitEl);
		
		// Finished
		finishedEnabledEl = uifactory.addToggleButton("finished.enabled", "automation.finished.enabled",
				translate("on"), translate("off"), formLayout);
		finishedEnabledEl.addActionListener(FormEvent.ONCHANGE);
		finishedEnabledEl.setEnabled(canEdit);
		
		finishedContainer = uifactory.addInlineFormLayout("finishedvalues", null, formLayout);
		finishedValueEl = uifactory.addTextElement("finished.value", null, 6, "", finishedContainer);
		finishedValueEl.setDisplaySize(6);
		finishedValueEl.setEnabled(canEdit);
		finishedUnitEl = uifactory.addDropdownSingleselect("finished.unit", null, finishedContainer, unitPK.keys(), unitPK.values());
		finishedUnitEl.addActionListener(FormEvent.ONCHANGE);
		finishedUnitEl.setEnabled(canEdit);
		uifactory.addStaticTextElement("finished.infos", null, translate("automation.after.addon"), finishedContainer);
		initValueUnit(curriculumElement.getAutoClosed(), finishedEnabledEl, finishedValueEl, finishedUnitEl);
	}
	
	private void initValueUnit(Automation automation, FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		if(automation == null) return;
		
		enabledEl.toggle(automation.getUnit() != null);
		
		String val = automation.getValue() != null ? automation.getValue().toString() : null;
		valueEl.setValue(val);
		if(automation.getUnit() != null && unitPK.containsKey(automation.getUnit().name())) {
			unitEl.select(automation.getUnit().name(), true);
		} else {
			unitEl.select(AutomationUnit.SAME_DAY.name(), true);
		}
	}
	
	private void updateUI() {
		instantiationContainer.setVisible(instantiationEnabledEl.isOn());
		accessForCoachContainer.setVisible(accessForCoachEnabledEl.isOn());
		publishedContainer.setVisible(publishedEnabledEl.isOn());
		finishedContainer.setVisible(finishedEnabledEl.isOn());
	}

	private void updateValuesUI() {
		updateUI(instantiationEnabledEl, instantiationValueEl, instantiationUnitEl);
		updateUI(accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl);
		updateUI(publishedEnabledEl, publishedValueEl, publishedUnitEl);
		updateUI(finishedEnabledEl, finishedValueEl, finishedUnitEl);
	}
	
	private void updateUI(FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		if(!enabledEl.isOn() || (unitEl.isOneSelected() && AutomationUnit.SAME_DAY.name().equals(unitEl.getSelectedKey()))) {
			valueEl.setValue("");
			valueEl.setEnabled(false);
		} else {
			valueEl.setEnabled(true);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateFormLogic(instantiationContainer, instantiationEnabledEl, instantiationValueEl, instantiationUnitEl);
		allOk &= validateFormLogic(accessForCoachContainer, accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl)
				&& validateAccessForCoachFormLogic(ureq, accessForCoachContainer, accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl);
		allOk &= validateFormLogic(publishedContainer, publishedEnabledEl, publishedValueEl, publishedUnitEl)
				&& validatePublishedFormLogic(ureq, publishedContainer, publishedEnabledEl, publishedValueEl, publishedUnitEl);
		allOk &= validateFormLogic(finishedContainer, finishedEnabledEl, finishedValueEl, finishedUnitEl);
		
		return allOk;
	}
	
	private boolean validateAccessForCoachFormLogic(UserRequest ureq, FormLayoutContainer container, FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		boolean allOk = true;
		
		Automation instantiationAutomation = getAutomationFor(instantiationEnabledEl, instantiationValueEl, instantiationUnitEl);
		Automation accessForCoachAutomation = getAutomationFor(enabledEl, valueEl, unitEl);
		if(instantiationAutomation != null && accessForCoachAutomation != null
				&& instantiationAutomation.getDateBefore(ureq.getRequestTimestamp()).after(accessForCoachAutomation.getDateBefore(ureq.getRequestTimestamp()))) {
			container.setErrorKey("error.access.for.coach.after.instantiation");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validatePublishedFormLogic(UserRequest ureq, FormLayoutContainer container, FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		boolean allOk = true;
		
		Automation instantiationAutomation = getAutomationFor(instantiationEnabledEl, instantiationValueEl, instantiationUnitEl);
		Automation accessForCoachAutomation = getAutomationFor(accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl);
		
		Automation publishAutomation = getAutomationFor(enabledEl, valueEl, unitEl);
		if(instantiationAutomation != null && publishAutomation != null
				&& instantiationAutomation.getDateBefore(ureq.getRequestTimestamp()).after(publishAutomation.getDateBefore(ureq.getRequestTimestamp()))) {
			container.setErrorKey("error.publish.after.instantiation");
			allOk &= false;
		} else if(accessForCoachAutomation != null && publishAutomation != null
				&& accessForCoachAutomation.getDateBefore(ureq.getRequestTimestamp()).after(publishAutomation.getDateBefore(ureq.getRequestTimestamp()))) {
			container.setErrorKey("error.publish.after.access.for.coach");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateFormLogic(FormLayoutContainer container, FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		boolean allOk = true;
		
		container.clearError();
		valueEl.clearError();
		unitEl.clearError();
		if(enabledEl.isOn()) {
			if(!unitEl.isOneSelected()) {
				container.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				AutomationUnit unit = AutomationUnit.valueOf(unitEl.getSelectedKey());
				if(unit != AutomationUnit.SAME_DAY) {
					allOk &= validateInteger(container, valueEl);
				}
			}
		}
		
		return allOk;
	}
	
	private static boolean validateInteger(FormLayoutContainer container, TextElement el) {
		boolean allOk = true;

		String val = el.getValue();
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				int value = Integer.parseInt(val);
				if(value <= 0) {
					allOk &= false;
					container.setErrorKey("form.error.nointeger");
				}
			} catch (NumberFormatException e) {
				allOk &= false;
				container.setErrorKey("form.error.nointeger");
			}
		} else {
			allOk &= false;
			container.setErrorKey("form.legende.mandatory");
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(instantiationEnabledEl == source || accessForCoachEnabledEl == source
				|| publishedEnabledEl == source || finishedEnabledEl == source) {
			updateUI();
		} else if(instantiationUnitEl == source || accessForCoachUnitEl == source
				|| publishedUnitEl == source || finishedUnitEl == source) {
			updateValuesUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		curriculumElement.setAutoInstantiation(getAutomationFor(instantiationEnabledEl, instantiationValueEl, instantiationUnitEl));
		curriculumElement.setAutoAccessForCoach(getAutomationFor(accessForCoachEnabledEl, accessForCoachValueEl, accessForCoachUnitEl));
		curriculumElement.setAutoPublished(getAutomationFor(publishedEnabledEl, publishedValueEl, publishedUnitEl));
		curriculumElement.setAutoClosed(getAutomationFor(finishedEnabledEl, finishedValueEl, finishedUnitEl));
		
		curriculumElement = curriculumService.updateCurriculumElement(curriculumElement);
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private Automation getAutomationFor(FormToggle enabledEl, TextElement valueEl, SingleSelection unitEl) {
		String val = valueEl.getValue();
		if(enabledEl.isOn() && unitEl.isOneSelected()) {
			AutomationUnit unit = AutomationUnit.valueOf(unitEl.getSelectedKey());
			if(unit == AutomationUnit.SAME_DAY) {
				return AutomationImpl.valueOf(Integer.valueOf(0), AutomationUnit.SAME_DAY);
			}
			if(StringHelper.isLong(val)) {
				return AutomationImpl.valueOf(Integer.valueOf(val), unit);
			}
		}
		return null;
	}
}
