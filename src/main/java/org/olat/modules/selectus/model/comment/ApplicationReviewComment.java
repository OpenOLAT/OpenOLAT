/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.comment;

import java.util.Date;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewComment {
	
	private final String text;
	private final Long authorKey;
	private final String authorName;
	private final Long commentKey;
	private final Long parentCommentKey;
	private final Date creationDate;
	private ApplicationReviewComment parentComment;

	public ApplicationReviewComment(Long commentKey, Long parentCommentKey, Date creationDate, Long authorKey, String authorName, String text) {
		this.commentKey = commentKey;
		this.parentCommentKey = parentCommentKey;
		this.authorKey = authorKey;
		this.authorName = authorName;
		this.creationDate = creationDate;
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public long size() {
		return text == null ? 0l : text.length();
	}

	public Long getAuthorKey() {
		return authorKey;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public Long getCommentKey() {
		return commentKey;
	}

	public Long getParentCommentKey() {
		return parentCommentKey;
	}

	public ApplicationReviewComment getParentComment() {
		return parentComment;
	}

	public void setParentComment(ApplicationReviewComment parentComment) {
		this.parentComment = parentComment;
	}
}
