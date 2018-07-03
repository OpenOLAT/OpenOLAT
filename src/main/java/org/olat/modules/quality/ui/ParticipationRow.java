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
package org.olat.modules.quality.ui;

import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityParticipation;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ParticipationRow {

	private final QualityParticipation participation;
	private final Long key;

	public ParticipationRow(QualityParticipation participation) {
		this.participation = participation;
		this.key = participation.getParticipationRef().getKey();
	}

	public QualityParticipation getParticipation() {
		return participation;
	}
	
	public EvaluationFormParticipationRef getParticipationRef() {
		return participation.getParticipationRef();
	}

	public String getFirstname() {
		return participation.getFirstname();
	}

	public String getLastname() {
		return participation.getLastname();
	}

	public String getEmail() {
		return participation.getEmail();
	}
	
	public QualityContextRef getContextRef() {
		return participation.getContextRef();
	}
	
	public QualityContextRole getRole() {
		return participation.getRole();
	}
	
	public String getAudienceRepositoryEntryName() {
		return participation.getAudienceRepositoryEntryName();
	}
	
	public String getAudienceCurriculumElementName() {
		return participation.getAudienceCurriculumElementName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParticipationRow other = (ParticipationRow) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
