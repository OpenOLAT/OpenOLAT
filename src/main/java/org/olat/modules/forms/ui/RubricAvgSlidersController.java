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
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.jpa.CalculatedDouble;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.component.SliderPoint;
import org.olat.modules.forms.ui.model.CalculatedDoubleDataSource;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricAvgSlidersController extends BasicController {

	private static final String SLIDER_POINT_COLOR = "#777";
	
	private final VelocityContainer mainVC;
	private final Rubric rubric;
	private final Map<String, Double> identToValue;
	
	
	public RubricAvgSlidersController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			CalculatedDoubleDataSource dataSource) {
		super(ureq, wControl);
		this.rubric = rubric;
		this.identToValue = dataSource.getResponses().stream()
				.collect(Collectors.toMap(CalculatedDouble::getIdentifier, CalculatedDouble::getValue));
		
		mainVC = createVelocityContainer("rubric_avg_sliders");
		RubricWrapper wrapper = createRubricWrapper();
		mainVC.contextPut("rubricWrapper", wrapper);
		putInitialPanel(mainVC);
	}

	private RubricWrapper createRubricWrapper() {
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		for (Slider slider: rubric.getSliders()) {
			SliderWrapper sliderWrapper = createSliderWrapper(slider);
			sliderWrappers.add(sliderWrapper);
		}
		RubricWrapper rubricWrapper = new RubricWrapper(rubric);
		rubricWrapper.setSliders(sliderWrappers);
		return rubricWrapper;
	}

	private SliderWrapper createSliderWrapper(Slider slider) {
		String id = "_" + CodeHelper.getRAMUniqueID();
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(rubric.getStart());
		overviewEl.setMaxValue(rubric.getEnd());
		
		String responseIdentifier = slider.getId();
		Double value = identToValue.get(responseIdentifier);
		List<SliderPoint> sliderPoints = new ArrayList<>();
		if (value != null) {
			SliderPoint sliderPoint = new SliderPoint(SLIDER_POINT_COLOR, value);
			sliderPoints.add(sliderPoint);
		}
		overviewEl.setValues(sliderPoints);
		mainVC.put(id, overviewEl.getComponent());
		return new SliderWrapper(slider, overviewEl);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public final static class RubricWrapper {

		private final Rubric rubric;
		private List<SliderWrapper> sliders;

		public RubricWrapper(Rubric rubric) {
			this.rubric = rubric;
		}
		
		public static int getWidthInPercent(Rubric theRubric) {
			if(theRubric.getSliderType() != SliderType.continuous) {
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
		private final SliderOverviewElement overviewEl;
		
		private SliderWrapper(Slider slider, SliderOverviewElement overviewEl) {
			this.slider = slider;
			this.overviewEl = overviewEl;
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
		
		public SliderOverviewElement getOverviewEl() {
			return overviewEl;
		}
	}

}
