/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;

import org.olat.modules.selectus.model.ApplicationLight;


/**
 * 
 * Description:<br>
 * Utility class for the single rating renderer
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserRatingMapper {
	
	private final ApplicationLight application;
	private UserRating rating;
	private List<UserRating> ratings;
	
	private String uuid;
	
	public UserRatingMapper(ApplicationLight application) {
		this.application = application;
		uuid = application.getKey().toString();
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public Date getCreationDate() {
		return application.getCreationDate();
	}
	
	public float getCurrentRating() {
		return rating == null ? 0.0f : rating.getRating();
	}

	public UserRating getRating() {
		return rating;
	}

	public void setRating(UserRating rating) {
		this.rating = rating;
	}

	public List<UserRating> getRatings() {
		return ratings;
	}

	public void setRatings(List<UserRating> ratings) {
		this.ratings = ratings;
	}

	public ApplicationLight getApplication() {
		return application;
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof UserRatingMapper) {
			UserRatingMapper mapper = (UserRatingMapper)obj;
			return uuid.equals(mapper.uuid);
		}
		return false;
	}
}
