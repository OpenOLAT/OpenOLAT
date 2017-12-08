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
package org.olat.modules.qpool.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ReviewService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.ReviewDecision;
import org.olat.search.service.indexer.LifeFullIndexer;

/**
 * 
 * Initial date: 07.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolServiceImplTest {
	
	private static final int RATING_FIVE = 5;
	private static final int NO_RATING = 0;
	private static final String COMMENT = "comment";
	private static final String NO_COMMENT = "";
	
	@Mock
	private DB dbInstanceMock;
	@Mock
	private QuestionPoolModule qPoolModuleMock;
	@Mock
	private QuestionItemDAO questionItemDaoMock;
	@Mock
	private ReviewService reviewServiceMock;
	@Mock
	private LifeFullIndexer lifeIndexerMock;
	@Mock
	private CommentAndRatingService commentAndRatingServiceMock;
	@Mock
	private Identity identityDummy;
	
	private QuestionItemImpl item = new QuestionItemImpl();
	
	@InjectMocks
	private QuestionPoolServiceImpl sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(questionItemDaoMock.merge(item)).thenReturn(item);
	}
	
	@Test
	public void shouldSaveRating() {
		when(reviewServiceMock.decideStatus(any(), any())).thenReturn(new ReviewDecision(false, null));

		sut.rateItem(item, identityDummy, new Float(RATING_FIVE), null);
		
		verify(commentAndRatingServiceMock).createRating(identityDummy, item, null, RATING_FIVE);
	}
	
	@Test
	public void shouldSaveRatingOnlyIfPresent() {
		sut.rateItem(item, identityDummy, new Float(NO_RATING), null);
		
		verify(commentAndRatingServiceMock, never()).createRating(identityDummy, item, null, NO_RATING);	
	}
	
	@Test
	public void shouldSaveComment() {
		sut.rateItem(item, identityDummy, null, COMMENT);
		
		verify(commentAndRatingServiceMock).createComment(identityDummy, item, null, COMMENT);
	}
	
	@Test
	public void shouldSaveCommentOnlyIfPresent() {
		sut.rateItem(item, identityDummy, null, NO_COMMENT);
		
		verify(commentAndRatingServiceMock, never()).createComment(identityDummy, item, null, NO_COMMENT);	
	}
	
	@Test
	public void shouldSetNewStatusIfChanged() {
		Float rating = new Float(RATING_FIVE);
		QuestionStatus status = QuestionStatus.finalVersion;
		ReviewDecision decision = new ReviewDecision(true, status);
		when(reviewServiceMock.decideStatus(item, rating)).thenReturn(decision);

		sut.rateItem(item, identityDummy, rating, null);
		
		assertThat(item.getQuestionStatus()).isEqualTo(status);
	}
	
	@Test
	public void shouldSetNewStatusOnlyIfChanged() {
		QuestionStatus status = QuestionStatus.draft;
		item.setQuestionStatus(status);
		Float rating = new Float(RATING_FIVE);
		ReviewDecision decision = new ReviewDecision(false, QuestionStatus.finalVersion);
		when(reviewServiceMock.decideStatus(item, rating)).thenReturn(decision);

		sut.rateItem(item, identityDummy, rating, null);
		
		assertThat(item.getQuestionStatus()).isEqualTo(status);
	}
	
}
