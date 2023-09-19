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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.bigbluebutton.BigBlueButtonEditController;
import org.olat.course.nodes.bigbluebutton.BigBlueButtonPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingDefaultConfiguration;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRunController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = 7965344505304490859L;
	public static final String TYPE = "bigbluebutton";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";
	
	public BigBlueButtonCourseNode() {
		super(TYPE);
	}
	
	@Override
	protected String getDefaultTitleOption() {
		// default is to only display content because the room has its own room title
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// create edit controller
		BigBlueButtonEditController childTabCtrl = new BigBlueButtonEditController(ureq, wControl, this);
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course,
				chosenNode, userCourseEnv, childTabCtrl);
		nodeEditCtr.addControllerListener(childTabCtrl);
		return nodeEditCtr;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		// check if user is admin. / moderator of the virtual classroom
		boolean admin = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
		boolean moderator = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
		// create run controller
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		ModuleConfiguration config = getModuleConfiguration();
		boolean moderatorStart = config.getBooleanSafe(BigBlueButtonEditController.MODERATOR_START_MEETING, true);
		BigBlueButtonMeetingDefaultConfiguration configuration = new BigBlueButtonMeetingDefaultConfiguration(moderatorStart);
		Controller controller = new BigBlueButtonRunController(ureq, wControl, entry, getIdent(), null, configuration,
				admin, moderator, userCourseEnv.isCourseReadOnly());

		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_vc_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		return new BigBlueButtonPeekViewController(ureq, wControl, userCourseEnv.getCourseEnvironment(), this);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		
		return StatusDescription.NOERROR;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		statusDescs.forEach(s -> s.setActivateableViewIdentifier(BigBlueButtonEditController.PANE_TAB_VCCONFIG));
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
		BigBlueButtonManager bigBlueButtonManager = CoreSpringFactory.getImpl(BigBlueButtonManager.class);
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(courseEntry, getIdent(), null, false);
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		for(BigBlueButtonMeeting meeting:meetings) {
			CoreSpringFactory.getImpl(BigBlueButtonManager.class).deleteMeeting(meeting, errors);
		}
		super.cleanupOnDelete(course);
	}
	
	

}
