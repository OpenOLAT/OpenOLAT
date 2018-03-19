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

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 21.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionItemAuditLog extends CreateInfo {
	
	public String getAction();
	
	public String getBefore();

	public String getAfter();

	public String getLicenseBefore();

	public String getLicenseAfter();

	public String getMessage();
	
	public Long getQuestionItemKey();
	
	public Long getAuthorKey();
	
	public enum Action {
		CREATE_QUESTION_ITEM_NEW,
		CREATE_QUESTION_ITEM_BY_COPY,
		CREATE_QUESTION_ITEM_BY_CONVERSION,
		CREATE_QUESTION_ITEM_BY_IMPORT,
		UPDATE_QUESTION_ITEM_METADATA,
		UPDATE_QUESTION,
		DELETE_QUESTION_ITEM,
		REVIEW_QUESTION_ITEM,
		STATUS_CHANGED
	}

}
