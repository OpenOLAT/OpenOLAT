/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.assessment.ui.reset;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Jun 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ResetWizardContext {
	
	private static final ResetDataStep[] STEPS = ResetDataStep.values();
	
	public enum ResetDataStep {
		options,
		courseElements,
		participants,
		coursePassedOverridden,
		coursePassed,
		overview
	}
	
	private final Identity doer;
	private final ResetDataContext dataContext;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolSecurityCallback secCallback;
	private final boolean withOptions;
	private final boolean withCourseNodeSelection;
	private final boolean withParticipantsSelection;
	private final boolean withCoursePassedOverriddenReset;
	private final boolean withCoursePassedReset;
	private List<ResetDataStep> availableSteps;
	private ResetDataStep current;
	
	private final AssessmentToolManager assessmentToolManager;
	
	public ResetWizardContext(Identity doer, ResetDataContext dataContext, UserCourseEnvironment coachCourseEnv,
			AssessmentToolSecurityCallback secCallback, boolean withOptions, boolean withCourseNodeSelection,
			boolean withParticipantsSelection) {
		this.doer = doer;
		this.dataContext = dataContext;
		this.coachCourseEnv = coachCourseEnv;
		this.secCallback = secCallback;
		this.withOptions = withOptions;
		this.withCourseNodeSelection = withCourseNodeSelection;
		this.withParticipantsSelection = withParticipantsSelection;
		this.withCoursePassedReset = isWithCoursePassedReset();
		this.withCoursePassedOverriddenReset = withCoursePassedReset;
		
		assessmentToolManager = CoreSpringFactory.getImpl(AssessmentToolManager.class);
		recalculateAvailableSteps();
	}
	
	public ResetDataContext getDataContext() {
		return dataContext;
	}

	public UserCourseEnvironment getCoachCourseEnv() {
		return coachCourseEnv;
	}

	public AssessmentToolSecurityCallback getSecCallback() {
		return secCallback;
	}

	public boolean isWithCourseNodeSelection() {
		return withCourseNodeSelection;
	}

	public boolean isWithParticipantsSelection() {
		return withParticipantsSelection;
	}

	private boolean isWithCoursePassedReset() {
		if (!LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachCourseEnv).getType())) {
			return false;
		}
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		RepositoryEntry courseEntry = coachCourseEnv.getCourseEnvironment() .getCourseGroupManager().getCourseEntry();
		CourseNode rootNode = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, rootNode);
		
		if (Mode.none == assessmentConfig.getPassedMode()) {
			return false;
		}
		
		return true;
	}
	
	public void setCurrent(ResetDataStep current) {
		this.current = current;
	}
	
	public Step createNextStep(UserRequest ureq, ResetDataStep current) {
		ResetDataStep next = getNext(current);
		
		return switch(next) {
		case courseElements -> new ResetData2CourseElementsStep(ureq, this);
		case participants -> new ResetData3ParticipantsStep(ureq, this);
		case coursePassedOverridden -> new ResetData4CoursePassedOverridenStep(ureq, this);
		case coursePassed -> new ResetData5CoursePassedStep(ureq, this);
		default -> new ResetData6ConfirmationStep(ureq, this);
		};
	}

	public ResetDataStep getNext(ResetDataStep current) {
		for (int i = current.ordinal() + 1; i < STEPS.length; i++) {
			ResetDataStep resetDataStep = STEPS[i];
			if (availableSteps.contains(resetDataStep)) {
				return resetDataStep;
			}
		}
		return null;
	}
	
	public boolean isRecalculationStep(ResetDataStep current) {
		return this.current == current;
	}
	
	public void recalculateAvailableSteps() {
		availableSteps = new ArrayList<>();
		
		if (withOptions) {
			availableSteps.add(ResetDataStep.options);
		}
		
		if (withCourseNodeSelection && ResetCourse.elements == dataContext.getResetCourse()) {
			availableSteps.add(ResetDataStep.courseElements);
		}
		
		if (withParticipantsSelection && ResetParticipants.selected == dataContext.getResetParticipants()) {
			availableSteps.add(ResetDataStep.participants);
		}
		
		if (availableSteps.contains(ResetDataStep.courseElements)) {
			if (ResetParticipants.all == dataContext.getResetParticipants() || !dataContext.getSelectedParticipants().isEmpty()) {
				if (withCoursePassedOverriddenReset) {
					if (isPassedOverriddenParticipantAvailable()) {
						availableSteps.add(ResetDataStep.coursePassedOverridden);
					}
				}
				
				if (withCoursePassedReset) {
					if (isPassedParticipantAvailable()) {
						availableSteps.add(ResetDataStep.coursePassed);
					}
				}
			}
		}
		
		availableSteps.add(ResetDataStep.overview);
	}
	
	/**
	 * @return true if ResetCoursePassedOverriddenController#loadModel() returns at least one row
	 */
	private boolean isPassedOverriddenParticipantAvailable() {
		Set<Long> identityKeys = null;
		if (ResetParticipants.selected == dataContext.getResetParticipants()) {
			identityKeys = dataContext.getSelectedParticipants().stream().map(Identity::getKey).collect(Collectors.toSet());
			if (identityKeys.isEmpty()) {
				return false;
			}
		}
		
		RepositoryEntry courseEntry =  dataContext.getRepositoryEntry();
		String rootIdent = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getIdent();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, rootIdent, null, secCallback);
		params.setPassedOverridden(Boolean.TRUE);
		params.setIdentityKeys(identityKeys);
		
		return !assessmentToolManager.getAssessmentEntries(doer, params, null).isEmpty();
	}
	
	/**
	 * @return true if ResetCoursePassedController#loadModel() returns at least one row
	 */
	private boolean isPassedParticipantAvailable() {	
		Set<Long> identityKeys = null;
	
		if (ResetParticipants.selected == dataContext.getResetParticipants()) {
			identityKeys = dataContext.getSelectedParticipants().stream().map(Identity::getKey).collect(Collectors.toSet());
			if (identityKeys.isEmpty()) {
				return false;
			}
		}
		
		RepositoryEntry courseEntry =  dataContext.getRepositoryEntry();
		String rootIdent = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getIdent();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, rootIdent, null, secCallback);
		params.setIdentityKeys(identityKeys);
		
		
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(doer, params, null);
		for (AssessmentEntry entry : assessmentEntries) {
			Overridable<Boolean> passedOverridable = entry.getPassedOverridable();
			Boolean passed = passedOverridable.getCurrent();
			
			if (passedOverridable.isOverridden()) {
				if (dataContext.getParticipantsResetPasedOverridden() != null && dataContext.getParticipantsResetPasedOverridden().contains(entry.getIdentity())) {
					passed = passedOverridable.getOriginal();
				} else {
					// User has overridden status which is not reset
					continue;
				}
			}
			
			if (passed != null && passed.booleanValue()) {
				return true;
			}
		}
		
		return false;
	}
}
