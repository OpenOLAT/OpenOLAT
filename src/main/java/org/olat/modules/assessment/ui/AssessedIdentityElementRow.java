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
package org.olat.modules.assessment.ui;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityElementRow extends UserPropertiesRow {
	
	private final Integer attempts;
	private final BigDecimal score;
	private final Boolean passed;
	private final Date creationDate;
	private final Date lastModified;
	private final AssessmentEntryStatus status;
	
	public AssessedIdentityElementRow(Identity identity, AssessmentEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		if(entry != null) {
			attempts = entry.getAttempts();
			score = entry.getScore();
			passed = entry.getPassed();
			creationDate = entry.getCreationDate();
			lastModified = entry.getLastModified();
			status = entry.getAssessmentStatus();
		} else {
			attempts = null;
			score = null;
			passed = null;
			creationDate = lastModified = null;
			status = null;
		}
	}

	public Integer getAttempts() {
		return attempts;
	}

	public BigDecimal getScore() {
		return score;
	}

	public Boolean getPassed() {
		return passed;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return status;
	}
}