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
package org.olat.core.dispatcher.mapper;

import java.util.Collections;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.manager.MapperDAO;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MapperServiceTest extends OlatTestCase {
	
	@Autowired
	private MapperDAO mapperDao;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserSessionManager sessionManager;
	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(mapperDao);
		Assert.assertNotNull(mapperService);
	}
	
	@Test
	public void testRegister() {
		UserSession session = createUserSession();
		MapperKey mapperKey = mapperService.register(session, new DummyMapper());
		Assert.assertNotNull(mapperKey);
		Assert.assertNotNull(mapperKey.getMapperId());
		Assert.assertNotNull(mapperKey.getSessionId());
		Assert.assertNotNull(mapperKey.getUrl());
		Assert.assertTrue(mapperService.inMemoryCount() > 0);
	}
	
	@Test
	public void testGetMapper() {
		//create a mapper
		UserSession session = createUserSession();
		DummyMapper mapper = new DummyMapper();
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();
		
		//retrieve the mapper
		Mapper reloadedMapper = mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper);
		Assert.assertEquals(mapper, reloadedMapper);
	}
	
	@Test
	public void testCleanUpMapper_notSerializable_byMappers() {
		//number of currently hold mappers
		int numOfMappers = mapperService.inMemoryCount();
		//create a mapper
		UserSession session = createUserSession();
		DummyMapper mapper = new DummyMapper();
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();
		
		//retrieve the mapper
		Mapper reloadedMapper = mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper);
		Assert.assertFalse(numOfMappers == mapperService.inMemoryCount());
		//cleanup
		mapperService.cleanUp(Collections.<MapperKey>singletonList(mapperKey));
		
		//check 1
		Mapper deletedMapper = mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNull(deletedMapper);
	}
	
	@Test
	public void testCleanUpMapper_notSerializable_bySessionId() {
		//number of currently hold mappers
		int numOfMappers = mapperService.inMemoryCount();
		//create a mapper
		UserSession session = createUserSession();
		DummyMapper mapper = new DummyMapper();
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();
		
		//retrieve the mapper
		Assert.assertFalse(numOfMappers == mapperService.inMemoryCount());
		//cleanup
		mapperService.cleanUp(session.getSessionInfo().getSession().getId());
		
		//check 1
		Assert.assertEquals(numOfMappers, mapperService.inMemoryCount());
		//check 2
		Mapper deletedMapper = mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNull(deletedMapper);
	}

	@Test
	public void testGetMapper_serializable() {
		//create a mapper
		int initialNumOfMappers = mapperService.inMemoryCount();
		UserSession session = createUserSession();
		PersistentMapper mapper = new PersistentMapper(UUID.randomUUID().toString());
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();

		//retrieve the mapper
		PersistentMapper reloadedMapper = (PersistentMapper)mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper);
		Assert.assertEquals(mapper.getKey(), reloadedMapper.getKey());
		Assert.assertFalse(initialNumOfMappers == mapperService.inMemoryCount());
		
		//remove in memory mappers
		mapperService.cleanUp(session.getSessionInfo().getSession().getId());
		Assert.assertEquals(initialNumOfMappers, mapperService.inMemoryCount());
		
		//reloaded episode 2
		PersistentMapper reloadedMapper2 = (PersistentMapper)mapperService.getMapperById(null, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper2);
		Assert.assertEquals(mapper.getKey(), reloadedMapper2.getKey());
	}
	
	@Test
	public void testChangingMapper_serializable() {
		//create a mapper
		int initialNumOfMappers = mapperService.inMemoryCount();
		UserSession session = createUserSession();
		PersistentMapper mapper = new PersistentMapper(UUID.randomUUID().toString());
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();

		//retrieve the mapper
		PersistentMapper reloadedMapper = (PersistentMapper)mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper);
		Assert.assertEquals(mapper, reloadedMapper);
		Assert.assertFalse(initialNumOfMappers == mapperService.inMemoryCount());
		
		//changing the key in the mapper
		String modKey = UUID.randomUUID().toString();
		reloadedMapper.setKey(modKey);

		//remove in memory mappers
		mapperService.cleanUp(Collections.<MapperKey>singletonList(mapperKey));
		mapperService.cleanUp(session.getSessionInfo().getSession().getId());
		Assert.assertEquals(initialNumOfMappers, mapperService.inMemoryCount());
		
		//reloaded episode 2
		PersistentMapper reloadedMapper2 = (PersistentMapper)mapperService.getMapperById(null, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper2);
		Assert.assertEquals(modKey, reloadedMapper2.getKey());
	}
	
	@Test
	public void testChangingMapper_serializableSessionChanged() {
		//create a mapper
		int initialNumOfMappers = mapperService.inMemoryCount();
		UserSession session = createUserSession();
		PersistentMapper mapper = new PersistentMapper(UUID.randomUUID().toString());
		MapperKey mapperKey = mapperService.register(session, mapper);
		dbInstance.commitAndCloseSession();

		//retrieve the mapper
		PersistentMapper reloadedMapper = (PersistentMapper)mapperService.getMapperById(session, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper);
		Assert.assertEquals(mapper, reloadedMapper);
		Assert.assertFalse(initialNumOfMappers == mapperService.inMemoryCount());
		
		//changing the key in the mapper
		String modKey = UUID.randomUUID().toString();
		reloadedMapper.setKey(modKey);

		//remove in memory mappers
		mapperService.cleanUp(Collections.<MapperKey>singletonList(mapperKey));
		mapperService.cleanUp(session.getSessionInfo().getSession().getId());
		Assert.assertEquals(initialNumOfMappers, mapperService.inMemoryCount());
		
		//reloaded episode 2
		UserSession session2 = createUserSession();
		PersistentMapper reloadedMapper2 = (PersistentMapper)mapperService.getMapperById(session2, mapperKey.getMapperId());
		Assert.assertNotNull(reloadedMapper2);
		Assert.assertEquals(modKey, reloadedMapper2.getKey());
	}
	
	private UserSession createUserSession() {
		HttpSession httpSession = new MockHttpSession();
		UserSession userSession = sessionManager.getUserSession(null, httpSession);
		SessionInfo infos = new SessionInfo(CodeHelper.getRAMUniqueID(), httpSession);
		userSession.setSessionInfo(infos);
		//check if our mocked HTTP session makes what we want
		Assert.assertNotNull(userSession.getSessionInfo());
		Assert.assertNotNull(userSession.getSessionInfo().getSession());
		Assert.assertNotNull(userSession.getSessionInfo().getSession().getId());
		return userSession;
	}
	
	private static class DummyMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			return null;
		}
	}
}
