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
import org.olat.course.nodes.gotomeeting.GoToMeetingEditController;
import org.olat.course.nodes.gotomeeting.GoToMeetingPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.ui.GoToMeetingRunController;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 8680935159748506305L;
	private static final Logger log = Tracing.createLoggerFor(GoToMeetingCourseNode.class);

	public static final String TYPE = "gotomeeting";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";

	private transient CourseGroupManager groupMgr;

	public GoToMeetingCourseNode() {
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
		GoToMeetingEditController childTabCntrllr = new GoToMeetingEditController(ureq, wControl);
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				userCourseEnv, childTabCntrllr);
		nodeEditCtr.addControllerListener(childTabCntrllr);
		return nodeEditCtr;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(GoToMeetingEditController.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			// check if user is moderator of the virtual classroom
			boolean admin = userCourseEnv.isAdmin();
			boolean moderator = admin || userCourseEnv.isCoach();

			// create run controller
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			controller = new GoToMeetingRunController(ureq, wControl, courseEntry, getIdent(), null,
					admin, moderator, userCourseEnv.isCourseReadOnly());
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_gotomeeting_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		return new GoToMeetingPeekViewController(ureq, wControl, userCourseEnv, getIdent());
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
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		return StatusDescription.NOERROR;
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
			GoToMeetingManager provider = CoreSpringFactory.getImpl(GoToMeetingManager.class);
			RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			provider.deleteAll(courseRe, getIdent(), null);
		} catch(Exception e) {
			log.error("A room could not be deleted for course node: " + getIdent() + " of course:" + course, e);
		}
	}
}