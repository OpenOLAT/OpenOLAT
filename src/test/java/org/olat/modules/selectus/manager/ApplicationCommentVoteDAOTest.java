/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
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
import org.olat.modules.selectus.model.review.ApplicationCommentVoteImpl;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCommentVoteDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PositionDAO positionDao;
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ApplicationCommentDAO applicationCommentDao;
	@Autowired
	private ApplicationCommentVoteDAO applicationCommentVoteDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-app-vote-unit-test", "Org-app-vote-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createVote() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-1");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-1");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		dbInstance.commit();
		
		// make the vote
		applicationCommentVoteDao.createVote(author, comment, true);
		dbInstance.commit();
	}
	
	@Test
	public void getVote() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-2");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-2");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		ApplicationCommentVoteImpl vote = applicationCommentVoteDao.createVote(author, comment, true);
		dbInstance.commit();
		
		ApplicationCommentVoteImpl loadedVote = applicationCommentVoteDao.getVote(author, comment);
		Assert.assertNotNull(loadedVote);
		Assert.assertNotNull(loadedVote.getCreationDate());
		Assert.assertNotNull(loadedVote.getLastModified());
		Assert.assertEquals(vote, loadedVote);
		Assert.assertEquals(author, loadedVote.getVoter());
		Assert.assertEquals(comment, loadedVote.getComment());
	}
	
	@Test
	public void getVotes_application() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-2");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-2");
		Position position = createRandomPosition(PositionStatus.published);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		ApplicationCommentVoteImpl vote = applicationCommentVoteDao.createVote(author, comment, true);
		dbInstance.commit();
		Assert.assertNotNull(vote);
		
		List<ApplicationCommentVoteStatistics> voteStatistics = applicationCommentVoteDao.getVotes(app);
		Assert.assertNotNull(voteStatistics);
		Assert.assertEquals(1, voteStatistics.size());
		ApplicationCommentVoteStatistics voteStats = voteStatistics.get(0);
		Assert.assertEquals(1, voteStats.getUp());
		Assert.assertEquals(0, voteStats.getDown());
		Assert.assertEquals(comment, voteStats.getComment());
	}
	
	@Test
	public void getVotes_applicationReviewer() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-5");
		Identity reviewer = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-5");
		Position position = createRandomPosition(PositionStatus.closed);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer, null);
		ApplicationCommentVoteImpl vote = applicationCommentVoteDao.createVote(author, comment, false);
		dbInstance.commit();
		Assert.assertNotNull(vote);
		
		List<ApplicationCommentVoteStatistics> voteStatistics = applicationCommentVoteDao.getVotes(app, reviewer);
		Assert.assertNotNull(voteStatistics);
		Assert.assertEquals(1, voteStatistics.size());
		ApplicationCommentVoteStatistics voteStats = voteStatistics.get(0);
		Assert.assertEquals(0, voteStats.getUp());
		Assert.assertEquals(1, voteStats.getDown());
		Assert.assertEquals(comment, voteStats.getComment());
	}
	
	@Test
	public void getVotes_comment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("commenter-6");
		Identity reviewer1 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-7");
		Identity reviewer2 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-8");
		Identity reviewer3 = JunitTestHelper.createAndPersistIdentityAsRndUser("reviewer-9");
		Position position = createRandomPosition(PositionStatus.closed);
		Application app = createRandomApplication(position);
		dbInstance.commit();
		ApplicationComment comment = applicationCommentDao.createAndPersistComment("Comment", author, app, reviewer1, null);
		applicationCommentVoteDao.createVote(reviewer1, comment, false);
		applicationCommentVoteDao.createVote(reviewer2, comment, true);
		applicationCommentVoteDao.createVote(reviewer3, comment, false);
		dbInstance.commit();
		
		ApplicationCommentVoteStatistics voteStatistics = applicationCommentVoteDao.getVotes(comment);
		Assert.assertEquals(1, voteStatistics.getUp());
		Assert.assertEquals(2, voteStatistics.getDown());
		Assert.assertEquals(comment, voteStatistics.getComment());
	}
	
	private Position createRandomPosition(PositionStatus status) {
		Position position = positionDao.createPosition("none", "none", defaultUnitTestOrganisation);
		position.setPlaningsNumber("Lonely vote");
		position.setPositionTitle("Votes");
		position.setShortTitle("Pilot of votes");
		position.setDepartment("COM");
		position.setHomepage("http://www.votes.ch");
		position.setApplicationDeadline(new Date());
		position.setStatus(status.name());
		position.setDescription("We search a expert in making a vote system.");
		return positionDao.savePosition(position);
	}
	
	private Application createRandomApplication(Position pos) {
		Application app = applicationDao.createApplication(pos);
		Person person = app.getPerson();
		person.setFirstName("Sonsaku " + UUID.randomUUID());
		person.setLastName("Hakufu");
		person.setNationality("JP");
		person.setMail("kanu@ikki.co.jp");
		person.setPhone("9435898");
		person.setBirthday(new Date());
		return applicationDao.saveTempApplication(app, true);
	}

}
