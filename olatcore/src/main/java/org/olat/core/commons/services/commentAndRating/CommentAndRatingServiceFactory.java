package org.olat.core.commons.services.commentAndRating;

public interface CommentAndRatingServiceFactory {
	
	/**
	 * call this method to get an prototype of the service. As this service is stateful
	 * you have to call this special method otherwise (like normal dependency injection) you will
	 * always have the same instance in your manager which will cause an error.
	 * @return
	 */
	public CommentAndRatingService getCommentAndRatingService();

}
