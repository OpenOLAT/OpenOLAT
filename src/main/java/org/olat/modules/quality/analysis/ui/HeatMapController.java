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
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.olat.core.gui.components.util.KeyValues;
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
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.QualityAnalysisService;
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
	
	private SingleSelection groupEl1;
	private SingleSelection groupEl2;
	private SingleSelection groupEl3;
	private MultipleSelectionElement insufficientEl;
	private HeatMapDataModel dataModel;
	private FlexiTableElement tableEl;
	private FormLayoutContainer legendLayout;
	
	// This list is the master for the sort order
	private final List<SliderWrapper> sliders;
	private final AvailableAttributes availableAttributes;
	private AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
	private MultiGroupBy multiGroupBy;
	private final boolean insufficientConfigured;
	private boolean insufficientOnly = false;
	
	private Map<String, String> groupNamesTopicIdentity;
	private Map<String, String> groupNamesTopicOrganisation;
	private Map<String, String> groupNamesTopicCurriculum;
	private Map<String, String> groupNamesTopicCurriculumElement;
	private Map<String, String> groupNamesTopicRepositoryEntry;
	private Map<String, String> groupNamesContextOrganisation;
	private Map<String, String> groupNamesContextCurriculum;
	private Map<String, String> groupNamesContextCurriculumElement;
	private Map<String, String> groupNamesContextTaxonomyLevel;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public HeatMapController(UserRequest ureq, WindowControl wControl, Form evaluationForm,
			AvailableAttributes availableAttributes, MultiGroupBy multiGroupBy) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.availableAttributes = availableAttributes;
		this.multiGroupBy = multiGroupBy;
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
		// Group by
		KeyValues groupByKV = initGroupByKeyValues();
		String[] groupKeys = groupByKV.keys();
		String[] groupValues = groupByKV.values();
		if (multiGroupBy.isNoGroupBy()) {
			GroupBy groupBy1 = GroupBy.valueOf(groupKeys[0]);
			multiGroupBy = MultiGroupBy.of(groupBy1);
		}
		groupEl1 = uifactory.addDropdownSingleselect("heatmap.group1", grouping, groupKeys, groupValues);
		groupEl1.addActionListener(FormEvent.ONCHANGE);
		selectGroupBy(groupEl1, multiGroupBy.getGroupBy1());
		groupEl2 = uifactory.addDropdownSingleselect("heatmap.group2", grouping, groupKeys, groupValues);
		groupEl2.setAllowNoSelection(true);
		groupEl2.addActionListener(FormEvent.ONCHANGE);
		selectGroupBy(groupEl2, multiGroupBy.getGroupBy2());
		groupEl3 = uifactory.addDropdownSingleselect("heatmap.group3", grouping, groupKeys, groupValues);
		groupEl3.setAllowNoSelection(true);
		groupEl3.addActionListener(FormEvent.ONCHANGE);
		selectGroupBy(groupEl3, multiGroupBy.getGroupBy3());
		
		// Insufficient filter
		insufficientEl = uifactory.addCheckboxesVertical("heatmap.insufficient", grouping, INSUFFICIENT_KEYS,
				translateAll(getTranslator(), INSUFFICIENT_KEYS), 1);
		insufficientEl.addActionListener(FormEvent.ONCHANGE);
		insufficientEl.setVisible(insufficientConfigured);
		
		// Heat map
		initTable(Collections.emptyList(), 0);
		
	}

	private void selectGroupBy(SingleSelection groupEl, GroupBy groupBy) {
		if (groupBy != null) {
			String groupByKey = groupBy.name();
			if (Arrays.asList(groupEl.getKeys()).contains(groupByKey)) {
				groupEl.select(groupByKey, true);
			}
		}
	}
	
	private KeyValues initGroupByKeyValues() {
		KeyValues keyValues = new KeyValues();
		if (availableAttributes.isTopicIdentity()) {
			addEntry(keyValues, GroupBy.TOPIC_IDENTITY);
		}
		if (availableAttributes.isTopicOrganisation() && organisationModule.isEnabled()) {
			addEntry(keyValues, GroupBy.TOPIC_ORGANISATION);
		}
		if (availableAttributes.isTopicCurriculum() && curriculumModule.isEnabled()) {
			addEntry(keyValues, GroupBy.TOPIC_CURRICULUM);
		}
		if (availableAttributes.isTopicCurriculumElement() && curriculumModule.isEnabled()) {
			addEntry(keyValues, GroupBy.TOPIC_CURRICULUM_ELEMENT);
		}
		if (availableAttributes.isTopicRepository()) {
			addEntry(keyValues, GroupBy.TOPIC_REPOSITORY);
		}
		if (availableAttributes.isContextOrganisation() && organisationModule.isEnabled()) {
			addEntry(keyValues, GroupBy.CONTEXT_ORGANISATION);
		}
		if (availableAttributes.isContextCurriculum() && curriculumModule.isEnabled()) {
			addEntry(keyValues, GroupBy.CONTEXT_CURRICULUM);
		}
		if (availableAttributes.isContextCurriculumElement() && curriculumModule.isEnabled()) {
			addEntry(keyValues, GroupBy.CONTEXT_CURRICULUM_ELEMENT);
		}
		if (availableAttributes.isContextTaxonomyLevel()) {
			addEntry(keyValues, GroupBy.CONTEXT_TAXONOMY_LEVEL);
		}
		if (availableAttributes.isContextLocation()) {
			addEntry(keyValues, GroupBy.CONTEXT_LOCATION);
		}
		return keyValues;
	}

	private void addEntry(KeyValues keyValues, GroupBy groupBy) {
		keyValues.add(KeyValues.entry(groupBy.name(), translate(groupBy.i18nKey())));
	}

	private void initTable(List<String> groupHeaders, int maxCount) {
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
		if (source == groupEl1 || source == groupEl2 || source == groupEl3) {
			setGroupBy(ureq);
			loadHeatMap();
		} else if (source == insufficientEl) {
			setInsufficientOnly();
			loadHeatMap();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setGroupBy(UserRequest ureq) {
		GroupBy groupBy1 = groupEl1.isOneSelected()? GroupBy.valueOf(groupEl1.getSelectedKey()): null;
		GroupBy groupBy2 = groupEl2.isOneSelected()? GroupBy.valueOf(groupEl2.getSelectedKey()): null;
		GroupBy groupBy3 = groupEl3.isOneSelected()? GroupBy.valueOf(groupEl3.getSelectedKey()): null;
		multiGroupBy = MultiGroupBy.of(groupBy1, groupBy2, groupBy3);
		fireEvent(ureq, new AnalysisGroupingEvent(multiGroupBy));
	}

	private void setInsufficientOnly() {
		if (insufficientEl.isVisible() && insufficientEl.isAtLeastSelected(1)) {
			insufficientOnly = true;
		} else {
			insufficientOnly = false;
		}
	}
	
	private void loadHeatMap() {
		List<String> identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		GroupedStatistics statistics = loadHeatMapStatistics();
		Set<MultiKey> keys = statistics.getKeys();
		List<HeatMapRow> rows = new ArrayList<>(keys.size());
		for (MultiKey multiKey : keys) {
			List<String> groupNames = new ArrayList<>(6);
			boolean found = true;
			
			if (multiGroupBy.getGroupBy1() != null) {
				String groupName1 = translate("heatmap.not.specified");
				if (multiKey.getKey1() != null) {
					groupName1 = getGroupName(multiGroupBy.getGroupBy1(), multiKey.getKey1());
					if (groupName1 == null) {
						found = false;
					}
				}
				groupNames.add(groupName1);
			}
			if (multiGroupBy.getGroupBy2() != null) {
				String groupName2 = translate("heatmap.not.specified");
				if (multiKey.getKey2() != null) {
					groupName2 = getGroupName(multiGroupBy.getGroupBy2(), multiKey.getKey2());
					if (groupName2 == null) {
						found = false;
					}
				}
				groupNames.add(groupName2);
			}
			if (multiGroupBy.getGroupBy3() != null) {
				String groupName3 = translate("heatmap.not.specified");
				if (multiKey.getKey3() != null) {
					groupName3 = getGroupName(multiGroupBy.getGroupBy3(), multiKey.getKey3());
					if (groupName3 == null) {
						found = false;
					}
				}
				groupNames.add(groupName3);
			}
			
			if (found) {
				// Iterate over the identifiers to sort the statistics according to the headers.
				List<GroupedStatistic> rowStatistics = new ArrayList<>();
				for (String identifier : identifiers) {
					GroupedStatistic rowStatistic = statistics.getStatistic(identifier, multiKey);
					rowStatistics.add(rowStatistic);
				}
				HeatMapRow row = new HeatMapRow(multiKey, groupNames, rowStatistics);
				rows.add(row);
			}
		}
		
		if (insufficientOnly) {
			rows.removeIf(this::hasNoInsufficientAvgs);
		}
		
		List<String> headers = getHeaders();
		int maxCount = getMaxCount(rows);
		initTable(headers, maxCount);
		
		rows.sort(new GroupNameAlphabeticalComparator());
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	public List<String> getHeaders() {
		List<String> headers = new ArrayList<>();
		if (multiGroupBy.getGroupBy1() != null) {
			String header1 = getHeader(multiGroupBy.getGroupBy1());
			headers.add(header1);
		}
		if (multiGroupBy.getGroupBy2() != null) {
			String header2 = getHeader(multiGroupBy.getGroupBy2());
			headers.add(header2);
		}
		if (multiGroupBy.getGroupBy3() != null) {
			String header3 = getHeader(multiGroupBy.getGroupBy3());
			headers.add(header3);
		}
		return headers;
	}
	
	private String getHeader(GroupBy groupBy) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			return translate("heatmap.table.title.identity");
		case TOPIC_ORGANISATION:
			return translate("heatmap.table.title.organisation");
		case TOPIC_CURRICULUM:
			return translate("heatmap.table.title.curriculum");
		case TOPIC_CURRICULUM_ELEMENT:
			return translate("heatmap.table.title.curriculum.element");
		case TOPIC_REPOSITORY:
			return translate("heatmap.table.title.repository");
		case CONTEXT_ORGANISATION:
			return translate("heatmap.table.title.organisation");
		case CONTEXT_CURRICULUM:
			return translate("heatmap.table.title.curriculum");
		case CONTEXT_CURRICULUM_ELEMENT:
			return translate("heatmap.table.title.curriculum.element");
		case CONTEXT_TAXONOMY_LEVEL:
			return translate("heatmap.table.title.taxonomy.level");
		case CONTEXT_LOCATION:
			return translate("heatmap.table.title.location");
		default:
			return null;
		}
	}
	
	private String getGroupName(GroupBy groupBy, String key) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			return getTopicIdentityGroupName(key);
		case TOPIC_ORGANISATION:
			return getTopicOrganisationGroupName(key);
		case TOPIC_CURRICULUM:
			return getTopicCurriculumGroupName(key);
		case TOPIC_CURRICULUM_ELEMENT:
			return getTopicCurriculumElementGroupName(key);
		case TOPIC_REPOSITORY:
			return getTopicRepositoryEntryGroupName(key);
		case CONTEXT_ORGANISATION:
			return getContextOrganisationGroupName(key);
		case CONTEXT_CURRICULUM:
			return getContextCurriculumGroupName(key);
		case CONTEXT_CURRICULUM_ELEMENT:
			return getContextCurriculumElementGroupName(key);
		case CONTEXT_TAXONOMY_LEVEL:
			return getContextTaxonomyLevelGroupName(key);
		case CONTEXT_LOCATION:
			return key;
		default:
			return null;
		}
	}

	private String getTopicIdentityGroupName(String key) {
		return getGroupNamesTopicIdentity().get(key);
	}

	private Map<String, String> getGroupNamesTopicIdentity() {
		if (groupNamesTopicIdentity == null) {
			groupNamesTopicIdentity = new HashMap<>();
			List<IdentityShort> identities = analysisService.loadTopicIdentity(getGroupNamesSearchParams());
			for (IdentityShort identity : identities) {
				String key = identity.getKey().toString();
				String value = identity.getLastName() + " " + identity.getFirstName();
				groupNamesTopicIdentity.put(key, value);
			}
		}
		return groupNamesTopicIdentity;
	}

	private String getTopicOrganisationGroupName(String key) {
		return getGroupNamesTopicOrganisation().get(key);
	}

	private Map<String, String> getGroupNamesTopicOrganisation() {
		if (groupNamesTopicOrganisation == null) {
			groupNamesTopicOrganisation = new HashMap<>();
			List<Organisation> organisations = analysisService.loadTopicOrganisations(getGroupNamesSearchParams(), false);
			for (Organisation organisation : organisations) {
				String key = organisation.getKey().toString();
				String value = organisation.getDisplayName();
				groupNamesTopicOrganisation.put(key, value);
			}
		}
		return groupNamesTopicOrganisation;
	}

	private String getTopicCurriculumGroupName(String key) {
		return getGroupNamesTopicCurriculum().get(key);
	}

	private Map<String, String> getGroupNamesTopicCurriculum() {
		if (groupNamesTopicCurriculum == null) {
			groupNamesTopicCurriculum = new HashMap<>();
			List<Curriculum> curriculums = analysisService.loadTopicCurriculums(getGroupNamesSearchParams());
			for (Curriculum curriculum : curriculums) {
				String key = curriculum.getKey().toString();
				String value = curriculum.getDisplayName();
				groupNamesTopicCurriculum.put(key, value);
			}
		}
		return groupNamesTopicCurriculum;
	}

	private String getTopicCurriculumElementGroupName(String key) {
		return getGroupNamesTopicCurriculumElement().get(key);
	}

	private Map<String, String> getGroupNamesTopicCurriculumElement() {
		if (groupNamesTopicCurriculumElement == null) {
			groupNamesTopicCurriculumElement = new HashMap<>();
			List<CurriculumElement> curriculumElements = analysisService.loadTopicCurriculumElements(getGroupNamesSearchParams());
			for (CurriculumElement curriculumElement : curriculumElements) {
				String key = curriculumElement.getKey().toString();
				String value = curriculumElement.getDisplayName();
				groupNamesTopicCurriculumElement.put(key, value);
			}
		}
		return groupNamesTopicCurriculumElement;
	}

	private String getTopicRepositoryEntryGroupName(String key) {
		return getGroupNamesTopicRepositoryEntry().get(key);
	}

	private Map<String, String> getGroupNamesTopicRepositoryEntry() {
		if (groupNamesTopicRepositoryEntry == null) {
			groupNamesTopicRepositoryEntry = new HashMap<>();
			List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(getGroupNamesSearchParams());
			for (RepositoryEntry entry : entries) {
				String key = entry.getKey().toString();
				String value = entry.getDisplayname();
				groupNamesTopicRepositoryEntry.put(key, value);
			}
		}
		return groupNamesTopicRepositoryEntry;
	}

	private String getContextOrganisationGroupName(String key) {
		return getContextOrganisationGroupNames().get(key);
	}

	private Map<String, String> getContextOrganisationGroupNames() {
		if (groupNamesContextOrganisation == null) {
			groupNamesContextOrganisation = new HashMap<>();
			List<Organisation> elements = analysisService.loadContextOrganisations(getGroupNamesSearchParams(), false);
			for (Organisation element : elements) {
				String key = element.getKey().toString();
				String value = element.getDisplayName();
				groupNamesContextOrganisation.put(key, value);
			}
		}
		return groupNamesContextOrganisation;
	}

	private String getContextCurriculumGroupName(String key) {
		return getContextCurriculumGroupNames().get(key);
	}

	private Map<String, String> getContextCurriculumGroupNames() {
		if (groupNamesContextCurriculum == null) {
			groupNamesContextCurriculum = new HashMap<>();
			List<Curriculum> elements = analysisService.loadContextCurriculums(getGroupNamesSearchParams());
			for (Curriculum element : elements) {
				String key = element.getKey().toString();
				String value = element.getDisplayName();
				groupNamesContextCurriculum.put(key, value);
			}
		}
		return groupNamesContextCurriculum;
	}

	private String getContextCurriculumElementGroupName(String key) {
		return getContextCurriculumElementGroupNames().get(key);
	}

	private Map<String, String> getContextCurriculumElementGroupNames() {
		if (groupNamesContextCurriculumElement == null) {
			groupNamesContextCurriculumElement = new HashMap<>();
			List<CurriculumElement> elements = analysisService.loadContextCurriculumElements(getGroupNamesSearchParams(), false);
			for (CurriculumElement element : elements) {
				String key = element.getKey().toString();
				String value = element.getDisplayName();
				groupNamesContextCurriculumElement.put(key, value);
			}
		}
		return groupNamesContextCurriculumElement;
	}

	private String getContextTaxonomyLevelGroupName(String key) {
		return getContextTaxonomyLevelGroupNames().get(key);
	}

	private Map<String, String> getContextTaxonomyLevelGroupNames() {
		if (groupNamesContextTaxonomyLevel == null) {
			groupNamesContextTaxonomyLevel = new HashMap<>();
			List<TaxonomyLevel> elements = analysisService.loadContextTaxonomyLevels(getGroupNamesSearchParams(), false);
			for (TaxonomyLevel element : elements) {
				String key = element.getKey().toString();
				String value = element.getDisplayName();
				groupNamesContextTaxonomyLevel.put(key, value);
			}
		}
		return groupNamesContextTaxonomyLevel;
	}

	private AnalysisSearchParameter getGroupNamesSearchParams() {
		AnalysisSearchParameter groupNameParams = new AnalysisSearchParameter();
		groupNameParams.setFormEntryRef(searchParams.getFormEntryRef());
		return groupNameParams;
	}
	
	public GroupedStatistics loadHeatMapStatistics() {
		List<String> identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		List<Rubric> rubrics = sliders.stream().map(SliderWrapper::getRubric).distinct().collect(toList());
		return analysisService.calculateStatistics(searchParams, identifiers, rubrics, multiGroupBy);
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

}
