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

package org.olat.core.commons.services.notifications;

import java.util.Date;
import java.util.Locale;


/**
 * Description:<br>
 * NotificationsHandler is stateless and represents a starting point for all subscription activities for a given type, see NotificationsManager.java
 * 
 * <P>
 * Initial Date:  25.10.2004 <br>
 *
 * @author Felix Jost
 */
public interface NotificationsHandler {

	/**
	 * pre: the publisher of the subscriber param must be valid!
	 * 
	 * @param subscriber
	 * @param locale
	 * @param compareDate, means the subscriptioninfo should be calcaluted using this date. 
	 * This is used to determine if there have been news since the given date. 
	 * @return the specificNotificationData (never null, but can be a nosubscriptioninfo)
	 */
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate);

	
	/**
	 * get specific titleInfo for this kind of notification
	 * a generic approach won't work, so collect info with handlers.
	 * @param subscriber
	 * @param locale
	 * @return 
	 */
	public String createTitleInfo(Subscriber subscriber, Locale locale);

  /**
   * Return handler type e.g. Forum, AssessmentManager, FolderModule etc.
   * @return
   */
	public String getType();
}

