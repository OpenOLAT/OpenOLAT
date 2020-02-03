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

import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 3 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigSecurityCallbackProvider implements DialogSecurityCallbackProvider {

	private final UserCourseEnvironment userCourseEnv;
	private final ModuleConfiguration moduleConfig;

	public ConfigSecurityCallbackProvider(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfig) {
		this.userCourseEnv = userCourseEnv;
		this.moduleConfig = moduleConfig;
	}

	@Override
	public boolean isUploader() {
		if ((moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_COACH) && userCourseEnv.isCoach())
				|| (moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_PARTICIPANT) && userCourseEnv.isParticipant())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isModerator() {
		if (moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_MODERATE_BY_COACH) && userCourseEnv.isCoach()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPoster() {
		if ((moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_POST_BY_COACH) && userCourseEnv.isCoach())
				|| (moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_POST_BY_PARTICIPANT) && userCourseEnv.isParticipant())) {
			return true;
		}
		return false;
	}

}
