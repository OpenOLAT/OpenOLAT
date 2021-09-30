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
package org.olat.ims.qti21.resultexport;

import java.math.BigDecimal;

import org.olat.course.assessment.AssessmentHelper;

/**
 * 
 * Initial date: 29 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedMember {
	
	private String lastname;
	private String firstname;
	private String email;
	private String nickname;
	
	private int attempts;
	private Boolean passed;
	private BigDecimal score;
	private String obligationString;
	private String href;

	public AssessedMember(String nickname, String lastname, String firstname, String email, int attempts,
			Boolean passed, BigDecimal score, String obligationString, String href) {
		this.nickname = nickname;
		this.lastname = lastname;
		this.firstname = firstname;
		this.email = email;
		this.attempts = attempts;
		this.passed = passed;
		this.score = score;
		this.obligationString = obligationString;
		this.href = href;
	}
	
	public String getNickname() {
		return nickname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getEmail() {
		return email;
	}

	public int getAttempts() {
		return attempts;
	}
	
	public String getPassedIcon() {
		return QTI21ResultsExportMediaResource.createPassedIcons(passed);
	}
	
	public String getScoreString() {
		String val = AssessmentHelper.getRoundedScore(score);
		return val == null ? "" : val;
	}
	
	public String getObligationString() {
		return obligationString;
	}

	public String getHref() {
		return href;
	}
}
