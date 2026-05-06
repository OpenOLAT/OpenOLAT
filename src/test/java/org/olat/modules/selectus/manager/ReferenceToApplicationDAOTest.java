/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceToApplicationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ReferenceDAO referenceDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ReferenceToApplicationDAO referenceToApplicationDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createReferenceToApplication() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prf.", "Albert", "Einstein", "University of Bern", "albert@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", app);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication referenceToApp = referenceToApplicationDao.createRelation(ref, app);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(referenceToApp);
		Assert.assertNotNull(referenceToApp.getKey());
		Assert.assertNotNull(referenceToApp.getCreationDate());
		Assert.assertEquals(app, referenceToApp.getApplication());
		Assert.assertEquals(ref, referenceToApp.getReference());
	}

	@Test
	public void getReferenceToApplications() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		Reference ref = referenceDao.createReference("Dr. Prf.", "Albert", "Einstein", "University of Bern", "albert@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceToApplication referenceToApp = referenceToApplicationDao.createRelation(ref, app);
		dbInstance.commitAndCloseSession();
		
		List<ReferenceToApplication> referenceToApplications = referenceToApplicationDao.getReferenceToApplications(ref);
		assertThat(referenceToApplications)
			.hasSize(1)
			.containsExactly(referenceToApp);
	}
	
	@Test
	public void deleteApplicationsWithReferenceToThreeApplications() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = applicationDao.createApplication(position);
		app1 = applicationDao.saveApplication(app1);
		Application app2 = applicationDao.createApplication(position);
		app2 = applicationDao.saveApplication(app2);
		Application app3 = applicationDao.createApplication(position);
		app3 = applicationDao.saveApplication(app3);
		
		Reference ref = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication referenceToApp2 = referenceToApplicationDao.createRelation(ref, app2);
		dbInstance.commitAndCloseSession();
		ReferenceToApplication referenceToApp3 = referenceToApplicationDao.createRelation(ref, app3);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(referenceToApp2);
		Assert.assertNotNull(referenceToApp3);
		
		recruitingService.deleteApplication(app3, actor);
		recruitingService.deleteApplication(app2, actor);
		recruitingService.deleteApplication(app1, actor);
		
		List<Reference> references = referenceDao.getReferences(position, null, false);
		Assert.assertTrue(references.isEmpty());
	}
	
	@Test
	public void deleteApplicationsWithMixedReferenceToApplications() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = applicationDao.createApplication(position);
		app1 = applicationDao.saveApplication(app1);
		Application app2 = applicationDao.createApplication(position);
		app2 = applicationDao.saveApplication(app2);
		Application app3 = applicationDao.createApplication(position);
		app3 = applicationDao.saveApplication(app3);
		
		Reference ref = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		Reference expertRef = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app1);
		
		ReferenceToApplication referenceToApp2 = referenceToApplicationDao.createRelation(ref, app2);
		ReferenceToApplication referenceToApp3 = referenceToApplicationDao.createRelation(ref, app3);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(expertRef);
		Assert.assertNotNull(referenceToApp2);
		Assert.assertNotNull(referenceToApp3);
		
		recruitingService.deleteApplication(app3, actor);
		recruitingService.deleteApplication(app2, actor);
		recruitingService.deleteApplication(app1, actor);
		
		List<Reference> references = referenceDao.getReferences(position, null, false);
		Assert.assertTrue(references.isEmpty());
	}
	
	@Test
	public void deleteApplicationWithReferenceToTwoApplications() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = applicationDao.createApplication(position);
		app1 = applicationDao.saveApplication(app1);
		Application app2 = applicationDao.createApplication(position);
		app2 = applicationDao.saveApplication(app2);
		Application app3 = applicationDao.createApplication(position);
		app3 = applicationDao.saveApplication(app3);
		
		Reference ref = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();

		ReferenceToApplication referenceToApp1 = referenceToApplicationDao.createRelation(ref, app1);
		ReferenceToApplication referenceToApp2 = referenceToApplicationDao.createRelation(ref, app2);
		ReferenceToApplication referenceToApp3 = referenceToApplicationDao.createRelation(ref, app3);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(referenceToApp1);
		Assert.assertNotNull(referenceToApp2);
		Assert.assertNotNull(referenceToApp3);
		
		recruitingService.deleteApplication(app1, actor);
		
		List<Reference> references = referenceDao.getReferences(position, ReferenceType.comparativeAssessmentExpert, false);
		Assert.assertTrue(references.isEmpty());
	}
	
	@Test
	public void deletePositionWithReferenceToApplications() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication referenceToApp = referenceToApplicationDao.createRelation(ref, app);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(referenceToApp);
		Assert.assertEquals(app, referenceToApp.getApplication());
		Assert.assertEquals(ref, referenceToApp.getReference());
		
		recruitingService.deletePosition(position);
	}
	
	@Test
	public void deletePositionWithReferenceToThreeApplications() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = applicationDao.createApplication(position);
		app1 = applicationDao.saveApplication(app1);
		Application app2 = applicationDao.createApplication(position);
		app2 = applicationDao.saveApplication(app2);
		Application app3 = applicationDao.createApplication(position);
		app3 = applicationDao.saveApplication(app3);
		
		Reference ref = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication referenceToApp2 = referenceToApplicationDao.createRelation(ref, app2);
		dbInstance.commitAndCloseSession();
		ReferenceToApplication referenceToApp3 = referenceToApplicationDao.createRelation(ref, app3);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(referenceToApp2);
		Assert.assertNotNull(referenceToApp3);
		
		recruitingService.deletePosition(position);
	}
	
	@Test
	public void deleteApplicationParanoiaWithReferenceToTwoApplications() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Position position = createRandomPosition(PositionStatus.published);
		Application app1 = applicationDao.createApplication(position);
		app1 = applicationDao.saveApplication(app1);
		Application app2 = applicationDao.createApplication(position);
		app2 = applicationDao.saveApplication(app2);
		Application app3 = applicationDao.createApplication(position);
		app3 = applicationDao.saveApplication(app3);
		
		// Reference app 1
		Reference compRef1 = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication compAssessmentToApp1 = referenceToApplicationDao.createRelation(compRef1, app1);
		dbInstance.commitAndCloseSession();
		ReferenceToApplication compAssessmentToApp2 = referenceToApplicationDao.createRelation(compRef1, app2);
		dbInstance.commitAndCloseSession();
		ReferenceToApplication compAssessmentToApp3 = referenceToApplicationDao.createRelation(compRef1, app3);
		dbInstance.commitAndCloseSession();

		// Reference app 2
		Reference expertRef2 = referenceDao.createReference("Dr.", "John", "Doe", "University", "john@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app2);
		dbInstance.commitAndCloseSession();
		
		Reference compRef2 = referenceDao.createReference("Dr.", "Livia", "Bluemontain", "University", "livia@frentix.com",
				new Date(), ReferenceType.comparativeAssessmentExpert, ReferenceRequestStatus.accepted, "-", null);
		dbInstance.commitAndCloseSession();
		
		ReferenceToApplication compAssessment2ToApp2 = referenceToApplicationDao.createRelation(compRef2, app2);
		dbInstance.commitAndCloseSession();
		ReferenceToApplication compAssessment2ToApp3 = referenceToApplicationDao.createRelation(compRef2, app3);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(compAssessmentToApp1);
		Assert.assertNotNull(compAssessmentToApp2);
		Assert.assertNotNull(compAssessmentToApp3);

		Assert.assertNotNull(expertRef2);
		Assert.assertNotNull(compAssessment2ToApp2);
		Assert.assertNotNull(compAssessment2ToApp3);
		
		recruitingService.deleteApplication(app1, actor);
		
		List<Reference> referencesApp2 = referenceDao.getReferences(app2, null);
		Assertions.assertThat(referencesApp2)
			.hasSize(3)
			.containsExactlyInAnyOrder(expertRef2, compRef1, compRef2);
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-255");
		position.setPositionTitle("Technician in aquarium");
		position.setShortTitle("Designer of planted aquarium");
		position.setDepartment("ADA");
		position.setHomepage("http://www.frentix.com");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a designer for our exhibition of planted aquariums.");
		return positionDao.savePosition(position);
	}
}
