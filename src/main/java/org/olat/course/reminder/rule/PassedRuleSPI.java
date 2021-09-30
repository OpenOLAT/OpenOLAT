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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
import org.olat.course.reminder.ui.PassedRuleEditor;
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
public class PassedRuleSPI implements FilterRuleSPI, CourseNodeRuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(PassedRuleSPI.class);
	
	public enum Status {
		
		gradedPassed,
		gradedFailed,
		notGraded;
		
		private final static List<String> NAMES = Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList());
		
		public static final Set<Status> split(String values) {
			Set<Status> status = new HashSet<>();
			if (StringHelper.containsNonWhitespace(values)) {
				for (String value : values.split(",")) {
					if (NAMES.contains(value)) {
						status.add(Status.valueOf(value));
					} else if ("passed".equals(value)) {
						// backward compatibility
						status.add(Status.gradedPassed);
					} else if("failed".equals(value)) {
						// backward compatibility: failed was "not passed"
						status.add(Status.gradedFailed);
						status.add(Status.notGraded);
					}
				}
			}
			return status;
		}
		
		public static final String join(Set<Status> status) {
			return status.stream().map(Status::name).collect(Collectors.joining(","));
		}
		
	}
	
	@Autowired
	private ReminderRuleDAO helperDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	@Override
	public int getSortValue() {
		return 302;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.passed";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(PassedRuleEditor.class, locale);
			String nodeIdent = r.getLeftOperand();
			String statusValue = r.getRightOperand();
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				courseNode = course.getEditorTreeModel().getCourseNode(nodeIdent);
				if (courseNode == null) {
					return null;
				}
			}
			
			Set<Status> status = Status.split(statusValue);
			StringBuilder statusText = new StringBuilder();
			boolean separator = false;
			if (status.contains(Status.gradedPassed)) {
				statusText.append(translator.translate("passed"));
				separator = true;
			}
			if (status.contains(Status.gradedFailed)) {
				if (separator) {
					statusText.append(translator.translate("rule.passed.separator"));
				}
				statusText.append(translator.translate("failed"));
				separator = true;
			}
			if (status.contains(Status.notGraded)) {
				if (separator) {
					statusText.append(translator.translate("rule.passed.separator"));
				}
				statusText.append(translator.translate("not.graded"));
			}
			
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), statusText.toString() };
			return translator.translate("rule.passed.text", args);
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new PassedRuleEditor(rule, entry);
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
			String statusValue = r.getRightOperand();
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				identities.clear();
				log.error("Passed rule in course " + entry.getKey() + " (" + entry.getDisplayname() + ") is missing a course element");
				return;
			}
			
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			if(Mode.none != assessmentConfig.getPassedMode()) {
				if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
					List<Long> excludedIdentityKeys = assessmentService.getExcludedIdentityKeys(entry, courseNode.getIdent());
					if (!excludedIdentityKeys.isEmpty()) {
						identities.removeIf(identity -> excludedIdentityKeys.contains(identity.getKey()));
					}
				}
				
				Set<Status> status = Status.split(statusValue);
				List<Long> matchedIdentityKeys = helperDao.getPassed(entry, courseNode, identities, status);
				identities.removeIf(identity -> !matchedIdentityKeys.contains(identity.getKey()));
			} else {
				// The rule is invalid if the curse node cannot be passed (anymore). Send no reminder at all.
				identities.clear();
			}
		}
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
