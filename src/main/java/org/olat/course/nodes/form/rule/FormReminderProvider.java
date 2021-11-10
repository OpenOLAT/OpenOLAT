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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.reminder.CourseNodeReminderProvider;

/**
 * 
 * Initial date: 09.06.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormReminderProvider implements CourseNodeReminderProvider {
	
	private final String nodeIdent;
	private final DueDateConfig participationDueDateConfig;
	private List<String> mainTypes;
	
	public FormReminderProvider(FormCourseNode formNode) {
		this.nodeIdent = formNode.getIdent();
		this.participationDueDateConfig = formNode.getDueDateConfig(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE);
	}

	@Override
	public String getCourseNodeIdent() {
		return nodeIdent;
	}

	@Override
	public boolean filter(Collection<String> ruleNodeIdents) {
		return ruleNodeIdents.contains(nodeIdent);
	}
	
	@Override
	public Collection<String> getMainRuleSPITypes() {
		if (mainTypes == null) {
			mainTypes = new ArrayList<>(1);
			
			if (DueDateConfig.isDueDate(participationDueDateConfig)) {
				mainTypes.add(FormParticipationRuleSPI.class.getSimpleName());
			}
		}
		
		return mainTypes;
	}

	@Override
	public String getDefaultMainRuleSPIType(List<String> availableRuleTypes) {
		if (availableRuleTypes.contains(FormParticipationRuleSPI.class.getSimpleName())) {
			return FormParticipationRuleSPI.class.getSimpleName();
		}
		return null;
	}
	
	@Override
	public void refresh() {
		mainTypes = null;
	}
	
}