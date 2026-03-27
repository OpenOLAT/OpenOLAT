/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog;

/**
 * 
 * Initial date: 20 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private AuditService auditService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RecruitingAuditLogDAO recruitingAuditLogDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createAuditLog_withIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-1");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();
		
		String positionXml = auditService.toAuditXml(position);
		String message = "Create new application";
		
		recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				positionXml, positionXml, message, null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAuditLog_quartz() {
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();
		
		String positionXml = auditService.toAuditXml(position);
		String message = "Create new application";
		
		recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				positionXml, positionXml, message, null, null, position, null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void delete_simpleCase() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("audit-2");
		Position position = createRandomPosition(PositionStatus.published);
		dbInstance.commitAndCloseSession();

		recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id);
		recruitingAuditLogDao.auditLog(RecruitingAuditLog.Action.add, RecruitingAuditLog.ActionTarget.position,
				"Before", "After", "Hello", null, null, position, null, null, null, null, null, null, id);
		dbInstance.commitAndCloseSession();
		
		// delete
		int deletedRows = recruitingAuditLogDao.delete(position);
		Assert.assertEquals(2, deletedRows);
		dbInstance.commit();
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
