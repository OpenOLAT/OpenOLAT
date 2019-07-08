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
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
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
					new HeatMapRenderer(maxCount));
			columnModel.setHeaderLabel(sliderWrapper.getLabelCode());
			columnsModel.addFlexiColumnModel(columnModel);
		}
		return columnIndex;
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
}
