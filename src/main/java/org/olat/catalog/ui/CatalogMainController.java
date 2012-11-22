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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 21.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogMainController extends BasicController implements Activateable2, StackedControllerAware {

	private Link loginLink;
	private final VelocityContainer mainVC;
	private CatalogNodeController nodeController;

	private final CatalogManager catalogManager;
	
	public CatalogMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		catalogManager = CoreSpringFactory.getImpl(CatalogManager.class);
		
		mainVC = createVelocityContainer("main_catalog");
		if(ureq.getUserSession().getRoles().isGuestOnly()) {
			mainVC.contextPut("isGuest", Boolean.TRUE);
			loginLink = LinkFactory.createLink("cat.login", mainVC, this);
		}

		List<CatalogEntry> rootNodes = catalogManager.getRootCatalogEntries();
		if(rootNodes.size() == 1) {
			boolean admin = ureq.getUserSession().getRoles().isOLATAdmin();
			nodeController = new CatalogNodeController(ureq, getWindowControl(), rootNodes.get(0), admin);
			mainVC.put("node", nodeController.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		if(nodeController != null) {
			nodeController.setStackedController(stackPanel);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == loginLink) {
			//login screen
		}
	}


	
	

}
