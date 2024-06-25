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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantRef;
import org.olat.modules.topicbroker.TBParticipantSearchParams;
import org.olat.modules.topicbroker.model.TBParticipantImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBParticipantDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBParticipant createParticipant(TBBroker broker, Identity identity) {
		TBParticipantImpl participant = new TBParticipantImpl();
		participant.setCreationDate(new Date());
		participant.setLastModified(participant.getCreationDate());
		participant.setBroker(broker);
		participant.setIdentity(identity);
		
		dbInstance.getCurrentEntityManager().persist(participant);
		return participant;
	}
	
	TBParticipant updateParticipant(TBParticipant participant) {
		if (participant instanceof TBParticipantImpl) {
			TBParticipantImpl impl = (TBParticipantImpl)participant;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(participant);
		return participant;
	}
	
	void deleteParticipant(TBParticipantRef participant) {
		String query = """
		delete
		  from topicbrokerparticipant participant
		 where participant.key = :participantKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("participantKey", participant.getKey())
				.executeUpdate();
	}

	void deleteParticipants(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokerparticipant participant
		 where participant.broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}

	List<TBParticipant> loadParticipants(TBParticipantSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select participant");
		sb.append("  from topicbrokerparticipant participant");
		if (params.isFetchBroker()) {
			sb.append(" join fetch participant.broker");
		}
		if (params.isFetchIdentity()) {
			sb.append(" join fetch participant.identity identity");
		}
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("participant.broker.key in :brokerKeys");
		}
		if (params.getParticipantKeys() != null && !params.getParticipantKeys().isEmpty()) {
			sb.and().append("participant.key in :participantKeys");
		}
		if (params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			sb.and().append("participant.identity.key in :identityKeys");
		}
		
		TypedQuery<TBParticipant> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBParticipant.class);
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getParticipantKeys() != null && !params.getParticipantKeys().isEmpty()) {
			query.setParameter("participantKeys", params.getParticipantKeys());
		}
		if (params.getIdentityKeys() != null && !params.getIdentityKeys().isEmpty()) {
			query.setParameter("identityKeys", params.getIdentityKeys());
		}
		return query.getResultList();
	}

}
