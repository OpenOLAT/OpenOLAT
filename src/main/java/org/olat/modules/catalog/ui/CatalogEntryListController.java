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
package org.olat.modules.catalog.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.login.LoginProcessController;
import org.olat.login.LoginProcessEvent;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.CatalogEntryDataModel.CatalogEntryCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.modules.curriculum.ui.CurriculumElementInfosController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyLevelTeaserImageMapper;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.ACRenderer;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogEntryListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {
	
	private BreadcrumbedStackedPanel stackPanel;
	private FlexiTableElement tableEl;
	private CatalogEntryDataModel dataModel;
	private final CatalogEntrySearchParams searchParams;
	private final CatalogEntryListParams listParams;
	
	private Controller infosCtrl;
	private LightboxController lightboxCtrl;
	private WebCatalogAuthController authCtrl;
	
	private String headerSearchString;
	private final MapperKey repositoryEntryMapperKey;
	private final TaxonomyLevelTeaserImageMapper taxonomyLevelMapper;
	private final MapperKey taxonomyLevelMapperKey;
	private final CurriculumElementImageMapper curriculumElementImageMapper;
	private final String curriculumElementImageMapperUrl;

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
	private CurriculumService curriculumService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private MapperService mapperService;

	public CatalogEntryListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel, 
			CatalogEntrySearchParams searchParams, CatalogEntryListParams listParams) {
		super(ureq, wControl, "entry_list");
		// Order of the translators matters.
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale()));
		setTranslator(Util.createPackageTranslator(CatalogEntryListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(OpenAccessOfferController.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.searchParams = searchParams;
		this.listParams = listParams;
		this.taxonomyLevel = searchParams.getLauncherTaxonomyLevels() != null && !searchParams.getLauncherTaxonomyLevels().isEmpty()
				? searchParams.getLauncherTaxonomyLevels().get(0)
				: null;
		this.repositoryEntryMapperKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		this.taxonomyLevelMapper = new TaxonomyLevelTeaserImageMapper();
		this.taxonomyLevelMapperKey = mapperService.register(null, "taxonomyLevelTeaserImage", taxonomyLevelMapper);
		this.curriculumElementImageMapper = new CurriculumElementImageMapper(curriculumService);
		this.curriculumElementImageMapperUrl = registerCacheableMapper(ureq, CurriculumElementImageMapper.DEFAULT_ID,
				curriculumElementImageMapper, CurriculumElementImageMapper.DEFAULT_EXPIRATION_TIME);
		
		initForm(ureq);
		loadModel(true);
		setWindowTitle();
	}

	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (taxonomyLevel != null) {
			flc.contextPut("square", CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogModule.getLauncherTaxonomyLevelStyle()));
			flc.contextPut("description", TaxonomyUIFactory.translateDescription(getTranslator(), taxonomyLevel));
			
			List<TaxonomyLevel> taxonomyLevels = taxonomyLevelDao.getChildren(taxonomyLevel);
			List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(searchParams);
			catalogEntries.removeIf(this::shouldExcludeCatalogEntry);
			catalogService.excludeLevelsWithoutEntries(taxonomyLevels, catalogEntries);
			taxonomyLevels.sort(CatalogV2UIFactory.getTaxonomyLevelComparator(getTranslator()));
			List<TaxonomyItem> items = new ArrayList<>(taxonomyLevels.size());
			for (TaxonomyLevel child : taxonomyLevels) {
				String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), child);
				
				String selectLinkName = "o_tl_" + child.getKey();
				FormLink selectLink = uifactory.addFormLink(selectLinkName, "select_tax", displayName, null, formLayout, Link.NONTRANSLATED);
				selectLink.setUserObject(child.getKey());
				
				TaxonomyItem item = new TaxonomyItem(child.getKey(), displayName, selectLinkName);
				
				String imageUrl = taxonomyLevelMapper.getImageUrl(child);
				if (StringHelper.containsNonWhitespace(imageUrl)) {
					item.setThumbnailRelPath(taxonomyLevelMapperKey.getUrl() + "/" + imageUrl);
				}
				
				items.add(item);
			}
			flc.contextPut("taxonomyLevels", items);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.type, new ResourceTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.title));
		if (repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.externalId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleLabel));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleSoftkey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.mainLanguage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.expenditureOfWork));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.educationalType, new EducationalTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.taxonomyLevels, new TaxonomyLevelRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.offers, new ACRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.detailsSmall));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.startSmall));

		dataModel = new CatalogEntryDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(listParams.isWithSearch());
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", "o_CourseModule_icon");
		tableEl.setElementCssClass("o_coursetable");
		// Is (more or less) the same visualization as row_1.html
		VelocityContainer row = createVelocityContainer("catalog_entry_row");
		row.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(row, this);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "catalog-v2-relist-2");
	}

	private boolean shouldExcludeCatalogEntry(CatalogEntry catalogEntry) {
		if (listParams.isExcludeRepositoryEntries()) {
			if (catalogEntry.getRepositoryEntryKey() != null) {
				return true;
			}
		}
		
		if (listParams.isExcludeMembers() && catalogEntry.isMember()) {
			return true;
		}
		
		if (catalogEntry.getCurriculumElementKey() != null && listParams.getExcludedCurriculumElementKeys() != null) {
			if (listParams.getExcludedCurriculumElementKeys().contains(catalogEntry.getCurriculumElementKey())) {
				return true;
			}
		}

		if (listParams.getExcludedAccessMethodTypes() != null) {
			Set<String> excludedAccessMethodTypes = listParams.getExcludedAccessMethodTypes();
			if (catalogEntry.getResourceAccess() != null) {
				for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
					for (PriceMethodBundle priceMethodBundle : resourceAccess.getMethods()) {
						if (excludedAccessMethodTypes.contains(priceMethodBundle.getMethod().getType())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private void initFilters(List<CatalogEntry> catalogEntries) {
		CatalogFilterSearchParams filterSearchParams = new CatalogFilterSearchParams();
		filterSearchParams.setEnabled(Boolean.TRUE);
		List<CatalogFilter> catalogFilters = catalogService.getCatalogFilters(filterSearchParams);
		if (catalogFilters.isEmpty()) {
			return;
		}
		
		List<FlexiTableExtendedFilter> flexiTableFilters = new ArrayList<>(catalogFilters.size());
		for (CatalogFilter catalogFilter : catalogFilters) {
			CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(catalogFilter.getType());
			if (handler != null && handler.isEnabled(searchParams.isGuestOnly())) {
				TaxonomyLevel launcherTaxonomyLevel = searchParams.getLauncherTaxonomyLevels() != null && searchParams.getLauncherTaxonomyLevels().size() == 1
						? searchParams.getLauncherTaxonomyLevels().get(0)
						: null;
				FlexiTableExtendedFilter flexiTableFilter = handler.createFlexiTableFilter(getTranslator(),
						catalogFilter, catalogEntries, launcherTaxonomyLevel);
				if (flexiTableFilter != null) {
					flexiTableFilters.add(flexiTableFilter);
				}
			}
		}
		if (!flexiTableFilters.isEmpty()) {
			tableEl.setFilters(true, flexiTableFilters, false, false);
			tableEl.expandFilters(true);
		}
	}
	
	private void loadModel(boolean initFilters) {
		List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(searchParams);
		catalogEntries.removeIf(this::shouldExcludeCatalogEntry);
		if(initFilters) {
			initFilters(catalogEntries);
		}
		
		List<CatalogEntryRow> rows = catalogEntries.stream()
				.map(this::toRow)
				.collect(Collectors.toList());
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters != null) {
			for(FlexiTableFilter filter:filters) {
				CatalogFilterHandler handler = catalogService.getCatalogFilterHandler(filter.getFilter());
				if (handler != null) {
					handler.filter(filter, rows);
				}
			}
		}
		
		applySearch(rows);
		
		rows.forEach(this::forgeLinks);
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}
	
	private void applySearch(List<CatalogEntryRow> rows) {
		String searchValue = listParams.isWithSearch() ? tableEl.getQuickSearchString(): headerSearchString;
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> 
					containsNot(searchValues, row.getTitle())
					&& containsNot(searchValues, row.getExternalRef())
					&& containsNot(searchValues, row.getAuthors())
					&& containsNot(searchValues, row.getTaxonomyLevelNamePaths())
				);
		}
	}
	
	private boolean containsNot(List<String> searchValues, List<TaxonomyLevelNamePath> taxonomyLevelNamePaths) {
		if (taxonomyLevelNamePaths == null || taxonomyLevelNamePaths.isEmpty()) {
			return true;
		}
		for (TaxonomyLevelNamePath taxonomyLevelNamePath : taxonomyLevelNamePaths) {
			if (!containsNot(searchValues, taxonomyLevelNamePath.getDisplayName())) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}
	
	private CatalogEntryRow toRow(CatalogEntry catalogEntry) {
		CatalogEntryRow row = new CatalogEntryRow(catalogEntry);
		
		List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), row.getTaxonomyLevels());
		row.setTaxonomyLevelNamePaths(taxonomyLevels);
		
		if (catalogEntry.isPublicVisible()) {
			
			Set<String> accessMethodTypes = new HashSet<>(2);
			List<PriceMethod> priceMethods = new ArrayList<>(2);
			for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
				for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
					accessMethodTypes.add(bundle.getMethod().getType());
					priceMethods.add(toPriceMethod(bundle));
				}
			}
			
			if (catalogEntry.isOpenAccess()) {
				priceMethods.add(new PriceMethod(null, "o_ac_openaccess_icon", translate("open.access.name")));
			}
			
			if (!accessMethodTypes.isEmpty()) {
				row.setAccessMethodTypes(accessMethodTypes);
			}
			
			updateAccessInfo(row, catalogEntry);
			updateAccessMaxParticipants(row, catalogEntry);
		}
		
		return row;
	}

	private void updateAccessInfo(CatalogEntryRow row, CatalogEntry catalogEntry) {
		if (searchParams.isGuestOnly() || row.isMember() || catalogEntry.isOpenAccess()) {
			return;
		}
		
		for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(FreeAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(translate("access.info.freely.available"));
					return;
				}
			}
		}
		
		for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(TokenAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(translate("access.info.token"));
					return;
				}
			}
		}
		
		BigDecimal lowestPriceAmount = null;
		String lowestPrice = null;
		int numOfPrices = 0;
		for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				Price p = bundle.getPrice();
				String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
				if (p != null && StringHelper.containsNonWhitespace(price)) {
					numOfPrices++;
					if (lowestPriceAmount == null || lowestPriceAmount.compareTo(p.getAmount()) > 0) {
						lowestPriceAmount = p.getAmount();
						lowestPrice = price;
					}
				}
			}
		}
		if (lowestPriceAmount != null) {
			if (numOfPrices > 1) {
				lowestPrice = translate("book.price.from", lowestPrice);
			}
			row.setAccessInfo(lowestPrice);
		}
	}

	private void updateAccessMaxParticipants(CatalogEntryRow row, CatalogEntry catalogEntry) {
		if (searchParams.isGuestOnly() || row.isMember() || catalogEntry.isOpenAccess() || row.getMaxParticipants() == null) {
			return;
		}
		
		if (row.getNumParticipants() != null) {
			if (row.getNumParticipants() >= row.getMaxParticipants()) {
				row.setAccessError("<i class=\"o_icon o_ac_offer_fully_booked_icon\"> </i> " + translate("book.fully.booked"));
			} else {
				Double participantsLeftMessagePercentage = acModule.getParticipantsLeftMessagePercentage();
				if (participantsLeftMessagePercentage != null) {
					long leftParticipants = row.getMaxParticipants() - row.getNumParticipants();
					double leftParticipantsPercentage = leftParticipants * 100l / row.getMaxParticipants();
					if (leftParticipants == 1) {
						row.setAccessWarning("<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.single"));
					} else if (leftParticipantsPercentage < participantsLeftMessagePercentage) {
						row.setAccessWarning("<i class=\"o_icon o_ac_offer_almost_fully_booked_icon\"> </i> " + translate("book.participants.left.multi", String.valueOf(leftParticipants)));
					}
				}
			}
		}
	}

	private PriceMethod toPriceMethod(PriceMethodBundle bundle) {
		String type = bundle.getMethod().getMethodCssClass() + "_icon";
		String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
		AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
		String displayName = amh.getMethodName(getLocale());
		return new PriceMethod(price, type, displayName);
	}
	
	private void forgeLinks(CatalogEntryRow row) {
		forgeSelectLink(row);
		forgeStartLink(row);
		forgeDetailsLink(row);
		forgeThumbnail(row);
	}

	private void forgeSelectLink(CatalogEntryRow row) {
		String displayName = StringHelper.escapeHtml(row.getTitle());
		FormLink selectLink = uifactory.addFormLink("select_" + row.getOlatResource().getKey(), "select", displayName, null, null, Link.NONTRANSLATED);
		if(row.isClosed()) {
			selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
		}
		if(row.isMember() || (!searchParams.isGuestOnly() && row.isPublicVisible() && row.isOpenAccess())) {
			selectLink.setUrl(getStartUrl(row, false));
		} else {
			selectLink.setUrl(CatalogBCFactory.get(searchParams.isWebPublish()).getOfferUrl(row.getOlatResource()));
		}
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	private void forgeStartLink(CatalogEntryRow row) {
		String url = getStartUrl(row, searchParams.isWebPublish() && row.isGuestAccess());
		
		String cmd = "start";
		String label = "open";
		if (searchParams.isWebPublish() && row.isGuestAccess()) {
			ExternalLinkItem link = uifactory.addExternalLink("start_" + row.getOlatResource().getKey(), url, "_self", null);
			link.setCssClass("btn btn-sm btn-primary");
			link.setIconRightCSS("o_icon o_icon_start");
			link.setName(translate("start.guest"));
			row.setStartLink(link);
			
			ExternalLinkItem linkSmall = uifactory.addExternalLink("starts_" + row.getOlatResource().getKey(), url, "_self", null);
			linkSmall.setCssClass("btn btn-xs btn-primary");
			linkSmall.setIconRightCSS("o_icon o_icon_start");
			linkSmall.setName(translate("start.guest"));
			row.setStartSmallLink(linkSmall);
			return;
		}
		
		if (!searchParams.isGuestOnly() && !row.isMember() && row.isPublicVisible() && !row.isOpenAccess() && row.getAccessMethodTypes() != null && !row.getAccessMethodTypes().isEmpty()) {
			cmd = "book";
			label = "book";
		}
		
		FormLink link = uifactory.addFormLink("start_" + row.getOlatResource().getKey(), cmd, label, null, flc, Link.BUTTON_SMALL);
		link.setUserObject(row);
		link.setPrimary(true);
		link.setIconRightCSS("o_icon o_icon_start");
		link.setTitle(label);
		link.setUrl(url);
		row.setStartLink(link);
		
		FormLink linkSmall = uifactory.addFormLink("starts_" + row.getOlatResource().getKey(), cmd, label, null, null, Link.BUTTON_XSMALL);
		linkSmall.setUserObject(row);
		linkSmall.setPrimary(true);
		linkSmall.setIconRightCSS("o_icon o_icon_start");
		linkSmall.setTitle(label);
		linkSmall.setUrl(url);
		row.setStartSmallLink(linkSmall);
		
		if (StringHelper.containsNonWhitespace(row.getAccessError())) {
			link.setEnabled(false);
			linkSmall.setEnabled(false);
		}
	}

	private void forgeDetailsLink(CatalogEntryRow row) {
		String url = CatalogBCFactory.get(searchParams.isWebPublish()).getOfferUrl(row.getOlatResource());
		
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getOlatResource().getKey(), "details", "learn.more", null, flc, Link.LINK);
		detailsLink.setIconRightCSS("o_icon o_icon_details");
		detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-default o_details");
		detailsLink.setTitle("details");
		detailsLink.setUrl(url);
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
		
		FormLink detailsSmallLink = uifactory.addFormLink("details_small_" + row.getOlatResource().getKey(), "details", "learn.more", null, null, Link.LINK);
		detailsSmallLink.setCustomEnabledLinkCSS("btn btn-xs btn-default o_details");
		detailsSmallLink.setTitle("details");
		detailsSmallLink.setUrl(url);
		detailsSmallLink.setUserObject(row);
		
		row.setDetailsSmallLink(detailsSmallLink);
	}
	
	private void forgeThumbnail(CatalogEntryRow row) {
		if (row.getRepositotyEntryKey() != null) {
			VFSLeaf image = repositoryManager.getImage(row.getRepositotyEntryKey(), row.getOlatResource());
			if (image != null) {
				row.setThumbnailRelPath(RepositoryEntryImageMapper.getImageUrl(repositoryEntryMapperKey.getUrl() , image));
			}
		} else if (row.getCurriculumElementKey() != null) {
			String imageUrl = curriculumElementImageMapper.getImageUrl(curriculumElementImageMapperUrl,
					() -> row.getCurriculumElementKey(), CurriculumElementFileType.teaserImage);
			if (imageUrl != null) {
				row.setThumbnailRelPath(imageUrl);
			}
		}
	}

	private void setWindowTitle() {
		String windowTitle = taxonomyLevel != null
				? translate("window.title.taxonomy", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel))
				: translate("window.title.main");
		getWindow().setTitle(windowTitle);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = null;
		if (rowObject instanceof CatalogEntryRow catalogRow) {
			cmps = new ArrayList<>(2);
			if (catalogRow.getDetailsLink() != null) {
				cmps.add(catalogRow.getDetailsLink().getComponent());
			}
			if (catalogRow.getStartLink() != null) {
				cmps.add(catalogRow.getStartLink().getComponent());
			}
		}
		return cmps;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		if (CatalogBCFactory.isOfferType(entry.getOLATResourceable())) {
			Long key = entry.getOLATResourceable().getResourceableId();
			loadModel(false);
			CatalogEntryRow row = dataModel.getObjectByResourceKey(key);
			if (row != null) {
				int index = dataModel.getObjects().indexOf(row);
				if (index >= 1 && tableEl.getPageSize() > 1) {
					int page = index / tableEl.getPageSize();
					tableEl.setPage(page);
				}
				doOpenDetails(ureq, row, false);
				if (infosCtrl instanceof Activateable2 activateable) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		} else if (CatalogBCFactory.isInfosType(entry.getOLATResourceable())) {
			Long key = entry.getOLATResourceable().getResourceableId();
			loadModel(false);
			CatalogEntryRow row = dataModel.getObjectByRepositoryEntryKey(key);
			if (row != null) {
				int index = dataModel.getObjects().indexOf(row);
				if (index >= 1 && tableEl.getPageSize() > 1) {
					int page = index / tableEl.getPageSize();
					tableEl.setPage(page);
				}
				doOpenDetails(ureq, row, false);
				if (infosCtrl instanceof Activateable2 activateable) {
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					activateable.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}

	public void search(String searchString) {
		headerSearchString = searchString;
		loadModel(false);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == infosCtrl) {
			if (event instanceof BookedEvent bookedEvent) {
				if (listParams.isFireBookedEvent()) {
					fireEvent(ureq, event);
				} else if (bookedEvent.getRepositoryEntry() != null) {
					doBooked(ureq, bookedEvent.getRepositoryEntry());
				} else if (bookedEvent.getCurriculumElement() != null) {
					doBooked(ureq, bookedEvent.getCurriculumElement());
				}
			} else if (event instanceof BookEvent bookEvent) {
				doBook(ureq, bookEvent.getResourceKey());
			} else if (event instanceof LeavingEvent) {
				stackPanel.popUpToController(this);
				loadModel(false);
			}
		} else if (authCtrl == source) {
			lightboxCtrl.deactivate();
			cleanUp();
			if (event instanceof LoginProcessEvent) {
				LoginProcessController loginProcessEventCtrl = new LoginProcessController(ureq, getWindowControl(), stackPanel, null);
				if (event == LoginProcessEvent.REGISTER_EVENT) {
					loginProcessEventCtrl.doOpenRegistration(ureq);
				}
			}
		} else if (lightboxCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(authCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		authCtrl = null;
		lightboxCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter("select");
			if (StringHelper.containsNonWhitespace(key) && StringHelper.isLong(key)) {
				doOpenDetails(ureq, Long.valueOf(key));
			}
			
			key = ureq.getParameter("select_taxonomy");
			if (StringHelper.containsNonWhitespace(key)) {
				fireEvent(ureq, new OpenTaxonomyEvent(
						Long.valueOf(key),
						searchParams.getLauncherEducationalTypeKeys(),
						searchParams.getLauncherResourceTypes()));
				return;
			}
		} else if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == this) {
					setWindowTitle();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent) {
				loadModel(false);
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel(false);
			}
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("start".equals(cmd)){
				CatalogEntryRow row = (CatalogEntryRow)link.getUserObject();
				doStart(ureq, row);
			} else if ("startGuest".equals(cmd)){
				CatalogEntryRow row = (CatalogEntryRow)link.getUserObject();
				doStartGuest(ureq, row);
			} else if ("book".equals(cmd)){
				CatalogEntryRow row = (CatalogEntryRow)link.getUserObject();
				doBook(ureq, row);
			} else if ("details".equals(cmd) || "select".equals(cmd)){
				CatalogEntryRow row = (CatalogEntryRow)link.getUserObject();
				doOpenDetails(ureq, row, false);
			} else if ("select_tax".equals(cmd)){
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new OpenTaxonomyEvent(
						key,
						searchParams.getLauncherEducationalTypeKeys(),
						searchParams.getLauncherResourceTypes()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public synchronized void dispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.dispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doStart(UserRequest ureq, CatalogEntryRow row) {
		if (searchParams.isWebPublish()) {
			doLogin(ureq, row);
		} else {
			try {
				String businessPath = getStartBusinessPath(row);
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				showError("error.corrupted");
			}
		}
	}
	
	private void doStartGuest(UserRequest ureq, CatalogEntryRow row) {
		DispatcherModule.redirectTo(ureq.getHttpResp(), getStartUrl(row, true));
	}
	
	private String getStartUrl(CatalogEntryRow row, boolean guest) {
		String businessPath = getStartBusinessPath(row);
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
		if (guest) {
			url += "?guest=true";
		}
		return url;
	}

	private String getStartBusinessPath(CatalogEntryRow row) {
		String businessPath = "";
		if (row.getRepositotyEntryKey() != null) {
			businessPath = "[RepositoryEntry:" + row.getRepositotyEntryKey() + "]";
		} else if (row.getCurriculumElementKey() != null) {
			businessPath = "[MyCoursesSite:0][Curriculum:0][Curriculum:" + row.getCurriculumKey() + "]";
		}
		return businessPath;
	}
	
	private void doOpenDetails(UserRequest ureq, Long resourceKey) {
		CatalogEntryRow row = dataModel.getObjectByResourceKey(resourceKey);
		if (row != null) {
			doOpenDetails(ureq, row, false);
		}
	}
	
	private void doOpenDetails(UserRequest ureq, CatalogEntryRow row, boolean scrollToOffers) {
		if (row.getRepositotyEntryKey() != null) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(row.getRepositotyEntryKey());
			doOpenDetails(ureq, entry, scrollToOffers);
		} else if (row.getCurriculumElementKey() != null) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(() -> row.getCurriculumElementKey());
			doOpenDetails(ureq, curriculumElement, scrollToOffers);
		}
	}
	
	private void doOpenDetails(UserRequest ureq, RepositoryEntry entry, boolean scrollToOffers) {
		if (entry != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createOfferOres(entry.getOlatResource());
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			infosCtrl = new CatalogRepositoryEntryInfosController(ureq, bwControl, stackPanel, entry, scrollToOffers, searchParams.isWebPublish());
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = entry.getDisplayname();
			stackPanel.pushController(displayName, infosCtrl);
			
			String windowTitle = translate("window.title.infos", displayName);
			getWindow().setTitle(windowTitle);
		} else {
			tableEl.reloadData();
		}
	}
	
	private void doOpenDetails(UserRequest ureq, CurriculumElement curriculumElement, boolean scrollToOffers) {
		if (curriculumElement != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createOfferOres(curriculumElement.getResource());
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			infosCtrl = new CurriculumElementInfosController(ureq, bwControl, curriculumElement, scrollToOffers, searchParams.getMember());
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = curriculumElement.getDisplayName();
			stackPanel.pushController(displayName, infosCtrl);
			
			String windowTitle = translate("window.title.infos", displayName);
			getWindow().setTitle(windowTitle);
		} else {
			tableEl.reloadData();
		}
	}
	
	private void doBook(UserRequest ureq, Long resourceKey) {
		CatalogEntryRow row = dataModel.getObjectByResourceKey(resourceKey);
		if (row != null) {
			doBook(ureq, row);
		}
	}

	private void doBook(UserRequest ureq, CatalogEntryRow row) {
		if (searchParams.isWebPublish()) {
			doLogin(ureq, row);
			return;
		}
		
		if (row.getRepositotyEntryKey() != null) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(row.getRepositotyEntryKey());
			if (entry != null) {
				AccessResult acResult = acService.isAccessible(entry, getIdentity(), row.isMember(), searchParams.isGuestOnly(), null, false);
				if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), entry, acResult)) {
					doStart(ureq, row);
					return;
				}
			}
		} else if (row.getCurriculumElementKey() != null) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(() -> row.getCurriculumElementKey());
			if (curriculumElement != null) {
				AccessResult acResult = acService.isAccessible(curriculumElement, getIdentity(), row.isMember(), searchParams.isGuestOnly(), null, false);
				if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), curriculumElement, acResult)) {
					doStart(ureq, row);
					return;
				}
			}
		}
		
		doOpenDetails(ureq, row, true);
	}

	private void doLogin(UserRequest ureq, CatalogEntryRow row) {
		if (guardModalController(authCtrl)) return;
		String offerBusinessPath = CatalogBCFactory.get(false).getOfferBusinessPath(row.getOlatResource());
		authCtrl = new WebCatalogAuthController(ureq, getWindowControl(), offerBusinessPath);
		listenTo(authCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), authCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	private void doBooked(UserRequest ureq, RepositoryEntry repositoryEntry) {
		stackPanel.popUpToController(this);
		
		if (repositoryEntry != null) {
			doOpenDetails(ureq, repositoryEntry, false);
			if (repositoryManager.isAllowed(ureq, repositoryEntry).canLaunch()) {
				CatalogEntryRow row = dataModel.getObjectByResourceKey(repositoryEntry.getOlatResource().getKey());
				if (row != null) {
					doStart(ureq, row);
				}
			}
		}
	}

	private void doBooked(UserRequest ureq, CurriculumElement element) {
		stackPanel.popUpToController(this);
		
		if (element != null) {
			doOpenDetails(ureq, element, false);
			boolean isMember = !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(getIdentity())).isEmpty();
			if (isMember) {
				CatalogEntryRow row = dataModel.getObjectByResourceKey(element.getResource().getKey());
				if (row != null) {
					doStart(ureq, row);
				}
			}
		}
	}
	
	public static final class TaxonomyItem {
		
		private final Long key;
		private final String displayName;
		private final String selectLinkName;
		private String thumbnailRelPath;
		
		public TaxonomyItem(Long key, String displayName, String selectLinkName) {
			this.key = key;
			this.displayName = displayName;
			this.selectLinkName = selectLinkName;
		}

		public Long getKey() {
			return key;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getSelectLinkName() {
			return selectLinkName;
		}

		public String getThumbnailRelPath() {
			return thumbnailRelPath;
		}

		public void setThumbnailRelPath(String thumbnailRelPath) {
			this.thumbnailRelPath = thumbnailRelPath;
		}
		
	}

}
