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
package org.olat.modules.quality.analysis.ui;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.RubricsComparison;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.HeatMapStatistic;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeatMapController extends GroupByController {
	
	private final boolean identicalRubrics;
	private FooterGroupByDataModel dataModel;
	private GroupedStatistics<GroupedStatistic> statistics;
	private int maxCount;
	
	@Autowired
	private QualityAnalysisService analysisService;

	public HeatMapController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			FilterController filterCtrl, Form evaluationForm, AvailableAttributes availableAttributes,
			MultiGroupBy multiGroupBy, Boolean insufficientOnly, TemporalGroupBy temporalGroupBy,
			TrendDifference trendDifference, String rubricId) {
		super(ureq, wControl, stackPanel, filterCtrl, evaluationForm, availableAttributes, multiGroupBy,
				insufficientOnly, temporalGroupBy, trendDifference, rubricId);
		List<Rubric> rubrics = getSliders().stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		this.identicalRubrics = RubricsComparison.areIdentical(rubrics, identicalRubricsAttributes);
	}

	@Override
	protected boolean showTemporalConfig() {
		return false;
	}

	@Override
	protected boolean showLegendQuestions() {
		return true;
	}
	
	@Override
	protected void loadStatistics() {
		List<Rubric> rubrics = getSliders().stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		statistics = analysisService.calculateStatistics(getSearchParams(), getIdentifiers(), rubrics, getMultiGroupBy());
		maxCount = getMaxCount();
	}
	
	private int getMaxCount() {
		long tempMaxCount = 0;
		for (GroupedStatistic statistic : statistics.getStatistics()) {
			if (!MultiKey.none().equals(statistic.getMultiKey())) {
				Long count = statistic.getCount();
				if (count > tempMaxCount) {
					tempMaxCount = count;
				}
			}
		}
		return Long.valueOf(tempMaxCount).intValue();
	}
	
	@Override
	protected int addDataColumns(FlexiTableColumnModel columnsModel, int columnIndex) {
		for (SliderWrapper sliderWrapper : getSliders()) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("", columnIndex++,
					HeatMapRenderer.variableSize(maxCount));
			columnModel.setHeaderLabel(sliderWrapper.getLabelCode());
			columnModel.setFooterCellRenderer(HeatMapRenderer.fixedSize());
			columnsModel.addFlexiColumnModel(columnModel);
		}
		return columnIndex;
	}
	

	@Override
	protected void addTotalDataColumn(FlexiTableColumnModel columnsModel, int columnIndex) {
		if (identicalRubrics) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("", columnIndex,
					HeatMapRenderer.variableSize(maxCount));
			columnModel.setHeaderLabel(translate("heatmap.table.title.average"));
			columnModel.setFooterCellRenderer(HeatMapRenderer.fixedSize());
			columnsModel.addFlexiColumnModel(columnModel);
		}
	}
	
	@Override
	protected List<? extends GroupedStatistic> getGroupedStatistcList(MultiKey multiKey) {
		// Iterate over the identifiers to sort the statistics according to the headers.
		List<GroupedStatistic> rowStatistics = new ArrayList<>();
		for (String identifier : getIdentifiers()) {
			GroupedStatistic rowStatistic = statistics.getStatistic(identifier, multiKey);
			rowStatistics.add(rowStatistic);
		}
		return rowStatistics;
	}

	@Override
	protected Set<MultiKey> getStatisticsMultiKeys() {
		return statistics.getMultiKeys();
	}

	@Override
	protected boolean hasFooter() {
		return true;
	}

	@Override
	protected void initModel(FlexiTableColumnModel columnsModel) {
		dataModel = new FooterGroupByDataModel(columnsModel, getLocale(), translate("heatmap.footer.title"));
	}

	@Override
	protected FlexiTableDataModel<GroupByRow> getModel() {
		return dataModel;
	}

	@Override
	protected void setModelOjects(List<GroupByRow> rows) {
		appendTotalColumn(rows);
		List<HeatMapStatistic> footerStatistics = getFooterStatistics(rows);
		HeatMapStatistic footerTotal = getFooterTotal(rows);
		dataModel.setObjects(rows, footerStatistics, footerTotal);
	}

	private void appendTotalColumn(List<GroupByRow> rows) {
		if (identicalRubrics) {
			List<Rubric> rubrics = getSliders().stream().map(SliderWrapper::getRubric).distinct().collect(toList());
			for (GroupByRow row : rows) {
				List<? extends GroupedStatistic> rowStatistics = row.getStatistics();
				HeatMapStatistic total = analysisService.calculateRubricsTotal(rowStatistics, rubrics);
				row.setTotal(total);
			}
		}
	}

	private List<HeatMapStatistic> getFooterStatistics(List<GroupByRow> rows) {
		List<HeatMapStatistic> footerStatistics = new ArrayList<>();
		if (!rows.isEmpty()) {
			int statisticsSize = rows.get(0).getStatisticsSize();
			for (int i = 0; i < statisticsSize; i++) {
				Rubric rubric = null;
				ArrayList<HeatMapStatistic> columnStatistics = new ArrayList<>(rows.size());
				for (GroupByRow row : rows) {
					GroupedStatistic columnStatistic = row.getStatistic(i);
					columnStatistics.add(columnStatistic);
					if (rubric == null && columnStatistic != null) {
						rubric = getRubric(columnStatistic.getIdentifier());
					}
				}
				HeatMapStatistic total = analysisService.calculateSliderTotal(columnStatistics, rubric);
				footerStatistics.add(total);
			}
		}
		return footerStatistics;
	}

	private HeatMapStatistic getFooterTotal(List<GroupByRow> rows) {
		HeatMapStatistic total = null;
		if (identicalRubrics) {
			ArrayList<HeatMapStatistic> columnStatistics = new ArrayList<>(rows.size());
			for (GroupByRow row : rows) {
				Object rowTotal = row.getTotal();
				if (rowTotal instanceof HeatMapStatistic) {
					columnStatistics.add((HeatMapStatistic)rowTotal);
				}
			}
			Optional<Rubric> rubric = getSliders().stream().map(SliderWrapper::getRubric).findFirst();
			if (rubric.isPresent()) {
				total = analysisService.calculateSliderTotal(columnStatistics, rubric.get());
			}
		}
		return total;
	}

}
