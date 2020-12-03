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
package org.olat.modules.bigbluebutton.manager;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonMeetingQueries {
	
	@Autowired
	private DB dbInstance;
	
	public int count(BigBlueButtonMeetingsSearchParameters searchParams) {
		TypedQuery<Number> query = createViewQuery(searchParams, Number.class);
		Number count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<BigBlueButtonMeetingAdminInfos> search(BigBlueButtonMeetingsSearchParameters searchParams, int firstResult, int maxResults) {
		TypedQuery<Object[]> query = createViewQuery(searchParams,  Object[].class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<Object[]> rawObjects = query.getResultList();
		List<BigBlueButtonMeetingAdminInfos> infos = new ArrayList<>(rawObjects.size());
		for(Object[] objects:rawObjects) {
			BigBlueButtonMeeting meeting =(BigBlueButtonMeeting)objects[0];
			long numOfRecordings = PersistenceHelper.extractPrimitiveLong(objects, 1);
			infos.add(new BigBlueButtonMeetingAdminInfos(meeting, numOfRecordings));
		}
		return infos;
	}
	
	private <T> TypedQuery<T> createViewQuery(BigBlueButtonMeetingsSearchParameters params, Class<T> type) {
		boolean count = Number.class.equals(type);
		QueryBuilder sb = new QueryBuilder(2048);
		if(count) {
			sb.append("select count(meeting.key)");
		} else {
			sb.append("select meeting, ")
			  .append(" (select count(record.key) from bigbluebuttonrecording as record")
			  .append("  where record.meeting.key=meeting.key")
			  .append(" ) as numOfRecordings");
		}
		sb.append(" from bigbluebuttonmeeting as meeting")
		  .append(" left join ").append("fetch", !count).append(" meeting.template as template")
		  .append(" left join ").append("fetch", !count).append(" meeting.server as server")
		  .append(" left join ").append("fetch", !count).append(" meeting.entry as entry")
		  .append(" left join ").append("fetch", !count).append(" meeting.businessGroup as businessGroup");
		
		Long id = null;
		String fuzzySearch = null;
		if(StringHelper.containsNonWhitespace(params.getSearchString())) {
			String search = params.getSearchString();
			fuzzySearch = PersistenceHelper.makeFuzzyQueryString(search);
			sb.and().append(" (lower(meeting.name) like :search")
			  .append(" or lower(entry.displayname) like :search")
			  .append(" or lower(businessGroup.name) like :search")
			  .append(" or lower(template.name) like :search")
			  .append(" or lower(server.url) like :search");
			
			if(StringHelper.isLong(search)) {
				try {
					id = Long.parseLong(search);
					sb.append(" or meeting.key=:mKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		if(params.getRecordings() != null) {
			sb.and()
			  .append(" not", !params.getRecordings().booleanValue())
			  .append(" exists (select recording.key from bigbluebuttonrecording as recording")
			  .append("  where recording.meeting.key=meeting.key)");
		}
		
		if(!count && params.getOrder() != null) {
			appendOrderBy(sb, params.getOrder(), params.isOrderAsc());
		}
		
		TypedQuery<T> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), type);
		
		if(id != null) {
			dbQuery.setParameter("mKey", id);
		}
		if(fuzzySearch != null) {
			dbQuery.setParameter("search", fuzzySearch);
		}
		
		return dbQuery;
	}
	
	private void appendOrderBy(QueryBuilder sb, OrderBy orderBy, boolean asc) {
		switch(orderBy) {
			case name:
				sb.append(" order by lower(meeting.name) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case permanent:
				sb.append(" order by meeting.permanent ").append("asc", "desc", asc).append(" nulls last");
				break;
			case startDate:
				sb.append(" order by meeting.startDate ").append("asc", "desc", asc).append(" nulls last");
				break;
			case endDate:
				sb.append(" order by meeting.endDate ").append("asc", "desc", asc).append(" nulls last");
				break;
			case server:
				sb.append(" order by server.url ").append("asc", "desc", asc).append(" nulls last");
				break;
			case template:
				sb.append(" order by lower(template.name) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case resource:
				sb.append(" order by lower(entry.displayname) ").append("asc", "desc", asc).append(", lower(businessGroup.name) ").append("asc", "desc", asc).append(" nulls last");
				break;
			case recordings:
				sb.append(" order by numOfRecordings ").append("asc", "desc", asc).append(", lower(meeting.name) ").append("asc", "desc", asc).append(" nulls last");
				break;	
				
			default: break;
		}
	}
}
