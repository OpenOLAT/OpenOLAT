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
package org.olat.course.nodes;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.feed.FeedNodeEditController;
import org.olat.course.nodes.feed.FeedNodeSecurityCallback;
import org.olat.course.nodes.feed.FeedPeekviewController;
import org.olat.course.nodes.feed.blog.BlogNodeEditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.FeedReadOnlySecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * The blog course node.
 * 
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogCourseNode extends AbstractFeedCourseNode {
	public static final String TYPE = FeedManager.KIND_BLOG;

	/**
	 * @param type
	 */
	public BlogCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl,  BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		TabbableController blogChildController = new BlogNodeEditController(this, course, euce, ureq, wControl);
		return new NodeEditController(ureq, wControl, course, chosenNode, euce, blogChildController);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		RepositoryEntry entry = getReferencedRepositoryEntry();
		FeedSecurityCallback callback = getFeedSecurityCallback(ureq, entry, userCourseEnv, nodeSecCallback.getNodeEvaluation());
		
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), this); 
		callback.setSubscriptionContext(subsContext);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(this));
		Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		FeedMainController blogCtr = BlogUIFactory.getInstance(ureq.getLocale())
				.createMainController(entry.getOlatResource(), ureq, wControl, callback, courseId, getIdent());
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(nodecmd);
		blogCtr.activate(ureq, entries, null);
		Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, blogCtr, this, "o_blog_icon");
		return new NodeRunConstructionResult(wrapperCtrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			// Create a feed peekview controller that shows the latest two entries
			RepositoryEntry entry = getReferencedRepositoryEntry();
			FeedSecurityCallback callback = getFeedSecurityCallback(ureq, entry, userCourseEnv, nodeSecCallback.getNodeEvaluation());
			FeedUIFactory uiFactory = BlogUIFactory.getInstance(ureq.getLocale());
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			return new FeedPeekviewController(entry.getOlatResource(), ureq, wControl, callback, courseId, getIdent(), uiFactory, 2, "o_blog_peekview");
		} else {
			// use standard peekview
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
		}
	}
	
	private FeedSecurityCallback getFeedSecurityCallback(UserRequest ureq, RepositoryEntry entry,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		FeedSecurityCallback callback;
		if(userCourseEnv.isCourseReadOnly()) {
			callback = new FeedReadOnlySecurityCallback();
		} else {
			Roles roles = ureq.getUserSession().getRoles();
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);

			boolean isGuest = roles.isGuestOnly();
			boolean isAdmin = userCourseEnv.isAdmin();
			boolean isOwner = !isGuest && repositoryService.hasRole(ureq.getIdentity(), entry, GroupRoles.owner.name());
			callback = new FeedNodeSecurityCallback(ne, isAdmin, isOwner, isGuest);
		}
		return callback;
	}

	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		String translatorStr = Util.getPackageName(BlogNodeEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription status = StatusDescription.NOERROR;
		boolean invalid = config.get(CONFIG_KEY_REPOSITORY_SOFTKEY) == null;
		if (invalid) {
			String[] params = new String[] { this.getShortTitle() };
			String shortKey = "error.no.reference.short";
			String longKey = "error.no.reference.long";
			String translationPackage = Util.getPackageName(BlogNodeEditController.class);
			status = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translationPackage);
			status.setDescriptionForUnit(getIdent());
			// Set which pane is affected by error
			status.setActivateableViewIdentifier(FeedNodeEditController.PANE_TAB_FEED);
		}
		return status;
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		if(withReferences) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(BlogFileResource.TYPE_NAME);
			importFeed(handler, importDirectory, owner, organisation, locale);
		} else {
			FeedNodeEditController.removeReference(getModuleConfiguration());
		}
	}
}
