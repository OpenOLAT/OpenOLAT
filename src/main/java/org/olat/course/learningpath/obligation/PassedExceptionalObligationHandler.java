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
package org.olat.course.learningpath.obligation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.PasseableVisitor;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PassedExceptionalObligationHandler implements ExceptionalObligationHandler {
	
	public static final String TYPE = "passed";

	public enum Status {
		gradedPassed,
		gradedFailed,
		notGraded;
		
		public static final String DELIMITER = ",";
	}
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getSortValue() {
		return 70;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getAddI18nKey() {
		return "exceptional.obligation.passed.add";
	}
	
	@Override
	public boolean isShowAdd(RepositoryEntry courseEntry) {
		return true;
	}

	@Override
	public String getDisplayType(Translator translator, ExceptionalObligation exceptionalObligation) {
		if (exceptionalObligation instanceof PassedExceptionalObligation) {
			PassedExceptionalObligation passedExceptionalObligation = (PassedExceptionalObligation)exceptionalObligation;
			String statusText = getStatusName(translator, passedExceptionalObligation);
			return translator.translate("exceptional.obligation.passed.type", new String[] {statusText});
		}
		return null;
	}

	private String getStatusName(Translator translator, PassedExceptionalObligation exceptionalObligation) {
		List<String> status = Arrays.asList(exceptionalObligation.getStatus().split(Status.DELIMITER));
		
		StringBuilder statusText = new StringBuilder();
		boolean separator = false;
		if (status.contains(Status.gradedPassed.name())) {
			statusText.append(translator.translate("exceptional.obligation.passed"));
			separator = true;
		}
		if (status.contains(Status.gradedFailed.name())) {
			if (separator) {
				statusText.append(translator.translate("exceptional.obligation.separator"));
			}
			statusText.append(translator.translate("exceptional.obligation.failed"));
			separator = true;
		}
		if (status.contains(Status.notGraded.name())) {
			if (separator) {
				statusText.append(translator.translate("exceptional.obligation.separator"));
			}
			statusText.append(translator.translate("exceptional.obligation.not.graded"));
		}
		return statusText.toString();
	}

	@Override
	public String getDisplayName(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof PassedExceptionalObligation) {
			PassedExceptionalObligation passedExceptionalObligation = (PassedExceptionalObligation)exceptionalObligation;
			ICourse course = CourseFactory.loadCourse(courseEntry);
			
			CourseNode courseNode = course.getRunStructure().getNode(passedExceptionalObligation.getCourseNodeIdent());
			if (courseNode == null) {
				courseNode = course.getEditorTreeModel().getCourseNode(passedExceptionalObligation.getCourseNodeIdent());
			}
			
			if (courseNode != null) {
				return courseNode.getShortName();
			}
		}
		return null;
	}

	@Override
	public String getDisplayText(Translator translator, ExceptionalObligation exceptionalObligation,
			RepositoryEntry courseEntry) {
		String displayName = getDisplayName(translator, exceptionalObligation, courseEntry);
		if (displayName != null) {
			if (exceptionalObligation instanceof PassedExceptionalObligation) {
				PassedExceptionalObligation passedExceptionalObligation = (PassedExceptionalObligation)exceptionalObligation;
				String statusText = getStatusName(translator, passedExceptionalObligation);
				return translator.translate("exceptional.obligation.passed.display", new String[] {displayName, statusText});
			}
		}
		return null;
	}

	@Override
	public boolean hasScoreAccountingTrigger() {
		return false;
	}

	@Override
	public ScoreAccountingTriggerData getScoreAccountingTriggerData(ExceptionalObligation exceptionalObligation) {
		return null;
	}

	@Override
	public boolean matchesIdentity(ExceptionalObligation exceptionalObligation, Identity identity,
			ObligationContext obligationContext, Structure runStructure, ScoreAccounting scoreAccounting) {
		if (exceptionalObligation instanceof PassedExceptionalObligation) {
			PassedExceptionalObligation passedExceptionalObligation = (PassedExceptionalObligation)exceptionalObligation;
			
			CourseNode courseNode = runStructure.getNode(passedExceptionalObligation.getCourseNodeIdent());
			if (courseNode == null) {
				return false;
			}
			
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			if (Mode.none == assessmentConfig.getPassedMode()) {
				return false;
			}
			
			AssessmentEvaluation assessmentEvaluation = scoreAccounting.evalCourseNode(courseNode);
			if (isExcluded(assessmentEvaluation)) {
				return false;
			}
			
			List<String> status = Arrays.asList(passedExceptionalObligation.getStatus().split(Status.DELIMITER));
			boolean userVisible = assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
			if (status.contains(Status.notGraded.name()) && (!userVisible || assessmentEvaluation.getPassed() == null)) {
				return true;
			}
			if (status.contains(Status.gradedPassed.name()) && userVisible && assessmentEvaluation.getPassed() != null && assessmentEvaluation.getPassed().booleanValue()) {
				return true;
			}
			if (status.contains(Status.gradedFailed.name()) && userVisible && assessmentEvaluation.getPassed() != null && !assessmentEvaluation.getPassed().booleanValue()) {
				return true;
			}
		}
		return false;
	}

	private boolean isExcluded(AssessmentEvaluation assessmentEvaluation) {
		Overridable<AssessmentObligation> obligation = assessmentEvaluation.getObligation();
		return obligation.getCurrent() != null && obligation.getCurrent() == AssessmentObligation.excluded;
	}

	@Override
	public ExceptionalObligationController createCreationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode) {
		List<CourseNode> passeableCourseNodes = getPasseableCourseNodes(courseEntry, courseNode);
		return new PassedExceptionalObligationController(ureq, wControl, passeableCourseNodes);
	}
	

	
	private List<CourseNode> getPasseableCourseNodes(RepositoryEntry courseEntry, CourseNode courseNode) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		if (course != null) {
			PasseableVisitor visitor = new PasseableVisitor(courseNode);
			TreeVisitor tv = new TreeVisitor(visitor, course.getEditorTreeModel().getRootNode(), false);
			tv.visitAll();
			return visitor.getCourseNodes();
		}
		return Collections.emptyList();
	}

}
