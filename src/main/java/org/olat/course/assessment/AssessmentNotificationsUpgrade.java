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

package org.olat.course.assessment;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * Upgrade publisher of assessments
 * 
 * <P>
 * Initial Date:  5 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class AssessmentNotificationsUpgrade implements NotificationsUpgrade {
	private static final OLog log = Tracing.createLoggerFor(AssessmentNotificationsUpgrade.class);

	@Override
	public Publisher ugrade(Publisher publisher) {
		String businessPath = publisher.getBusinessPath();
		if(businessPath != null && businessPath.startsWith("[")) return null;
		
		try {
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(CourseModule.class, publisher.getResId()), true);
			businessPath = "[RepositoryEntry:" + re.getKey() + "]";
		} catch (Exception e) {
			//if something went wrong...
			log.error("error while processing resid: "+publisher.getResId(), e);
		}
	
		if(businessPath != null) {
			publisher.setBusinessPath(businessPath);
			return publisher;
		}
		return null;
	}

	@Override
	public String getType() {
		return "AssessmentManager";
	}
}
