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
package org.olat.instantMessaging;

import static org.olat.instantMessaging.InstantMessagingModule.CONFIG_SYNCED_LEARNING_GROUPS;
import static org.olat.instantMessaging.InstantMessagingModule.createPropertyName;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.scheduler.JobWithDB;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.quartz.JobExecutionContext;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for InstantMessagingJob
 * 
 * <P>
 * Initial Date:  14 juil. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.fentix.com
 */

//fxdiff: FXOLAT-219 decrease the load for synching groups
public class InstantMessagingJob extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext job) {
		if(!InstantMessagingModule.isEnabled()) return;
		
		final InstantMessaging im = InstantMessagingModule.getAdapter();
		final PropertyManager propertyManager = PropertyManager.getInstance();
		final DB database = DBFactory.getInstance();

		boolean success = false;
		try {
			List<Property> props = propertyManager.findProperties(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_LEARNING_GROUPS));
			if (props.size() == 0 || !Boolean.getBoolean(props.get(0).getStringValue())) {
				if (InstantMessagingModule.isSyncGroups()) {
					long start = System.currentTimeMillis();
					log.info("Start synching learning groups with IM");
					boolean result = im.synchronizeBusinessGroupsWithIMServer();
					Property property = propertyManager.createPropertyInstance(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_LEARNING_GROUPS), null,  null, Boolean.toString(result), null);
					propertyManager.saveProperty(property);
					log.info("Synching learning groups with IM terminated in " + (System.currentTimeMillis() - start) + " (ms)");
				}
			}
			database.commitAndCloseSession();
			success = true;
		} finally {
			if (!success) {
				database.rollbackAndCloseSession();
			}
		}
	}
}
