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
package org.olat.core.commons.services.notifications.ui;

import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.services.notifications.NotificationUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.home.HomeMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller shows the list of the users subscriptions and allows him to
 * manage (delete) them. This controller does not show the actual news generated
 * by the subscriptions, use the NotificationNewsController for this purpose.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CHANGED_EVENT when a subscription has been deleted</li>
 * </ul>
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationSubscriptionController extends BasicController {
	private VelocityContainer subscriptionsVC;
	private TableController subscriptionsTableCtr;
	private NotificationSubscriptionTableDataModel subscriptionsTableModel;
	private DialogBoxController delYesNoC;
	private Identity subscriberIdentity;
	
	@Autowired
	private NotificationsManager notificationsManager;

	public NotificationSubscriptionController(UserRequest ureq, WindowControl wControl,
			Identity subscriberIdentity, boolean adminColumns, boolean fieldSet) {
		// use home fallback for rss translations
		super(ureq, wControl, Util.createPackageTranslator(HomeMainController.class, ureq.getLocale()));
		this.subscriberIdentity = subscriberIdentity;
		// Build the table that contains all the subscriptions
		TableGuiConfiguration tableGuiPrefs = new TableGuiConfiguration();
		tableGuiPrefs.setTableEmptyMessage(translate("subscriptions.no.subscriptions"), translate("subscriptions.no.subscriptions.hint"), "o_icon_notification");
		tableGuiPrefs.setPreferencesOffered(true, "notifications-" + adminColumns);
		subscriptionsTableCtr = new TableController(tableGuiPrefs, ureq, wControl, getTranslator());
		subscriptionsTableModel = new NotificationSubscriptionTableDataModel(getTranslator());
		subscriptionsTableModel.addTableColumns(subscriptionsTableCtr, adminColumns);
		updateSubscriptionsDataModel();
		listenTo(subscriptionsTableCtr);
		// Main view is a velocity container
		subscriptionsVC = createVelocityContainer(fieldSet ? "notificationsSubscriptionsField" : "notificationsSubscriptions");
		subscriptionsVC.put("subscriptionsTableCtr", subscriptionsTableCtr.getInitialComponent());
		putInitialPanel(subscriptionsVC);
	}

	/**
	 * Update the table model
	 * 
	 * @param ureq
	 */
	void updateSubscriptionsDataModel() {
		// Load subscriptions from DB. Don't use the ureq.getIdentity() but the
		// subscriberIdentity instead to make this controller also be usable in the
		// admin environment (admins might change notifications for a user)
		List<Subscriber> subs = notificationsManager.getSubscribers(subscriberIdentity, true);
		for(Iterator<Subscriber> subIt=subs.iterator(); subIt.hasNext(); ) {
			Subscriber sub = subIt.next();
			if(!notificationsManager.isPublisherValid(sub.getPublisher())) {
				subIt.remove();
			}
		}
		
		subscriptionsTableModel.setObjects(subs);
		// Tell table about model change or set table model if not already set
		if (subscriptionsTableCtr.getTableDataModel() == null) {
			subscriptionsTableCtr.setTableDataModel(subscriptionsTableModel);
		} else {
			subscriptionsTableCtr.modelChanged(true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == subscriptionsTableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				Subscriber sub = subscriptionsTableModel.getObject(te.getRowId());
				if (actionid.equals("launch")) {
					// User want to go to the subscription source, e.g. the forum or the
					// folder
					NotificationUIFactory.launchSubscriptionResource(ureq, getWindowControl(), sub);
				} else if (actionid.equals("del")) {
					delYesNoC = activateYesNoDialog(ureq, null, translate("confirm.delete"), delYesNoC);
					delYesNoC.setUserObject(sub);
				}
			}
		} else if (source == delYesNoC) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				// Remove subscription and update data model
				Subscriber sub = (Subscriber) delYesNoC.getUserObject();
				notificationsManager.unsubscribe(sub);
				updateSubscriptionsDataModel();
				showInfo("info.notification.deleted");
				// Notify parent controller
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			// cleanup dialog
			delYesNoC.dispose();
			delYesNoC = null;
		}
	}
}
