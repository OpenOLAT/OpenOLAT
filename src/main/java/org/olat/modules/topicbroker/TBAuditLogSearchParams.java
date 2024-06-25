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
package org.olat.modules.topicbroker;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.topicbroker.TBAuditLog.Action;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TBAuditLogSearchParams {
	
	private Collection<Action> actions;
	private Collection<Long> doerKeys;
	private Collection<Long> brokerKeys;
	private Collection<Long> topicKeys;
	private Boolean orderAsc;
	private boolean fetchDoer;

	public Collection<Action> getActions() {
		return actions;
	}

	public void setActions(Collection<Action> actions) {
		this.actions = actions;
	}
	
	public Collection<Long> getDoerKeys() {
		return doerKeys;
	}

	public void setDoerKeys(Collection<Long> doerKeys) {
		this.doerKeys = doerKeys;
	}

	public void setDoers(Collection<? extends IdentityRef> doers) {
		this.doerKeys = doers.stream().map(IdentityRef::getKey).toList();
	}
	
	public void setDoer(IdentityRef doer) {
		this.doerKeys = List.of(doer.getKey());
	}

	public Collection<Long> getBrokerKeys() {
		return brokerKeys;
	}
	
	public void setBroker(TBBroker broker) {
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

	public Boolean getOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(Boolean orderAsc) {
		this.orderAsc = orderAsc;
	}

	public boolean isFetchDoer() {
		return fetchDoer;
	}

	public void setFetchDoer(boolean fetchDoer) {
		this.fetchDoer = fetchDoer;
	}

}
