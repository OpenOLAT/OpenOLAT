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

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.analysis.ui.AnalysisUIFactory.getGroupBy;
import static org.olat.modules.quality.analysis.ui.AnalysisUIFactory.getKey;
import static org.olat.modules.quality.analysis.ui.AnalysisUIFactory.toLongOrZero;
import static org.olat.modules.quality.ui.QualityUIFactory.emptyArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupByKey;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.quality.model.QualityDataCollectionRefImpl;
import org.olat.modules.quality.ui.DataCollectionReportController;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class GroupByController extends FormBasicController implements FilterableController {

	private static final String CMD_GROUP_PREFIX = "CLICKED_";
	private static final String CMD_TREND = "TREND";
	private static final String[] INSUFFICIENT_KEYS = new String[] {"heatmap.insufficient.select"};
	private static final Collection<GroupBy> GROUP_BY_TOPICS = Arrays.asList(GroupBy.TOPIC_IDENTITY,
			GroupBy.TOPIC_ORGANISATION, GroupBy.TOPIC_CURRICULUM, GroupBy.TOPIC_CURRICULUM_ELEMENT,
			GroupBy.TOPIC_REPOSITORY);
	
	private BreadcrumbPanel stackPanel;
	private FormLayoutContainer groupingCont;
	private SingleSelection groupEl1;
	private SingleSelection groupEl2;
	private SingleSelection groupEl3;
	private MultipleSelectionElement insufficientEl;
	private SingleSelection temporalGroupEl;
	private SingleSelection differenceEl;
	private SingleSelection rubricEl;
	private GroupByDataModel dataModel;
	private FlexiTableElement tableEl;
	private FormLayoutContainer legendLayout;
	
	private Controller trendCtrl;
	private Controller detailCtrl;
	
	// This list is the master for the sort order of the questions (sliders).
	private final List<SliderWrapper> sliders;
	private final List<String> identifiers;
	private final AvailableAttributes availableAttributes;
	private final GroupByNameCache groupByNames;
	private AnalysisSearchParameter searchParams = new AnalysisSearchParameter();
	private MultiGroupBy multiGroupBy;
	private final boolean insufficientConfigured;
	private boolean insufficientOnly = false;
	private TemporalGroupBy temporalGroupBy = TemporalGroupBy.DATA_COLLECTION_DEADLINE_YEAR;
	private TrendDifference trendDifference = TrendDifference.NONE;
	private String rubricId;

	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private QualityService qualityService;
	
	public GroupByController(UserRequest ureq, WindowControl wControl, Form evaluationForm,
			AvailableAttributes availableAttributes, MultiGroupBy multiGroupBy, Boolean insufficientOnly,
			TemporalGroupBy temporalGroupBy, TrendDifference trendDifference, String rubricId) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.availableAttributes = availableAttributes;
		this.multiGroupBy = multiGroupBy;
		this.groupByNames = new GroupByNameCache(getLocale());
		this.insufficientOnly = insufficientOnly != null? insufficientOnly.booleanValue(): false;
		this.sliders = initSliders(evaluationForm);
		this.identifiers = sliders.stream().map(SliderWrapper::getIdentifier).collect(toList());
		this.insufficientConfigured = initInsufficientConfigured(evaluationForm);
		this.temporalGroupBy = temporalGroupBy;
		this.trendDifference = trendDifference;
		this.rubricId = rubricId;
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
					String label = AnalysisUIFactory.formatSliderLabel(slider);
					SliderWrapper sliderWrapper = new SliderWrapper(rubric, slider, labelCode, label);
					sliderWrappers.add(sliderWrapper);
				}
			}
		}
		return sliderWrappers;
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
	
	void setInsufficientOnly(boolean insufficientOnly) {
		this.insufficientOnly = insufficientOnly;
		updateUI();
	}

	public void setMultiGroupBy(MultiGroupBy multiGroupBy) {
		this.multiGroupBy = multiGroupBy;
		updateUI();
	}

	protected TemporalGroupBy getTemporalGroupBy() {
		return temporalGroupBy;
	}

	protected Set<Rubric> getTrendRubrics() {
		if (!StringHelper.containsNonWhitespace(rubricId)) {
			return getSliders().stream().map(SliderWrapper::getRubric).distinct().collect(toSet());
		}
		Set<Rubric> rubrics = new HashSet<>();
		rubrics.add(getRubricById(rubricId));
		return rubrics;
	}
	
	protected List<SliderWrapper> getSliders() {
		return sliders;
	}
	
	protected List<String> getIdentifiers() {
		return identifiers;
	}

	protected AnalysisSearchParameter getSearchParams() {
		return searchParams;
	}

	protected MultiGroupBy getMultiGroupBy() {
		return multiGroupBy;
	}
	
	protected TrendDifference getTrendDifference() {
		return trendDifference;
	}
	
	protected abstract boolean showTemporalConfig();
	
	protected abstract boolean showLegend();
	
	protected abstract void loadStatistics();

	protected abstract int addDataColumns(FlexiTableColumnModel columnsModel, int columnIndex);

	protected abstract List<? extends GroupedStatistic> getGroupedStatistcList(MultiKey multiKey);

	protected abstract Set<MultiKey> getStatisticsMultiKeys();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String groupPage = velocity_root + "/groupby_config.html";
		groupingCont = FormLayoutContainer.createCustomFormLayout("config", getTranslator(), groupPage);
		flc.add("config", groupingCont);
		
		groupEl1 = uifactory.addDropdownSingleselect("heatmap.group1", groupingCont, emptyArray(), emptyArray());
		groupEl1.addActionListener(FormEvent.ONCHANGE);
		groupEl2 = uifactory.addDropdownSingleselect("heatmap.group2", groupingCont, emptyArray(), emptyArray());
		groupEl2.setAllowNoSelection(true);
		groupEl2.addActionListener(FormEvent.ONCHANGE);
		groupEl3 = uifactory.addDropdownSingleselect("heatmap.group3", groupingCont, emptyArray(), emptyArray());
		groupEl3.setAllowNoSelection(true);
		groupEl3.addActionListener(FormEvent.ONCHANGE);
		
		// Insufficient filter
		insufficientEl = uifactory.addCheckboxesVertical("heatmap.insufficient", groupingCont, INSUFFICIENT_KEYS,
				translateAll(getTranslator(), INSUFFICIENT_KEYS), 1);
		insufficientEl.addActionListener(FormEvent.ONCHANGE);
		
		// Temporal
		if (showTemporalConfig()) {
			KeyValues temporalKV = AnalysisUIFactory.getTemporalGroupByKeyValues(getTranslator());
			temporalGroupEl = uifactory.addDropdownSingleselect("trend.temporal.group", groupingCont,
					temporalKV.keys(), temporalKV.values());
			temporalGroupEl.addActionListener(FormEvent.ONCHANGE);
			String temporalGroupKey = AnalysisUIFactory.getKey(temporalGroupBy);
			if (Arrays.asList(temporalGroupEl.getKeys()).contains(temporalGroupKey)) {
				temporalGroupEl.select(temporalGroupKey, true);
			}
			
			KeyValues diffKV = AnalysisUIFactory.getTrendDifferenceKeyValues(getTranslator());
			differenceEl = uifactory.addDropdownSingleselect("trend.difference", groupingCont,
					diffKV.keys(), diffKV.values());
			differenceEl.addActionListener(FormEvent.ONCHANGE);
			String differenceKey = AnalysisUIFactory.getKey(trendDifference);
			if (Arrays.asList(differenceEl.getKeys()).contains(differenceKey)) {
				differenceEl.select(differenceKey, true);
			}
			
			List<Rubric> rubrics = getSliders().stream().map(SliderWrapper::getRubric).distinct().collect(toList());
			if (rubrics.size() > 0) {
				KeyValues rubricKV = AnalysisUIFactory.getRubricKeyValue(getTranslator(), rubrics);
				rubricEl = uifactory.addDropdownSingleselect("trend.rubrics", groupingCont, rubricKV.keys(), rubricKV.values());
				rubricEl.addActionListener(FormEvent.ONCHANGE);
				if (StringHelper.containsNonWhitespace(rubricId)) {
					Optional<Rubric> rubric = rubrics.stream().filter(r -> r.getId().equals(rubricId)).findFirst();
					if (rubric.isPresent()) {
						String rubricKey = AnalysisUIFactory.getKey(rubric.get());
						if (Arrays.asList(rubricEl.getKeys()).contains(rubricKey)) {
							rubricEl.select(rubricKey, true);
						}
					}
				}
				if (!rubricEl.isOneSelected() && rubricEl.getKeys().length > 0) {
					rubricEl.select(rubricEl.getKeys()[0], true);
				}
				if (rubricEl.getKeys().length <= 2 && rubrics.size() <= 1) {
					rubricEl.setVisible(false);
				}
			}
		}

		updateUI();
	}
	
	private void updateUI() {
		// groupings
		KeyValues groupByKV1 = initGroupByKeyValues(groupEl1);
		String[] groupKeys1 = groupByKV1.keys();
		if (multiGroupBy.isNoGroupBy() && groupKeys1.length > 0) {
			GroupBy groupBy1 = GroupBy.valueOf(groupKeys1[0]);
			multiGroupBy = MultiGroupBy.of(groupBy1);
		}
		groupEl1.setKeysAndValues(groupKeys1, groupByKV1.values(), null);
		selectGroupBy(groupEl1, multiGroupBy.getGroupBy1());

		KeyValues groupByKV2 = initGroupByKeyValues(groupEl2);
		groupEl2.setKeysAndValues(groupByKV2.keys(), groupByKV2.values(), null);
		groupEl2.setVisible(!groupByKV2.isEmpty());
		selectGroupBy(groupEl2, multiGroupBy.getGroupBy2());

		KeyValues groupByKV3 = initGroupByKeyValues(groupEl3);
		groupEl3.setKeysAndValues(groupByKV3.keys(), groupByKV3.values(), null);
		groupEl3.setVisible(!groupByKV3.isEmpty());
		selectGroupBy(groupEl3, multiGroupBy.getGroupBy3());
		
		boolean groupingVisible = !groupByKV1.isEmpty();
		groupingCont.setVisible(groupingVisible);
		
		// insufficient
		if (insufficientOnly) {
			insufficientEl.select(insufficientEl.getKey(0), true);
		} else {
			insufficientEl.select(insufficientEl.getKey(0), false);
		}
		insufficientEl.setVisible(insufficientConfigured);
	}
	
	private KeyValues initGroupByKeyValues(SingleSelection groupEl) {
		KeyValues keyValues = AnalysisUIFactory.getGroupByKeyValues(getTranslator(), availableAttributes);
		Collection<GroupBy> elsewhereSelected = getElsewhereSelected(groupEl);
		for (GroupBy groupBy : elsewhereSelected) {
			keyValues.remove(getKey(groupBy));
		}
		return keyValues;
	}

	private Collection<GroupBy> getElsewhereSelected(SingleSelection groupEl) {
		Collection<GroupBy> elsewhereSelected = new ArrayList<>(2);
		if (groupEl != groupEl1 && multiGroupBy.getGroupBy1() != null) {
			elsewhereSelected.addAll(ammendTopics(multiGroupBy.getGroupBy1()));
		}
		if (groupEl != groupEl2 && multiGroupBy.getGroupBy2() != null) {
			elsewhereSelected.addAll(ammendTopics(multiGroupBy.getGroupBy2()));
		}
		if (groupEl != groupEl3 && multiGroupBy.getGroupBy3() != null) {
			elsewhereSelected.addAll(ammendTopics(multiGroupBy.getGroupBy3()));
		}
		return elsewhereSelected;
	}

	/**
	 * A data collection has never multiple topics. So a grouping by multiple topics makes no sense.
	 *
	 * @param groupBy
	 * @return
	 */
	private Collection<? extends GroupBy> ammendTopics(GroupBy groupBy) {
		if (GROUP_BY_TOPICS.contains(groupBy)) {
			return GROUP_BY_TOPICS;
		}
		return Collections.singletonList(groupBy);
	}

	private void selectGroupBy(SingleSelection groupEl, GroupBy groupBy) {
		if (groupBy != null) {
			String groupByKey = getKey(groupBy);
			if (Arrays.asList(groupEl.getKeys()).contains(groupByKey)) {
				groupEl.select(groupByKey, true);
			}
		}
	}

	private void updateTable(List<ColumnConfig> columnConfigs) {
		int columnIndex = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (columnConfigs.isEmpty()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("heatmap.table.title.group", columnIndex++));
		} else {
			for (ColumnConfig columnConfig : columnConfigs) {
				DefaultFlexiColumnModel columnModel = columnConfig.isActionEnabled()
						? new DefaultFlexiColumnModel("heatmap.table.title.blank", columnIndex++, CMD_GROUP_PREFIX + columnIndex,
								new StaticFlexiCellRenderer(CMD_GROUP_PREFIX + columnIndex, new TextFlexiCellRenderer(EscapeMode.none)))
						: new DefaultFlexiColumnModel("heatmap.table.title.blank", columnIndex++);
				columnModel.setHeaderLabel(columnConfig.getHeader());
				columnModel.setAlwaysVisible(true);
				columnsModel.addFlexiColumnModel(columnModel);
			}
		}
		
		columnIndex = addDataColumns(columnsModel, columnIndex);
		
		DefaultFlexiColumnModel trendColumn = new DefaultFlexiColumnModel("heatmap.table.title.trend", columnIndex++,
				CMD_TREND, new StaticFlexiCellRenderer("", CMD_TREND, "o_icon o_icon-lg o_icon_qual_ana_trend", null));
		trendColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(trendColumn);
		
		dataModel = new GroupByDataModel(columnsModel, getLocale());
		if (tableEl != null) flc.remove(tableEl);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), flc);
		tableEl.setElementCssClass("o_qual_hm o_qual_trend");
		tableEl.setEmtpyTableMessageKey("heatmap.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		
		// legend
		if (legendLayout != null) flc.remove(legendLayout);
		if (showLegend()) {
			String legendPage = velocity_root + "/heatmap_legend.html";
			legendLayout = FormLayoutContainer.createCustomFormLayout("legend", getTranslator(), legendPage);
			flc.add("legend", legendLayout);
			legendLayout.contextPut("sliders", sliders);
		}
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	public void onFilter(UserRequest ureq, AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
		groupByNames.init(searchParams.getFormEntryRef());
		refreshDiagram();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == groupEl1 || source == groupEl2 || source == groupEl3) {
			setGroupBy(ureq);
		} else if (source == insufficientEl) {
			setInsufficientOnly(ureq);
		} else if (source == temporalGroupEl) {
			setTemporalGroupBy(ureq);
		} else if (source == differenceEl) {
			setTrendDifference(ureq);
		} else if (source == rubricEl) {
			setRubricId(ureq);
		} else if (source == tableEl && event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			GroupByRow row = dataModel.getObject(se.getIndex());
			if (CMD_TREND.equals(cmd)) {
				doShowTrend(ureq, row);
			} else if (cmd.indexOf(CMD_GROUP_PREFIX) > -1) {
				String cmdIndex = cmd.substring(CMD_GROUP_PREFIX.length());
				int index = Integer.parseInt(cmdIndex);
				doShowDetails(ureq, row, index);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setGroupBy(UserRequest ureq) {
		GroupBy groupBy1 = groupEl1.isOneSelected()? getGroupBy(groupEl1.getSelectedKey()): null;
		GroupBy groupBy2 = groupEl2.isOneSelected()? getGroupBy(groupEl2.getSelectedKey()): null;
		GroupBy groupBy3 = groupEl3.isOneSelected()? getGroupBy(groupEl3.getSelectedKey()): null;
		multiGroupBy = MultiGroupBy.of(groupBy1, groupBy2, groupBy3);
		fireEvent(ureq, new AnalysisGroupingEvent(multiGroupBy));
		updateUI();
		refreshDiagram();
	}

	private void setInsufficientOnly(UserRequest ureq) {
		if (insufficientEl.isVisible() && insufficientEl.isAtLeastSelected(1)) {
			insufficientOnly = true;
		} else {
			insufficientOnly = false;
		}
		refreshDiagram();
		fireEvent(ureq, new AnalysisInsufficientOnlyEvent(insufficientOnly));
	}
	
	private void setTemporalGroupBy(UserRequest ureq) {
		temporalGroupBy = temporalGroupEl.isOneSelected()
				? AnalysisUIFactory.getTemporalGroupBy(temporalGroupEl.getSelectedKey())
				: null;
		refreshDiagram();
		fireEvent(ureq, new TemporalGroupingEvent(temporalGroupBy));
	}

	private void setTrendDifference(UserRequest ureq) {
		trendDifference = differenceEl.isOneSelected()
				? AnalysisUIFactory.getTrendDifference(differenceEl.getSelectedKey())
				: TrendDifference.NONE;
		refreshDiagram();
		fireEvent(ureq, new TrendDifferenceEvent(trendDifference));
	}
	
	private void setRubricId(UserRequest ureq) {
		rubricId = rubricEl.isOneSelected() && !AnalysisUIFactory.isAllRubricsKey(rubricEl.getSelectedKey())
				? AnalysisUIFactory.getRubricId(rubricEl.getSelectedKey())
				: null;
		refreshDiagram();
		fireEvent(ureq, new RubricIdEvent(rubricId));
	}
	
	private void refreshDiagram() {
		String groupNameNA = translate("heatmap.not.specified");
		loadStatistics();
		Set<MultiKey> keys = getStatisticsMultiKeys();
		List<GroupByRow> rows = new ArrayList<>(keys.size());
		for (MultiKey multiKey : keys) {
			if (MultiKey.none().equals(multiKey)) continue;
			
			List<String> groupNames = getGroupNames(multiKey, groupNameNA);
			
			List<? extends GroupedStatistic> rowStatistics = getGroupedStatistcList(multiKey);
			GroupByRow row = new GroupByRow(multiKey, groupNames, rowStatistics);
			rows.add(row);
		}
		
		if (insufficientOnly) {
			rows.removeIf(this::hasNoInsufficientAvgs);
		}
		
		List<ColumnConfig> columnConfigs = getColumnConfigs();
		updateTable(columnConfigs);
		
		rows.sort(new GroupNameAlphabeticalComparator());
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<String> getGroupNames(MultiKey multiKey, String groupNameNA) {
		List<String> groupNames = new ArrayList<>(3);
		if (multiGroupBy.getGroupBy1() != null) {
			groupNames.add(getGroupName(multiKey, 1, groupNameNA));
		}
		if (multiGroupBy.getGroupBy2() != null) {
			groupNames.add(getGroupName(multiKey, 2, groupNameNA));
		}
		if (multiGroupBy.getGroupBy3() != null) {
			groupNames.add(getGroupName(multiKey, 3, groupNameNA));
		}
		return groupNames;
	}

	private String getGroupName(MultiKey mKey, int index, String groupNameNA) {
		GroupByKey groupByAndKey = getGroupByAndKey(multiGroupBy, mKey, index);
		String groupName = groupByNames.getName(groupByAndKey);
		return StringHelper.containsNonWhitespace(groupName)? groupName: groupNameNA;
	}

	public List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> columConfigs = new ArrayList<>();
		if (multiGroupBy.getGroupBy1() != null) {
			ColumnConfig columConfig1 = getColumnConfig(multiGroupBy.getGroupBy1());
			columConfigs.add(columConfig1);
		}
		if (multiGroupBy.getGroupBy2() != null) {
			ColumnConfig columConfig2 = getColumnConfig(multiGroupBy.getGroupBy2());
			columConfigs.add(columConfig2);
		}
		if (multiGroupBy.getGroupBy3() != null) {
			ColumnConfig columConfig3 = getColumnConfig(multiGroupBy.getGroupBy3());
			columConfigs.add(columConfig3);
		}
		return columConfigs;
	}
	
	private ColumnConfig getColumnConfig(GroupBy groupBy) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			return new ColumnConfig(translate("heatmap.table.title.identity"));
		case TOPIC_ORGANISATION:
			return new ColumnConfig(translate("heatmap.table.title.organisation"));
		case TOPIC_CURRICULUM:
			return new ColumnConfig(translate("heatmap.table.title.curriculum"));
		case TOPIC_CURRICULUM_ELEMENT:
			return new ColumnConfig(translate("heatmap.table.title.curriculum.element"));
		case TOPIC_REPOSITORY:
			return new ColumnConfig(translate("heatmap.table.title.repository"));
		case CONTEXT_ORGANISATION:
			return new ColumnConfig(translate("heatmap.table.title.organisation"));
		case CONTEXT_CURRICULUM:
			return new ColumnConfig(translate("heatmap.table.title.curriculum"));
		case CONTEXT_CURRICULUM_ELEMENT:
			return new ColumnConfig(translate("heatmap.table.title.curriculum.element"));
		case CONTEXT_CURRICULUM_ORGANISATION:
			return new ColumnConfig(translate("heatmap.table.title.curriculum.organisation"));
		case CONTEXT_TAXONOMY_LEVEL:
			return new ColumnConfig(translate("heatmap.table.title.taxonomy.level"));
		case CONTEXT_LOCATION:
			return new ColumnConfig(translate("heatmap.table.title.location"));
		case DATA_COLLECTION:
			return new ColumnConfig(translate("heatmap.table.title.data.collection"), true);
		default:
			return null;
		}
	}

	private boolean hasNoInsufficientAvgs(GroupByRow row) {
		for (int i = 0; i < row.getStatisticsSize(); i++) {
			GroupedStatistic statistic = row.getStatistic(i);
			if (statistic != null) {
				Double avg = statistic.getAvg();
				String identifier = statistic.getIdentifier();
				Rubric rubric = getRubricByIdentifier(identifier);
				boolean isInsufficient = analysisService.isInsufficient(rubric, avg);
				if (isInsufficient) {
					return false;
				}
			}
		}
		return true;
	}

	private Rubric getRubricByIdentifier(String identifier) {
		for (SliderWrapper sliderWrapper : getSliders()) {
			if (identifier.equals(sliderWrapper.getIdentifier())) {
				return sliderWrapper.getRubric();
			}
		}
		return null;
	}

	private Rubric getRubricById(String id) {
		for (SliderWrapper sliderWrapper : getSliders()) {
			if (id.equals(sliderWrapper.getRubric().getId())) {
				return sliderWrapper.getRubric();
			}
		}
		return null;
	}
	
	private void doShowTrend(UserRequest ureq, GroupByRow row) {
		MultiKey multiKey = row.getMultiKey();
		AnalysisSearchParameter trendSearchParameter = getTrendSearchParams(multiKey);
		trendCtrl = new SliderTrendController(ureq, getWindowControl(), sliders, trendSearchParameter);
		listenTo(trendCtrl);
		stackPanel.changeDisplayname(translate("analysis.trend"));
		stackPanel.pushController(getTrendTitle(multiKey), trendCtrl);
	}

	private AnalysisSearchParameter getTrendSearchParams(MultiKey multiKey) {
		AnalysisSearchParameter trendSearchParams = searchParams.clone();
		ammendGroupBySearchParam(trendSearchParams, getGroupByAndKey(multiGroupBy, multiKey, 1));
		ammendGroupBySearchParam(trendSearchParams, getGroupByAndKey(multiGroupBy, multiKey, 2));
		ammendGroupBySearchParam(trendSearchParams, getGroupByAndKey(multiGroupBy, multiKey, 3));
		return trendSearchParams;
	}

	private void ammendGroupBySearchParam(AnalysisSearchParameter trendSearchParams, GroupByKey groupByKey) {
		if (groupByKey == null || groupByKey.getGroupBy() == null) return;
		
		String key = groupByKey.getKey();
		GroupBy groupBy = groupByKey.getGroupBy();
		if (key != null) {
			ammendGroupBySearchParamKey(trendSearchParams, groupBy, key);
		} else {
			ammendGroupBySearchParamNull(trendSearchParams, groupBy);
		}
	}

	private void ammendGroupBySearchParamKey(AnalysisSearchParameter trendSearchParams, GroupBy groupBy, String key) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			trendSearchParams.setTopicIdentityRefs(singletonList(new IdentityRefImpl(toLongOrZero(key))));
			break;
		case TOPIC_ORGANISATION:
			trendSearchParams.setTopicOrganisationRefs(singletonList(new OrganisationRefImpl(toLongOrZero(key))));
			break;
		case TOPIC_CURRICULUM:
			trendSearchParams.setTopicCurriculumRefs(singletonList(new CurriculumRefImpl(toLongOrZero(key))));
			break;
		case TOPIC_CURRICULUM_ELEMENT:
			trendSearchParams.setTopicCurriculumElementRefs(singletonList(new CurriculumElementRefImpl(toLongOrZero(key))));
			break;
		case TOPIC_REPOSITORY:
			trendSearchParams.setTopicRepositoryRefs(singletonList(new RepositoryEntryRefImpl(toLongOrZero(key))));
			break;
		case CONTEXT_ORGANISATION:
			trendSearchParams.setContextOrganisationRef(new OrganisationRefImpl(toLongOrZero(key)));
			break;
		case CONTEXT_CURRICULUM:
			trendSearchParams.setContextCurriculumRefs(singletonList(new CurriculumRefImpl(toLongOrZero(key))));
			break;
		case CONTEXT_CURRICULUM_ELEMENT:
			trendSearchParams.setContextCurriculumElementRef(new CurriculumElementRefImpl(toLongOrZero(key)));
			break;
		case CONTEXT_CURRICULUM_ORGANISATION:
			trendSearchParams.setContextCurriculumOrganisationRef(new OrganisationRefImpl(toLongOrZero(key)));
			break;
		case CONTEXT_TAXONOMY_LEVEL:
			trendSearchParams.setContextTaxonomyLevelRef(new TaxonomyLevelRefImpl(toLongOrZero(key)));
			break;
		case CONTEXT_LOCATION:
			trendSearchParams.setContextLocations(singletonList(key));
			break;
		case DATA_COLLECTION:
			trendSearchParams.setDataCollectionRefs(singletonList(new QualityDataCollectionRefImpl(toLongOrZero(key))));
			break;
		default:
		}
	}

	private void ammendGroupBySearchParamNull(AnalysisSearchParameter trendSearchParams, GroupBy groupBy) {
		switch (groupBy) {
		case TOPIC_IDENTITY:
			trendSearchParams.setTopicIdentityNull(true);
			break;
		case TOPIC_ORGANISATION:
			trendSearchParams.setTopicOrganisationNull(true);
			break;
		case TOPIC_CURRICULUM:
			trendSearchParams.setTopicCurriculumNull(true);
			break;
		case TOPIC_CURRICULUM_ELEMENT:
			trendSearchParams.setTopicCurriculumElementNull(true);
			break;
		case TOPIC_REPOSITORY:
			trendSearchParams.setTopicRepositoryNull(true);
			break;
		case CONTEXT_ORGANISATION:
			trendSearchParams.setContextOrganisationNull(true);
			break;
		case CONTEXT_CURRICULUM:
			trendSearchParams.setContextCurriculumNull(true);
			break;
		case CONTEXT_CURRICULUM_ELEMENT:
			trendSearchParams.setContextCurriculumElementNull(true);
			break;
		case CONTEXT_CURRICULUM_ORGANISATION:
			trendSearchParams.setContextCurriculumOrganisationNull(true);
			break;
		case CONTEXT_TAXONOMY_LEVEL:
			trendSearchParams.setContextTaxonomyLevelNull(true);
			break;
		case CONTEXT_LOCATION:
			trendSearchParams.setContextLocationNull(true);
			break;
		case DATA_COLLECTION:
			// is never null
		default:
		}
	}

	private String getTrendTitle(MultiKey multiKey) {
		String groupNameNA = translate("heatmap.not.specified");
		return getGroupNames(multiKey, groupNameNA)
				.stream()
				.collect(Collectors.joining(", "));
	}

	private void doShowDetails(UserRequest ureq, GroupByRow row, int index) {
		GroupByKey groupByKey = getGroupByAndKey(multiGroupBy, row.getMultiKey(), index);
		doShowDetails(ureq, groupByKey);
	}
	
	private void doShowDetails(UserRequest ureq, GroupByKey groupByKey) {
		if (groupByKey != null && GroupBy.DATA_COLLECTION.equals(groupByKey.getGroupBy())
				&& StringHelper.containsNonWhitespace(groupByKey.getKey())) {
			Long key = Long.valueOf(groupByKey.getKey());
			QualityDataCollection dataCollection = qualityService.loadDataCollectionByKey(() -> key);
			detailCtrl = new DataCollectionReportController(ureq, getWindowControl(), dataCollection);
			listenTo(detailCtrl);
			stackPanel.changeDisplayname(translate("analysis.details"));
			stackPanel.pushController(dataCollection.getTitle(), detailCtrl);
		}
	}
	
	private GroupByKey getGroupByAndKey(MultiGroupBy mGroupBy, MultiKey mKey, int index) {
		GroupByKey groupByKey = null;
		if (index == 3) {
			groupByKey = new GroupByKey(mGroupBy.getGroupBy3(), mKey.getKey3());
		} else if (index == 2) {
			groupByKey = new GroupByKey(mGroupBy.getGroupBy2(), mKey.getKey2());
		} else if (index == 1) {
			groupByKey =new GroupByKey(mGroupBy.getGroupBy1(), mKey.getKey1());
		}
		return groupByKey;
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
	
	private final static class ColumnConfig {
		
		private final String header;
		private final boolean actionEnabled;
		
		private ColumnConfig(String header) {
			this(header, false);
		}
		
		private ColumnConfig(String header, boolean actionEnabled) {
			this.header = header;
			this.actionEnabled = actionEnabled;
		}

		public String getHeader() {
			return header;
		}

		public boolean isActionEnabled() {
			return actionEnabled;
		}
	}

}
