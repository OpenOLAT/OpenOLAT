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
package org.olat.modules.portfolio.model;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntryLight;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBinder implements AssessmentEntryLight {
	
	private final Long binderKey;
	private final String binderTitle;
	private final String entryDisplayname;
	
	private final Boolean passed;
	private final BigDecimal score;
	private final Identity assessedIdentity;
	
	private final int numOfOpenSections;
	
	public AssessedBinder(Long binderKey, String binderTitle, String entryDisplayname,
			Boolean passed, BigDecimal score, Identity assessedIdentity, int numOfOpenSections) {
		this.binderKey = binderKey;
		this.binderTitle = binderTitle;
		this.entryDisplayname = entryDisplayname;
		this.passed = passed;
		this.score = score;
		this.assessedIdentity = assessedIdentity;
		this.numOfOpenSections = numOfOpenSections;
	}
	
	public Long getBinderKey() {
		return binderKey;
	}
	
	public String getBinderTitle() {
		return binderTitle;
	}
	
	public Date getLastModified() {
		return null;
	}
	
	public String getEntryDisplayname() {
		return entryDisplayname;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	@Override
	public BigDecimal getScore() {
		return score;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public int getNumOfOpenSections() {
		return numOfOpenSections;
	}
}
