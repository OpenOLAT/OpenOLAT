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
package org.olat.repository.ui.catalog;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.repository.CatalogEntry;
import org.olat.repository.manager.CatalogManager;

/**
 * 
 * Initial date: 16.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSiteMainController extends BasicController implements Activateable2 {

	private CatalogNodeController nodeController;
	private final BreadcrumbedStackedPanel stackPanel;
	
	public CatalogSiteMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		stackPanel = new BreadcrumbedStackedPanel("catstack", getTranslator(), this);
		stackPanel.setInvisibleCrumb(0); // show root level

		putInitialPanel(stackPanel);

		CatalogManager catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
		List<CatalogEntry> rootNodes = catalogManager.getRootCatalogEntries();
		CatalogEntry root = rootNodes.get(0);
		// use same title as catalog site title
		
		if(rootNodes.size() == 1) {
			nodeController = new CatalogNodeController(ureq, getWindowControl(), getWindowControl(), root, stackPanel, true);
		}
		stackPanel.pushController(root.getShortTitle(), nodeController);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("Catalog".equalsIgnoreCase(type)) {
			//remove the Catalog/0
			entries = entries.subList(1, entries.size());
		}
		
		stackPanel.popUpToRootController(ureq);
		nodeController.activate(ureq, entries, state);
	}
}
