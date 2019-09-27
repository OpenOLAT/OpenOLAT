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
package org.olat.core.commons.services.notifications.manager;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br>
 * job that sends notification emails wrapped in DBjob to close DB sessions properly
 * 
 * <P>
 * Initial Date:  09.09.2008 <br>
 * @author guido
 */
@DisallowConcurrentExecution
public class EmailNotificationJob extends JobWithDB {

	
	/**
	 * 
	 * @see org.olat.core.commons.services.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		NotificationsManager.getInstance().notifyAllSubscribersByEmail();
	}

}
