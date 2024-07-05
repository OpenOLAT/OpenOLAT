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
package org.olat.modules.topicbroker.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionRef;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.model.TBCustomFieldDefinitionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBCustomFieldDefinitionDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBCustomFieldDefinition createDefinition(TBBroker broker, String identifier) {
		TBCustomFieldDefinitionImpl definition = new TBCustomFieldDefinitionImpl();
		definition.setCreationDate(new Date());
		definition.setLastModified(definition.getCreationDate());
		definition.setIdentifier(identifier);
		definition.setSortOrder(getNextSortOrder(broker));
		definition.setBroker(broker);
		
		dbInstance.getCurrentEntityManager().persist(definition);
		return definition;
	}
	
	public int getNextSortOrder(TBBroker broker) {
		String query = """
		select  max(definition.sortOrder) + 1
		  from topicbrokercustomfielddefinition definition
		 where definition.broker.key = :brokerKey
		   and definition.deletedDate is null
		""";
		
		List<Integer> next = dbInstance.getCurrentEntityManager()
				.createQuery(query, Integer.class)
				.setParameter("brokerKey", broker.getKey())
				.getResultList();
		return next != null && !next.isEmpty() && next.get(0) != null? next.get(0).intValue(): 1;
	}
	
	TBCustomFieldDefinition loadNext(TBCustomFieldDefinition definition, boolean up) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select definition");
		sb.append("  from topicbrokercustomfielddefinition definition");
		sb.and().append("definition.deletedDate is null");
		sb.and().append("definition.broker.key = :brokerKey");
		sb.and().append("definition.sortOrder ").append("<", ">", up).append(" :sortOrder");
		sb.orderBy().append("definition.sortOrder").appendAsc(!up);
		
		List<TBCustomFieldDefinition> definitions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBCustomFieldDefinition.class)
				.setParameter("brokerKey", definition.getBroker().getKey())
				.setParameter("sortOrder", definition.getSortOrder())
				.setMaxResults(1)
				.getResultList();
		
		return !definitions.isEmpty()? definitions.get(0): null;
	}
	
	TBCustomFieldDefinition updateDefinition(TBCustomFieldDefinition definition) {
		if (definition instanceof TBCustomFieldDefinitionImpl) {
			TBCustomFieldDefinitionImpl impl = (TBCustomFieldDefinitionImpl)definition;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(definition);
		return definition;
	}
	
	void deleteDefinition(TBCustomFieldDefinitionRef definition) {
		String query = """
		delete
		  from topicbrokercustomfielddefinition definition
		 where definition.key = :definitionKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("definitionKey", definition.getKey())
				.executeUpdate();
	}

	void deleteDefinitions(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokercustomfielddefinition definition
		 where definition.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}

	List<TBCustomFieldDefinition> loadDefinitions(TBCustomFieldDefinitionSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select definition");
		sb.append("  from topicbrokercustomfielddefinition definition");
		if (params.isFetchBroker()) {
			sb.append(" join fetch definition.broker");
		}
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("definition.broker.key in :brokerKeys");
		}
		if (params.getDefinitionKeys() != null && !params.getDefinitionKeys().isEmpty()) {
			sb.and().append("definition.key in :definitionKeys");
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			sb.and().append("definition.identifier in :identifiers");
		}
		if (params.getNames() != null && !params.getNames().isEmpty()) {
			sb.and().append("definition.name in :names");
		}
		if (params.getDeleted() != null) {
			sb.and().append("definition.deletedDate is ").append("not ", params.getDeleted()).append("null");
		}
		
		TypedQuery<TBCustomFieldDefinition> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBCustomFieldDefinition.class);
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getDefinitionKeys() != null && !params.getDefinitionKeys().isEmpty()) {
			query.setParameter("definitionKeys", params.getDefinitionKeys());
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			query.setParameter("identifiers", params.getIdentifiers());
		}
		if (params.getNames() != null && !params.getNames().isEmpty()) {
			query.setParameter("names", params.getNames());
		}
		return query.getResultList();
	}

}
