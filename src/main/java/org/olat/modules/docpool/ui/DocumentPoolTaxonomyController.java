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
package org.olat.modules.docpool.ui;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.docpool.manager.DocumentPoolNotificationsHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolTaxonomyController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private SinglePageController indexCtrl;
	
	@Autowired
	private DocumentPoolModule docPoolModule;
	@Autowired
	private DocumentPoolNotificationsHandler notificationsHandler;
	
	public DocumentPoolTaxonomyController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("document_pool");
		
		//add subscription
		SubscriptionContext subsContext = notificationsHandler.getTaxonomyDocumentsLibrarySubscriptionContext();
		PublisherData data = notificationsHandler.getTaxonomyDocumentsLibraryPublisherData();
		ContextualSubscriptionController csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
		listenTo(csController);
		mainVC.put("subscription", csController.getInitialComponent());
		
		VFSContainer container = docPoolModule.getInfoPageContainer();
		if(container.resolve("index.html") != null) {
			indexCtrl = new SinglePageController(ureq, getWindowControl(), container, "index.html", false);
			listenTo(indexCtrl);
			mainVC.put("index", indexCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
