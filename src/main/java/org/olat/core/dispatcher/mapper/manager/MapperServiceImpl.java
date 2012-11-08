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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mapperService")
public class MapperServiceImpl implements MapperService {
	
	private Map<String,Mapper> mapperIdToMapper = new HashMap<String,Mapper>();
	private Map<Mapper,String> mapperToMapperId = new HashMap<Mapper, String>();
	private Map<String,List<String>> sessionIdToMapperIds = new HashMap<String,List<String>>();

	private CacheWrapper mapperCache;
	
	@Autowired
	private MapperDAO mapperDao;
	

	private CacheWrapper getMapperCache() {
		if (mapperCache == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableType(Mapper.class);
			CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
				@SuppressWarnings("synthetic-access")
				public void execute() {
					if (mapperCache == null) {
						Coordinator coordinator = CoordinatorManager.getInstance().getCoordinator();
						mapperCache = coordinator.getCacher().getOrCreateCache(MapperService.class, "mapper");
					}
				}
			});
		}
		return mapperCache;
	}
	
	@Override
	public int inMemoryCount() {
		return mapperIdToMapper.size() + mapperToMapperId.size() + sessionIdToMapperIds.size();
	}

	@Override
	public String register(UserSession session, Mapper mapper) {
		String mapid = UUID.randomUUID().toString().replace("-", "");
		return register(session, mapid, mapper);
	}	
		
	@Override
	public String register(UserSession session, String mapid, Mapper mapper) {
		String saveMapperID = Encoder.encrypt(mapid);
		return internRegister(session, saveMapperID, mapper);
	}
	
	private String internRegister(UserSession session, String mapid, Mapper mapper) {
		mapperIdToMapper.put(mapid, mapper);
		mapperToMapperId.put(mapper, mapid);
		if(session.getSessionInfo() == null) {
			return WebappHelper.getServletContextPath() + DispatcherAction.PATH_MAPPED + mapid;
		}
		
		String sessionId = session.getSessionInfo().getSession().getId();
		if(sessionIdToMapperIds.containsKey(sessionId)) {
			sessionIdToMapperIds.get(sessionId).add(mapid);
		} else {
			List<String> ampIds = new ArrayList<String>();
			ampIds.add(mapid);
			sessionIdToMapperIds.put(sessionId, ampIds);
		}
		
		if(mapper instanceof Serializable) {
			mapperDao.persistMapper(sessionId, mapid, (Serializable)mapper);
		}
		return WebappHelper.getServletContextPath() + DispatcherAction.PATH_MAPPED + mapid;
	}

	@Override
	public Mapper getMapperById(String id) {
		if(!StringHelper.containsNonWhitespace(id)) {
			return null;
		}
		
		int index = id.indexOf(DispatcherAction.PATH_MAPPED);
		if(index >= 0) {
			id = id.substring(index + DispatcherAction.PATH_MAPPED.length(), id.length());
		}
		
		Mapper mapper = mapperIdToMapper.get(id);
		if(mapper == null) {
			mapper = (Mapper)getMapperCache().get(id);
			if(mapper == null) {
				mapper = mapperDao.retrieveMapperById(id);
				getMapperCache().put(id, (Serializable)mapper);
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
		List<String> mapperIds = sessionIdToMapperIds.remove(sessionId);
		if(mapperIds != null && !mapperIds.isEmpty()) {
			for(String mapperId:mapperIds) {
				Mapper mapper = mapperIdToMapper.remove(mapperId);
				if(mapper != null) {
					if(mapper instanceof Serializable) {
						mapperDao.updateConfiguration(mapperId, (Serializable)mapper);
					}
					mapperToMapperId.remove(mapper);
				}
			}
		}
	}
	
	@Override
	public void cleanUp(List<Mapper> mappers) {
		if(mappers == null || mappers.isEmpty()) return;
		for(Mapper mapper:mappers) {
			String mapperId = mapperToMapperId.remove(mapper);
			if(mapperId != null) {
				mapperIdToMapper.remove(mapperId);
				if(mapper instanceof Serializable) {
					mapperDao.updateConfiguration(mapperId, (Serializable)mapper);
				}
			}
			
		}
	}
}