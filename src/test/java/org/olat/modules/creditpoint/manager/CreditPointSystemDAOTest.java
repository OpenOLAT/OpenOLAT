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

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.manager.CertificationProgramDAO;
import org.olat.modules.creditpoint.CreditPointExpirationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.CreditPointSystemToOrganisation;
import org.olat.modules.creditpoint.CreditPointTransaction;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.model.CreditPointSystemWithWalletInfos;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CreditPointSystemDAO creditPointSystemDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CreditPointTransactionDAO creditPointTransactionDao;
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
	public void createNewSystem() {
		String name = "OpenOlat coin";
		String label = "OOC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(cpSystem);
		Assert.assertNotNull(cpSystem.getKey());
		Assert.assertNotNull(cpSystem.getCreationDate());
		Assert.assertNotNull(cpSystem.getLastModified());
	}
	
	@Test
	public void loadCreditPointSystems() {
		String name = "frentix coin three";
		String label = "FXC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointSystem> allCpSystems = creditPointSystemDao.loadCreditPointSystems();
		Assertions.assertThat(allCpSystems)
			.containsAnyOf(cpSystem);
	}
	
	@Test
	public void loadActiveCreditPointSystems() {
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("OpenOlat coin three", "OOC", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
	
		CreditPointSystem devaluatedSystem = creditPointSystemDao.createSystem("Devaluated system", "DC", null, null, false, false);
		devaluatedSystem.setStatus(CreditPointSystemStatus.inactive);
		devaluatedSystem = creditPointSystemDao.updateSystem(devaluatedSystem);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointSystem> allCpSystems = creditPointSystemDao.loadActiveCreditPointSystems();
		Assertions.assertThat(allCpSystems)
			.doesNotContain(devaluatedSystem)
			.containsAnyOf(cpSystem);
	}
	
	@Test
	public void loadCreditPointSystem() {
		final String name = "OLAT coin two";
		final String label = "OLATC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(192), CreditPointExpirationType.DAY, true, false);
		dbInstance.commitAndCloseSession();
		
		CreditPointSystem reloadedSystem = creditPointSystemDao.loadCreditPointSystem(cpSystem.getKey());
		Assert.assertNotNull(reloadedSystem);
		Assert.assertEquals(cpSystem, reloadedSystem);
		Assert.assertEquals(name, reloadedSystem.getName());
		Assert.assertEquals(label, reloadedSystem.getLabel());
		Assert.assertEquals(Integer.valueOf(192), reloadedSystem.getDefaultExpiration());
		Assert.assertTrue(reloadedSystem.isRolesRestrictions());
		Assert.assertFalse(reloadedSystem.isOrganisationsRestrictions());
	}
	
	@Test
	public void getCreditPointSystemsWithTransactions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("coin");
		
		// A system
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("Cur. coins", "CC", Integer.valueOf(192), CreditPointExpirationType.DAY, true, false);

		// User has at least a transaction
		CreditPointWallet wallet = creditPointWalletDao.createWallet(id, cpSystem);
		CreditPointTransaction transaction = creditPointTransactionDao.createTransaction(CreditPointTransactionType.deposit,
				new BigDecimal("100"), new BigDecimal("100"), null, "Note", id, wallet, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(transaction);
		
		List<CreditPointSystem> systems = creditPointSystemDao.getCreditPointSystemsWithProgramsOrTransactions(id);
		Assertions.assertThat(systems)
			.hasSize(1)
			.containsExactly(cpSystem);
	}
	
	@Test
	public void getCreditPointSystemsWithCertificationProgram() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("coin");
		
		// A system
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("Cur. coins", "CC", Integer.valueOf(192), CreditPointExpirationType.DAY, true, false);

		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-4", "Program to curriculum");
		program.setStatus(CertificationProgramStatusEnum.active);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(cpSystem);
		certificationProgramDao.updateCertificationProgram(program);
		
		CertificateConfig config = CertificateConfig.builder().build();
		CertificateInfos certificateInfos = new CertificateInfos(id, null, null, null, null, "");
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		
		List<CreditPointSystem> systems = creditPointSystemDao.getCreditPointSystemsWithProgramsOrTransactions(id);
		Assertions.assertThat(systems)
			.hasSize(1)
			.containsExactly(cpSystem);
	}
	
	@Test
	public void loadCreditPointSystemsWithInfos() {
		final String name = "OLAT coin one";
		final String label = "OLATC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(192), CreditPointExpirationType.DAY, true, false);
		dbInstance.commitAndCloseSession();
		
		List<CreditPointSystemWithWalletInfos> systems = creditPointSystemDao.loadCreditPointSystemsWithInfos();
		Assertions.assertThat(systems)
			.hasSizeGreaterThanOrEqualTo(1);
		
		CreditPointSystemWithWalletInfos systemWithInfos = systems.stream()
				.filter(infos -> infos.system().equals(cpSystem))
				.findFirst()
				.orElse(null);
		
		Assert.assertNotNull(systemWithInfos);
		Assert.assertEquals(cpSystem, systemWithInfos.system());
		Assert.assertEquals(0, systemWithInfos.usage());
	}
	
	@Test
	public void loadCreditPointSystemsWithUsageInfos() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cps-1");
		final String name = "OLAT coin one";
		final String label = "OLATC";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, label, Integer.valueOf(192), CreditPointExpirationType.DAY, true, false);
		CreditPointWallet wallet = creditPointWalletDao.createWallet(owner, cpSystem);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(wallet);
		
		List<CreditPointSystemWithWalletInfos> systems = creditPointSystemDao.loadCreditPointSystemsWithInfos();
		Assertions.assertThat(systems)
			.hasSizeGreaterThanOrEqualTo(1);
		
		CreditPointSystemWithWalletInfos systemWithInfos = systems.stream()
				.filter(infos -> infos.system().equals(cpSystem))
				.findFirst()
				.orElse(null);
		
		Assert.assertNotNull(systemWithInfos);
		Assert.assertEquals(cpSystem, systemWithInfos.system());
		Assert.assertEquals(1, systemWithInfos.usage());
	}
	
	@Test
	public void getOrganisationsMap() {
		String name = "CPS-6";
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem(name, "R6", Integer.valueOf(180), CreditPointExpirationType.DAY, true, true);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<OrganisationRef> organisations = List.of(defaultOrganisation);
		List<OrganisationRef> restrictedOrganisations = List.of(defaultUnitTestOrganisation);
		List<CreditPointSystem> systemList = creditPointSystemDao.loadCreditPointSystemsFor(organisations, restrictedOrganisations);
		Assertions.assertThat(systemList)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(cpSystem);
	}
	
	@Test
	public void getOrganisationsMapForbidden() {
		Organisation forbiddenOrganisation = organisationService
				.createOrganisation("Credit-point-forbidden-2", "Credit-point-forbidden-2", "", null, null, JunitTestHelper.getDefaultActor());
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("CSP-7", "R7", Integer.valueOf(180), CreditPointExpirationType.DAY, true, true);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, forbiddenOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<OrganisationRef> organisations = List.of(defaultOrganisation, defaultUnitTestOrganisation);
		List<OrganisationRef> restrictedOrganisations = List.of(defaultUnitTestOrganisation);
		List<CreditPointSystem> systemList = creditPointSystemDao.loadCreditPointSystemsFor(organisations, restrictedOrganisations);
		Assertions.assertThat(systemList)
			.doesNotContain(cpSystem);
	}
	
	@Test
	public void getOrganisationsMapNoRestriction() {
		Organisation forbiddenOrganisation = organisationService
				.createOrganisation("Credit-point-forbidden-3", "Credit-point-forbidden-3", "", null, null, JunitTestHelper.getDefaultActor());
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("CSP-8", "R8", Integer.valueOf(180), CreditPointExpirationType.DAY, false, false);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, forbiddenOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<OrganisationRef> organisations = List.of(defaultOrganisation, defaultUnitTestOrganisation);
		List<OrganisationRef> restrictedOrganisations = List.of(defaultUnitTestOrganisation);
		List<CreditPointSystem> systemList = creditPointSystemDao.loadCreditPointSystemsFor(organisations, restrictedOrganisations);
		Assertions.assertThat(systemList)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(cpSystem);
	}
	
	@Test
	public void getOrganisationsMapOrganisationRestriction() {
		Organisation forbiddenOrganisation = organisationService
				.createOrganisation("Credit-point-forbidden-4", "Credit-point-forbidden-4", "", null, null, JunitTestHelper.getDefaultActor());
		CreditPointSystem cpSystem = creditPointSystemDao.createSystem("CSP-9", "R9", Integer.valueOf(180), CreditPointExpirationType.DAY, true, false);
		CreditPointSystemToOrganisation relation = creditPointSystemToOrganisationDao.createRelation(cpSystem, forbiddenOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		List<OrganisationRef> restrictedOrganisations = List.of(defaultUnitTestOrganisation);
		List<CreditPointSystem> systemList = creditPointSystemDao.loadCreditPointSystemsFor(List.of(), restrictedOrganisations);
		Assertions.assertThat(systemList)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(cpSystem);
	}
}
