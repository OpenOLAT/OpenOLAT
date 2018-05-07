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
package org.olat.modules.forms.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricController extends FormBasicController implements EvaluationFormResponseController {
	
	private static final String NO_RESPONSE_KEY = "enabled";
	private static final String[] NO_RESPONSE_KEYS = new String[] { NO_RESPONSE_KEY };
	
	private final Rubric rubric;
	private List<SliderWrapper> sliderWrappers;
	private Map<String, EvaluationFormResponse> responses = new HashMap<>();
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricController(UserRequest ureq, WindowControl wControl, Rubric rubric) {
		super(ureq, wControl, "rubric");
		this.rubric = rubric;
		initForm(ureq);
	}
	
	public RubricController(UserRequest ureq, WindowControl wControl, Rubric rubric, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "rubric", rootForm);
		this.rubric = rubric;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateForm();
	}
	
	protected void updateForm() {
		RubricWrapper wrapper = new RubricWrapper(rubric);
		List<Slider> sliders = rubric.getSliders();
		sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			SliderType type = rubric.getSliderType();
			SliderWrapper sliderWrapper = null;
			if(type == SliderType.discrete) {
				sliderWrapper = forgeDiscreteRadioButtons(slider, rubric);
			} else if(type == SliderType.discrete_slider) {
				sliderWrapper = forgeDiscreteSlider(slider, rubric);
			} else if(type == SliderType.continuous) {
				sliderWrapper = forgeContinuousSlider(slider, rubric);
			}
			
			if(sliderWrapper != null) {
				sliderWrappers.add(sliderWrapper);
			}
		}
		wrapper.setSliders(sliderWrappers);
		flc.contextPut("element", wrapper);
	}

	private SliderWrapper forgeContinuousSlider(Slider slider, Rubric element) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		MultipleSelectionElement noResponseEl = null;
		if (element.isNoResponseEnabled()) {
			noResponseEl = uifactory.addCheckboxesHorizontal("no_resp_" + CodeHelper.getRAMUniqueID(), null, flc, NO_RESPONSE_KEYS,
					getNoResponseValue());
			noResponseEl.setEscapeHtml(false);
			noResponseEl.setDomReplacementWrapperRequired(false);
			noResponseEl.addActionListener(FormEvent.ONCHANGE);
		}
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl, noResponseEl);
		sliderEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}
	
	private SliderWrapper forgeDiscreteSlider(Slider slider, Rubric element) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + CodeHelper.getRAMUniqueID(), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		sliderEl.setStep(1);
		MultipleSelectionElement noResponseEl = null;
		if (element.isNoResponseEnabled()) {
			noResponseEl = uifactory.addCheckboxesHorizontal("no_resp_" + CodeHelper.getRAMUniqueID(), null, flc, NO_RESPONSE_KEYS,
					getNoResponseValue());
			noResponseEl.setEscapeHtml(false);
			noResponseEl.setDomReplacementWrapperRequired(false);
			noResponseEl.addActionListener(FormEvent.ONCHANGE);
		}
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl, noResponseEl);
		sliderEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}

	private SliderWrapper forgeDiscreteRadioButtons(Slider slider, Rubric element) {
		int start = element.getStart();
		int end = element.getEnd();
		int steps = element.getSteps();
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		String[] theValues = new String[steps];
		
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
			theValues[i] = "";
		}

		SingleSelection radioEl = uifactory.addRadiosVertical("slider_" + CodeHelper.getRAMUniqueID(), null, flc, theKeys, theValues);
		radioEl.setAllowNoSelection(true);
		radioEl.setDomReplacementWrapperRequired(false);
		int widthInPercent = RubricWrapper.getWidthInPercent(element);
		radioEl.setWidthInPercent(widthInPercent, true);

		MultipleSelectionElement noResponseEl = null;
		if (element.isNoResponseEnabled()) {
			noResponseEl = uifactory.addCheckboxesVertical("no_resp_" + CodeHelper.getRAMUniqueID(), flc, NO_RESPONSE_KEYS,
					getNoResponseValue(), 0);
			noResponseEl.setEscapeHtml(false);
			noResponseEl.setDomReplacementWrapperRequired(false);
			noResponseEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		SliderWrapper sliderWrapper = new SliderWrapper(slider, radioEl, noResponseEl);
		radioEl.setUserObject(sliderWrapper);
		if (noResponseEl != null) {
			noResponseEl.setUserObject(sliderWrapper);
		}
		return sliderWrapper;
	}
	
	private String[] getNoResponseValue() {
		return new String[] { "&nbsp;<span class='o_evaluation_no_resp_value'>" + getTranslator().translate("no.response") + "</span>"};
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement) {
			MultipleSelectionElement noResponseEl = (MultipleSelectionElement) source;
			Object uobject = noResponseEl.getUserObject();
			if (uobject instanceof SliderWrapper) {
				SliderWrapper sliderWrapper = (SliderWrapper) uobject;
				boolean noAnswer = noResponseEl.isAtLeastSelected(1);
				sliderWrapper.getFormItem().setEnabled(!noAnswer);
				flc.setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			sliderWrapper.getFormItem().setEnabled(!readOnly);
			if (sliderWrapper.getNoResponseEl() != null) {
				sliderWrapper.getNoResponseEl().setEnabled(!readOnly);
			}
			if (!readOnly) {
				disableSliderIfNoResponse(sliderWrapper);
			}
		}
	}

	@Override
	public boolean hasResponse() {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			if (!responses.containsKey(sliderWrapper.getId())) {
				return false;
			}
			EvaluationFormResponse response = responses.get(sliderWrapper.getId());
			if (response == null || (!response.isNoResponse()) && response.getNumericalResponse() == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void loadResponse(EvaluationFormSessionRef session) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			EvaluationFormResponse response = evaluationFormManager.loadResponse(sliderWrapper.getId(), session);
			if (response != null) {
				responses.put(sliderWrapper.getId(), response);
				if (response.getNumericalResponse() != null) {
					BigDecimal numericalResponse = response.getNumericalResponse();
					setValue(sliderWrapper, numericalResponse);
				}
				disableSliderIfNoResponse(sliderWrapper);
			}
		}
	}

	private void disableSliderIfNoResponse(SliderWrapper sliderWrapper) {
		EvaluationFormResponse response = responses.get(sliderWrapper.getId());
		if (response != null && response.isNoResponse()) {
			MultipleSelectionElement noResponseEl = sliderWrapper.getNoResponseEl();
			if (noResponseEl != null) {
				noResponseEl.select(NO_RESPONSE_KEY, true);
			}
			sliderWrapper.getFormItem().setEnabled(false);
		}
	}

	private void setValue(SliderWrapper sliderWrapper, BigDecimal numericalResponse) {
		if (sliderWrapper.getRadioEl() != null) {
			setValue(sliderWrapper.getRadioEl(), numericalResponse);
		} else if (sliderWrapper.getSliderEl() != null) {
			setValue(sliderWrapper.getSliderEl(), numericalResponse);
		}
	}

	private void setValue(SingleSelection radioEl, BigDecimal numericalResponse) {
		int start = rubric.getStart();
		int end = rubric.getEnd();
		int steps = rubric.getSteps();
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		
		double step = (end - start + 1) / (double) steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
		}
		
		double val = numericalResponse.doubleValue();
		double error = step / 10.0d;
		for (int i = 0; i < theSteps.length; i++) {
			double theStep = theSteps[i];
			double margin = Math.abs(theStep - val);
			if (margin < error) {
				radioEl.select(theKeys[i], true);
			}
		}
	}

	private void setValue(SliderElement sliderEl, BigDecimal numericalResponse) {
		double val = numericalResponse.doubleValue();
		sliderEl.setValue(val);
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
		for (SliderWrapper sliderWrapper: sliderWrappers) {
			boolean noResponseSelected = sliderWrapper.getNoResponseEl() != null && sliderWrapper.getNoResponseEl().isAtLeastSelected(1);
			if (noResponseSelected) {
				saveNoResponse(session, sliderWrapper);
			} else {
				saveSliderResponse(session, sliderWrapper);
			}
		}
	}
	
	private void saveNoResponse(EvaluationFormSession session, SliderWrapper sliderWrapper) {
		EvaluationFormResponse response = responses.get(sliderWrapper.getId());
		if (response == null) {
			response = evaluationFormManager.createNoResponse(sliderWrapper.getId(), session);
		} else {
			response = evaluationFormManager.updateNoResponse(response);
		}
		responses.put(sliderWrapper.getId(), response);
	}
	
	private void saveSliderResponse(EvaluationFormSession session, SliderWrapper sliderWrapper) {
		BigDecimal value = null;
		SliderElement slider = sliderWrapper.getSliderEl();
		if (slider != null && slider.hasValue()) {
			value = BigDecimal.valueOf(slider.getValue());
		} else {
			SingleSelection radioEl = sliderWrapper.getRadioEl();
			if (radioEl != null && radioEl.isOneSelected()) {
				String selectedKey = radioEl.getSelectedKey();
				if (StringHelper.containsNonWhitespace(selectedKey)) {
					value = new BigDecimal(selectedKey);
				}
			}
		}
		if (value != null) {
			EvaluationFormResponse response = responses.get(sliderWrapper.getId());
			if (response == null) {
				response = evaluationFormManager.createNumericalResponse(sliderWrapper.getId(), session, value);
			} else {
				response = evaluationFormManager.updateNumericalResponse(response, value);
			}
			responses.put(sliderWrapper.getId(), response);
		} else {
			EvaluationFormResponse response = responses.get(sliderWrapper.getId());
			if (response != null) {
				evaluationFormManager.deleteResponse(response);
				responses.remove(sliderWrapper.getId());
			}
		}
	}
	
	public final static class RubricWrapper {

		private final Rubric rubric;
		private List<SliderWrapper> sliders;

		public RubricWrapper(Rubric rubric) {
			this.rubric = rubric;
		}
		
		public boolean isDiscreteRubric() {
			return rubric.getSliderType() == SliderType.discrete;
		}
		
		public boolean isDiscreteSliderRubric() {
			return rubric.getSliderType() == SliderType.discrete_slider;
		}
		
		public boolean isNoResponseEnabled() {
			return rubric.isNoResponseEnabled();
		}
		

		public static int getWidthInPercent(Rubric theRubric) {
			if(theRubric.getSliderType() == SliderType.discrete) {
				int steps = theRubric.getSteps();
				int stepInPercent = Math.round(100.0f / steps) - 1;
				return stepInPercent;
			}
			return 0;
		}
		
		public int getStepInPercent() {
			return getWidthInPercent(rubric);
		}
		
		public boolean isStepLabels() {
			if(rubric.getStepLabels() == null || rubric.getStepLabels().isEmpty()) {
				return false;
			}
			
			List<StepLabel> stepLabels = rubric.getStepLabels();
			for(StepLabel stepLabel:stepLabels) {
				if(stepLabel != null && StringHelper.containsNonWhitespace(stepLabel.getLabel())) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isLeftLabels() {
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getStartLabel())) {
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean isRightLabels() {
			List<Slider> rubricSliders = rubric.getSliders();
			if(rubricSliders != null && rubricSliders.size() > 0) {
				for(Slider slider:rubricSliders) {
					if(slider != null && StringHelper.containsNonWhitespace(slider.getEndLabel())) {
						return true;
					}
				}
			}	
			return false;
		}
		
		public List<String> getStepLabels() {
			if(rubric.getStepLabels() != null && rubric.getStepLabels().size() > 0) {
				List<String> stepLabels = new ArrayList<>(rubric.getStepLabels().size());
				for(StepLabel stepLabel:rubric.getStepLabels()) {
					stepLabels.add(stepLabel.getLabel());
				}
				return stepLabels;
			}
			return new ArrayList<>(1);
		}

		public List<SliderWrapper> getSliders() {
			return sliders;
		}

		public void setSliders(List<SliderWrapper> sliders) {
			this.sliders = sliders;
		}
	}
	
	public final static class SliderWrapper {

		private final Slider slider;
		private final SliderElement sliderEl;
		private final SingleSelection radioEl;
		private final MultipleSelectionElement noResponseEl;
		
		public SliderWrapper(Slider slider, SingleSelection radioEl, MultipleSelectionElement noResponseEl) {
			this(slider, radioEl, null, noResponseEl);
		}
		
		public SliderWrapper(Slider slider, SliderElement sliderEl, MultipleSelectionElement noResponseEl) {
			this(slider, null, sliderEl, noResponseEl);
		}
		
		private SliderWrapper(Slider slider, SingleSelection radioEl, SliderElement sliderEl,
				MultipleSelectionElement noResponseEl) {
			this.slider = slider;
			this.radioEl = radioEl;
			this.sliderEl = sliderEl;
			this.noResponseEl = noResponseEl;
		}
		
		public String getId() {
			return slider.getId();
		}
		
		public String getStartLabel() {
			String start = slider.getStartLabel();
			return start == null ? "" : start;
		}
		
		public String getEndLabel() {
			String end = slider.getEndLabel();
			return end == null ? "" : end;
		}
		
		public Slider getSlider() {
			return slider;
		}
		
		public FormItem getFormItem() {
			return radioEl == null ? sliderEl : radioEl;
		}
		
		public SingleSelection getRadioEl() {
			return radioEl;
		}
		
		public SliderElement getSliderEl() {
			return sliderEl;
		}
		
		public MultipleSelectionElement getNoResponseEl() {
			return noResponseEl;
		}
	}

}
