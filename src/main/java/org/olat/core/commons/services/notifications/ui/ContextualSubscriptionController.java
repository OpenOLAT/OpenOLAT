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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
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
	private VelocityContainer myContent;

	private List<PublisherDecorated> publishers;

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
		Publisher publisher = notificationsManager.getOrCreatePublisherWithData(subscriptionContext, publisherData, null, PublisherChannel.PULL);
		if (publisher == null) {
			putInitialPanel(new Panel("empty:nosubscription"));
			return;
		}
		
		if (optOut) {
			notificationsManager.subscribe(getIdentity(), publisher);
		}
		publishers = List.of(new PublisherDecorated(publisher, "command.subscribe", false));
		init();
	}
	
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl, List<PublisherDecorated> labelledPublishers, boolean optOut) {
		super(ureq, wControl);
		this.publishers = List.copyOf(labelledPublishers);

		if (optOut) {
			List<Publisher> publishers = labelledPublishers.stream()
					.map(PublisherDecorated::publisher)
					.toList();
			List<Subscriber> subscribers = notificationsManager.getSubscribers(getIdentity(), publishers);
			Map<Publisher,Subscriber> subscribersMap = subscribers.stream()
					.collect(Collectors.toMap(Subscriber::getPublisher, p -> p, (u, v) -> u));
			for(PublisherDecorated publisher:labelledPublishers) {
				if(!subscribersMap.containsKey(publisher.publisher())) {
					notificationsManager.subscribe(getIdentity(), publisher.publisher());
				}
			}
		}
		init();
	}

	private void init() {
		myContent = createVelocityContainer("consubs");
		myContent.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID

		subscribeButton = LinkFactory.createLink("command.subscribe", "subscribe", getTranslator(), myContent, this, Link.BUTTON + Link.NONTRANSLATED);
		subscribeButton.setTitle(translate("command.subscribe"));
		subscribeButton.setCustomDisplayText("");
		subscribeButton.setIconRightCSS("o_icon o_icon-fw o_icon_caret");
		subscribeButton.setElementCssClass("o_noti_subscribe_link");
		subscribeButton.setGhost(true);
		
		toggleSubscriptionIcon();

		putInitialPanel(myContent);
	}

	private void doOpenEventCallout(UserRequest ureq) {
		if (eventCalloutCtrl != null && contextualSubscriptionListCtrl != null) return;

		removeAsListenerAndDispose(eventCalloutCtrl);
		removeAsListenerAndDispose(contextualSubscriptionListCtrl);

		contextualSubscriptionListCtrl = new ContextualSubscriptionListController(ureq, getWindowControl(), publishers);
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
		
		List<Publisher> list = publishers.stream()
				.map(PublisherDecorated::publisher)
				.toList();
		boolean hasSubscriber = notificationsManager.hasSubscribers(getIdentity(), list);
		if (hasSubscriber) {
			subscribeButton.setIconLeftCSS("o_icon o_icon-fw o_icon-bell");
		} else {
			subscribeButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reminder");
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(contextualSubscriptionListCtrl);
		removeAsListenerAndDispose(eventCalloutCtrl);
		contextualSubscriptionListCtrl = null;
		eventCalloutCtrl = null;
	}
}