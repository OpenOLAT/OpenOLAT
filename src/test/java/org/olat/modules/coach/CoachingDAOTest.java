package org.olat.modules.coach;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.coach.manager.CoachingDAO;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

public class CoachingDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CoachingDAO coachingDAO;
	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void getStudents()
	throws URISyntaxException {
		URL courseWithForumsUrl = CoachingDAOTest.class.getResource("CoachingCourse.zip");
		File courseWithForums = new File(courseWithForumsUrl.toURI());
		String softKey = UUID.randomUUID().toString();
		RepositoryEntry re = CourseFactory.deployCourseFromZIP(courseWithForums, softKey, 4);
		Assert.assertNotNull(re);

		dbInstance.commitAndCloseSession();
		
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());			
		boolean enabled =course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled();
		Assert.assertTrue(enabled);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("Coach-1-" + UUID.randomUUID());
		repositoryService.addRole(coach, re, GroupRoles.coach.name());
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-1");
		repositoryService.addRole(participant1, re, GroupRoles.participant.name());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Coaching-Part-2");
		repositoryService.addRole(participant2, re, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		
		List<StudentStatEntry> stats = coachingDAO.getStudentsStatistics(coach);
		Assert.assertNotNull(stats);
		Assert.assertEquals(2, stats.size());
	}

}
