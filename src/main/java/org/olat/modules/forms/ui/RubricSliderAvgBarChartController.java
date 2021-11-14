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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.NameDisplay;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.component.ResponsiveBarChartComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderAvgBarChartController extends BasicController {
	
	private final RubricStatistic rubricStatistic;

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public RubricSliderAvgBarChartController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			SessionFilter filter) {
		super(ureq, wControl);
		
		rubricStatistic = evaluationFormManager.getRubricStatistic(rubric, filter);
		List<SliderWrapper> sliders = initSliders(rubric);
		BarSeries barSeries = createBarSeries(sliders, rubric);
		List<LegendEntry> legend = createLegend(sliders);
		
		VelocityContainer mainVC = createVelocityContainer("bar_chart");
		String title = rubric.getNameDisplays().contains(NameDisplay.report)
				? rubric.getName()
				: translate("report.overview.total.title");
		mainVC.contextPut("title", title);
		ResponsiveBarChartComponent chart = new ResponsiveBarChartComponent("o_eve_bc_" + CodeHelper.getRAMUniqueID());
		double yMin = rubric.getScaleType().getStepValue(rubric.getSteps(), 1);
		double yMax = rubric.getScaleType().getStepValue(rubric.getSteps(), rubric.getSteps());
		double yMinTemp = yMin;
		if (yMax < yMin) {
			yMin = yMax;
			yMax = yMinTemp;
		}
		yMin = yMin > 0.0? 0.0: yMin;
		chart.setYMax(yMax);
		chart.setYMin(yMin);
		chart.setYLegend(translate("chart.avg"));
		chart.addSeries(barSeries);
		mainVC.put("chart", chart);
		mainVC.contextPut("legend", legend);
		
		putInitialPanel(mainVC);
	}

	private List<SliderWrapper> initSliders(Rubric rubric) {
		int counter = 1;
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		for (Slider slider : rubric.getSliders()) {
			String labelCode = getTranslator().translate("slider.label.code", new String[] { Integer.toString(counter++) });
			String label = EvaluationFormFormatter.formatSliderLabel(slider);
			SliderWrapper sliderWrapper = new SliderWrapper(rubric, slider, labelCode, label);
			sliderWrappers.add(sliderWrapper);
		}
		return sliderWrappers;
	}

	private BarSeries createBarSeries(List<SliderWrapper> sliders, Rubric rubric) {
		BarSeries series = new BarSeries("o_eva_bar");
		for (SliderWrapper sliderWrapper : sliders) {
			SliderStatistic statistic = rubricStatistic.getSliderStatistic(sliderWrapper.getSlider());
			double avg = statistic.getAvg() != null? statistic.getAvg().doubleValue(): 0.0;
			RubricRating rating = evaluationFormManager.getRubricRating(sliderWrapper.getRubric(), statistic.getAvg());
			series.add(avg, sliderWrapper.getLabelCode(), getRatingCss(rating));
		}
		if (sliders.size() >= 2) {
			double avg = rubricStatistic.getTotalStatistic().getAvg() != null? rubricStatistic.getTotalStatistic().getAvg().doubleValue(): 0.0;
			RubricRating rating = evaluationFormManager.getRubricRating(rubric, avg);
			series.add(avg, getTranslator().translate("slider.label.total").substring(0,1), getRatingCss(rating));
		}
		return series;
	}

	private String getRatingCss(RubricRating rating) {
		String ratingCssClass = RubricAvgRenderer.getRatingCssClass(rating);
		if (ratingCssClass == null) {
			ratingCssClass = "o_rubric_unrated";
		}
		return ratingCssClass;
	}
	
	private List<LegendEntry> createLegend(List<SliderWrapper> sliders) {
		List<LegendEntry> legend = new ArrayList<>();
		for (SliderWrapper sliderWrapper : sliders) {
			LegendEntry legendEntry = new LegendEntry(sliderWrapper.getLabelCode(), sliderWrapper.getLabel());
			legend.add(legendEntry);
		}
		if (sliders.size() >= 2) {
			String total = getTranslator().translate("slider.label.total");
			legend.add(new LegendEntry(total.substring(0,1), total));
		}
		return legend;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public final static class SliderWrapper {
		
		private final Rubric rubric;
		private final Slider slider;
		private final String labelCode;
		private final String label;
		
		public SliderWrapper(Rubric rubric, Slider slider, String labelCode, String label) {
			this.rubric = rubric;
			this.slider = slider;
			this.labelCode = labelCode;
			this.label = label;
		}

		public Rubric getRubric() {
			return rubric;
		}
		
		public Slider getSlider() {
			return slider;
		}

		public String getIdentifier() {
			return slider.getId();
		}

		public String getLabelCode() {
			return labelCode;
		}

		public String getLabel() {
			return label;
		}
	}
	
	public static final class LegendEntry {
		
		private String labelCode;
		private String label;
		
		public LegendEntry(String labelCode, String label) {
			this.labelCode = labelCode;
			this.label = label;
		}

		public String getLabelCode() {
			return labelCode;
		}

		public String getLabel() {
			return label;
		}
		
	}

}
