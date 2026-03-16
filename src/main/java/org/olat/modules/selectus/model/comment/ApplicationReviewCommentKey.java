/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.comment;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewCommentKey {
	
	private final Long applicationKey;
	private final Long reviewKey;
	
	public ApplicationReviewCommentKey(Long applicationKey, Long reviewKey) {
		this.applicationKey = applicationKey;
		this.reviewKey = reviewKey;
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Long getReviewKey() {
		return reviewKey;
	}

	@Override
	public int hashCode() {
		return applicationKey.hashCode() + reviewKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ApplicationReviewCommentKey) {
			ApplicationReviewCommentKey key = (ApplicationReviewCommentKey)obj;
			return applicationKey.equals(key.applicationKey) && reviewKey.equals(key.reviewKey);
		}
		return true;
	}
}
