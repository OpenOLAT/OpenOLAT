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
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.cns.CNSEnvironment;
import org.olat.course.nodes.cns.manager.CNSAssessmentEnvironment;
import org.olat.course.nodes.cns.ui.CNSEditController;
import org.olat.course.nodes.cns.ui.CNSRunCoachController;
import org.olat.course.nodes.cns.ui.CNSSelectionController;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeService;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * CNSCourseNode, the Course Node Selection Course Node.
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = 3542634787418361029L;
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(CNSSelectionController.class);
	
	public static final String TYPE = "cns";
	public static final String ICON_CSS = "o_icon_cns";
	
	private static final int CURRENT_VERSION = 1;
	public static final String CONFIG_KEY_REQUIRED_SELECTIONS = "required.selections";

	public CNSCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		CNSEditController childTabCtrl = new CNSEditController(ureq, wControl, this);
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				userCourseEnv, childTabCtrl);
		nodeEditCtr.addControllerListener(childTabCtrl);
		return nodeEditCtr;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd,
			VisibilityFilter visibilityFilter) {
	
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else if (userCourseEnv.isParticipant()) {
			CNSEnvironment cnsEnv = new CNSAssessmentEnvironment(userCourseEnv, this);
			controller = new CNSSelectionController(ureq, wControl, this, userCourseEnv, cnsEnv);
		} else {
			controller = new CNSRunCoachController(ureq, wControl, this, userCourseEnv);
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, ICON_CSS);
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}
		
		return StatusDescription.NOERROR;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		
		if (cev != null) {
			String requiredSelectionsConfig = getModuleConfiguration().getStringValue(CONFIG_KEY_REQUIRED_SELECTIONS);
			if (StringHelper.isLong(requiredSelectionsConfig)) {
				Integer requiredSelections = Integer.valueOf(requiredSelectionsConfig);
				if (requiredSelections > getChildCount(cev)) {
					String shortKey = "error.too.less.children.short";
					String longKey = "error.too.less.children.long";
					String[] params = new String[] { getShortTitle() };
					StatusDescription sd = new StatusDescription(StatusDescription.WARNING, shortKey, longKey, params, TRANSLATOR_PACKAGE);
					sd.setDescriptionForUnit(getIdent());
					sd.setActivateableViewIdentifier(CNSEditController.PANE_TAB_CONFIG);
					statusDescs.add(sd);
				}
			}
		}
		
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	private int getChildCount(CourseEditorEnv cev) {
		CourseEditorTreeNode cen = cev.getTreeNode(getIdent());
		if (cen == null) {
			return 0;
		}
		
		int count = 0;
		for (int i = 0; i < cen.getChildCount(); i++) {
			CourseEditorTreeNode child = (CourseEditorTreeNode)cen.getChildAt(i);
			if (!child.isDeleted() && !child.getCourseNode().isConfigValid().isError()) {
				count++;
			}
		}
		
		return count;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		
		if (isNewNode) {
			config.setStringValue(CONFIG_KEY_REQUIRED_SELECTIONS, "3");
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		topicBrokerCourseNodeService.synchBroker(publisher, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}

}
