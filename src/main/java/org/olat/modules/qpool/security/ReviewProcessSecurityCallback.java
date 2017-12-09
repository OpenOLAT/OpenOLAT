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
import org.olat.modules.qpool.ui.QuestionItemsSource;
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

	public static Collection<QuestionStatus> editableQuestionStates =
			Arrays.asList(QuestionStatus.draft, QuestionStatus.revised);

	private QuestionItemView itemView;
	private QuestionItemsSource questionItemSource;
	
	public void setItemView(QuestionItemView itemView) {
		this.itemView = itemView;
	}

	public void setQuestionItemSource(QuestionItemsSource questionItemSource) {
		this.questionItemSource = questionItemSource;
	}

	@Override
	public boolean canEditQuestion() {
		return editableQuestionStates.contains(itemView.getQuestionStatus())
				&& (itemView.isAuthor() || itemView.isEditableInPool() || itemView.isEditableInShare()) ;
	}

	@Override
	public boolean canEditMetadata() {
		return itemView.isAuthor() || itemView.isManager();
	}

	@Override
	public boolean canEditLifecycle() {
		return false;
	}

	@Override
	public boolean canStartReview() {
		return itemView.isReviewableFormat()
				&& itemView.isAuthor()
				&& editableQuestionStates.contains(itemView.getQuestionStatus());
	}

	@Override
	public boolean canReview() {
		return itemView.isReviewer() && QuestionStatus.review.equals(itemView.getQuestionStatus());
	}

	@Override
	public boolean canSetRevision() {
		return false;
	}

	@Override
	public boolean canSetFinal() {
		return false;
	}

	@Override
	public boolean canSetEndOfLife() {
		return itemView.isManager() && QuestionStatus.finalVersion.equals(itemView.getQuestionStatus());
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean canRemove() {
		return itemView.isAuthor() && questionItemSource.isRemoveEnabled();
	}

	@Override
	public boolean canRate() {
		return QuestionStatus.draft.equals(itemView.getQuestionStatus());
	}

}
