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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.model.PersistedMapper;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mapperService")
public class MapperServiceImpl implements MapperService, InitializingBean {
	
	private Map<MapperKey,Mapper> mapperKeyToMapper = new ConcurrentHashMap<>();
	private Map<String,List<MapperKey>> sessionIdToMapperKeys = new ConcurrentHashMap<>();

	private CacheWrapper<String, Mapper> mapperCache;
	
	@Autowired
	private MapperDAO mapperDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		mapperCache = coordinatorManager.getCoordinator().getCacher().getCache(MapperService.class.getSimpleName(), "mapper");
	}
	
	@Override
	public int inMemoryCount() {
		return mapperKeyToMapper.size() + sessionIdToMapperKeys.size();
	}

	@Override
	public MapperKey register(UserSession session, Mapper mapper) {
		String mapid = UUID.randomUUID().toString().replace("-", "");
		mapid = Encoder.md5hash(mapid);
		
		MapperKey mapperKey = new MapperKey(session, mapid);
		mapperKeyToMapper.put(mapperKey, mapper);
		if(session == null || session.getSessionInfo() == null) {
			mapperKey.setUrl(WebappHelper.getServletContextPath() + DispatcherModule.PATH_MAPPED + mapid);
			return mapperKey;
		}
		
		String sessionId = session.getSessionInfo().getSession().getId();
		if(sessionIdToMapperKeys.containsKey(sessionId)) {
			sessionIdToMapperKeys.get(sessionId).add(mapperKey);
		} else {
			List<MapperKey> mapKeys = new ArrayList<>();
			mapKeys.add(mapperKey);
			sessionIdToMapperKeys.put(sessionId, mapKeys);
		}
		
		if(mapper instanceof Serializable) {
			mapperDao.persistMapper(sessionId, mapid, (Serializable)mapper, -1);
		}
		mapperKey.setUrl(WebappHelper.getServletContextPath() + DispatcherModule.PATH_MAPPED + mapid);
		return mapperKey;
	}	

	/**
	 * Cacheable mapper, not session dependant
	 */
	@Override
	public MapperKey register(UserSession session, String mapperId, Mapper mapper) {
		return register(session, mapperId, mapper, -1);
	}

	@Override
	public MapperKey register(UserSession session, String mapperId, Mapper mapper, int expirationTime) {
		String encryptedMapId = Encoder.md5hash(mapperId);
		MapperKey mapperKey = new MapperKey(session, encryptedMapId);
		boolean alreadyLoaded = mapperKeyToMapper.containsKey(mapperKey);
		if(mapper instanceof Serializable) {
			if(alreadyLoaded) {
				if(!mapperDao.updateConfiguration(encryptedMapId, (Serializable)mapper, expirationTime)) {
					mapperDao.persistMapper(null, encryptedMapId, (Serializable)mapper, expirationTime);
				}
			} else {
				PersistedMapper persistedMapper = mapperDao.loadByMapperId(encryptedMapId);
				if(persistedMapper == null) {
					mapperDao.persistMapper(null, encryptedMapId, (Serializable)mapper, expirationTime);
				} else {
					mapperDao.updateConfiguration(encryptedMapId, (Serializable)mapper, expirationTime);
				}
			}
		} else if(expirationTime > 0) {
			mapperCache.put(encryptedMapId, mapper, expirationTime);
		}

		mapperKeyToMapper.put(mapperKey, mapper);
		mapperKey.setUrl(WebappHelper.getServletContextPath() + DispatcherModule.PATH_MAPPED + encryptedMapId);
		return mapperKey;
	}

	@Override
	public Mapper getMapperById(UserSession session, String id) {
		if(!StringHelper.containsNonWhitespace(id)) {
			return null;
		}
		
		int index = id.indexOf(DispatcherModule.PATH_MAPPED);
		if(index >= 0) {
			id = id.substring(index + DispatcherModule.PATH_MAPPED.length(), id.length());
		}
		
		MapperKey mapperKey = new MapperKey(session, id);
		Mapper mapper = mapperKeyToMapper.get(mapperKey);
		if(mapper == null) {
			mapper = mapperCache.get(id);
			if(mapper == null) {
				mapper = mapperDao.retrieveMapperById(id);
				if(mapper != null) {
					mapperCache.put(id, mapper);
				}
			}
		}
		return mapper;
	}

	@Override
	public void slayZombies() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, -6);
		mapperDao.deleteMapperByDate(cal.getTime());
	}

	@Override
	public void cleanUp(String sessionId) {
		List<MapperKey> mapKeys = sessionIdToMapperKeys.remove(sessionId);
		if(mapKeys != null && !mapKeys.isEmpty()) {
			for(MapperKey mapKey:mapKeys) {
				Mapper mapper = mapperKeyToMapper.remove(mapKey);
				if(mapper != null) {
					if(mapper instanceof Serializable) {
						mapperDao.updateConfiguration(mapKey.getMapperId(), (Serializable)mapper, -1);
					}
				}
			}
		}
	}
	
	@Override
	public void cleanUp(List<MapperKey> mapperKeys) {
		if(mapperKeys == null || mapperKeys.isEmpty()) return;
		for(MapperKey mapperKey:mapperKeys) {
			Mapper mapper = mapperKeyToMapper.remove(mapperKey);
			if(mapper instanceof Serializable) {
				mapperDao.updateConfiguration(mapperKey.getMapperId(), (Serializable)mapper, -1);
			}
		}
	}
}