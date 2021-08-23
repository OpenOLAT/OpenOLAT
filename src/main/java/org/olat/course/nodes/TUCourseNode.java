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

package org.olat.course.nodes;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.course.nodes.tu.TUEditController;
import org.olat.course.nodes.tu.TURunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: 28.11.2003
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class TUCourseNode extends AbstractAccessableCourseNode {

	public static final String TYPE = "tu";
	
	// NLS support:
	private static final String NLS_ERROR_HOSTMISSING_SHORT = "error.hostmissing.short";
	private static final String NLS_ERROR_HOSTMISSING_LONG = "error.hostmissing.long";

	public TUCourseNode() {
		this(null);
	}
	
	public TUCourseNode(CourseNode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		TUEditController childTabCntrllr = new TUEditController(getModuleConfiguration(), ureq, wControl, stackPanel, this, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		return new NodeRunConstructionResult(new TURunController(wControl, getModuleConfiguration(), ureq, this, userCourseEnv));
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}

	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		String host = (String) getModuleConfiguration().get(TUConfigForm.CONFIGKEY_HOST);
		boolean isValid = host != null;
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			// FIXME: refine statusdescriptions
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(TUConfigForm.class);
			sd = new StatusDescription(StatusDescription.ERROR, NLS_ERROR_HOSTMISSING_SHORT, NLS_ERROR_HOSTMISSING_LONG, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(TUEditController.PANE_TAB_TUCONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(TUEditController.class);
		List<StatusDescription> sds =  isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
			config.setConfigurationVersion(2);
		} else {
			// clear old popup configuration
			config.remove(NodeEditController.CONFIG_INTEGRATION);
			config.remove("width");
			config.remove("height");
			if (config.getConfigurationVersion() < 2) {
				// update new configuration options using default values for existing nodes
				config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
				config.setConfigurationVersion(2);
			}
			// else node is up-to-date - nothing to do
		}
	}

}
