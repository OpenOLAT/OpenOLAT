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
 * Initial date: 01.07.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
package org.olat.course.config;

import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntrySecurity;

public enum ExternalToolVisibility {

	participant("_VIS_PARTICIPANT", false) {
		@Override
		boolean appliesTo(RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv) {
			return userCourseEnv != null && userCourseEnv.isParticipant();
		}
	},
	coach("_VIS_COACH", false) {
		@Override
		boolean appliesTo(RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv) {
			return reSecurity.isCoach();
		}
	},
	owner("_VIS_OWNER", true) {
		@Override
		boolean appliesTo(RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv) {
			return reSecurity.isEntryAdmin() || reSecurity.isOwner() || reSecurity.isPrincipal()
					|| reSecurity.isLearnResourceManager() || reSecurity.isCurriculumManager();
		}
	};

	private final String suffix;
	private final boolean defaultVisible;

	ExternalToolVisibility(String suffix, boolean defaultVisible) {
		this.suffix = suffix;
		this.defaultVisible = defaultVisible;
	}

	public String suffix() {
		return suffix;
	}

	public boolean defaultVisible() {
		return defaultVisible;
	}

	abstract boolean appliesTo(RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv);

	public static boolean isVisible(CourseConfig courseConfig, int toolIndex, RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv) {
		if (!courseConfig.isExternalToolEnabled(toolIndex)) {
			return false;
		}
		for (ExternalToolVisibility v : values()) {
			if (v.appliesTo(reSecurity, userCourseEnv) && courseConfig.isExternalToolVisible(toolIndex, v)) {
				return true;
			}
		}
		return false;
	}
}
