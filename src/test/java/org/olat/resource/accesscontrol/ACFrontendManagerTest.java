/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.manager.ACOfferManager;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Test the frontend manager
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ACFrontendManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ACOfferManager acOfferManager;
	
	@Autowired
	private ACFrontendManager acFrontendManager;
	
	@Autowired
	private OLATResourceManager resourceManager;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Autowired
	private BaseSecurity baseSecurityManager;
	
	@Test
	public void testManagers() {
		assertNotNull(acOfferManager);
		assertNotNull(acFrontendManager);
		assertNotNull(dbInstance);
		assertNotNull(resourceManager);
		assertNotNull(repositoryManager);
		assertNotNull(baseSecurityManager);
	}
	
	@Test
	public void testRepoWorkflow() {
		//create a repository entry
		RepositoryEntry re = createRepositoryEntry();
		assertNotNull(re);
		
		//create and save an offer
		Offer offer = acFrontendManager.createOffer(re.getOlatResource(), "TestRepoWorkflow");
		assertNotNull(offer);
		acFrontendManager.save(offer);
		
		dbInstance.commitAndCloseSession();
		
		//retrieve the offer
		List<Offer> offers = acFrontendManager.findOfferByResource(re.getOlatResource(), true, null);
		assertEquals(1, offers.size());
		Offer savedOffer = offers.get(0);
		assertNotNull(savedOffer);
		assertNotNull(savedOffer.getResource());
		assertTrue(re.getOlatResource().equalsByPersistableKey(savedOffer.getResource()));
	}
	
	private RepositoryEntry createRepositoryEntry() {
		//create a repository entry
		OLATResourceable resourceable = new TypedResourceable(UUID.randomUUID().toString().replace("-", ""));
		OLATResource r =  resourceManager.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(r);

		// now make a repository entry for this resource
		RepositoryEntry re = repositoryManager.createRepositoryEntryInstance("Florian Gnägi", "Access controlled by OLAT ", "Description");
		re.setDisplayname("JunitRE" + UUID.randomUUID().toString().replace("-", ""));
		re.setOlatResource(r);
		re.setAccess(RepositoryEntry.ACC_OWNERS_AUTHORS);
		
		SecurityGroup ownerGroup = baseSecurityManager.createAndPersistSecurityGroup();
		re.setOwnerGroup(ownerGroup);
		
		SecurityGroup participantGroup = baseSecurityManager.createAndPersistSecurityGroup();
		re.setParticipantGroup(participantGroup);
		
		SecurityGroup tutorGroup = baseSecurityManager.createAndPersistSecurityGroup();
		re.setTutorGroup(tutorGroup);
		
		repositoryManager.saveRepositoryEntry(re);
		
		dbInstance.commitAndCloseSession();

		return re;
	}
}