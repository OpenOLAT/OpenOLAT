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
package org.olat.modules.selectus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.manager.ReviewElementDefinitionDAO.ReviewPair;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionStatistics;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewResponse;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SelectusReviewService {
	
	/**
	 * 
	 * @return A definition, not persisted on the database
	 */
	public PositionReviewDefinition createReviewDefinition();
	
	public PositionReviewDefinition saveReviewDefinition(PositionReviewDefinition def);
	
	public PositionReviewDefinition getReviewDefinition(PositionReviewDefinition def);
	
	public ReviewPair createReviewElement(PositionReviewDefinition def, ReviewElementType type);
	
	public ReviewElementDefinition saveReviewElement(ReviewElementDefinition element);
	
	public PositionReviewDefinition deleteReviewElement(PositionReviewDefinition def, ReviewElementDefinition element);
	
	/**
	 * @param app The application
	 * @return All the responses for the specified application
	 */
	public List<ReviewResponse> getResponses(ApplicationRef app);
	
	/**
	 * @param app The application
	 * @param identity The user who did the review
	 * @return The responses of the review for the specified application and reviewer
	 */
	public List<ReviewResponse> getResponses(ApplicationRef app, IdentityRef identity);
	
	/**
	 * Add or update a response
	 * 
	 * @param element The definition of the element
	 * @param app The application to review
	 * @param identity The reviewer
	 * @param stringValue The value to save
	 * @return The response
	 */
	public ReviewResponse addResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity, String stringValue);
	
	/**
	 * Add or update a response
	 * 
	 * @param element The definition of the element
	 * @param app The application to review
	 * @param identity The reviewer
	 * @param integerValue The value to save
	 * @return The response
	 */
	public ReviewResponse addResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity, Integer integerValue);
	
	public void removeResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity);
	
	/**
	 * Delete the review of the specified user for the specified application.
	 * 
	 * @param app The application
	 * @param identity The identity
	 */
	public void deleteResponses(ApplicationRef app, Identity identity);
	
	public boolean isReviewEnabled(Position position);
	
	/**
	 * The list of reviewers based on the settings of the position
	 * and the general availability of reviews.
	 * 
	 * @param position The position
	 * @return A list of identities
	 */
	public List<Identity> getReviewers(Position position);
	
	/**
	 * The list of reviewers based on the settings of the position
	 * and the general availability of reviews.
	 * 
	 * @param position The position
	 * @return A list of identity references
	 */
	public List<IdentityRef> getReviewerRefs(Position position);
	
	
	public Map<Long,AtomicInteger> getNumberOfReviews(Position position, List<IdentityRef> reviewers);
	
	public Set<Long> getApplicationReviewed(Position position, Identity identity);
	
	public PositionStatistics getReviewStatistics(Position position, Identity viewer, RecruitingPositionSecurityCallback secCallback);
	


}
