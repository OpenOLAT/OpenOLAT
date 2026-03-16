/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import org.olat.core.commons.services.commentAndRating.model.UserRating;

/**
 * 
 * Initial date: 18.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingClosedException extends Exception {
	
	private final UserRating lastRating;

	private static final long serialVersionUID = 4729990594868219849L;
	
	public RatingClosedException(UserRating lastRating) {
		this.lastRating = lastRating;
	}
	
	public UserRating getLastRating() {
		return lastRating;
	}
}