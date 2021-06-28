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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodes.basiclti.LTIAssessmentConfig;
import org.olat.course.nodes.basiclti.LTIConfigForm;
import org.olat.course.nodes.basiclti.LTIEditController;
import org.olat.course.nodes.basiclti.LTILearningPathNodeHandler;
import org.olat.course.nodes.basiclti.LTIRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * @author guido
 * @author Charles Severance
 */
public class BasicLTICourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 2210572148308757127L;
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(LTIEditController.class);
	
	public static final String TYPE = "lti";
	
	public static final int CURRENT_VERSION = 3;
	public static final String CONFIG_KEY_AUTHORROLE = "authorRole";
	public static final String CONFIG_KEY_COACHROLE = "coachRole";
	public static final String CONFIG_KEY_PARTICIPANTROLE = "participantRole";
	public static final String CONFIG_KEY_SCALEVALUE = "scaleFactor";
	public static final String CONFIG_KEY_HAS_SCORE_FIELD = MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD;
	public static final String CONFIG_KEY_HAS_PASSED_FIELD = MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD;
	public static final String CONFIG_KEY_PASSED_CUT_VALUE = MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE;
	public static final String CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT = MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT;
	public static final String CONFIG_SKIP_LAUNCH_PAGE = "skiplaunchpage";
	public static final String CONFIG_SKIP_ACCEPT_LAUNCH_PAGE = "skipacceptlaunchpage";
	public static final String CONFIG_HEIGHT = "displayHeight";
	public static final String CONFIG_WIDTH = "displayWidth";
	public static final String CONFIG_HEIGHT_AUTO = DeliveryOptions.CONFIG_HEIGHT_AUTO;
	public static final String CONFIG_DISPLAY = "display";
	
	// NLS support:
	private static final String NLS_ERROR_HOSTMISSING_SHORT = "error.hostmissing.short";
	private static final String NLS_ERROR_HOSTMISSING_LONG = "error.hostmissing.long";

	public BasicLTICourseNode() {
		this(null);
	}

	public BasicLTICourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		LTIEditController childTabCntrllr = new LTIEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller runCtrl;
		if(userCourseEnv.isCourseReadOnly()) {
			Translator trans = Util.createPackageTranslator(BasicLTICourseNode.class, ureq.getLocale());
            String title = trans.translate("freezenoaccess.title");
            String message = trans.translate("freezenoaccess.message");
            runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			Roles roles = ureq.getUserSession().getRoles();
			if (roles.isGuestOnly()) {
				if(isGuestAllowed()) {
					Translator trans = Util.createPackageTranslator(BasicLTICourseNode.class, ureq.getLocale());
					String title = trans.translate("guestnoaccess.title");
					String message = trans.translate("guestnoaccess.message");
					runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
				} else {
					runCtrl = getRunController(ureq, wControl, userCourseEnv);
				}
			} else {
				runCtrl = getRunController(ureq, wControl, userCourseEnv);
			}
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, this, "o_lti_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public Controller getRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		Controller runCtrl;
		ModuleConfiguration config = getModuleConfiguration();
		String ltiVersion = config.getStringValue(LTIConfigForm.CONFIGKEY_LTI_VERSION, LTIConfigForm.CONFIGKEY_LTI_11);
		if(LTIConfigForm.CONFIGKEY_LTI_13.equals(ltiVersion)) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			LTI13ToolDeployment deployment = CoreSpringFactory.getImpl(LTI13Service.class).getToolDeployment(courseEntry, getIdent());
			runCtrl = new LTIRunController(ureq, wControl, this, deployment, userCourseEnv);
		} else {
			runCtrl = new LTIRunController(ureq, wControl, this, userCourseEnv);
		}
		return runCtrl;
	}
	
	public String getUrl() {
		ModuleConfiguration config = getModuleConfiguration();
		// put url in template to show content on extern page
		URL url = null;
		try {
			url = new URL(config.getStringValue(LTIConfigForm.CONFIGKEY_PROTO), config.getStringValue(LTIConfigForm.CONFIGKEY_HOST),
					((Integer)config.get(LTIConfigForm.CONFIGKEY_PORT)).intValue(), config.getStringValue(LTIConfigForm.CONFIGKEY_URI));
		} catch (MalformedURLException e) {
			// this should not happen since the url was already validated in edit mode
			return null;
		}

		StringBuilder querySb = new StringBuilder(128);
		querySb.append(url.toString());
		// since the url only includes the path, but not the query (?...), append
		// it here, if any
		String query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
		if (query != null) {
			querySb.append("?");
			querySb.append(query);
		}
		return querySb.toString();
	}
	
	public boolean isGuestAllowed() {
		ModuleConfiguration config = getModuleConfiguration();
		boolean assessable = config.getBooleanSafe(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, false);
		boolean sendName = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDNAME, false);
		boolean sendEmail = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDEMAIL, false);
		boolean customValues = StringHelper.containsNonWhitespace(config.getStringValue(LTIConfigForm.CONFIG_KEY_CUSTOM));
		return !assessable && !sendName && !sendEmail && !customValues;
	}
	
	public Float getCutValue(AssessmentConfig assessmentConfig) {
		if(Mode.setByNode == assessmentConfig.getPassedMode()) {
			Float cutValue = assessmentConfig.getCutValue();
			if(cutValue == null) {
				return null;
			}
			return cutValue;
		}
		return null;
	}
	
	/**
	 * Return the scaling factor or 1.0f if not configured.
	 * 
	 * @param node
	 * @return
	 */
	public float getScalingFactor() {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(this);
		if(Mode.none != assessmentConfig.getScoreMode()) {
			Float scale = getModuleConfiguration().getFloatEntry(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
			if(scale == null) {
				return 1.0f;
			}
			return scale.floatValue();
		}
		return 1.0f;
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		CourseNode parent = this.getParent() instanceof CourseNode? (CourseNode)this.getParent(): null;
		updateModuleConfigDefaults(false, parent);
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}

		List<StatusDescription> statusDescs = validateInternalConfiguration();
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration() {
		List<StatusDescription> sdList = new ArrayList<>(1);
		
		if (isFullyAssessedScoreConfigError()) {
			addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError()) {
			addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		ModuleConfiguration config = getModuleConfiguration();
		String host = (String)config.get(LTIConfigForm.CONFIGKEY_HOST);
		if (!StringHelper.containsNonWhitespace(host)) {
			addStatusErrorDescription(NLS_ERROR_HOSTMISSING_SHORT, NLS_ERROR_HOSTMISSING_LONG,
					LTIEditController.PANE_TAB_LTCONFIG, sdList);
		}
		
		String ltiVersion = (String)config.get(LTIConfigForm.CONFIGKEY_LTI_VERSION);
		if((LTIConfigForm.CONFIGKEY_LTI_13.equals(ltiVersion) || StringHelper.isLong(ltiVersion))
				&& !config.has(LTIConfigForm.CONFIGKEY_13_DEPLOYMENT_KEY)) {	
			addStatusErrorDescription("error.deployment.missing", "error.deployment.missing",
					LTIEditController.PANE_TAB_LTCONFIG, sdList);
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError() {
		boolean hasScore = Mode.none != new LTIAssessmentConfig(getModuleConfiguration()).getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(LTILearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError() {
		boolean hasPassed = new LTIAssessmentConfig(getModuleConfiguration()).getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(LTILearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}
	
	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, shortDescKey, longDescKey, params,
				TRANSLATOR_PACKAGE);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
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
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		CoreSpringFactory.getImpl(LTIManager.class)
			.deleteOutcomes(cgm.getCourseResource());
		CoreSpringFactory.getImpl(LTI13Service.class)
			.deleteToolsAndDeployments(cgm.getCourseEntry(), getIdent());
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
		super.postCopy(envMapper, processType, course, sourceCrourse);
		removeLTI13DeploymentReference();
	}

	@Override
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
		super.postImport(importDirectory, course, envMapper, processType);
		removeLTI13DeploymentReference();
	}
	
	private void removeLTI13DeploymentReference() {
		ModuleConfiguration config = getModuleConfiguration();
		
		String ltiVersion = (String)config.get(LTIConfigForm.CONFIGKEY_LTI_VERSION);
		if(LTIConfigForm.CONFIGKEY_LTI_13.equals(ltiVersion)) {	
			config.remove(LTIConfigForm.CONFIGKEY_13_DEPLOYMENT_KEY);
		} else if(StringHelper.isLong(ltiVersion)) {
			config.remove(LTIConfigForm.CONFIGKEY_13_DEPLOYMENT_KEY);
			config.setStringValue(LTIConfigForm.CONFIGKEY_LTI_VERSION, LTIConfigForm.CONFIGKEY_LTI_13);
		}
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to useful default values
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
			config.setBooleanEntry(CONFIG_SKIP_LAUNCH_PAGE, Boolean.FALSE.booleanValue());
			config.setBooleanEntry(CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.FALSE.booleanValue());
		} else {
			// clear old popup configuration
			config.remove(NodeEditController.CONFIG_INTEGRATION);
			config.remove("width");
			config.remove("height");
			if (config.getConfigurationVersion() < 2) {
				// update new configuration options using default values for existing nodes
				config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
			}
			if (config.getConfigurationVersion() < 3) {
				if (BasicLTICourseNode.CONFIG_DISPLAY.equals(LTIDisplayOptions.window.name())) {
					config.setBooleanEntry(CONFIG_SKIP_LAUNCH_PAGE, Boolean.FALSE.booleanValue());
				} else {
					config.setBooleanEntry(CONFIG_SKIP_LAUNCH_PAGE, Boolean.TRUE.booleanValue());
				}
				config.setBooleanEntry(CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.FALSE.booleanValue());
			}
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}

}