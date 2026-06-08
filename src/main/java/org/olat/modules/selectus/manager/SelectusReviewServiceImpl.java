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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.manager.ReviewElementDefinitionDAO.ReviewPair;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.review.ApplicationStatisticElement;
import org.olat.modules.selectus.model.review.ApplicationStatistics;
import org.olat.modules.selectus.model.review.ApplicationTextCollectionElement;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionStatistics;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.model.review.ReviewResponseImpl;
import org.olat.modules.selectus.model.review.Reviewer;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SelectusReviewServiceImpl implements SelectusReviewService {
	
	private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private ApplicationDAO applicationDao;
	@Autowired
	private ReviewResponseDAO reviewResponseDao;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private ReviewElementDefinitionDAO reviewElementDefinitionDao;
	@Autowired
	private PositionReviewDefinitionDAO positionReviewDefinitionDao;

	@Override
	public PositionReviewDefinition createReviewDefinition() {
		return positionReviewDefinitionDao.create();
	}

	@Override
	public PositionReviewDefinition saveReviewDefinition(PositionReviewDefinition def) {
		List<ReviewElementDefinition> elements = def.getElements();
		
		final int elementsSize = elements.size();
		if(elementsSize > 0 && elements.get(0) == null) {
			List<ReviewElementDefinition> nonNulls = new ArrayList<>();
			for(int i=0; i<elementsSize; i++) {
				if(elements.get(i) != null) {
					nonNulls.add(elements.get(i));
				}
			}
			
			for(int i=0; i<elementsSize; i++) {
				if(i < nonNulls.size()) {
					elements.set(i, nonNulls.get(i));
				} else {
					elements.set(i, null);
				}
			}
		}

		return positionReviewDefinitionDao.save(def);
	}

	@Override
	public PositionReviewDefinition getReviewDefinition(PositionReviewDefinition def) {
		if(def == null) {
			return null;
		}
		return positionReviewDefinitionDao.loadByKey(def.getKey());
	}

	@Override
	public ReviewPair createReviewElement(PositionReviewDefinition def, ReviewElementType type) {
		return reviewElementDefinitionDao.create(def, type);
	}

	@Override
	public PositionReviewDefinition deleteReviewElement(PositionReviewDefinition def, ReviewElementDefinition element) {
		def.getElements().remove(element);
		//delete associated responses
		reviewResponseDao.delete(element);
		reviewElementDefinitionDao.delete(element);
		return positionReviewDefinitionDao.save(def);
	}

	@Override
	public ReviewElementDefinition saveReviewElement(ReviewElementDefinition element) {
		return reviewElementDefinitionDao.merge(element);
	}

	@Override
	public List<ReviewResponse> getResponses(ApplicationRef app) {
		return reviewResponseDao.getResponses(app);
	}

	@Override
	public List<ReviewResponse> getResponses(ApplicationRef app, IdentityRef identity) {
		return reviewResponseDao.getResponses(app, identity);
	}
	

	@Override
	public void deleteResponses(ApplicationRef app, Identity identity) {
		List<ReviewResponse> responses = reviewResponseDao.getResponses(app, identity);
		for(ReviewResponse response:responses) {
			reviewResponseDao.delete(response);
		}
	}

	@Override
	public ReviewResponse addResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity, String stringValue) {
		return addResponse(element, app, identity, stringValue, null);
	}

	@Override
	public ReviewResponse addResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity, Integer integerValue) {
		return addResponse(element, app, identity, null, integerValue);
	}
	
	private ReviewResponse addResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity,
			String stringValue, Integer integerValue) {
		ReviewResponse response = reviewResponseDao.getResponse(app, element, identity);
		if(response == null) {
			ApplicationLight appLight = applicationDao.loadApplicationLightForReference(app.getKey());
			response = reviewResponseDao.create(appLight, element, identity, stringValue, integerValue);
		} else {
			response.setStringValue(stringValue);
			response.setIntegerValue(integerValue);
			response = reviewResponseDao.merge(response);
		}
		return response;
	}

	@Override
	public void removeResponse(ReviewElementDefinition element, ApplicationRef app, Identity identity) {
		ReviewResponse response = reviewResponseDao.getResponse(app, element, identity);
		if(response != null) {
			reviewResponseDao.delete(response);
		}
	}

	@Override
	public boolean isReviewEnabled(Position position) {
			return recruitingModule.isReviewEnabled()
					&& position.isReviewEnabled()
					&& position.getReviewDefinition() != null;
	}

	@Override
	public List<Identity> getReviewers(Position position) {
		List<Identity> reviewers;
		if(isReviewEnabled(position)) {
			PositionReviewDefinition reviewDef = position.getReviewDefinition();
			PositionRole[] reviewRoles = reviewDef.getReviewFillRoles();
			if(reviewRoles != null && reviewRoles.length > 0) {
				reviewers = recruitingService.getCommittee(position,  reviewDef.getReviewFillRoles());
			} else {
				reviewers = Collections.emptyList();
			}
		} else {
			reviewers = Collections.emptyList();
		}
		return reviewers;
	}
	
	@Override
	public List<IdentityRef> getReviewerRefs(Position position) {
		List<IdentityRef> reviewers;
		if(isReviewEnabled(position)) {
			PositionReviewDefinition reviewDef = position.getReviewDefinition();
			PositionRole[] reviewRoles = reviewDef.getReviewFillRoles();
			if(reviewRoles != null && reviewRoles.length > 0) {
				reviewers = recruitingService.getCommitteeRefs(position,  reviewDef.getReviewFillRoles());
			} else {
				reviewers = Collections.emptyList();
			}
		} else {
			reviewers = Collections.emptyList();
		}
		return reviewers;
	}

	@Override
	public Map<Long, AtomicInteger> getNumberOfReviews(Position position, List<IdentityRef> reviewers) {
		if(reviewers == null || reviewers.isEmpty()) {
			return new HashMap<>();
		}
		
		List<Long> reviewerKeys = reviewers.stream()
				.map(IdentityRef::getKey).collect(Collectors.toList());
		return reviewResponseDao.getNumberOfReviews(position, reviewerKeys);
	}

	@Override
	public Set<Long> getApplicationReviewed(Position position, Identity identity) {
		return reviewResponseDao.getApplicationReviewed(position, identity);
	}
	
	@Override
	public PositionStatistics getReviewStatistics(Position position, Identity viewer, RecruitingPositionSecurityCallback secCallback) {
		boolean statisticsZeroBased = recruitingModule.isReviewStatisticsZeroBased();
		
		List<Identity> reviewerIdentities = reviewResponseDao.getReviewers(position);
		List<Identity> committee = getReviewers(position);
		reviewerIdentities.retainAll(committee);
		
		List<Reviewer> reviewers = reviewerIdentities.stream()
				.map(Reviewer::new)
				.collect(Collectors.toList());
		Map<Long,Reviewer> reviewersMap = reviewers.stream()
				.collect(Collectors.toMap(Reviewer::getKey, Function.identity(), (u, v) -> u));

		Collections.sort(reviewers);
		PositionStatistics positionStatistics = new PositionStatistics(position, reviewers);
		
		ApplicationStatistics currentAppStats = null;
		ApplicationStatisticElement currentElementStats = null;
		
		int counter = 0;
		List<ReviewResponse> responses;
		do {
			responses = reviewResponseDao.getResponses(position, counter, BATCH_SIZE);
			counter += responses.size();
			for(ReviewResponse response:responses) {
				if(!reviewerIdentities.contains(response.getReviewer())) {
					continue;// filter removed reviewer
				}
				
				ReviewResponseImpl responseImpl = (ReviewResponseImpl)response;
				if(currentAppStats == null || !currentAppStats.getApplication().getKey().equals(responseImpl.getApplication().getKey())) {
					currentAppStats = new ApplicationStatistics(responseImpl.getApplication());
					positionStatistics.addStatistics(currentAppStats);
				}
				
				if(currentElementStats == null || !currentElementStats.getElementDefinition().getKey().equals(responseImpl.getElement().getKey())) {
					currentElementStats = new ApplicationStatisticElement(responseImpl.getElement());
					currentAppStats.addStatisticsElement(currentElementStats);
				}
				
				Long reviewerKey = response.getReviewer().getKey();
				if(reviewerKey.equals(viewer.getKey())
						&& (response.getIntegerValue() != null || StringHelper.containsNonWhitespace(response.getStringValue()))) {
					currentAppStats.setHasReviewed(true);
				}
				
				if(currentElementStats.getElementDefinition().getType() == ReviewElementType.slider) {
					Integer val = response.getIntegerValue();
					if(val != null && val.intValue() < ReviewElementDefinition.MIN_SLIDER_VALUE) {
						val = (int)Math.round(ReviewElementDefinition.MIN_SLIDER_VALUE);
					}
					if(val != null && statisticsZeroBased) {
						val = val.intValue() - 1;
					}
					currentElementStats.addValue(val);
				} else if(currentElementStats.getElementDefinition().getType() == ReviewElementType.text) {
					Reviewer reviewer = reviewersMap.get(response.getReviewer().getKey());
					if(reviewer != null) {
						ApplicationTextCollectionElement element = new ApplicationTextCollectionElement(responseImpl.getElement(), reviewer, response.getStringValue());
						currentAppStats.addTextCollectionElement(element);
					}
				}
			}
		} while(responses.size() == BATCH_SIZE);
		
		boolean canSeeNotReviewed = secCallback.canViewReviews(false);
		if(!canSeeNotReviewed) {
			for(Iterator<ApplicationStatistics> appStatisticsIt=positionStatistics.getApplicationsStatistics().iterator(); appStatisticsIt.hasNext(); ) {
				ApplicationStatistics appStatistics = appStatisticsIt.next();
				if(!appStatistics.isHasReviewed()) {
					appStatisticsIt.remove();
				}
			}
		}

		return positionStatistics;
	}
}
