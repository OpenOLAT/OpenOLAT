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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.analysis.GroupBy.CONETXT_CURRICULUM_ELEMENT;
import static org.olat.modules.quality.analysis.GroupBy.CONTEXT_CURRICULUM;
import static org.olat.modules.quality.analysis.GroupBy.CONTEXT_ORAGANISATION;
import static org.olat.modules.quality.analysis.GroupBy.TOPIC_CURRICULUM;
import static org.olat.modules.quality.analysis.GroupBy.TOPIC_CURRICULUM_ELEMENT;
import static org.olat.modules.quality.analysis.GroupBy.TOPIC_ORGANISATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
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

	private static final String[] INSUFFICIENT_KEYS = new String[] {"heatmap.insufficient.select"};
	
	private SingleSelection groupEl;
	private MultipleSelectionElement insufficientEl;
	private HeatMapDataModel dataModel;
	private FlexiTableElement tableEl;
	
	// This list is the master for the sort order
	private final List<SliderWrapper> sliders;
	
	private AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
	private GroupBy groupBy;
	private boolean insufficientOnly;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer groupByLayout = FormLayoutContainer.createDefaultFormLayout("groupByLayout", getTranslator());
		// Group by selection
		flc.add("groupByLayout", groupByLayout);
		List<GroupBy> values = new ArrayList<>(asList(GroupBy.values()));
		if (!organisationModule.isEnabled()) {
			values.removeIf(value -> asList(TOPIC_ORGANISATION, CONTEXT_ORAGANISATION).contains(value));
		}
		if (!curriculumModule.isEnabled()) {
			values.removeIf(value -> asList(TOPIC_CURRICULUM, TOPIC_CURRICULUM_ELEMENT, CONTEXT_CURRICULUM,
					CONETXT_CURRICULUM_ELEMENT).contains(value));
		}
		String[] groupKeys = new String[values.size()];
		String[] groupValues = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			GroupBy groupBy = values.get(i);
			groupKeys[i] = groupBy.name();
			groupValues[i] = translate(groupBy.i18nKey());
		}
		groupEl = uifactory.addDropdownSingleselect("heatmap.group", groupByLayout, groupKeys, groupValues);
		groupEl.addActionListener(FormEvent.ONCHANGE);
		setGroupBy();
		
		// Insufficient filter
		insufficientEl = uifactory.addCheckboxesVertical("heatmap.insufficient", groupByLayout, INSUFFICIENT_KEYS,
				translateAll(getTranslator(), INSUFFICIENT_KEYS), 1);
		insufficientEl.addActionListener(FormEvent.ONCHANGE);
		
		// Heat map
		initTable(Collections.emptyList(), 0);
	}

	public void initTable(List<String> groupHeaders, int maxCount) {
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
		addSliderColumns(columnsModel, columnIndex, maxCount);
		
		dataModel = new HeatMapDataModel(columnsModel, getLocale());
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), flc);
		tableEl.setElementCssClass("o_qual_hm");
		tableEl.setEmtpyTableMessageKey("heatmap.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}

	private void addSliderColumns(FlexiTableColumnModel columnsModel, int columnIndex, int maxCount) {
		for (int sliderIndex = 0; sliderIndex < sliders.size(); sliderIndex++) {
			String header = translate("heatmap.table.slider.header", new String[] { Integer.toString(sliderIndex + 1) });
			Rubric rubric = sliders.get(sliderIndex).getRubric();
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("", columnIndex++,
					new HeatMapRenderer(rubric, maxCount));
			columnModel.setHeaderLabel(header);
			columnsModel.addFlexiColumnModel(columnModel);
		}
	}

	@Override
	public void onFilter(UserRequest ureq, AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
		loadHeatMap();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == groupEl) {
			setGroupBy();
			loadHeatMap();
		} else if (source == insufficientEl) {
			setInsufficientOnly();
			loadHeatMap();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setGroupBy() {
		if (groupEl.isOneSelected()) {
			String selectedKey = groupEl.getSelectedKey();
			groupBy = GroupBy.valueOf(selectedKey);
		} else {
			groupBy = GroupBy.valueOf(groupEl.getKey(0));
		}
	}

	private void setInsufficientOnly() {
		if (insufficientEl.isAtLeastSelected(1)) {
			insufficientOnly = true;
		} else {
			insufficientOnly = false;
		}
	}

	
	private void loadHeatMap() {
		HeatMapData heatMapData = createHeatMapData();
		List<HeatMapRow> rows = heatMapData.getRows();
		if (!rows.isEmpty()) {
			addHeatMapStatistics(rows);
		}
		if (insufficientOnly) {
			rows.removeIf(this::hasNoInsufficientAvgs);
		}
		int maxCount = getMaxCount(rows);
		initTable(heatMapData.getHeaders(), maxCount);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	public void addHeatMapStatistics(List<HeatMapRow> rows) {
		List<String> identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		List<Rubric> rubrics = sliders.stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		GroupedStatistics statistics = analysisService.calculateStatistics(searchParams, identifiers, rubrics, groupBy);
		

		for (HeatMapRow row : rows) {
			List<GroupedStatistic> rowStatistics = new ArrayList<>();
			// Iterate over the identifiers to sort the statistics according to the headers.
			for (String identifier : identifiers) {
				GroupedStatistic rowStatistic = statistics.getStatistic(identifier, row.getGroupKey());
				rowStatistics.add(rowStatistic);
			}
			row.setStatistics(rowStatistics);
		}
		rows.sort(new GroupNameAlphabeticalComparator());
	}

	private boolean hasNoInsufficientAvgs(HeatMapRow row) {
		for (int i = 0; i < row.getStatisticsSize(); i++) {
			GroupedStatistic statistic = row.getStatistic(i);
			if (statistic != null) {
				Double avg = statistic.getAvg();
				String identifier = statistic.getIdentifier();
				Rubric rubric = getRubric(identifier);
				boolean isInsufficient = analysisService.isInsufficient(rubric, avg);
				if (isInsufficient) {
					return false;
				}
			}
		}
		return true;
	}

	private Rubric getRubric(String identifier) {
		for (SliderWrapper sliderWrapper : sliders) {
			if (identifier.equals(sliderWrapper.getIdentifier())) {
				return sliderWrapper.getRubric();
			}
		}
		return null;
	}

	private int getMaxCount(List<HeatMapRow> rows) {
		long maxCount = 0;
		for (HeatMapRow row : rows) {
			for (int i = 0; i < row.getStatisticsSize(); i++) {
				GroupedStatistic statistic = row.getStatistic(i);
				if (statistic != null) {
					Long count = statistic.getCount();
					if (count > maxCount) {
						maxCount = count;
					}
				}
			}
		}
		return Long.valueOf(maxCount).intValue();
	}
	
	private HeatMapData createHeatMapData() {
		switch (groupBy) {
		case TOPIC_ORGANISATION:
			return createTopicOrganisationData();
		case TOPIC_CURRICULUM:
			return createTopicCurriculumData();
		case TOPIC_CURRICULUM_ELEMENT:
			return createTopicCurriculumElementData();
		case TOPIC_IDENTITY:
			return createTopicIdentityData();
		case CONTEXT_ORAGANISATION:
			return createContextOraganisationData();
		case CONTEXT_CURRICULUM:
			return createContextCurriculumData();
		case CONETXT_CURRICULUM_ELEMENT:
			return createContextCurriculumElementData();
		default:
			return new HeatMapData(emptyList(), emptyList());
		}
	}

	private HeatMapData createTopicOrganisationData() {
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParams);
		return createOrganisationData(organisations);
	}

	private HeatMapData createContextOraganisationData() {
		List<Organisation> organisations = analysisService.loadContextOrganisations(searchParams);
		return createOrganisationData(organisations);
	}

	public HeatMapData createOrganisationData(List<Organisation> organisations) {
		List<HeatMapRow> rows = new ArrayList<>(organisations.size());
		for (Organisation organisation : organisations) {
			Long groupKey = organisation.getKey();
			List<String> groupNames = new ArrayList<>();
			addParentOrganisationNames(groupNames, organisation);
			Collections.reverse(groupNames);
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		int maxSize = getMaxGroupNamesSize(rows);
		fillGroupNamesToSameSize(rows, maxSize);
		
		List<String> headers = new ArrayList<>(maxSize);
		headers.add(translate("heatmap.table.title.organisation"));
		addNulls(headers, maxSize - 1);
		return new HeatMapData(rows, headers);
	}

	private void addParentOrganisationNames(List<String> names, Organisation organisation) {
		names.add(organisation.getDisplayName());
		Organisation parent = organisation.getParent();
		if (parent != null) {
			addParentOrganisationNames(names, parent);
		}
	}

	private HeatMapData createTopicCurriculumData() {
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParams);
		return createCurriculumData(curriculums);
	}
	
	private HeatMapData createContextCurriculumData() {
		List<Curriculum> curriculums = analysisService.loadContextCurriculums(searchParams);
		return createCurriculumData(curriculums);
	}

	public HeatMapData createCurriculumData(List<Curriculum> curriculums) {
		List<HeatMapRow> rows = new ArrayList<>(curriculums.size());
		for (Curriculum curriculum : curriculums) {
			Long groupKey = curriculum.getKey();
			List<String> groupNames = singletonList(curriculum.getDisplayName());
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		List<String> headers = singletonList(translate("heatmap.table.title.curriculum"));
		return new HeatMapData(rows, headers);
	}

	private HeatMapData createTopicCurriculumElementData() {
		List<CurriculumElement> curriculumElements = analysisService.loadTopicCurriculumElements(searchParams);
		return createCurriculumElements(curriculumElements);
	}

	private HeatMapData createContextCurriculumElementData() {
		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		if (curriculumElementSearchParams.getCurriculumRefs() == null || searchParams.getCurriculumRefs().isEmpty()) {
			List<Curriculum> curriculums = analysisService.loadContextCurriculums(curriculumElementSearchParams);
			curriculumElementSearchParams.setCurriculumRefs(curriculums);
		}
		
		List<CurriculumElement> curriculumElements = analysisService.loadContextCurriculumElements(curriculumElementSearchParams, false);
		return createCurriculumElements(curriculumElements);
	}

	public HeatMapData createCurriculumElements(List<CurriculumElement> curriculumElements) {
		List<HeatMapRow> rows = new ArrayList<>(curriculumElements.size());
		for (CurriculumElement curriculumElement : curriculumElements) {
			Long groupKey = curriculumElement.getKey();
			List<String> groupNames = new ArrayList<>();
			addParentCurriculumElementNames(groupNames, curriculumElement);
			Collections.reverse(groupNames);
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		int maxSize = getMaxGroupNamesSize(rows);
		fillGroupNamesToSameSize(rows, maxSize);
		List<String> headers = new ArrayList<>(maxSize);
		headers.add(translate("heatmap.table.title.curriculum.element"));
		addNulls(headers, maxSize - 1);
		return new HeatMapData(rows, headers);
	}
	
	private void addParentCurriculumElementNames(List<String> names, CurriculumElement curriculumElement) {
		names.add(curriculumElement.getDisplayName());
		CurriculumElement parent = curriculumElement.getParent();
		if (parent != null) {
			addParentCurriculumElementNames(names, parent);
		}
	}

	private HeatMapData createTopicIdentityData() {
		List<IdentityShort> identities = analysisService.loadTopicIdentity(searchParams);
		List<HeatMapRow> rows = new ArrayList<>(identities.size());
		for (IdentityShort itentity : identities) {
			Long groupKey = itentity.getKey();
			List<String> groupNames = new ArrayList<>(2);
			groupNames.add(itentity.getLastName());
			groupNames.add(itentity.getFirstName());
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		List<String> headers = new ArrayList<>(2);
		headers.add(translate("heatmap.table.title.topic.identity"));
		addNulls(headers, 1);
		return new HeatMapData(rows, headers);
	}

	public int getMaxGroupNamesSize(List<HeatMapRow> rows) {
		int maxSize = 0;
		for (HeatMapRow row : rows) {
			if (maxSize < row.getGroupNamesSize()) {
				maxSize = row.getGroupNamesSize();
			}
		}
		return maxSize;
	}

	public void fillGroupNamesToSameSize(List<HeatMapRow> rows, int maxSize) {
		for (HeatMapRow row : rows) {
			if (maxSize > row.getGroupNamesSize()) {
				List<String> groupNames = row.getGroupNames();
				addNulls(groupNames, maxSize - row.getGroupNamesSize());
			}
		}
	}

	public void addNulls(List<String> list, int howMany) {
		for (int index = 0; index < howMany; index++) {
			list.add(null);
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
	
	private final static class HeatMapData {
		
		private final List<HeatMapRow> rows;
		private final List<String> headers;
		
		public HeatMapData(List<HeatMapRow> rows, List<String> headers) {
			this.rows = rows;
			this.headers = headers;
		}

		public List<HeatMapRow> getRows() {
			return rows;
		}

		public List<String> getHeaders() {
			return headers;
		}
	}

}
