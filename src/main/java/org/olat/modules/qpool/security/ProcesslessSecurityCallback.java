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

import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 04.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
@Scope("prototype")
public class ProcesslessSecurityCallback implements QuestionItemSecurityCallback {

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
		return admin || itemView.isAuthor() || itemView.isEditableInPool() || itemView.isEditableInShare();
	}

	@Override
	public boolean canEditMetadata() {
		return admin 
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canRemoveTaxonomy() {
		return admin 
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canStartReview() {
		return false;
	}

	@Override
	public boolean canReviewNotStartable() {
		return false;
	}

	@Override
	public boolean canReview() {
		return false;
	}

	@Override
	public boolean canViewReviews() {
		return false;
	}

	@Override
	public boolean canSetDraft() {
		return admin
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canSetRevised() {
		return admin
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canSetReview() {
		return admin
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canSetFinal() {
		return admin
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canSetEndOfLife() {
		return admin
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditStatus());
	}

	@Override
	public boolean canDelete() {
		return admin || itemView.isAuthor() || itemView.isEditableInPool() || itemView.isEditableInShare();
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
		return true;
	}

	@Override
	public boolean canChangeVersion() {
		return admin 
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canEditAuthors() {
		return admin 
				|| itemView.isAuthor()
				|| itemView.isEditableInPool()
				|| itemView.isEditableInShare()
				|| (poolAdmin && qpoolModule.isPoolAdminAllowedToEditMetadata());
	}

	@Override
	public boolean canExportAuditLog() {
		return admin || poolAdmin;
	}

	@Override
	public boolean isDeletableQuestionStatus() {
		return true;
	}
}
