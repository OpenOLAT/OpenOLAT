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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.analysis.TemporalKey;
import org.olat.modules.quality.analysis.TrendSeries;
import org.olat.modules.quality.analysis.ui.HeatMapController.SliderWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * Initial date: 14 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SliderTrendController extends FormBasicController {

	private SingleSelection temporalGroupEl;
	private SliderTrendDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final List<SliderWrapper> sliders;
	private final AnalysisSearchParameter searchParams;
	private TemporalGroupBy temporalGroupBy = TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR;
	
	@Autowired
	private QualityAnalysisService analysisService;

	public SliderTrendController(UserRequest ureq, WindowControl wControl, List<SliderWrapper> sliders,
			AnalysisSearchParameter searchParams) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.sliders = sliders;
		this.searchParams = searchParams;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String groupPage = velocity_root + "/slider_trend_grouping.html";
		FormLayoutContainer groupingCont = FormLayoutContainer.createCustomFormLayout("grouping", getTranslator(), groupPage);
		flc.add("grouping", groupingCont);
		
		KeyValues temporalKV = AnalysisUIFactory.getTemporalGroupByKeyValues(getTranslator());
		temporalGroupEl = uifactory.addDropdownSingleselect("slider.trend.group.temporal", groupingCont,
				temporalKV.keys(), temporalKV.values());
		temporalGroupEl.addActionListener(FormEvent.ONCHANGE);
		String key = AnalysisUIFactory.getKey(temporalGroupBy);
		if (Arrays.asList(temporalGroupEl.getKeys()).contains(key)) {
			temporalGroupEl.select(key, true);
		}
		
		updateTrendDiagram();
	}

	private void updateTrendDiagram() {
		MultiTrendSeries<String> multiTrendSeries = loadData();
		List<String> temporalHeaders = getTemporalHeaders(multiTrendSeries);
		updateTable(temporalHeaders);
		updateModel(multiTrendSeries);
	}

	private void updateTable(List<String> temporalHeaders) {
		int columnIndex = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("slider.trend.table.title.question", columnIndex++));
		for (String header: temporalHeaders) {
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("slider.trend.table.title.question", columnIndex++);
			columnModel.setHeaderLabel(header);
			columnModel.setCellRenderer(new TrendRenderer());
			columnsModel.addFlexiColumnModel(columnModel);
		}
		
		dataModel = new SliderTrendDataModel(columnsModel, getLocale());
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), flc);
		tableEl.setElementCssClass("o_qual_slider_trend");
		tableEl.setEmtpyTableMessageKey("slider.trend.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}

	private MultiTrendSeries<String> loadData() {
		List<String> identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		List<Rubric> rubrics = sliders.stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		return analysisService.calculateIdentifierTrends(searchParams, identifiers, rubrics, temporalGroupBy);
	}

	private void updateModel(MultiTrendSeries<String> multiTrendSeries) {
		List<SliderTrendRow> rows = new ArrayList<>();
		for (SliderWrapper slider : sliders) {
			String question = slider.getLabel();
			TrendSeries trendSeries = multiTrendSeries.getSeries(slider.getIdentifier());
			SliderTrendRow row = new SliderTrendRow(question, trendSeries);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<String> getTemporalHeaders(MultiTrendSeries<String> multiTrendSeries) {
		List<TemporalKey> temporalKeys = multiTrendSeries.getTemporalKeys();
		List<String> headers = new ArrayList<>(temporalKeys.size());
		for (TemporalKey temporalKey : temporalKeys) {
			String header = TemporalKey.NO_VALUE == temporalKey.getYearPart()
					? translate("slider.trend.table.year", new String[] { Integer.toString(temporalKey.getYear()) })
					: translate("slider.trend.table.year.part", new String[] {
							Integer.toString(temporalKey.getYear()), AnalysisUIFactory.formatYearPart(temporalKey.getYearPart()) });
			headers.add(header);
		}
		return headers;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == temporalGroupEl) {
			setTemporalGroupBy();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setTemporalGroupBy() {
		temporalGroupBy = temporalGroupEl.isOneSelected()
			? AnalysisUIFactory.getTemporalGroupBy(temporalGroupEl.getSelectedKey())
			: null;
		updateTrendDiagram();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
