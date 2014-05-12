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
package org.olat.catalog.ui;

import java.util.List;

import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 12.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogAdminSiteMainController extends BasicController implements Activateable2 {

	private CatalogController catalogCtrl;
	private LayoutMain3ColsController columnsLayoutCtr;
	
	public CatalogAdminSiteMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		CatalogManager catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
		List<CatalogEntry> rootNodes = catalogManager.getRootCatalogEntries();
		if(rootNodes.size() == 1) {
			catalogCtrl = new CatalogController(ureq, getWindowControl());
			listenTo(catalogCtrl);
		}
		
		if(catalogCtrl != null) {
			Component mainPanel = catalogCtrl.getInitialComponent();
			ToolController toolCtrl = catalogCtrl.createCatalogToolController();
			columnsLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, toolCtrl.getInitialComponent(), mainPanel, "repomain");
			columnsLayoutCtr.addCssClassToMain("o_repository");
			listenTo(columnsLayoutCtr);
			putInitialPanel(columnsLayoutCtr.getInitialComponent());
		} else {
			putInitialPanel(new Panel("empty"));
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == catalogCtrl) {
			if (event == Event.CHANGED_EVENT) {
				ToolController toolC = catalogCtrl.createCatalogToolController();
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnsLayoutCtr.setCol2(toolComp);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}
