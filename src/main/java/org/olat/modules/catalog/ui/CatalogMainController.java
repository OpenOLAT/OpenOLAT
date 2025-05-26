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

import java.util.Collection;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.CatalogSecurityCallbackFactory;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler.Levels;
import org.olat.modules.catalog.ui.admin.CatalogTaxonomyEditController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogMainController extends BasicController implements Activateable2 {
	
	private VelocityContainer mainVC;
	private TooledStackedPanel taxonomyStackPanel;
	private CatalogSearchHeaderController headerSearchCtrl;
	private BreadcrumbedStackedPanel stackPanel;
	private CatalogLaunchersController launchersCtrl;
	private CatalogTaxonomyHeaderController headerTaxonomyCtrl;
	private CatalogEntryListController catalogRepositoryEntryListCtrl;
	private CatalogTaxonomyEditController taxonomyAdminCtrl;

	private final CatalogSecurityCallback secCallback;
	private final CatalogEntrySearchParams defaultSearchParams;
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelLauncherHandler taxonomyLevelLauncherHandler;
	
	public CatalogMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.secCallback = createSecCallback(ureq);
		this.defaultSearchParams = createDefaultSearchParams(ureq);
		
		mainVC = createVelocityContainer("main");
		putInitialPanel(mainVC);
		
		List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(defaultSearchParams);
		
		headerSearchCtrl = new CatalogSearchHeaderController(ureq, wControl, defaultSearchParams.isWebPublish());
		listenTo(headerSearchCtrl);
		headerSearchCtrl.setTotalCatalogEntries(catalogEntries.size());
		mainVC.put("header", headerSearchCtrl.getInitialComponent());
		
		taxonomyStackPanel = new TooledStackedPanel("taxonomystack", getTranslator(), this);
		taxonomyStackPanel.setInvisibleCrumb(0);
		taxonomyStackPanel.setToolbarEnabled(false);
		taxonomyStackPanel.setVisible(false);
		mainVC.put("taxonomy", taxonomyStackPanel);
		
		stackPanel = new BreadcrumbedStackedPanel("catalogstack", getTranslator(), this);
		stackPanel.setCssClass("o_catalog_breadcrumb");
		stackPanel.setInvisibleCrumb(0);
		mainVC.put("stack", stackPanel);
		
		launchersCtrl = new CatalogLaunchersController(ureq, getWindowControl(), defaultSearchParams.copy(), secCallback);
		listenTo(launchersCtrl);
		launchersCtrl.update(ureq, catalogEntries);
		stackPanel.pushController(translate("overview"), launchersCtrl);
		taxonomyStackPanel.pushController(translate("overview"), launchersCtrl);
	}

	protected CatalogSecurityCallback createSecCallback(UserRequest ureq) {
		return CatalogSecurityCallbackFactory.create(ureq.getUserSession().getRoles());
	}
	
	protected CatalogEntrySearchParams createDefaultSearchParams(UserRequest ureq) {
		CatalogEntrySearchParams searchParams = new CatalogEntrySearchParams();
		searchParams.setMember(getIdentity());
		searchParams.setGuestOnly(ureq.getUserSession().getRoles().isGuestOnly());
		searchParams.setOfferOrganisations(acService.getOfferOrganisations(getIdentity()));
		return searchParams;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			stackPanel.popUpToRootController(ureq);
			doOpenSearchHeader();
		} else {
			OLATResourceable ores = entries.get(0).getOLATResourceable();
			if (CatalogBCFactory.isSearchType(ores)) {
				headerSearchCtrl.setSearchString(null);
				headerSearchCtrl.setExploreLinkVisibile(false);
				doSearch(ureq, null, null);
				entries = entries.subList(1, entries.size());
				catalogRepositoryEntryListCtrl.activate(ureq, entries, state);
			} else if (CatalogBCFactory.isTaxonomyLevelType(ores)) {
				doActivateTaxonomy(ureq, ores.getResourceableId());
			} else if (CatalogBCFactory.ORES_TYPE_TAXONOMY_ADMIN.equalsIgnoreCase(ores.getResourceableTypeName())) {
				if (secCallback.canEditTaxonomy()) {
					doOpenTaxonomyAdmin(ureq);
					entries = entries.subList(1, entries.size());
					taxonomyAdminCtrl.activate(ureq, entries, state);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerSearchCtrl) {
			if (event instanceof CatalogSearchEvent cse) {
				headerSearchCtrl.setExploreLinkVisibile(false);
				doSearch(ureq, cse.getSearchString(), null);
			}
		} else if (source == launchersCtrl) {
			if (event == CatalogLaunchersController.OPEN_ADMIN_EVENT) {
				doOpenAdmin(ureq);
			} else if (event == CatalogLaunchersController.TAXONOMY_ADMIN_EVENT) {
				doOpenTaxonomyAdmin(ureq);
			} else if (event instanceof OpenSearchEvent ose) {
				headerSearchCtrl.setSearchString(null);
				String header = ose.getState() != null? ose.getState().getSpecialFilterLabel(): null;
				headerSearchCtrl.setHeaderOnly(header);
				doSearch(ureq, null, ose.getState());
				List<ContextEntry> entries = null;
				if (ose.getInfoResourceKey() != null) {
					entries = BusinessControlFactory.getInstance().createCEListFromString(CatalogBCFactory.createOfferOres(ose.getInfoResourceKey()));
				}
				catalogRepositoryEntryListCtrl.activate(ureq, entries, null);
			} else if (event instanceof OpenTaxonomyEvent) {
				OpenTaxonomyEvent ote = (OpenTaxonomyEvent)event;
				doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey(), ote.getEducationalTypeKeys(), ote.getResourceTypes());
			}
		} else if (source instanceof CatalogEntryListController) {
			if (event instanceof OpenTaxonomyEvent ote ) {
				doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey(), ote.getEducationalTypeKeys(), ote.getResourceTypes());
			}
		} else if (source == taxonomyAdminCtrl) {
			if (event == CatalogTaxonomyEditController.OPEN_ADMIN_EVENT) {
				doOpenAdmin(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == taxonomyStackPanel) {
			if (taxonomyStackPanel.getLastController() == taxonomyStackPanel.getRootController()) {
				doCloseTaxonomyAdmin();
			}
		} else if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == stackPanel.getRootController()) {
					// Clicked on root breadcrumb
					doOpenSearchHeader();
				} else if (stackPanel.getLastController() instanceof CatalogEntryListController) {
					// Clicked on taxonomy level in breadcrumb
					TaxonomyLevel taxonomyLevel = ((CatalogEntryListController)stackPanel.getLastController()).getTaxonomyLevel();
					if (taxonomyLevel != null) {
						doOpenTaxonomyHeader(ureq, taxonomyLevel);
					}
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}

	private void doOpenSearchHeader() {
		removeAsListenerAndDispose(headerTaxonomyCtrl);
		headerTaxonomyCtrl = null;
		headerSearchCtrl.setExploreLinkVisibile(true);
		headerSearchCtrl.setHeaderOnly(null);
		mainVC.put("header", headerSearchCtrl.getInitialComponent());
		
		String windowTitle = translate("window.title.main");
		getWindow().setTitle(windowTitle);
	}

	private void doSearch(UserRequest ureq, String searchString, CatalogEntryState catalogEntryState) {
		if (stackPanel.getLastController() != catalogRepositoryEntryListCtrl) {
			if (stackPanel.hasController(catalogRepositoryEntryListCtrl)) {
				// User is on Infos or Offers
				stackPanel.popUpToController(catalogRepositoryEntryListCtrl);
				addToHistory(ureq, catalogRepositoryEntryListCtrl.getWindowControlForDebug());
			} else {
				// User is on Launchers
				removeAsListenerAndDispose(catalogRepositoryEntryListCtrl);
				stackPanel.popUpToRootController(ureq);
				
				WindowControl swControl = addToHistory(ureq, CatalogBCFactory.createSearchOres(), null);
				CatalogEntrySearchParams searchParams = defaultSearchParams.copy();
				if (catalogEntryState != null) {
					searchParams.setResourceKeys(catalogEntryState.getSpecialFilterResourceKeys());
				}
				boolean withSearch = catalogEntryState != null;
				CatalogEntryListParams listParams = new CatalogEntryListParams();
				listParams.setWithSearch(withSearch);
				catalogRepositoryEntryListCtrl = new CatalogEntryListController(ureq, swControl, stackPanel, searchParams, listParams);
				listenTo(catalogRepositoryEntryListCtrl);
				String crumbName = catalogEntryState != null
						? catalogEntryState.getSpecialFilterLabel()
						: translate("search.results");
				stackPanel.pushController(crumbName, catalogRepositoryEntryListCtrl);
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
			}
		}
		catalogRepositoryEntryListCtrl.search(searchString);
	}

	private void doOpenTaxonomy(UserRequest ureq, Long taxonomyLevelKey, Collection<Long> eductaionalTypeKeys, Collection<String> resourceTypes) {
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> taxonomyLevelKey);
		doOpenTaxonomyLevel(ureq, taxonomyLevel, eductaionalTypeKeys, resourceTypes);
	}

	private void doOpenTaxonomyLevel(UserRequest ureq, TaxonomyLevel taxonomyLevel, Collection<Long> eductaionalTypeKeys, Collection<String> resourceTypes) {
		if (taxonomyLevel != null) {
			popUpToTaxonomyCtrl();
			doOpenTaxonomyHeader(ureq, taxonomyLevel);
			doOpenTaxonomyList(ureq, taxonomyLevel, eductaionalTypeKeys, resourceTypes);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createScrollTop());
		}
	}
	
	/**
	 * Open all taxonomy level microsites until the requested level.
	 * It takes the first launcher which contains the requested taxonomy level.
	 */
	private void doActivateTaxonomy(UserRequest ureq, Long key) {
		List<CatalogEntry> catalogEntries = catalogService.getCatalogEntries(defaultSearchParams);
		for (CatalogLauncher catalogLauncher : launchersCtrl.getTaxonomyLevelCatalogLaunchers()) {
			Levels levels = taxonomyLevelLauncherHandler.getTaxonomyLevels(catalogLauncher, key, catalogEntries);
			if(levels != null) {
				List<TaxonomyLevel> taxonomyLevels = levels.getTaxonomyLevels();
				if (taxonomyLevels != null) {
					stackPanel.popUpToRootController(ureq);
					taxonomyLevels.forEach(level -> doOpenTaxonomyLevel(ureq, level, levels.getEducationalTypeKeys(), levels.getResourceTypes()));
					break;
				}
			}
		}
	}
	
	private void popUpToTaxonomyCtrl() {
		if (!(stackPanel.getLastController() instanceof CatalogEntryListController) && stackPanel.getLastController() != stackPanel.getRootController()) {
			stackPanel.popController(stackPanel.getLastController());
			popUpToTaxonomyCtrl();
		}
	}

	private void doOpenTaxonomyHeader(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		removeAsListenerAndDispose(headerTaxonomyCtrl);
		
		headerTaxonomyCtrl = new CatalogTaxonomyHeaderController(ureq, getWindowControl(), taxonomyLevel);
		listenTo(headerTaxonomyCtrl);
		mainVC.put("header", headerTaxonomyCtrl.getInitialComponent());
	}
	
	private void doOpenTaxonomyList(UserRequest ureq, TaxonomyLevel taxonomyLevel, Collection<Long> educationalTypeKeys, Collection<String> resourceTypes) {
		removeAsListenerAndDispose(catalogRepositoryEntryListCtrl);
		catalogRepositoryEntryListCtrl = null;

		WindowControl swControl = addToHistory(ureq,CatalogBCFactory.createTaxonomyLevelOres(taxonomyLevel), null);
		CatalogEntrySearchParams searchParams = defaultSearchParams.copy();
		searchParams.setLauncherTaxonomyLevels(List.of(taxonomyLevel));
		searchParams.setLauncherEducationalTypeKeys(educationalTypeKeys);
		searchParams.setLauncherResourceTypes(resourceTypes);
		CatalogEntryListParams listParams = new CatalogEntryListParams();
		listParams.setWithSearch(true);
		CatalogEntryListController taxonomyListCtrl = new CatalogEntryListController(ureq, swControl, stackPanel, searchParams, listParams);
		listenTo(taxonomyListCtrl);
		stackPanel.pushController(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel), taxonomyListCtrl);
	}

	private void doOpenTaxonomyAdmin(UserRequest ureq) {
		taxonomyStackPanel.popUpToRootController(ureq);
		
		removeAsListenerAndDispose(taxonomyAdminCtrl);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CatalogBCFactory.ORES_TYPE_TAXONOMY_ADMIN),
				null);
		taxonomyAdminCtrl = new CatalogTaxonomyEditController(ureq, swControl, taxonomyStackPanel, secCallback);
		listenTo(taxonomyAdminCtrl);
		taxonomyStackPanel.pushController(translate("taxonomy.management"), taxonomyAdminCtrl);
		taxonomyStackPanel.setVisible(true);
		mainVC.setDirty(true);
	}

	private void doCloseTaxonomyAdmin() {
		taxonomyStackPanel.setVisible(false);
		mainVC.setDirty(true);
	}

	private void doOpenAdmin(UserRequest ureq) {
		try {
			String businessPath = "[AdminSite:0][catalog:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (Exception e) {
			//
		}
	}

}
