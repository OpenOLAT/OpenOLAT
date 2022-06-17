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

import java.util.Collections;
import java.util.List;

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
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogMainController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final CatalogSearchHeaderController headerSearchCtrl;
	private final BreadcrumbedStackedPanel stackPanel;
	private final CatalogLaunchersController launchersCtrl;
	private CatalogTaxonomyHeaderController headerTaxonomyCtrl;
	private CatalogRepositoryEntryListController catalogRepositoryEntryListCtrl;
	
	private final CatalogRepositoryEntrySearchParams defaultSearchParams;
	
	@Autowired
	private ACService acService;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public CatalogMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.defaultSearchParams = createDefaultSearchParams(ureq);
		
		mainVC = createVelocityContainer("main");
		
		headerSearchCtrl = new CatalogSearchHeaderController(ureq, wControl);
		listenTo(headerSearchCtrl);
		mainVC.put("header", headerSearchCtrl.getInitialComponent());
		
		stackPanel = new BreadcrumbedStackedPanel("catalogstack", getTranslator(), this);
		stackPanel.setCssClass("o_catalog_breadcrumb");
		stackPanel.setInvisibleCrumb(0);
		mainVC.put("stack", stackPanel);
		
		launchersCtrl = new CatalogLaunchersController(ureq, wControl, defaultSearchParams.copy());
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
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if ("Search".equalsIgnoreCase(type)) {
			headerSearchCtrl.setSearchString(null);
			doSearch(ureq, null, true);
			entries = entries.subList(1, entries.size());
			catalogRepositoryEntryListCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == headerSearchCtrl) {
			if (event instanceof CatalogSearchEvent) {
				CatalogSearchEvent cse = (CatalogSearchEvent)event;
				doSearch(ureq, cse.getSearchString(), false);
			}
		} else if (source == launchersCtrl) {
			if (event instanceof OpenSearchEvent) {
				OpenSearchEvent ose = (OpenSearchEvent)event;
				headerSearchCtrl.setSearchString(null);
				doSearch(ureq, null, true);
				List<ContextEntry> entries = null;
				if (ose.getInfoRepositoryEntryKey() != null) {
					OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", ose.getInfoRepositoryEntryKey());
					entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
				}
				catalogRepositoryEntryListCtrl.activate(ureq, entries, ose.getState());
			} else if (event instanceof OpenTaxonomyEvent) {
				OpenTaxonomyEvent ote = (OpenTaxonomyEvent)event;
				doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey());
			}
		} else if (source instanceof CatalogRepositoryEntryListController) {
			OpenTaxonomyEvent ote = (OpenTaxonomyEvent)event;
			doOpenTaxonomy(ureq, ote.getTaxonomyLevelKey());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == stackPanel) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == stackPanel.getRootController()) {
					// Clicked on root breadcrumb
					removeAsListenerAndDispose(headerTaxonomyCtrl);
					headerTaxonomyCtrl = null;
					mainVC.put("header", headerSearchCtrl.getInitialComponent());
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
			
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Search"), null);
				CatalogRepositoryEntrySearchParams searchParams = defaultSearchParams.copy();
				catalogRepositoryEntryListCtrl = new CatalogRepositoryEntryListController(ureq, swControl, stackPanel, searchParams, null, false);
				listenTo(catalogRepositoryEntryListCtrl);
				stackPanel.pushController(translate("search"), catalogRepositoryEntryListCtrl);
			}
		}
		catalogRepositoryEntryListCtrl.search(ureq, searchString, reset);
	}

	private void doOpenTaxonomy(UserRequest ureq, Long taxonomyLevelKey) {
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> taxonomyLevelKey);
		if (taxonomyLevel != null) {
			popUpToTaxonomyCtrl();
			doOpenTaxonomyHeader(ureq, taxonomyLevel);
			doOpenTaxonomyList(ureq, taxonomyLevel);
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
	
	private void doOpenTaxonomyList(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		removeAsListenerAndDispose(catalogRepositoryEntryListCtrl);
		catalogRepositoryEntryListCtrl = null;
	
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Taxonomy", taxonomyLevel.getKey()), null);
		CatalogRepositoryEntrySearchParams searchParams = defaultSearchParams.copy();
		searchParams.getIdentToTaxonomyLevels().put("launcher", Collections.singletonList(taxonomyLevel));
		CatalogRepositoryEntryListController taxonomyListCtrl = new CatalogRepositoryEntryListController(ureq, swControl, stackPanel, searchParams, taxonomyLevel, true);
		listenTo(taxonomyListCtrl);
		stackPanel.pushController(taxonomyLevel.getDisplayName(), taxonomyListCtrl);
	}

}
