/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCommentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ApplicationCommentDAO applicationCommentDao;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-comment-unit-test", "Org-app-comment-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createComment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-1");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-1");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commitAndCloseSession();
		
		applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
	}
	
	@Test
	public void getApplicationComments_application() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-2");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-2");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		
		// load comments
		List<ApplicationComment> comments = applicationCommentDao.getApplicationComments(app);
		Assert.assertNotNull(comments);
		Assert.assertEquals(1,  comments.size());
		Assert.assertEquals(comment, comments.get(0));
	}
	
	@Test
	public void getApplicationComments_reviewer() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-3");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-3");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		
		// load comments
		List<ApplicationComment> comments = applicationCommentDao.getApplicationComments(app, reviewer);
		Assert.assertNotNull(comments);
		Assert.assertEquals(1,  comments.size());
		Assert.assertEquals(comment, comments.get(0));
	}
	
	@Test
	public void hasReply() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-3");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-3");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		ApplicationComment reply = applicationCommentDao.createAndPersistComment("Reply", author, app, reviewer, comment);
		dbInstance.commit();

		
		// load comments
		boolean hasCommentReply = applicationCommentDao.hasReply(comment);
		Assert.assertTrue(hasCommentReply);
		boolean hasReplyReply = applicationCommentDao.hasReply(reply);
		Assert.assertFalse(hasReplyReply);
	}
	
	@Test
	public void getReviewers() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-7");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-7");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		Assert.assertNotNull(comment);
		
		// load comments
		List<Identity> reviewers = applicationCommentDao.getReviewers(position);
		Assert.assertNotNull(reviewers);
		Assert.assertEquals(1,  reviewers.size());
		Assert.assertEquals(reviewer, reviewers.get(0));
	}
	
	@Test
	public void getAuthors() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-7");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-7");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		Assert.assertNotNull(comment);
		
		// load comments
		List<Identity> authors = applicationCommentDao.getAuthors(position);
		Assert.assertNotNull(authors);
		Assert.assertEquals(1,  authors.size());
		Assert.assertEquals(author, authors.get(0));
	}
	
	@Test
	public void getAllComments() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-7");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-7");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		for(int i=0; i<50; i++) {
			applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		}
		dbInstance.commit();
		
		// load comments
		List<ApplicationComment> allComments = applicationCommentDao.getAllComments(position, 0, 100);
		Assert.assertNotNull(allComments);
		Assert.assertEquals(50,  allComments.size());
		Assert.assertEquals(50, new HashSet<>(allComments).size());
	}
	
	@Test
	public void getAllComments_only() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-9");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-9");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		for(int i=0; i<50; i++) {
			applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		}
		dbInstance.commit();
		
		// load comments
		List<ApplicationComment> allComments = applicationCommentDao.getAllComments(position, 0, 10);
		Assert.assertNotNull(allComments);
		Assert.assertEquals(10,  allComments.size());
		Assert.assertEquals(10, new HashSet<>(allComments).size());
	}
	
	@Test
	public void getAllComments_byBatch() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-9");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-9");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		for(int i=0; i<50; i++) {
			applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		}
		dbInstance.commit();
		
		// load comments
		int counter = 0;
		int batchSize = 10;
		List<ApplicationComment> comments;
		Set<ApplicationComment> commentsSet = new HashSet<>();
		do {
			comments = applicationCommentDao.getAllComments(position, counter, batchSize);
			counter += comments.size();
			commentsSet.addAll(comments);
		} while(comments.size() == batchSize);
		
		Assert.assertEquals(50,  commentsSet.size());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Lonely comment");
		position.setPositionTitle("Comments");
		position.setShortTitle("Pilot of comments");
		position.setDepartment("COM");
		position.setHomepage("http://www.comments.ch");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in making of comments.");
		return positionDao.savePosition(position);
	}
	
	private Application createRandomApplication(Position pos) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Ryomou " + UUID.randomUUID());
		person.setLastName("Shimei");
		person.setNationality("JP");
		person.setMail("kanu@ikki.co.jp");
		person.setPhone("9435898");
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}
}
