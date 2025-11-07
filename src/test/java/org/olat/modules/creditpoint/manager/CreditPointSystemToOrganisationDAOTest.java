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
package org.olat.modules.creditpoint.manager;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemToOrganisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemToOrganisationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CreditPointSystemToOrganisationDAO creditPointSystemToOrganisationDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Credit-point-unit-test", "Credit-point-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createRelation() {
		String name = "REL-1";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, "R1", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(cpSystem, relation.getCreditPointSystem());
		Assert.assertEquals(defaultUnitTestOrganisation, relation.getOrganisation());
		
		CreditPointSystem reloadedSystem = creditPointSystemDao.loadCreditPointSystem(cpSystem.getKey());
		Assert.assertNotNull(reloadedSystem);
		Assert.assertEquals(cpSystem, reloadedSystem);
		Assert.assertEquals(1, reloadedSystem.getOrganisations().size());
		Assert.assertEquals(relation, reloadedSystem.getOrganisations().iterator().next());
	}
	
	@Test
	public void loadRelations() {
		String name = "REL-2";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, "R2", Integer.valueOf(180), CreditPointExpirationType.DAY, true, true);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		List<CreditPointSystemToOrganisation> relations = creditPointSystemToOrganisationDao.loadRelations(cpSystem);
		Assertions.assertThat(relations)
			.hasSize(1)
			.containsExactly(relation);
	}
	
	@Test
	public void getOrganisationsMap() {
		String name = "REL-3";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, "R3", Integer.valueOf(180), CreditPointExpirationType.DAY, true, true);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		Map<Long,List<Long>> organisationsMap = creditPointSystemToOrganisationDao.getOrganisationsMap(List.of(cpSystem.getKey()));
		Assertions.assertThat(organisationsMap)
			.hasSize(1)
			.containsKey(cpSystem.getKey())
			.containsEntry(cpSystem.getKey(), List.of(defaultUnitTestOrganisation.getKey()));
	}
	
	@Test
	public void deleteRelations() {
		String name = "REL-4";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, "R4", Integer.valueOf(180), CreditPointExpirationType.DAY, true, true);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		List<CreditPointSystemToOrganisation> relations = creditPointSystemToOrganisationDao.loadRelations(cpSystem);
		Assertions.assertThat(relations)
			.hasSize(1)
			.containsExactly(relation);
		
		creditPointSystemToOrganisationDao.deleteRelations(cpSystem);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointSystemToOrganisation> noRelations = creditPointSystemToOrganisationDao.loadRelations(cpSystem);
		Assertions.assertThat(noRelations)
			.isEmpty();
	}
}
