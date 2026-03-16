/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.List;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.comment.PositionComments;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;

/**
 * 
 * Initial date: 13 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CommentService {
	
	public List<ApplicationComment> getComments(ApplicationRef application);
	

	public List<ApplicationComment> getComments(ApplicationRef application, Identity reviewer);

	
	/**
	 * The method add a comment to the specified reviewer (the review)
	 * 
	 * @param application The application
	 * @param reviewer The reviewer to comment
	 * @param comment
	 * @param author
	 * @return
	 */
	public ApplicationComment addComment(Application application, Identity reviewer, String comment, Identity author, ApplicationComment parentComment);
	
	public ApplicationComment updateComment(ApplicationComment comment, String text);
	
	public void deleteComment(ApplicationComment comment);
	
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application);
	
	public List<ApplicationCommentVoteStatistics> getVotes(ApplicationRef application, Identity reviewer);
	
	public ApplicationCommentVoteStatistics getVotes(ApplicationComment appComment);
	
	public void vote(ApplicationComment comment, Identity identity, boolean agree);
	
	public PositionComments getComments(PositionRef position);

}
