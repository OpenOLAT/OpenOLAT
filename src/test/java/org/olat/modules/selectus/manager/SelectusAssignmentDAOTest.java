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
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationAssignment;
import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectusAssignmentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private SelectusAssignmentDAO assignmentDao;
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
	public void createAssignment() {
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsRndUser("assignee-1");
		Position position = createRandomPosition();
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		ApplicationAssignment assignment = assignmentDao.createAssignment(assignee, application);
		dbInstance.commit();
		
		Assert.assertNotNull(assignment);
		Assert.assertNotNull(assignment.getKey());
		Assert.assertNotNull(assignment.getCreationDate());
		Assert.assertEquals(assignee, assignment.getAssignee());
		Assert.assertEquals(application, assignment.getApplication());
		
	}
	
	@Test
	public void loadAssignmentsLight() {
		Identity assignee1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignee-2");
		Identity assignee2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignee-3");
		Position position = createRandomPosition();
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		assignmentDao.createAssignment(assignee1, application);
		assignmentDao.createAssignment(assignee2, application);
		dbInstance.commit();
		
		List<ApplicationAssignmentLight> assignments = assignmentDao.getAssignmentPosition(position);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(2, assignments.size());
		Assert.assertEquals(application.getKey(), assignments.get(0).getApplicationKey());
		Assert.assertEquals(application.getKey(), assignments.get(1).getApplicationKey());
		Assert.assertTrue(assignments.get(0).getAssigneeKey().equals(assignee1.getKey())
				|| assignments.get(0).getAssigneeKey().equals(assignee2.getKey()));
		Assert.assertTrue(assignments.get(1).getAssigneeKey().equals(assignee1.getKey())
				|| assignments.get(1).getAssigneeKey().equals(assignee2.getKey()));
	}
	
	@Test
	public void getAssignees() {
		Identity assignee1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignee-2");
		Identity assignee2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignee-3");
		Position position = createRandomPosition();
		Application application = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		assignmentDao.createAssignment(assignee1, application);
		assignmentDao.createAssignment(assignee2, application);
		dbInstance.commit();
		
		List<Identity> assignees = assignmentDao.getAssignees(application);
		Assert.assertNotNull(assignees);
		Assert.assertEquals(2, assignees.size());
		Assert.assertTrue(assignees.contains(assignee1));
		Assert.assertTrue(assignees.contains(assignee2));
	}
	
	private Application createRandomApplication(Position position) {
		Application app = applicationDao.createApplication(position);
		
		Person person = app.getPerson();
		person.setFirstName("Rei");
		person.setLastName("Ayanami");
		person.setNationality("JP");
		person.setMail("rei@nerv.co.jp");
		person.setPhone("9435892");
		person.setBirthday(new Date());
		
		app = applicationDao.saveTempApplication(app, true);
		dbInstance.commitAndCloseSession();
		return app;
	}
	
	private Position createRandomPosition() {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Assignment-234");
		position.setPositionTitle("Technician in automatic assignment");
		position.setShortTitle("Pilot of robot");
		position.setDepartment("selectus");
		position.setHomepage("http://www.selectus.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(PositionStatus.published.name());
		position.setDescription("We search a young pilot for our semi-living robot.");
		return positionDao.savePosition(position);
	}

}
