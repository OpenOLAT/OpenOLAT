/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.CommentService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.comment.ApplicationReviewComment;
import org.olat.modules.selectus.model.comment.ApplicationReviewCommentComparator;
import org.olat.modules.selectus.model.comment.ApplicationReviewCommentKey;
import org.olat.modules.selectus.model.comment.ApplicationReviewComments;
import org.olat.modules.selectus.model.comment.PositionComments;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteImpl;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 14 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CommentServiceImpl implements CommentService {
	
	private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ApplicationCommentDAO applicationCommentDao;
	@Autowired
	private ApplicationCommentVoteDAO applicationCommentVoteDao;

	@Override
	public List<ApplicationComment> getComments(ApplicationRef application) {
		return applicationCommentDao.getApplicationComments(application);
	}

	@Override
	public List<ApplicationComment> getComments(ApplicationRef application, Identity reviewer) {
		return applicationCommentDao.getApplicationComments(application, reviewer);
	}

	@Override
	public ApplicationComment addComment(Application application, Identity reviewer, String comment, Identity author, ApplicationComment parentComment) {
		return applicationCommentDao.createAndPersistComment(comment, author, application, reviewer, parentComment);
	}
	
	@Override
	public ApplicationComment updateComment(ApplicationComment comment, String text) {
		comment.setComment(text);
		return applicationCommentDao.update(comment);
	}

	@Override
	public void deleteComment(ApplicationComment comment) {
		applicationCommentVoteDao.deleteVotes(comment);
		
		if(applicationCommentDao.hasReply(comment)) {
			applicationCommentDao.markAsDelete(comment);
		} else {
			applicationCommentDao.delete(comment);
		}
	}

	@Override
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application) {
		return applicationCommentVoteDao.getVotes(application);
	}

	@Override
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application, Identity reviewer) {
		return applicationCommentVoteDao.getVotes(application, reviewer);
	}

	@Override
	public ApplicationCommentVoteStatistics getVotes(ApplicationComment appComment) {
		return applicationCommentVoteDao.getVotes(appComment);
	}

	@Override
	public void vote(ApplicationComment comment, Identity identity, boolean agree) {
		ApplicationCommentVoteImpl vote = applicationCommentVoteDao.getVote(identity, comment);
		if(vote == null) {
			applicationCommentVoteDao.createVote(identity, comment, agree);
		} else {
			vote.setUp(agree);
			applicationCommentVoteDao.update(vote);
		}
		dbInstance.commit();
	}

	@Override
	public PositionComments getComments(PositionRef position) {
		PositionComments positionComments = new PositionComments(position);
		
		List<Identity> authors = applicationCommentDao.getAuthors(position);
		Map<Long,String> authorsFullNameMap = authors.stream()
				.collect(Collectors.toMap(Identity::getKey, RecruitingHelper::formatFullName));
		
		int counter = 0;
		List<ApplicationComment> comments;
		Map<ApplicationReviewCommentKey,ApplicationReviewComments> commentsMap = new HashMap<>();
		do {
			comments = applicationCommentDao.getAllComments(position, counter, BATCH_SIZE);
			counter += comments.size();
			for(ApplicationComment comment:comments) {
				Long appKey = comment.getApplication().getKey();
				Long reviewerKey = comment.getReviewer().getKey();
				ApplicationReviewCommentKey commentKey = new ApplicationReviewCommentKey(appKey, reviewerKey);
				ApplicationReviewComments appReviewComments = commentsMap
						.computeIfAbsent(commentKey, k -> new ApplicationReviewComments());
				String authorFullName = authorsFullNameMap.get(comment.getAuthor().getKey());
				Long parentKey = comment.getParentComment() == null ? null : comment.getParentComment().getKey();
				ApplicationReviewComment minComment = new ApplicationReviewComment(comment.getKey(), parentKey, comment.getCreationDate(),
						comment.getAuthor().getKey(), authorFullName, comment.getComment());
				appReviewComments.addComment(minComment);
			}
		} while(comments.size() == BATCH_SIZE);
		
		for(ApplicationReviewComments appReviewComments:commentsMap.values()) {
			List<ApplicationReviewComment> commentList = appReviewComments.getComments();
			// parent them
		
			Map<Long,ApplicationReviewComment> reviewCommentKeyToReviewComment = commentList.stream()
					.collect(Collectors.toMap(ApplicationReviewComment::getCommentKey, Function.identity(), (u,v) -> u));	
			for(ApplicationReviewComment reviewComment:commentList) {
				if(reviewComment.getParentCommentKey() != null) {
					ApplicationReviewComment parentReviewComment = reviewCommentKeyToReviewComment.get(reviewComment.getParentCommentKey());
					reviewComment.setParentComment(parentReviewComment);
				}
			}
			Collections.sort(commentList, new ApplicationReviewCommentComparator());
		}
		
		positionComments.setCommentsMap(commentsMap);
		dbInstance.commitAndCloseSession();
		return positionComments;
	}
}
