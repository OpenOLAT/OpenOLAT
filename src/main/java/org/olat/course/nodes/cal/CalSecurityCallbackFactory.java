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
package org.olat.course.nodes.cal;

import org.olat.core.CoreSpringFactory;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 24 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CalSecurityCallbackFactory {
	
	public static CalSecurityCallback createCourseCalendarCallback(UserCourseEnvironment userCourseEnv) {
		boolean privileged = !userCourseEnv.isCourseReadOnly() && userCourseEnv.isAdmin();
		
		return new CalSecurityCallbackImpl(privileged, privileged);
	}
	
	public static CalSecurityCallback createCourseNodeCallback(CalCourseNode courseNode, UserCourseEnvironment userCourseEnv, NodeEvaluation ne)  {
		boolean canWrite = !userCourseEnv.isCourseReadOnly() && hasEditRights(courseNode, userCourseEnv, ne);
		boolean canReadPrivateEvents = userCourseEnv.isAdmin() || userCourseEnv.isCoach() || userCourseEnv.isMemberParticipant();
		
		return new CalSecurityCallbackImpl(canWrite, canReadPrivateEvents);
	}
	
	private static boolean hasEditRights(CalCourseNode courseNode, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (userCourseEnv.isAdmin()) {
			return true;
		}
		
		if (courseNode.hasCustomPreConditions() && ne != null) {
			return ne.isCapabilityAccessible(CalCourseNode.EDIT_CONDITION_ID);
		}
		
		return CoreSpringFactory.getImpl(NodeRightService.class).isGranted(courseNode.getModuleConfiguration(),
				userCourseEnv, CalCourseNode.EDIT);
	}

	private static class CalSecurityCallbackImpl implements CalSecurityCallback {
		
		private final boolean canWrite;
		private final boolean canReadPrivateEvents;
		
		CalSecurityCallbackImpl(boolean canWrite, boolean canReadPrivateEvents) {
			this.canWrite = canWrite;
			this.canReadPrivateEvents = canReadPrivateEvents;
		}

		@Override
		public boolean canWrite() {
			return canWrite;
		}

		@Override
		public boolean canReadPrivateEvents() {
			return canReadPrivateEvents;
		}
		
	}
	
}
