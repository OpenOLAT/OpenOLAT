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
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.StepLabel;
import org.olat.modules.forms.ui.component.SliderChartComponent;
import org.olat.modules.forms.ui.model.RubricStatistic;
import org.olat.modules.forms.ui.model.SliderStatistic;

/**
 * 
 * Initial date: 25.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricBarChartsController extends FormBasicController {

	private final Rubric rubric;
	private final List<? extends EvaluationFormSessionRef> sessions;
	
	public RubricBarChartsController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			List<? extends EvaluationFormSessionRef> sessions) {
		super(ureq, wControl, "rubric_bar_charts");
		this.rubric = rubric;
		this.sessions = sessions;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("rubricWrapper", createRubricWrapper());
	}
	
	private RubricWrapper createRubricWrapper() {
		RubricStatistic rubricStatistic = new RubricStatistic(rubric, sessions);
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		for (Slider slider: rubric.getSliders()) {
			SliderStatistic sliderStatistic = rubricStatistic.getSliderStatistic(slider);
			SliderWrapper sliderWrapper = createSliderWrapper(slider, sliderStatistic);
			sliderWrappers.add(sliderWrapper);
		}
		RubricWrapper rubricWrapper = new RubricWrapper(rubric);
		rubricWrapper.setSliders(sliderWrappers);
		return rubricWrapper;
	}

	private SliderWrapper createSliderWrapper(Slider slider, SliderStatistic sliderStatistic) {
		String barChartName = createChartAndGetName(sliderStatistic);
		String tableName = createTableAndGetName(sliderStatistic);
		return new SliderWrapper(slider, barChartName, tableName);
	}

	private String createChartAndGetName(SliderStatistic sliderStatistic) {
		String barChartName = "o_eve_bc_" + CodeHelper.getRAMUniqueID();
		SliderChartComponent chart = new SliderChartComponent(barChartName);
		chart.setYLegend(translate("chart.count"));
		BarSeries barSeries = createBarSeries(sliderStatistic);
		chart.addSeries(barSeries);
		flc.put(barChartName, chart);
		return barChartName;
	}

	private BarSeries createBarSeries(SliderStatistic sliderStatistic) {
		BarSeries series = new BarSeries("o_eva_bar");
		List<Long> stepCounts = sliderStatistic.getStepCounts();
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = stepCounts.get(step -1);
			double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step);
			series.add(count, stepValue);
		}
		return series;
	}

	private String createTableAndGetName(SliderStatistic sliderStatistic) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatisticCols.term));
		DefaultFlexiColumnModel valueColumn = new DefaultFlexiColumnModel(StatisticCols.value);
		valueColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(valueColumn);
		
		StatisticDataModel dataModel = new StatisticDataModel(columnsModel);
		List<StatisticRow> objects = getStatisticRows(sliderStatistic);
		dataModel.setObjects(objects );
		String tableName = "ru_" + CodeHelper.getRAMUniqueID();
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), tableName, dataModel, getTranslator(), flc);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		return tableName;
	}

	private List<StatisticRow> getStatisticRows(SliderStatistic sliderStatistic) {
		List<StatisticRow> rows = new ArrayList<>();
		rows.add(new StatisticRow(translate("rubric.report.number.no.responses.title"), String.valueOf(sliderStatistic.getNumberOfNoResponses())));
		rows.add(new StatisticRow(translate("rubric.report.number.responses.title"), String.valueOf(sliderStatistic.getNumberOfResponses())));
		rows.add(new StatisticRow(translate("rubric.report.median.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getMedian())));
		rows.add(new StatisticRow(translate("rubric.report.avg.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getAvg())));
		rows.add(new StatisticRow(translate("rubric.report.variance.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getVariance())));
		rows.add(new StatisticRow(translate("rubric.report.sdtdev.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getStdDev())));
		return rows;
	}

	@Override
	protected void formOK(UserRequest ureq) {
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
		
		public boolean isContinous() {
			return SliderType.continuous.equals(rubric.getSliderType());
		}
		
		public boolean isWithEndLabel() {
			for (Slider slider: rubric.getSliders()) {
				if (StringHelper.containsNonWhitespace(slider.getEndLabel())) {
					return true;
				}
			}
			return false;
		}
	}
		
	
	public static final class SliderWrapper {
		
		private final Slider slider;
		private final String chartName;
		private final String tableName;
		
		SliderWrapper(Slider slider, String chartName, String tableName) {
			super();
			this.slider = slider;
			this.chartName = chartName;
			this.tableName = tableName;
		}
		
		public String getStartLabel() {
			String start = slider.getStartLabel();
			return start == null ? "" : start;
		}
		
		public String getEndLabel() {
			String end = slider.getEndLabel();
			return end == null ? "" : end;
		}

		public String getChartName() {
			return chartName;
		}

		public String getTableName() {
			return tableName;
		}
	}
	
	private final static class StatisticRow {
		
		private final String term;
		private final String value;
		
		StatisticRow(String term, String value) {
			super();
			this.term = term;
			this.value = value;
		}

		public String getTerm() {
			return term;
		}

		public String getValue() {
			return value;
		}
	}
	
	private final static class StatisticDataModel extends DefaultFlexiTableDataModel<StatisticRow> {
		
		StatisticDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			StatisticRow statisticRow = getObject(row);
			switch(StatisticCols.values()[col]) {
				case term: return statisticRow.getTerm();
				case value: return statisticRow.getValue();
				default: return null;
			}
		}

		@Override
		public DefaultFlexiTableDataModel<StatisticRow> createCopyWithEmptyList() {
			return new StatisticDataModel(getTableColumnModel());
		}
	}
	
	private enum StatisticCols implements FlexiColumnDef {
		term("rubric.report.figure.title"),
		value("rubric.report.value.title");
		
		private final String i18nKey;
		
		private StatisticCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
