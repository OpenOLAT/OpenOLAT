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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.ReviewDecision;

/**
 * 
 * Initial date: 07.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LowerLimitProviderTest {

	private static final int RATING_LOWER_LIMIT = 3;
	private static final float RATING_OK = 5;
	private static final float RATING_TO_LOW = 2;
	private static final int RATINGS_NEEDED = 10;
	private static final long RATINGS_TOO_LITTLE = 9;
	private static final long RATINGS_OK = 10;

	QuestionItemImpl item = new QuestionItemImpl();
	
	@Mock
	private QPoolService qpoolServiceMock;
	@Mock
	private QuestionPoolModule qpoolModuleMock;
	@Mock
	private CommentAndRatingService commentAndRatingServiceMock;
	
	@InjectMocks
	private LowerLimitProvider sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(qpoolModuleMock.getReviewDecisionLowerLimit()).thenReturn(RATING_LOWER_LIMIT);
		when(qpoolModuleMock.getReviewDecisionNumberOfRatings()).thenReturn(RATINGS_NEEDED);
	}

	@Test
	public void shouldSetRevisedIfRatingToLow() {
		ReviewDecision decision = sut.decideStatus(item, RATING_TO_LOW);
		
		assertThat(decision.isStatusChanged()).isTrue();
		assertThat(decision.getStatus()).isEqualTo(QuestionStatus.revised);
	}
	
	@Test
	public void shouldSetFinalIfEnoughRatings() {
		when(commentAndRatingServiceMock.countRatings(any(), any())).thenReturn(RATINGS_OK);
		
		ReviewDecision decision = sut.decideStatus(item, RATING_OK);
		
		assertThat(decision.isStatusChanged()).isTrue();
		assertThat(decision.getStatus()).isEqualTo(QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldNotChangeStatusIfNotEnoughRatings() {
		when(commentAndRatingServiceMock.countRatings(any(), any())).thenReturn(RATINGS_TOO_LITTLE);
		
		ReviewDecision decision = sut.decideStatus(item, RATING_OK);
		
		assertThat(decision.isStatusChanged()).isFalse();
		assertThat(decision.getStatus()).isEqualTo(item.getQuestionStatus());
	}
	
}
