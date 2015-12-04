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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.OpenSubDetailsEvent;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentityCourseNodeController extends BasicController implements AssessedIdentityController {
	
	private final TooledStackedPanel stackPanel;
	private final VelocityContainer identityAssessmentVC;
	
	private AssessmentForm assessmentForm;
	private Controller subDetailsController;
	private Controller detailsEditController;
	
	private final CourseNode courseNode;
	private final Identity assessedIdentity;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	public AssessmentIdentityCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, Identity assessedIdentity) {
		super(ureq, wControl);
		
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.assessedIdentity = assessedIdentity;
		
		identityAssessmentVC = createVelocityContainer("identity_personal_node_infos");
		identityAssessmentVC.contextPut("user", assessedIdentity.getUser());
		identityAssessmentVC.contextPut("fullName", userManager.getUserDisplayName(assessedIdentity));
		identityAssessmentVC.contextPut("courseNode", courseNode.getShortTitle());
		
		String courseNodeCssClass = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		identityAssessmentVC.contextPut("courseNodeCss", courseNodeCssClass);
		
		ModuleConfiguration modConfig = courseNode.getModuleConfiguration();
		String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		infoCoach = Formatter.formatLatexFormulas(infoCoach);
		identityAssessmentVC.contextPut("infoCoach", infoCoach);
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		assessedUserCourseEnv.getScoreAccounting().evaluateAll();

		// Add the assessment details form
		if(courseNode instanceof AssessableCourseNode) {
			AssessableCourseNode aCourseNode = (AssessableCourseNode)courseNode;

			// Add the users details controller
			if (aCourseNode.hasDetails()) {
				detailsEditController = aCourseNode.getDetailsEditController(ureq, wControl, stackPanel, assessedUserCourseEnv);
				listenTo(detailsEditController);
				identityAssessmentVC.put("details", detailsEditController.getInitialComponent());
			}

			assessmentForm = new AssessmentForm(ureq, wControl, aCourseNode, assessedUserCourseEnv, true);
			listenTo(assessmentForm);
			identityAssessmentVC.put("assessmentForm", assessmentForm.getInitialComponent());
		}
		
		putInitialPanel(identityAssessmentVC);
	}
	
	@Override
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public CourseNode getCourseNode() {
		return courseNode;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == detailsEditController) {
			// reset SCORM test
			if(event == Event.CHANGED_EVENT) {
				assessmentForm.reloadData();
			} else if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(event instanceof OpenSubDetailsEvent) {
				removeAsListenerAndDispose(subDetailsController);
				
				OpenSubDetailsEvent detailsEvent = (OpenSubDetailsEvent)event;
				subDetailsController = detailsEvent.getSubDetailsController();
				listenTo(subDetailsController);
				stackPanel.pushController(translate("sub.details"), subDetailsController);
			}
		} else if(assessmentForm == source) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		 
	}
}
