/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.generator.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.scope.DateScope;
import org.olat.core.gui.components.scope.FormDateScopeSelection;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeFactory.DateScopesBuilder;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjDecisionFilter;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.GeneratorPreviewSearchParams;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.ui.PreviewDataModel.PreviewCols;
import org.olat.modules.quality.ui.DataCollectionController;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPreviewListController extends FormBasicController implements Activateable2 {
	
	private enum PreviewFilter { form, generator, title, topicType, topic, status }
	
	private static final String CMD_OPEN = "open";
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_COURSE = "Course";
	private static final String TAB_ID_CURRICULUM_ELEMENT = "CurriculumElement";
	private static final String TAB_ID_BLACKLIST = "Blacklist";
	
	private final TooledStackedPanel stackPanel;
	private FormDateScopeSelection scopeEl;
	protected FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabCourse;
	private FlexiFiltersTab tabCurriculumElement;
	private FlexiFiltersTab tabBlacklist;
	private FlexiTableElement tableEl;
	private PreviewDataModel dataModel;

	private PreviewController previewCtrl;
	private DataCollectionController dataCollectionCtrl;
	
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifecycleDao;
	@Autowired
	private BaseSecurityModule securityModule;

	protected AbstractPreviewListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(QualityUIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
	}
	
	protected abstract  List<OrganisationRef> getDataCollectionOrganisationRefs();
	
	protected abstract  List<OrganisationRef> getLearnResourceManagerOrganisationRefs();
	
	protected abstract List<OrganisationRef> getGeneratorOrganisationRefs();
	
	protected abstract RepositoryEntryRef getRestrictRepositoryEntry();
	
	protected abstract boolean isFilterGenerator();
	
	protected abstract boolean canEdit();

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<DateScope> scopes = ScopeFactory.dateScopesBuilder(getLocale())
				.nextMonths(1)
				.nextMonths(3)
				.build();
		scopeEl = uifactory.addDateScopeSelection(getWindowControl(), "scope", null, formLayout, scopes, getLocale());
		DateRange customScopeLimit = new DateRange(
				DateUtils.getStartOfDay(new Date()),
				DateUtils.getEndOfDay(DateUtils.addYears(new Date(), 1)));
		scopeEl.setCustomScopeLimit(customScopeLimit);
		DateScopesBuilder additionalDateScopeBuilder = ScopeFactory.dateScopesBuilder(getLocale())
				.nextWeeks(2)
				.nextMonths(1)
				.toEndOfYear()
				.christmasToNewYear();
		reLifecycleDao.loadPublicLifecycle().stream()
				.filter(ls -> ls.getValidFrom() != null && ls.getValidFrom().before(customScopeLimit.getTo()))
				.filter(ls -> ls.getValidTo() != null && ls.getValidTo().after(customScopeLimit.getFrom()))
				.forEach(ls -> additionalDateScopeBuilder.add(ScopeFactory.createDateScope(
						"ls" + ls.getKey(), ls.getLabel(), null, 
						new DateRange(DateUtils.getStartOfDay(ls.getValidFrom()), DateUtils.getEndOfDay(ls.getValidTo())))));
		scopeEl.setAdditionalDateScopes(additionalDateScopeBuilder.build());
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.title, CMD_OPEN));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.deadline));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.topicType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.topic));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.formName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.numberParticipants));
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PreviewCols.generatorId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PreviewCols.generatorTitle));
		
		dataModel = new PreviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "qm-previews");
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(PreviewCols.start.name(), true));
		tableEl.setSortSettings(options);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		List<QualityGenerator> generators = isFilterGenerator()
				? generatorService.getEnabledGenerators(getGeneratorOrganisationRefs())
				: List.of();
		if (!generators.isEmpty()) {
			List<RepositoryEntry> formEntries = generators.stream().map(QualityGenerator::getFormEntry).toList();
			SelectionValues formValues = new SelectionValues();
			formEntries.forEach(formEntry -> formValues.add(SelectionValues.entry(formEntry.getKey().toString(), formEntry.getDisplayname())));
			formValues.sort(SelectionValues.VALUE_ASC);
			filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.form"), PreviewFilter.form.name(), formValues, true));
		
			SelectionValues generatorsValues = new SelectionValues();
			generators.forEach(generator -> generatorsValues.add(SelectionValues.entry(generator.getKey().toString(), generator.getTitle())));
			generatorsValues.sort(SelectionValues.VALUE_ASC);
			filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.generator.title"), PreviewFilter.generator.name(), generatorsValues, true));
		}
		
		filters.add(new FlexiTableTextFilter(translate("data.collection.title"), PreviewFilter.title.name(), true));
		
		SelectionValues topicTypeValues = new SelectionValues();
		Arrays.stream(QualityDataCollectionTopicType.values())
				.forEach(type -> topicTypeValues.add(SelectionValues.entry(type.name(), translate(type.getI18nKey()))));
		topicTypeValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.topic.type"), PreviewFilter.topicType.name(), topicTypeValues, false));
		
		filters.add(new FlexiTableTextFilter(translate("data.collection.topic"), PreviewFilter.topic.name(), false));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(QualityPreviewStatus.dataCollection.name(), getTranslatedStatus(QualityPreviewStatus.dataCollection)));
		statusValues.add(SelectionValues.entry(QualityPreviewStatus.regular.name(), getTranslatedStatus(QualityPreviewStatus.regular)));
		statusValues.add(SelectionValues.entry(QualityPreviewStatus.changed.name(), getTranslatedStatus(QualityPreviewStatus.changed)));
		statusValues.add(SelectionValues.entry(QualityPreviewStatus.blacklist.name(), getTranslatedStatus(QualityPreviewStatus.blacklist)));
		filters.add(new FlexiTableMultiSelectionFilter(translate("preview.status"), PreviewFilter.status.name(), statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private String getTranslatedStatus(QualityPreviewStatus status) {
		return switch (status) {
		case dataCollection -> translate("preview.status.data.collection");
		case regular -> translate("preview.status.regular");
		case changed -> translate("preview.status.changed");
		case blacklist -> translate("preview.status.blacklist");
		default -> null;
		};
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		if (getRestrictRepositoryEntry() == null) {
			tabCourse = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_COURSE,
					translate(QualityDataCollectionTopicType.REPOSITORY.getI18nKey()),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(PreviewFilter.topicType, QualityDataCollectionTopicType.REPOSITORY.name())));
			tabs.add(tabCourse);
			
			tabCurriculumElement = FlexiFiltersTabFactory.tabWithImplicitFilters(
					TAB_ID_CURRICULUM_ELEMENT,
					translate(QualityDataCollectionTopicType.CURRICULUM_ELEMENT.getI18nKey()),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(PreviewFilter.topicType, QualityDataCollectionTopicType.CURRICULUM_ELEMENT.name())));
			tabs.add(tabCurriculumElement);
		}
		
		tabBlacklist = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_BLACKLIST,
				translate("preview.status.blacklist"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjDecisionFilter.status, QualityPreviewStatus.blacklist.name())));
		tabs.add(tabBlacklist);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		loadModel();
	}

	protected void loadModel() {
		GeneratorPreviewSearchParams generatorSearchParams = new GeneratorPreviewSearchParams();
		QualityDataCollectionViewSearchParams dataCollectionSearchParams = new QualityDataCollectionViewSearchParams();
		applyFilters(generatorSearchParams, dataCollectionSearchParams);
		
		generatorSearchParams.setGeneratorOrganisationRefs(getGeneratorOrganisationRefs());
		generatorSearchParams.setRepositoryEntry(getRestrictRepositoryEntry());
		List<QualityPreview> previews = generatorService.getPreviews(generatorSearchParams);
		
		List<PreviewRow> rows = new ArrayList<>();
		for (QualityPreview preview : previews) {
			PreviewRow row = new PreviewRow(preview);
			
			row.setTranslatedStatus(getTranslatedStatus(row.getStatus()));
			row.setTopicType(translate(preview.getTopicType().getI18nKey()));
			row.setTopic(getTopic(preview));
			
			rows.add(row);
		}
		
		List<PreviewRow> dataCollectionRows = getDataCollectionRows(dataCollectionSearchParams);
		rows.addAll(dataCollectionRows);
		
		applyFilters(rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	private List<PreviewRow> getDataCollectionRows(QualityDataCollectionViewSearchParams searchParams) {
		searchParams.setOrgansationRefs(getDataCollectionOrganisationRefs());
		searchParams.setReportAccessIdentity(getIdentity());
		searchParams.setLearnResourceManagerOrganisationRefs(getLearnResourceManagerOrganisationRefs());
		searchParams.setIgnoreReportAccessRelationRole(!securityModule.isRelationRoleEnabled());
		searchParams.setCountToDoTasks(false);
		searchParams.setTopicOrAudienceRepositoryEntry(getRestrictRepositoryEntry());
		
		List<QualityDataCollectionView> dataCollections = qualityService.loadDataCollections(getTranslator(), searchParams, 0, -1);
		
		List<PreviewRow> rows = new ArrayList<>();
		for (QualityDataCollectionView dataCollection : dataCollections) {
			PreviewRow row = new PreviewRow(dataCollection);
			row.setTranslatedStatus(getTranslatedStatus(row.getStatus()));
			rows.add(row);
		}
		
		return rows;
	}

	private void applyFilters(GeneratorPreviewSearchParams generatorSearchParams, QualityDataCollectionViewSearchParams dataCollectionSearchParams) {
		DateRange selectedDateRange = getSelectedDateRange();
		generatorSearchParams.setDateRange(selectedDateRange);
		dataCollectionSearchParams.setStartAfter(selectedDateRange.getFrom());
		dataCollectionSearchParams.setStartBefore(selectedDateRange.getTo());
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (PreviewFilter.form.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<Long> formEntryKeys = values.stream()
							.map(Long::valueOf)
							.collect(Collectors.toList());
					generatorSearchParams.setFormEntryKeys(formEntryKeys);
					dataCollectionSearchParams.setFormEntryKeys(formEntryKeys);
				} else {
					generatorSearchParams.setFormEntryKeys(null);
					dataCollectionSearchParams.setFormEntryKeys(null);
				}
			}
			if (PreviewFilter.generator.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<Long> generatorKeys = values.stream()
							.map(Long::valueOf)
							.collect(Collectors.toList());
					generatorSearchParams.setGeneratorKeys(generatorKeys);
					dataCollectionSearchParams.setGeneratorKeys(generatorKeys);
				} else {
					generatorSearchParams.setGeneratorKeys(null);
					dataCollectionSearchParams.setGeneratorKeys(null);
				}
			}
			if (PreviewFilter.topicType.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<QualityDataCollectionTopicType> topicTypes = values.stream()
							.map(QualityDataCollectionTopicType::valueOf)
							.collect(Collectors.toList());
					generatorSearchParams.setTopicTypes(topicTypes);
					dataCollectionSearchParams.setTopicTypes(topicTypes);
				} else {
					generatorSearchParams.setTopicTypes(null);
					dataCollectionSearchParams.setTopicTypes(null);
				}
			}
		}
	}

	private void applyFilters(List<PreviewRow> rows) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (PreviewFilter.title.name() == filter.getFilter()) {
				if (StringHelper.containsNonWhitespace(filter.getValue())) {
					String lowerCaseValue = filter.getValue().toLowerCase();
					rows.removeIf(row -> row.getTitle().toLowerCase().indexOf(lowerCaseValue) < 0);
				}
			}
			if (PreviewFilter.topic.name() == filter.getFilter()) {
				if (StringHelper.containsNonWhitespace(filter.getValue())) {
					String lowerCaseValue = filter.getValue().toLowerCase();
					rows.removeIf(row -> row.getTopic().toLowerCase().indexOf(lowerCaseValue) < 0);
				}
			}
			if (PreviewFilter.status.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.size() < QualityPreviewStatus.values().length) {
					List<QualityPreviewStatus> status = values.stream()
							.map(QualityPreviewStatus::valueOf)
							.collect(Collectors.toList());
					rows.removeIf(row -> !status.contains(row.getStatus()));
				}
			}
		}
	}

	private DateRange getSelectedDateRange() {
		DateRange selectedDateRange = scopeEl.getSelectedDateRange();
		return selectedDateRange.getFrom().before(new Date())
				? new DateRange(new Date(), selectedDateRange.getTo())
				: selectedDateRange;
	}

	private String getTopic(QualityPreview preview) {
		return switch (preview.getTopicType()) {
		case CUSTOM -> preview.getTopicCustom();
		case IDENTIY -> userManager.getUserDisplayName(preview.getTopicIdentity().getKey());
		case ORGANISATION -> preview.getTopicOrganisation().getDisplayName();
		case CURRICULUM -> preview.getTopicCurriculum().getDisplayName();
		case CURRICULUM_ELEMENT -> preview.getTopicCurriculumElement().getDisplayName();
		case REPOSITORY -> preview.getTopicRepositoryEntry().getDisplayname();
		default -> null;
		};
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String type = entry.getOLATResourceable().getResourceableTypeName();
			FlexiFiltersTab tab = tableEl.getFilterTabById(type);
			if (tab != null) {
				selectFilterTab(ureq, tab);
			}
		}
	}
	

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			loadModel();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				PreviewRow row = dataModel.getObject(se.getIndex());
				if (CMD_OPEN.equals(cmd)) {
					doOpen(ureq, row);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		} else if (source == scopeEl) {
			loadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}
	
	private void doOpen(UserRequest ureq, PreviewRow row) {
		if (row.getDataCollectionKey() != null) {
			doOpenDataCollection(ureq, row);
		} else {
			doOpenGeneratorPreview(ureq, row);
		}
	}

	private void doOpenDataCollection(UserRequest ureq, PreviewRow row) {
		dataCollectionCtrl = new DataCollectionController(ureq, getWindowControl(), stackPanel, () -> row.getDataCollectionKey());
		listenTo(dataCollectionCtrl);
		String title = row.getTitle();
		String formattedTitle = StringHelper.containsNonWhitespace(title)
				? Formatter.truncate(title, 50)
				: translate("data.collection.title.empty");
		stackPanel.pushController(formattedTitle, dataCollectionCtrl);
		dataCollectionCtrl.activate(ureq, null, null);
	}

	private void doOpenGeneratorPreview(UserRequest ureq, PreviewRow row) {
		Optional<QualityPreview> preview = loadGeneratorPreview(row);
		if (preview.isPresent()) {
			previewCtrl = new PreviewController(ureq, getWindowControl(), stackPanel, preview.get(), canEdit());
			listenTo(previewCtrl);
			stackPanel.pushController(preview.get().getTitle(), previewCtrl);
			previewCtrl.activate(ureq, null, null);
		} else {
			showWarning("preview.error.not.available");
			loadModel();
			return;
		}
	}

	private Optional<QualityPreview> loadGeneratorPreview(PreviewRow row) {
		GeneratorPreviewSearchParams generatorSearchParams = new GeneratorPreviewSearchParams();
		generatorSearchParams.setDateRange(getSelectedDateRange());
		generatorSearchParams.setGeneratorKeys(List.of(row.getGeneratorId()));
		return generatorService.getPreviews(generatorSearchParams).stream()
				.filter(preview -> row.getIdentifier().equals(preview.getIdentifier()))
				.findFirst();
	}

}
