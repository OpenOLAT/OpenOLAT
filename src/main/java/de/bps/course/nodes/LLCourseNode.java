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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.course.nodes.ll.LLEditController;
import de.bps.course.nodes.ll.LLModel;
import de.bps.course.nodes.ll.LLRunController;

/**
 * Description:<br>
 * Link list course node.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLCourseNode extends AbstractAccessableCourseNode {

	public static final String TYPE = "ll";
	public static final String CONF_COURSE_ID = "ll_course_id";
	public static final String CONF_COURSE_NODE_ID = "ll_course_node_id";
	public static final String CONF_LINKLIST = "ll_link_list";

	public LLCourseNode() {
		super(TYPE);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
	
		ModuleConfiguration config = getModuleConfiguration();
		
		if (isNewNode) {
			List<LLModel> initialList = new ArrayList<>(1);
			initialList.add(new LLModel());
			config.set(CONF_LINKLIST, initialList);
		}
		
		if(config.getConfigurationVersion() < 2) {
			@SuppressWarnings("unchecked")
			List<LLModel> links = (List<LLModel>)config.get(CONF_LINKLIST);
			for(LLModel link:links) {
				String linkValue = link.getTarget();
				if(!linkValue.contains("://")) {
					linkValue = "http://".concat(linkValue.trim());
				}
				if(linkValue.startsWith(Settings.getServerContextPathURI())) {
					link.setHtmlTarget("_self");
				} else {
					link.setHtmlTarget("_blank");
				}
			}
			config.setConfigurationVersion(2);
		}
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			UserCourseEnvironment userCourseEnv) {
		LLEditController childTabCntrllr = new LLEditController(getModuleConfiguration(), ureq, wControl, course);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// needed for DENEditController.isConfigValid()
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, userCourseEnv, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_ll_icon");
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		// Use normal view as peekview
		return new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, false);
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_ll_icon");
		return controller;
	}
	
	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);

		List<LLModel> links =  getLinks();
		for(LLModel link:links) {
			String target = link.getTarget();
			if(!target.contains("://") && !target.contains("/library/")) {
				String renamedTarget = envMapper.getRenamedPath(target);
				if(renamedTarget != null) {
					renamedTarget = VFSManager.appendLeadingSlash(renamedTarget);
					link.setTarget(renamedTarget);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<LLModel> getLinks() {
		return (List<LLModel>)getModuleConfiguration().get(CONF_LINKLIST);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;

		if (!LLEditController.isConfigValid(getModuleConfiguration())) {
			String transPackage = Util.getPackageName(LLEditController.class);
			sd = new StatusDescription(StatusDescription.WARNING, "config.nolinks.short", "config.nolinks.long", null, transPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(LLEditController.PANE_TAB_LLCONFIG);
		}

		return sd;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

}
