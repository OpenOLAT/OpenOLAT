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
package org.olat.course.nodes.form.ui;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.course.learningpath.ui.CoachedIdentityLargeInfosController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationController extends BasicController {

	private static final EmptyStateConfig EMPTY_STATE = EmptyStateConfig.builder()
			.withIconCss(FormCourseNode.ICON_CSS)
			.withMessageI18nKey("form.not.filled.in")
			.build();
	
	private final VelocityContainer mainVC;
	private Dropdown runsDropdown;

	private CoachedIdentityLargeInfosController coachedIdentityLargeInfosCtrl;
	private EvaluationFormExecutionController executionCtrl;
	
	@Autowired
	private FormManager formManager;

	public FormParticipationController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachedCourseEnv, boolean displayUser) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("participation");
		String courseTitle = coachedCourseEnv.getCourseEnvironment().getCourseTitle();
		mainVC.contextPut("courseTitle", courseTitle);
		
		if (displayUser) {
			coachedIdentityLargeInfosCtrl = new CoachedIdentityLargeInfosController(ureq, wControl, coachedCourseEnv);
			listenTo(coachedIdentityLargeInfosCtrl);
			mainVC.put("user", coachedIdentityLargeInfosCtrl.getInitialComponent());
		}
		
		RepositoryEntry courseEntry = coachedCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdent);
		
		List<EvaluationFormParticipation> participations = null;
		
		if (courseNode.getModuleConfiguration().getBooleanSafe(FormCourseNode.CONFIG_KEY_MULTI_PARTICIPATION)) {
			participations = formManager.loadParticipations(survey, coachedCourseEnv.getIdentityEnvironment().getIdentity())
					.stream()
					.filter(participation -> participation.getStatus() == EvaluationFormParticipationStatus.done)
					.sorted((p1, p2) -> Integer.compare(p2.getRun(), p1.getRun()))
					.toList();
		} else {
			EvaluationFormParticipation lastParticipation = formManager.loadLastParticipation(survey,
					coachedCourseEnv.getIdentityEnvironment().getIdentity());
			participations = lastParticipation != null && lastParticipation.getStatus() == EvaluationFormParticipationStatus.done
					? List.of(lastParticipation)
					: List.of();
		}
		
		if (!participations.isEmpty()) {
			if (participations.size() == 1) {
				doSelectParticipation(ureq, participations.get(participations.size() - 1));
			} else {
				runsDropdown = new Dropdown("runs", null, true, getTranslator());
				runsDropdown.setButton(true);
				runsDropdown.setEmbbeded(true);
				runsDropdown.setOrientation(DropdownOrientation.right);
				mainVC.put(runsDropdown.getComponentName(), runsDropdown);
				
				Map<Long, Date> participationKeyToSubmissionDate = formManager.getSessions(participations)
						.stream()
						.collect(Collectors.toMap(
								session -> session.getParticipation().getKey(),
								EvaluationFormSession::getSubmissionDate));
				ParticipationSubmissionDate lastParticipationSubmissionDate = null;
				for (EvaluationFormParticipation participation : participations) {
					Date submissionDate = participationKeyToSubmissionDate.get(participation.getKey());
					ParticipationSubmissionDate participationSubmissionDate = new ParticipationSubmissionDate(participation, submissionDate);
					if (lastParticipationSubmissionDate == null) {
						lastParticipationSubmissionDate = participationSubmissionDate;
					}
					
					Link link = LinkFactory.createLink("run." + participation.getRun(), "run", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
					link.setCustomDisplayText(getTranslatedRunName(participationSubmissionDate));
					link.setUserObject(participationSubmissionDate);
					runsDropdown.addComponent(link);
				}
				doSelectParticipation(ureq, lastParticipationSubmissionDate);
			}
		} else {
			EmptyStateFactory.create("emptyState", mainVC, this, EMPTY_STATE);
		}
		
		putInitialPanel(mainVC);
	}
	
	private String getTranslatedRunName(ParticipationSubmissionDate participationSubmissionDate) {
		return translate("submission.number",
				String.valueOf(participationSubmissionDate.participation.getRun()),
				Formatter.getInstance(getLocale()).formatDate(participationSubmissionDate.submissionDate()));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (link.getUserObject() instanceof ParticipationSubmissionDate participationSubmissionDate) {
				doSelectParticipation(ureq, participationSubmissionDate);
			}
		}
	}
	
	private void doSelectParticipation(UserRequest ureq, ParticipationSubmissionDate participationSubmissionDate) {
		runsDropdown.setTranslatedLabel(getTranslatedRunName(participationSubmissionDate));
		
		doSelectParticipation(ureq, participationSubmissionDate.participation());
	}
	
	private void doSelectParticipation(UserRequest ureq, EvaluationFormParticipation participation) {
		EvaluationFormSession session = formManager.loadOrCreateSession(participation);
		if (session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, null, true, false,
					false, FormCourseNode.EMPTY_STATE);
			listenTo(executionCtrl);
			mainVC.put("evaluationForm", executionCtrl.getInitialComponent());
		}
	}
	
	private record ParticipationSubmissionDate(EvaluationFormParticipation participation, Date submissionDate) { }
	
}
