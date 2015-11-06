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
package org.olat.course.assessment.ui.tool;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityCourseRow extends UserPropertiesRow {
	
	private final Float score;
	private final Boolean passed;
	private final Date lastModified;
	
	public AssessedIdentityCourseRow(Identity identity, EfficiencyStatementEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		
		if(entry != null && entry.getUserEfficencyStatement() != null) {
			score = entry.getUserEfficencyStatement().getScore();
			passed = entry.getUserEfficencyStatement().getPassed();
			lastModified = entry.getUserEfficencyStatement().getLastModified();
		} else {
			score = null;
			passed = null;
			lastModified = null;
		}
	}

	public Float getScore() {
		return score;
	}

	public Boolean getPassed() {
		return passed;
	}

	public Date getLastModified() {
		return lastModified;
	}
}