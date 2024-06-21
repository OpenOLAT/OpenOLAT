/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.TaskReviewAssignmentImpl;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.user.UserAvatarMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEvaluationFormExecutionController extends BasicController {
	
	private Link backLink;
	private final VelocityContainer mainVC;
	
	private Task task;
	private final boolean edit;
	private final boolean editedByCoach;
	private final GTACourseNode gtaNode;
	private final MapperKey avatarMapperKey;
	private TaskReviewAssignment assignment;
	private final EvaluationFormSurvey survey;
	private final CourseEnvironment courseEnv;
	
	private RatingController ratingCtrl;
	private final EvaluationFormExecutionController executionCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public GTAEvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, TaskReviewAssignment assignment,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, GTAEvaluationFormExecutionOptions options,
			boolean edit, boolean editedByCoach) {
		super(ureq, wControl, Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.edit = edit;
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.assignment = assignment;
		this.editedByCoach = editedByCoach;
		avatarMapperKey =  mapperService.register(ureq.getUserSession(), new UserAvatarMapper(true));

		Identity reviewer = assignment.getAssignee();
		task = gtaManager.getTask(assignment.getTask());
		survey = task.getSurvey();
		EvaluationFormParticipation participation = assignment.getParticipation();
		if(participation == null) {
			participation = peerReviewManager.loadOrCreateParticipation(survey, reviewer);
			((TaskReviewAssignmentImpl)assignment).setParticipation(participation);
			assignment = peerReviewManager.updateAssignment(assignment);
		}
		
		boolean showCancel = !edit && !options.withRating();
		EvaluationFormSession session = peerReviewManager.loadOrCreateSession(participation);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session, null, null,
				!edit, edit, showCancel, false, null);
		executionCtrl.setSaveDisplayText(translate("quick.save"));
		executionCtrl.setDoneI18nKey("save.as.rating");
		listenTo(executionCtrl);
		
		mainVC = createVelocityContainer("evaluation_form_review");	
		mainVC.put("form-execution", executionCtrl.getInitialComponent());
		if(options.withBackButton()) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
		}
		if(options.withDocuments()) {
			initDocuments(ureq, options.getPlaceHolderName(), options.isAnonym());
		}
		if(options.withAssessedIdentityHeader()) {
			initAssessedIdentityHeader(ureq, assignment, gtaNode, options.getPlaceHolderName(), options.isAnonym());
		}
		if(options.withReviewerHeader()) {
			initReviewerHeader(ureq, assignment, options.getPlaceHolderName(), options.isAnonym());
		}
		if(options.withRating()) {
			initRating(ureq);
		}
		putInitialPanel(mainVC);
	}
	
	private void initRating(UserRequest ureq) {
		ratingCtrl = new RatingController(ureq, getWindowControl());
		listenTo(ratingCtrl);
		mainVC.put("rating", ratingCtrl.getInitialComponent());
	}
	
	private void initAssessedIdentityHeader(UserRequest ureq, TaskReviewAssignment assignment,
			GTACourseNode gtaNode, String placeholderName, boolean anonym) {
		Identity user = anonym ? null : task.getIdentity();
		GTAAssessedIdentityInformationsController userInfosCtrl = new GTAAssessedIdentityInformationsController(ureq, getWindowControl(),
				avatarMapperKey, assignment.getStatus(), gtaNode, user, placeholderName, anonym);
		listenTo(userInfosCtrl);
		mainVC.put("assessed.identity.infos", userInfosCtrl.getInitialComponent());
	}
	
	private void initReviewerHeader(UserRequest ureq, TaskReviewAssignment assignment, String placeholderName, boolean anonym) {
		Identity user = anonym ? null : assignment.getAssignee();
		GTAReviewerIdentityInformationsController userInfosCtrl = new GTAReviewerIdentityInformationsController(ureq, getWindowControl(),
				avatarMapperKey, user, placeholderName, anonym);
		listenTo(userInfosCtrl);
		mainVC.put("reviewer.infos", userInfosCtrl.getInitialComponent());
	}
	
	private void initDocuments(UserRequest ureq, String fullName, boolean anonym) {
		VFSContainer submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, task.getIdentity());
		submitContainer.setLocalSecurityCallback(new ReadOnlyCallback());
		GTADocumentsController documentsCtrl = new GTADocumentsController(ureq, getWindowControl(),
				avatarMapperKey, submitContainer, fullName, anonym);
		listenTo(documentsCtrl);
		mainVC.put("documents", documentsCtrl.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(executionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				if(edit) {
					updateAssignmentAndScore();
				}
				fireEvent(ureq, event);
			}
		} else if(ratingCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateAssignmentRating(ratingCtrl.getRating());
			}
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
	
	private void updateAssignmentAndScore() {
		assignment = peerReviewManager.loadAssignment(assignment);
		TaskReviewAssignmentStatus assignmentStatus = assignment.getStatus();
		if(assignmentStatus == TaskReviewAssignmentStatus.open || assignmentStatus == TaskReviewAssignmentStatus.inProgress) {
			EvaluationFormParticipation participation = peerReviewManager.loadParticipation(survey, assignment.getAssignee());
			EvaluationFormSession session = peerReviewManager.loadSession(participation);
			if(participation.getStatus() == EvaluationFormParticipationStatus.done
					|| session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
				updateAssignmentStatus(TaskReviewAssignmentStatus.done);
			} else if(assignmentStatus == TaskReviewAssignmentStatus.open) {
				updateAssignmentStatus(TaskReviewAssignmentStatus.inProgress);
			}
		}
		
		if(assignment.getStatus() == TaskReviewAssignmentStatus.done) {
			gtaNode.recalculateAndUpdateScore(courseEnv, task.getIdentity(), getIdentity(), getLocale());
			if(!editedByCoach) {
				gtaNode.recalculateAndUpdateScore(courseEnv, assignment.getAssignee(), getIdentity(), getLocale());
			}
		}
	}
	
	private void updateAssignmentStatus(TaskReviewAssignmentStatus newStatus) {
		assignment.setStatus(newStatus);
		assignment = peerReviewManager.updateAssignment(assignment);
		dbInstance.commit();
	}
	
	private void updateAssignmentRating(float rating) {
		assignment.setRating(rating);
		assignment = peerReviewManager.updateAssignment(assignment);
		dbInstance.commit();
	}
	
	private class RatingController extends FormBasicController {
		
		private RatingFormItem ratingEl;
		
		public RatingController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "evaluation_rating", Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
			
			initForm(ureq);
		}
		
		public float getRating() {
			return ratingEl.getCurrentRating();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			Float ratingVal = assignment.getRating();
			float initialVal = ratingVal == null ? 0 : ratingVal.floatValue();
			
			String qualityFeedbackType = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE,
					GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO);
			if(GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS.equals(qualityFeedbackType)) {
				ratingEl = uifactory.addRatingItem("rating", null, initialVal, 5, true, formLayout);
			} else {
				ratingEl = uifactory.addRatingItemYesNo("rating", null, initialVal, 5, true, formLayout);
			}
			
			uifactory.addFormSubmitButton("close", formLayout);
		}

		@Override
		protected void formCancelled(UserRequest ureq) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
