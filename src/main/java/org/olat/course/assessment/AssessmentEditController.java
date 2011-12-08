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
* <p>
*/ 

package org.olat.course.assessment;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.ICourse;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<BR>
 * Edit controller to change a users assessment for a particular course node.
 * Make sure when using this controller that the current user has the permission
 * to edit the assessed users assessment. This controller does not check for
 * security. When finished this controller fires a Event.CANCELLED_EVENT or a
 * Event.CHANGED_EVENT and a global assessment_changed_event //TODO doku events
 * <BR>
 * When finished do not forget to call doDispose() to release the edit lock!
 * <P>
 * 
 * Initial Date: Oct 28, 2004
 * @author gnaegi
 */
public class AssessmentEditController extends BasicController {
	
	private VelocityContainer detailView;
	private AssessmentForm assessmentForm;
	private Controller detailsEditController;
	private AssessedIdentityWrapper assessedIdentityWrapper;
	private AssessableCourseNode courseNode;

	private Link backLink;
	private Link hideLogButton;
	private Link showLogButton;
	private LockResult lockEntry;
	private DialogBoxController alreadyLockedDialogController;

	/**
	 * Constructor for the identity assessment edit controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param course
	 * @param courseNode The assessable course node
	 * @param assessedIdentityWrapper The wrapped assessed identity
	 */
	public AssessmentEditController(UserRequest ureq, WindowControl wControl, ICourse course, AssessableCourseNode courseNode,
			AssessedIdentityWrapper assessedIdentityWrapper) {
		super(ureq, wControl);
		this.assessedIdentityWrapper = assessedIdentityWrapper;
		this.courseNode = courseNode;
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		if (courseNode == null) { throw new OLATRuntimeException(AssessmentEditController.class,
				"Can not initialize the assessment detail view when the current course node is null", null); }
		if (assessedIdentityWrapper == null) { throw new OLATRuntimeException(AssessmentEditController.class,
				"Can not initialize the assessment detail view when the current assessedIdentityWrapper is null", null); }

		//acquire lock and show dialog box on failure.
		String lockSubKey = "AssessmentLock-NID::" + courseNode.getIdent() + "-IID::" + assessedIdentityWrapper.getIdentity().getKey();
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(course, ureq.getIdentity(), lockSubKey);
		if (lockEntry.isSuccess()) {
			// Initialize the assessment detail view
			detailView = createVelocityContainer("detailview");
			hideLogButton = LinkFactory.createButtonSmall("command.hidelog", detailView, this);
			showLogButton = LinkFactory.createButtonSmall("command.showlog", detailView, this);
			backLink = LinkFactory.createLinkBack(detailView, this);
			
			// Add the user object to the view
			Identity assessedIdentity = assessedIdentityWrapper.getIdentity();
			detailView.contextPut("user", assessedIdentity.getUser());
			// Add the coaching info message
			ModuleConfiguration modConfig = courseNode.getModuleConfiguration();
			String infoCoach = (String) modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH);
			infoCoach = Formatter.formatLatexFormulas(infoCoach);
			detailView.contextPut("infoCoach", infoCoach);
			// Add the assessment details form
			assessmentForm = new AssessmentForm(ureq, wControl, courseNode, assessedIdentityWrapper);
			listenTo(assessmentForm);
			
			detailView.put("assessmentform", assessmentForm.getInitialComponent());
			// Add user log. Get it from user properties
			UserCourseEnvironment uce = assessedIdentityWrapper.getUserCourseEnvironment();
			String nodeLog = courseNode.getUserLog(uce);
			detailView.contextPut("log", nodeLog);
			// Add the users details controller
			if (courseNode.hasDetails()) {
				detailView.contextPut("hasDetails", Boolean.TRUE);
				detailsEditController = courseNode.getDetailsEditController(ureq, wControl, uce);
				listenTo(detailsEditController);
				detailView.put("detailsController", detailsEditController.getInitialComponent());
			} else {
				detailView.contextPut("hasDetails", Boolean.FALSE);
			}

			// push node for page header
			detailView.contextPut("courseNode", courseNode);
			// push node css class
			detailView.contextPut("courseNodeCss", CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());

			// push infos about users groups
			List<BusinessGroup> participantGroups = course.getCourseEnvironment().getCourseGroupManager().getParticipatingLearningGroupsFromAllContexts(
					assessedIdentity);
			final Collator collator = Collator.getInstance(ureq.getLocale());
			Collections.sort(participantGroups, new Comparator<BusinessGroup>() {
				public int compare(BusinessGroup a, BusinessGroup b) {
					return collator.compare(a.getName(), b.getName());
				}
			});
			detailView.contextPut("participantGroups", participantGroups);
			detailView.contextPut("noParticipantGroups", (participantGroups.size() > 0 ? Boolean.FALSE : Boolean.TRUE));

			putInitialPanel(detailView);
		}else{
			//lock was not successful !
			alreadyLockedDialogController = DialogBoxUIFactory.createResourceLockedMessage(ureq, wControl, lockEntry, "assessmentLock", getTranslator());
			listenTo(alreadyLockedDialogController);
			alreadyLockedDialogController.activate();
			//no initial component set -> empty behind dialog box!
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink){
			releaseEditorLock();
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == hideLogButton) {
			detailView.contextPut("showLog", Boolean.FALSE);
		} else if (source == showLogButton) {
			detailView.contextPut("showLog", Boolean.TRUE);
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == assessmentForm) {
			if (event == Event.CANCELLED_EVENT) {
				releaseEditorLock();
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if (event == Event.DONE_EVENT) {
				releaseEditorLock();
				doUpdateAssessmentData(ureq.getIdentity());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == detailsEditController) {
			//fxdiff FXOLAT-108: reset SCORM test
			if(event == Event.CHANGED_EVENT) {
				doUpdateAssessmentData(ureq.getIdentity());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == alreadyLockedDialogController) {
			if (event == Event.CANCELLED_EVENT || DialogBoxUIFactory.isOkEvent(event)) {
				//ok clicked or box closed
				releaseEditorLock();
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	/**
	 * Persists the changed form data in the user node properties and updates the
	 * wrapped identity that has been used to initialize the form
	 * 
	 * @param coachIdentity The identity of the coach who changes the users values
	 *          (will be written to the user node log)
	 */
	protected void doUpdateAssessmentData(Identity coachIdentity) {
		UserCourseEnvironment userCourseEnvironment = assessedIdentityWrapper.getUserCourseEnvironment();
		ScoreEvaluation scoreEval = null;
		Float newScore = null;
		Boolean newPassed = null;
		//String userName = userCourseEnvironment.getIdentityEnvironment().getIdentity().getName();

		if (assessmentForm.isHasAttempts() && assessmentForm.isAttemptsDirty()) {
			this.courseNode.updateUserAttempts(new Integer(assessmentForm.getAttempts()), userCourseEnvironment, coachIdentity);
		}

		if (assessmentForm.isHasScore() && assessmentForm.isScoreDirty()) {
		//fxdiff VCRP-4: assessment overview with max score
			newScore = assessmentForm.getScore();
			// Update properties in db later... see
			// courseNode.updateUserSocreAndPassed...
		}
		
		if (assessmentForm.isHasPassed()) {
			if (assessmentForm.getCut() != null && assessmentForm.getScore() != null) {
			//fxdiff VCRP-4: assessment overview with max score
				newPassed = assessmentForm.getScore() >= assessmentForm.getCut().floatValue()
				          ? Boolean.TRUE : Boolean.FALSE;
			} else {
        //"passed" info was changed or not 
				String selectedKeyString = assessmentForm.getPassed().getSelectedKey();
				if("true".equalsIgnoreCase(selectedKeyString) || "false".equalsIgnoreCase(selectedKeyString)) {
					newPassed = Boolean.valueOf(selectedKeyString);
				}
				else {
          // "undefined" was choosen
					newPassed = null;
				}				
			}
		}
		// Update score,passed properties in db
		scoreEval = new ScoreEvaluation(newScore, newPassed);
		this.courseNode.updateUserScoreEvaluation(scoreEval, userCourseEnvironment, coachIdentity, false);

		if (assessmentForm.isHasComment() && assessmentForm.isUserCommentDirty()) {
			String newComment = assessmentForm.getUserComment().getValue();
			// Update properties in db
			courseNode.updateUserUserComment(newComment, userCourseEnvironment, coachIdentity);
		}

		if (assessmentForm.isCoachCommentDirty()) {
			String newCoachComment = assessmentForm.getCoachComment().getValue();
			// Update properties in db
			this.courseNode.updateUserCoachComment(newCoachComment, userCourseEnvironment);
		}
		
		// Refresh score view
		userCourseEnvironment.getScoreAccounting().scoreInfoChanged(this.courseNode, scoreEval);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//first release editor lock
		releaseEditorLock();
	}
	
	private void releaseEditorLock() {
		if (lockEntry == null) return;
		
		if (lockEntry.isSuccess()) {
			// release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
		}else {
			removeAsListenerAndDispose(alreadyLockedDialogController);
		}
		lockEntry = null;
	}

}
