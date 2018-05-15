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
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QPoolSecurityCallback;
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

	public QuestionItemSecurityCallback createQuestionItemSecurityCallback(QuestionItemView itemView,
			QuestionItemsSource questionItemSource, Roles roles) {
		QuestionItemSecurityCallback securityCallback;
		if (qpoolModule.isReviewProcessEnabled()) {
			securityCallback = CoreSpringFactory.getImpl(ReviewProcessSecurityCallback.class);
		} else {
			securityCallback = CoreSpringFactory.getImpl(ProcesslessSecurityCallback.class);
		}
		securityCallback.setQuestionItemView(itemView);
		securityCallback.setQuestionItemSource(questionItemSource);
		securityCallback.setAdmin(roles.isOLATAdmin());
		securityCallback.setPoolAdmin(roles.isQPoolManager());
		return securityCallback;
	}

	public QPoolSecurityCallback createQPoolSecurityCallback(Roles roles) {
		QPoolSecurityCallback securityCallback = CoreSpringFactory.getImpl(QPoolSecurityCallbackImpl.class);
		securityCallback.setRoles(roles);
		return securityCallback;
	}

}