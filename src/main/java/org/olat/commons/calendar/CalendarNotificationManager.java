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
package org.olat.commons.calendar;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface CalendarNotificationManager {
	

	public SubscriptionContext getSubscriptionContext(KalendarRenderWrapper courseCalendar);
	
	public SubscriptionContext getSubscriptionContext(KalendarRenderWrapper courseCalendar, OLATResourceable res);
	
	
	public BusinessGroup getBusinessGroup(KalendarRenderWrapper wrapper);

}
