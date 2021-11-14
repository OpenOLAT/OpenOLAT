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
package org.olat.course.nodes.fo;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.ForumController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FOToolController extends BasicController {
	
	private static final String SUBSCRIPTION_SUBIDENTIFIER = "forum";

	private final ForumController forumCtrl;

	@Autowired
	private ForumManager forumManager;

	public FOToolController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Forum forum = getOrCreateForum(courseEntry);
		
		String resName = CourseModule.getCourseTypeName();
		Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(resName, resId);
		SubscriptionContext forumSubContext = new SubscriptionContext(courseOres, SUBSCRIPTION_SUBIDENTIFIER);
		
		ForumCallback forumCallback = userCourseEnv.isCourseReadOnly() || ureq.getUserSession().getRoles().isGuestOnly()
				? new ReadOnlyForumCallback(userCourseEnv)
				: new ToolSecurityCallback(userCourseEnv, forumSubContext);
				
		forumCtrl = new ForumController(ureq, wControl, forum, forumCallback, true);
		listenTo(forumCtrl);
		putInitialPanel(forumCtrl.getInitialComponent());
	}

	private Forum getOrCreateForum(RepositoryEntry courseEntry) {
		Forum forum = forumManager.loadForum(courseEntry);
		if (forum == null) {
			forum = forumManager.addAForum(courseEntry);
		}
		return forum;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private static class ToolSecurityCallback implements ForumCallback {
		
		private final boolean admin;
		private final boolean coach;
		private final SubscriptionContext subscriptionContext;

		private ToolSecurityCallback(UserCourseEnvironment userCourseEnv, SubscriptionContext subscriptionContext) {
			this.admin = userCourseEnv.isAdmin();
			this.coach = userCourseEnv.isCoach();
			this.subscriptionContext = subscriptionContext;
		}

		@Override
		public boolean mayUsePseudonym() {
			return false;
		}

		@Override
		public boolean mayOpenNewThread() {
			return true;
		}

		@Override
		public boolean mayReplyMessage() {
			return true;
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
			return admin || coach;
		}

		@Override
		public boolean mayDeleteMessageAsModerator() {
			return admin || coach;
		}

		@Override
		public boolean mayArchiveForum() {
			return false;
		}

		@Override
		public boolean mayFilterForUser() {
			return admin || coach;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subscriptionContext;
		}
		
	}
	
	private static class ReadOnlyForumCallback implements ForumCallback {
		
		private final boolean admin;
		private final boolean coach;
		
		public ReadOnlyForumCallback(UserCourseEnvironment userCourseEnv) {
			this.admin = userCourseEnv.isAdmin();
			this.coach = userCourseEnv.isCoach();
		}

		@Override
		public boolean mayUsePseudonym() {
			return false;
		}

		@Override
		public boolean mayOpenNewThread() {
			return false;
		}

		@Override
		public boolean mayReplyMessage() {
			return false;
		}
		
		@Override
		public boolean mayEditOwnMessage() {
			return false;
		}

		@Override
		public boolean mayDeleteOwnMessage() {
			return false;
		}

		@Override
		public boolean mayEditMessageAsModerator() {
			return false;
		}

		@Override
		public boolean mayDeleteMessageAsModerator() {
			return false;
		}

		@Override
		public boolean mayArchiveForum() {
			return false;
		}

		@Override
		public boolean mayFilterForUser() {
			return admin || coach;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return null;
		}
	}

}
