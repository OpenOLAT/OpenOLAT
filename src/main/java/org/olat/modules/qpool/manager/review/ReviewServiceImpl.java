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
package org.olat.modules.qpool.manager.review;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ReviewDecisionProvider;
import org.olat.modules.qpool.ReviewService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.ReviewDecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReviewServiceImpl implements ReviewService {
	
	private static final Collection<QuestionStatus> CHANGED_STATUS_FOR_REVIEW = Arrays.asList(
			QuestionStatus.draft,
			QuestionStatus.revised,
			QuestionStatus.finalVersion,
			QuestionStatus.endOfLife,
			QuestionStatus.unavailable);
	private static final Collection<QuestionStatus> CHANGED_STATUS_FOR_FINAL = Arrays.asList(
			QuestionStatus.draft,
			QuestionStatus.revised,
			QuestionStatus.unavailable);

	@Autowired
	private List<ReviewDecisionProvider> reviewDecisionProvidersList;
    private Map<String, ReviewDecisionProvider> reviewDecisionProviders = new HashMap<>();
    
	@PostConstruct
    void initReviewDecisionProviders() {
        for(ReviewDecisionProvider provider : reviewDecisionProvidersList) {
        		reviewDecisionProviders.put(provider.getType(), provider);
        }
    }

	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private CommentAndRatingService commentAndRatingService;
	
	@Override
	public boolean isReviewStarting(QuestionStatus previousStatus, QuestionStatus newStatus) {
		return (QuestionStatus.review.equals(newStatus) && CHANGED_STATUS_FOR_REVIEW.contains(previousStatus))
				|| (QuestionStatus.finalVersion.equals(newStatus) && CHANGED_STATUS_FOR_FINAL.contains(previousStatus));
	}

	@Override
	public void startReview(QuestionItem item) {
		if (qpoolModule.isReviewProcessEnabled()) {
			commentAndRatingService.deleteAllIgnoringSubPath(item);
			incrementVersion(item);
		}
	}

	/**
	 * Increments the version of the question item by 1. If the actual version is not numeric, the new version is set to 1.
	 * 
	 * @param item
	 */
	public void incrementVersion(QuestionItem item) {
		if (item instanceof QuestionItemImpl) {
			int incrementedVersion = 1;
			try {
				int val = Integer.parseInt(item.getItemVersion());
				if(val > 0) {
					incrementedVersion = val + 1;
				}
			} catch (Exception e) {
				// use default value
			}
			String itemVersion = Integer.toString(incrementedVersion);
			((QuestionItemImpl) item).setItemVersion(itemVersion);
		}
	}
	
	@Override
	public boolean hasRatingController() {
		return getActiveReviewDecisionProvider().hasRatingController();
	}

	@Override
	public ReviewDecision decideStatus(QuestionItem item, Float rating) {
		return getActiveReviewDecisionProvider().decideStatus(item, rating);
	}
	
	@Override
	public List<ReviewDecisionProvider> getSelectableReviewDecisionProviders() {
		return reviewDecisionProvidersList.stream()
				.filter(provider -> provider.isSelectable())
				.collect(Collectors.toList());
	}	

	private ReviewDecisionProvider getActiveReviewDecisionProvider() {
		ReviewDecisionProvider provider = reviewDecisionProviders.get(ProcesslessDecisionProvider.TYPE);

		String cofiguredProviderType = qpoolModule.getReviewDecisionProviderType();
		if (qpoolModule.isReviewProcessEnabled() && reviewDecisionProviders.containsKey(cofiguredProviderType)) {
			provider = reviewDecisionProviders.get(cofiguredProviderType);
		}
		
		return provider;
	}

}
