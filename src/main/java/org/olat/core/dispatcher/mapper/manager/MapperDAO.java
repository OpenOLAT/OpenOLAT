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
import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.model.PersistedMapper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mapperDao")
public class MapperDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PersistedMapper persistMapper(String sessionId, String mapperId, Serializable mapper) {
		PersistedMapper m = new PersistedMapper();
		m.setMapperId(mapperId);
		m.setOriginalSessionId(sessionId);
		
		String configuration = XStreamHelper.createXStreamInstance().toXML(mapper);
		m.setXmlConfiguration(configuration);
		
		dbInstance.getCurrentEntityManager().persist(m);
		return m;
	}
	
	public void updateConfiguration(String mapperId, Serializable mapper) {
		PersistedMapper m = loadForUpdate(mapperId);
		
		String configuration = XStreamHelper.createXStreamInstance().toXML(mapper);
		m.setXmlConfiguration(configuration);
		m.setLastModified(new Date());
		
		dbInstance.getCurrentEntityManager().merge(m);
	}
	
	private PersistedMapper loadForUpdate(String mapperId) {
		StringBuilder q = new StringBuilder();
		q.append("select mapper from ").append(PersistedMapper.class.getName()).append(" as mapper ")
		 .append(" where mapper.mapperId=:mapperId");
		
		List<PersistedMapper> mappers = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), PersistedMapper.class)
				.setParameter("mapperId", mapperId)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return mappers.isEmpty() ? null : mappers.get(0);
	}
	
	public PersistedMapper loadByMapperId(String mapperId) {
		StringBuilder q = new StringBuilder();
		q.append("select mapper from ").append(PersistedMapper.class.getName()).append(" as mapper ")
		 .append(" where mapper.mapperId=:mapperId");
		
		List<PersistedMapper> mappers = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), PersistedMapper.class)
				.setParameter("mapperId", mapperId)
				.getResultList();
		return mappers.isEmpty() ? null : mappers.get(0);
	}
	
	public Mapper retrieveMapperById(String mapperId) {
		StringBuilder q = new StringBuilder();
		q.append("select mapper from ").append(PersistedMapper.class.getName()).append(" as mapper ")
		 .append(" where mapper.mapperId=:mapperId");
		
		List<PersistedMapper> mappers = dbInstance.getCurrentEntityManager()
				.createQuery(q.toString(), PersistedMapper.class)
				.setParameter("mapperId", mapperId)
				.getResultList();
		PersistedMapper pm = mappers.isEmpty() ? null : mappers.get(0);
		
		if(pm != null && StringHelper.containsNonWhitespace(pm.getXmlConfiguration())) {
			String configuration = pm.getXmlConfiguration();
			
			Object obj = XStreamHelper.createXStreamInstance().fromXML(configuration);
			if(obj instanceof Mapper) {
				return (Mapper)obj;
			}
		}
		return null;
	}
	
	public int deleteMapperByDate(Date limit) {
		StringBuilder q = new StringBuilder();
		q.append("delete from ").append(PersistedMapper.class.getName()).append(" as mapper ")
		 .append(" where mapper.lastModified<:limit");

		return dbInstance.getCurrentEntityManager()
				.createQuery(q.toString())
				.setParameter("limit", limit, TemporalType.TIMESTAMP)
				.executeUpdate();
	}
}
