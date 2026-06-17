/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.util.mail.MailerResult;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RejectionEmailLog;
import org.olat.modules.selectus.model.RejectionEmailLogFull;

/**
 * 
 * Initial date: 20 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RejectionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private RejectionDAO rejectionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void addLog() {
		//create a position and an application
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application application = applicationDao.createApplication(pos);
		application.getPerson().setFirstName("John");
		application.getPerson().setLastName("Decision B");
		application = applicationDao.saveTempApplication(application, true);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		Assert.assertEquals(1, applications.size());
		
		MailerResult result = new MailerResult();
		result.setReturnCode(MailerResult.OK);
		rejectionDao.addLog("Template", "Subject§", "Content", null, false, applications.get(0), result);
		dbInstance.commit();
	}
	
	@Test
	public void getFullLog() {
		//create a position and an application
		Position pos = createRandomPosition(PositionStatus.publishedAndInScreening);
		Application application = applicationDao.createApplication(pos);
		application.getPerson().setFirstName("John");
		application.getPerson().setLastName("Decision or not decision");
		application = applicationDao.saveTempApplication(application, true);
		dbInstance.commitAndCloseSession();
		
		List<ApplicationLight> applications = applicationDao.findApplicationsLight(pos, true);
		Assert.assertEquals(1, applications.size());
		
		// make the log
		MailerResult result = new MailerResult();
		result.setReturnCode(MailerResult.OK);
		rejectionDao.addLog("Template", "Subject", "Content", null, false, applications.get(0), result);
		dbInstance.commit();

		// load the logs
		List<RejectionEmailLog> logs = rejectionDao.getLog(pos);
		Assert.assertEquals(1, logs.size());
		
		// load the full log
		RejectionEmailLogFull fullLog = rejectionDao.getFullLog(logs.get(0));
		Assert.assertNotNull(fullLog);
		Assert.assertNotNull(fullLog.getKey());
		Assert.assertEquals(MailerResult.OK, fullLog.getStatus());
		Assert.assertEquals(applications.get(0), fullLog.getApplication());
		Assert.assertEquals("Template", fullLog.getMailTemplate());
		Assert.assertEquals("Subject", fullLog.getMailSubject());
		Assert.assertEquals("Content", fullLog.getMailContent());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-323");
		position.setPositionTitle("Log analyser");
		position.setShortTitle("Logger with influx");
		position.setDepartment("Influx");
		position.setHomepage("http://www.frentix.com");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a analyser of log files.");
		return positionDao.savePosition(position);
	}

}
