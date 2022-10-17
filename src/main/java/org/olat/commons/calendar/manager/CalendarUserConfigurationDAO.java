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
package org.olat.commons.calendar.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CalendarUserConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	/**
	 * Create and persist an user configuration with default settings:
	 * 
	 * @param calendar
	 * @param identity
	 * @return
	 */
	public CalendarUserConfiguration createCalendarUserConfiguration(Kalendar calendar, Identity identity) {
		return createCalendarUserConfiguration(calendar, identity, null, true, true);
	}
	
	/**
	 * Create and persist an user configuration
	 * @param calendar
	 * @param identity
	 * @param token
	 * @param aggregated
	 * @param visible
	 * @return
	 */
	public CalendarUserConfiguration createCalendarUserConfiguration(Kalendar calendar, Identity identity, String token, boolean aggregated, boolean visible) {
		CalendarUserConfiguration config = new CalendarUserConfiguration();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setCalendarId(calendar.getCalendarID());
		config.setType(calendar.getType());
		config.setToken(token);
		config.setIdentity(identity);
		config.setInAggregatedFeed(aggregated);
		config.setVisible(visible);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public List<CalendarUserConfiguration> getCalendarUserConfigurations(IdentityRef identity, String type, String calendarId) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadCalUserConfigByIdentityAndCalendar", CalendarUserConfiguration.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("type", type)
				.setParameter("calendarId", calendarId)
				.getResultList();
	}
	
	public List<CalendarUserConfiguration> getCalendarUserConfigurations(IdentityRef identity, String... types) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select conf from caluserconfig conf where conf.identity.key=:identityKey");
		if(types != null && types.length > 0 && types[0] != null) {
			sb.append(" and conf.type in (:types)");
		}
		
		TypedQuery<CalendarUserConfiguration> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalendarUserConfiguration.class)
				.setParameter("identityKey", identity.getKey());
		if(types != null && types.length > 0 && types[0] != null) {
			List<String> typeList = new ArrayList<>(types.length);
			for(String type:types) {
				typeList.add(type);
			}
			query.setParameter("types", typeList);
		}
		return query.getResultList();
	}
	
	public CalendarUserConfiguration getCalendarUserConfiguration(IdentityRef identity, String calendarId, String type) {
		List<CalendarUserConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadCalUserConfigByIdentityAndCalendar", CalendarUserConfiguration.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("calendarId", calendarId)
				.setParameter("type", type)
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public CalendarUserConfiguration update(CalendarUserConfiguration config) {
		config.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(config);
	}
	
	public String getCalendarToken(String calendarType, String calendarID, String username) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select conf.token from caluserconfig conf")
		  .append(" inner join conf.identity ident")
		  .append(" where ident.name=:username and conf.calendarId=:calendarId and conf.type=:type");
		
		List<String> tokens = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("username", username)
				.setParameter("calendarId", calendarID)
				.setParameter("type", calendarType)
				.getResultList();
		return tokens == null || tokens.isEmpty() ? null : tokens.get(0);
	}
	
	public CalendarUserConfiguration getCalendarUserConfiguration(Long key) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select conf from caluserconfig conf")
		  .append(" inner join fetch conf.identity ident")
		  .append(" where conf.key=:key");
		
		List<CalendarUserConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CalendarUserConfiguration.class)
				.setParameter("key", key)
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
}
