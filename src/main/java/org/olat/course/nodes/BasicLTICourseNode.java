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
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.basiclti.LTIConfigForm;
import org.olat.course.nodes.basiclti.LTIEditController;
import org.olat.course.nodes.basiclti.LTIRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * @author guido
 * @author Charles Severance
 */
public class BasicLTICourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode {

	private static final long serialVersionUID = 2210572148308757127L;
	private static final String translatorPackage = Util.getPackageName(LTIEditController.class);
	public static final String TYPE = "lti";
	
	public static final int CURRENT_VERSION = 3;
	public static final String CONFIG_KEY_AUTHORROLE = "authorRole";
	public static final String CONFIG_KEY_COACHROLE = "coachRole";
	public static final String CONFIG_KEY_PARTICIPANTROLE = "participantRole";
	public static final String CONFIG_KEY_SCALEVALUE = "scaleFactor";
	public static final String CONFIG_KEY_HAS_SCORE_FIELD = MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD;
	public static final String CONFIG_KEY_HAS_PASSED_FIELD = MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD;
	public static final String CONFIG_KEY_PASSED_CUT_VALUE = MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE;
	public static final String CONFIG_SKIP_LAUNCH_PAGE = "skiplaunchpage";
	public static final String CONFIG_SKIP_ACCEPT_LAUNCH_PAGE = "skipacceptlaunchpage";
	public static final String CONFIG_HEIGHT = "displayHeight";
	public static final String CONFIG_WIDTH = "displayWidth";
	public static final String CONFIG_HEIGHT_AUTO = DeliveryOptions.CONFIG_HEIGHT_AUTO;
	public static final String CONFIG_DISPLAY = "display";
	
	
	// NLS support:
	
	private static final String NLS_ERROR_HOSTMISSING_SHORT = "error.hostmissing.short";
	private static final String NLS_ERROR_HOSTMISSING_LONG = "error.hostmissing.long";

	/**
	 * Constructor for a course node of type learning content tunneling
	 */
	public BasicLTICourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		LTIEditController childTabCntrllr = new LTIEditController(getModuleConfiguration(), ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		
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
					runCtrl = new LTIRunController(wControl, getModuleConfiguration(), ureq, this, userCourseEnv);
				}
			} else {
				runCtrl = new LTIRunController(wControl, getModuleConfiguration(), ureq, this, userCourseEnv);
			}
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, this, "o_lti_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public boolean isGuestAllowed() {
		ModuleConfiguration config = getModuleConfiguration();
		boolean assessable = config.getBooleanSafe(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, false);
		boolean sendName = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDNAME, false);
		boolean sendEmail = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDEMAIL, false);
		boolean customValues = StringHelper.containsNonWhitespace(config.getStringValue(LTIConfigForm.CONFIG_KEY_CUSTOM));
		return !assessable && !sendName && !sendEmail && !customValues;
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	@Override
	public StatusDescription isConfigValid() {

		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		String host = (String) getModuleConfiguration().get(LTIConfigForm.CONFIGKEY_HOST);
		boolean isValid = host != null;
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			// FIXME: refine statusdescriptions
			String[] params = new String[] { this.getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, NLS_ERROR_HOSTMISSING_SHORT, NLS_ERROR_HOSTMISSING_LONG, params, translatorPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(LTIEditController.PANE_TAB_LTCONFIG);
		}
		return sd;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		List<StatusDescription> sds =  isConfigValidWithTranslator(cev, translatorPackage, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	/**
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CoreSpringFactory.getImpl(LTIManager.class).deleteOutcomes(resource);
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
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