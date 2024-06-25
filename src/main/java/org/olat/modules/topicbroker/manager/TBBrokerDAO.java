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
import org.olat.modules.topicbroker.TBBrokerSearchParams;
import org.olat.modules.topicbroker.model.TBBrokerImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBBrokerDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBBroker createBroker(RepositoryEntry repositoryEntry, String subIdent) {
		TBBrokerImpl broker = new TBBrokerImpl();
		broker.setCreationDate(new Date());
		broker.setLastModified(broker.getCreationDate());
		broker.setMaxSelections(Integer.valueOf(3));
		broker.setParticipantCanEditRequiredEnrollments(false);
		broker.setRequiredEnrollments(Integer.valueOf(1));
		broker.setAutoEnrollment(true);
		broker.setParticipantCanWithdraw(false);
		
		broker.setRepositoryEntry(repositoryEntry);
		broker.setSubIdent(subIdent);
		
		dbInstance.getCurrentEntityManager().persist(broker);
		return broker;
	}
	
	TBBroker updateBroker(TBBroker broker) {
		if (broker instanceof TBBrokerImpl) {
			TBBrokerImpl impl = (TBBrokerImpl)broker;
			impl.setLastModified(new Date());
		}
		dbInstance.getCurrentEntityManager().merge(broker);
		return broker;
	}
	
	void deleteBroker(TBBroker broker) {
		String query = """
		delete
		  from topicbrokerbroker broker
		 where broker.key = :brokerKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}
	
	public TBBroker loadBroker(RepositoryEntry repositoryEntry, String subIdent) {
		String query = """
		select broker
		  from topicbrokerbroker broker
		 where broker.repositoryEntry.key = :repositoryEntryKey
		   and broker.subIdent = :subIdent
		""";
		
		List<TBBroker> brokers = dbInstance.getCurrentEntityManager()
				.createQuery(query, TBBroker.class)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		
		return !brokers.isEmpty()? brokers.get(0): null;
	}

	List<TBBroker> loadBrokers(TBBrokerSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select broker");
		sb.append("  from topicbrokerbroker broker");
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			sb.and().append("broker.key in :brokerKeys");
		}
		if (params.getSelectionEndDateBefore() != null) {
			sb.and().append("broker.selectionEndDate <= :selectionEndDate");
		}
		if (params.getAutoEnrollment() != null) {
			sb.and().append("broker.autoEnrollment = ").append(params.getAutoEnrollment());
		}
		if (params.getEnrollmentStartNull() != null) {
			sb.and().append("broker.enrollmentStartDate is ").append("not ", !params.getEnrollmentStartNull()).append("null");
		}
		
		TypedQuery<TBBroker> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBBroker.class);
		if (params.getBrokerKeys() != null && !params.getBrokerKeys().isEmpty()) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		if (params.getSelectionEndDateBefore() != null) {
			query.setParameter("selectionEndDate", params.getSelectionEndDateBefore());
		}
		return query.getResultList();
	}

}
