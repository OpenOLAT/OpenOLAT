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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessageNotification;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachingNotificationsController extends BasicController implements GenericEventListener {
	
	private final Link notificationsLink;
	
	private final OLATResourceable personalEventsOres;
	
	private CoachingNotificationsListController listCtrl;
	private CloseableCalloutWindowController listCalloutCtrl;
	
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CoachingNotificationsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("tool_notifications");
		mainVC.setDomReplacementWrapperRequired(false);
		notificationsLink = LinkFactory.createLink("coaching.notifications.link", "notifications", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		notificationsLink.setCustomDisplayText("0");
		notificationsLink.setElementCssClass("badge");
		notificationsLink.setIconRightCSS("o_icon o_icon_time");
		notificationsLink.setDomReplacementWrapperRequired(false);
		
		StackedPanel panel = new SimpleStackedPanel("coachingNotificationsPanel");
		panel.setContent(mainVC);
		putInitialPanel(panel);
		loadNotifications();

		personalEventsOres = OresHelper.createOLATResourceableInstance(InstantMessagingService.PERSONAL_EVENT_ORES_NAME, getIdentity().getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), personalEventsOres);
	}

	private void loadNotifications() {
		long numOfNotifications = imService.countRequestNotifications(getIdentity());
		notificationsLink.setCustomDisplayText(Long.toString(numOfNotifications));
		notificationsLink.setVisible(numOfNotifications > 0);
		notificationsLink.setElementCssClass("badge o_notification_request");
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, personalEventsOres);
		super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof InstantMessagingEvent) {
			processInstantMessagingEvent((InstantMessagingEvent)event);
		}
	}
	
	private void processInstantMessagingEvent(InstantMessagingEvent event) {
		try {
			if(InstantMessagingEvent.REQUEST.equals(event.getCommand())
					|| InstantMessagingEvent.DELETE_NOTIFICATION.equals(event.getCommand())
					|| InstantMessagingEvent.END_CHANNEL.equals(event.getCommand())) {
				loadNotifications();
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(listCtrl == source) {
			listCalloutCtrl.deactivate();
			cleanUp();
		} else if(listCalloutCtrl == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(listCalloutCtrl);
		removeAsListenerAndDispose(listCtrl);
		listCalloutCtrl = null;
		listCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(notificationsLink == source) {
			doOpenNotifications(ureq, notificationsLink);
		}
	}
	
	private void doOpenNotifications(UserRequest ureq, Link link) {
		removeAsListenerAndDispose(listCtrl);
		removeAsListenerAndDispose(listCalloutCtrl);

		listCtrl = new CoachingNotificationsListController(ureq, getWindowControl());
		listenTo(listCtrl);

		listCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				listCtrl.getInitialComponent(), link.getDispatchID(), "", true, "");
		listenTo(listCalloutCtrl);
		listCalloutCtrl.activate();
	}
	
	private class CoachingNotificationsListController extends FormBasicController {
		
		private int count = 0;
		
		public CoachingNotificationsListController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "tool_notifications_callout");
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			List<InstantMessageNotification> notifications = imService.getRequestNotifications(getIdentity());
			Map<OLATResourceable,LinkWithMessage> resourcesMap = new HashMap<>();
			for(InstantMessageNotification notification:notifications) {
				LinkWithMessage wrapper = resourcesMap.get(notification.getChatResource());
				if(wrapper == null) {
					FormLink link = uifactory.addFormLink("notification_" + (++count), "communication", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
					wrapper = new LinkWithMessage(link);
					link.setUserObject(wrapper);
					resourcesMap.put(notification.getChatResource(), wrapper);
					forgeLink(notification, link, wrapper);
				}
				wrapper.incrementRequests();
			}
			
			List<LinkWithMessage> communications = new ArrayList<>(resourcesMap.values());
			for(LinkWithMessage communication:communications) {
				String i18nMsg = communication.getNumOfRequests() <= 1 ? "coaching.tool.communication.new.request" : "coaching.tool.communication.new.requests";
				communication.setMessage(translate(i18nMsg, Integer.toString(communication.getNumOfRequests())));
			}
			((FormLayoutContainer)formLayout).contextPut("communicationLinks", communications);
		}
		
		private boolean forgeLink(InstantMessageNotification notification, FormLink link, LinkWithMessage wrapper) {
			boolean ok = false;
			String resName = notification.getChatResource().getResourceableTypeName();
			if("CourseModule".equals(resName)) {
				RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(notification.getChatResource(), false);
				if(entry != null) {
					String businessPath = "[RepositoryEntry:" + entry.getKey() + "][CourseNode:" + notification.getResSubPath() + "][Communication:0]";
					wrapper.setBusinessPath(businessPath);
					link.getComponent().setCustomDisplayText(entry.getDisplayname());
					link.setUrl(BusinessControlFactory.getInstance()
							.getAuthenticatedURLFromBusinessPathString(businessPath));
					link.setIconLeftCSS("o_icon o_red_led");
				}
			} else {
				logWarn("Notifications of type: " + resName, null);
			}
			return ok;
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(source instanceof FormLink) {
				FormLink link = (FormLink)source;
				if("communication".equals(link.getCmd()) && link.getUserObject() instanceof LinkWithMessage) {
					doOpenResource(ureq, ((LinkWithMessage)link.getUserObject()).getBusinessPath());
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
		
		private void doOpenResource(UserRequest ureq, String businessPath) {
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
		}
	}
	
	public static class LinkWithMessage {
		
		private final FormLink link;
		private String businessPath;
		private String message;
		private int numOfRequests = 0;
		
		public LinkWithMessage(FormLink link) {
			this.link = link;
		}

		public FormLink getLink() {
			return link;
		}
		
		public String getBusinessPath() {
			return businessPath;
		}
		
		public void setBusinessPath(String businessPath) {
			this.businessPath = businessPath;
		}

		public String getMessage() {
			return message;
		}
		
		public void setMessage(String message) {
			this.message = message;
		}
		
		public int getNumOfRequests() {
			return numOfRequests;
		}
		
		public void incrementRequests() {
			numOfRequests++;
		}
	}
}
