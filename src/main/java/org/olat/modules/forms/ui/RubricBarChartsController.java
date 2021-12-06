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
import org.olat.core.gui.components.chart.BarSeries.BarPoint;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.component.ResponsiveBarChartComponent;

/**
 * 
 * Initial date: 19.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class RubricBarChartsController extends FormBasicController {

	private final Rubric rubric;
	private final SessionFilter filter;

	public RubricBarChartsController(UserRequest ureq, WindowControl wControl, Rubric rubric, SessionFilter filter) {
		super(ureq, wControl, "rubric_bar_charts");
		this.rubric = rubric;
		this.filter = filter;
	}

	protected abstract RubricWrapper createRubricWrapper();
	
	public Rubric getRubric() {
		return rubric;
	}

	public SessionFilter getFilter() {
		return filter;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("rubricWrapper", createRubricWrapper());
	}

	protected SliderWrapper createSliderWrapper(String startLabel, String endLabel, SliderStatistic sliderStatistic) {
		String barChartName = createChartAndGetName(sliderStatistic);
		String tableName = createTableAndGetName(sliderStatistic);
		return new SliderWrapper(startLabel, endLabel, barChartName, tableName);
	}

	private String createChartAndGetName(SliderStatistic sliderStatistic) {
		String barChartName = "o_eve_bc_" + CodeHelper.getRAMUniqueID();
		ResponsiveBarChartComponent chart = new ResponsiveBarChartComponent(barChartName);
		chart.setYLegend(translate("chart.count"));
		BarSeries barSeries = createBarSeries(sliderStatistic);
		Double max = barSeries.getPoints().stream().map(BarPoint::getValue).max(Double::compare).orElse(1.0);
		chart.addSeries(barSeries);
		chart.setYMax(max);
		chart.setYMin(0.0);
		flc.put(barChartName, chart);
		return barChartName;
	}

	private BarSeries createBarSeries(SliderStatistic sliderStatistic) {
		BarSeries series = new BarSeries("o_eva_bar");
		for (int step = 1; step <= rubric.getSteps(); step++) {
			Long count = sliderStatistic.getStepCount(step);
			double stepValue = rubric.getScaleType().getStepValue(rubric.getSteps(), step);
			String stepName = EvaluationFormFormatter.formatZeroOrOneDecimals(stepValue);
			series.add(count, stepName);
		}
		return series;
	}

	private String createTableAndGetName(SliderStatistic sliderStatistic) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatisticCols.term));
		DefaultFlexiColumnModel valueColumn = new DefaultFlexiColumnModel(StatisticCols.value);
		valueColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		valueColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		valueColumn.setFooterCellRenderer(new RubricAvgRenderer(rubric));
		columnsModel.addFlexiColumnModel(valueColumn);
		
		String footerHeader = translate("rubric.report.avg.title");
		StatisticDataModel dataModel = new StatisticDataModel(columnsModel, footerHeader);
		List<StatisticRow> objects = getStatisticRows(sliderStatistic);
		dataModel.setObjects(objects, sliderStatistic.getAvg());
		String tableName = "ru_" + CodeHelper.getRAMUniqueID();
		FlexiTableElement tableEl = uifactory.addTableElement(getWindowControl(), tableName, dataModel, getTranslator(), flc);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setFooter(true);
		return tableName;
	}

	private List<StatisticRow> getStatisticRows(SliderStatistic sliderStatistic) {
		List<StatisticRow> rows = new ArrayList<>();
		if (rubric.isNoResponseEnabled()) {
			rows.add(new StatisticRow(translate("rubric.report.number.no.responses.title"), StringHelper.toStringOrBlank(sliderStatistic.getNumberOfNoResponses())));
		}
		rows.add(new StatisticRow(translate("rubric.report.number.responses.title"), StringHelper.toStringOrBlank(sliderStatistic.getNumberOfResponses())));
		rows.add(new StatisticRow(translate("rubric.report.median.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getMedian())));
		rows.add(new StatisticRow(translate("rubric.report.variance.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getVariance())));
		rows.add(new StatisticRow(translate("rubric.report.sdtdev.title"), EvaluationFormFormatter.formatDouble(sliderStatistic.getStdDev())));
		return rows;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public final static class RubricWrapper {
		
		private Rubric rubric;
		private List<SliderWrapper> sliders;
		
		public RubricWrapper() {
		}
	
		public RubricWrapper(Rubric rubric) {
			this.rubric = rubric;
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
		
		private final String startLabel;
		private final String endLabel;
		private final String chartName;
		private final String tableName;
		
		SliderWrapper(String startLabel, String endLabel, String chartName, String tableName) {
			super();
			this.startLabel = startLabel;
			this.endLabel = endLabel;
			this.chartName = chartName;
			this.tableName = tableName;
		}
		
		public String getStartLabel() {
			return startLabel == null ? "" : startLabel;
		}
		
		public String getEndLabel() {
			return endLabel == null ? "" : endLabel;
		}
	
		public String getChartName() {
			return chartName;
		}
	
		public String getTableName() {
			return tableName;
		}
	}

	protected final static class StatisticRow {
			
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

	protected final static class StatisticDataModel extends DefaultFlexiTableDataModel<StatisticRow>
			implements FlexiTableFooterModel {
		
		private final String footerHeader;
		private Double average;

		StatisticDataModel(FlexiTableColumnModel columnsModel, String footerHeader) {
			super(columnsModel);
			this.footerHeader = footerHeader;
		}

		public void setObjects(List<StatisticRow> objects, Double average) {
			super.setObjects(objects);
			this.average = average;
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
		public String getFooterHeader() {
			return footerHeader;
		}

		@Override
		public Object getFooterValueAt(int col) {
			return col == 1? average: null;
		}
	}

	protected enum StatisticCols implements FlexiColumnDef {
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