/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;


/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ReferenceDAO referenceDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RecruitingService recruitingFrontendManager;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-selectus-service-unit-test", "Org-selectus-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void testSaveApplication() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prf.", "Albert", "Einstein", "University of Bern", "albert@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(ref);
		Assert.assertNotNull(ref.getKey());
		Assert.assertNotNull(ref.getCreationDate());
		Assert.assertNotNull(ref.getLastModified());
		Assert.assertNotNull(ref.getSubmissionDeadline());
		Assert.assertEquals(app, ref.getApplication());
		Assert.assertEquals("Dr. Prf.", ref.getTitle());
		Assert.assertEquals("Albert", ref.getFirstName());
		Assert.assertEquals("Einstein", ref.getLastName());
		Assert.assertEquals("University of Bern", ref.getInstitution());
		Assert.assertEquals("albert@frentix.com", ref.getEmail());
		Assert.assertEquals(ReferenceType.expert, ref.getReferenceType());
		Assert.assertEquals(ReferenceRequestStatus.accepted, ref.getRequestStatus());
		Assert.assertEquals("-", ref.getAdminNote());
	}
	
	@Test
	public void testSaveAndLoadApplication() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Max", "Planck", "University of Leipzig", "max@frentix.com",
				new Date(), ReferenceType.recommendation, ReferenceRequestStatus.declined, null, app);
		dbInstance.commitAndCloseSession();
		
		Reference reloadedRef = referenceDao.loadByKey(ref.getKey());
		
		Assert.assertNotNull(reloadedRef);
		Assert.assertNotNull(reloadedRef.getKey());
		Assert.assertNotNull(reloadedRef.getCreationDate());
		Assert.assertNotNull(reloadedRef.getLastModified());
		Assert.assertNotNull(reloadedRef.getSubmissionDeadline());
		Assert.assertEquals(app, reloadedRef.getApplication());
		Assert.assertEquals("Dr. Prof.", reloadedRef.getTitle());
		Assert.assertEquals("Max", reloadedRef.getFirstName());
		Assert.assertEquals("Planck", reloadedRef.getLastName());
		Assert.assertEquals("University of Leipzig", reloadedRef.getInstitution());
		Assert.assertEquals("max@frentix.com", reloadedRef.getEmail());
		Assert.assertEquals(ReferenceType.recommendation, ref.getReferenceType());
		Assert.assertEquals(ReferenceRequestStatus.declined, ref.getRequestStatus());
	}

	@Test
	public void loadBySubmissionUrl() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Wolgang", "Pauli", "ETH", "pauli@frentix.com",
				new Date(), ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, app);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ref);
		Assert.assertNotNull(ref.getSubmissionUrl());

		Reference reloadRef = referenceDao.loadBySubmissionUrl(ref.getSubmissionUrl());
		Assert.assertNotNull(reloadRef);
		Assert.assertEquals(ref, reloadRef);
	}
	
	@Test
	public void getReferences_app_type() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Heinrich", "Rohrer", "ETH", "rohrer@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.notAnswered, "No answer", app);
		dbInstance.commitAndCloseSession();
		
		//load by app and type
		List<Reference> expertReferences = referenceDao.getReferences(app, ReferenceType.expert);
		Assert.assertNotNull(expertReferences);
		Assert.assertEquals(1, expertReferences.size());
		Assert.assertTrue(expertReferences.contains(ref));
		
		//check recommendation
		List<Reference> recommendationReferences = referenceDao.getReferences(app, ReferenceType.recommendation);
		Assert.assertNotNull(recommendationReferences);
		Assert.assertTrue(recommendationReferences.isEmpty());
		
		//load by app and type
		List<Reference> allReferences = referenceDao.getReferences(app, null);
		Assert.assertNotNull(allReferences);
		Assert.assertEquals(1, allReferences.size());
		Assert.assertTrue(allReferences.contains(ref));
	}
	
	@Test
	public void getReferences_pos_type() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		((ApplicationImpl)app).setValid(true);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Georg", "Bednorz", "ETH", "bednorz@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.notAnswered, "No response", app);
		dbInstance.commitAndCloseSession();
		
		//load by app and type
		List<Reference> expertReferences = referenceDao.getReferences(position, ReferenceType.expert, false);
		Assert.assertNotNull(expertReferences);
		Assert.assertEquals(1, expertReferences.size());
		Assert.assertTrue(expertReferences.contains(ref));
		
		//check recommendation
		List<Reference> recommendationReferences = referenceDao.getReferences(position, ReferenceType.recommendation, false);
		Assert.assertNotNull(recommendationReferences);
		Assert.assertTrue(recommendationReferences.isEmpty());
	}
	
	@Test
	public void hasReferences() throws IOException {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		((ApplicationImpl)app).setValid(true);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Georg", "Bednorz", "ETH", "bednorz@frentix.com",
				new Date(), ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, app);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ref);

		URL url = ApplicationDAOTest.class.getResource("Curriculum_vitae_1.pdf");
		byte[] bytes = IOUtils.toByteArray(url);
		Attachment letter = recruitingFrontendManager.setAttachmentDatas(position, ref, ref.getLetter(), "Dru", DocumentType.pdf, bytes);
		ref.setLetter(letter);
		ref = recruitingFrontendManager.updateReference(ref);

		//check recommendation
		boolean hasRecommendationReferences = referenceDao.hasReferences(position, ReferenceType.recommendation);
		Assert.assertTrue(hasRecommendationReferences);
		//check expert
		boolean hasExpertReferences = referenceDao.hasReferences(position, ReferenceType.expert);
		Assert.assertFalse(hasExpertReferences);
	}
	
	@Test
	public void hasReferences_noLetter() throws IOException {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Georg", "Bednorz", "ETH", "bednorz@frentix.com",
				new Date(), ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, app);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ref);

		//check recommendation
		boolean hasRecommendationReferences = referenceDao.hasReferences(position, ReferenceType.recommendation);
		Assert.assertFalse(hasRecommendationReferences);
	}
	
	@Test
	public void hasReferences_noReferences() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		dbInstance.commitAndCloseSession();

		//check recommendation
		boolean hasRecommendationReferences = referenceDao.hasReferences(position, ReferenceType.recommendation);
		Assert.assertFalse(hasRecommendationReferences);
		//check expert
		boolean hasExpertReferences = referenceDao.hasReferences(position, ReferenceType.expert);
		Assert.assertFalse(hasExpertReferences);
	}
	
	@Test
	public void getReferences_status() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		((ApplicationImpl)app).setValid(true);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prof.", "Max", "Planck", "University of Leipzig", "max@frentix.com",
				new Date(), ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, app);
		dbInstance.commitAndCloseSession();
		
		ref.setReferenceStatus(ReferenceStatus.sentAwaiting);
		ref = referenceDao.updateReference(ref);
		dbInstance.commitAndCloseSession();
		
		//load by status
		ReferenceSearchParameters searchParams = new ReferenceSearchParameters(ReferenceStatus.sentAwaiting);
		List<Reference> awaitingReferences = referenceDao.getReferences(searchParams);
		Assert.assertNotNull(awaitingReferences);
		Assert.assertTrue(awaitingReferences.size() >= 1);
		Assert.assertTrue(awaitingReferences.contains(ref));
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-235");
		position.setPositionTitle("Technician in robotic");
		position.setShortTitle("Pilot of robot");
		position.setDepartment("NERV");
		position.setHomepage("http://www.nerv.co.jp");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a young pilot for our semi-living robot.");
		return positionDao.savePosition(position);
	}

}
