/**
 * OLAT - Online Learning and Training<br>
 * https://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <hr>
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * This file has been modified by the OpenOLAT community. Changes are licensed
 * under the Apache 2.0 license as the original file.
 * <p>
 */

package org.olat.core.commons.services.notifications.ui;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * Controller having a subscribe / unsubscribe button for notifications depending on users state
 *
 * @author Felix Jost
 */
public class ContextualSubscriptionController extends BasicController {

	private Link subscribeButton;
	private final VelocityContainer myContent;

	private final PublisherData publisherData;
	private final SubscriptionContext subscriptionContext;
	private Subscriber subscriber;

	private CloseableCalloutWindowController eventCalloutCtrl;
	private Controller contextualSubscriptionListCtrl;

	@Autowired
	private NotificationsManager notificationsManager;

	/**
	 * @param ureq                The user request
	 * @param wControl            The window control
	 * @param subscriptionContext The subscription context (which resource is involved)
	 * @param publisherData       The publisher data
	 */
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl,
											SubscriptionContext subscriptionContext, PublisherData publisherData) {
		this(ureq, wControl, subscriptionContext, publisherData, false);
	}

	/**
	 * @param ureq                The user request
	 * @param wControl            The window control
	 * @param subscriptionContext The subscription context (which resource is involved)
	 * @param publisherData       The publisher data
	 * @param optOut              true will automatically subscribe the user if it has not previously opt-out
	 */
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl,
											SubscriptionContext subscriptionContext, PublisherData publisherData, boolean optOut) {
		super(ureq, wControl);
		this.subscriptionContext = subscriptionContext;
		this.publisherData = publisherData;
		myContent = createVelocityContainer("consubs");
		myContent.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		subscriber = notificationsManager.getSubscriber(getIdentity(), subscriptionContext);

		if (subscriptionContext == null) {
			putInitialPanel(new Panel("empty:nosubscription"));
			return;
		}

		if (optOut) {
			notificationsManager.subscribe(getIdentity(), subscriptionContext, publisherData);
		}

		subscribeButton = LinkFactory.createLink("command.subscribe", "subscribe", getTranslator(), myContent, this, Link.BUTTON_LARGE + Link.NONTRANSLATED);
		subscribeButton.setCustomDisplayText("");
		toggleSubscriptionIcon();
		subscribeButton.setIconRightCSS("o_icon o_icon_caret");
		subscribeButton.setElementCssClass("o_noti_subscribe_link");
		subscribeButton.setGhost(true);

		putInitialPanel(myContent);
	}

	private void doOpenEventCallout(UserRequest ureq) {
		if (eventCalloutCtrl != null && contextualSubscriptionListCtrl != null) return;

		removeAsListenerAndDispose(eventCalloutCtrl);
		removeAsListenerAndDispose(contextualSubscriptionListCtrl);

		contextualSubscriptionListCtrl = new ContextualSubscriptionListController(ureq, getWindowControl(), subscriptionContext, publisherData);
		listenTo(contextualSubscriptionListCtrl);

		Component eventCmp = contextualSubscriptionListCtrl.getInitialComponent();
		eventCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), eventCmp, subscribeButton.getDispatchID(),
				null, true, "o_sub_event_callout");
		listenTo(eventCalloutCtrl);
		eventCalloutCtrl.activate();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == subscribeButton) {
			doOpenEventCallout(ureq);
		}
	}

	@Override
	protected void doDispose() {
		cleanUp();
		super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (eventCalloutCtrl == source) {
			cleanUp();
		} else if (contextualSubscriptionListCtrl == source) {
			toggleSubscriptionIcon();
		}
		super.event(ureq, source, event);
	}

	private void toggleSubscriptionIcon() {
		myContent.contextRemove("command.subscribe");
		subscriber = notificationsManager.getSubscriber(getIdentity(), subscriptionContext);
		if (subscriber != null && subscriber.isEnabled()) {
			subscribeButton.setIconLeftCSS("o_icon o_icon-bell");
		} else {
			subscribeButton.setIconLeftCSS("o_icon o_icon-bell-slash");
		}
		myContent.contextPut("command.subscribe", subscribeButton);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(contextualSubscriptionListCtrl);
		removeAsListenerAndDispose(eventCalloutCtrl);
		contextualSubscriptionListCtrl = null;
		eventCalloutCtrl = null;
	}
}