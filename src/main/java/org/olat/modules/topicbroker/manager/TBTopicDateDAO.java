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
package org.olat.modules.topicbroker.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicDateRef;
import org.olat.modules.topicbroker.TBTopicDateSearchParams;
import org.olat.modules.topicbroker.TBTopicDateValues;
import org.olat.modules.topicbroker.model.TBTopicDateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 4 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Component
public class TBTopicDateDAO {
	
	@Autowired
	private DB dbInstance;
	
	TBTopicDate createDate(TBTopic topic, TBTopicDateValues values) {
		TBTopicDateImpl date = new TBTopicDateImpl();
		date.setCreationDate(new Date());
		date.setLastModified(date.getCreationDate());
		date.setStartDate(values.startDate());
		date.setEndDate(values.endDate());
		date.setLocation(values.location());
		date.setDescription(values.description());
		date.setTopic(topic);
		
		dbInstance.getCurrentEntityManager().persist(date);
		return date;
	}
	
	TBTopicDate updateDate(TBTopicDate date) {
		date.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(date);
	}
	
	void deleteDate(TBTopicDateRef date) {
		String query = """
		delete
		  from topicbrokertopicdate tdate
		 where tdate.key = :dateKey
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("dateKey", date.getKey())
				.executeUpdate();
	}
	
	void deleteDates(TBBrokerRef broker) {
		String query = """
		delete
		  from topicbrokertopicdate tdate
		 where tdate.topic.key in (select topic.key from topicbrokertopic topic where topic.broker.key = :brokerKey)
		""";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("brokerKey", broker.getKey())
				.executeUpdate();
	}
	
	List<TBTopicDate> loadDates(TBTopicDateSearchParams params) {
		if ((params.getTopicKeys() != null && params.getTopicKeys().isEmpty())
				|| (params.getBrokerKeys() != null && params.getBrokerKeys().isEmpty())) {
			return List.of();
		}
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select tdate");
		sb.append("  from topicbrokertopicdate tdate");
		if (params.isFetchTopic()) {
			sb.append(" join fetch tdate.topic topic");
		}
		if (params.getTopicKeys() != null) {
			sb.and().append("tdate.topic.key in :topicKeys");
		}
		if (params.getBrokerKeys() != null) {
			sb.and().append("tdate.topic.broker.key in :brokerKeys");
		}
		if (params.getDeletedTopic() != null) {
			sb.and().append("tdate.topic.deletedDate is ").append("not ", params.getDeletedTopic()).append("null");
		}
		sb.orderBy().append("tdate.startDate").appendAsc(true).append(", tdate.key").appendAsc(true);
		
		TypedQuery<TBTopicDate> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TBTopicDate.class);
		if (params.getTopicKeys() != null) {
			query.setParameter("topicKeys", params.getTopicKeys());
		}
		if (params.getBrokerKeys() != null) {
			query.setParameter("brokerKeys", params.getBrokerKeys());
		}
		return query.getResultList();
	}
	
}
