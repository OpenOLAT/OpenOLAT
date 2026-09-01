/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you hy not use this file except in compliance with the License.<br>
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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.mediasite.MediaSiteEditController;
import org.olat.course.nodes.mediasite.MediaSiteRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.LtiVersion;
import org.olat.modules.mediasite.MediaSiteManager;
import org.olat.modules.mediasite.MediaSiteModule;
import org.olat.modules.mediasite.ui.MediaSiteAdminController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID 				= 7548967513297071079L;
	
	public static final String TYPE 						= "mediaSite";
	
	public static final String CONFIG_ELEMENT_ID 			= "elementId";
	public static final String CONFIG_ENABLE_PRIVATE_LOGIN 	= "enablePrivateLogin";
	
	public static final String CONFIG_LTI_VERSION			= "ltiVersion";
	
	public static final String CONFIG_SERVER_URL			= "serverUrl";
	public static final String CONFIG_ADMINISTRATION_URL	= "administrationUrl";
	public static final String CONFIG_PRIVATE_KEY			= "privateKey";
	public static final String CONFIG_PRIVATE_SECRET 		= "privateSecret";
	public static final String CONFIG_USER_NAME_KEY			= "usernameKey";
	
	public static final String CONFIG_LTI13_TOOL_KEY		= "lti13ToolKey";
	public static final String CONFIG_LTI13_DEPLOYMENT_KEY	= "lti13DeploymentKey";
	public static final String CONFIG_LTI13_BASE_URL		= "lti13BaseUrl";
	public static final String CONFIG_LTI13_ADMIN_URL		= "lti13AdminUrl";
	
	public static final String CONFIG_SUPRESS_AGREEMENT		= "supressDataTransmissionAgreement";
	public static final String CONFIG_IS_DEBUG				= "isDebug";
	
	public MediaSiteCourseNode() {
		super(TYPE);
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
	public StatusDescription isConfigValid() {
		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		
		StatusDescription sd =  StatusDescription.NOERROR;
		MediaSiteModule mediaSiteModule = CoreSpringFactory.getImpl(MediaSiteModule.class);
		
		boolean usesPrivateLogin = getModuleConfiguration().getBooleanSafe(CONFIG_ENABLE_PRIVATE_LOGIN);
		String moduleId = getModuleConfiguration().getStringValue(CONFIG_ELEMENT_ID);
		
		if (!mediaSiteModule.isGlobalLoginEnabled() && !usesPrivateLogin) {
			String shortKey = "edit.warning.global.login.disabled.short";
			String longKey = "edit.warning.global.login.disabled";
			String translPackage = MediaSiteAdminController.class.getPackageName();
			
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, null, translPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(MediaSiteEditController.PANE_TAB_VCCONFIG);
		} else if (!StringHelper.containsNonWhitespace(moduleId)) {
			String shortKey = "edit.warning.module.id.short";
			String longKey = "edit.warning.module.id";
			String translPackage = MediaSiteAdminController.class.getPackageName();
			
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, null, translPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(MediaSiteEditController.PANE_TAB_VCCONFIG);
		} else if (isLti13Incomplete(mediaSiteModule, usesPrivateLogin)) {
			String shortKey = "edit.warning.lti13.not.configured.short";
			String longKey = "edit.warning.lti13.not.configured";
			String translPackage = MediaSiteAdminController.class.getPackageName();

			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, null, translPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(MediaSiteEditController.PANE_TAB_VCCONFIG);
		}

		return sd;
	}

	/**
	 * Checks that the config keys needed to launch LTI 1.3 (see MediaSiteRunController.showContentLti13())
	 * are present, without resolving them against the database - a course tree with many nodes can call
	 * isConfigValid() repeatedly, so this stays a cheap, config-only check, same as the equivalent check
	 * in BasicLTICourseNode.validateInternalConfiguration() for the generic LTI 1.3 course element.
	 */
	private boolean isLti13Incomplete(MediaSiteModule mediaSiteModule, boolean usesPrivateLogin) {
		if (!LtiVersion.lti_1_3.name().equals(getModuleConfiguration().getStringValue(CONFIG_LTI_VERSION))) {
			return false;
		}
		if (usesPrivateLogin) {
			String toolKey = getModuleConfiguration().getStringValue(CONFIG_LTI13_TOOL_KEY);
			String baseUrl = getModuleConfiguration().getStringValue(CONFIG_LTI13_BASE_URL);
			return !StringHelper.isLong(toolKey) || !StringHelper.containsNonWhitespace(baseUrl);
		}
		return mediaSiteModule.getLti13DeploymentKey() == null
				|| !StringHelper.containsNonWhitespace(mediaSiteModule.getLti13BaseUrl());
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		copyLti13Configuration(sourceCourse, course, getIdent());
	}

	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings,
			CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		copyLti13Configuration(sourceCourse, course, sourceCourseNode.getIdent());
	}

	private void copyLti13Configuration(ICourse sourceCourse, ICourse targetCourse, String subIdent) {
		ModuleConfiguration config = getModuleConfiguration();
		String version = config.getStringValue(CONFIG_LTI_VERSION);
		if (LtiVersion.lti_1_3.name().equals(version)) {
			MediaSiteManager mediaSiteManager = CoreSpringFactory.getImpl(MediaSiteManager.class);
			RepositoryEntry sourceEntry = sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryEntry targetEntry = targetCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			Long clonedToolKey = mediaSiteManager.copyLti13MediaSiteConfiguration(sourceEntry, targetEntry, subIdent);
			if (clonedToolKey != null) {
				config.setStringValue(CONFIG_LTI13_TOOL_KEY, String.valueOf(clonedToolKey));
			}
		}
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		ModuleConfiguration config = getModuleConfiguration();
		String version = config.getStringValue(CONFIG_LTI_VERSION);
		String toolKeyString = config.getStringValue(CONFIG_LTI13_TOOL_KEY);
		if (LtiVersion.lti_1_3.name().equals(version) && StringHelper.containsNonWhitespace(toolKeyString)) {
			MediaSiteManager mediaSiteManager = CoreSpringFactory.getImpl(MediaSiteManager.class);
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			mediaSiteManager.deleteLti13MediaSiteConfiguration(entry, getIdent(), Long.valueOf(toolKeyString));
		}
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		
		MediaSiteEditController editController = new MediaSiteEditController(ureq, wControl, getModuleConfiguration(), (MediaSiteCourseNode) chosenNode, course, euce);
		
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editController);
		
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller runCtrl;
		
		runCtrl = new MediaSiteRunController(ureq, wControl, this, userCourseEnv);
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, "o_mediasite_icon");
		return new NodeRunConstructionResult(ctrl);
		
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null, null).getRunController();
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = ConditionEditController.class.getPackageName();
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		statusDescs.forEach(s -> s.setActivateableViewIdentifier(MediaSiteEditController.PANE_TAB_VCCONFIG));
		return StatusDescriptionHelper.sort(statusDescs);
	}

}
