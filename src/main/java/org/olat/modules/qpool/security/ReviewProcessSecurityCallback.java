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
import java.util.List;

import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
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
	
	private static final List<QuestionStatus> EDITABLE_STATES = Arrays.asList(
			QuestionStatus.draft,
			QuestionStatus.revised);
	private static final Collection<QuestionStatus> DELETABLE_STATES = Arrays.asList(
			QuestionStatus.draft,
			QuestionStatus.revised,
			QuestionStatus.endOfLife);

	private QuestionItemView itemView;
	private QuestionItemsSource questionItemSource;
	private boolean admin = false;
	private boolean poolAdmin = false;
	
	@Autowired
	private QuestionPoolModule qpoolModule;

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
		this.admin = admin;
	}

	@Override
	public void setPoolAdmin(boolean poolAdmin) {
		this.poolAdmin = poolAdmin;
	}

	@Override
	public boolean canEditQuestion() {
		return isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (admin || itemView.isAuthor() || itemView.isEditableInPool() || itemView.isEditableInShare()) ;
	}

	@Override
	public boolean canEditMetadata() {
		return admin || itemView.isAuthor() || itemView.isManager() || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canRemoveTaxonomy() {
		return QuestionStatus.draft.equals(itemView.getQuestionStatus())
				&& (admin
						|| itemView.isAuthor()
						|| itemView.isManager()
						|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata()));
	}

	@Override
	public boolean canStartReview() {
		return itemView.isReviewableFormat()
				&& isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (admin || itemView.isAuthor());
	}

	@Override
	public boolean canReviewNotStartable() {
		return !itemView.isReviewableFormat()
				&& isEditableQuestionStatus(itemView.getQuestionStatus())
				&& (admin || itemView.isAuthor());
	}
	
	@Override
	public boolean canReview() {
		return itemView.isReviewableFormat()
				&& QuestionStatus.review.equals(itemView.getQuestionStatus())
				&& itemView.isReviewer();
	}

	@Override
	public boolean canViewReviews() {
		return admin || itemView.isAuthor() || itemView.isManager();
	}

	@Override
	public boolean canSetDraft() {
		return admin;
	}

	@Override
	public boolean canSetRevised() {
		return itemView.isReviewableFormat()
				&& (admin || itemView.isManager() || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus()));
	}

	@Override
	public boolean canSetReview() {
		return false;
	}

	@Override
	public boolean canSetFinal() {
		return itemView.isReviewableFormat()
				&& (admin || itemView.isManager() || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus()));
	}

	@Override
	public boolean canSetEndOfLife() {
		return admin || itemView.isManager() || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canDelete() {
		return isDeletableQuestionStatus(itemView.getQuestionStatus())
				&& (admin || itemView.isAuthor());
	}

	@Override
	public boolean canRemove() {
		return  questionItemSource.isRemoveEnabled()
				|| admin
				|| poolAdmin
				|| itemView.isAuthor();
	}

	@Override
	public boolean canRate() {
		return QuestionStatus.draft.equals(itemView.getQuestionStatus());
	}

	@Override
	public boolean canChangeVersion() {
		return !itemView.isReviewableFormat()
				&& (admin
						|| itemView.isAuthor()
						|| itemView.isManager()
						|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata()));
	}

	@Override
	public boolean canEditAuthors() {
		return admin || itemView.isAuthor() || itemView.isManager() || (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canExportAuditLog() {
		return admin || poolAdmin;
	}
	
	private boolean isEditableQuestionStatus(QuestionStatus status) {
		return EDITABLE_STATES.contains(status);
	}
	
	@Override
	public boolean isDeletableQuestionStatus() {
		return isDeletableQuestionStatus(itemView.getQuestionStatus());
	}
	
	private boolean isDeletableQuestionStatus(QuestionStatus status) {
		return DELETABLE_STATES.contains(status);
	}

}
