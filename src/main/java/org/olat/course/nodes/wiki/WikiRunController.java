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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.DryRunAssessmentProvider;
import org.olat.modules.wiki.PersistingAssessmentProvider;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiAssessmentProvider;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiReadOnlySecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallback;
import org.olat.modules.wiki.WikiSecurityCallbackImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private NodeRightService nodeRightsService;

	public WikiRunController(WindowControl wControl, UserRequest ureq, WikiCourseNode wikiCourseNode,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		super(ureq, wControl);
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		this.config = wikiCourseNode.getModuleConfiguration();
		addLoggingResourceable(LoggingResourceable.wrap(wikiCourseNode));
		
		//get repository entry in "strict" mode
		RepositoryEntry wikiEntry = WikiEditController.getWikiRepoReference(config, true);
		
		//check role
		UserSession usess = ureq.getUserSession();
		boolean isAdmininstrator = userCourseEnv.isAdmin();
		boolean isGuestOnly = usess.getRoles().isGuestOnly();
		boolean isResourceOwner = isAdmininstrator || repositoryService.hasRole(getIdentity(), wikiEntry, GroupRoles.owner.name());

		// Check for jumping to certain wiki page
		BusinessControl bc = wControl.getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(courseEnv, wikiCourseNode);
		WikiSecurityCallback callback;
		WikiAssessmentProvider assessmentProvider;
		if(userCourseEnv.isCourseReadOnly()) {
			callback = new WikiReadOnlySecurityCallback(isGuestOnly, (isAdmininstrator || isResourceOwner));
			assessmentProvider = DryRunAssessmentProvider.create();
		} else {
			Boolean courseEditRight = Boolean.valueOf(hasEditRights(wikiCourseNode, userCourseEnv, ne));
			callback = new WikiSecurityCallbackImpl(courseEditRight, isAdmininstrator, isGuestOnly, false,
					isResourceOwner, subsContext);
			assessmentProvider = userCourseEnv.isParticipant()
					? PersistingAssessmentProvider.create(wikiEntry, getIdentity(), true)
					: DryRunAssessmentProvider.create();
		}
		
		
		if ( ce != null ) { //jump to a certain context
			OLATResourceable ores = ce.getOLATResourceable();
			String typeName = ores.getResourceableTypeName();
			String page = typeName.substring("page=".length());
			if(page.endsWith(":0")) {
				page = page.substring(0, page.length() - 2);
			}
			wikiCtr = WikiManager.getInstance().createWikiMainController(ureq, wControl, wikiEntry.getOlatResource(), callback, assessmentProvider, page);
		} else {
			wikiCtr = WikiManager.getInstance().createWikiMainController(ureq, wControl, wikiEntry.getOlatResource(), callback, assessmentProvider, null);
		}
		listenTo(wikiCtr);

		Controller wrappedCtr = TitledWrapperHelper.getWrapper(ureq, wControl, wikiCtr, userCourseEnv, wikiCourseNode, Wiki.CSS_CLASS_WIKI_ICON);		
		putInitialPanel(wrappedCtr.getInitialComponent());
	}
	
	private boolean hasEditRights(WikiCourseNode courseNode, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (courseNode.hasCustomPreConditions()) {
			return ne != null && ne.isCapabilityAccessible(WikiCourseNode.EDIT_CONDITION);
		}
		return nodeRightsService.isGranted(courseNode.getModuleConfiguration(), userCourseEnv, WikiCourseNode.EDIT);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		wikiCtr.activate(ureq, entries, state);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//no events yet
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		//
	}
	
	public NodeRunConstructionResult createNodeRunConstructionResult() {
		TreeModel wikiTreeModel = wikiCtr.getAndUseExternalTree();
		String selNodeId = wikiTreeModel.getRootNode().getChildAt(0).getIdent();
		return new NodeRunConstructionResult(this, wikiTreeModel, selNodeId, wikiCtr);
	}
}