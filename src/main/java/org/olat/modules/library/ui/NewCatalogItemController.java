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
package org.olat.modules.library.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.notifications.ui.DateChooserController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.ui.event.OpenFolderEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is the controller for the notifications of new documents,
 * with subscribe / unsubscribe.<br>
 * Events fired:
 * <ul>
 *   <li>OpenFolderEvent</li>
 * </ul>
 * <P>
 * Initial Date:  4 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NewCatalogItemController extends BasicController {
	
	private CatalogController catalogController;
	private ContextualSubscriptionController csController;
	
	private VelocityContainer mainVC;
	private final String mapperBaseURL;
	private final String thumbnailMapperBaseURL;
	private OLATResourceable libraryOres;
	private DateChooserController dateChooserController;
	
	@Autowired
	private LibraryManager libraryManager;
		
	public NewCatalogItemController(UserRequest ureq, WindowControl control, String mapperBaseURL, String thumbnailMapperBaseURL,
			OLATResourceable libraryOres) {
		super(ureq, control);
		this.mapperBaseURL = mapperBaseURL;
		this.libraryOres = libraryOres;
		this.thumbnailMapperBaseURL = thumbnailMapperBaseURL;
		mainVC = createVelocityContainer("newCatalogItems");

		SubscriptionContext subsContext = libraryManager.getSubscriptionContext();
		PublisherData publisherData = libraryManager.getPublisherData();
		csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
		listenTo(csController);
		mainVC.put("subscription", csController.getInitialComponent());
		
		if(subsContext != null) {
			catalogController = new CatalogController(ureq, control, mapperBaseURL, thumbnailMapperBaseURL, true, libraryOres);
			listenTo(catalogController);
		}
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		dateChooserController = new DateChooserController(ureq, control, cal.getTime());
		listenTo(dateChooserController);
		mainVC.put("dateChooser", dateChooserController.getInitialComponent());
		
		updateUI(ureq, cal.getTime());
		putInitialPanel(mainVC);
	}
	
	protected void updateRepositoryEntry(UserRequest ureq, OLATResourceable ores) {
		this.libraryOres = ores;
		if(csController != null) {
			removeAsListenerAndDispose(csController);
		}
		SubscriptionContext subsContext = libraryManager.getSubscriptionContext();
		PublisherData publisherData = libraryManager.getPublisherData();
		csController = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, publisherData);
		listenTo(csController);
		mainVC.put("subscription", csController.getInitialComponent());
		
		if(catalogController != null) {
			removeAsListenerAndDispose(catalogController);
		}
		if(subsContext != null) {
			catalogController = new CatalogController(ureq, getWindowControl(), mapperBaseURL, thumbnailMapperBaseURL, true, libraryOres);
			listenTo(catalogController);
		}
	}
	
	protected void updateUI(UserRequest ureq, Date compareDate) {
	//list of new users
		Subscriber subscriber = libraryManager.getSubscriber(getIdentity());
		if(subscriber == null || !subscriber.isEnabled()) {
			//hasn't subscribed to new users notifications
			mainVC.contextPut("hasSubscriptions", "false");
		} else {
			mainVC.contextPut("hasSubscriptions", "true");
			List<CatalogItem> items = libraryManager.getNewCatalogItems(compareDate, getLocale());
			catalogController.display(items, ureq);
			mainVC.put("notifications", catalogController.getInitialComponent());
			
			if(!items.isEmpty()) {
				mainVC.contextPut("hasNews", "true");
			} else {
				Formatter form = Formatter.getInstance(ureq.getLocale());
				mainVC.contextPut("noNews", translate("library.notification.noNews", form.formatDateAndTime(compareDate)));
				mainVC.contextPut("hasNews", "false");
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == catalogController) {
			if(event instanceof OpenFolderEvent) {
				fireEvent(ureq, event);
			}
		}
		else if(source == csController) {
			String cmd = event.getCommand();
			if("command.subscribe".equals(cmd) || "command.unsubscribe".equals(cmd)) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
		} else if(source == dateChooserController) {
			if(Event.CHANGED_EVENT == event) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
		}		
	}
}
