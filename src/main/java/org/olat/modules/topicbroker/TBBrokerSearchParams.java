/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 
 * Initial date: 30 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBBrokerSearchParams {
	
	private Collection<Long> brokerKeys;
	private Date selectionEndDateBefore;
	private Boolean autoEnrollment;
	private Boolean enrollmentStartNull;
	
	public Collection<Long> getBrokerKeys() {
		return brokerKeys;
	}

	public void setBroker(TBBrokerRef broker) {
		brokerKeys = broker != null? List.of(broker.getKey()): null;
	}
	
	public void setBrokers(Collection<? extends TBBrokerRef> brokers) {
		brokerKeys = brokers != null? brokers.stream().map(TBBrokerRef::getKey).toList(): null;
	}

	public Date getSelectionEndDateBefore() {
		return selectionEndDateBefore;
	}

	public void setSelectionEndDateBefore(Date selectionEndDateBefore) {
		this.selectionEndDateBefore = selectionEndDateBefore;
	}

	public Boolean getAutoEnrollment() {
		return autoEnrollment;
	}

	public void setAutoEnrollment(Boolean autoEnrollment) {
		this.autoEnrollment = autoEnrollment;
	}

	public Boolean getEnrollmentStartNull() {
		return enrollmentStartNull;
	}

	public void setEnrollmentStartNull(Boolean enrollmentStartNull) {
		this.enrollmentStartNull = enrollmentStartNull;
	}

}
