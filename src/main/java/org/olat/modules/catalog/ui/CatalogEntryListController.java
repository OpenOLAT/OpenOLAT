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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.login.LoginModule;
import org.olat.login.LoginProcessEvent;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.filter.LifecyclePublicHandler;
import org.olat.modules.catalog.ui.CatalogEntryDataModel.CatalogEntryCols;
import org.olat.modules.creditpoint.CreditPointModule;
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
import org.olat.registration.PwChangeController;
import org.olat.registration.RegWizardConstants;
import org.olat.registration.RegistrationAdditionalPersonalDataController;
import org.olat.registration.RegistrationLangStep00;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.registration.RegistrationPersonalDataController;
import org.olat.registration.SelfRegistrationAdvanceOrderInput;
import org.olat.registration.TemporaryKey;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.EducationalTypeRenderer;
import org.olat.repository.ui.list.LeavingEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.auto.AutoAccessManager;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CatalogEntryListController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	static final String CMD_DETAILS = "details";
	// The SelectonEvent if fired for every for with the same action?!
	// So every row needs an own action.
	static final String CMD_TITLE = "title";

	private static final Logger log = Tracing.createLoggerFor(CatalogEntryListController.class);

	private final BreadcrumbedStackedPanel stackPanel;
	private FlexiTableElement tableEl;
	private CatalogEntryDataModel dataModel;
	private final CatalogEntrySearchParams searchParams;
	private final CatalogEntryListParams listParams;
	
	private Controller infosCtrl;
	private LightboxController lightboxCtrl;
	private WebCatalogAuthController authCtrl;
	private StepsMainRunController registrationWizardCtrl;
	private PwChangeController pwChangeCtrl;
	
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
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserModule userModule;
	@Autowired
	private RegistrationManager registrationManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AutoAccessManager autoAccessManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private UserPropertiesConfig userPropertiesConfig;
	@Autowired
	private CreditPointModule creditPointModule;
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private LifecycleModule lifecycleModule;

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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.title, CMD_TITLE));
		if (repositoryModule.isManagedRepositoryEntries()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.externalId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.externalRef));
		if (lifecycleModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, CatalogEntryCols.lifecycleSoftkey));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.lifecycleLabel));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleStart, new DateFlexiCellRenderer(getLocale())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.lifecycleEnd, new DateFlexiCellRenderer(getLocale())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.mainLanguage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.expenditureOfWork));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.educationalType, new EducationalTypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CatalogEntryCols.taxonomyLevels, new TaxonomyLevelRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.offers));
		if(creditPointModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.creditPoints));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.certificate,
				new CertificateEnabledRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.availability, new ParticipantsAvailabilityRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CatalogEntryCols.detailsSmall,
				new StaticFlexiCellRenderer(translate("learn.more"), CMD_DETAILS, "btn btn-xs btn-default o_details tablecell o_button_ghost", null, null)));
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
		
		tableEl.setAndLoadPersistedPreferences(ureq, "catalog-v2-relist-3.1");
	}

	private boolean shouldExcludeCatalogEntry(CatalogEntry catalogEntry) {
		if (listParams.isExcludeRepositoryEntries()) {
			if (catalogEntry.getRepositoryEntryKey() != null) {
				return true;
			}
		}
		
		if (listParams.isExcludeMembers() && (catalogEntry.isMember() || catalogEntry.isReservationAvailable())) {
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

			if (handler instanceof LifecyclePublicHandler && !lifecycleModule.isEnabled()) {
				continue;
			}
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
		tableEl.reset(true, true, true);
	}
	
	private void applySearch(List<CatalogEntryRow> rows) {
		String searchValue = listParams.isWithSearch() ? tableEl.getQuickSearchString(): headerSearchString;
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> !containsAllValues(row, searchValues));
		}
	}
	
	private boolean containsAllValues(CatalogEntryRow row, List<String> searchValues) {
		for (String searchValue : searchValues) {
			if (!containsValue(row, searchValue)) {
				return false;
			}
		}
		
		return true;
	}

	private boolean containsValue(CatalogEntryRow row, String searchValue) {
		return containsValue(row.getTitle(), searchValue)
				|| containsValue(row.getExternalRef(), searchValue)
				|| containsValue(row.getAuthors(), searchValue)
				|| containsValue(row.getTaxonomyLevelNamePaths(), searchValue);
	}

	private boolean containsValue(String candidate, String searchValue) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			boolean b = candidate.toLowerCase().indexOf(searchValue) >= 0;
			return b;
		}
		return false;
	}

	private boolean containsValue(List<TaxonomyLevelNamePath> taxonomyLevelNamePaths, String searchValue) {
		if (taxonomyLevelNamePaths == null || taxonomyLevelNamePaths.isEmpty()) {
			return false;
		}
		for (TaxonomyLevelNamePath taxonomyLevelNamePath : taxonomyLevelNamePaths) {
			if (containsValue(taxonomyLevelNamePath.getDisplayName(), searchValue)) {
				return true;
			}
		}
		
		return false;
	}
	
	private CatalogEntryRow toRow(CatalogEntry catalogEntry) {
		CatalogEntryRow row = new CatalogEntryRow(catalogEntry);
		
		List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(getTranslator(), row.getTaxonomyLevels());
		row.setTaxonomyLevelNamePaths(taxonomyLevels);
		
		if (catalogEntry.isPublicVisible()) {
			
			boolean autoBooking = true;
			Set<String> accessMethodTypes = new HashSet<>(2);
			List<PriceMethod> priceMethods = new ArrayList<>(2);
			for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
				for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
					accessMethodTypes.add(bundle.getMethod().getType());
					priceMethods.add(toPriceMethod(bundle));
					if (!FreeAccessHandler.METHOD_TYPE.equals(bundle.getMethod().getType()) || !bundle.isAutoBooking()) {
						autoBooking = false;
					}
				}
			}
			// Prevents auto booking with (only) two auto booking offers
			if (priceMethods.size() > 1) {
				autoBooking = false;
			}
			
			if (catalogEntry.isOpenAccess()) {
				priceMethods.add(new PriceMethod(null, "o_ac_openaccess_icon", translate("open.access.name")));
				autoBooking = false;
			}
			
			if (!accessMethodTypes.isEmpty()) {
				row.setAccessMethodTypes(accessMethodTypes);
				row.setAutoBooking(autoBooking);
			}
			
			updateAccessInfo(row, catalogEntry);
		}
		
		if(StringHelper.containsNonWhitespace(catalogEntry.getTechnicalType())) {
			NodeAccessType type = NodeAccessType.of(catalogEntry.getTechnicalType());
			String translatedType = ConditionNodeAccessProvider.TYPE.equals(type.getType())
					? translate("CourseModule")
					: nodeAccessService.getNodeAccessTypeName(type, getLocale());
			row.setTranslatedTechnicalType(translatedType);
		}
		
		row.setInfoUrl(CatalogBCFactory.get(searchParams.isWebPublish()).getOfferUrl(row.getOlatResource()));
		row.setStartUrl(getStartUrl(row, searchParams.isWebPublish() && row.isGuestAccess()));
		
		return row;
	}

	private void updateAccessInfo(CatalogEntryRow row, CatalogEntry catalogEntry) {
		if (searchParams.isGuestOnly() || row.isMember() || row.isReservationAvailable() || catalogEntry.isOpenAccess()) {
			return;
		}
		
		if (row.getAccessMethodTypes() != null && row.getAccessMethodTypes().size() > 1) {
			row.setAccessInfo(translate("access.info.several.types"));
			row.setAccessInfoIconCssClass("o_icon_tags");
			return;
		}
		
		for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(FreeAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(translate("access.info.freely.available"));
					row.setAccessInfoIconCssClass("o_ac_free_icon");
					return;
				}
			}
		}
		
		for (OLATResourceAccess resourceAccess : catalogEntry.getResourceAccess()) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				if (bundle.getMethod().getType().equals(TokenAccessHandler.METHOD_TYPE)) {
					row.setAccessInfo(translate("access.info.token"));
					row.setAccessInfoIconCssClass("o_ac_token_icon");
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
			row.setAccessInfoIconCssClass("o_ac_invoice_icon");
		}
	}

	private void updateAccessMaxParticipants(CatalogEntryRow row) {
		if (searchParams.isGuestOnly() || row.isMember() || row.isReservationAvailable() || row.isOpenAccess() || row.getMaxParticipants() == null) {
			return;
		}
		
		ParticipantsAvailabilityNum participantsAvailabilityNum = acService.getParticipantsAvailability(row.getMaxParticipants(), row.getNumParticipants(), false);
		row.setParticipantsAvailabilityNum(participantsAvailabilityNum);
		
		if (participantsAvailabilityNum.availability() == ParticipantsAvailability.fullyBooked) {
			row.setAccessError(getAvailabilityText(participantsAvailabilityNum));
		} else if (participantsAvailabilityNum.availability() == ParticipantsAvailability.fewLeft) {
			row.setAccessWarning(getAvailabilityText(participantsAvailabilityNum));
		}
	}
	
	private String getAvailabilityText(ParticipantsAvailabilityNum participantsAvailabilityNum) {
		return "<i class=\"o_icon " + ParticipantsAvailability.getIconCss(participantsAvailabilityNum) + "\"> </i> " 
				+ ParticipantsAvailability.getText(getTranslator(), participantsAvailabilityNum);
	}

	private PriceMethod toPriceMethod(PriceMethodBundle bundle) {
		String type = bundle.getMethod().getMethodCssClass() + "_icon";
		String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
		AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
		String displayName = amh.getMethodName(getLocale());
		return new PriceMethod(price, type, displayName);
	}
	
	private void forgeLinks(CatalogEntryRow row) {
		updateAccessMaxParticipants(row);
		forgeStartLink(row);
		forgeThumbnail(row);
		forgeRatings(row);
	}
	
	private void forgeRatings(CatalogEntryRow row) {
		if(!repositoryModule.isRatingEnabled() || row.getRepositotyEntryKey() == null) return;
		
		Double averageRating = row.getAverageRating();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
		RatingFormItem ratingEl = uifactory.addRatingItem("rat_" + row.getRepositotyEntryKey(), null,  averageRatingValue, 5, false, flc);
		ratingEl.addActionListener(FormEvent.ONCLICK);
		ratingEl.setLargeIcon(false);
		row.setRatingFormItem(ratingEl);
		ratingEl.setUserObject(row);
		ratingEl.setTranslator(getTranslator());
	}

	private void forgeStartLink(CatalogEntryRow row) {
		String url = row.getStartUrl();
		String cmd = "start";
		String label = "open";
		if (searchParams.isWebPublish() && row.isGuestAccess()) {
			ExternalLinkItem link = uifactory.addExternalLink("start_" + row.getOlatResource().getKey(), url, "_self", null);
			link.setCssClass("btn btn-sm btn-primary o_catalog_start");
			link.setIconRightCSS("o_icon o_icon_start");
			link.setName(translate("start.guest"));
			row.setStartLink(link);
			return;
		}
		
		if (!searchParams.isGuestOnly() && !row.isMember() && !row.isReservationAvailable() && row.isPublicVisible() && !row.isOpenAccess()) {
			cmd = "book";
			if (!row.isAutoBooking()) {
				url = row.getInfoUrl();
				label = "book";
			}
		}
		
		FormLink link = uifactory.addFormLink("start_" + row.getOlatResource().getKey(), cmd, label, null, null, Link.BUTTON_SMALL);
		link.setUserObject(row);
		link.setPrimary(true);
		link.setElementCssClass("o_catalog_start");
		link.setIconRightCSS("o_icon o_icon_start");
		link.setUrl(url);
		row.setStartLink(link);
		
		if (StringHelper.containsNonWhitespace(
				row.getAccessError()) ||
				row.isReservationAvailable() ||
				(row.isMember() && row.isUnpublishedImplementation()) ||
				(row.isAutoBooking() && row.isUnpublishedSingleCourseImplementation())
				) {
			link.setEnabled(false);
		}
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
		if (rowObject instanceof CatalogEntryRow catalogRow) {
			List<Component> cmps = new ArrayList<>(3);
			if (catalogRow.getStartLink() != null) {
				cmps.add(catalogRow.getStartLink().getComponent());
			}
			if (catalogRow.getRatingFormItem() != null) {
				if(catalogRow.getRatingFormItem() != null && catalogRow.getRatingFormItem().getRootForm() != mainForm) {
					catalogRow.getRatingFormItem().setRootForm(mainForm);
				}
				cmps.add(catalogRow.getRatingFormItem().getComponent());
			}
			return cmps;
		}
		return null;
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
				doOpenDetails(ureq, row);
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
				doOpenDetails(ureq, row);
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
			} else if (event instanceof LeavingEvent leavingEvent) {
				doLeaved(ureq, leavingEvent.getRepositoryEntry(), leavingEvent.getCurriculumElement());
			}
		} else if (authCtrl == source) {
			lightboxCtrl.deactivate();
			cleanUp();
			if (event instanceof LoginProcessEvent) {
				if (event == LoginProcessEvent.REGISTER_EVENT) {
					doOpenRegistration(ureq);
				} else if (event == LoginProcessEvent.PWCHANGE_EVENT) {
					doOpenChangePassword(ureq);
				}
			}
		} else if (lightboxCtrl == source) {
			cleanUp();
		} else if (registrationWizardCtrl == source) {
			stackPanel.popController(registrationWizardCtrl);
			cleanUp();
			if (event == StepsEvent.RELOAD) {
				doOpenRegistration(ureq);
			}
		} else if (source == pwChangeCtrl) {
			if (event == Event.CANCELLED_EVENT
					&& loginModule.getAuthenticationProvider(ShibbolethDispatcher.PROVIDER_SHIB) != null) {
				// Redirect to context path to prevent Javascript error when using Shibboleth provider OO-7777
				ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(Settings.getServerContextPathURI()));
			}
			stackPanel.popController(pwChangeCtrl);
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(authCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		removeAsListenerAndDispose(pwChangeCtrl);
		removeAsListenerAndDispose(registrationWizardCtrl);
		authCtrl = null;
		lightboxCtrl = null;
		pwChangeCtrl = null;
		registrationWizardCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String key = ureq.getParameter(CMD_DETAILS);
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
		if(source instanceof RatingFormItem ratingItem
				&& ratingItem.getUserObject() instanceof CatalogEntryRow row) {
			doOpenDetails(ureq, row);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				CatalogEntryRow row = dataModel.getObject(se.getIndex());
				if (CMD_DETAILS.equals(cmd) || CMD_TITLE.equals(cmd)) {
					doOpenDetails(ureq, row);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
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
			} else if ("select_tax".equals(cmd)){
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new OpenTaxonomyEvent(
						key,
						searchParams.getLauncherEducationalTypeKeys(),
						searchParams.getLauncherResourceTypes()));
			} else if ("comments".equals(cmd)){
				CatalogEntryRow row = (CatalogEntryRow)link.getUserObject();
				doOpenDetails(ureq, row);
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
				if (StringHelper.containsNonWhitespace(businessPath)) {
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				}
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
		if (!StringHelper.containsNonWhitespace(businessPath)) {
			return null;
		}
		
		String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
		if (guest) {
			url += "?guest=true";
		}
		return url;
	}

	private String getStartBusinessPath(CatalogEntryRow row) {
		String businessPath = null;
		if (row.getRepositotyEntryKey() != null) {
			businessPath = "[RepositoryEntry:" + row.getRepositotyEntryKey() + "]";
		} else if (row.getCurriculumElementKey() != null) {
			if (!row.isUnpublishedImplementation()) {
				if (row.isSingleCourseImplementation()) {
					businessPath = "[RepositoryEntry:" + row.getSingleCourseEntryKey() + "]";
				} else {
					businessPath = "[MyCoursesSite:0][Implementation:" + row.getCurriculumElementKey() + "]";
				}
			}
		}
		return businessPath;
	}
	
	private void doOpenDetails(UserRequest ureq, Long resourceKey) {
		CatalogEntryRow row = dataModel.getObjectByResourceKey(resourceKey);
		if (row != null) {
			doOpenDetails(ureq, row);
		}
	}
	
	private void doOpenDetails(UserRequest ureq, CatalogEntryRow row) {
		if (row.getRepositotyEntryKey() != null) {
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(row.getRepositotyEntryKey());
			doOpenDetails(ureq, entry);
		} else if (row.getCurriculumElementKey() != null) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row::getCurriculumElementKey);
			doOpenDetails(ureq, curriculumElement);
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
			
			String windowTitle = translate("window.title.infos", displayName);
			getWindow().setTitle(windowTitle);
		} else {
			tableEl.reloadData();
		}
	}
	
	private void doOpenDetails(UserRequest ureq, CurriculumElement curriculumElement) {
		if (curriculumElement != null) {
			removeAsListenerAndDispose(infosCtrl);
			OLATResourceable ores = CatalogBCFactory.createOfferOres(curriculumElement.getResource());
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			
			RepositoryEntry entry = getSingleCourse(curriculumElement);
			infosCtrl = new CurriculumElementInfosController(ureq, bwControl, curriculumElement, entry, searchParams.getMember(), false);
			listenTo(infosCtrl);
			addToHistory(ureq, infosCtrl);
			
			String displayName = curriculumElement.getDisplayName();
			stackPanel.pushController(displayName, infosCtrl);
			
			String windowTitle = translate("window.title.infos", displayName);
			getWindow().setTitle(windowTitle);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
		} else {
			tableEl.reloadData();
		}
	}
	
	private RepositoryEntry getSingleCourse(CurriculumElement curriculumElement) {
		if(curriculumElement.isSingleCourseImplementation()) {	
			List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
			if(entries.size() == 1) {
				return entries.get(0);
			}
		}
		return null;
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
		
		doOpenDetails(ureq, row);
	}

	public void doOpenRegistration(UserRequest ureq) {
		boolean isAdditionalRegistrationFormEnabled = !userManager
				.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
		Step startReg = new RegistrationLangStep00(ureq, null, registrationModule.isDisclaimerEnabled(),
				registrationModule.isEmailValidationEnabled(), isAdditionalRegistrationFormEnabled, registrationModule.isAllowRecurringUserEnabled());
		// Skip the language step if there is only one language enabled - default
		// language will be used. Use the calculated next step as start step instead.
		if (i18nModule.getEnabledLanguageKeys().size() == 1) {	
			startReg = startReg.nextStep();			
		}
		registrationWizardCtrl = new StepsMainRunController(ureq, getWindowControl(), startReg, new RegisterFinishCallback(),
				new RegCancelCallback(), translate("menu.register"), "o_sel_registration_start_wizard");
		listenTo(registrationWizardCtrl);
		stackPanel.pushController(translate("menu.register"), registrationWizardCtrl);
	}

	private void doOpenChangePassword(UserRequest ureq) {
		getWindowControl().getWindowBackOffice().getWindowManager().setAjaxEnabled(true);

		if (userModule.isAnyPasswordChangeAllowed()) {
			pwChangeCtrl = new PwChangeController(ureq, getWindowControl(), null, false);
			listenTo(pwChangeCtrl);
			stackPanel.pushController(translate("pwchange.wizard.title"), pwChangeCtrl);
		} else {
			showWarning("warning.not.allowed.to.change.pwd", new String[]  {WebappHelper.getMailConfig("mailSupport") });
		}
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
			doOpenDetails(ureq, repositoryEntry);
		}
	}

	private void doBooked(UserRequest ureq, CurriculumElement element) {
		stackPanel.popUpToController(this);
		
		if (element != null) {
			doOpenDetails(ureq, element);
		}
	}
	
	private void doLeaved(UserRequest ureq, RepositoryEntry repositoryEntry, CurriculumElement curriculumElement) {
		stackPanel.popUpToController(this);
		loadModel(false);
		if (repositoryEntry != null) {
			doOpenDetails(ureq, repositoryEntry);
		} else if (curriculumElement != null) {
			doOpenDetails(ureq, curriculumElement);
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

	private class RegisterFinishCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			if (runContext.get(RegWizardConstants.RECURRINGDETAILS) == null) {

				Identity identity = createNewUser(runContext);
				if (identity == null) {
					showError("user.notregistered");
					return null;
				}
				updateUserData(identity, runContext);
				doLogin(ureq, identity, BaseSecurityModule.getDefaultAuthProviderIdentifier());
			}

			return StepsMainRunController.DONE_MODIFIED;
		}

		public void doLogin(UserRequest ureq, Identity persistedIdentity, String authProvider) {
			int loginStatus = AuthHelper.doLogin(persistedIdentity, authProvider, ureq);
			if (loginStatus == AuthHelper.LOGIN_OK) {
				// it's ok
			} else if (loginStatus == AuthHelper.LOGIN_NOTAVAILABLE) {
				DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
			} else if (loginStatus == AuthHelper.LOGIN_INACTIVE) {
				getWindowControl().setError(translate("login.error.inactive", WebappHelper.getMailConfig("mailSupport")));
			} else {
				getWindowControl().setError(translate("login.error", WebappHelper.getMailConfig("mailReplyTo")));
			}
		}

		private Identity createNewUser(StepsRunContext runContext) {
			String firstName = (String) runContext.get(RegWizardConstants.FIRSTNAME);
			String lastName = (String) runContext.get(RegWizardConstants.LASTNAME);
			String email = (String) runContext.get(RegWizardConstants.EMAIL);
			String username = (String) runContext.get(RegWizardConstants.USERNAME);
			String password = (String) runContext.get(RegWizardConstants.PASSWORD);

			// create user with mandatory fields from registration-form
			User volatileUser = userManager.createUser(firstName, lastName, email);

			// create an identity with the given username / pwd and the user object
			@SuppressWarnings("unchecked")
			List<Authentication> passkeys = (List<Authentication>) runContext.get(RegWizardConstants.PASSKEYS);

			// if organisation module and emailDomain is enabled, then set the selected orgaKey
			// otherwise selectedOrgaKey is null
			String selectedOrgaKey = (String) runContext.get(RegWizardConstants.SELECTEDORGANIZATIONKEY);

			TemporaryKey temporaryKey = (TemporaryKey) runContext.get(RegWizardConstants.TEMPORARYKEY);
			Identity identity = registrationManager.createNewUserAndIdentityFromTemporaryKey(username, password, volatileUser, temporaryKey, selectedOrgaKey);

			if (identity != null && passkeys != null && !passkeys.isEmpty()) {
				securityManager.persistAuthentications(identity, passkeys);
			}
			return identity;
		}

		@SuppressWarnings("unchecked")
		private void updateUserData(Identity identity, StepsRunContext runContext) {
			User user = identity.getUser();

			// Set user configured language
			Preferences preferences = user.getPreferences();
			preferences.setLanguage((String) runContext.get(RegWizardConstants.CHOSEN_LANG));
			user.setPreferences(preferences);

			autoEnrollUser(identity);

			// Add static properties if enabled and not invited
			if (registrationModule.isStaticPropertyMappingEnabled()) {
				addStaticProperty(user);
			}

			// Add user property values from registration forms
			populateUserPropertiesFromForm(user, RegistrationPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.PROPFORMITEMS));

			boolean isAdditionalRegistrationFormEnabled = !userManager
					.getUserPropertyHandlersFor(RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, false).isEmpty();
			if (isAdditionalRegistrationFormEnabled) {
				populateUserPropertiesFromForm(user, RegistrationAdditionalPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER, (Map<String, FormItem>) runContext.get(RegWizardConstants.ADDITIONALPROPFORMITEMS));
			}

			// Persist changes and send notifications
			userManager.updateUserFromIdentity(identity);
			notifyAdminOnNewUser(identity);

			// Register user's disclaimer acceptance
			registrationManager.setHasConfirmedDislaimer(identity);
		}

		private void autoEnrollUser(Identity identity) {
			SelfRegistrationAdvanceOrderInput input = new SelfRegistrationAdvanceOrderInput();
			input.setIdentity(identity);
			input.setRawValues(registrationModule.getAutoEnrolmentRawValue());
			autoAccessManager.createAdvanceOrders(input);
			autoAccessManager.grantAccessToCourse(identity);
		}

		private void addStaticProperty(User user) {
			String propertyName = registrationModule.getStaticPropertyMappingName();
			String propertyValue = registrationModule.getStaticPropertyMappingValue();

			if (StringHelper.containsNonWhitespace(propertyName) && StringHelper.containsNonWhitespace(propertyValue)
					&& userPropertiesConfig.getPropertyHandler(propertyName) != null) {
				try {
					user.setProperty(propertyName, propertyValue);
				} catch (Exception e) {
					log.error("Cannot set the static property value", e);
				}
			}
		}

		private void populateUserPropertiesFromForm(User user, String formIdentifier, Map<String, FormItem> propFormItems) {
			List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifier, false);
			for (UserPropertyHandler handler : userPropertyHandlers) {
				FormItem formItem = propFormItems.get(handler.getName());
				handler.updateUserFromFormItem(user, formItem);
			}
		}

		private void notifyAdminOnNewUser(Identity identity) {
			String notificationEmail = registrationModule.getRegistrationNotificationEmail();
			if (notificationEmail != null) {
				registrationManager.sendNewUserNotificationMessage(notificationEmail, identity);
			}
		}
	}

	private static class RegCancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			TemporaryKey temporaryKey = (TemporaryKey) runContext.get(RegWizardConstants.TEMPORARYKEY);
			// remove temporaryKey entry, if process gets canceled
			if (temporaryKey != null) {
				CoreSpringFactory.getImpl(RegistrationManager.class).deleteTemporaryKey(temporaryKey);
			}
			return Step.NOSTEP;
		}
	}

}
