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
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.manager.ReminderRuleDAO;
import org.olat.course.reminder.ui.ScoreRuleEditor;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentObligation;
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
public class ScoreRuleSPI implements FilterRuleSPI, CourseNodeRuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(ScoreRuleSPI.class);
	private static final double ROUND = 0.000001d;
	
	@Autowired
	private ReminderRuleDAO helperDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	@Override
	public int getSortValue() {
		return 303;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.score";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ScoreRuleEditor.class, locale);
			String nodeIdent = r.getLeftOperand();
			String operator = r.getOperator();
			String score = r.getRightOperand();
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				courseNode = course.getEditorTreeModel().getCourseNode(nodeIdent);
				if (courseNode == null) {
					return null;
				}
			}
			
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), score };
			switch(operator) {
				case "<": return translator.translate("rule.score.less", args);
				case "<=": return translator.translate("rule.score.less.equals", args);
				case "=": return translator.translate("rule.score.equals", args);
				case "=>": return translator.translate("rule.score.greater.equals", args);
				case ">": return translator.translate("rule.score.greater", args);
				case "!=": return translator.translate("rule.score.not.equals", args);
			}
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new ScoreRuleEditor(rule, entry);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public void filter(RepositoryEntry entry, List<Identity> identities, ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String nodeIdent = r.getLeftOperand();
			String operator = r.getOperator();
			float value = Float.parseFloat(r.getRightOperand());
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				identities.clear();
				log.error("Score rule in course " + entry.getKey() + " (" + entry.getDisplayname() + ") is missing a course element");
				return;
			}
			
			Map<Long, Float> scores;
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, courseNode);
			if(Mode.none == assessmentConfig.getScoreMode()) {
				// The rule is invalid if the curse node has no score (anymore). Send no reminder at all.
				identities.clear();
				return;
			}
			
			if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
				List<Long> excludedIdentityKeys = assessmentService.getIdentityKeys(entry, courseNode.getIdent(), AssessmentObligation.EXCLUDED);
				if (!excludedIdentityKeys.isEmpty()) {
					identities.removeIf(identity -> excludedIdentityKeys.contains(identity.getKey()));
				}
			}
			
			scores = helperDao.getScores(entry, courseNode, identities);
			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				Float score = scores.get(identity.getKey());
				if(score == null) {
					if(!operator.equals("!=")) {//always different
						identityIt.remove();
					}
				} else if(!evaluateScore(score.floatValue(), operator, value)) {
					identityIt.remove();
				}
			}
		}
	}
	
	private boolean evaluateScore(float score, String operator, float value) {
		boolean eval = false;
		switch(operator) {
			case "<": eval = score < value; break;
			case "<=": eval = Math.abs(score - value) < ROUND || score <= value; break;
			case "=": eval = Math.abs(score - value) < ROUND; break;
			case "=>": eval = Math.abs(score - value) < ROUND || score >= value; break;
			case ">": eval = score > value;  break;
			case "!=": eval = Math.abs(score - value) > ROUND; break;
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
