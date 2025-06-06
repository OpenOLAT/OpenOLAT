/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.logging.activity;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ActivityLogServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ActivityLogService activityLogService;
	
	@Test
	public void log() {
		RepositoryEntry test = JunitTestHelper.createAndPersistRepositoryEntry();
		
		ILoggingAction loggingAction = QTI21LoggingAction.QTI_START_IN_COURSE;
		ActionType actionType = loggingAction.getResourceActionType();

		Long identityKey = 1l;
		Class<?> callingClass = ActivityLogServiceTest.class;
		String businessPath = "[RepositoryEntry:" + test.getKey() + "]";
		List<ILoggingResourceable> loggingResourceableList = new ArrayList<>();
		loggingResourceableList.add(LoggingResourceable.wrapTest(test));
		
		String sessionId = activityLogService.getSessionId(null);
		LoggingObject logObj = activityLogService.log(loggingAction, actionType, sessionId, identityKey, callingClass,
				false, businessPath,  List.of(), loggingResourceableList);
		dbInstance.commit();
		
		Assert.assertNotNull(logObj);
		Assert.assertNotNull(logObj.getSessionId());
		Assert.assertEquals(businessPath, logObj.getBusinessPath());
		Assert.assertEquals(ActionObject.test.name(), logObj.getActionObject());
		Assert.assertEquals(ActionVerb.launch.name(), logObj.getActionVerb());
		Assert.assertEquals(test.getOlatResource().getResourceableId().toString(), logObj.getTargetResId());
		Assert.assertEquals(test.getOlatResource().getResourceableTypeName(), logObj.getTargetResName());
		Assert.assertEquals(test.getOlatResource().getResourceableTypeName(), logObj.getTargetResType());
	}
}
