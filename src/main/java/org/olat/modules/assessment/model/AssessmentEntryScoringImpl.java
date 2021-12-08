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
package org.olat.modules.assessment.model;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.Overridable;

/**
 * 
 * Initial date: 24 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryScoringImpl implements AssessmentEntryScoring {
	
	private final Long key;
	private final Long repositoryEntryKey;
	private final Double completion;
	private final BigDecimal score;
	private final BigDecimal maxScore;
	private transient Overridable<Boolean> passedOverridable;
	private final Boolean passed;
	private final Boolean passedOriginal;
	private final Date passedModificationDate;
	
	public AssessmentEntryScoringImpl(Long key, Long repositoryEntryKey, Double completion, BigDecimal score, BigDecimal maxScore,
			Boolean passed, Boolean passedOriginal, Date passedModificationDate) {
		this.key = key;
		this.repositoryEntryKey = repositoryEntryKey;
		this.completion = completion;
		this.score = score;
		this.maxScore = maxScore;
		this.passed = passed;
		this.passedOriginal = passedOriginal;
		this.passedModificationDate = passedModificationDate;
	}

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	@Override
	public Double getCompletion() {
		return completion;
	}
	
	private Overridable<Boolean> getPassedOverridable() {
		if (passedOverridable == null) {
			passedOverridable = new OverridableImpl<>(passed, passedOriginal, null, passedModificationDate);
		}
		return passedOverridable;
	}

	@Override
	public Boolean getPassed() {
		return getPassedOverridable().getCurrent();
	}

	@Override
	public BigDecimal getScore() {
		return score;
	}
	
	@Override
	public BigDecimal getMaxScore() {
		return maxScore;
	}
	
}
