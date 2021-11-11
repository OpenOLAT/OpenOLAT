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
package org.olat.course.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.util.UserSession;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.run.CourseRuntimeController;
import org.olat.course.run.RunMainController;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;

/**
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class CourseSite extends AbstractSiteInstance {
	private NavElement origNavElem;
	private NavElement curNavElem;

	private final Long repoEntryKey;
	private boolean showToolController;
	private SiteSecurityCallback siteSecCallback;

	/**
	 * 
	 * @param siteDef The site definitionn
	 * @param repoEntryKey The primary key of the repository entry of the course
	 * @param showToolController true if the tools are shown
	 * @param siteSecCallback The security callback
	 * @param titleKeyPrefix Prefix for the title
	 * @param navIconCssClass An CSS class for icon
	 */
	public CourseSite(SiteDefinition siteDef, Long repoEntryKey, boolean showToolController,
			SiteSecurityCallback siteSecCallback, String titleKeyPrefix, String navIconCssClass) {
		super(siteDef);
		this.repoEntryKey = repoEntryKey;
		String businessPath = "[RepositoryEntry:" + repoEntryKey + "]";
		origNavElem = new DefaultNavElement(businessPath, titleKeyPrefix, titleKeyPrefix, navIconCssClass);
		curNavElem = new DefaultNavElement(origNavElem);
		this.showToolController = showToolController;
		this.siteSecCallback = siteSecCallback;
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		RepositoryManager rm = CoreSpringFactory.getImpl(RepositoryManager.class);
		RepositoryEntry entry = rm.lookupRepositoryEntry(repoEntryKey, false);
		if(entry == null) {
			return getAlternativeController(ureq, wControl, config);
		}

		MainLayoutController c;
		ICourse course = CourseFactory.loadCourse(entry);
		UserSession usess = ureq.getUserSession();

		// course-launch-state depending course-settings
		RepositoryEntrySecurity reSecurity = rm.isAllowed(ureq, entry);
		boolean isAllowedToLaunch = reSecurity.canLaunch();
		boolean hasAccess = false;
		if (isAllowedToLaunch) {
			// either check with securityCallback or use access-settings from course-nodes
			if (siteSecCallback != null) {
				hasAccess = siteSecCallback.isAllowedToLaunchSite(ureq);
			} else if(usess.isInAssessmentModeProcess() && !usess.matchPrimaryLockResource(course)){
				hasAccess = false;
			} else {
				// check within course: accessibility of course root node
				UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course
						.getCourseEnvironment());
				NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
				CourseTreeNode courseTreeNode = (CourseTreeNode)nodeAccessService.getCourseTreeModelBuilder(uce)
						.withFilter(AccessibleFilter.create())
						.build()
						.getRootNode();
				hasAccess = courseTreeNode.isVisible() && courseTreeNode.isAccessible();
			}
		}
		
		// load course (admins always see content) or alternative controller if course is not launchable
		if (hasAccess || reSecurity.isEntryAdmin()) {
			RepositoryService rs = CoreSpringFactory.getImpl(RepositoryService.class);
			rs.incrementLaunchCounter(entry); 
			// build up the context path for linked course
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, entry, new StateSite(this), wControl, true);	
			CourseRuntimeController runCtr = new CourseRuntimeController(ureq, bwControl, entry, reSecurity,
				(uureq, wwControl, toolbarPanel, re, security, assessmentMode) -> 
					new RunMainController(uureq, wwControl, toolbarPanel, CourseFactory.loadCourse(re), re, security, assessmentMode), false, true);
			// Configure run controller
			// a: don't show close link, is opened as site not tab
			runCtr.setCourseCloseEnabled(false);
			// b: don't show toolbar
			if (!showToolController && !reSecurity.isEntryAdmin()) {
				runCtr.setToolControllerEnabled(false);
			}
			c = runCtr;
		} else {
			// access restricted (not in group / author) -> show controller
			// defined in olat_extensions (type autoCreator)
			c = getAlternativeController(ureq, wControl, config);
		}
		return c;
	}

	@Override
	public boolean isKeepState() {
		return true;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
