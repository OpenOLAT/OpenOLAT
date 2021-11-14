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
package org.olat.course.nodes.feed.blog;

import java.util.List;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BlogToolController extends BasicController implements Activateable2 {

	public static final String SUBSCRIPTION_SUBIDENTIFIER = "blog";
	
	private FeedMainController blogCtrl;
	private MessageController noBlogCtrl;
	
	@Autowired
	private RepositoryManager repositoryManager;

	public BlogToolController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		String blogSoftKey = userCourseEnv.getCourseEnvironment().getCourseConfig().getBlogSoftKey();
		RepositoryEntry blogEntry = repositoryManager.lookupRepositoryEntryBySoftkey(blogSoftKey, false);
		if (blogEntry != null) {
			
			String resName = CourseModule.getCourseTypeName();
			Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			OLATResourceable courseOres = OresHelper.createOLATResourceableInstance(resName, resId);
			SubscriptionContext subsContext = new SubscriptionContext(courseOres, SUBSCRIPTION_SUBIDENTIFIER);
			
			FeedSecurityCallback callback = userCourseEnv.isCourseReadOnly() || ureq.getUserSession().getRoles().isGuestOnly()
					? new ReadOnlyForumCallback()
					: new ToolSecurityCallback(userCourseEnv, subsContext);
			
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			blogCtrl = BlogUIFactory.getInstance(ureq.getLocale()).createMainController(blogEntry.getOlatResource(),
					ureq, wControl, callback, courseId, SUBSCRIPTION_SUBIDENTIFIER);
			listenTo(blogCtrl);
			
			putInitialPanel(blogCtrl.getInitialComponent());
		} else {
			String title = translate("tool.no.blog.title");
			String text = translate("tool.no.blog.text");
			noBlogCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, text);
			listenTo(noBlogCtrl);
			putInitialPanel(noBlogCtrl.getInitialComponent());
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (blogCtrl != null) {
			blogCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private static class ToolSecurityCallback implements FeedSecurityCallback {
		
		private final boolean admin;
		private final boolean coach;
		private SubscriptionContext subscriptionContext;

		private ToolSecurityCallback(UserCourseEnvironment userCourseEnv, SubscriptionContext subscriptionContext) {
			this.admin = userCourseEnv.isAdmin();
			this.coach = userCourseEnv.isCoach();
			this.subscriptionContext = subscriptionContext;
		}
		
		@Override
		public boolean mayEditMetadata() {
			return admin || coach;
		}

		@Override
		public boolean mayCreateItems() {
			return admin || coach;
		}

		@Override
		public boolean mayEditItems() {
			return admin || coach;
		}

		@Override
		public boolean mayEditOwnItems() {
			return true;
		}

		@Override
		public boolean mayDeleteItems() {
			return admin || coach;
		}

		@Override
		public boolean mayDeleteOwnItems() {
			return true;
		}

		@Override
		public boolean mayViewAllDrafts() {
			return admin || coach;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return subscriptionContext;
		}

		@Override
		public void setSubscriptionContext(SubscriptionContext subsContext) {
			this.subscriptionContext = subsContext;
		}

	}
	
	private static class ReadOnlyForumCallback implements FeedSecurityCallback {

		@Override
		public boolean mayEditMetadata() {
			return false;
		}

		@Override
		public boolean mayCreateItems() {
			return false;
		}

		@Override
		public boolean mayEditItems() {
			return false;
		}

		@Override
		public boolean mayEditOwnItems() {
			return false;
		}

		@Override
		public boolean mayDeleteItems() {
			return false;
		}

		@Override
		public boolean mayDeleteOwnItems() {
			return false;
		}

		@Override
		public boolean mayViewAllDrafts() {
			return false;
		}

		@Override
		public SubscriptionContext getSubscriptionContext() {
			return null;
		}

		@Override
		public void setSubscriptionContext(SubscriptionContext subsContext) {
			//
		}

	}

}
