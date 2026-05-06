/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogReadDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RecruitingAuditLogDAO recruitingAuditLogDao;
	@Autowired
	private RecruitingAuditLogReadDAO recruitingAuditLogReadDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createRead() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
		
		RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
		params.setPosition(position);
		List<RecruitingAuditLog> logs = recruitingAuditLogDao.getPositionLogs(id, params);
		for(RecruitingAuditLog log:logs) {
			recruitingAuditLogReadDao.create(id, log);
		}
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getRead() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		RecruitingAuditLog log1 = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id);
		RecruitingAuditLog log2 = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id);
		recruitingAuditLogReadDao.create(id, log1);
		dbInstance.commitAndCloseSession();
		
		Set<Long> readLogKeys = recruitingAuditLogReadDao.getRead(id);
		Assert.assertNotNull(readLogKeys);
		Assert.assertEquals(1, readLogKeys.size());
		Assert.assertTrue(readLogKeys.contains(log1.getKey()));
		Assert.assertFalse(readLogKeys.contains(log2.getKey()));
	}
	
	@Test
	public void getPositionLogs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-10");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		RecruitingAuditLog log = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Large before", "Larger after", "Hello", null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
		
		RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
		params.setPosition(position);
		List<RecruitingAuditLog> logs = recruitingAuditLogDao.getPositionLogs(id, params);
		Assert.assertNotNull(logs);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(log, logs.get(0));
	}
	
	@Test
	public void getPositionLogsWithOrganisation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-12");
		Identity nonId = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-13");
		Position position = createRandomPosition(PositionStatus.published);
		Organisation organisation = organisationService
				.createOrganisation("Org-audit-position-unit-test", "Org-audit-position-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		position.setOrganisation(organisation);
		position = positionDao.savePosition(position);
		dbInstance.commitAndCloseSession();
		
		organisationService.addMember(organisation, id, OrganisationRoles.selectusmanager, id);

		RecruitingAuditLog log = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Large before", "Larger after", "Hello", null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
		
		RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
		params.setPosition(position);
		params.setOrganisation(true);
		List<RecruitingAuditLog> logs = recruitingAuditLogDao.getPositionLogs(id, params);
		Assertions.assertThat(logs)
			.hasSize(1)
			.containsExactly(log);
		
		List<RecruitingAuditLog> nonLogs = recruitingAuditLogDao.getPositionLogs(nonId, params);
		Assertions.assertThat(nonLogs)
			.isEmpty();
	}
	
	@Test
	public void getPositionLightLogs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-11");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		RecruitingAuditLog log = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Larger before", "Bigger larger after", "Hello", null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
		
		RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
		params.setPosition(position);
		List<RecruitingAuditLogLight> logs = recruitingAuditLogDao.getPositionLightLogs(id, params);
		Assert.assertNotNull(logs);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(log.getKey(), logs.get(0).getKey());
	}
	
	@Test
	public void delete() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-3");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-4");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-5");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		RecruitingAuditLog log1 = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id1);
		RecruitingAuditLog log2 = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id2);
		recruitingAuditLogReadDao.create(id1, log1);
		recruitingAuditLogReadDao.create(id2, log1);
		recruitingAuditLogReadDao.create(id3, log1);
		recruitingAuditLogReadDao.create(id1, log2);
		dbInstance.commitAndCloseSession();
		
		// check data before delete
		Set<Long> readLogKeys = recruitingAuditLogReadDao.getRead(id1);
		Assert.assertEquals(2, readLogKeys.size());
		
		// delete
		int deletedRows = recruitingAuditLogReadDao.delete(position);
		Assert.assertEquals(4, deletedRows);
		
		// check really deleted
		Set<Long> deletedLogKeys = recruitingAuditLogReadDao.getRead(id1);
		Assert.assertTrue(deletedLogKeys.isEmpty());
	}
	
	@Test
	public void delete_paranoia() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-6");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-7");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-8");
		Position position1 = createRandomPosition(PositionStatus.published);
		Position position2 = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		RecruitingAuditLog log1a = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position1, null, null, null, null, null, null, id1);
		RecruitingAuditLog log1b = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position1, null, null, null, null, null, null, id2);
		RecruitingAuditLog log2a = recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position2, null, null, null, null, null, null, id2);
		recruitingAuditLogReadDao.create(id1, log1a);
		recruitingAuditLogReadDao.create(id2, log1a);
		recruitingAuditLogReadDao.create(id3, log1b);
		recruitingAuditLogReadDao.create(id1, log2a);
		dbInstance.commitAndCloseSession();
		
		// check data before delete
		Set<Long> readLogKeys = recruitingAuditLogReadDao.getRead(id1);
		Assert.assertEquals(2, readLogKeys.size());
		
		// delete
		int deletedRows = recruitingAuditLogReadDao.delete(position1);
		Assert.assertEquals(3, deletedRows);
		
		// check really deleted
		Set<Long> survivingLogKeys = recruitingAuditLogReadDao.getRead(id1);
		Assert.assertEquals(1, survivingLogKeys.size());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Audited position");
		position.setPositionTitle("Audits");
		position.setShortTitle("Pilot of audits");
		position.setDepartment("COM");
		position.setHomepage("http://www.audit.ch");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in auditing.");
		return positionDao.savePosition(position);
	}

}
