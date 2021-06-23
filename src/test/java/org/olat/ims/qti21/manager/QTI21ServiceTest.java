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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qti21Service;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	
	@Test
	public void isRunningAssessmentTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("service-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasRunningTestSessions = qti21Service.isRunningAssessmentTestSession(courseEntry, subIdent, testEntry, Collections.singletonList(assessedIdentity));
		Assert.assertTrue(hasRunningTestSessions);
		
		// check more than 512
		List<IdentityRef> refs = new ArrayList<>();
		refs.add(assessedIdentity);
		for(int i=0; i<1024; i++) {
			refs.add(new IdentityRefImpl(Long.valueOf(i++)));
		}
		boolean hasLotOfRunningTestSessions = qti21Service.isRunningAssessmentTestSession(courseEntry, subIdent, testEntry, refs);
		Assert.assertTrue(hasLotOfRunningTestSessions);
		
		// negative
		List<IdentityRef> negatifRefs = new ArrayList<>();
		for(int i=0; i<1024; i++) {
			negatifRefs.add(new IdentityRefImpl(Long.valueOf(-(i++))));
		}
		boolean noRunningTestSessions = qti21Service.isRunningAssessmentTestSession(courseEntry, subIdent, testEntry, negatifRefs);
		Assert.assertFalse(noRunningTestSessions);
	}
}
