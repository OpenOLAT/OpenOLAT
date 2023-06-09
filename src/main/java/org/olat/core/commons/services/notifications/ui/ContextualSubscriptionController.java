/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
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
* <a href="http://www.openolat.org">
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
import org.olat.core.gui.components.form.flexible.impl.elements.FormToggleComponent;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * Controller having a subscribe / unsubscribe button for notifications depending on users state
 * 
 * @author Felix Jost
 */
public class ContextualSubscriptionController extends BasicController {

	private final VelocityContainer myContent;
	private FormToggleComponent subscribeButton;
	
	private final boolean optOut;
	private final PublisherData publisherData;
	private SubscriptionContext subscriptionContext;
	
	@Autowired
	private NotificationsManager notifManager;
	
	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param subscriptionContext The subscription context (which resource is involved)
	 * @param publisherData The publisher data
	 */
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl,
			SubscriptionContext subscriptionContext, PublisherData publisherData) {
		this(ureq, wControl, subscriptionContext, publisherData, false);
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param subscriptionContext  The subscription context (which resource is involved)
	 * @param publisherData The publisher data
	 * @param optOut true will automatically subscribed the user if it has not previously opt-out
	 */
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl,
			SubscriptionContext subscriptionContext, PublisherData publisherData, boolean optOut) {
		super(ureq, wControl);
		this.optOut = optOut;
		this.subscriptionContext = subscriptionContext;
		this.publisherData = publisherData;
		myContent = createVelocityContainer("consubs");
		myContent.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		if (subscriptionContext == null) {
			putInitialPanel(new Panel("empty:nosubscription"));
			return;
		}

		subscribeButton = LinkFactory.createToggle("command.subscribe", translate("on"), translate("off"),  myContent, this);
		subscribeButton.setElementCssClass("o_noti_subscribe_link");
		subscribeButton.setAriaLabelledBy("o_sub_label");
		
		loadModel();
		putInitialPanel(myContent);
	}
	
	public boolean isSubscribed() {
		return subscribeButton != null && subscribeButton.isOn();
	}
	
	public void loadModel() {
		// if subscribed, offer a unsubscribe button and vice versa.
		Subscriber subscriber = notifManager.getSubscriber(getIdentity(), subscriptionContext);
		if(subscriber == null && optOut) {
			notifManager.subscribe(getIdentity(), subscriptionContext, publisherData);
			subscribeButton.toggleOn();
		} else if(subscriber == null || !subscriber.isEnabled()) {
			subscribeButton.toggleOff();
		} else {
			subscribeButton.toggleOn();
		}
		updateUI();
	}

	private void updateUI() {
		myContent.contextPut("subscribed", Boolean.valueOf(isSubscribed()));
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == subscribeButton) {
			if(subscribeButton.isOn()) {
				notifManager.subscribe(getIdentity(), subscriptionContext, publisherData);
			} else {
				notifManager.unsubscribe(getIdentity(), subscriptionContext);
			}
			updateUI();
			fireEvent(ureq, event);
		}
	}
}