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
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.OpenSubDetailsEvent;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.ui.AssessedIdentityController;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
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
	private Controller identityInfosCtrl;
	private Controller subDetailsController;
	private Controller detailsEditController;

	private LockResult lockEntry;
	private final CourseNode courseNode;
	private final Identity assessedIdentity;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnvironment;
	private final boolean showTitle;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentIdentityCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			Identity assessedIdentity, boolean courseNodeDetails, boolean showTitle) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentModule.class, ureq.getLocale()));
		
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.assessedIdentity = assessedIdentity;
		this.coachCourseEnv = coachCourseEnv;
		this.showTitle = showTitle;

		ICourse course = CourseFactory.loadCourse(courseEntry);
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		assessedUserCourseEnvironment = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment(),
				coachCourseEnv.getCourseReadOnlyDetails());
		assessedUserCourseEnvironment.getScoreAccounting().evaluateAll();
		
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		identityAssessmentVC = createVelocityContainer("identity_personal_node_infos");
		initDetails();
		
		identityInfosCtrl = new AssessedIdentityLargeInfosController(ureq, wControl, assessedIdentity, course, courseNode);
		listenTo(identityInfosCtrl);
		identityAssessmentVC.put("identityInfos", identityInfosCtrl.getInitialComponent());

		//acquire lock and show dialog box on failure.
		String lockSubKey = lockKey(courseNode, assessedIdentity);
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(course, ureq.getIdentity(), lockSubKey, getWindow());
		if(!lockEntry.isSuccess()) {
			String msg = DialogBoxUIFactory.getLockedMessage(ureq, lockEntry.getLockEntry(), "assessmentLock", getTranslator());
			getWindowControl().setWarning(msg);
		} else {
			// Add the users details controller
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			if (assessmentConfig.hasEditableDetails() && courseNodeDetails) {
				detailsEditController = courseAssessmentService.getDetailsEditController(ureq, wControl, stackPanel,
						courseNode, coachCourseEnv, assessedUserCourseEnvironment);
				listenTo(detailsEditController);
				identityAssessmentVC.put("details", detailsEditController.getInitialComponent());
			}
			
			assessmentForm = new AssessmentForm(ureq, wControl, courseNode, coachCourseEnv, assessedUserCourseEnvironment);
			listenTo(assessmentForm);
			identityAssessmentVC.put("assessmentForm", assessmentForm.getInitialComponent());
			
			String nodeLog = courseAssessmentService.getAuditLog(courseNode, assessedUserCourseEnvironment);
			if(StringHelper.containsNonWhitespace(nodeLog)) {
				identityAssessmentVC.contextPut("log", StringHelper.escapeHtml(nodeLog));
			}
		}
		putInitialPanel(identityAssessmentVC);
	}
	
	public static String lockKey(CourseNode node, IdentityRef identity) {
		return "AssessmentLock-NID::" + node.getIdent() + "-IID::" + identity.getKey();
	}
	
	public UserCourseEnvironment getCoachCourseEnvironment() {
		return coachCourseEnv;
	}
	
	public UserCourseEnvironment getAssessedUserCourseEnvironment() {
		return assessedUserCourseEnvironment;
	}
	
	private void initDetails() {
		identityAssessmentVC.contextPut("showTitle", Boolean.valueOf(showTitle));
		identityAssessmentVC.contextPut("courseNode", courseNode.getShortTitle());
		
		String courseNodeCssClass = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		identityAssessmentVC.contextPut("courseNodeCss", courseNodeCssClass);
		
		ModuleConfiguration modConfig = courseNode.getModuleConfiguration();
		String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
		infoCoach = Formatter.formatLatexFormulas(infoCoach);
		identityAssessmentVC.contextPut("infoCoach", infoCoach);
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
		releaseEditorLock();
        super.doDispose();
	}
	
	private void releaseEditorLock() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			// release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
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
				fireEvent(ureq, event);
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
			if(detailsEditController instanceof AssessmentFormCallback) {
				if(AssessmentFormEvent.ASSESSMENT_DONE.equals(event.getCommand())) {
					((AssessmentFormCallback)detailsEditController).assessmentDone(ureq);
				} else if(AssessmentFormEvent.ASSESSMENT_REOPEN.equals(event.getCommand())) {
					((AssessmentFormCallback)detailsEditController).assessmentReopen(ureq);	
				}
			}
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
