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
package org.olat.modules.immunityproof;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.immunityproof.manager.ImmunityProofDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 16.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofDaoTest extends OlatTestCase {
	
	private Identity id1;
	private Identity id2;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ImmunityProofDAO immunityProofDAO;
	
	@Before
	public void setUpTestCase() {
		Random random = new Random();
		id1 = JunitTestHelper.createAndPersistIdentityAsUser(String.valueOf(random.nextInt()));
		id2 = JunitTestHelper.createAndPersistIdentityAsUser(String.valueOf(random.nextInt()));
	}
	
	@Test
	public void shouldCreateImmunityProof() {
		ImmunityProof proof = immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		
		dbInstance.commitAndCloseSession();
		
		assertTrue(proof.getKey() != null);				
	}
	
	@Test
	public void shouldGetProofByIdentity() {
		immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		ImmunityProof loadedProof1 = immunityProofDAO.getImmunitiyProof(id1);
		ImmunityProof loadedProof2 = immunityProofDAO.getImmunitiyProof(id2);
		
		assertTrue(loadedProof1 != null);		
		assertTrue(loadedProof2 == null);		
	}
	
	@Test
	public void shouldGetAll() {
		immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		immunityProofDAO.createImmunityProof(id2, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		List<ImmunityProof> proofs = immunityProofDAO.getAllCertificates();
		
		assertTrue(proofs.size() >= 2);
	}
	
	@Test
	public void shouldGetCount() {
		immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		immunityProofDAO.createImmunityProof(id2, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		long count = immunityProofDAO.getCount();
		
		assertTrue(count >= 2);
	}
	
	@Test
	public void shouldDeleteImmunityProofByIdentity() {
		immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		immunityProofDAO.createImmunityProof(id2, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		immunityProofDAO.deleteImmunityProof(id1);
		
		ImmunityProof proof1 = immunityProofDAO.getImmunitiyProof(id1);
		ImmunityProof proof2 = immunityProofDAO.getImmunitiyProof(id2);
		
		assertTrue(proof1 == null);
		assertTrue(proof2 != null);
	}
	
	@Test
	public void shouldDeleteImmunityProof() {
		ImmunityProof proof1 = immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		ImmunityProof proof2 = immunityProofDAO.createImmunityProof(id2, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		immunityProofDAO.deleteImmunityProof(proof1);
		
		proof1 = immunityProofDAO.getImmunitiyProof(id1);
		proof2 = immunityProofDAO.getImmunitiyProof(id2);
		
		assertTrue(proof1 == null);
		assertTrue(proof2 != null);
	}
	
	@Test
	public void shouldPruneProofs() {
		ImmunityProof proof1 = immunityProofDAO.createImmunityProof(id1, DateUtils.addDays(new Date(), -36), true, true);
		ImmunityProof proof2 = immunityProofDAO.createImmunityProof(id2, DateUtils.addDays(new Date(), -18), true, true);
		dbInstance.commitAndCloseSession();
		
		immunityProofDAO.pruneImmunityProofs(DateUtils.addDays(new Date(), -26));
		dbInstance.commitAndCloseSession();
		
		List<ImmunityProof> allProofs = immunityProofDAO.getAllCertificates();
		
		assertFalse(allProofs.contains(proof1));
		assertTrue(allProofs.contains(proof2));
	}
	
	@Test
	public void shouldDeleteAllProofs() {
		ImmunityProof proof1 = immunityProofDAO.createImmunityProof(id1, new Date(), true, true);
		ImmunityProof proof2 = immunityProofDAO.createImmunityProof(id2, new Date(), true, true);
		dbInstance.commitAndCloseSession();
		
		immunityProofDAO.deleteAllImmunityProofs();
		dbInstance.commitAndCloseSession();
		
		List<ImmunityProof> allProofs = immunityProofDAO.getAllCertificates();
		
		assertFalse(allProofs.contains(proof1));
		assertFalse(allProofs.contains(proof2));
	}
}
