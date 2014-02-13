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
package org.olat.admin.user;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.services.notifications.PublisherData;
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
import org.olat.core.id.Identity;
import org.olat.user.notification.UsersSubscriptionManager;

/**
 * 
 * Description:<br>
 * This workflow show the latest created users based on notifications.
 * Form the list an identity can be selected which results in a
 * SingleIdentityChosenEvent.
 * <P>
 * Initial Date:  18 august 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com
 */
public class NewUsersNotificationsController extends BasicController {

	private DateChooserController dateChooserController;
	private UsermanagerUserSearchController searchController;
	private ContextualSubscriptionController subscriptionController;
	
	private VelocityContainer mainVC;
	
	public NewUsersNotificationsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("newusersNotifications");
		
		// subscribe/unsubscribe
		SubscriptionContext subContext = UsersSubscriptionManager.getInstance().getNewUsersSubscriptionContext();
		PublisherData publisherData = UsersSubscriptionManager.getInstance().getNewUsersPublisherData();
		
		subscriptionController = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, publisherData);
		listenTo(subscriptionController);
		mainVC.put("newUsersSubscription", subscriptionController.getInitialComponent());
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		dateChooserController = new DateChooserController(ureq, wControl, cal.getTime());
		listenTo(dateChooserController);
		mainVC.put("dateChooser", dateChooserController.getInitialComponent());
		
		updateUI(ureq, cal.getTime());
		putInitialPanel(mainVC);
	}
	
	private void updateUI(UserRequest ureq, Date compareDate) {
		if(searchController != null) {
			removeAsListenerAndDispose(searchController);
		}
		List<Identity> identities = UsersSubscriptionManager.getInstance().getNewIdentityCreated(compareDate);
		searchController = new UsermanagerUserSearchController(ureq, getWindowControl(), identities, Identity.STATUS_VISIBLE_LIMIT, true, false);
		listenTo(searchController);
		mainVC.put("notificationsList", searchController.getInitialComponent());

		if(identities.isEmpty()) {
			mainVC.contextPut("hasNews", "false");
		} else {
			mainVC.contextPut("hasNews", "true");
		}
	}

	@Override
	protected void doDispose() {
		// controllers autodisposed by basic controller
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchController) {
			if(event instanceof SingleIdentityChosenEvent) {
				fireEvent(ureq, event);
			}
		}
		else if(source == subscriptionController) {
			String cmd = event.getCommand();
			if("command.subscribe".equals(cmd)) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
			else if("command.unsubscribe".equals(cmd)) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
			else if("command.markread".equals(cmd)) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
		} else if(source == dateChooserController) {
			if(Event.CHANGED_EVENT == event) {
				updateUI(ureq, dateChooserController.getChoosenDate());
			}
		}
	}
}