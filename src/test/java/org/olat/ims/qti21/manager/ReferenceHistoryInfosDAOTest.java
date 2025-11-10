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
package org.olat.ims.qti21.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ReferenceHistoryWithInfos;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.references.ReferenceHistory;
import org.olat.resource.references.ReferenceHistoryDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReferenceHistoryInfosDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private ReferenceHistoryDAO referenceHistoryDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private ReferenceHistoryInfosDAO referenceHistoryInfosDAO;
	
	
	@Test
	public void createReferenceHistory() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-1");
		// prepare a test and a user
		String subIdent = "ref-history-qti-21";
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, testEntry, false);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 300, false);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
		
		ReferenceHistory referenceHistory = referenceHistoryDao.addReferenceHistory(courseEntry.getOlatResource(), testEntry.getOlatResource(), subIdent, assessedIdentity);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(referenceHistory);
		
		List<ReferenceHistoryWithInfos> infosList = referenceHistoryInfosDAO.getReferenceHistoryWithInfos(courseEntry, subIdent);
		Assert.assertEquals(1, infosList.size());
		
		ReferenceHistoryWithInfos infos = infosList.get(0);
		Assert.assertEquals(testEntry, infos.testEntry());
		Assert.assertEquals(assessedIdentity, infos.doer());
		Assert.assertEquals(1, infos.runs());
	}

}
