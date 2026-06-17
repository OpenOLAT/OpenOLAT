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
