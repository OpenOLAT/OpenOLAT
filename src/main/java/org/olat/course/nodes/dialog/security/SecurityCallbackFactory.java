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
package org.olat.course.nodes.dialog.security;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 2 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SecurityCallbackFactory {

	/**
	 * Creates a DialogSecurityCallback without a subscription context.
	 *
	 * @param courseNode
	 * @param userCourseEnv
	 * @param nodeEvaluation
	 * @return
	 */
	public static DialogSecurityCallback create(DialogCourseNode courseNode, UserCourseEnvironment userCourseEnv,
			NodeEvaluation nodeEvaluation) {
		
		DialogSecurityCallbackProvider provider = courseNode.hasCustomPreConditions()
				? new NodeEvaluationSecurityCallbackProvider(nodeEvaluation)
				: new ConfigSecurityCallbackProvider(userCourseEnv, courseNode.getModuleConfiguration());
		
		if (userCourseEnv.isCourseReadOnly()) {
			return new ReadOnlyDialogCallback(userCourseEnv, provider.isModerator());
		}
		return new DialogCallback(userCourseEnv, provider.isUploader(), provider.isModerator(), provider.isPoster());
	}
	
	/**
	 * Decorates the DialogSecurityCallback with a subscription context.
	 *
	 * @param secCallback
	 * @param subsContext
	 * @return
	 */
	public static DialogSecurityCallback create(DialogSecurityCallback secCallback, SubscriptionContext subsContext) {
		return new SubscriptionContextCallback(secCallback, subsContext);
	}
	

}
