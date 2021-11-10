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
package org.olat.course.nodes.form.rule;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.ui.FormBeforeDueDateRuleEditor;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.rule.AbstractDueDateRuleSPI;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FormParticipationRuleSPI extends AbstractDueDateRuleSPI implements CourseNodeRuleSPI {

	@Autowired
	private FormManager formManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Override
	public int getSortValue() {
		return 1100;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.form.participation";
	}

	@Override
	protected String getStaticTextPrefix() {
		return "rule.participation.";
	}

	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new FormBeforeDueDateRuleEditor(rule, entry, FormParticipationRuleSPI.class.getSimpleName());
	}
	
	@Override
	protected List<Identity> getPeopleToRemind(RepositoryEntry courseEntry, CourseNode courseNode) {
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdent);
		List<Identity> participants = formManager.getParticipations(survey, EvaluationFormParticipationStatus.done, true).stream()
				.map(EvaluationFormParticipation::getExecutor)
				.collect(Collectors.toList());
		
		List<Identity> identities = repositoryEntryRelationDao.getMembers(courseEntry, RepositoryEntryRelationType.all,
				GroupRoles.participant.name());
		for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
			Identity identity = identityIt.next();
			if(participants.contains(identity)) {
				identityIt.remove();
			}
		}
		
		return identities;
	}
	
	@Override
	public String getCourseNodeIdent(ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			return r.getLeftOperand();
		}
		return null;
	}

	@Override
	protected DueDateConfig getDueDateConfig(CourseNode courseNode) {
		if (courseNode instanceof FormCourseNode) {
			FormCourseNode formCourseNode = (FormCourseNode)courseNode;
			return formCourseNode.getDueDateConfig(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE);
		}
		return DueDateConfig.noDueDateConfig();
	}

}
