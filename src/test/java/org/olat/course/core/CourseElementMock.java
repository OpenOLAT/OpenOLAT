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
package org.olat.course.core;

import java.math.BigDecimal;

import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 8 Dec 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseElementMock implements CourseElement {
	
	private Long key;
	private String type;
	private String shortTitle;
	private String longTitle;
	private boolean assesseable;
	private Mode scoreMode;
	private Mode passedMode;
	private BigDecimal cutValue;
	private RepositoryEntry repositoryEntry;
	private String subIdent;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	@Override
	public boolean isAssesseable() {
		return assesseable;
	}

	public void setAssesseable(boolean assesseable) {
		this.assesseable = assesseable;
	}

	@Override
	public Mode getScoreMode() {
		return scoreMode;
	}

	public void setScoreMode(Mode scoreMode) {
		this.scoreMode = scoreMode;
	}

	@Override
	public Mode getPassedMode() {
		return passedMode;
	}

	public void setPassedMode(Mode passedMode) {
		this.passedMode = passedMode;
	}

	@Override
	public BigDecimal getCutValue() {
		return cutValue;
	}

	public void setCutValue(BigDecimal cutValue) {
		this.cutValue = cutValue;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

}
