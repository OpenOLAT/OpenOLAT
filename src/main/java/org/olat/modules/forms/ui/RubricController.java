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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.model.SliderWrapper;
import org.olat.modules.forms.ui.model.EvaluationFormElementWrapper;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricController extends FormBasicController {
	
	private int count = 0;
	private final Rubric rubric;
	
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
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(rubric);
		List<Slider> sliders = rubric.getSliders();
		List<SliderWrapper> sliderWrappers = new ArrayList<>(sliders.size());
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
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (count++), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl);
		sliderEl.setUserObject(sliderWrapper);
		return sliderWrapper;
	}
	
	private SliderWrapper forgeDiscreteSlider(Slider slider, Rubric element) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (count++), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		sliderEl.setStep(1);
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl);
		sliderEl.setUserObject(sliderWrapper);
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

		SingleSelection radioEl = uifactory.addRadiosVertical("slider_" + (count++), null, flc, theKeys, theValues);
		radioEl.setDomReplacementWrapperRequired(false);
		radioEl.addActionListener(FormEvent.ONCHANGE);
		int widthInPercent = EvaluationFormElementWrapper.getWidthInPercent(element);
		radioEl.setWidthInPercent(widthInPercent, true);

		SliderWrapper sliderWrapper = new SliderWrapper(slider, radioEl);
		radioEl.setUserObject(sliderWrapper);
		return sliderWrapper;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
