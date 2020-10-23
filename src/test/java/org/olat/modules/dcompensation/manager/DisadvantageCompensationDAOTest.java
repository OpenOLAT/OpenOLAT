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
package org.olat.modules.dcompensation.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DisadvantageCompensationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private DisadvantageCompensationDAO disadvantageCompensationDao;
	
	
	@Test
	public void createDisadvantageCompensation() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-1");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -5);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "By me", approval, creator, entry, subIdent, "Test");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(compensation.getKey());
		Assert.assertNotNull(compensation.getCreationDate());
		Assert.assertNotNull(compensation.getLastModified());
		
		Assert.assertEquals(Integer.valueOf(15), compensation.getExtraTime());
		Assert.assertEquals("By me", compensation.getApprovedBy());
		Assert.assertEquals(identity, compensation.getIdentity());
		Assert.assertEquals(creator, compensation.getCreator());
		Assert.assertEquals(subIdent, compensation.getSubIdent());
		Assert.assertEquals("Test", compensation.getSubIdentName());
		Assert.assertEquals(entry, compensation.getEntry());
	}
	
	@Test
	public void getDisadvantageCompensationsByIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-3");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "The chef", approval, creator, entry, subIdent, "Long test");
		dbInstance.commitAndCloseSession();
		
		List<DisadvantageCompensation> compensations = disadvantageCompensationDao
				.getDisadvantageCompensations(identity);
		Assert.assertNotNull(compensations);
		Assert.assertEquals(1, compensations.size());
		Assert.assertEquals(compensation, compensations.get(0));
	}
	
	@Test
	public void getActiveDisadvantageCompensationsByEntry() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-5");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "Big boss say", approval, creator, entry, subIdent, "Exam");
		dbInstance.commitAndCloseSession();
		
		List<DisadvantageCompensation> compensations = disadvantageCompensationDao
				.getActiveDisadvantageCompensations(entry, subIdent);
		Assert.assertNotNull(compensations);
		Assert.assertEquals(1, compensations.size());
		Assert.assertEquals(compensation, compensations.get(0));
	}
	
	@Test
	public void getActiveDisadvantageCompensationsByIdentityAndEntry() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-7");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-8");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "Not responsible", approval, creator, entry, subIdent, "Element");
		dbInstance.commitAndCloseSession();
		
		DisadvantageCompensation activeCompensation = disadvantageCompensationDao
				.getActiveDisadvantageCompensation(identity, entry, subIdent);
		Assert.assertNotNull(activeCompensation);
		Assert.assertEquals(compensation, activeCompensation);
	}
	
	@Test
	public void getActiveDisadvantagedUsers() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-11");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-12");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-12");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = UUID.randomUUID().toString();
		String subIdent2 = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation1 = disadvantageCompensationDao
				.createDisadvantageCompensation(identity1, 15, "Not responsible", approval, creator, entry, subIdent1, "Element-1");
		DisadvantageCompensation compensation2 = disadvantageCompensationDao
				.createDisadvantageCompensation(identity2, 15, "Not responsible", approval, creator, entry, subIdent2, "Element-2");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(compensation1);
		Assert.assertNotNull(compensation2);
		
		List<String> subIdents = List.of(subIdent1,  subIdent2);
		List<IdentityRef> disadvantegdIdentities = disadvantageCompensationDao
				.getActiveDisadvantagedUsers(entry, subIdents);
		assertThat(disadvantegdIdentities)
			.isNotNull()
			.hasSize(2)
			.extracting(IdentityRef::getKey)
			.containsExactlyInAnyOrder(identity1.getKey(), identity2.getKey());
	}
	
	@Test
	public void isActiveDisadvantagedUser() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-16");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-18");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "Not responsible", approval, creator, entry, subIdent, "Element-1");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(compensation);
		
		boolean disadvantegdCompensations = disadvantageCompensationDao
				.isActiveDisadvantagedUser(identity, entry, List.of(subIdent));
		Assert.assertTrue(disadvantegdCompensations);
		
		boolean creatorCompensations = disadvantageCompensationDao
				.isActiveDisadvantagedUser(creator, entry, List.of(subIdent));
		Assert.assertFalse(creatorCompensations);
	}
	
	@Test
	public void getDisadvantageCompensationsByIdentityAndEntry() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-9");
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("dcompensation-10");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Date approval = DateUtils.addDays(new Date(), -21);
		
		DisadvantageCompensation compensation1 = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "Not responsible 1", approval, creator, entry, subIdent, "Element");
		DisadvantageCompensation compensation2 = disadvantageCompensationDao
				.createDisadvantageCompensation(identity, 15, "Not responsible 2", approval, creator, entry, subIdent, "Element");
		dbInstance.commitAndCloseSession();
		
		List<DisadvantageCompensation> allCompensations = disadvantageCompensationDao
				.getDisadvantageCompensations(identity, entry, subIdent);
		Assert.assertNotNull(allCompensations);
		Assert.assertEquals(2, allCompensations.size());
		assertThat(allCompensations)
			.containsExactlyInAnyOrder(compensation1, compensation2);
	}
	

}
