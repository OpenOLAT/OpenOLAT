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
package org.olat.modules.qpool.model;

import org.olat.modules.qpool.QuestionItemSecurityCallback;

/**
 * 
 * Initial date: 24.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemSecurityCallbackImpl implements QuestionItemSecurityCallback {

	private final boolean canEdit;
	private final boolean canReview;
	private final boolean canDelete;
	
	public QuestionItemSecurityCallbackImpl(boolean canEdit, boolean canReview, boolean canDelete) {
		super();
		this.canEdit = canEdit;
		this.canReview = canReview;
		this.canDelete = canDelete;
	}

	@Override
	public boolean canEditQuestion() {
		return canEdit;
	}

	@Override
	public boolean canEditMetadata() {
		return true;
	}

	@Override
	public boolean canEditLifecycle() {
		return false;
	}

	@Override
	public boolean canStartReview() {
		return true;
	}

	@Override
	public boolean canReview() {
		return canReview;
	}

	@Override
	public boolean canSetEndOfLife() {
		return true;
	}

	@Override
	public boolean canDelete() {
		return canDelete;
	}

}
