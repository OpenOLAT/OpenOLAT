/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import org.olat.modules.selectus.model.ApplicationComment;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCommentVoteStatistics {
	
	private final ApplicationComment comment;
	private final int up;
	private final int down;
	
	public ApplicationCommentVoteStatistics(ApplicationComment comment, int up, int down) {
		this.comment = comment;
		this.up = up;
		this.down = down;
	}

	public ApplicationComment getComment() {
		return comment;
	}

	public int getUp() {
		return up;
	}

	public int getDown() {
		return down;
	}
}
