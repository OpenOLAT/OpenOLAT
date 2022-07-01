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
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLaunchersController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private List<Controller> launcherCtrls;
	
	private final CatalogRepositoryEntrySearchParams defaultSearchParams;
	private final List<CatalogLauncher> taxonomyLevelCatalogLaunchers = new ArrayList<>(2);

	@Autowired
	private CatalogV2Service catalogService;

	protected CatalogLaunchersController(UserRequest ureq, WindowControl wControl, CatalogRepositoryEntrySearchParams defaultSearchParams) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.defaultSearchParams = defaultSearchParams;
		mainVC = createVelocityContainer("launchers");
		
		putInitialPanel(mainVC);
		reload(ureq);
	}

	private void reload(UserRequest ureq) {
		cleanUp();
		
		CatalogLauncherSearchParams searchParams = new CatalogLauncherSearchParams();
		searchParams.setEnabled(Boolean.TRUE);
		List<CatalogLauncher> launchers = catalogService.getCatalogLaunchers(searchParams);
		Collections.sort(launchers);
		launcherCtrls = new ArrayList<>(launchers.size());
		List<String> componentNames = new ArrayList<>(launchers.size());
		for (CatalogLauncher launcher : launchers) {
			CatalogLauncherHandler handler = catalogService.getCatalogLauncherHandler(launcher.getType());
			if (handler.isEnabled()) {
				Controller launcherCtrl = handler.createRunController(ureq, getWindowControl(), getTranslator(), launcher, defaultSearchParams);
				if (launcherCtrl != null) {
					listenTo(launcherCtrl);
					launcherCtrls.add(launcherCtrl);
					String componentName = "launcher_" + launcher.getKey();
					componentNames.add(componentName);
					mainVC.put(componentName, launcherCtrl.getInitialComponent());
					if (TaxonomyLevelLauncherHandler.TYPE.equals(launcher.getType())) {
						taxonomyLevelCatalogLaunchers.add(launcher);
					}
				}
			}
		}
		mainVC.contextPut("componentNames", componentNames);
	}

	private void cleanUp() {
		if (launcherCtrls != null) {
			for (Controller launcherCtrl : launcherCtrls) {
				removeAsListenerAndDispose(launcherCtrl);
			}
		}
	}
	
	public List<CatalogLauncher> getTaxonomyLevelCatalogLaunchers() {
		return taxonomyLevelCatalogLaunchers;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (launcherCtrls.contains(source)) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
