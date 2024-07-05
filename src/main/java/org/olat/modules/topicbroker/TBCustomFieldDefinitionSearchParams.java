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
 * Initial date: 24 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBCustomFieldDefinitionSearchParams {
	
	private Collection<Long> brokerKeys;
	private Collection<Long> definitionKeys;
	private Collection<String> identifiers;
	private Collection<String> names;
	private Boolean deleted = Boolean.FALSE;
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
	
	public Collection<Long> getDefinitionKeys() {
		return definitionKeys;
	}

	public void setDefinition(TBCustomFieldDefinitionRef definition) {
		definitionKeys = definition != null? List.of(definition.getKey()): null;
	}
	
	public void setDefinitions(Collection<? extends TBCustomFieldDefinitionRef> definitions) {
		definitionKeys = definitions != null? definitions.stream().map(TBCustomFieldDefinitionRef::getKey).toList(): null;
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

	public Collection<String> getNames() {
		return names;
	}

	public void setNames(Collection<String> names) {
		this.names = names;
	}
	
	public void setName(String name) {
		this.names = List.of(name);
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isFetchBroker() {
		return fetchBroker;
	}

	public void setFetchBroker(boolean fetchBroker) {
		this.fetchBroker = fetchBroker;
	}

}
