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
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
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

	private static final String TYPE = "ll";
	public static final String CONF_COURSE_ID = "ll_course_id";
	public static final String CONF_COURSE_NODE_ID = "ll_course_node_id";
	public static final String CONF_LINKLIST = "ll_link_list";

	/**
	 * Create default link list course node.
	 */
	public LLCourseNode() {
		super(TYPE);
		initDefaultConfig();
	}

	private void initDefaultConfig() {
		ModuleConfiguration config = getModuleConfiguration();
		// add an empty link entry as default if none existent
		if (config.get(CONF_LINKLIST) == null) {
			List<LLModel> initialList = new ArrayList<>(1);
			initialList.add(new LLModel());
			config.set(CONF_LINKLIST, initialList);
		}
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if(config.getConfigurationVersion() < 2) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			UserCourseEnvironment userCourseEnv) {
		updateModuleConfigDefaults(false);
		LLEditController childTabCntrllr = new LLEditController(getModuleConfiguration(), ureq, wControl, this, course, userCourseEnv);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// needed for DENEditController.isConfigValid()
		getModuleConfiguration().set(CONF_COURSE_ID, course.getResourceableId());
		getModuleConfiguration().set(CONF_COURSE_NODE_ID, chosenNode.getIdent());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, userCourseEnv, childTabCntrllr);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ll_icon");
		return new NodeRunConstructionResult(controller);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPeekViewRunController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		updateModuleConfigDefaults(false);
		// Use normal view as peekview
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, false);
		return controller;
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		Controller controller = new LLRunController(ureq, wControl, getModuleConfiguration(), this, userCourseEnv, true);
		controller = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ll_icon");
		return controller;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	/**
	 * {@inheritDoc}
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

}
