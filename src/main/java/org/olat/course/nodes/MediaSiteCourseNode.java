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
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.mediasite.MediaSiteEditController;
import org.olat.course.nodes.mediasite.MediaSiteRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.mediasite.MediaSiteModule;
import org.olat.modules.mediasite.ui.MediaSiteAdminController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID 				= 7548967513297071079L;
	
	public static final String TYPE 						= "mediaSite";
	
	public static final String CONFIG_ELEMENT_ID 			= "elementId";
	public static final String CONFIG_ENABLE_PRIVATE_LOGIN 	= "enablePrivateLogin";
	public static final String CONFIG_SERVER_URL			= "serverUrl";
	public static final String CONFIG_ADMINISTRATION_URL	= "administrationUrl";
	public static final String CONFIG_SERVER_NAME			= "serverName";
	public static final String CONFIG_PRIVATE_KEY			= "privateKey";
	public static final String CONFIG_PRIVATE_SECRET 		= "privateSecret";
	public static final String CONFIG_USER_NAME_KEY			= "usernameKey";
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
		}

		return sd;
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
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller runCtrl;
		
		if(userCourseEnv.isCourseReadOnly()) {
			Translator trans = Util.createPackageTranslator(Card2BrainCourseNode.class, ureq.getLocale());
		    String title = trans.translate("freezenoaccess.title");
		    String message = trans.translate("freezenoaccess.message");
		    runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if (userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(Card2BrainCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
		    runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			runCtrl = new MediaSiteRunController(ureq, wControl, this, userCourseEnv);
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, "o_mediasite_icon");
		return new NodeRunConstructionResult(ctrl);
		
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = ConditionEditController.class.getPackageName();
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

}
