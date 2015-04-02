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

import java.util.List;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.util.UserSession;

/**
 * Description:<br>
 * Allows to register so called mappers, which map a certain session-unique url
 * to a mapper which can dispatch requests to that url.
 * <P>
 * As of the 7.0 release, an additional cacheable mapper register method has
 * been added that allows registering of mappers with a permanent mapper ID.
 * This allows browsers to cache the resources delivered by the mapper using the
 * last-modified date.
 * <P>
 * Initial Date: 10.06.2005 <br>
 * 
 * @author Felix Jost
 * @author Florian Gnägi, <a href="http://www.frentix.com">frentix GmbH</a>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface MapperService {
	
	/**
	 * Sum of the size of the maps used as cache
	 * 
	 * @return
	 */
	public int inMemoryCount();
	
	
	/**
	 * Register a non-cachable mapper. The mapper will have a new URL every time
	 * it is registered.
	 * <p>
	 * If your resources are static files, use the cacheable register version to
	 * allow browsers using the last-modified date to cache the resources.
	 * <p>
	 * 
	 * @param mapper
	 * @return the path e.g. /olat/m/1001 without / at the end
	 */
	public MapperKey register(UserSession session, Mapper mapper);
	
	/**
	 * Register a cacheable mapper by giving a mapper path ID. When setting the
	 * same ID every time for the same usage context, the browser can use the
	 * last modified date to cache the resources. The mapper ID will be use as
	 * MD5 hash to make it URL save and shorter. In debug mode, all special
	 * characters are replaced with the '_' character.
	 * <p>
	 * Whenever possible use this method to improve the user experience.
	 * <p>
	 * 
	 * @param cacheableMapperID
	 *            the ID of the mapper.
	 * @param mapper
	 * @return the path e.g. /olat/c/my.mapper.path without / at the end
	 */
	public MapperKey register(UserSession session, String mapperId, Mapper mapper);
	
	/**
	 * Same as above but with a defined expiration time
	 * @param session
	 * @param mapperId
	 * @param mapper
	 * @param expiration Expiration time in seconds
	 * @return
	 */
	public MapperKey register(UserSession session, String mapperId, Mapper mapper, int expiration);
	
	
	
	public Mapper getMapperById(UserSession session, String id);
	
	public void cleanUp(String sessionId);

	public void cleanUp(List<MapperKey> mappers);
	
	public void slayZombies();
	
	

}
