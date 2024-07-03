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
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldRef;
import org.olat.modules.topicbroker.TBCustomFieldSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBCustomFieldImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 26 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBCustomFieldDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBCustomField createCustomField(TBCustomFieldDefinition definition, TBTopic topic) {
		TBCustomFieldImpl customField = new TBCustomFieldImpl();
		customField.setCreationDate(new Date());
		customField.setLastModified(customField.getCreationDate());
		customField.setDefinition(definition);
		customField.setTopic(topic);
		
		dbInstance.getCurrentEntityManager().persist(customField);
		return customField;
	}
	
	TBCustomField updateCustomField(TBCustomField customField) {
		if (customField instanceof TBCustomFieldImpl) {
			TBCustomFieldImpl impl = (TBCustomFieldImpl)customField;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(customField);
		return customField;
	}
	
	void deleteCustomField(TBCustomFieldRef customField) {
		String query = """
		delete
		  from topicbrokercustomfield customfield
		 where customfield.key = :customFieldKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("customFieldKey", customField.getKey())
				.executeUpdate();
	}

	void deleteCustomFields(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokercustomfield customfield
		 where customfield.definition.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
		
	}

	List<TBCustomField> loadCustomFields(TBCustomFieldSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select customfield");
		sb.append("  from topicbrokercustomfield customfield");
		if (params.isFetchDefinition()) {
			sb.append(" join fetch customfield.definition definition");
			if (params.isFetchBroker()) {
				sb.append(" join fetch definition.broker");
			}
		}
		if (params.isFetchTopic()) {
			sb.append(" join fetch customfield.topic topic");
			if (params.isFetchBroker()) {
				sb.append(" join fetch topic.broker");
			}
			if (params.isFetchIdentities()) {
				sb.append(" join fetch topic.creator");
				sb.append(" left join fetch topic.deletedBy");
			}
		}
		if (params.isFetchVfsMetadata()) {
			sb.append(" left join fetch customfield.vfsMetadata");
		}
		if (params.getCustomFieldKeys() != null && !params.getCustomFieldKeys().isEmpty()) {
			sb.and().append("customfield.key in :customFieldKeys");
		}
		if (params.getDefinitionKeys() != null && !params.getDefinitionKeys().isEmpty()) {
			sb.and().append("customfield.definition.key in :definitionKeys");
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			sb.and().append("customfield.topic.key in :topicKeys");
		}
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("customfield.definition.broker.key in :brokerKeys");
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			sb.and().append("customfield.definition.identifier in :identifiers");
		}
		if (params.getDeletedDefinition() != null) {
			sb.and().append("customfield.definition.deletedDate is ").append("not ", params.getDeletedDefinition()).append("null");
		}
		if (params.getDeletedTopic() != null) {
			sb.and().append("customfield.topic.deletedDate is ").append("not ", params.getDeletedTopic()).append("null");
		}
		
		TypedQuery<TBCustomField> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBCustomField.class);
		if (params.getCustomFieldKeys() != null && !params.getCustomFieldKeys().isEmpty()) {
			query.setParameter("customFieldKeys", params.getCustomFieldKeys());
		}
		if (params.getDefinitionKeys() != null && !params.getDefinitionKeys().isEmpty()) {
			query.setParameter("definitionKeys", params.getDefinitionKeys());
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			query.setParameter("topicKeys", params.getTopicKeys());
		}
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getIdentifiers() != null && !params.getIdentifiers().isEmpty()) {
			query.setParameter("identifiers", params.getIdentifiers());
		}
		return query.getResultList();
	}

}
