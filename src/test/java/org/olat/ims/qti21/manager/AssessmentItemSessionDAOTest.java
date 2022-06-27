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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemSessionDAOTest extends OlatTestCase {
	

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentService assessmentService;
	
	@Test
	public void createAndGetItemSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("response-session-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", null, testEntry);
		dbInstance.commit();
		
		String itemIdentifier = UUID.randomUUID().toString();
		ParentPartItemRefs parentParts = new ParentPartItemRefs();
		String sectionIdentifier = UUID.randomUUID().toString();
		parentParts.setSectionIdentifier(sectionIdentifier);
		String testPartIdentifier = UUID.randomUUID().toString();
		parentParts.setTestPartIdentifier(testPartIdentifier);
		
		//make test, item and response
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "_", assessmentEntry, assessedIdentity, null, null, true);
		Assert.assertNotNull(testSession);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, parentParts, itemIdentifier, null);
		Assert.assertNotNull(itemSession);
		dbInstance.commitAndCloseSession();
		
		AssessmentItemSession reloadedItemSession = itemSessionDao.getAssessmentItemSession(testSession, itemIdentifier);
		Assert.assertNotNull(reloadedItemSession);
		Assert.assertNotNull(reloadedItemSession.getCreationDate());
		Assert.assertNotNull(reloadedItemSession.getLastModified());
		Assert.assertEquals(itemIdentifier, reloadedItemSession.getAssessmentItemIdentifier());
		Assert.assertEquals(sectionIdentifier, reloadedItemSession.getSectionIdentifier());
		Assert.assertEquals(testPartIdentifier, reloadedItemSession.getTestPartIdentifier());
	}
}
