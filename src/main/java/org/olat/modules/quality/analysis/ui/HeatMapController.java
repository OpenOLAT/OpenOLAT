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
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
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
	private FormLayoutContainer legendLayout;
	
	// This list is the master for the sort order
	private final List<SliderWrapper> sliders;
	
	private AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
	private GroupBy groupBy;
	private final boolean insufficientConfigured;
	private boolean insufficientOnly = false;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public HeatMapController(UserRequest ureq, WindowControl wControl, Form evaluationForm) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.sliders = initSliders(evaluationForm);
		this.insufficientConfigured = initInsufficientConfigured(evaluationForm);
		initForm(ureq);
	}

	private List<SliderWrapper> initSliders(Form evaluationForm) {
		int counter = 1;
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		for (AbstractElement element : evaluationForm.getElements()) {
			if (element instanceof Rubric) {
				Rubric rubric = (Rubric) element;
				for (Slider slider : rubric.getSliders()) {
					String labelCode = translate("heatmap.table.slider.header", new String[] { Integer.toString(counter++) });
					String label = getLabel(slider);
					SliderWrapper sliderWrapper = new SliderWrapper(rubric, slider, labelCode, label);
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

	private boolean initInsufficientConfigured(Form evaluationForm) {
		for (AbstractElement element : evaluationForm.getElements()) {
			if (element instanceof Rubric) {
				Rubric rubric = (Rubric) element;
				if (rubric.getLowerBoundInsufficient() != null && rubric.getUpperBoundInsufficient() != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String groupPage = velocity_root + "/heatmap_grouping.html";
		FormLayoutContainer grouping = FormLayoutContainer.createCustomFormLayout("grouping", getTranslator(), groupPage);
		flc.add("grouping", grouping);
		// Group by selection
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
		groupEl = uifactory.addDropdownSingleselect("heatmap.group", grouping, groupKeys, groupValues);
		groupEl.addActionListener(FormEvent.ONCHANGE);
		setGroupBy();
		
		// Insufficient filter
		insufficientEl = uifactory.addCheckboxesVertical("heatmap.insufficient", grouping, INSUFFICIENT_KEYS,
				translateAll(getTranslator(), INSUFFICIENT_KEYS), 1);
		insufficientEl.addActionListener(FormEvent.ONCHANGE);
		insufficientEl.setVisible(insufficientConfigured);
		
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
		
		// legend
		if (legendLayout != null) flc.remove(legendLayout);
		String legendPage = velocity_root + "/heatmap_legend.html";
		legendLayout = FormLayoutContainer.createCustomFormLayout("legend", getTranslator(), legendPage);
		flc.add("legend", legendLayout);
		legendLayout.contextPut("sliders", sliders);
	}

	private void addSliderColumns(FlexiTableColumnModel columnsModel, int columnIndex, int maxCount) {
		for (SliderWrapper sliderWrapper : sliders) {
			Rubric rubric = sliderWrapper.getRubric();
			DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("", columnIndex++,
					new HeatMapRenderer(rubric, maxCount));
			columnModel.setHeaderLabel(sliderWrapper.getLabelCode());
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
		if (insufficientEl.isVisible() && insufficientEl.isAtLeastSelected(1)) {
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
		MultiGroupBy multiGroupBy = new MultiGroupBy(groupBy, null, null);
		GroupedStatistics statistics = analysisService.calculateStatistics(searchParams, identifiers, rubrics, multiGroupBy);
		
		for (HeatMapRow row : rows) {
			List<GroupedStatistic> rowStatistics = new ArrayList<>();
			// Iterate over the identifiers to sort the statistics according to the headers.
			for (String identifier : identifiers) {
				GroupedStatistic rowStatistic = statistics.getStatistic(identifier, MultiKey.of(row.getGroupKey()));
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
		case TOPIC_IDENTITY:
			return createTopicIdentityData();
		case TOPIC_ORGANISATION:
			return createTopicOrganisationData();
		case TOPIC_CURRICULUM:
			return createTopicCurriculumData();
		case TOPIC_CURRICULUM_ELEMENT:
			return createTopicCurriculumElementData();
		case TOPIC_REPOSITORY:
			return createTopicRepositoryData();
		case CONTEXT_ORAGANISATION:
			return createContextOraganisationData();
		case CONTEXT_CURRICULUM:
			return createContextCurriculumData();
		case CONETXT_CURRICULUM_ELEMENT:
			return createContextCurriculumElementData();
		case CONETXT_TAXONOMY_LEVEL:
			return createContextTaxonomyLevelData();
		default:
			return new HeatMapData(emptyList(), emptyList());
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
		headers.add(translate("heatmap.table.title.identity"));
		addNulls(headers, 1);
		return new HeatMapData(rows, headers);
	}

	private HeatMapData createTopicOrganisationData() {
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParams, false);
		return createOrganisationData(organisations);
	}

	private HeatMapData createContextOraganisationData() {
		List<Organisation> organisations = analysisService.loadContextOrganisations(searchParams, false);
		return createOrganisationData(organisations);
	}

	private HeatMapData createOrganisationData(List<Organisation> organisations) {
		List<HeatMapRow> rows = new ArrayList<>(organisations.size());
		for (Organisation organisation : organisations) {
			Long groupKey = organisation.getKey();
			List<String> groupNames = new ArrayList<>();
			QualityUIFactory.addParentOrganisationNames(groupNames, organisation);
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

	private HeatMapData createTopicCurriculumData() {
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParams);
		return createCurriculumData(curriculums);
	}
	
	private HeatMapData createContextCurriculumData() {
		List<Curriculum> curriculums = analysisService.loadContextCurriculums(searchParams);
		return createCurriculumData(curriculums);
	}

	private HeatMapData createCurriculumData(List<Curriculum> curriculums) {
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
		List<CurriculumElement> curriculumElements = analysisService.loadContextCurriculumElements(searchParams, false);
		return createCurriculumElements(curriculumElements);
	}

	private HeatMapData createCurriculumElements(List<CurriculumElement> curriculumElements) {
		List<HeatMapRow> rows = new ArrayList<>(curriculumElements.size());
		for (CurriculumElement curriculumElement : curriculumElements) {
			Long groupKey = curriculumElement.getKey();
			List<String> groupNames = new ArrayList<>();
			QualityUIFactory.addParentCurriculumElementNames(groupNames, curriculumElement);
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
	
	private HeatMapData createTopicRepositoryData() {
		List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(searchParams);
		List<HeatMapRow> rows = new ArrayList<>(entries.size());
		for (RepositoryEntry entry : entries) {
			Long groupKey = entry.getKey();
			List<String> groupNames = new ArrayList<>(2);
			groupNames.add(entry.getDisplayname());
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		List<String> headers = singletonList(translate("heatmap.table.title.repository"));
		return new HeatMapData(rows, headers);
	}

	private HeatMapData createContextTaxonomyLevelData() {
		List<TaxonomyLevel> levels = analysisService.loadContextTaxonomyLevels(searchParams, false);
		List<HeatMapRow> rows = new ArrayList<>(levels.size());
		for (TaxonomyLevel level : levels) {
			Long groupKey = level.getKey();
			List<String> groupNames = new ArrayList<>();
			QualityUIFactory.addParentTaxonomyLevelNames(groupNames, level);
			Collections.reverse(groupNames);
			HeatMapRow row = new HeatMapRow(groupKey, groupNames);
			rows.add(row);
		}
		
		int maxSize = getMaxGroupNamesSize(rows);
		fillGroupNamesToSameSize(rows, maxSize);
		List<String> headers = new ArrayList<>(maxSize);
		headers.add(translate("heatmap.table.title.taxonomy.level"));
		addNulls(headers, maxSize - 1);
		return new HeatMapData(rows, headers);
	}

	private int getMaxGroupNamesSize(List<HeatMapRow> rows) {
		int maxSize = 0;
		for (HeatMapRow row : rows) {
			if (maxSize < row.getGroupNamesSize()) {
				maxSize = row.getGroupNamesSize();
			}
		}
		return maxSize;
	}

	private void fillGroupNamesToSameSize(List<HeatMapRow> rows, int maxSize) {
		for (HeatMapRow row : rows) {
			if (maxSize > row.getGroupNamesSize()) {
				List<String> groupNames = row.getGroupNames();
				addNulls(groupNames, maxSize - row.getGroupNamesSize());
			}
		}
	}

	private void addNulls(List<String> list, int howMany) {
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
