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

/**
 * 
 * Initial date: 30 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTopicSearchParams {
	
	private Collection<Long> brokerKeys;
	private Collection<Long> topicKeys;
	private Collection<String> identifiers;
	private Boolean deleted = Boolean.FALSE;
	private boolean fetchIdentities = false;
	private boolean fetchBroker = false;
	
	public Collection<Long> getBrokerKeys() {
		return brokerKeys;
	}
	
	public void setBroker(TBBrokerRef broker) {
		brokerKeys = broker != null? List.of(broker.getKey()): null;
	}

	public void setBrokers(Collection<? extends TBBrokerRef> brokers) {
		brokerKeys = brokers != null? brokers.stream().map(TBBrokerRef::getKey).toList(): null;
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

	public Collection<String> getIdentifiers() {
		return identifiers;
	}
	
	public void setIdentifier(String identifier) {
		this.identifiers = List.of(identifier);
	}

	public void setIdentifiers(Collection<String> identifiers) {
		this.identifiers = identifiers;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isFetchIdentities() {
		return fetchIdentities;
	}

	public void setFetchIdentities(boolean fetchIdentities) {
		this.fetchIdentities = fetchIdentities;
	}

	public boolean isFetchBroker() {
		return fetchBroker;
	}

	public void setFetchBroker(boolean fetchBroker) {
		this.fetchBroker = fetchBroker;
	}

}
