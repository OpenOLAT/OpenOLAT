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

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.QuestionItemsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.12.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QPoolSecurityCallbackFactory {

	@Autowired
	private QuestionPoolModule qpoolModule;

	public QuestionItemSecurityCallback createQuestionItemSecurityCallback(Identity identity, QuestionItemView itemView,
			QuestionItemsSource questionItemSource) {
		QuestionItemSecurityCallback securityCallback;
		if (qpoolModule.isReviewProcessEnabled()) {
			securityCallback = createReviewProcessSecurityCallback(itemView, questionItemSource);
		} else {
			securityCallback = createProcesslessSecurityCallback(itemView, questionItemSource);
		}
		return securityCallback;
	}

	private QuestionItemSecurityCallback createReviewProcessSecurityCallback(QuestionItemView itemView,
			QuestionItemsSource questionItemSource) {
		ReviewProcessSecurityCallback reviewProcessSecurityCallback = CoreSpringFactory.getImpl(ReviewProcessSecurityCallback.class);
		reviewProcessSecurityCallback.setItemView(itemView);
		reviewProcessSecurityCallback.setQuestionItemSource(questionItemSource);
		return reviewProcessSecurityCallback;
	}

	private QuestionItemSecurityCallback createProcesslessSecurityCallback(QuestionItemView itemView,
			QuestionItemsSource questionItemSource) {
		ProcesslessSecurityCallback processlessSecurityCallback = CoreSpringFactory.getImpl(ProcesslessSecurityCallback.class);
		processlessSecurityCallback.setItemView(itemView);
		processlessSecurityCallback.setQuestionItemSource(questionItemSource);
		return processlessSecurityCallback;
	}

}