/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.catalog.ui.CatalogRepositoryEntryInfosController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.modules.curriculum.ui.CurriculumElementInfosController;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.InPreparationQueries;
import org.olat.repository.model.CurriculumElementInPreparation;
import org.olat.repository.model.RepositoryEntryInPreparation;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.olat.repository.ui.list.DefaultRepositoryEntryDataSource.FilterButton;
import org.olat.repository.ui.list.InPreparationDataModel.InPreparationCols;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InPreparationListController extends FormBasicController implements FlexiTableComponentDelegate {

	private FlexiFiltersTab allTab;
	
	private FlexiTableElement tableEl;
	private InPreparationDataModel tableModel;
	private BreadcrumbedStackedPanel stackPanel;

	private final MapperKey repositoryEntryMapperKey;
	private final String curriculumElementImageMapperUrl;
	private final CurriculumElementImageMapper curriculumElementImageMapper;

	private int count = 0;
	private List<RepositoryEntryEducationalType> educationalTypes;
	
	private Controller infosCtrl;

	@Autowired
	private MarkManager markManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private InPreparationQueries inPreparationQueries;
	@Autowired
	private LifecycleModule lifecycleModule;
	
	public InPreparationListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "inpreparation");
		setTranslator(Util.createPackageTranslator(OpenAccessOfferController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		repositoryEntryMapperKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		curriculumElementImageMapper = new CurriculumElementImageMapper(curriculumService);
		curriculumElementImageMapperUrl = registerCacheableMapper(ureq, CurriculumElementImageMapper.DEFAULT_ID,
				curriculumElementImageMapper, CurriculumElementImageMapper.DEFAULT_EXPIRATION_TIME);
		educationalTypes = repositoryManager.getAllEducationalTypes();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InPreparationCols.type,
				new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InPreparationCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.externalRef));

		if (lifecycleModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, InPreparationCols.lifecycleSoftkey));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.lifecycleLabel));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InPreparationCols.lifecycleStart,
					new DateFlexiCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InPreparationCols.lifecycleEnd,
					new DateFlexiCellRenderer(getLocale())));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InPreparationCols.educationalType,
				new EducationalTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InPreparationCols.detailsSmall));
		
		tableModel = new InPreparationDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setSearchEnabled(true);
		
		VelocityContainer row = createVelocityContainer("row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		
		initFilterPresets(ureq);
		initFilters();
		initSorters(tableEl);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "re-list-in-preparation-v1");
	}
	
	private void initFilterPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("search.all"),
				TabSelectionBehavior.reloadData, List.of());
		allTab.setElementCssClass("o_sel_inpreparation_all");
		tabs.add(allTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// authors / owners
		filters.add(new FlexiTableTextFilter(translate("cif.author"), FilterButton.AUTHORS.name(), true));
		
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
		sorters.add(new FlexiTableSort(translate("orderby.title"), InPreparationCols.displayName.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lifecycle"), InPreparationCols.lifecycleStart.name()));
		sorters.add(new FlexiTableSort(translate("orderby.author"), InPreparationCols.authors.name()));
		sorters.add(new FlexiTableSort(translate("orderby.creationDate"), InPreparationCols.creationDate.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lastModified"), InPreparationCols.lastModified.name()));

		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(InPreparationCols.displayName.name(), true));
		tableElement.setSortSettings(options);
	}
	
	protected void reloadRows() {
		loadModel();
	}
	
	private void loadModel() {
		List<InPreparationRow> rows = new ArrayList<>();
		
		List<CurriculumElementInPreparation> elements = inPreparationQueries.searchCurriculumElementsInPreparation(getIdentity());
		Set<Long> entriesKeys = new HashSet<>();
		for(CurriculumElementInPreparation element:elements) {
			rows.add(forgeRow(element));
			if(element.entry() != null) {
				entriesKeys.add(element.entry().getKey());
			}
		}
		
		List<RepositoryEntryInPreparation> entries = inPreparationQueries.searchRepositoryEntriesInPreparation(getIdentity());
		for(RepositoryEntryInPreparation entry:entries) {
			if(entriesKeys.contains(entry.entry().getKey())) {
				continue;
			}
			rows.add(forgeRow(entry));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private InPreparationRow forgeRow(RepositoryEntryInPreparation entry) {
		InPreparationRow row = new InPreparationRow(Long.valueOf(++count), entry.entry(), entry.marked());
		forgeDetailsLink(row);
		forgeSelectLink(row);
		forgeMarkLink(row);
		
		List<TaxonomyLevelNamePath> taxonomyLevels = (entry.levels() != null) 
				? TaxonomyUIFactory.getNamePaths(getTranslator(), entry.levels())
				: List.of();
		row.setTaxonomyLevels(taxonomyLevels);

		VFSLeaf image = repositoryManager.getImage(entry.entry().getKey(), entry.entry().getOlatResource());
		if(image != null) {
			row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(repositoryEntryMapperKey.getUrl(), image));
		}
		return row;
	}
	
	private InPreparationRow forgeRow(CurriculumElementInPreparation element) {
		InPreparationRow row = new InPreparationRow(Long.valueOf(++count), element.element(), element.entry(), element.marked());
		forgeDetailsLink(row);
		forgeSelectLink(row);
		forgeMarkLink(row);
		
		List<TaxonomyLevelNamePath> taxonomyLevels = (element.levels() != null) 
				? TaxonomyUIFactory.getNamePaths(getTranslator(), element.levels())
				: List.of();
		row.setTaxonomyLevels(taxonomyLevels);
		
		String imageUrl = curriculumElementImageMapper.getImageUrl(curriculumElementImageMapperUrl,
				row::getCurriculumElementKey, CurriculumElementFileType.teaserImage);
		if (imageUrl != null) {
			row.setThumbnailRelPath(imageUrl);
		}
		return row;
	}
	
	private void forgeDetailsLink(InPreparationRow row) {
		String url = "";
	
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getOlatResource().getKey(), "details", "learn.more", null, flc, Link.LINK);
		detailsLink.setIconRightCSS("o_icon o_icon_details");
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-primary o_details o_in_preparation");
		detailsLink.setTitle("details");
		detailsLink.setUrl(url);
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
		
		FormLink detailsSmallLink = uifactory.addFormLink("details_small_" + row.getOlatResource().getKey(), "details", "learn.more", null, null, Link.LINK);
		detailsSmallLink.setCustomEnabledLinkCSS("btn btn-xs btn-primary o_details o_in_preparation");
		detailsSmallLink.setTitle("details");
		detailsSmallLink.setUrl(url);
		detailsSmallLink.setUserObject(row);
		
		row.setDetailsSmallLink(detailsSmallLink);
	}
	
	private void forgeSelectLink(InPreparationRow row) {
		String displayName = StringHelper.escapeHtml(row.getDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_" + row.getOlatResource().getKey(), "select", displayName, tableEl, Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}
	
	private void forgeMarkLink(InPreparationRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getOlatResource().getKey(), "mark", "", tableEl, Link.NONTRANSLATED);
		markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setAriaLabel(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(4);
		if(rowObject instanceof InPreparationRow inRow) {
			if(inRow.getDetailsLink() != null) {
				cmps.add(inRow.getDetailsLink().getComponent());
			}
			if(inRow.getDetailsSmallLink() != null) {
				cmps.add(inRow.getDetailsSmallLink().getComponent());
			}
			if(inRow.getSelectLink() != null) {
				cmps.add(inRow.getSelectLink().getComponent());
			}
			if(inRow.getMarkLink() != null) {
				cmps.add(inRow.getMarkLink().getComponent());
			}
		}
		return cmps;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = Long.valueOf(rowKeyStr);
						InPreparationRow row = tableModel.getObjectByKey(rowKey);
						if(row != null) {
							doOpenDetails(ureq, row);
						}
					} catch (NumberFormatException e) {
						logWarn("Not a valid long: " + rowKeyStr, e);
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//do not update the 
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof FlexiTableSearchEvent) {
				tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
				tableEl.reset(true, true, false);
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof InPreparationRow row) {
			if(("details".equals(link.getCmd()) || "select".equals(link.getCmd()))) {
				doOpenDetails(ureq, row);
			} else if("mark".equals(link.getCmd())) {
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenDetails(UserRequest ureq, InPreparationRow row) {
		if (row.getCurriculumElementKey() != null) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row::getCurriculumElementKey);
			CurriculumElementType type = curriculumElement.getType();
			RepositoryEntry entry = row.getRepositoryEntryKey() != null
					? repositoryService.loadByKey(row.getRepositoryEntryKey())
					: null;
			// Show single course implementation in implementation info, course info otherwise
			if(entry != null && (type == null || type.getMaxRepositoryEntryRelations() == -1 || !type.isSingleElement())) {
				doOpenDetails(ureq, entry);
			} else {
				doOpenDetails(ureq, curriculumElement, entry);
			}
		} else if (row.getRepositoryEntryKey() != null) {
			RepositoryEntry entry = repositoryService.loadByKey(row.getRepositoryEntryKey());
			doOpenDetails(ureq, entry);
		} 
	}
	
	private void doOpenDetails(UserRequest ureq, RepositoryEntry entry) {
		if (entry != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createOfferOres(entry.getOlatResource());
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			infosCtrl = new CatalogRepositoryEntryInfosController(ureq, bwControl, entry);
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = entry.getDisplayname();
			stackPanel.pushController(displayName, infosCtrl);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
		} else {
			tableEl.reloadData();
		}
	}
	
	private void doOpenDetails(UserRequest ureq, CurriculumElement curriculumElement, RepositoryEntry entry) {
		if (curriculumElement != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createOfferOres(curriculumElement.getResource());
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			infosCtrl = new CurriculumElementInfosController(ureq, bwControl, curriculumElement, entry, getIdentity(), false);
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = curriculumElement.getDisplayName();
			stackPanel.pushController(displayName, infosCtrl);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
		} else {
			tableEl.reloadData();
		}
	}
	
	private boolean doMark(UserRequest ureq, InPreparationRow row) {
		String businessPath;
		OLATResourceable item;
		if(row.getRepositoryEntryKey() != null) {
			item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getRepositoryEntryKey());
			businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		} else if(row.getCurriculumElementKey() != null) {
			item = OresHelper.createOLATResourceableInstance("CurriculumElement", row.getCurriculumElementKey());
			businessPath = "[MyCoursesSite:0][CurriculumElement:" + item.getResourceableId() + "]";
		} else {
			return false;
		}

		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			sendBookmarkEvent(ureq, row, Change.removeBookmark);
			return false;
		} 
		markManager.setMark(item, getIdentity(), null, businessPath);
		sendBookmarkEvent(ureq, row, Change.addBookmark);
		return true;
	}
	
	private void sendBookmarkEvent(UserRequest ureq, InPreparationRow row, Change change) {
		if(row.getRepositoryEntryKey() != null) {
			RepositoryEntryRef ref = new RepositoryEntryRefImpl(row.getRepositoryEntryKey());
			EntryChangedEvent e = new EntryChangedEvent(ref, getIdentity(), change, "re-list-in-preparation");
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
		}
	}
}
