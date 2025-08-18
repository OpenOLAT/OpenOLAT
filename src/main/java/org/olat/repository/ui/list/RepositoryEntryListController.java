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
package org.olat.repository.ui.list;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFilterEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabPosition;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderLabels;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.olat.repository.ui.list.DefaultRepositoryEntryDataSource.FilterButton;
import org.olat.repository.ui.list.DefaultRepositoryEntryDataSource.FilterStatus;
import org.olat.repository.ui.list.RepositoryEntryDataModel.Cols;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class RepositoryEntryListController extends FormBasicController
	implements Activateable2, RepositoryEntryDataSourceUIFactory, FlexiTableComponentDelegate {

	private final List<Link> filterLinks = new ArrayList<>();
	private final List<Link> orderByLinks = new ArrayList<>();
	private List<RepositoryEntryEducationalType> educationalTypes;
	
	private final RepositoryEntryListConfig config;
	
	private FlexiFiltersTab myTab;
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab bookmarkTab;

	private final String name;
	private FlexiTableElement tableEl;
	private RepositoryEntryDataModel model;
	private DefaultRepositoryEntryDataSource dataSource;
	private SearchMyRepositoryEntryViewParams searchParams;
	
	private final BreadcrumbPanel stackPanel;
	private RepositoryEntryDetailsController detailsCtrl;
	
	private final MapperKey mapperThumbnailKey;
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private LifecycleModule lifecycleModule;
	
	private final boolean guestOnly;
	
	public RepositoryEntryListController(UserRequest ureq, WindowControl wControl,
			SearchMyRepositoryEntryViewParams searchParams, boolean load, 
			RepositoryEntryListConfig config, String name, BreadcrumbPanel stackPanel) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(OpenAccessOfferController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(210, 140));
		this.name = name;
		this.config = config;
		this.stackPanel = stackPanel;
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		OLATResourceable ores = OresHelper.createOLATResourceableType("MyCoursesSite");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		
		this.searchParams = searchParams;
		dataSource = new DefaultRepositoryEntryDataSource(searchParams, this);
		initForm(ureq);
		
		if(load) {
			tableEl.reloadData();
		}
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}
	
	public String getName() {
		return name;
	}
	
	public FlexiFiltersTab getBookmarkPreset() {
		return bookmarkTab;
	}
	
	public FlexiFiltersTab getMyEntriesPreset() {
		return myTab;
	}
	
	public FlexiFiltersTab getAllPreset() {
		return allTab;
	}
	
	public void reloadRows() {
		tableEl.reloadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), true, OrderBy.key.name()));
		if(!guestOnly) {
			DefaultFlexiColumnModel markColModel = new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal(), true, OrderBy.favorit.name());
			markColModel.setIconHeader("o_icon o_icon_bookmark_header");
			columnsModel.addFlexiColumnModel(markColModel);
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.type.i18nKey(), Cols.type.ordinal(), true, OrderBy.type.name(),
				FlexiColumnModel.ALIGNMENT_LEFT, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.select.ordinal(),
				true, OrderBy.displayname.name()));
		if(repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18nKey(), Cols.externalId.ordinal(),
				true, OrderBy.externalId.name()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalRef.i18nKey(), Cols.externalRef.ordinal(),
				true, OrderBy.externalRef.name()));

		if (lifecycleModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal(),
					true, OrderBy.lifecycleSoftkey.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal(),
					true, OrderBy.lifecycleLabel.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
					true, OrderBy.lifecycleStart.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
					true, OrderBy.lifecycleEnd.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(getLocale())));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.location.i18nKey(), Cols.location.ordinal(),
				true, OrderBy.location.name()));
		DefaultFlexiColumnModel educationalTypeColumnModel = new DefaultFlexiColumnModel(false, Cols.educationalType.i18nKey(),
				Cols.educationalType.ordinal(), false, null);
		educationalTypeColumnModel.setCellRenderer(new EducationalTypeRenderer());
		columnsModel.addFlexiColumnModel(educationalTypeColumnModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.details.i18nKey(), Cols.details.ordinal(), false, null));
		if(!guestOnly) {
			DefaultFlexiColumnModel completionColumnModel = new DefaultFlexiColumnModel(Cols.completion.i18nKey(),
					Cols.completion.ordinal(), true, OrderBy.completion.name());
			columnsModel.addFlexiColumnModel(completionColumnModel);
			DefaultFlexiColumnModel successStatusColumnModel = new DefaultFlexiColumnModel(false, Cols.successStatus.i18nKey(),
					Cols.successStatus.ordinal(), true, OrderBy.passed.name(), FlexiColumnModel.ALIGNMENT_LEFT, new PassedCellRenderer(getLocale()));
			columnsModel.addFlexiColumnModel(successStatusColumnModel);
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.start.i18nKey(), Cols.start.ordinal()));
		if(repositoryModule.isRatingEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.ratings.i18nKey(), Cols.ratings.ordinal(),
				true, OrderBy.rating.name()));
		}
		
		DefaultFlexiColumnModel levelsCol = new DefaultFlexiColumnModel(Cols.taxonomyLevels.i18nKey(), Cols.taxonomyLevels.ordinal());
		levelsCol.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(levelsCol);
		
		model = new RepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(config.withSearch());
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_coursetable");
		if (config.withSearch()) {
			tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", "o_CourseModule_icon");			
		} else {
			tableEl.setEmptyTableSettings("table.list.empty", "table.list.empty.hint", "o_CourseModule_icon");			
		}
		VelocityContainer row = createVelocityContainer("row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		
		if(config.withPresets()) {
			educationalTypes = repositoryManager.getAllEducationalTypes();
			initFiltersPresets();
			initFiltersButtons();
		} else {
			initSmallFilters(tableEl);
		}
		initSorters(tableEl);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "re-list-v2.1-".concat(name));
		
		if (!config.withSavedSettings()) {
			SortKey sortKey = new SortKey(OrderBy.custom.name(), true);
			tableEl.sort(sortKey);
		}
	}

	private void initSmallFilters(FlexiTableElement tableElement) {
		List<FlexiTableFilter> filters = new ArrayList<>(16);
		filters.add(new FlexiTableFilter(translate("filter.show.all"), Filter.showAll.name(), true));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.only.courses"), Filter.onlyCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.current.courses"), Filter.currentCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.upcoming.courses"), Filter.upcomingCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.old.courses"), Filter.oldCourses.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.booked.participant"), Filter.asParticipant.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.coach"), Filter.asCoach.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.author"), Filter.asAuthor.name()));
		if(!searchParams.isMembershipMandatory() && !searchParams.isMembershipOnly()) {
			filters.add(new FlexiTableFilter(translate("filter.not.booked"), Filter.notBooked.name()));
		}
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.passed"), Filter.passed.name()));
		filters.add(new FlexiTableFilter(translate("filter.not.passed"), Filter.notPassed.name()));
		filters.add(new FlexiTableFilter(translate("filter.without.passed.infos"), Filter.withoutPassedInfos.name()));
		tableElement.setFilters(null, filters, false);
	}
	
	private void initFiltersPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		// bookmarks
		if(!guestOnly && config.presets().withFavoritePreset()) {
			bookmarkTab = FlexiFiltersTabFactory.tabWithImplicitAndDefaultFilters("Bookmarks", translate("search.mark"),
					TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FilterButton.MARKED, "marked")),
					List.of(FlexiTableFilterValue.valueOf(FilterButton.STATUS, List.of(FilterStatus.PREPARATION.name(), FilterStatus.ACTIVE.name(), FilterStatus.CLOSED.name()))));
			bookmarkTab.setElementCssClass("o_sel_mycourses_fav");
			tabs.add(bookmarkTab);
		}
		
		if(config.presets().withAllPreset()) {
			allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("search.all"),
					TabSelectionBehavior.reloadData, List.of(
							FlexiTableFilterValue.valueOf(FilterButton.OWNED, "owned")));
			allTab.setElementCssClass("o_sel_mycourses_all");
			tabs.add(allTab);
		}
		
		if(config.presets().withMyPreset()) {
			myTab = FlexiFiltersTabFactory.tabWithImplicitFilters("My", translate("search.active"),
					TabSelectionBehavior.reloadData, List.of(
							FlexiTableFilterValue.valueOf(FilterButton.OWNED, "owned"),
							FlexiTableFilterValue.valueOf(FilterButton.STATUS, FilterStatus.ACTIVE.name())));
			myTab.setElementCssClass("o_sel_mycourses_my");
			tabs.add(myTab);
		}
		
		if(config.presets().withEducationalTypesPreset()) {
			for(RepositoryEntryEducationalType educationalType:educationalTypes) {
				if(educationalType.isPresetMyCourses()) {
					String id = educationalType.getIdentifier().replace(".", "").replace("-", "").replace(" ", "");
					String i18nKey = RepositoyUIFactory.getPresetI18nKey(educationalType);
					String typeName = translate(i18nKey);
					if(typeName.equals(i18nKey) || typeName.length() > 255) {
						typeName = translate(RepositoyUIFactory.getI18nKey(educationalType));
					}
					FlexiFiltersTab typeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(id, typeName,
							TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FilterButton.EDUCATIONALTYPE, List.of(educationalType.getKey().toString()))));
					tabs.add(typeTab);
				}
			}
		}
		
		if(config.presets().withClosedPreset()) {
			FlexiFiltersTab closedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Closed", translate("search.courses.closed"),
					TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FilterButton.STATUS, FilterStatus.CLOSED.name()),
							FlexiTableFilterValue.valueOf(FilterButton.OWNED, "owned")));
			closedTab.setElementCssClass("o_sel_mycourses_closed");
			tabs.add(closedTab);
		}
		
		// search
		if(config.presets().withSearch()) {
			FlexiFiltersTab searchTab = FlexiFiltersTabFactory.tab("Search", translate("search.courses.student"), TabSelectionBehavior.clear);
			searchTab.setElementCssClass("o_sel_mycourses_search");
			searchTab.setPosition(FlexiFilterTabPosition.right);
			searchTab.setFiltersExpanded(true);
			searchTab.setLargeSearch(true);
			tabs.add(searchTab);
		}
		
		tableEl.setFilterTabs(true, tabs);
	}
	
	private void initFiltersButtons() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// bookmarked
		SelectionValues markedKeyValue = new SelectionValues();
		markedKeyValue.add(SelectionValues.entry("marked", translate("search.mark")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
				FilterButton.MARKED.name(), markedKeyValue, true));
		
		// my resources
		SelectionValues myResourcesKeyValue = new SelectionValues();
		myResourcesKeyValue.add(SelectionValues.entry("owned", translate("cif.owned.resources.only")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("cif.owned.resources.only"),
				FilterButton.OWNED.name(), myResourcesKeyValue, true));

		SelectionValues coursesValues = new SelectionValues();
		coursesValues.add(SelectionValues.entry(Filter.currentCourses.name(), translate("filter.execution.period.current")));
		coursesValues.add(SelectionValues.entry(Filter.upcomingCourses.name(), translate("filter.execution.period.upcoming")));
		coursesValues.add(SelectionValues.entry(Filter.oldCourses.name(), translate("filter.execution.period.finished")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.execution.period"),
				FilterButton.DATES.name(), coursesValues, true));

		SelectionValues bookingValues = new SelectionValues();
		bookingValues.add(SelectionValues.entry(Filter.asParticipant.name(), translate("filter.booked.participant")));
		bookingValues.add(SelectionValues.entry(Filter.asCoach.name(), translate("filter.booked.coach")));
		bookingValues.add(SelectionValues.entry(Filter.asAuthor.name(), translate("filter.booked.author")));
		if(!searchParams.isMembershipMandatory() && !searchParams.isMembershipOnly()) {
			bookingValues.add(SelectionValues.entry(Filter.notBooked.name(), translate("filter.not.booked")));
		}
		filters.add(new FlexiTableSingleSelectionFilter(translate("cif.resources.membership"),
				FilterButton.BOOKING.name(), bookingValues, true));

		SelectionValues passedValues = new SelectionValues();
		passedValues.add(SelectionValues.entry(Filter.passed.name(), translate("filter.passed")));
		passedValues.add(SelectionValues.entry(Filter.notPassed.name(), translate("filter.not.passed")));
		passedValues.add(SelectionValues.entry(Filter.withoutPassedInfos.name(), translate("filter.without.passed.infos")));
		filters.add(new FlexiTableSingleSelectionFilter(translate("cif.resources.score"),
				FilterButton.PASSED.name(), passedValues, true));
		
		// life-cycle
		SelectionValues lifecycleValues = new SelectionValues();
		lifecycleValues.add(SelectionValues.entry(FilterStatus.PREPARATION.name(), translate("cif.resources.status.preparation")));
		lifecycleValues.add(SelectionValues.entry(FilterStatus.ACTIVE.name(), translate("cif.resources.status.active")));
		lifecycleValues.add(SelectionValues.entry(FilterStatus.CLOSED.name(), translate("cif.resources.status.closed")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.resources.status"),
				FilterButton.STATUS.name(), lifecycleValues, true));
		
		// authors / owners
		filters.add(new FlexiTableTextFilter(translate("cif.author.search"), FilterButton.AUTHORS.name(), true));
		
		// educational type
		SelectionValues educationalTypeKV = new SelectionValues();
		educationalTypes
			.forEach(type -> educationalTypeKV.add(entry(type.getKey().toString(), StringHelper.escapeHtml(translate(RepositoyUIFactory.getI18nKey(type))))));
		educationalTypeKV.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("cif.educational.type"),
				FilterButton.EDUCATIONALTYPE.name(), educationalTypeKV, true));
		
		tableEl.setFilters(true, filters, true, false);
	}
	
	private void initSorters(FlexiTableElement tableElement) {
		List<FlexiTableSort> sorters = new ArrayList<>(14);
		sorters.add(new FlexiTableSort(translate("orderby.automatic"), OrderBy.automatic.name()));
		if (searchParams.getParentEntry() != null) {
			sorters.add(new FlexiTableSort(translate("orderby.custom"), OrderBy.custom.name()));
		}
		sorters.add(new FlexiTableSort(translate("orderby.favorit"), OrderBy.favorit.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lastVisited"), OrderBy.lastVisited.name()));
		sorters.add(new FlexiTableSort(translate("orderby.score"), OrderBy.score.name()));
		sorters.add(new FlexiTableSort(translate("orderby.passed"), OrderBy.passed.name()));
		sorters.add(new FlexiTableSort(translate("orderby.completion"), OrderBy.completion.name()));
		sorters.add(FlexiTableSort.SPACER);
		sorters.add(new FlexiTableSort(translate("orderby.title"), OrderBy.title.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lifecycle"), OrderBy.lifecycle.name()));
		sorters.add(new FlexiTableSort(translate("orderby.author"), OrderBy.author.name()));
		sorters.add(new FlexiTableSort(translate("orderby.creationDate"), OrderBy.creationDate.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lastModified"), OrderBy.lastModified.name()));
		if(repositoryModule.isRatingEnabled()) {
			sorters.add(new FlexiTableSort(translate("orderby.rating"), OrderBy.rating.name()));
		}
		
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(OrderBy.title.name(), true));
		tableElement.setSortSettings(options);
	}
	
	private void saveFilterPreferences(UserRequest ureq, List<FlexiTableFilter> filters) {
		ureq.getUserSession().getGuiPreferences()
		.putAndSave(RepositoryEntryListController.class, "rev-filters-".concat(name),
				FilterPreferences.valueOf(filters));
	}

	@Override
	public String getMapperThumbnailUrl() {
		return mapperThumbnailKey.getUrl();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty()) {
			ContextEntry entry = entries.get(0);
			String tabId = entry.getOLATResourceable().getResourceableTypeName();
			if(tableEl.getSelectedFilterTab() == null || !tableEl.getSelectedFilterTab().getId().equals(tabId)) {
				FlexiFiltersTab tab = tableEl.getFilterTabById(tabId);
				if(tab == null) {
					selectFilterTab(ureq, myTab);
				} else {
					selectFilterTab(ureq, tab);
				}
			} else {
				tableEl.addToHistory(ureq);
			}
		} else {
			selectFilterTab(ureq, myTab);
		}
		
		if(state instanceof RepositoryEntryListState se) {
			if(se.getTableState() != null) {
				tableEl.setStateEntry(ureq, se.getTableState());
			}
			if(se.getSearchEvent() != null) {
				doSearch(ureq, se.getSearchEvent());
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
		if(source instanceof RatingFormItem ratingItem) {
			RepositoryEntryRow row = (RepositoryEntryRow)ratingItem.getUserObject();
			doOpenDetails(ureq, row, "[Ratings:0]");
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			
			if("mark".equals(cmd)) {
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark" : "o_icon o_icon_bookmark_add");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if ("start".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpen(ureq, row, null);
			} else if ("details".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row, null);
			} else if ("select".equals(cmd)) {
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				if (row.isMember()) {
					doOpen(ureq, row, null);					
				} else {
					doOpenDetails(ureq, row, null);
				}
			} else if ("comments".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row, "[Comments:0]");
			} else if ("levels".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row, "[Taxonomy:0]");
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				RepositoryEntryRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					if (row.isMember()) {
						doOpen(ureq, row, null);					
					} else {
						doOpenDetails(ureq, row, null);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				RepositoryEntryListState state = new RepositoryEntryListState();
				state.setTableState(tableEl.getStateEntry());
				addToHistory(ureq, state);
			} else if(event instanceof FlexiTableFilterEvent ftfe) {
				saveFilterPreferences(ureq, ftfe.getFilters());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link) {
			Object uo = link.getUserObject();
			if(uo instanceof OrderBy sort) {
				for(Link order:orderByLinks) {
					removeCheck(order);
				}
				toggleCheck(link);
				doOrderBy(sort);
				flc.setDirty(true);
			} else if(uo instanceof Filter) {
				toggleCheck(link);
				List<Filter> selectedFilters = new ArrayList<>();
				for(Link filter:filterLinks) {
					String iconCss = filter.getIconLeftCSS();
					if(StringHelper.containsNonWhitespace(iconCss)) {
						selectedFilters.add((Filter)filter.getUserObject());
					}
				}
				doFilter(selectedFilters);
				flc.setDirty(true);
			}
		} else if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = Long.valueOf(rowKeyStr);
						List<RepositoryEntryRow> rows = model.getObjects();
						for(RepositoryEntryRow row:rows) {
							if(row != null && row.getKey().equals(rowKey)) {
								if (row.isMember()) {
									doOpen(ureq, row, null);					
								} else {
									doOpenDetails(ureq, row, null);
								}
							}
						}
					} catch (NumberFormatException e) {
						logWarn("Not a valid long: " + rowKeyStr, e);
					}
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void toggleCheck(Link link) {
		String iconCss = link.getIconLeftCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconLeftCSS(null);
		} else {
			link.setIconLeftCSS("o_icon o_icon_check o_icon-fw");
		}
	}
	
	private void removeCheck(Link link) {
		String iconCss = link.getIconLeftCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconLeftCSS(null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == detailsCtrl) {
			if (event instanceof BookedEvent) {
				doCloseDetails();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//do not update the 
	}
	
	private void doSearch(UserRequest ureq, SearchEvent se) {
		searchParams.setIdAndRefs(se.getId());
		searchParams.setAuthor(se.getAuthor());
		searchParams.setText(se.getDisplayname());
		searchParams.setMembershipMandatory(se.isMembershipMandatory() || searchParams.isMembershipOnly());
		if(se.getClosed() != null) {
			if(se.getClosed().booleanValue()) {
				searchParams.setEntryStatus(new RepositoryEntryStatusEnum[] { RepositoryEntryStatusEnum.closed });
			} else {
				searchParams.setEntryStatus(RepositoryEntryStatusEnum.preparationToPublished());
			}
		} else {
			searchParams.setEntryStatus(null);
		}
		tableEl.reset(true, true, true);
		
		RepositoryEntryListState state = new RepositoryEntryListState();
		state.setTableState(tableEl.getStateEntry());
		state.setSearchEvent(se);
		addToHistory(ureq, state);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if(tab == null) return;
		tableEl.setSelectedFilterTab(ureq, tab);
	}
	
	private void doFilter(List<Filter> filters) {	
		dataSource.setFilters(filters);
		tableEl.reset();
	}
	
	protected void doOrderBy(OrderBy orderBy) {
		dataSource.setOrderBy(orderBy);
		tableEl.reset();
	}
	
	protected void doRating(RepositoryEntryRow row, float rating) {
		OLATResourceable ores = row.getRepositoryEntryResourceable();
		userRatingsDao.updateRating(getIdentity(), ores, null, Math.round(rating));
	}
	
	protected void doOpen(UserRequest ureq, RepositoryEntryRow row, String subPath) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
			if (subPath != null) {
				businessPath += subPath;
			}
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	protected void doOpenDetails(UserRequest ureq, RepositoryEntryRow row, String additionalBusinessPath) {
		// to be more consistent: course members see info page within the course, non-course members see it outside the course
		if (row.isMember()) {
			String path = "[Infos:0]";
			if(StringHelper.containsNonWhitespace(additionalBusinessPath)) {
				path += additionalBusinessPath;
			}
			doOpen(ureq, row, path);
		} else {
			removeAsListenerAndDispose(detailsCtrl);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

			RepositoryEntry entry = repositoryService.loadByKey(row.getKey());
			if(entry == null) {
				showWarning("repositoryentry.not.existing");
			} else {
				detailsCtrl = new RepositoryEntryInfosController(ureq, bwControl, entry, false);
				listenTo(detailsCtrl);
				addToHistory(ureq, detailsCtrl);
				
				String displayName = row.getDisplayName();
				stackPanel.pushController(displayName, detailsCtrl);	
			}
		}
	}
	
	private void doCloseDetails() {
		removeAsListenerAndDispose(detailsCtrl);
		detailsCtrl = null;
		
		stackPanel.popUpToController(this);
		reloadRows();
	}
	
	protected boolean doMark(UserRequest ureq, RepositoryEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			
			EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.removeBookmark, name);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			
			EntryChangedEvent e = new EntryChangedEvent(row, getIdentity(), Change.addBookmark, name);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			return true;
		}
	}

	@Override
	public void forgeMarkLink(RepositoryEntryRow row) {
		if(!guestOnly) {
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON);
			markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setAriaLabel(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setUserObject(row);
			row.setMarkLink(markLink);
		}
	}
	
	@Override
	public void forgeCompletion(RepositoryEntryRow row) {
		if(!guestOnly && row.getCompletion() != null) {
			if (isConventionalUnpassedCourse(row)) {
				return;
			}
			
			float completion = row.getCompletion().floatValue();
			ProgressBarItem completionItem = new ProgressBarItem("completion_" + row.getKey(), 100,
					completion, Float.valueOf(1), null);
			completionItem.setWidthInPercent(true);
			completionItem.setLabelAlignment(LabelAlignment.none);
			completionItem.setRenderStyle(RenderStyle.radial);
			completionItem.setRenderSize(RenderSize.small);		
			completionItem.setBarColor(BarColor.neutral);	
			completionItem.setPercentagesEnabled(true);
			completionItem.setRenderLabels(RenderLabels.always);
			
			// Inline rendering of status
			if (row.isPassed()) {
				completionItem.setCssClass("o_progress_passed");
			} else if (row.isFailed()) {
				completionItem.setBarColor(BarColor.danger);					
				completionItem.setCssClass("o_progress_failed");
			}
			// Inline rendering of score
			if (StringHelper.containsNonWhitespace(row.getScore())) {				
				completionItem.setInfo(row.getScore() + "pt");
			}					
			row.setCompletionItem(completionItem);
		}
	}

	private boolean isConventionalUnpassedCourse(RepositoryEntryRow row) {
		if ("CourseModule".equals(row.getOLATResourceable().getResourceableTypeName())) {
			try {
				ICourse course = CourseFactory.loadCourse(row.getOLATResourceable());
				return ConditionNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())
						&& row.getCompletion().doubleValue() < 1.0;
			} catch (Exception e) {
				logError("", e);
			}
		}
		return false;
	}
	
	@Override
	public void forgeSelectLink(RepositoryEntryRow row) {
		String displayName = StringHelper.escapeHtml(row.getDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", displayName, null, null, Link.NONTRANSLATED);
		if(row.isClosed()) {
			selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
		}
		if(row.isMember()) {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
			selectLink.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathString(businessPath));
		}
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	@Override
	public void forgeStartLink(RepositoryEntryRow row) {
		String label;
		String iconCss;
		if(!row.isMember() && row.isPublicVisible() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty()) {
			label = "book";
			iconCss = "btn btn-sm btn-primary o_book ";
		} else {
			label = "open";
			iconCss = "btn btn-sm btn-primary o_start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS(iconCss);
		startLink.setIconRightCSS("o_icon o_icon_start");
		startLink.setTitle(label);
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		startLink.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathString(businessPath));
		row.setStartLink(startLink);
	}	
	
	@Override
	public void forgeDetails(RepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "learn.more", null, null, Link.LINK);
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-default o_details o_button_ghost");
		detailsLink.setIconRightCSS("o_icon o_icon_details");
		detailsLink.setTitle("learn.more");
		detailsLink.setGhost(true);
		detailsLink.setUserObject(row);
		if (row.isMember()) {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "][Infos:0]";
			detailsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathString(businessPath));
		}
		row.setDetailsLink(detailsLink);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	public void forgeRatings(RepositoryEntryRow row) {
		if(!repositoryModule.isRatingEnabled()) return;
		
		Double averageRating = row.getAverageRating();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
		RatingFormItem ratingEl = uifactory.addRatingItem("rat_" + row.getKey(), null,  averageRatingValue, 5, false, null);
		ratingEl.addActionListener(FormEvent.ONCLICK);
		ratingEl.setLargeIcon(false);
		row.setRatingFormItem(ratingEl);
		ratingEl.setUserObject(row);
	}
	
	@Override
	public void forgeTaxonomyLevels(RepositoryEntryRow row) {
		if(row.getNumOfTaxonomyLevels() <= 0) return;
		
		String title = Long.toString(row.getNumOfTaxonomyLevels());
		FormLink levelsLink = uifactory.addFormLink("tlevels_" + row.getKey(), "levels", title, null, null, Link.NONTRANSLATED);
		levelsLink.setUserObject(row);
		levelsLink.setCustomEnabledLinkCSS("o_taxonomy_levels");
		levelsLink.setIconLeftCSS("o_icon o_icon_tags");
		row.setTaxonomyLevelsLink(levelsLink);
	}

	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}
	
}