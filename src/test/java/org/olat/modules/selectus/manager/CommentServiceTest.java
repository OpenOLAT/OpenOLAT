/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import org.olat.modules.selectus.CommentService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.Person;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.comment.ApplicationReviewComment;
import org.olat.modules.selectus.model.comment.ApplicationReviewCommentKey;
import org.olat.modules.selectus.model.comment.ApplicationReviewComments;
import org.olat.modules.selectus.model.comment.PositionComments;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommentServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private CommentService commentService;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ApplicationCommentDAO applicationCommentDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-cat-unit-test", "Org-app-cat-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void getPositionComments() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-2");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-2");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment1 = applicationCommentDao.createAndPersistComment("Comment 1", author, app, reviewer, null);
		ApplicationComment comment2 = applicationCommentDao.createAndPersistComment("Comment 2", author, app, reviewer, null);
		dbInstance.commit();	
		
		PositionComments allComments = commentService.getComments(position);
		Assert.assertNotNull(allComments);
		Map<ApplicationReviewCommentKey, ApplicationReviewComments> reviewComments = allComments.getCommentsMap();
		Assert.assertNotNull(reviewComments);
		Assert.assertEquals(1, reviewComments.size());
		
		ApplicationReviewComments comments = reviewComments.get(new ApplicationReviewCommentKey(app.getKey(), reviewer.getKey()));
		List<ApplicationReviewComment> commentList = comments.getComments();
		Assert.assertNotNull(commentList);
		Assert.assertEquals(2, commentList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(ApplicationReviewComment comment:commentList) {
			if(comment.getText().equals(comment1.getComment())) {
				found1 = true;
			}
			if(comment.getText().equals(comment2.getComment())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void getPositionComments_aLot() {
		Identity reviewOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-3");
		Identity reviewer1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-3");
		Identity reviewer2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-4");
		Identity reviewer3 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-5");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		for(int i=0; i<10; i++) {
			ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment " + i, reviewer1, app, reviewOwner, null);
			ApplicationComment subComment = applicationCommentDao.createAndPersistComment("Comment sub " + i, reviewer2, app, reviewOwner, comment);
			ApplicationComment subSubComment = applicationCommentDao.createAndPersistComment("Comment sub sub " + i, reviewer3, app, reviewOwner, subComment);
			Assert.assertNotNull(subSubComment);
			Assert.assertEquals(subComment, subSubComment.getParentComment());
		}
		dbInstance.commit();	
		
		PositionComments allComments = commentService.getComments(position);
		Assert.assertNotNull(allComments);
		Map<ApplicationReviewCommentKey, ApplicationReviewComments> reviewComments = allComments.getCommentsMap();
		Assert.assertNotNull(reviewComments);
		Assert.assertEquals(1, reviewComments.size());
		
		ApplicationReviewComments comments = reviewComments.get(new ApplicationReviewCommentKey(app.getKey(), reviewOwner.getKey()));
		List<ApplicationReviewComment> commentList = comments.getComments();
		Assert.assertNotNull(commentList);
		Assert.assertEquals(30, commentList.size());
		
		// check 30 distincts comments
		Set<String> commentTexts = commentList.stream()
				.map(ApplicationReviewComment::getText)
				.collect(Collectors.toSet());
		Assert.assertEquals(30, commentTexts.size());
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
