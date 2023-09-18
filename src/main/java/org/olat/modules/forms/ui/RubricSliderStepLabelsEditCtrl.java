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
package org.olat.modules.forms.ui;

import java.util.List;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.RubricEditorController.SliderRow;

/**
 * 
 * Initial date: 14 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderStepLabelsEditCtrl extends FormBasicController {
	
	private FormLink previousEl;
	private SingleSelection labelSelectionEl;
	private FormLink nextEl;
	private TextAreaElement labelEl;
	
	private final Rubric rubric;
	private final List<StepLabel> stepLabels;
	private final int steps;
	private int currentStep = 0;

	protected RubricSliderStepLabelsEditCtrl(UserRequest ureq, WindowControl wControl, Rubric rubric, SliderRow row) {
		super(ureq, wControl, "rubric_slider_step_labels_edit");
		this.rubric = rubric;
		this.steps = rubric.getSteps();
		this.stepLabels = row.getSlider().getStepLabels();
		
		for (int i = stepLabels.size(); i < steps; i++) {
			StepLabel label = new StepLabel();
			label.setId(UUID.randomUUID().toString());
			label.setLabel(null);
			stepLabels.add(label);
		}
		
		initForm(ureq);
		updateSelectedLabel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousEl = uifactory.addFormLink("previous", "", "", formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		previousEl.setIconRightCSS("o_icon o_icon_back");
		
		SelectionValues labelSV = new SelectionValues();
		ScaleType scaleType = rubric.getScaleType() != null? rubric.getScaleType(): null;
		for (int i=0; i<steps; i++) {
			String label = stepLabels.get(i).getLabel();
			if (label == null) {
				label = "";
			}
			
			if (scaleType != null) {
				double stepValue = scaleType.getStepValue(steps, i + 1);
				label += " - " + translate("rubric.scale.example.value", new String[] {String.valueOf(stepValue)});
			}
			
			labelSV.add(SelectionValues.entry(String.valueOf(i), label));
		}
		labelSelectionEl = uifactory.addDropdownSingleselect("labels", null, formLayout, labelSV.keys(), labelSV.values());
		labelSelectionEl.addActionListener(FormEvent.ONCHANGE);
		
		nextEl = uifactory.addFormLink("next", "", "", formLayout, Link.BUTTON | Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS);
		nextEl.setIconRightCSS("o_icon o_icon_start");
		
		labelEl = uifactory.addTextAreaElement("label", null, 1000, 8, 72, false, false, true, null, formLayout);
		labelEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (labelSelectionEl == source) {
			doSelectLabel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (nextEl == source) {
			doNext();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (previousEl == source) {
			doPrevious();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == labelEl) {
			stepLabels.get(currentStep).setLabel(labelEl.getValue());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSelectLabel() {
		if (labelSelectionEl.isOneSelected()) {
			stepLabels.get(currentStep).setLabel(labelEl.getValue());
			
			currentStep = labelSelectionEl.getSelected();
			updateSelectedLabel();
		}
	}
	
	private void doPrevious() {
		stepLabels.get(currentStep).setLabel(labelEl.getValue());
		
		currentStep--;
		updateSelectedLabel();
	}

	private void doNext() {
		stepLabels.get(currentStep).setLabel(labelEl.getValue());
		
		currentStep++;
		updateSelectedLabel();
	}
	
	private void updateSelectedLabel() {
		if (currentStep == 0) {
			previousEl.setEnabled(false);
		} else {
			previousEl.setEnabled(true);
		}
		
		if (currentStep == (steps-1)) {
			nextEl.setEnabled(false);
		} else {
			nextEl.setEnabled(true);
		}
		
		labelSelectionEl.select(String.valueOf(currentStep), true);
		
		String label = stepLabels.get(currentStep).getLabel();
		labelEl.setValue(label);
	}

}
