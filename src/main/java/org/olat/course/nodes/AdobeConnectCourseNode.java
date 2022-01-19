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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.adobeconnect.AdobeConnectCourseNodeConfiguration;
import org.olat.course.nodes.adobeconnect.AdobeConnectEditController;
import org.olat.course.nodes.adobeconnect.compatibility.AdobeConnectCompatibilityConfiguration;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.ui.AdobeConnectMeetingDefaultConfiguration;
import org.olat.modules.adobeconnect.ui.AdobeConnectRunController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = 7965344505304490859L;
	private static final Logger log = Tracing.createLoggerFor(AdobeConnectCourseNode.class);
	public static final String TYPE = "adobeconnect";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";

	private transient CourseGroupManager groupMgr;
	
	public AdobeConnectCourseNode() {
		super(TYPE);
	}

	private void updateModuleConfigDefaults(RepositoryEntry courseEntry, boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if(config.getConfigurationVersion() < 2) {
			Object oldConfiguration = config.get(CONF_VC_CONFIGURATION);
			if(oldConfiguration instanceof AdobeConnectCompatibilityConfiguration) {
				AdobeConnectCompatibilityConfiguration oldConfig = (AdobeConnectCompatibilityConfiguration)oldConfiguration;
				config.setBooleanEntry(AdobeConnectEditController.ACCESS_BY_DATES, oldConfig.isUseMeetingDates());
				config.setBooleanEntry(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, oldConfig.isGuestAccessAllowed());
				config.setBooleanEntry(AdobeConnectEditController.MODERATOR_START_MEETING, !oldConfig.isGuestStartMeetingAllowed());
				
				if(courseEntry != null && oldConfig.getMeetingDatas() != null && !oldConfig.getMeetingDatas().isEmpty()) {
					AdobeConnectManager adobeConnectManager = CoreSpringFactory.getImpl(AdobeConnectManager.class);
					boolean hasMeetings = adobeConnectManager.hasMeetings(courseEntry, getIdent(), null);
					if(!hasMeetings) {
						synchronized(this) {// enough
							adobeConnectManager.convert(oldConfig.getMeetingDatas(), courseEntry, getIdent());
						}
					}
				}
			}
		}
		config.setConfigurationVersion(2);
	}
	
	@Override
	protected String getDefaultTitleOption() {
		// default is to only display content because the room has its own room title
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		RepositoryEntry courseEntry = course.getCourseEnvironment()
				.getCourseGroupManager().getCourseEntry();
		updateModuleConfigDefaults(courseEntry, false);
		
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// create edit controller
		AdobeConnectEditController childTabCtrl = new AdobeConnectEditController(ureq, wControl, this);
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				userCourseEnv, childTabCtrl);
		nodeEditCtr.addControllerListener(childTabCtrl);
		return nodeEditCtr;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment()
				.getCourseGroupManager().getCourseEntry();
		updateModuleConfigDefaults(courseEntry, false);
		
		String providerId = getModuleConfiguration().getStringValue("vc_provider_id");
		
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if("wimba".equals(providerId)) {
			Translator trans = Util.createPackageTranslator(AdobeConnectCourseNodeConfiguration.class, ureq.getLocale());
			String title = trans.translate("wimba.not.supported.title");
			String message = trans.translate("wimba.not.supported.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if(roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(AdobeConnectCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			// check if user is moderator of the virtual classroom
			boolean admin = userCourseEnv.isAdmin();
			boolean moderator = admin || userCourseEnv.isCoach();
			// create run controller
			RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			AdobeConnectModule adobeConnectModule = CoreSpringFactory.getImpl(AdobeConnectModule.class);
			
			ModuleConfiguration config = getModuleConfiguration();
			boolean onlyDates = config.getBooleanSafe(AdobeConnectEditController.ACCESS_BY_DATES, false);
			
			boolean guestAccess = config.getBooleanSafe(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, false);
			boolean moderatorStart;
			if(adobeConnectModule.isCreateMeetingImmediately()) {
				moderatorStart = config.getBooleanSafe(AdobeConnectEditController.MODERATOR_START_MEETING, false);
			} else {
				moderatorStart = config.getBooleanSafe(AdobeConnectEditController.MODERATOR_START_MEETING, true);
			}
			
			AdobeConnectMeetingDefaultConfiguration configuration = new AdobeConnectMeetingDefaultConfiguration(onlyDates, guestAccess, moderatorStart);
			controller = new AdobeConnectRunController(ureq, wControl, entry, getIdent(), null, configuration,
					admin, moderator, userCourseEnv.isCourseReadOnly());
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_vc_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		
		StatusDescription sd = StatusDescription.NOERROR;
		if(groupMgr != null) {
			//
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		if (groupMgr == null) {
			groupMgr = cev.getCourseGroupManager();
		}
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
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
		// remove meeting
		try {
			AdobeConnectManager provider = CoreSpringFactory.getImpl(AdobeConnectManager.class);
			RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			provider.delete(courseRe, getIdent());
		} catch(Exception e) {
			log.error("A room could not be deleted for course node: " + getIdent() + " of course:" + course, e);
		}
	}
}
