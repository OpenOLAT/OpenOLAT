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
package org.olat.modules.qpool.security;

import java.util.Arrays;
import java.util.Collection;

import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ReviewService;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 04.12.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
@Scope("prototype")
public class ReviewProcessSecurityCallback implements QuestionItemSecurityCallback {
	
	private static final Collection<QuestionStatus> DELETABLE_STATES = Arrays.asList(
			QuestionStatus.draft,
			QuestionStatus.revised,
			QuestionStatus.endOfLife);

	private QuestionItemView itemView;
	private QuestionItemsSource questionItemSource;
	private boolean isAdmin = false;
	
	@Autowired
	private ReviewService reviewService;

	@Override
	public void setQuestionItemView(QuestionItemView itemView) {
		this.itemView = itemView;
	}

	@Override
	public void setQuestionItemSource(QuestionItemsSource questionItemSource) {
		this.questionItemSource = questionItemSource;
	}

	@Override
	public void setAdmin(boolean admin) {
		this.isAdmin = admin;
	}

	@Override
	public boolean canEditQuestion() {
		return reviewService.isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (isAdmin || itemView.isAuthor() || itemView.isEditableInPool() || itemView.isEditableInShare()) ;
	}

	@Override
	public boolean canEditMetadata() {
		return isAdmin || itemView.isAuthor() || itemView.isManager();
	}

	@Override
	public boolean canRemoveTaxonomy() {
		return QuestionStatus.draft.equals(itemView.getQuestionStatus())
				&& (isAdmin || itemView.isAuthor() || itemView.isManager());
	}

	@Override
	public boolean canStartReview() {
		return itemView.isReviewableFormat()
				&& reviewService.isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (isAdmin || itemView.isAuthor());
	}

	@Override
	public boolean canReviewNotStartable() {
		return !itemView.isReviewableFormat()
				&& reviewService.isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (isAdmin || itemView.isAuthor());
	}
	
	@Override
	public boolean canReview() {
		return itemView.isReviewableFormat()
				&& QuestionStatus.review.equals(itemView.getQuestionStatus())
				&& itemView.isReviewer();
	}

	@Override
	public boolean canSetDraft() {
		return isAdmin;
	}

	@Override
	public boolean canSetRevised() {
		return itemView.isReviewableFormat()
				&& (isAdmin || itemView.isManager());
	}

	@Override
	public boolean canSetReview() {
		return false;
	}

	@Override
	public boolean canSetFinal() {
		return itemView.isReviewableFormat()
				&& (isAdmin || itemView.isManager());
	}

	@Override
	public boolean canSetEndOfLife() {
		return isAdmin || itemView.isManager();
	}

	@Override
	public boolean canDelete() {
		return DELETABLE_STATES.contains(itemView.getQuestionStatus())
				&& (isAdmin || itemView.isManager());
	}

	@Override
	public boolean canRemove() {
		return questionItemSource.isRemoveEnabled()
				&& (isAdmin || itemView.isAuthor());
	}

	@Override
	public boolean canRate() {
		return QuestionStatus.draft.equals(itemView.getQuestionStatus());
	}

	@Override
	public boolean canChangeVersion() {
		return false;
	}

}
