/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.dialog.security;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Initial date: 2 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
class DialogCallback implements DialogSecurityCallback {

	private final boolean isOlatAdmin;
	private final boolean isUploader;
	private final boolean isModerator;
	private final boolean isPoster;
	private final boolean isCourseEditor;
	private final boolean isGuestOnly;

	DialogCallback(UserCourseEnvironment userCourseEnv, boolean isUploader, boolean isModerator, boolean isPoster) {
		this.isOlatAdmin = userCourseEnv.isAdmin();
		this.isUploader = isUploader;
		this.isModerator = isModerator;
		this.isPoster = isPoster;
		GroupRoles role = GroupRoles.owner;
		if (userCourseEnv.isParticipant()) {
			role = GroupRoles.participant;
		} else if (userCourseEnv.isCoach()) {
			role = GroupRoles.coach;
		}
		this.isCourseEditor = userCourseEnv.getCourseEnvironment().getCourseGroupManager().hasRight(
				userCourseEnv.getIdentityEnvironment().getIdentity(),
				CourseRights.RIGHT_COURSEEDITOR, role);
		this.isGuestOnly = userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly();
	}

	@Override
	public boolean mayUsePseudonym() {
		return false;
	}

	@Override
	public boolean mayOpenNewThread() {
		if (isGuestOnly) return false;
		return isUploader || isOlatAdmin;
	}

	@Override
	public boolean mayReplyMessage() {
		if (isGuestOnly) return false;
		return isPoster || isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayEditOwnMessage() {
		return true;
	}

	@Override
	public boolean mayDeleteOwnMessage() {
		return true;
	}

	@Override
	public boolean mayEditMessageAsModerator() {
		if (isGuestOnly) return false;
		return isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {
		if (isGuestOnly) return false;
		return isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayArchiveForum() {
		if (isGuestOnly) return false;
		return true;
	}

	@Override
	public boolean mayFilterForUser() {
		if (isGuestOnly) return false;
		return isModerator|| isOlatAdmin;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

	@Override
	public boolean canCopyFile() {
		return isOlatAdmin || isCourseEditor;
	}

}
