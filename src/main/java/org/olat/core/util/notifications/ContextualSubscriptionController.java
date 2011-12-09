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

package org.olat.core.util.notifications;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Description: <br>
 * Controller having a subscribe / unsubscribe button for notifications depending on users state
 * 
 * @author Felix Jost
 */
public class ContextualSubscriptionController extends BasicController {

	private VelocityContainer myContent;
	private Link subscribeButton;
	private Link unsubscribeButton;
	private Panel allPanel;
	private Panel detailsPanel;
	private NotificationsManager notifManager;
	private SubscriptionContext subscriptionContext;
	private boolean isSubscribed;
	private final PublisherData publisherData;
	
	/**
	 * @param ureq
	 * @param subscriptionContext
	 * @param publisherData
	 */
	public ContextualSubscriptionController(UserRequest ureq, WindowControl wControl, SubscriptionContext subscriptionContext, PublisherData publisherData) {
		super(ureq, wControl);
		this.subscriptionContext = subscriptionContext;
		this.publisherData = publisherData;
		myContent = createVelocityContainer("consubs");
		
		if (subscriptionContext == null) {
			putInitialPanel(new Panel("empty:nosubscription"));
			return;
		}
		
		detailsPanel = new Panel("subscription_detail");
		allPanel = new Panel("subscription_all");

		subscribeButton = LinkFactory.createButtonSmall("command.subscribe", myContent, this);
		subscribeButton.setCustomEnabledLinkCSS("b_noti_subscribe_link");
		
		unsubscribeButton = LinkFactory.createButtonSmall("command.unsubscribe", myContent, this);
		unsubscribeButton.setCustomEnabledLinkCSS("b_noti_unsubscribe_link");
		
		notifManager = NotificationsManager.getInstance();
		// if subscribed, offer a unsubscribe button and vica versa.
		isSubscribed = notifManager.isSubscribed(ureq.getIdentity(), subscriptionContext);

		updateUI();
		myContent.put("detailsPanel", detailsPanel);
		allPanel.setContent(myContent);
		putInitialPanel(allPanel);
	}
	
	public boolean isSubscribed() {
		return isSubscribed;
	}

	private void updateUI() {
		myContent.contextPut("subscribed", (isSubscribed ? Boolean.TRUE : Boolean.FALSE));
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == subscribeButton) {
			notifManager.subscribe(ureq.getIdentity(), subscriptionContext, publisherData);
			isSubscribed = true;
			updateUI();
			fireEvent(ureq, event);
		} else if (source == unsubscribeButton) {
			notifManager.unsubscribe(ureq.getIdentity(), subscriptionContext);
			isSubscribed = false;
			updateUI();
			fireEvent(ureq, event);
		} 
	}

	@Override
	protected void doDispose() {
		// nothing to do
	}

}