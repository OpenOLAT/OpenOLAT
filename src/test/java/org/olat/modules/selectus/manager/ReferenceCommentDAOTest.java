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
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceComment;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 27 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceCommentDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ReferenceDAO referenceDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ReferenceCommentDAO referenceCommentDao;
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
	public void testCreateComment() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prf.", "Albert", "Einstein", "University of Bern", "albert@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceComment comment = referenceCommentDao.createComment(ref, "Hello comment");
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(comment);
		Assert.assertNotNull(comment.getKey());
		Assert.assertNotNull(comment.getCreationDate());
		Assert.assertEquals("Hello comment", comment.getComment());
	}
	
	@Test
	public void getReferencesWithComments() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref1 = referenceDao.createReference("Dr. Prf.", "Albert", "Einstein", "University of Bern", "albert@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceComment comment1 = referenceCommentDao.createComment(ref1, "Hello comment");
		
		Reference ref2 = referenceDao.createReference("Dr. Prf.", "Paul", "Dirac", "University of Paris", "paul.dirac@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceComment comment2 = referenceCommentDao.createComment(ref2, "Hello comment");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(comment1);
		Assert.assertNotNull(comment2);
		
		List<Long> referencesWithComments = referenceCommentDao.getReferencesWithComments(position);
		Assert.assertEquals(2, referencesWithComments.size());
		Assert.assertTrue(referencesWithComments.contains(ref1.getKey()));
		Assert.assertTrue(referencesWithComments.contains(ref2.getKey()));
	}
	
	@Test
	public void getComments() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prf.", "Marie", "Curie", "University of Paris", "marie.curie@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceComment comment1 = referenceCommentDao.createComment(ref, "Hello comment");
		ReferenceComment comment2 = referenceCommentDao.createComment(ref, "Hello comment");
		dbInstance.commitAndCloseSession();
		
		List<ReferenceComment> comments = referenceCommentDao.getComments(ref);
		Assert.assertEquals(2, comments.size());
		Assert.assertTrue(comments.contains(comment1));
		Assert.assertTrue(comments.contains(comment2));
	}
	
	@Test
	public void deleteComments() {
		Position position = createRandomPosition(PositionStatus.published);
		Application app = applicationDao.createApplication(position);
		app = applicationDao.saveApplication(app);
		
		Reference ref = referenceDao.createReference("Dr. Prf.", "Marie", "Curie", "University of Paris", "marie.curie@frentix.com",
				new Date(), ReferenceType.expert, ReferenceRequestStatus.accepted, "-", app);
		ReferenceComment comment1 = referenceCommentDao.createComment(ref, "Hello comment");
		ReferenceComment comment2 = referenceCommentDao.createComment(ref, "Hello comment");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(comment1);
		Assert.assertNotNull(comment2);
		
		int deleted = referenceCommentDao.deleteComments(ref);
		Assert.assertEquals(2, deleted);
		dbInstance.commitAndCloseSession();
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("AC-274");
		position.setPositionTitle("Technician in telescopes");
		position.setShortTitle("Master of optic");
		position.setDepartment("CEU");
		position.setHomepage("http://www.ceu.com");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a specialist in optic especially telescopes.");
		return positionDao.savePosition(position);
	}
	
}
