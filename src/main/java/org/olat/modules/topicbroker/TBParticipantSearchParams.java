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
import java.util.List;

import org.olat.basesecurity.IdentityRef;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBParticipantSearchParams {
	
	private Collection<Long> brokerKeys;
	private Collection<Long> participantKeys;
	private Collection<Long> identityKeys;
	private boolean fetchBroker = false;
	private boolean fetchIdentity = false;
	
	public Collection<Long> getBrokerKeys() {
		return brokerKeys;
	}
	
	public void setBroker(TBBrokerRef broker) {
		brokerKeys = broker != null? List.of(broker.getKey()): null;
	}

	public void setBrokers(Collection<? extends TBBrokerRef> brokers) {
		brokerKeys = brokers != null? brokers.stream().map(TBBrokerRef::getKey).toList(): null;
	}
	
	public Collection<Long> getParticipantKeys() {
		return participantKeys;
	}

	public void setParticipant(TBParticipantRef participant) {
		participantKeys = participant != null? List.of(participant.getKey()): null;
	}
	
	public void setParticipants(Collection<? extends TBParticipantRef> participants) {
		participantKeys = participants != null? participants.stream().map(TBParticipantRef::getKey).toList(): null;
	}
	
	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}

	public void setIdentity(IdentityRef identity) {
		this.identityKeys = identity != null? List.of(identity.getKey()): null;
	}
	
	public void setIdentities(Collection<? extends IdentityRef> identities) {
		identityKeys = identities != null? identities.stream().map(IdentityRef::getKey).toList(): null;
	}

	public boolean isFetchBroker() {
		return fetchBroker;
	}

	public void setFetchBroker(boolean fetchBroker) {
		this.fetchBroker = fetchBroker;
	}

	public boolean isFetchIdentity() {
		return fetchIdentity;
	}

	public void setFetchIdentity(boolean fetchIdentity) {
		this.fetchIdentity = fetchIdentity;
	}

}
