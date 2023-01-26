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
package org.olat.course.learningpath.ui;

import java.math.BigDecimal;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer.CompletionPassed;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumLearningPathRepositoryRow implements CompletionPassed {

	private final RepositoryEntry repositoryEntry;
	private final Double completion;
	private final Boolean passed;
	private final BigDecimal score;
	
	private FormLink learningPathLink;

	public CurriculumLearningPathRepositoryRow(RepositoryEntry repositoryEntry, AssessmentEntryScoring assessmentEntry) {
		this.repositoryEntry = repositoryEntry;
		this.completion = assessmentEntry != null ? assessmentEntry.getCompletion(): null;
		this.passed = assessmentEntry != null ? assessmentEntry.getPassed(): null;
		this.score = assessmentEntry != null && assessmentEntry.getScore() != null? assessmentEntry.getScore(): null;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	public Double getCompletion() {
		return completion;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	public BigDecimal getScore() {
		return score;
	}

	public FormLink getLearningPathLink() {
		return learningPathLink;
	}

	public void setLearningPathLink(FormLink learningPathLink) {
		this.learningPathLink = learningPathLink;
	}

}
