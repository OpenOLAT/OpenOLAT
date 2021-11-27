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

import org.olat.modules.qpool.ui.QuestionItemsSource;

/**
 * 
 * Initial date: 22.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItemSecurityCallback extends MetadataSecurityCallback {
	
	public void setQuestionItemView(QuestionItemView itemView);
	
	public void setQuestionItemSource(QuestionItemsSource questionItemSource);
	
	public void setAdmin(boolean admin);
	
	public void setPoolAdmin(boolean poolAdmin);

	public boolean isDeletableQuestionStatus();
	
	public boolean canEditQuestion();
	
	public boolean canStartReview();
	
	public boolean canReviewNotStartable();

	public boolean canReview();

	public boolean canSetDraft();

	public boolean canSetReview();

	public boolean canSetRevised();

	public boolean canSetFinal();

	public boolean canSetEndOfLife();

	public boolean canDelete();
	
	public boolean canRemove();

	/**
	 * Can a user rate the question item outside the review process?
	 */
	public boolean canRate();
	
	public boolean canExportAuditLog();

}
