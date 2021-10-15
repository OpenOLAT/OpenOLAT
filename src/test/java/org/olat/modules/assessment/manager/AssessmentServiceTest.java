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
package org.olat.modules.assessment.manager;

import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentServiceTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	
	@Test
	public void getOrCreateAssessmentEntryOnHighLoad() throws URISyntaxException {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();

		//make statements
	   
		int NUM_OF_THREADS = 20;

		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<GetOrCreateAssessmentEntryThread> threads = new ArrayList<>(NUM_OF_THREADS);
		for(int i=0; i<NUM_OF_THREADS; i++) {
			threads.add(new GetOrCreateAssessmentEntryThread(assessedIdentity, entry, finishCount));
		}
		
		// remove the participants
		for(GetOrCreateAssessmentEntryThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
			Assert.fail();
		}

		for(GetOrCreateAssessmentEntryThread thread:threads) {
			assertTrue("Cannot properly get or create the assessment entry", thread.isOk());
		}

		dbInstance.commitAndCloseSession();
	}
	
	private class GetOrCreateAssessmentEntryThread extends Thread {
		
		private final Identity participant;
		private final RepositoryEntry re;
		private final CountDownLatch finishCount;
		private boolean ok = false;
		
		public GetOrCreateAssessmentEntryThread(Identity participant, RepositoryEntry re, CountDownLatch finishCount) {
			this.finishCount = finishCount;
			this.participant = participant;
			this.re = re;
		}
		
		public boolean isOk() {
			return ok;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(10);
				AssessmentEntry nodeAssessment = assessmentService.getOrCreateAssessmentEntry(participant, null, re, "39485349775", Boolean.TRUE, re);
				dbInstance.commitAndCloseSession();
				ok = nodeAssessment != null;
			} catch (InterruptedException e) {
				log.error("", e);
			} finally {
				finishCount.countDown();
			}
		}
	}

}
