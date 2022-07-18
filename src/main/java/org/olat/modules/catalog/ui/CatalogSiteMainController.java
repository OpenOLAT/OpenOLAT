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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.catalog.CatalogNodeController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSiteMainController extends BasicController implements Activateable2 {

	private BreadcrumbedStackedPanel stackPanel;
	private CatalogMainController catalogMainCtrl;
	private CatalogNodeController catalogNodeCtrl;
	
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CatalogManager catalogManager;
	
	public CatalogSiteMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		if (catalogV2Module.isEnabled()) {
			catalogMainCtrl = new CatalogMainController(ureq, getWindowControl());
			listenTo(catalogMainCtrl);
			addToHistory(ureq, catalogMainCtrl);
			putInitialPanel(catalogMainCtrl.getInitialComponent());
		} else if (repositoryModule.isCatalogEnabled()) {
			stackPanel = new BreadcrumbedStackedPanel("catstack", getTranslator(), this);
			stackPanel.setInvisibleCrumb(0);
			putInitialPanel(stackPanel);
			
			List<CatalogEntry> rootNodes = catalogManager.getRootCatalogEntries();
			CatalogEntry root = rootNodes.get(0);
			if(rootNodes.size() == 1) {
				catalogNodeCtrl = new CatalogNodeController(ureq, getWindowControl(), getWindowControl(), root, stackPanel, true);
				listenTo(catalogNodeCtrl);
			}
			stackPanel.pushController(root.getShortTitle(), catalogNodeCtrl);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if (catalogMainCtrl != null) {
				catalogMainCtrl.activate(ureq, entries, state);
				addToHistory(ureq, catalogMainCtrl);
			}
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Catalog".equalsIgnoreCase(type)) {
				entries = entries.subList(1, entries.size());
			}
			
			if (catalogMainCtrl != null) {
				catalogMainCtrl.activate(ureq, entries, state);
				addToHistory(ureq, catalogMainCtrl);
			} else if (catalogNodeCtrl != null) {
				stackPanel.popUpToRootController(ureq);
				catalogNodeCtrl.activate(ureq, entries, state);
			}
		}
	}
}
