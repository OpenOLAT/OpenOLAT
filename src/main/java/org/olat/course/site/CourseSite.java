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

import java.util.Locale;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.site.ui.DisposedCourseSiteRestartController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * based on Intranet-Site (goodsolutions) 
 * more config-options (see NetworkSiteDef / olat_extensions.xml) 
 * 
 * TODO:RH: maybe move back to SiteDef / Site for generic usage.
 * use repositoryuifactory instead of manually do things like, incrementing, building businesspath, etc...
 * 
 * <P>
 * Initial Date: 19.07.2005 <br>
 * 
 * @author Felix Jost
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class CourseSite extends AbstractSiteInstance {
	private NavElement origNavElem;
	private NavElement curNavElem;

	private final String repositorySoftKey;
	private boolean showToolController;
	private AutoCreator alternativeControllerIfNotLaunchable;
	private SiteSecurityCallback siteSecCallback;

	/**
	 * @param loc
	 * @param alternativeControllerIfNotLaunchable
	 * @param titleKeyPrefix
	 */
	public CourseSite(SiteDefinition siteDef, Locale loc, String repositorySoftKey, boolean showToolController, AutoCreator alternativeController, SiteSecurityCallback siteSecCallback, String titleKeyPrefix, String navIconCssClass) {
		super(siteDef);
		this.repositorySoftKey = repositorySoftKey;
		origNavElem = new DefaultNavElement(titleKeyPrefix, titleKeyPrefix, navIconCssClass);
		curNavElem = new DefaultNavElement(origNavElem);
		this.showToolController = showToolController;
		this.alternativeControllerIfNotLaunchable = alternativeController;
		this.siteSecCallback = siteSecCallback;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(entry == null) {
			return getAlternativeController(ureq, wControl);
		}

		MainLayoutController c;
		ICourse course = CourseFactory.loadCourse(entry.getOlatResource());

		// course-launch-state depending course-settings 
		boolean isAllowedToLaunch = rm.isAllowedToLaunch(ureq, entry);
		boolean hasAccess = false;
		
		if (isAllowedToLaunch) {
			// either check with securityCallback or use access-settings from course-nodes
			if (siteSecCallback != null) {
				hasAccess = siteSecCallback.isAllowedToLaunchSite(ureq);
			} else {
				// check within course: accessibility of course root node
				CourseNode rootNode = course.getRunStructure().getRootNode();
				UserCourseEnvironmentImpl uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course
						.getCourseEnvironment());
				NodeEvaluation nodeEval = rootNode.eval(uce.getConditionInterpreter(), new TreeEvaluation());
				boolean mayAccessWholeTreeUp = NavigationHandler.mayAccessWholeTreeUp(nodeEval);
				hasAccess = mayAccessWholeTreeUp && nodeEval.isVisible();
			}
		}
		
		// load course (admins always see content) or alternative controller if course is not launchable
		if (hasAccess || ureq.getUserSession().getRoles().isOLATAdmin()) {
			rm.incrementLaunchCounter(entry); 
			// build up the context path for linked course
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, entry, new StateSite(this), wControl, true) ;	
			
			c = new RunMainController(ureq, bwControl, course, false, true);
			BasicController disposeMsgController = new DisposedCourseSiteRestartController(ureq, wControl, entry);
			((RunMainController) c).setDisposedMsgController(disposeMsgController);
			if (!showToolController) {
				((RunMainController) c).disableToolController(true);
			}
		} else {
			// access restricted (not in group / author) -> show controller
			// defined in olat_extensions (type autoCreator)
			c = getAlternativeController(ureq, wControl);
		}
		return c;
	}
	
	protected MainLayoutController getAlternativeController(UserRequest ureq, WindowControl wControl) {
		MainLayoutController c;
		if (alternativeControllerIfNotLaunchable != null) {
			c = (MainLayoutController) alternativeControllerIfNotLaunchable
					.createController(ureq, wControl);
		} else {
			Translator pT = Util.createPackageTranslator(CourseSite.class, ureq.getLocale());
			MessageController msgController = MessageUIFactory.createErrorMessage(ureq, wControl, pT.translate("course.site.no.access.title"), pT.translate("course.site.no.access.text"));			
			Controller main3colsCtr = new LayoutMain3ColsController(ureq, wControl, null, null, msgController.getInitialComponent(), null);
			c = (MainLayoutController) main3colsCtr;
		}
		return c;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	@Override
	public boolean isKeepState() {
		return true;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#reset()
	 */
	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
