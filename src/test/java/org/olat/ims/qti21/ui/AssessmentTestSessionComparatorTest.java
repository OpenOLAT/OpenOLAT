package org.olat.ims.qti21.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestSessionComparatorTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	
	@Test
	public void createTestSession_repo() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("sort-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", null, testEntry);
		
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, 300, true);
		testSession1.setFinishTime(DateUtils.addDays(new Date(), -1));
		testSession1 = testSessionDao.update(testSession1);
		
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, 300, true);
		testSession2.setFinishTime(DateUtils.addDays(new Date(), -3));
		testSession2 = testSessionDao.update(testSession2);
		
		AssessmentTestSession testSession3 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, 300, true);
		testSession3.setTerminationTime(DateUtils.addDays(new Date(), -2));
		testSession3 = testSessionDao.update(testSession3);
		
		AssessmentTestSession lastTestSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, 300, true);
		dbInstance.commit();
		
		List<AssessmentTestSession> sessions = new ArrayList<>();
		sessions.add(testSession1);
		sessions.add(testSession2);
		sessions.add(testSession3);
		sessions.add(lastTestSession);
		
		Collections.sort(sessions, new AssessmentTestSessionComparator());
		
		Assert.assertEquals(lastTestSession, sessions.get(0));
		Assert.assertEquals(testSession1, sessions.get(1));
		Assert.assertEquals(testSession3, sessions.get(2));
		Assert.assertEquals(testSession2, sessions.get(3));
	}
}
