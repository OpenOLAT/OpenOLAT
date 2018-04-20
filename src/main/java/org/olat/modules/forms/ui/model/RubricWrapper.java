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
package org.olat.modules.forms.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;

/**
 * 
 * Initial date: 13 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricWrapper {

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
