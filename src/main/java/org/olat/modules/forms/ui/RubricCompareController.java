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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.RadarChartComponent.Format;
import org.olat.core.gui.components.chart.RadarChartElement;
import org.olat.core.gui.components.chart.RadarSeries;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.component.SliderPoint;
import org.olat.modules.forms.ui.model.CompareResponse;

/**
 * 
 * Initial date: 20.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricCompareController extends FormBasicController implements Controller {

	private final Rubric rubric;
	private final List<CompareResponse> compareResponses;
	private final Map<String, List<SliderResponse>> identifierToResponse = new HashMap<>();
	
	public RubricCompareController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			List<CompareResponse> compareResponses) {
		super(ureq, wControl, "rubric_compare");
		this.rubric = rubric;
		this.compareResponses = compareResponses;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		for (CompareResponse compareResponse: compareResponses) {
			for (EvaluationFormResponse evaluationFromResponse: compareResponse.getResponses()) {
				if (!evaluationFromResponse.isNoResponse()) {
					String responseIdentifier = evaluationFromResponse.getResponseIdentifier();
					List<SliderResponse> sliderResponses = identifierToResponse.get(responseIdentifier);
					if (sliderResponses == null) {
						sliderResponses = new ArrayList<>();
						identifierToResponse.put(responseIdentifier, sliderResponses);
					}
					SliderResponse sliderResponse = new SliderResponse(compareResponse.getLegendName(),
							compareResponse.getColor(), evaluationFromResponse);
					sliderResponses.add(sliderResponse);
				}
			}
		}
		
		flc.contextPut("rubricWrapper", createWrapper());
	}

	private RubricCompareWrapper createWrapper() {
		if(rubric.getSliders().size() > 2) {
			return createRadarRubricWrapper();
		}
		return createRubricWrapper();
	}

	private RubricCompareWrapper createRadarRubricWrapper() {
		RubricCompareWrapper wrapper = new RubricCompareWrapper(rubric);
		wrapper.setRadarOverview(true);
		
		List<String> axisList = new ArrayList<>();
		List<Slider> sliders = rubric.getSliders();
		Map<EvaluationFormSession,RadarSeries> series = new HashMap<>();
		for(Slider slider:sliders) {
			String axis;
			 if(StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				axis = slider.getEndLabel();
			} else if(StringHelper.containsNonWhitespace(slider.getStartLabel())) {
				axis = slider.getStartLabel();
			} else {
				axis = "";
			}
			axisList.add(axis);
			
			String responseIdentifier = slider.getId();
			List<SliderResponse> sliderReesponses = identifierToResponse.get(responseIdentifier);
			if(sliderReesponses != null && sliderReesponses.size() > 0) {
				for(SliderResponse sliderResponse: sliderReesponses) {
					EvaluationFormResponse response = sliderResponse.getResponse();
					EvaluationFormSession responseSession = response.getSession();
					if(!series.containsKey(responseSession)) {
						String legend = sliderResponse.getLegendName();
						String color = sliderResponse.getColor();
						series.put(responseSession, new RadarSeries(legend, color));
					}
					if(response.getNumericalResponse() != null ) {
						double value = response.getNumericalResponse().doubleValue();
						series.get(responseSession).addPoint(axis, value);
					}
				}
			}
		}
		
		String id = "radar_" + CodeHelper.getRAMUniqueID();
		RadarChartElement radarEl = new RadarChartElement(id);
		radarEl.setSeries(new ArrayList<>(series.values()));
		radarEl.setShowLegend(true);
		radarEl.setAxis(axisList);
		if(rubric.getSliderType() == SliderType.discrete || rubric.getSliderType() == SliderType.discrete_slider) {
			radarEl.setLevels(rubric.getSteps());
			radarEl.setMaxValue(rubric.getSteps());
			radarEl.setFormat(Format.integer);
		} else if(rubric.getSliderType() == SliderType.continuous) {
			radarEl.setLevels(10);
			radarEl.setMaxValue(100);
			radarEl.setFormat(Format.integer);
		}
		wrapper.setRadarEl(radarEl);
		flc.add(id, radarEl);
		return wrapper;
	}

	private RubricCompareWrapper createRubricWrapper() {
		RubricCompareWrapper wrapper = new RubricCompareWrapper(rubric);
		wrapper.setSliderOverview(true);
		List<Slider> sliders = rubric.getSliders();
		List<SliderCompareWrapper> sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			String responseIdentifier = slider.getId();
			List<SliderResponse> sliderResponses = identifierToResponse.get(responseIdentifier);
			SliderCompareWrapper sliderWrapper = createSliderCompareWrapper(slider, sliderResponses);
			sliderWrappers.add(sliderWrapper);
		}
		wrapper.setSliders(sliderWrappers);
		return wrapper;
	}
	
	private SliderCompareWrapper createSliderCompareWrapper(Slider slider, List<SliderResponse> sliderResponses) {
		String id = "overview_" + CodeHelper.getRAMUniqueID();
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(rubric.getStart());
		overviewEl.setMaxValue(rubric.getEnd());
		flc.add(id, overviewEl);
		
		List<SliderPoint> values = new ArrayList<>();
		if(sliderResponses != null && sliderResponses.size() > 0) {
			for(SliderResponse sliderResponse: sliderResponses) {
				EvaluationFormResponse response = sliderResponse.getResponse();
				if(response.getNumericalResponse() != null) {
					String color = sliderResponse.getColor();
					double value = response.getNumericalResponse().doubleValue();
					values.add(new SliderPoint(color, value));
				}
			}
		}
		overviewEl.setValues(values);
		
		return new SliderCompareWrapper(slider, overviewEl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private final static class SliderResponse {
		
		private final String legendName;
		private final String color;
		private final EvaluationFormResponse response;
		
		SliderResponse(String legendName, String color, EvaluationFormResponse response) {
			this.legendName = legendName;
			this.color = color;
			this.response = response;
		}

		String getLegendName() {
			return legendName;
		}

		String getColor() {
			return color;
		}

		EvaluationFormResponse getResponse() {
			return response;
		}
		
	}
	
	public final static class RubricCompareWrapper {

		private final Rubric rubric;
		private boolean sliderOverview;
		private boolean radarOverview;
		private List<SliderCompareWrapper> sliders;
		private RadarChartElement radarEl;
		
		public RubricCompareWrapper(Rubric rubric) {
			this.rubric = rubric;
		}

		public boolean isSliderOverview() {
			return sliderOverview;
		}

		void setSliderOverview(boolean sliderOverview) {
			this.sliderOverview = sliderOverview;
		}

		public boolean isRadarOverview() {
			return radarOverview;
		}

		void setRadarOverview(boolean radarOverview) {
			this.radarOverview = radarOverview;
		}

		public List<SliderCompareWrapper> getSliders() {
			return sliders;
		}

		void setSliders(List<SliderCompareWrapper> sliders) {
			this.sliders = sliders;
		}

		public RadarChartElement getRadarEl() {
			return radarEl;
		}

		void setRadarEl(RadarChartElement radarEl) {
			this.radarEl = radarEl;
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
		
		public int getStepInPercent() {
			if(rubric.getSliderType() == SliderType.discrete) {
				int steps = rubric.getSteps();
				int stepInPercent = Math.round(100.0f / steps) - 1;
				return stepInPercent;
			}
			return 0;
		}

	}
	
	public final static class SliderCompareWrapper {
		
		private final Slider slider;
		private final SliderOverviewElement overviewEl;

		public SliderCompareWrapper(Slider slider, SliderOverviewElement overviewEl) {
			this.slider = slider;
			this.overviewEl = overviewEl;
		}
		
		public String getStartLabel() {
			String start = slider.getStartLabel();
			return start == null ? "" : start;
		}
		
		public String getEndLabel() {
			String end = slider.getEndLabel();
			return end == null ? "" : end;
		}
		
		public SliderOverviewElement getOverviewEl() {
			return overviewEl;
		}
	}

}
