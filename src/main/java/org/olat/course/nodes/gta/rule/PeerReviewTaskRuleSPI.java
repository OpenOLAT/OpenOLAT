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
package org.olat.course.nodes.gta.rule;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.ui.BeforeDateTaskRuleEditor;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PeerReviewTaskRuleSPI extends AbstractDueDateTaskRuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(PeerReviewTaskRuleSPI.class);
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	@Override
	public int getSortValue() {
		return 1002;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.peerreview.task";
	}
	
	@Override
	protected String getStaticTextPrefix() {
		return "rule.peerreview.";
	}
	
	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new BeforeDateTaskRuleEditor(rule, entry, PeerReviewTaskRuleSPI.class.getSimpleName());
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		//node ident must be the same
		return rule.clone();
	}

	@Override
	protected DueDateConfig getDueDateConfig(CourseNode courseNode) {
		if (courseNode instanceof GTACourseNode gtaCourseNode) {
			return gtaCourseNode.getDueDateConfig(GTACourseNode.GTASK_PEER_REVIEW_DEADLINE);
		}
		return DueDateConfig.noDueDateConfig();
	}

	@Override
	protected List<Identity> getPeopleToRemind(RepositoryEntry entry, CourseNode courseNode) {
		if (courseNode instanceof GTACourseNode gtaCourseNode
				&& GTAType.individual.name().equals(courseNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			TaskList taskList = gtaManager.getTaskList(entry, gtaCourseNode);
			List<Identity> assignees = peerReviewManager.getAssigneesToRemind(taskList, gtaCourseNode);
			log.debug("{} assignees found to remind for course element: {}", assignees.size(), gtaCourseNode.getShortTitle());
			return assignees;
		}
		return Collections.emptyList();
	}

	@Override
	protected boolean isRuleDone(Task task) {
		return task != null;
	}
}
