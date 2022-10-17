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
package org.olat.core.dispatcher.mapper.manager;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.model.PersistedMapper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mapperDao")
public class MapperDAO {
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(xstream);
	}
	
	@Autowired
	private DB dbInstance;
	
	/**
	 * Persist a mapper on the database.
	 * 
	 * @param sessionId The HTTP session id
	 * @param mapperId The unique id of the mapper
	 * @param mapper The mapper (serializable)
	 * @param expirationTime The expiration time in seconds
	 * @return
	 */
	public PersistedMapper persistMapper(String sessionId, String mapperId, Serializable mapper, int expirationTime) {
		PersistedMapper m = new PersistedMapper();
		m.setMapperId(mapperId);
		Date currentDate = new Date();
		m.setLastModified(currentDate);
		if(expirationTime > 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.SECOND, expirationTime);
			m.setExpirationDate(cal.getTime());
		}
		m.setOriginalSessionId(sessionId);
		
		String configuration = xstream.toXML(mapper);
		m.setXmlConfiguration(configuration);
		
		dbInstance.getCurrentEntityManager().persist(m);
		return m;
	}
	
	protected static String toXml(Object mapper) {
		return xstream.toXML(mapper);
	}
	
	public static Object fromXML(String configuration) {
		return xstream.fromXML(configuration);
	}
	
	/**
	 * Update a persisted mapper.
	 * 
	 * @param mapperId The mapper unique id (uuid)
	 * @param mapper The mapper itself (serializable)
	 * @param expirationTime The expiration time in seconds
	 * @return
	 */
	public boolean updateConfiguration(String mapperId, Serializable mapper, int expirationTime) {
		String configuration = xstream.toXML(mapper);
		Date currentDate = new Date();
		Date expirationDate = null;
		if(expirationTime > 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(currentDate);
			cal.add(Calendar.SECOND, expirationTime);
			expirationDate = cal.getTime();
		}
		int row = dbInstance.getCurrentEntityManager().createNamedQuery("updateMapperByMapperId")
			.setParameter("now", currentDate)
			.setParameter("expirationDate", expirationDate)
			.setParameter("config", configuration)
			.setParameter("mapperId", mapperId)
			.executeUpdate();

		dbInstance.commit();
		return row > 0;
	}
	
	public PersistedMapper loadByMapperId(String mapperId) {
		List<PersistedMapper> mappers = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadMapperByKey", PersistedMapper.class)
				.setParameter("mapperId", mapperId)
				.getResultList();
		return mappers.isEmpty() ? null : mappers.get(0);
	}
	
	public Mapper retrieveMapperById(String mapperId) {
		List<PersistedMapper> mappers = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadMapperByKey", PersistedMapper.class)
				.setParameter("mapperId", mapperId)
				.getResultList();
		PersistedMapper pm = mappers.isEmpty() ? null : mappers.get(0);
		
		if(pm != null && StringHelper.containsNonWhitespace(pm.getXmlConfiguration())) {
			String configuration = pm.getXmlConfiguration();
			
			Object obj = xstream.fromXML(configuration);
			if(obj instanceof Mapper) {
				return (Mapper)obj;
			}
		}
		return null;
	}
	
	public int deleteMapperByDate(Date limit) {
		StringBuilder q = new StringBuilder();
		q.append("delete from pmapper as mapper where ")
		 .append(" (mapper.expirationDate is null and mapper.lastModified<:limit)")
		 .append(" or (mapper.expirationDate<:now)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(q.toString())
				.setParameter("limit", limit, TemporalType.TIMESTAMP)
				.setParameter("now", new Date(), TemporalType.TIMESTAMP)
				.executeUpdate();
	}
}
