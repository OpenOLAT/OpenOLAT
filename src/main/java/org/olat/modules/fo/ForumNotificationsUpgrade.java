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

package org.olat.modules.fo;

import org.olat.core.logging.LogDelegator;
import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.notifications.NotificationsUpgradeHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Description:<br>
 * Upgrade publisher of forums
 * 
 * <P>
 * Initial Date:  5 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ForumNotificationsUpgrade extends LogDelegator implements NotificationsUpgrade {

	@Override
	public Publisher ugrade(Publisher publisher) {
		String businessPath = publisher.getBusinessPath();
		if(businessPath != null && businessPath.startsWith("[")) return null;// already upgrade
		
		String type = publisher.getResName();
		if("BusinessGroup".equals(type)) {
			businessPath = "[BusinessGroup:" + publisher.getResId()+ "][toolforum:0]";
		} else if ("CourseModule".equals(type)) {
			String courseNode = publisher.getSubidentifier();
			if(courseNode.indexOf(':') < 0) {
				businessPath = NotificationsUpgradeHelper.getCourseNodePath(publisher);
			} else {
				try {
					String courseNodeId = courseNode.substring(0, courseNode.indexOf(':'));
					Long resId = publisher.getResId();
					ICourse course = CourseFactory.loadCourse(resId);
					RepositoryManager rm = RepositoryManager.getInstance();
					OLATResource rsrc = OLATResourceManager.getInstance().findResourceable(course.getResourceableId(), course.getResourceableTypeName());
					RepositoryEntry re = rm.lookupRepositoryEntry(rsrc, true);
					//node forum
					businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + courseNodeId + "]";
				} catch (Exception e) {
					businessPath = null;
					//if something went wrong, like error while loading course...
					logWarn("error while processing resid: "+publisher.getResId(), e);
					
				}
			}
			//no notification for forums in wiki
		}
	
		if(businessPath != null) {
			publisher.setBusinessPath(businessPath);
			return publisher;
		}
		return null;
	}

	@Override
	public String getType() {
		return "Forum";
	}
}
