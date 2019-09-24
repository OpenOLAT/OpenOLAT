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
*/
package org.olat.core.commons.services.notifications;

import java.util.Date;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.notifications.ui.NotificationNewsController;
import org.olat.core.commons.services.notifications.ui.NotificationSubscriptionAndNewsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
public class NotificationUIFactory {

	/**
	 * Create a controller that displays the users news from his subscriptions
	 * 
	 * @param subscriberIdentity
	 * @param ureq
	 * @param windowControl
	 * @param newsSinceDate optional date that represents the lower boundary of
	 *          the news period or NULL to use the default value
	 * @return
	 */
	public static NotificationNewsController createNewsListingController(Identity subscriberIdentity, UserRequest ureq,
			WindowControl windowControl, Date newsSinceDate) {
		return new NotificationNewsController(subscriberIdentity, ureq, windowControl, newsSinceDate);
	}

	/**
	 * Create a controller which shows the users subscriptions and the generated
	 * news since the given date in one view
	 * 
	 * @param subscriberIdentity
	 * @param ureq
	 * @param windowControl
	 * @param newsSinceDate optional date that represents the lower boundary of
	 *          the news period or NULL to use the default value
	 * @return
	 */
	public static NotificationSubscriptionAndNewsController createCombinedSubscriptionsAndNewsController(Identity subscriberIdentity,
			UserRequest ureq, WindowControl windowControl, Date newsSinceDate) {
		return new NotificationSubscriptionAndNewsController(subscriberIdentity, ureq, windowControl, newsSinceDate);
	}

	/**
	 * Create a controller which shows the users subscriptions and the generated
	 * news in one view that has been accumulated during the configured
	 * notification period
	 * 
	 * @param subscriberIdentity
	 * @param ureq
	 * @param windowControl
	 * @return
	 */
	public static NotificationSubscriptionAndNewsController createCombinedSubscriptionsAndNewsController(Identity subscriberIdentity,
			UserRequest ureq, WindowControl windowControl) {
		NotificationsManager notiMgr = NotificationsManager.getInstance();
		// default use the interval
		Date compareDate = notiMgr.getCompareDateFromInterval(notiMgr.getUserIntervalOrDefault(ureq.getIdentity()));
		return new NotificationSubscriptionAndNewsController(subscriberIdentity, ureq, windowControl, compareDate);
	}

	/**
	 * launch a subscription resource in its tab
	 * @param ureq
	 * @param windowControl
	 * @param sub
	 */
	public static void launchSubscriptionResource(UserRequest ureq, WindowControl windowControl, Subscriber sub) {
		Publisher pub = sub.getPublisher();
		if (!NotificationsManager.getInstance().isPublisherValid(pub)) {
			Translator trans = Util.createPackageTranslator(NotificationUIFactory.class, ureq.getLocale());
			windowControl.setError(trans.translate("error.publisherdeleted"));
			return;
		}
		if("Inbox".equals(pub.getResName())) {
			String businessPath = "[HomeSite:" + ureq.getIdentity().getKey() + "][Inbox:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, windowControl);
			return;
		} else if(StringHelper.containsNonWhitespace(pub.getBusinessPath())) {
			NewControllerFactory.getInstance().launch(pub.getBusinessPath(), ureq, windowControl);
			return;
		}
	}
}
