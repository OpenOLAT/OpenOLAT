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

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;

/**
 * 
 * Initial date: 22.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemAuditLogBuilderImpl implements QuestionItemAuditLogBuilder {
	
	private Long authorKey;
	private Action action;
	private Long questionItemKey;
	private String before;
	private String after;
	private String message;
	
	private final QPoolService qpoolService;
	
	public QuestionItemAuditLogBuilderImpl(Identity author, Action action) {
		this(CoreSpringFactory.getImpl(QPoolService.class), author, action);
	}
	
	QuestionItemAuditLogBuilderImpl(QPoolService qpoolService, Identity author, Action action) {
		this.qpoolService = qpoolService;
		if (author != null) {
			this.authorKey = author.getKey();
		}
		this.action = action;
	}

	@Override
	public QuestionItemAuditLogBuilder withBefore(QuestionItem item) {
		this.before = qpoolService.toAuditXml(item);
		this.questionItemKey = item.getKey();
		return this;
	}

	@Override
	public QuestionItemAuditLogBuilder withAfter(QuestionItem item) {
		this.after = qpoolService.toAuditXml(item);
		this.questionItemKey = item.getKey();
		return this;
	}

	@Override
	public QuestionItemAuditLogBuilder withMessage(String message) {
		this.message = message;
		return this;
	}

	@Override
	public QuestionItemAuditLog create() {
		QuestionItemAuditLogImpl auditLog = new QuestionItemAuditLogImpl();
		auditLog.setAuthorKey(authorKey);
		auditLog.setAction(action.name());
		auditLog.setQuestionItemKey(questionItemKey);
		auditLog.setAfter(after);
		auditLog.setBefore(before);
		auditLog.setMessage(message);
		return auditLog;
	}

}
