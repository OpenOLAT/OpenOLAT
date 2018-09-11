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
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HeatMapController extends FormBasicController implements FilterableController {

	private HeatMapDataModel dataModel;
	private FlexiTableElement tableEl;
	
	// This list is the master for the sort order
	private final List<SliderWrapper> sliders;
	
	@Autowired
	private QualityAnalysisService analysisService;

	public HeatMapController(UserRequest ureq, WindowControl wControl, Form evaluationForm) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.sliders = initSliders(evaluationForm);
		initForm(ureq);
	}

	private List<SliderWrapper> initSliders(Form evaluationForm) {
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		for (AbstractElement element : evaluationForm.getElements()) {
			if (element instanceof Rubric) {
				Rubric rubric = (Rubric) element;
				for (Slider slider : rubric.getSliders()) {
					String label = getLabel(slider);
					SliderWrapper sliderWrapper = new SliderWrapper(rubric, slider, label);
					sliderWrappers.add(sliderWrapper);
					
				}
			}
		}
		return sliderWrappers;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTable(Collections.emptyList());
	}

	public void initTable(List<String> groupHeaders) {
		int columnIndex = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (groupHeaders.isEmpty()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("heatmap.table.title.group", columnIndex++));
		} else {
			for (String header : groupHeaders) {
				DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("heatmap.table.title.blank", columnIndex++);
				columnModel.setHeaderLabel(header);
				columnModel.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(columnModel);
			}
		}
		addFormColumns(columnsModel, columnIndex);
		
		dataModel = new HeatMapDataModel(columnsModel, getLocale());
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), flc);
		tableEl.setEmtpyTableMessageKey("heatmap.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}

	private void addFormColumns(FlexiTableColumnModel columnsModel, int columnIndex) {
		for (int index = 1; index <= sliders.size(); index++) {
			String header = translate("heatmap.table.slider.header", new String[] { Integer.toString(index++) });
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("", columnIndex++);
			columnModel.setHeaderLabel(header);
			columnsModel.addFlexiColumnModel(columnModel);
		}
	}

	private String getLabel(Slider slider) {
		boolean hasStartLabel = StringHelper.containsNonWhitespace(slider.getStartLabel());
		boolean hasEndLabel = StringHelper.containsNonWhitespace(slider.getEndLabel());
		if (hasStartLabel && hasEndLabel) {
			return slider.getStartLabel() + " ... " + slider.getEndLabel();
		} else if (hasStartLabel) {
			return slider.getStartLabel();
		} else if (hasEndLabel) {
			return slider.getEndLabel();
		}
		return null;
	}

	@Override
	public void onFilter(UserRequest ureq, AnalysisSearchParameter searchParams) {
		List<HeatMapRow> rows = createRows(searchParams);
		
		List<String> identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		List<Rubric> rubrics = sliders.stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		GroupedStatistics statistics = analysisService.calculateStatistics(searchParams, identifiers, rubrics,
				GroupBy.ORAGANISATION);
		
		for (HeatMapRow row : rows) {
			List<GroupedStatistic> rowStatistics = new ArrayList<>();
			// Iterate over the identifiers to sort the statistics according to the headers.
			for (String identifier : identifiers) {
				GroupedStatistic rowStatistic = statistics.getStatistic(identifier, row.getGroupKey());
				rowStatistics.add(rowStatistic);
			}
			row.setStatistics(rowStatistics);
		}
		
		List<String> headers = Collections.emptyList();
		if (!rows.isEmpty()) {
			int groupNamesSize = rows.get(0).getGroupNamesSize();
			headers = new ArrayList<>(groupNamesSize);
			headers.add(translate("heatmap.table.title.organisation"));
			for (int i = 1; i < groupNamesSize; i++) {
				headers.add(null);
			}
		}
		initTable(headers);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<HeatMapRow> createRows(AnalysisSearchParameter searchParams) {
		List<Organisation> organisations = analysisService.loadFilterOrganisations(searchParams);
		List<HeatMapRow> rows = new ArrayList<>(organisations.size());
		for (Organisation organisation : organisations) {
			Long groupKey = organisation.getKey();
			List<String> groupNames = new ArrayList<>();
			addParentNames(groupNames, organisation);
			Collections.reverse(groupNames);
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		// All group name list have to have the same size
		int maxSize = 0;
		for (HeatMapRow row : rows) {
			if (maxSize < row.getGroupNamesSize()) {
				maxSize = row.getGroupNamesSize();
			}
		}
		for (HeatMapRow row : rows) {
			if (maxSize > row.getGroupNamesSize()) {
				List<String> groupNames = row.getGroupNames();
				for (int index = row.getGroupNamesSize(); index < maxSize; index++) {
					groupNames.add(null);
				}
			}
		}
		
		return rows;
	}

	private void addParentNames(List<String> names, Organisation organisation) {
		names.add(organisation.getDisplayName());
		Organisation parent = organisation.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private final static class SliderWrapper {
		
		private final Rubric rubric;
		private final Slider slider;
		private final String label;
		
		public SliderWrapper(Rubric rubric, Slider slider, String label) {
			this.rubric = rubric;
			this.slider = slider;
			this.label = label;
		}

		public Rubric getRubric() {
			return rubric;
		}
		
		public String getIdentifier() {
			return slider.getId();
		}

		public Slider getSlider() {
			return slider;
		}

		public String getLabel() {
			return label;
		}
	}

}
