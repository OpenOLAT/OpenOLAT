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
package org.olat.course.nodes.livestream.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.activity.LoggingObject;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamStatisticDAOTest extends OlatTestCase {
	
	@Autowired
	private LiveStreamStatisticDAO sut;
	@Autowired
	private DB dbInstance;
	
	@Test
	public void shouldGetViewers() {
		Long userKey1 = 132L;
		Long userKey2 = 1324L;
		Long userKeyOther = 13245L;
		String courseResId = "courseResId";
		String nodeIdent = "nodeIdent";
		Date before = new GregorianCalendar(2010, 2, 8).getTime();
		Date from = new GregorianCalendar(2010, 2, 9).getTime();
		Date inside = new GregorianCalendar(2010, 2, 10).getTime();
		Date to = new GregorianCalendar(2010, 2, 11).getTime();
		Date after = new GregorianCalendar(2010, 2, 12).getTime();
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKey1, inside);
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKey1, inside);
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKey1, inside);
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKey2, inside);
		// These log entries should have all wrong parameters. So userKeyOther should not be a viewer.
		createLoggingObject("OTHER", courseResId, "livestream", nodeIdent, userKeyOther, inside);
		createLoggingObject("launch", "OTHER", "livestream", nodeIdent, userKeyOther, inside);
		createLoggingObject("launch", courseResId, "OTHER", nodeIdent, userKeyOther, inside);
		createLoggingObject("launch", courseResId, "livestream", "OTHER", userKeyOther, inside);
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKeyOther, before);
		createLoggingObject("launch", courseResId, "livestream", nodeIdent, userKeyOther, after);
		dbInstance.commitAndCloseSession();
		
		Long viewers = sut.getViewers(courseResId, nodeIdent, from, to);
		
		assertThat(viewers).isEqualTo(2);
	}

	private void createLoggingObject(String actionVerb, String parentResId, String targetResType, String targetResId,
			Long identityKey, Date creationDate) {
		LoggingObject logObj = new LoggingObject(random(), identityKey, "r", actionVerb, "node");
		logObj.setCreationDate(creationDate);
		logObj.setParentResId(parentResId);
		logObj.setTargetResType(targetResType);
		logObj.setTargetResId(targetResId);
		logObj.setResourceAdminAction(Boolean.TRUE);
		dbInstance.saveObject(logObj);
	}


}
