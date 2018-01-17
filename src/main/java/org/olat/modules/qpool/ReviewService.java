/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool;

import java.util.List;

import org.olat.modules.qpool.model.ReviewDecision;

/**
 * 
 * Initial date: 08.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ReviewService {

	/**
	 * Has the review of the question item immediately started.
	 * 
	 * @param previousStatus
	 * @param newStatus
	 * @return
	 */
	public boolean isReviewStarting(QuestionStatus previousStatus, QuestionStatus newStatus);

	/**
	 * Perform all needed actions when review is starting e.g. delete all ratings.
	 * 
	 * @param item
	 */
	public void startReview(QuestionItem item);

	/**
	 * Decide if the question item has a new status after a rating and what the new status is.
	 * 
	 * @param item
	 * @param rating
	 * @return
	 */
	public ReviewDecision decideStatus(QuestionItem item, Float rating);

	/**
	 * @return a list of all selectable review decision providers.
	 */
	public List<ReviewDecisionProvider> getSelectableReviewDecisionProviders();

	/**
	 * Should the RatingController be shown for the rating of a question.
	 * 
	 * @return
	 */
	public boolean hasRatingController();

}
