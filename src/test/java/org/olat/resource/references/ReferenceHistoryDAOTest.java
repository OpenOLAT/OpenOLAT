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
package org.olat.resource.references;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReferenceHistoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ReferenceHistoryDAO referenceHistoryDao;
	
	
	@Test
	public void createReferenceHistory() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("rh-");
		OLATResource source = JunitTestHelper.createRandomResource();
		OLATResource target = JunitTestHelper.createRandomResource();
		
		ReferenceHistory referenceHistory = referenceHistoryDao.addReferenceHistory(source, target, "sub-ident", actor);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(referenceHistory);
		Assert.assertNotNull(referenceHistory.getKey());
		Assert.assertNotNull(referenceHistory.getCreationDate());
		Assert.assertEquals(source, referenceHistory.getSource());
		Assert.assertEquals(target, referenceHistory.getTarget());
		Assert.assertEquals(actor, referenceHistory.getIdentity());
	}
	
	@Test
	public void loadHistory() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("rh-");
		OLATResource source = JunitTestHelper.createRandomResource();
		OLATResource target = JunitTestHelper.createRandomResource();
		
		ReferenceHistory referenceHistory = referenceHistoryDao.addReferenceHistory(source, target, "sub-identifier", actor);
		dbInstance.commitAndCloseSession();
		
		List<ReferenceHistory> history = referenceHistoryDao.loadHistory(source, "sub-identifier");
		Assertions.assertThat(history)
			.hasSize(1)
			.containsExactly(referenceHistory);
	}
	
	@Test
	public void deleteHistory() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("rh-");
		OLATResource source = JunitTestHelper.createRandomResource();
		OLATResource target = JunitTestHelper.createRandomResource();
		
		ReferenceHistory referenceHistory = referenceHistoryDao.addReferenceHistory(source, target, "sub-deleted-identifier", actor);
		dbInstance.commitAndCloseSession();
		
		// Check
		List<ReferenceHistory> history = referenceHistoryDao.loadHistory(source, "sub-deleted-identifier");
		Assertions.assertThat(history)
			.hasSize(1);
		
		referenceHistoryDao.delete(referenceHistory);
		dbInstance.commitAndCloseSession();
		
		List<ReferenceHistory> deletedHistory = referenceHistoryDao.loadHistory(source, "sub-deleted-identifier");
		Assertions.assertThat(deletedHistory)
			.isEmpty();
	}
	
	@Test
	public void deleteAllHistory() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("rh-");
		OLATResource source = JunitTestHelper.createRandomResource();
		OLATResource target = JunitTestHelper.createRandomResource();
		
		ReferenceHistory referenceHistory = referenceHistoryDao.addReferenceHistory(source, target, "sub-deleted-identifier", actor);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(referenceHistory);
		
		// Check
		List<ReferenceHistory> history = referenceHistoryDao.loadHistory(source, "sub-deleted-identifier");
		Assertions.assertThat(history)
			.hasSize(1);
		
		referenceHistoryDao.deleteAllReferencesHistoryOf(source);
		dbInstance.commitAndCloseSession();
		
		List<ReferenceHistory> deletedHistory = referenceHistoryDao.loadHistory(source, null);
		Assertions.assertThat(deletedHistory)
			.isEmpty();
	}
}
