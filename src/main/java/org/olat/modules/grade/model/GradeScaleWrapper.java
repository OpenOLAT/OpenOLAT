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
package org.olat.modules.grade.model;

import java.math.BigDecimal;

import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeSystem;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleWrapper implements GradeScale {

	private BigDecimal minScore;
	private BigDecimal maxScore;
	private GradeSystem gradeSystem;
	
	@Override
	public Long getKey() {
		return null;
	}
	
	@Override
	public BigDecimal getMinScore() {
		return minScore;
	}
	
	@Override
	public void setMinScore(BigDecimal minScore) {
		this.minScore = minScore;
	}
	
	@Override
	public BigDecimal getMaxScore() {
		return maxScore;
	}
	
	@Override
	public void setMaxScore(BigDecimal maxScore) {
		this.maxScore = maxScore;
	}
	
	@Override
	public GradeSystem getGradeSystem() {
		return gradeSystem;
	}
	
	@Override
	public void setGradeSystem(GradeSystem gradeSystem) {
		this.gradeSystem = gradeSystem;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return null;
	}

	@Override
	public String getSubIdent() {
		return null;
	}
	
}
