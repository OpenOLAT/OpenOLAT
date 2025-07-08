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
package org.olat.modules.topicbroker.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.topicbroker.TBEnrollmentStrategyConfig;
import org.olat.modules.topicbroker.TBEnrollmentStrategyFactory;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.topicbroker.ui.events.TBEnrollmentProcessRunEvent;

/**
 * 
 * Initial date: Jul 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEnrollmentStrategyEditController extends FormBasicController {

	private SingleSelection presetEl;
	private SliderElement weightMaxEnrollmentEl;
	private SliderElement weightMaxTopicEl;
	private SliderElement weightMaxPrioritiesEl;

	private final TBEnrollmentStrategyConfig initialStrategyConfig;

	protected TBEnrollmentStrategyEditController(UserRequest ureq, WindowControl wControl, TBEnrollmentStrategyConfig initialStrategyConfig) {
		super(ureq, wControl, "strategy_edit");
		this.initialStrategyConfig = initialStrategyConfig;
		
		initForm(ureq);
		updateSliderUI();
	}

	public TBEnrollmentStrategyConfig getStrategyConfig() {
		TBEnrollmentStrategyConfig config = TBEnrollmentStrategyFactory.createConfig(TBEnrollmentStrategyType.valueOf(presetEl.getSelectedKey()));
		
		if (TBEnrollmentStrategyType.custom == config.getType()) {
			config.setMaxEnrollmentsWeight((int)weightMaxEnrollmentEl.getValue());
			config.setMaxTopicsWeight((int)weightMaxTopicEl.getValue());
			config.setMaxPrioritiesWeight((int)weightMaxPrioritiesEl.getValue());
		}
		
		return config;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues presetValues = new SelectionValues();
		presetValues.add(createPresetEntry(TBEnrollmentStrategyType.maxEnrollments));
		presetValues.add(createPresetEntry(TBEnrollmentStrategyType.maxPriorities));
		presetValues.add(createPresetEntry(TBEnrollmentStrategyType.maxTopics));
		presetValues.add(createPresetEntry(TBEnrollmentStrategyType.custom));
		presetEl = uifactory.addCardSingleSelectHorizontal("preset", "enrollment.strategy.preset", formLayout, presetValues);
		presetEl.addActionListener(FormEvent.ONCHANGE);
		presetEl.select(initialStrategyConfig.getType().name(), true);
		
		weightMaxEnrollmentEl = uifactory.addSliderElement("max.enrollments.slider", null, formLayout);
		weightMaxEnrollmentEl.setLabel("<i class=\"o_icon o_icon_tb_enrollments\"> </i> " + translate("enrollment.criterion.max.enrollments.weighting.label"), null, false);
		weightMaxEnrollmentEl.setVertical(true);
		weightMaxEnrollmentEl.setMinValue(1);
		weightMaxEnrollmentEl.setMaxValue(5);
		weightMaxEnrollmentEl.setValue(intOrThree(initialStrategyConfig.getMaxEnrollmentsWeight()));
		
		weightMaxTopicEl = uifactory.addSliderElement("max.topics.slider", null, formLayout);
		weightMaxTopicEl.setLabel("<i class=\"o_icon o_icon_tb_topics\"> </i> " + translate("enrollment.criterion.max.topics.weighting.label"), null, false);
		weightMaxTopicEl.setVertical(true);
		weightMaxTopicEl.setMinValue(1);
		weightMaxTopicEl.setMaxValue(5);
		weightMaxTopicEl.setValue(intOrThree(initialStrategyConfig.getMaxTopicsWeight()));
		
		weightMaxPrioritiesEl = uifactory.addSliderElement("max.priorities.slider", null, formLayout);
		weightMaxPrioritiesEl.setLabel("<i class=\"o_icon o_icon_tb_priority\"> </i> " + translate("enrollment.criterion.max.priorities.weighting.label"), null, false);
		weightMaxPrioritiesEl.setVertical(true);
		weightMaxPrioritiesEl.setMinValue(1);
		weightMaxPrioritiesEl.setMaxValue(5);
		weightMaxPrioritiesEl.setValue(intOrThree(initialStrategyConfig.getMaxPrioritiesWeight()));
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		FormSubmit applRunButton = uifactory.addFormSubmitButton("enrollment.strategy.apply.run", buttonLayout);
		applRunButton.setIconLeftCSS("o_icon o_icon-lg o_icon_tb_run_start");
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private SelectionValue createPresetEntry(TBEnrollmentStrategyType type) {
		return entry(
				type.name(),
				TBUIFactory.getTranslatedType(getTranslator(), type),
				TBUIFactory.getTranslatedTypeDesc(getTranslator(), type),
				"o_icon " +  TBUIFactory.getTypeIconCss(type),
				null, true);
	}
	
	private double intOrThree(Integer integer) {
		return integer != null? integer.intValue(): 3;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == presetEl) {
			updateSliderUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, TBEnrollmentProcessRunEvent.EVENT);
	}

	private void updateSliderUI() {
		flc.contextPut("custom", presetEl.isKeySelected(TBEnrollmentStrategyType.custom.name()));
	}

}
