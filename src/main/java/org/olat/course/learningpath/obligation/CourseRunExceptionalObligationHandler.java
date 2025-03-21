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
package org.olat.course.learningpath.obligation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.Structure;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Mar 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CourseRunExceptionalObligationHandler implements ExceptionalObligationHandler {
	
	public static final String TYPE = "courseRun";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public int getSortValue() {
		return 80;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getAddI18nKey() {
		return "exceptional.obligation.course.run.add";
	}

	@Override
	public boolean isShowAdd(RepositoryEntry courseEntry) {
		return true;
	}

	@Override
	public String getDisplayType(Translator translator, ExceptionalObligation exceptionalObligation) {
		if (exceptionalObligation instanceof CourseRunExceptionalObligation courseRunExceptionalObligation) {
			return translator.translate("exceptional.obligation.course.run.type", courseRunExceptionalObligation.getOperator(),
					String.valueOf(courseRunExceptionalObligation.getOperand()));
		}
		return null;
	}

	@Override
	public String getDisplayName(Translator translator, ExceptionalObligation exceptionalObligation,
			RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof CourseRunExceptionalObligation courseRunExceptionalObligation) {
			return translator.translate("exceptional.obligation.course.run.name", courseRunExceptionalObligation.getOperator(),
					String.valueOf(courseRunExceptionalObligation.getOperand()));
		}
		return null;
	}

	@Override
	public String getDisplayText(Translator translator, ExceptionalObligation exceptionalObligation,
			RepositoryEntry courseEntry) {
		if (exceptionalObligation instanceof CourseRunExceptionalObligation courseRunExceptionalObligation) {
			return translator.translate("exceptional.obligation.course.run.display", courseRunExceptionalObligation.getOperator(),
					String.valueOf(courseRunExceptionalObligation.getOperand()));
		}
		return null;
	}

	@Override
	public boolean hasScoreAccountingTrigger() {
		return true;
	}

	@Override
	public ScoreAccountingTriggerData getScoreAccountingTriggerData(ExceptionalObligation exceptionalObligation) {
		return null;
	}

	@Override
	public boolean matchesIdentity(ExceptionalObligation exceptionalObligation, Identity identity,
			ObligationContext obligationContext, RepositoryEntry courseEntry, Structure runStructure,
			ScoreAccounting scoreAccounting) {
		if (exceptionalObligation instanceof CourseRunExceptionalObligation courseRunExceptionalObligation) {
			Long courseRun = obligationContext.getCourseRun(identity, courseEntry);
			if (courseRun == null) {
				courseRun = Long.valueOf(0);
			}
			return evaluateRun(courseRun.longValue(), courseRunExceptionalObligation.getOperator(), courseRunExceptionalObligation.getOperand());
		}
		return false;
	}
	
	private boolean evaluateRun(long run, String operator, long operand) {
		boolean eval = false;
		switch(operator) {
			case "<": eval = run < operand; break;
			case "<=": eval = run <= operand; break;
			case "=": eval = run == operand; break;
			case "=>": eval = run >= operand; break;
			case ">": eval = run > operand;  break;
			default: eval = false; break;
		}
		return eval;
	}

	@Override
	public ExceptionalObligationController createCreationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode) {
		return new CourseRunExceptionalObligationController(ureq, wControl);
	}

}
