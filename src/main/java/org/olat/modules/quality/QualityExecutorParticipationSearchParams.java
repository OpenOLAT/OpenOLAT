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
package org.olat.modules.quality;

import java.util.Collection;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 10.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityExecutorParticipationSearchParams {
	
	private IdentityRef executorRef;
	private QualityDataCollectionRef dataCollectionRef;
	private EvaluationFormParticipationRef participationRef;
	private EvaluationFormParticipationStatus participationStatus;
	private Collection<QualityDataCollectionStatus> dataCollectionStatus;

	public IdentityRef getExecutorRef() {
		return executorRef;
	}

	public void setExecutorRef(IdentityRef executorRef) {
		this.executorRef = executorRef;
	}

	public QualityDataCollectionRef getDataCollectionRef() {
		return dataCollectionRef;
	}

	public void setDataCollectionRef(QualityDataCollectionRef dataCollectionRef) {
		this.dataCollectionRef = dataCollectionRef;
	}

	public EvaluationFormParticipationRef getParticipationRef() {
		return participationRef;
	}

	public void setParticipationRef(EvaluationFormParticipationRef participationRef) {
		this.participationRef = participationRef;
	}

	public EvaluationFormParticipationStatus getParticipationStatus() {
		return participationStatus;
	}
	
	public void setParticipationStatus(EvaluationFormParticipationStatus participationStatus) {
		this.participationStatus = participationStatus;
	}

	public Collection<QualityDataCollectionStatus> getDataCollectionStatus() {
		return dataCollectionStatus;
	}

	public void setDataCollectionStatus(Collection<QualityDataCollectionStatus> dataCollectionStatus) {
		this.dataCollectionStatus = dataCollectionStatus;
	}

}

