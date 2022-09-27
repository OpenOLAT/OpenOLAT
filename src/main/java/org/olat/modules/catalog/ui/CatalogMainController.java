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

import static org.olat.modules.catalog.CatalogRepositoryEntrySearchParams.KEY_LAUNCHER;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.CatalogSecurityCallbackFactory;
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
	
	public static final String ORES_TYPE_SEARCH = "Search";
	public static final String ORES_TYPE_TAXONOMY = "Microsite";
	public static final String ORES_TYPE_TAXONOMY_ADMIN = "TaxonomyAdmin";
	public static final String ORES_TYPE_INFOS = "Infos";
	
	private final VelocityContainer mainVC;
	private final CatalogSearchHeaderController headerSearchCtrl;
	private final BreadcrumbedStackedPanel stackPanel;
	private final CatalogLaunchersController launchersCtrl;
	private CatalogTaxonomyHeaderController headerTaxonomyCtrl;
	private CatalogRepositoryEntryListController catalogRepositoryEntryListCtrl;
	private CatalogTaxonomyEditController taxonomyAdminCtrl;

	private final CatalogSecurityCallback secCallback;
	private final CatalogRepositoryEntrySearchParams defaultSearchParams;
	
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelLauncherHandler taxonomyLevelLauncherHandler;
	
	public CatalogMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.secCallback = CatalogSecurityCallbackFactory.create(ureq.getUserSession().getRoles());
		this.defaultSearchParams = createDefaultSearchParams(ureq);
		
		mainVC = createVelocityContainer("main");
		
		headerSearchCtrl = new CatalogSearchHeaderController(ureq, wControl, secCallback);
		listenTo(headerSearchCtrl);
		mainVC.put("header", headerSearchCtrl.getInitialComponent());
		
		stackPanel = new BreadcrumbedStackedPanel("catalogstack", getTranslator(), this);
		stackPanel.setInvisibleCrumb(0);
		mainVC.put("stack", stackPanel);
		
		launchersCtrl = new CatalogLaunchersController(ureq, getWindowControl(), defaultSearchParams.copy());
		listenTo(launchersCtrl);
		stackPanel.pushController(translate("overview"), launchersCtrl);
		
		putInitialPanel(mainVC);
	}
	
	private CatalogRepositoryEntrySearchParams createDefaultSearchParams(UserRequest ureq) {
		CatalogRepositoryEntrySearchParams searchParams = new CatalogRepositoryEntrySearchParams();
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
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if (ORES_TYPE_SEARCH.equalsIgnoreCase(type)) {
				headerSearchCtrl.setSearchString(null);
				doSearch(ureq, null, true);
				entries = entries.subList(1, entries.size());
				catalogRepositoryEntryListCtrl.activate(ureq, entries, state);
			} else if (ORES_TYPE_TAXONOMY.equalsIgnoreCase(type)) {
				Long key = entries.get(0).getOLATResourceable().getResourceableId();
				doActivateTaxonomy(ureq, key);
			} else if (ORES_TYPE_TAXONOMY_ADMIN.equalsIgnoreCase(type)) {
				if (secCallback.canEditTaxonomy()) {
					doTaxonomyAdmin(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerSearchCtrl) {
			if (event instanceof CatalogSearchEvent) {
				CatalogSearchEvent cse = (CatalogSearchEvent)event;
				doSearch(ureq, cse.getSearchString(), false);
			} else if (event == CatalogSearchHeaderController.OPEN_ADMIN_EVENT) {
				doOpenAdmin(ureq);
			} else if (event == CatalogSearchHeaderController.TAXONOMY_ADMIN_EVENT) {
				doTaxonomyAdmin(ureq);
			}
		} else if (source == launchersCtrl) {
			if (event instanceof OpenSearchEvent) {
				OpenSearchEvent ose = (OpenSearchEvent)event;
				headerSearchCtrl.setSearchString(null);
				doSearch(ureq, null, true);
				List<ContextEntry> entries = null;
				if (ose.getInfoRepositoryEntryKey() != null) {
					OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_TYPE_INFOS, ose.getInfoRepositoryEntryKey());
					entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
				}
				catalogRepositoryEntryListCtrl.activate(ureq, entries, ose.getState());
			} else if (event instanceof OpenTaxonomyEvent) {
				OpenTaxonomyEvent ote = (OpenTaxonomyEvent)event;
				doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey(), ote.getEducationalTypeKeys(), ote.getResourceTypes());
			}
		} else if (source instanceof CatalogRepositoryEntryListController) {
			OpenTaxonomyEvent ote = (OpenTaxonomyEvent)event;
			doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey(), ote.getEducationalTypeKeys(), ote.getResourceTypes());
		} else if (source == taxonomyAdminCtrl) {
			if (event == Event.BACK_EVENT) {
				getWindowControl().pop();
			} else if (event == CatalogTaxonomyEditController.OPEN_ADMIN_EVENT) {
				doOpenAdmin(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == stackPanel.getRootController()) {
					// Clicked on root breadcrumb
					doOpenSearchHeader();
					resetStackPanelCssClass();
				} else if (stackPanel.getLastController() instanceof CatalogRepositoryEntryListController) {
					// Clicked on taxonomy level in breadcrumb
					TaxonomyLevel taxonomyLevel = ((CatalogRepositoryEntryListController)stackPanel.getLastController()).getTaxonomyLevel();
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
		mainVC.put("header", headerSearchCtrl.getInitialComponent());
	}

	private void doSearch(UserRequest ureq, String searchString, boolean reset) {
		if (stackPanel.getLastController() != catalogRepositoryEntryListCtrl) {
			if (stackPanel.hasController(catalogRepositoryEntryListCtrl)) {
				// User is on Infos or Offers
				stackPanel.popUpToController(catalogRepositoryEntryListCtrl);
				addToHistory(ureq, catalogRepositoryEntryListCtrl.getWindowControlForDebug());
			} else {
				// User is on Launchers
				removeAsListenerAndDispose(catalogRepositoryEntryListCtrl);
				stackPanel.popUpToRootController(ureq);
				
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_SEARCH), null);
				CatalogRepositoryEntrySearchParams searchParams = defaultSearchParams.copy();
				catalogRepositoryEntryListCtrl = new CatalogRepositoryEntryListController(ureq, swControl, stackPanel, searchParams, false);
				listenTo(catalogRepositoryEntryListCtrl);
				stackPanel.pushController(translate("search.results"), catalogRepositoryEntryListCtrl);
				resetStackPanelCssClass();
			}
		}
		catalogRepositoryEntryListCtrl.search(ureq, searchString, reset);
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
		}
	}
	
	/**
	 * Open all taxonomy level microsites until the requested level.
	 * It takes the first launcher which contains the requested taxonomy level.
	 */
	private void doActivateTaxonomy(UserRequest ureq, Long key) {
		for (CatalogLauncher catalogLauncher : launchersCtrl.getTaxonomyLevelCatalogLaunchers()) {
			Levels levels = taxonomyLevelLauncherHandler.getTaxonomyLevels(catalogLauncher, key, defaultSearchParams);
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
		if (!(stackPanel.getLastController() instanceof CatalogRepositoryEntryListController) && stackPanel.getLastController() != stackPanel.getRootController()) {
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
	
	private void doOpenTaxonomyList(UserRequest ureq, TaxonomyLevel taxonomyLevel, Collection<Long> eductaionalTypeKeys, Collection<String> resourceTypes) {
		removeAsListenerAndDispose(catalogRepositoryEntryListCtrl);
		catalogRepositoryEntryListCtrl = null;

		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(ORES_TYPE_TAXONOMY, taxonomyLevel.getKey()), null);
		CatalogRepositoryEntrySearchParams searchParams = defaultSearchParams.copy();
		searchParams.getIdentToTaxonomyLevels().put(KEY_LAUNCHER, Collections.singletonList(taxonomyLevel));
		if (eductaionalTypeKeys != null && !eductaionalTypeKeys.isEmpty()) {
			searchParams.getIdentToEducationalTypeKeys().put(KEY_LAUNCHER, eductaionalTypeKeys);
		}
		if (resourceTypes != null && !resourceTypes.isEmpty()) {
			searchParams.getIdentToResourceTypes().put(KEY_LAUNCHER, resourceTypes);
		}
		resetStackPanelCssClass();
		CatalogRepositoryEntryListController taxonomyListCtrl = new CatalogRepositoryEntryListController(ureq, swControl, stackPanel, searchParams, true);
		listenTo(taxonomyListCtrl);
		stackPanel.pushController(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel), taxonomyListCtrl);
	}

	@SuppressWarnings("deprecation")
	private void doTaxonomyAdmin(UserRequest ureq) {
		removeAsListenerAndDispose(taxonomyAdminCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_TAXONOMY_ADMIN),
				null);
		stackPanel.setCssClass("o_catalog_breadcrumb");
		taxonomyAdminCtrl = new CatalogTaxonomyEditController(ureq, swControl, secCallback);
		listenTo(taxonomyAdminCtrl);
		
		getWindowControl().pushToMainArea(taxonomyAdminCtrl.getInitialComponent());
	}

	private void resetStackPanelCssClass() {
		stackPanel.setCssClass("o_catalog_breadcrumb");
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
