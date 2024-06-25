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
public class TBSelectionSearchParams {
	
	private Collection<Long> brokerKeys;
	private Collection<Long> participantKeys;
	private Collection<Long> identityKeys;
	private Collection<Long> topicKeys;
	private Collection<Long> selectionKeys;
	private Integer enrolledOrMaxSortOrder;
	private boolean fetchBroker = false;
	private boolean fetchParticipant = false;
	private boolean fetchIdentity = false;
	private boolean fetchTopic = false;

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

	public Collection<Long> getTopicKeys() {
		return topicKeys;
	}
	
	public void setTopic(TBTopicRef topic) {
		topicKeys = topic != null? List.of(topic.getKey()): null;
	}
	
	public void setTopics(Collection<? extends TBTopicRef> topics) {
		topicKeys = topics != null? topics.stream().map(TBTopicRef::getKey).toList(): null;
	}

	public Collection<Long> getSelectionKeys() {
		return selectionKeys;
	}

	public void setSelection(TBSelectionRef selection) {
		selectionKeys = selection != null? List.of(selection.getKey()): null;
	}
	
	public void setSelections(Collection<? extends TBSelectionRef> selections) {
		selectionKeys = selections != null? selections.stream().map(TBSelectionRef::getKey).toList(): null;
	}

	public Integer getEnrolledOrMaxSortOrder() {
		return enrolledOrMaxSortOrder;
	}

	public void setEnrolledOrMaxSortOrder(Integer enrolledOrMaxSortOrder) {
		this.enrolledOrMaxSortOrder = enrolledOrMaxSortOrder;
	}

	public boolean isFetchBroker() {
		return fetchBroker;
	}

	public void setFetchBroker(boolean fetchBroker) {
		this.fetchBroker = fetchBroker;
		if (fetchBroker) {
			setFetchTopic(fetchBroker);
		}
	}

	public boolean isFetchParticipant() {
		return fetchParticipant;
	}

	public void setFetchParticipant(boolean fetchParticipant) {
		this.fetchParticipant = fetchParticipant;
	}

	public boolean isFetchIdentity() {
		return fetchIdentity;
	}

	public void setFetchIdentity(boolean fetchIdentity) {
		this.fetchIdentity = fetchIdentity;
		if (fetchIdentity) {
			setFetchParticipant(fetchIdentity);
		}
	}

	public boolean isFetchTopic() {
		return fetchTopic;
	}

	public void setFetchTopic(boolean fetchTopic) {
		this.fetchTopic = fetchTopic;
	}
}
