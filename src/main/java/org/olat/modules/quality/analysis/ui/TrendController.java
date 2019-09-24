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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TrendController extends GroupByController {

	private GroupByDataModel dataModel;
	private MultiTrendSeries<MultiKey> multiTrendSeries;
	
	@Autowired
	private QualityAnalysisService analysisService;

	public TrendController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			FilterController filterCtrl, Form evaluationForm, AvailableAttributes availableAttributes,
			MultiGroupBy multiGroupBy, Boolean insufficientOnly, TemporalGroupBy temporalGroupBy,
			TrendDifference trendDifference, String rubricId) {
		super(ureq, wControl, stackPanel, filterCtrl, evaluationForm, availableAttributes, multiGroupBy,
				insufficientOnly, temporalGroupBy, trendDifference, rubricId);
	}

	@Override
	protected boolean showTemporalConfig() {
		return true;
	}

	@Override
	protected boolean showLegendQuestions() {
		return false;
	}

	@Override
	protected void loadStatistics() {
		multiTrendSeries = analysisService.calculateTrends(getSearchParams(), getTrendRubrics(), getMultiGroupBy(), getTemporalGroupBy());
	}

	@Override
	protected int addDataColumns(FlexiTableColumnModel columnsModel, int columnIndex) {
		for (String header: getTemporalHeaders()) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("trend.year", columnIndex++);
			columnModel.setHeaderLabel(header);
			columnModel.setCellRenderer(new TrendRenderer(getTrendDifference()));
			columnsModel.addFlexiColumnModel(columnModel);
		}
		return 0;
	}

	@Override
	protected void addTotalDataColumn(FlexiTableColumnModel columnsModel, int columnIndex) {
		//
	}
	
	private List<String> getTemporalHeaders() {
		List<TemporalKey> temporalKeys = multiTrendSeries.getTemporalKeys();
		List<String> headers = new ArrayList<>(temporalKeys.size());
		for (TemporalKey temporalKey : temporalKeys) {
			String header = TemporalKey.NO_VALUE == temporalKey.getYearPart()
					? translate("trend.year", new String[] { Integer.toString(temporalKey.getYear()) })
					: translate("trend.year.part", new String[] {
							Integer.toString(temporalKey.getYear()), AnalysisUIFactory.formatYearPart(temporalKey.getYearPart()) });
			headers.add(header);
		}
		return headers;
	}

	@Override
	protected List<? extends GroupedStatistic> getGroupedStatistcList(MultiKey multiKey) {
		return multiTrendSeries.getSeries(multiKey).toList();
	}

	@Override
	protected Set<MultiKey> getStatisticsMultiKeys() {
		return multiTrendSeries.getIdentifiers();
	}

	@Override
	protected boolean hasFooter() {
		return false;
	}

	@Override
	protected void initModel(FlexiTableColumnModel columnsModel) {
		dataModel = new GroupByDataModel(columnsModel, getLocale());
	}

	@Override
	protected FlexiTableDataModel<GroupByRow> getModel() {
		return dataModel;
	}

	@Override
	protected void setModelOjects(List<GroupByRow> rows) {
		dataModel.setObjects(rows);
	}

}
