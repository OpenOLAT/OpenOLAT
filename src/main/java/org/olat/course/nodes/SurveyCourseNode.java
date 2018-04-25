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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.survey.SurveyEditController;
import org.olat.course.nodes.survey.SurveyRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 905046067514602922L;

	public static final String SURVEY_ICON = "o_survey_icon";

	private static final String TYPE = "survey";
	
	public static final int CURRENT_VERSION = 1;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repository.softkey";

	public SurveyCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return getSurvey(getModuleConfiguration());
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		String repoKey = getModuleConfiguration().getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		boolean repoKeyMissing = !StringHelper.containsNonWhitespace(repoKey);
		if (repoKeyMissing) {
			String shortKey = "error.repo.no.key.short";
			String longKey = "error.repo.no.key.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(SurveyEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(SurveyEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		TabbableController childTabCntrllr	= new SurveyEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		OLATResourceable ores = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getOlatResource();
		Controller runCtrl = new SurveyRunController(ureq, wControl, ores, this);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, this, SURVEY_ICON);
		return new NodeRunConstructionResult(ctrl);
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			//
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	public static RepositoryEntry getSurvey(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = config.getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (!StringHelper.containsNonWhitespace(repoSoftkey)) return null;
		
		//TODO uh soft deletes?
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
}
