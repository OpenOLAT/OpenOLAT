/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.comment;

import java.util.Map;

import org.olat.modules.selectus.model.PositionRef;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionComments {
	
	private final PositionRef position;
	private Map<ApplicationReviewCommentKey,ApplicationReviewComments> commentsMap;
	
	public PositionComments(PositionRef position) {
		this.position = position;
	}
	
	public PositionRef getPosition() {
		return position;
	}

	public Map<ApplicationReviewCommentKey, ApplicationReviewComments> getCommentsMap() {
		return commentsMap;
	}

	public void setCommentsMap(Map<ApplicationReviewCommentKey, ApplicationReviewComments> commentsMap) {
		this.commentsMap = commentsMap;
	}
}
