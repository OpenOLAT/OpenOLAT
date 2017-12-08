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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.model.QuestionItemImpl;

/**
 * 
 * Initial date: 08.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewServiceImplTest {

	QuestionItemImpl item = new QuestionItemImpl();
	
	@Mock
	private QuestionPoolModule qpoolModuleMock;
	@Mock
	private CommentAndRatingService commentAndRatingService;
	
	@InjectMocks
	private ReviewServiceImpl sut;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldIfStatusChangesFromDraftToDraftNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.draft, QuestionStatus.draft);
	}
	
	@Test
	public void shouldIfStatusChangesFromDraftToRevisedNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.draft, QuestionStatus.revised);
	}
	
	@Test
	public void shouldIfStatusChangesFromDraftToReviewBeStarting() {
		shouldBeStarting(QuestionStatus.draft, QuestionStatus.review);
	}
	
	@Test
	public void shouldIfStatusChangesFromDraftToFinalBeStarting() {
		shouldBeStarting(QuestionStatus.draft, QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldIfStatusChangesFromDraftToEndOfLinfNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.draft, QuestionStatus.endOfLife);
	}
	
	@Test
	public void shouldIfStatusChangesFromRevisedToDraftNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.revised, QuestionStatus.draft);
	}
	
	@Test
	public void shouldIfStatusChangesFromRevisedToRevisedNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.revised, QuestionStatus.revised);
	}
	
	@Test
	public void shouldIfStatusChangesFromRevisedToReviewBeStarting() {
		shouldBeStarting(QuestionStatus.revised, QuestionStatus.review);
	}
	
	@Test
	public void shouldIfStatusChangesFromRevisedToFinalBeStarting() {
		shouldBeStarting(QuestionStatus.revised, QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldIfStatusChangesFromRevisedToEndOfLinfNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.revised, QuestionStatus.endOfLife);
	}
	
	@Test
	public void shouldIfStatusChangesFromReviewToDraftNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.review, QuestionStatus.draft);
	}
	
	@Test
	public void shouldIfStatusChangesFromReviewToRevisedNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.review, QuestionStatus.revised);
	}
	
	@Test
	public void shouldIfStatusChangesFromReviewToReviewBeStarting() {
		shouldNotBeStarting(QuestionStatus.review, QuestionStatus.review);
	}
	
	@Test
	public void shouldIfStatusChangesFromReviewToFinalBeStarting() {
		shouldNotBeStarting(QuestionStatus.review, QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldIfStatusChangesFromReviewToEndOfLinfNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.review, QuestionStatus.endOfLife);
	}
	
	@Test
	public void shouldIfStatusChangesFromFinalToDraftNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.finalVersion, QuestionStatus.draft);
	}
	
	@Test
	public void shouldIfStatusChangesFromFinalToRevisedNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.finalVersion, QuestionStatus.revised);
	}
	
	@Test
	public void shouldIfStatusChangesFromFinalToReviewBeStarting() {
		shouldBeStarting(QuestionStatus.finalVersion, QuestionStatus.review);
	}
	
	@Test
	public void shouldIfStatusChangesFromFinalToFinalBeStarting() {
		shouldNotBeStarting(QuestionStatus.finalVersion, QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldIfStatusChangesFromFinalToEndOfLinfNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.finalVersion, QuestionStatus.endOfLife);
	}

	@Test
	public void shouldIfStatusChangesFromEndOfLifeToDraftNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.endOfLife, QuestionStatus.draft);
	}
	
	@Test
	public void shouldIfStatusChangesFromEndOfLifeToRevisedNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.endOfLife, QuestionStatus.revised);
	}
	
	@Test
	public void shouldIfStatusChangesFromEndOfLifeToReviewBeStarting() {
		shouldBeStarting(QuestionStatus.endOfLife, QuestionStatus.review);
	}
	
	@Test
	public void shouldIfStatusChangesFromEndOfLifeToFinalBeStarting() {
		shouldNotBeStarting(QuestionStatus.endOfLife, QuestionStatus.finalVersion);
	}
	
	@Test
	public void shouldIfStatusChangesFromEndOfLifeToEndOfLinfNotBeStarting() {
		shouldNotBeStarting(QuestionStatus.endOfLife, QuestionStatus.endOfLife);
	}
	
	private void shouldBeStarting(QuestionStatus previousStatus, QuestionStatus newStatus) {
		boolean isReviewStarting = sut.isReviewStarting(previousStatus, newStatus);
		
		assertThat(isReviewStarting).isTrue();
	}
	
	private void shouldNotBeStarting(QuestionStatus previousStatus, QuestionStatus newStatus) {
		boolean isReviewStarting = sut.isReviewStarting(previousStatus, newStatus);
		
		assertThat(isReviewStarting).isFalse();
	}
	
	@Test
	public void shouldIncrementVersionByOne() {
		item.setItemVersion("3");
		
		sut.incrementVersion(item);
		
		assertThat(item.getItemVersion()).isEqualTo("4");
	}
	
	@Test
	public void shouldIncrementNonNumericVersionsToOne() {
		item.setItemVersion("my non numeric version");
		
		sut.incrementVersion(item);
		
		assertThat(item.getItemVersion()).isEqualTo("1");
	}
	
	@Test
	public void shouldDeleteAllRatings() {
		when(qpoolModuleMock.isReviewProcessEnabled()).thenReturn(true);
		
		sut.startReview(item);
		
		verify(commentAndRatingService).deleteAllIgnoringSubPath(item);		
	}
	
	@Test
	public void shouldDeleteAllRatingsOnlyIfReviewProcessEnabled() {
		when(qpoolModuleMock.isReviewProcessEnabled()).thenReturn(false);
		
		sut.startReview(item);
		
		verify(commentAndRatingService, never()).deleteAllIgnoringSubPath(item);		
	}
 }
