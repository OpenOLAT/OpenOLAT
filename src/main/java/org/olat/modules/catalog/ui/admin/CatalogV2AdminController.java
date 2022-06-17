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
package org.olat.modules.catalog.ui.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.repository.RepositoryModule;
import org.olat.repository.ui.admin.CatalogAdminController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogV2AdminController  extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_SETTINGS = "Settings";
	private static final String ORES_TYPE_CATEGORY_LAUNCHERS = "Launcher";
	private static final String ORES_TYPE_FILTERS = "Filters";
	private static final String ORES_TYPE_LAYOUT = "Layout";
	private static final String ORES_TYPE_CATALOG_V1 = "Config";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link settingsLink;
	private final Link categoryLaunchersLink;
	private final Link catalogFiltersLink;
	private final Link layoutLink;
	private final Link catalogV1Link;
	
	private Controller settingsCtrl;
	private Controller launchersCtrl;
	private Controller filtersCtrl;
	private Controller layoutCtrl;
	private Controller catalogV1Ctrl;
	
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public CatalogV2AdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		settingsLink = LinkFactory.createLink("admin.settings", mainVC, this);
		segmentView.addSegment(settingsLink, true);
		
		categoryLaunchersLink = LinkFactory.createLink("admin.launchers", mainVC, this);
		segmentView.addSegment(categoryLaunchersLink, false);
		
		catalogFiltersLink = LinkFactory.createLink("admin.filters", mainVC, this);
		segmentView.addSegment(catalogFiltersLink, false);
		
		layoutLink = LinkFactory.createLink("admin.layout", mainVC, this);
		segmentView.addSegment(layoutLink, false);
		
		catalogV1Link = LinkFactory.createLink("admin.v1.config", mainVC, this);
		segmentView.addSegment(catalogV1Link, false);
		
		updateUI();
		doOpenSettings(ureq);
		putInitialPanel(mainVC);
	}

	private void updateUI() {
		if (catalogV2Module.isEnabled()) {
			categoryLaunchersLink.setVisible(true);
			catalogFiltersLink.setVisible(true);
			layoutLink.setVisible(true);
			catalogV1Link.setVisible(false);
		} else if (repositoryModule.isCatalogEnabled()) {
			categoryLaunchersLink.setVisible(false);
			catalogFiltersLink.setVisible(false);
			layoutLink.setVisible(false);
			catalogV1Link.setVisible(true);
		} else {
			categoryLaunchersLink.setVisible(false);
			catalogFiltersLink.setVisible(false);
			layoutLink.setVisible(false);
			catalogV1Link.setVisible(false);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_SETTINGS.equalsIgnoreCase(type) && settingsLink.isVisible()) {
			doOpenSettings(ureq);
			segmentView.select(settingsLink);
		} else if (ORES_TYPE_CATEGORY_LAUNCHERS.equalsIgnoreCase(type) && categoryLaunchersLink.isVisible()) {
			doOpenCategoryLaunchers(ureq);
			segmentView.select(categoryLaunchersLink);
		} else if (ORES_TYPE_FILTERS.equalsIgnoreCase(type) && catalogFiltersLink.isVisible()) {
			doOpenCatalogFilters(ureq);
			segmentView.select(catalogFiltersLink);
		} else if (ORES_TYPE_LAYOUT.equals(type) && layoutLink.isVisible()) {
			doOpenLayout(ureq);
			segmentView.select(layoutLink);
		} else if (ORES_TYPE_CATALOG_V1.equals(type) && catalogV1Link.isVisible()) {
			doOpenCatalogV1Config(ureq);
			segmentView.select(catalogV1Link);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == settingsCtrl) {
			if (event == FormEvent.CHANGED_EVENT) {
				updateUI();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == settingsLink) {
					doOpenSettings(ureq);
				} else if (clickedLink == categoryLaunchersLink) {
					doOpenCategoryLaunchers(ureq);
				} else if (clickedLink == catalogFiltersLink) {
					doOpenCatalogFilters(ureq);
				} else if (clickedLink == layoutLink) {
					doOpenLayout(ureq);
				} else if (clickedLink == catalogV1Link) {
					doOpenCatalogV1Config(ureq);
				}
			}
		}
	}

	private void doOpenSettings(UserRequest ureq) {
		if (settingsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_SETTINGS), null);
			settingsCtrl = new CatalogSettingsController(ureq, swControl);
			listenTo(settingsCtrl);
		} else {
			addToHistory(ureq, settingsCtrl);
		}
		mainVC.put("segmentCmp", settingsCtrl.getInitialComponent());
	}

	private void doOpenCategoryLaunchers(UserRequest ureq) {
		if (launchersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_CATEGORY_LAUNCHERS), null);
			launchersCtrl = new CatalogLauncherListController(ureq, swControl);
			listenTo(launchersCtrl);
		} else {
			addToHistory(ureq, launchersCtrl);
		}
		mainVC.put("segmentCmp", launchersCtrl.getInitialComponent());
	}

	private void doOpenCatalogFilters(UserRequest ureq) {
		if (filtersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_FILTERS), null);
			filtersCtrl = new CatalogFilterListController(ureq, swControl);
			listenTo(filtersCtrl);
		} else {
			addToHistory(ureq, filtersCtrl);
		}
		mainVC.put("segmentCmp", filtersCtrl.getInitialComponent());
	}

	private void doOpenLayout(UserRequest ureq) {
		if (layoutCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_LAYOUT), null);
			layoutCtrl = new CatalogLayoutController(ureq, swControl);
			listenTo(layoutCtrl);
		} else {
			addToHistory(ureq, layoutCtrl);
		}
		mainVC.put("segmentCmp", layoutCtrl.getInitialComponent());
	}

	private void doOpenCatalogV1Config(UserRequest ureq) {
		if (catalogV1Ctrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_CATALOG_V1), null);
			catalogV1Ctrl = new CatalogAdminController(ureq, swControl);
			listenTo(catalogV1Ctrl);
		} else {
			addToHistory(ureq, catalogV1Ctrl);
		}
		mainVC.put("segmentCmp", catalogV1Ctrl.getInitialComponent());
	}

}
