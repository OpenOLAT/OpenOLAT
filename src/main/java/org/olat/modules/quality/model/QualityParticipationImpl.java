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
package org.olat.modules.quality.model;

import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.quality.QualityParticipation;

/**
 * 
 * Initial date: 13.06.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityParticipationImpl implements QualityParticipation {

	private final Long participationKey;
	private final String firstname;
	private final String lastname;
	private final String email;

	public QualityParticipationImpl(Long participationKey, String firstname, String lastname,
			String email) {
		super();
		this.participationKey = participationKey;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	@Override
	public EvaluationFormParticipationRef getParticipationRef() {
		return new EvaluationFormParticipationRef() {
			
			@Override
			public Long getKey() {
				return participationKey;
			}
		};
	}

	@Override
	public String getFirstname() {
		return firstname;
	}

	@Override
	public String getLastname() {
		return lastname;
	}

	@Override
	public String getEmail() {
		return email;
	}

}
