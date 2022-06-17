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
package org.olat.modules.catalog.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataModel.CatalogRepositoryEntryCols;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryDataSource.CatalogRepositoryEntryRowItemCreator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.ACRenderer;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogRepositoryEntryListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate, CatalogRepositoryEntryRowItemCreator {
	
	public static final String FILTER_SPECIAL_RE_KEYS = "filterspecialre";
	
	private BreadcrumbedStackedPanel stackPanel;
	private FlexiTableElement tableEl;
	private CatalogRepositoryEntryDataModel dataModel;
	private CatalogRepositoryEntryDataSource dataSource;
	private final CatalogRepositoryEntrySearchParams searchParams;
	
	private CatalogRepositoryEntryInfosController infosCtrl;
	
	private final boolean withSearch;
	private final MapperKey mapperThumbnailKey;
	private final TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private MapperService mapperService;

	public CatalogRepositoryEntryListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel, 
			CatalogRepositoryEntrySearchParams searchParams, TaxonomyLevel taxonomyLevel, boolean withSearch) {
		super(ureq, wControl, "entry_list");
		// Order of the translators matters.
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale()));
		setTranslator(Util.createPackageTranslator(CatalogRepositoryEntryListController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.searchParams = searchParams;
		this.taxonomyLevel = taxonomyLevel;
		this.withSearch = withSearch;
		this.dataSource = new CatalogRepositoryEntryDataSource(searchParams, withSearch, this, getLocale());
		this.mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		initForm(ureq);
		tableEl.reloadData();
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (taxonomyLevel != null) {
			flc.contextPut("square", CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogModule.getLauncherTaxonomyLevelStyle()));
			flc.contextPut("description", taxonomyLevel.getDescription());
			
			List<TaxonomyLevel> taxonomyLevels = taxonomyLevelDao.getChildren(taxonomyLevel);
			Comparator<TaxonomyLevel> comparator = Comparator.comparing(TaxonomyLevel::getSortOrder, Comparator.nullsLast(Comparator.reverseOrder()))
					.thenComparing(TaxonomyLevel::getDisplayName);
			taxonomyLevels.sort(comparator);
			List<TaxonomyItem> items = new ArrayList<>(taxonomyLevels.size());
			for (TaxonomyLevel child : taxonomyLevels) {
				String selectLinkName = "o_tl_" + child.getKey();
				FormLink selectLink = uifactory.addFormLink(selectLinkName, "select_tax", child.getDisplayName(), null, formLayout, Link.NONTRANSLATED);
				selectLink.setUserObject(child.getKey());
				
				TaxonomyItem item = new TaxonomyItem(child.getKey(), selectLinkName, null);
				items.add(item);
			}
			flc.contextPut("taxonomyLevels", items);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.type, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.displayName));
		if (repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.externalId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleLabel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleSoftkey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogRepositoryEntryCols.educationalType, new EducationalTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.offers, new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.details));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogRepositoryEntryCols.start));

		dataModel = new CatalogRepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(withSearch);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", "o_CourseModule_icon");
		tableEl.setElementCssClass("o_coursetable");
		// Is (more or less) the same visualization as row_1.html
		VelocityContainer row = createVelocityContainer("catalog_repository_entry_row");
		row.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(row, this);
		
		
		initFilters();
		tableEl.setAndLoadPersistedPreferences(ureq, "catalog-v2-relist");
	}
	
	private void initFilters() {
		CatalogFilterSearchParams filterSearchParams = new CatalogFilterSearchParams();
		filterSearchParams.setEnabled(Boolean.TRUE);
		List<CatalogFilter> catalogFilters = catalogService.getCatalogFilters(filterSearchParams);
		List<FlexiTableExtendedFilter> flexiTableFilters = new ArrayList<>(catalogFilters.size());
		for (CatalogFilter catalogFilter : catalogFilters) {
			CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(catalogFilter.getType());
			if (handler != null && handler.isEnabled(searchParams.isGuestOnly())) {
				FlexiTableExtendedFilter flexiTableFilter = handler.createFlexiTableFilter(getTranslator(), catalogFilter);
				if (flexiTableFilter != null) {
					flexiTableFilters.add(flexiTableFilter);
				}
			}
		}
		if (!flexiTableFilters.isEmpty()) {
			tableEl.setFilters(true, flexiTableFilters, false, false);
		}
	}
	
	private void addSpecialFilter(String label, Collection<Long> repositoryEntryKeys) {
		List<FlexiTableExtendedFilter> flexiTableFilters = tableEl.getExtendedFilters();
		flexiTableFilters.removeIf(f -> f.getFilter().equals(FILTER_SPECIAL_RE_KEYS));
		
		SelectionValues specialKV = new SelectionValues();
		specialKV.add(SelectionValues.entry("key", label));
		FlexiTableMultiSelectionFilter filter = new FlexiTableMultiSelectionFilter(label, FILTER_SPECIAL_RE_KEYS, specialKV, true);
		filter.setValues(Collections.singletonList("key"));
		filter.setUserObject(repositoryEntryKeys);
		flexiTableFilters.add(filter);
		tableEl.setFilters(true, flexiTableFilters, false, false);
	}

	@Override
	public void forgeSelectLink(CatalogRepositoryEntryRow row) {
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
	public void forgeStartLink(CatalogRepositoryEntryRow row) {
		String cmd;
		String label;
		String iconCss;
		FormLink link = null;
		if(!row.isMember() && row.isPublicVisible() && !row.isOpenAccess() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty()) {
			cmd = "book";
			label = "book";
			iconCss = "btn btn-sm btn-primary o_book ";
		} else {
			cmd = "start";
			label = "start";
			iconCss = "btn btn-sm btn-primary o_start";
		}
		link = uifactory.addFormLink("start_" + row.getKey(), cmd, label, null, null, Link.LINK);
		link.setUserObject(row);
		link.setCustomEnabledLinkCSS(iconCss);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setTitle(label);
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		link.setUrl(BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathString(businessPath));
		row.setStartLink(link);
	}	
	
	@Override
	public void forgeDetailsLink(CatalogRepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-default o_details");
		detailsLink.setTitle("details");
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
	}
	
	@Override
	public void forgeThumbnail(CatalogRepositoryEntryRow row) {
		VFSLeaf image = repositoryManager.getImage(row.getKey(), row.getOlatResource());
		if (image != null) {
			row.setThumbnailRelPath(mapperThumbnailKey.getUrl() + "/" + image.getName());
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (state instanceof CatalogRepositoryEntryState) {
			CatalogRepositoryEntryState catalogState = (CatalogRepositoryEntryState)state;
			if (catalogState.getSpecialFilterRepositoryEntryKeys() != null) {
				addSpecialFilter(catalogState.getSpecialFilterRepositoryEntryLabel(), catalogState.getSpecialFilterRepositoryEntryKeys());
				tableEl.reset(true, true, true);
			}
		}
		
		if (entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if ("Infos".equalsIgnoreCase(type)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			tableEl.resetSearch(ureq);
			dataModel.clear();
			dataModel.load(null, null, 0, -1);
			CatalogRepositoryEntryRow row = dataModel.getObjectByKey(key);
			if (row != null) {
				int index = dataModel.getObjects().indexOf(row);
				if (index >= 1 && tableEl.getPageSize() > 1) {
					int page = index / tableEl.getPageSize();
					tableEl.setPage(page);
				}
				doOpenDetails(ureq, row);
				if (infosCtrl != null) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					infosCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}

	public void search(UserRequest ureq, String searchString, boolean reset) {
		searchParams.setSearchString(searchString);
		if (reset) {
			tableEl.resetSearch(ureq);
		} else {
			tableEl.reset(true, true, true);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select_taxonomy");
			if (StringHelper.containsNonWhitespace(key)) {
				fireEvent(ureq, new OpenTaxonomyEvent(Long.valueOf(key)));
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if ("start".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doStart(ureq, row);
			} else if ("book".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doBook(ureq, row);
			} else if ("details".equals(cmd) || "select".equals(cmd)){
				CatalogRepositoryEntryRow row = (CatalogRepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row);
			} else if ("select_tax".equals(cmd)){
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new OpenTaxonomyEvent(Long.valueOf(key)));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStart(UserRequest ureq, CatalogRepositoryEntryRow row) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			showError("error.corrupted");
		}
	}

	private void doBook(UserRequest ureq, CatalogRepositoryEntryRow row) {
		doOpenDetails(ureq, row);
		
		if (infosCtrl != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("Offers");
			ContextEntry contextEntry = BusinessControlFactory.getInstance().createContextEntry(ores);
			infosCtrl.activate(ureq, List.of(contextEntry), null);
		}
	}
	
	protected void doOpenDetails(UserRequest ureq, CatalogRepositoryEntryRow row) {
		removeAsListenerAndDispose(infosCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", row.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(row.getKey());
		if(entry != null) {
			infosCtrl = new CatalogRepositoryEntryInfosController(ureq, bwControl, stackPanel, entry);
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = row.getDisplayName();
			stackPanel.pushController(displayName, infosCtrl);
		} else {
			tableEl.reloadData();
		}
	}
	
	public static final class TaxonomyItem {
		
		private final Long key;
		private final String selectLinkName;
		private final String thumbnailRelPath;
		
		public TaxonomyItem(Long key, String selectLinkName, String thumbnailRelPath) {
			this.key = key;
			this.selectLinkName = selectLinkName;
			this.thumbnailRelPath = thumbnailRelPath;
		}

		public Long getKey() {
			return key;
		}

		public String getSelectLinkName() {
			return selectLinkName;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}
		
	}

}
