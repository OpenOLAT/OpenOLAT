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
import org.olat.core.id.Identity;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionRef;
import org.olat.modules.topicbroker.TBSelectionSearchParams;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.model.TBSelectionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBSelectionDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBSelection createSelection(Identity creator, TBParticipant participant, TBTopic topic, int sortOrder) {
		TBSelectionImpl selection = new TBSelectionImpl();
		selection.setCreationDate(new Date());
		selection.setLastModified(selection.getCreationDate());
		selection.setCreator(creator);
		selection.setParticipant(participant);
		selection.setTopic(topic);
		selection.setSortOrder(sortOrder);
		
		dbInstance.getCurrentEntityManager().persist(selection);
		return selection;
	}
	
	TBSelection updateSelection(TBSelection selection) {
		if (selection instanceof TBSelectionImpl) {
			TBSelectionImpl impl = (TBSelectionImpl)selection;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(selection);
		return selection;
	}
	
	void deleteSelection(TBSelectionRef selection) {
		String query = """
		delete
		  from topicbrokerselection selection
		 where selection.key = :selectionKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("selectionKey", selection.getKey())
				.executeUpdate();
	}

	void deleteSelections(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokerselection selection
		 where selection.participant.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}

	List<TBSelection> loadSelections(TBSelectionSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select selection");
		sb.append("  from topicbrokerselection selection");
		if (params.isFetchParticipant()) {
			sb.append(" join fetch selection.participant participant");
		}
		if (params.isFetchIdentity()) {
			sb.append(" join fetch participant.identity identity");
			sb.append(" left join fetch selection.creator creator");
		}
		if (params.isFetchTopic()) {
			sb.append(" join fetch selection.topic topic");
		}
		if (params.isFetchBroker()) {
			sb.append(" join fetch topic.broker");
		}
		
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("selection.participant.broker.key in :brokerKeys");
		}
		if (params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			sb.and().append("selection.participant.identity.key in :identityKeys");
		}
		if (params.getEnrolledOrIdentityKeys() != null && !params.getEnrolledOrIdentityKeys().isEmpty()) {
			sb.and().append("(selection.enrolled = true or selection.participant.identity.key in :enrolledOrIdentityKeys)");
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			sb.and().append("selection.topic.key in :topicKeys");
		}
		if (params.getSelectionKeys() != null && !params.getSelectionKeys().isEmpty()) {
			sb.and().append("selection.key in :selectionKeys");
		}
		if (params.getEnrolledOrMaxSortOrder() != null) {
			sb.and().append("(selection.enrolled = true or selection.sortOrder <= :maxSortOrder)");
		}
		
		TypedQuery<TBSelection> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBSelection.class);
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			query.setParameter("identityKeys", params.getIdentityKeys());
		}
		if (params.getEnrolledOrIdentityKeys() != null && !params.getEnrolledOrIdentityKeys().isEmpty()) {
			query.setParameter("enrolledOrIdentityKeys", params.getEnrolledOrIdentityKeys());
		}
		if (params.getTopicKeys() != null && !params.getTopicKeys().isEmpty()) {
			query.setParameter("topicKeys", params.getTopicKeys());
		}
		if (params.getSelectionKeys() != null && !params.getSelectionKeys().isEmpty()) {
			query.setParameter("selectionKeys", params.getSelectionKeys());
		}
		if (params.getEnrolledOrMaxSortOrder() != null) {
			query.setParameter("maxSortOrder", params.getEnrolledOrMaxSortOrder());
		}
		return query.getResultList();
	}

}
