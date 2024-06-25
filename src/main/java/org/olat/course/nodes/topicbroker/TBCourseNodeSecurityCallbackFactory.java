/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.topicbroker;

import org.olat.core.CoreSpringFactory;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.topicbroker.TBSecurityCallback;

/**
 * 
 * Initial date: 10 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCourseNodeSecurityCallbackFactory {
	
	public static final TBSecurityCallback ADMIN_SEC_CALLBACK = new TBCourseNodeSecurityCallback(true, true, true, true);
	private static final TBSecurityCallback RO_SEC_CALLBACK = new TBCourseNodeSecurityCallback(false, false, false, false);
	
	public static final TBSecurityCallback createSecurityCallback(TopicBrokerCourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment) {
		if (userCourseEnvironment.isCourseReadOnly()) {
			return RO_SEC_CALLBACK;
		}
		if (userCourseEnvironment.isAdmin()) {
			return ADMIN_SEC_CALLBACK;
		}
		
		NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
		ModuleConfiguration configs = courseNode.getModuleConfiguration();
		boolean canEditTopics = nodeRightService.isGranted(configs, userCourseEnvironment, TopicBrokerCourseNode.EDIT_TOPIC);
		boolean canEditSelections = nodeRightService.isGranted(configs, userCourseEnvironment, TopicBrokerCourseNode.EDIT_SELECTIONS);
		
		return new TBCourseNodeSecurityCallback(true, canEditTopics, canEditSelections, false);
	}
	
	public static final class TBCourseNodeSecurityCallback implements TBSecurityCallback {
		
		private final boolean canEdit;
		private final boolean canEditTopics;
		private final boolean canEditSelections;
		private final boolean canStartManualEnrollment;
		
		public TBCourseNodeSecurityCallback(boolean canEdit, boolean canEditTopics, boolean canEditSelections,
				boolean canStartManualEnrollment) {
			this.canEdit = canEdit;
			this.canEditTopics = canEditTopics;
			this.canEditSelections = canEditSelections;
			this.canStartManualEnrollment = canStartManualEnrollment;
		}
		
		@Override
		public boolean canEdit() {
			return canEdit;
		}

		@Override
		public boolean canEditTopics() {
			return canEditTopics;
		}

		@Override
		public boolean canEditSelections() {
			return canEditSelections;
		}

		@Override
		public boolean canStartManualEnrollment() {
			return canStartManualEnrollment;
		}

	}

}
