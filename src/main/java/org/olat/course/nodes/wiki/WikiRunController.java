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

package org.olat.course.nodes.wiki;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.clone.CloneController;
import org.olat.core.gui.control.generic.clone.CloneLayoutControllerCreatorCallback;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.course.CourseFactory;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiReadOnlySecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: 
 * 
 * Initial Date: Oct 12, 2004
 * @author Guido Schnider
 */
public class WikiRunController extends BasicController implements Activateable2 {
	
	private CourseEnvironment courseEnv;
	private WikiMainController wikiCtr;
	private ModuleConfiguration config;
	private CloneController cloneCtr;
	

	public WikiRunController(WindowControl wControl, UserRequest ureq, WikiCourseNode wikiCourseNode,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		super(ureq, wControl);
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		this.config = wikiCourseNode.getModuleConfiguration();
		addLoggingResourceable(LoggingResourceable.wrap(wikiCourseNode));
		
		//get repository entry in "strict" mode
		RepositoryEntry re = WikiEditController.getWikiRepoReference(config, true);
		
		//check role
		UserSession usess = ureq.getUserSession();
		boolean isOlatAdmin = usess.getRoles().isOLATAdmin();
		boolean isGuestOnly = usess.getRoles().isGuestOnly();
		boolean isResourceOwner = false;
		if (isOlatAdmin) isResourceOwner = true;
		else {
			isResourceOwner = RepositoryManager.getInstance().isOwnerOfRepositoryEntry(ureq.getIdentity(), re);
		}
		
		// Check for jumping to certain wiki page
		BusinessControl bc = wControl.getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(courseEnv, wikiCourseNode);
		WikiSecurityCallback callback;
		if(userCourseEnv.isCourseReadOnly()) {
			callback = new WikiReadOnlySecurityCallback(isGuestOnly, (isOlatAdmin || isResourceOwner));
		} else {
			callback = new WikiSecurityCallbackImpl(ne, isOlatAdmin, isGuestOnly, false, isResourceOwner, subsContext);
		}
		
		if ( ce != null ) { //jump to a certain context
			OLATResourceable ores = ce.getOLATResourceable();
			String typeName = ores.getResourceableTypeName();
			String page = typeName.substring("page=".length());
			if(page.endsWith(":0")) {
				page = page.substring(0, page.length() - 2);
			}
			wikiCtr = WikiManager.getInstance().createWikiMainController(ureq, wControl, re.getOlatResource(), callback, page);
		} else {
			wikiCtr = WikiManager.getInstance().createWikiMainController(ureq, wControl, re.getOlatResource(), callback, null);
		}
		listenTo(wikiCtr);

		Controller wrappedCtr = TitledWrapperHelper.getWrapper(ureq, wControl, wikiCtr, wikiCourseNode, Wiki.CSS_CLASS_WIKI_ICON);
		
		CloneLayoutControllerCreatorCallback clccc = new CloneLayoutControllerCreatorCallback() {
			public ControllerCreator createLayoutControllerCreator(UserRequest uureq, final ControllerCreator contentControllerCreator) {
				return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(uureq, new ControllerCreator() {
					@SuppressWarnings("synthetic-access")
					public Controller createController(UserRequest lureq, WindowControl lwControl) {
						// wrapp in column layout, popup window needs a layout controller
						Controller ctr = contentControllerCreator.createController(lureq, lwControl);
						LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, ctr);
						layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), courseEnv));
						layoutCtr.addDisposableChildController(ctr);
						return layoutCtr;
					}
				});
			}
		};
		
		if (wrappedCtr instanceof CloneableController) {
			cloneCtr = new CloneController(ureq, getWindowControl(), (CloneableController)wrappedCtr, clccc);
			listenTo(cloneCtr);
			putInitialPanel(cloneCtr.getInitialComponent());
		} else {
			putInitialPanel(new Panel("uups.no.clone.controller"));			
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		wikiCtr.activate(ureq, entries, state);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//no events yet
	}
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
	
	public NodeRunConstructionResult createNodeRunConstructionResult() {
		TreeModel wikiTreeModel = wikiCtr.getAndUseExternalTree();
		String selNodeId = wikiTreeModel.getRootNode().getChildAt(0).getIdent();
		return new NodeRunConstructionResult(this, wikiTreeModel, selNodeId, wikiCtr);
	}
}