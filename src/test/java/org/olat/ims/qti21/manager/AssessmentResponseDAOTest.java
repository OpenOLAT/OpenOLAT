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
package org.olat.ims.qti21.manager;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;

/**
 * 
 * Initial date: 29.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentResponseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentResponseDAO responseDao;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentService assessmentService;
	
	@Test
	public void createResponse() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("response-session-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", null, testEntry);
		dbInstance.commit();
		
		String itemIdentifier = UUID.randomUUID().toString();
		String responseIdentifier = UUID.randomUUID().toString();
		
		//make test, item and response
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "_", assessmentEntry, assessedIdentity, null, null, true);
		Assert.assertNotNull(testSession);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, null, itemIdentifier, null);
		Assert.assertNotNull(itemSession);
		AssessmentResponse response = responseDao.createAssessmentResponse(testSession, itemSession, responseIdentifier, ResponseLegality.VALID, ResponseDataType.FILE);
		Assert.assertNotNull(response);
		response.setStringuifiedResponse("Hello QTI 2.1");
		responseDao.save(Collections.singletonList(response));
		dbInstance.commit();
	}
	
	@Test
	public void loadResponse_itemSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("response-session-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", Boolean.FALSE, testEntry);
		dbInstance.commit();

		//make test, item and response
		String itemIdentifier = UUID.randomUUID().toString();
		String responseIdentifier = UUID.randomUUID().toString();
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "_", assessmentEntry, assessedIdentity, null, null, true);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, null, itemIdentifier, null);
		AssessmentResponse response = responseDao.createAssessmentResponse(testSession, itemSession, responseIdentifier, ResponseLegality.VALID, ResponseDataType.FILE);
		response.setStringuifiedResponse("Hello QTI 2.1");
		responseDao.save(Collections.singletonList(response));
		dbInstance.commit();
		
		List<AssessmentResponse> loadedResponses = responseDao.getResponses(itemSession);
		Assert.assertNotNull(loadedResponses);
		Assert.assertEquals(1, loadedResponses.size());
		Assert.assertEquals(response, loadedResponses.get(0));
	}
	
	@Test
	public void loadResponse_testSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("response-session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", Boolean.FALSE, testEntry);
		dbInstance.commit();

		//make test, item and response
		String itemIdentifier = UUID.randomUUID().toString();
		String responseIdentifier = UUID.randomUUID().toString();
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "_", assessmentEntry, assessedIdentity, null, null, true);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, null, itemIdentifier, null);
		AssessmentResponse response = responseDao.createAssessmentResponse(testSession, itemSession, responseIdentifier, ResponseLegality.VALID, ResponseDataType.FILE);
		response.setStringuifiedResponse("Hello QTI 2.1");
		responseDao.save(Collections.singletonList(response));
		dbInstance.commit();
		
		List<AssessmentResponse> loadedResponses = responseDao.getResponses(testSession);
		Assert.assertNotNull(loadedResponses);
		Assert.assertEquals(1, loadedResponses.size());
		Assert.assertEquals(response, loadedResponses.get(0));
	}
}
