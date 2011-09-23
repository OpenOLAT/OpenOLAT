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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package de.bps.course.nodes.den;

import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.notifications.NotificationsUpgradeHelper;

/**
 * 
 * Description:<br>
 * Upgrade publisher of DEN
 * 
 * <P>
 * Initial Date:  7 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class DENCourseNodeNotificationsUpgrade implements NotificationsUpgrade {

	@Override
	public Publisher ugrade(Publisher publisher) {
		String businessPath = publisher.getBusinessPath();
		if(businessPath != null && businessPath.startsWith("[")) return null;// already upgrade
		
		String type = publisher.getResName();
		if ("CourseModule".equals(type)) {
			businessPath = NotificationsUpgradeHelper.getCourseNodePath(publisher);
		}
	
		if(businessPath != null) {
			publisher.setBusinessPath(businessPath);
			return publisher;
		}
		return null;
	}

	@Override
	public String getType() {
		return "DENCourseNode";
	}
}