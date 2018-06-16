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
import org.olat.modules.quality.QualityDataCollectionParticipation;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ParticipationRow {

	private final QualityDataCollectionParticipation participation;

	public ParticipationRow(QualityDataCollectionParticipation participation) {
		this.participation = participation;
	}

	public QualityDataCollectionParticipation getParticipation() {
		return participation;
	}
	
	public EvaluationFormParticipationRef getParticipationRef() {
		return participation.getParticipationRef();
	}

	public Object getFirstname() {
		return participation.getFirstname();
	}

	public Object getLastname() {
		return participation.getLastname();
	}

	public Object getEmail() {
		return participation.getEmail();
	}

}
