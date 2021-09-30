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
package org.olat.course.reminder.rule;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.manager.ReminderRuleDAO;
import org.olat.course.reminder.ui.AttemptsRuleEditor;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.reminder.FilterRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AttemptsRuleSPI implements FilterRuleSPI, CourseNodeRuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(AttemptsRuleSPI.class);
	
	@Autowired
	private ReminderRuleDAO helperDao;
	@Autowired
	private AssessmentService assessmentService;
	
	@Override
	public int getSortValue() {
		return 301;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.attempts";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(AttemptsRuleEditor.class, locale);
			String nodeIdent = r.getLeftOperand();
			String operator = r.getOperator();
			String attempts = r.getRightOperand();
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				courseNode = course.getEditorTreeModel().getCourseNode(nodeIdent);
				if (courseNode == null) {
					return null;
				}
			}
			
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), attempts };
			switch(operator) {
				case "<": return translator.translate("rule.attempts.less", args);
				case "<=": return translator.translate("rule.attempts.less.equals", args);
				case "=": return translator.translate("rule.attempts.equals", args);
				case "=>": return translator.translate("rule.attempts.greater.equals", args);
				case ">": return translator.translate("rule.attempts.greater", args);
				case "!=": return translator.translate("rule.attempts.not.equals", args);
			}
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new AttemptsRuleEditor(rule, entry);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		//the node ident must be the same
		return rule.clone();
	}

	@Override
	public void filter(RepositoryEntry entry, List<Identity> identities, ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String nodeIdent = r.getLeftOperand();
			String operator = r.getOperator();
			int value = Integer.parseInt(r.getRightOperand());
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				identities.clear();
				log.warn("Attempts rule in course {} ({}) is missing a course element", entry.getKey(), entry.getDisplayname());
				return;
			}
			
			if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
				List<Long> excludedIdentityKeys = assessmentService.getExcludedIdentityKeys(entry, courseNode.getIdent());
				if (!excludedIdentityKeys.isEmpty()) {
					identities.removeIf(identity -> excludedIdentityKeys.contains(identity.getKey()));
				}
			}
			
			Map<Long, Integer> attempts = helperDao.getAttempts(entry, courseNode, identities);
			
			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				Integer attempt = attempts.get(identity.getKey());
				if(attempt == null) {
					attempt = 0;
				}
				if(!evaluateAttempt(attempt.intValue(), operator, value)) {
					identityIt.remove();
				}
			}
		}
	}
	
	private boolean evaluateAttempt(int attempt, String operator, int value) {
		boolean eval = false;
		switch(operator) {
			case "<": eval = attempt < value; break;
			case "<=": eval = attempt <= value; break;
			case "=": eval = attempt == value; break;
			case "=>": eval = attempt >= value; break;
			case ">": eval = attempt > value;  break;
			case "!=": eval = attempt != value; break;
		}
		return eval;
	}
	
	@Override
	public String getCourseNodeIdent(ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			return r.getLeftOperand();
		}
		return null;
	}
	
}
