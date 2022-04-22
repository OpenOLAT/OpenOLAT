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
package org.olat.course.nodes.cl;

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.handler.ModuleAssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentConfig extends ModuleAssessmentConfig {
	
	private final RepositoryEntryRef courseEntry;
	private final String nodeIdent;

	public CheckListAssessmentConfig(RepositoryEntryRef courseEntry, CourseNode courseNode) {
		super(courseNode.getModuleConfiguration());
		this.courseEntry = courseEntry;
		this.nodeIdent = courseNode.getIdent();
	}

	@Override
	public Mode getPassedMode() {
		if (hasGrade() && Mode.none != getScoreMode()) {
			if (CoreSpringFactory.getImpl(GradeService.class).hasPassed(courseEntry, nodeIdent)) {
				return Mode.setByNode;
			}
			return Mode.none;
		}
		return super.getPassedMode();
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return coachCanNotEdit? Boolean.FALSE: Boolean.TRUE;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isBulkEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return true;
	}

}
