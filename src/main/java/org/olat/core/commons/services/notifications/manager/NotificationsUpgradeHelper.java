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

package org.olat.core.commons.services.notifications.manager;

import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 jan. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class NotificationsUpgradeHelper {

	public static boolean checkOLATResourceable(Publisher publisher) {
		Long resId = publisher.getResId();
		try {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(publisher.getResName(), resId);
			if(ores == null) return false;
			Long reKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(ores, true);
			return reKey != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean checkCourse(Publisher publisher) {
		Long resId = publisher.getResId();
		try {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, resId);
			if(ores == null) return false;
			Long reKey = RepositoryManager.getInstance().lookupRepositoryEntryKey(ores, true);
			return reKey != null;
		} catch (Exception e) {
			return false;
		}
	}
}
