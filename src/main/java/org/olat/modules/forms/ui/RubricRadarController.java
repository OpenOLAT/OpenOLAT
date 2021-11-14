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
import java.util.stream.Collectors;

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
import org.olat.modules.forms.Limit;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.manager.EvaluationFormReportDAO;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.ReportHelper.Legend;
import org.olat.modules.forms.ui.component.SliderOverviewElement;
import org.olat.modules.forms.ui.component.SliderPoint;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricRadarController extends FormBasicController {

	private final Rubric rubric;
	private final ReportHelper reportHelper;
	private final Map<String, List<EvaluationFormResponse>> identifierToResponses;
	
	@Autowired
	private EvaluationFormReportDAO reportDAO;

	public RubricRadarController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			SessionFilter filter, ReportHelper reportHelper) {
		super(ureq, wControl, "rubric_radar");
		this.rubric = rubric;
		this.reportHelper = reportHelper;
		List<String> responseIdentifiers = rubric.getSliders().stream().map(Slider::getId).collect(Collectors.toList());
		identifierToResponses = reportDAO.getResponses(responseIdentifiers , filter, Limit.all()).stream()
				.collect(Collectors.groupingBy(EvaluationFormResponse::getResponseIdentifier));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("rubricWrapper", createWrapper());
	}

	private RubricWrapper createWrapper() {
		if(rubric.getSliders().size() > 2) {
			return createRadarRubricWrapper();
		}
		return createRubricWrapper();
	}

	private RubricWrapper createRadarRubricWrapper() {
		RubricWrapper wrapper = new RubricWrapper(rubric);
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
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			if(responses != null && responses.size() > 0) {
				for(EvaluationFormResponse response: responses) {
					EvaluationFormSession responseSession = response.getSession();
					if(!series.containsKey(responseSession)) {
						Legend legend = reportHelper.getLegend(responseSession);
						series.put(responseSession, new RadarSeries(legend.getName(), legend.getColor()));
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

	private RubricWrapper createRubricWrapper() {
		RubricWrapper wrapper = new RubricWrapper(rubric);
		wrapper.setSliderOverview(true);
		List<Slider> sliders = rubric.getSliders();
		List<SliderWrapper> sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			String responseIdentifier = slider.getId();
			List<EvaluationFormResponse> responses = identifierToResponses.get(responseIdentifier);
			SliderWrapper sliderWrapper = createSliderCompareWrapper(slider, responses);
			sliderWrappers.add(sliderWrapper);
		}
		wrapper.setSliders(sliderWrappers);
		return wrapper;
	}
	
	private SliderWrapper createSliderCompareWrapper(Slider slider, List<EvaluationFormResponse> responses) {
		String id = "overview_" + CodeHelper.getRAMUniqueID();
		SliderOverviewElement overviewEl = new SliderOverviewElement(id);
		overviewEl.setMinValue(rubric.getStart());
		overviewEl.setMaxValue(rubric.getEnd());
		flc.add(id, overviewEl);
		
		List<SliderPoint> values = new ArrayList<>();
		if(responses != null && responses.size() > 0) {
			for(EvaluationFormResponse response: responses) {
				if(response.getNumericalResponse() != null) {
					Legend legend = reportHelper.getLegend(response.getSession());
					String color = legend.getColor();
					double value = response.getNumericalResponse().doubleValue();
					values.add(new SliderPoint(color, value));
				}
			}
		}
		overviewEl.setValues(values);
		
		return new SliderWrapper(slider, overviewEl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public final static class RubricWrapper {

		private final Rubric rubric;
		private boolean sliderOverview;
		private boolean radarOverview;
		private List<SliderWrapper> sliders;
		private RadarChartElement radarEl;
		
		public RubricWrapper(Rubric rubric) {
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

		public List<SliderWrapper> getSliders() {
			return sliders;
		}

		void setSliders(List<SliderWrapper> sliders) {
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
	
	public final static class SliderWrapper {
		
		private final Slider slider;
		private final SliderOverviewElement overviewEl;

		public SliderWrapper(Slider slider, SliderOverviewElement overviewEl) {
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
