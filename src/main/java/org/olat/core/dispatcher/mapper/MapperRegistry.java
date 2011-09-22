/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.dispatcher.mapper;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Encoder;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;

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
 */
public class MapperRegistry  extends LogDelegator implements Dispatcher {
	private static final String CNAME = MapperRegistry.class.getName();
	private Map<String, Mapper> map = new HashMap<String, Mapper>();
	private int mapid;

	/**
	 * 
	 */
	private MapperRegistry() {
		// to avoid browser caching if logging in again using the same browser (css
		// cache seems not to be emptied even if browser is closed...)
		mapid = ((int) (System.currentTimeMillis() % 1000l)) * 100;
	}

	/**
	 * @param us
	 * @return MapperRegistry
	 */
	public static MapperRegistry getInstanceFor(UserSession us) {
		MapperRegistry mreg;
			mreg = (MapperRegistry) us.getEntry(CNAME);
			if (mreg == null) {
				synchronized (us) {
					// need to sync since a browser may request many e.g. pictures in parallel
					mreg = (MapperRegistry) us.getEntry(CNAME);
					if(mreg == null){
						mreg = new MapperRegistry();
						us.putEntry(CNAME, mreg);						
					}
				}
			}
		//}
		return mreg;
	}

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
	public String register(Mapper mapper) {
		int cur = mapid;
		map.put(String.valueOf(cur), mapper);
		mapid++; // do not sync instance var inc since mappers of a user will not
							// be allocated concurrently (should be done in constructors of
							// controllers only / = one gui thread).
		return WebappHelper.getServletContextPath() + DispatcherAction.PATH_MAPPED + cur;
	}

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
	public String registerCacheable(String cacheableMapperID, Mapper mapper) {
		// Use MD5 hash from mapper ID unless when in debug mode - this solves the problem of invalid 
		String saveMapperID;;
		if (isLogDebugEnabled()) {
			saveMapperID = cacheableMapperID.replaceAll(
					"[^a-zA-Z0-9-_.:]", "_");
		} else {
			saveMapperID = Encoder.encrypt(cacheableMapperID);
		}
		if (map.containsKey(saveMapperID)) {
			logWarn(
					"Trying to add a cacheable mapper with the cacheableMapperID::"
							+ cacheableMapperID
							+ " that is already registered for another mapper. Using a non-cachable mapper instead. Check your code and use unique mapper ID's everywhere!",
					null);
			return register(mapper);
		} else {
			map.put(saveMapperID, mapper);
			return WebappHelper.getServletContextPath()
					+ DispatcherAction.PATH_MAPPED + saveMapperID;
		}
	}
	
	/**
	 * @param hreq
	 * @param hres
	 */
	public void execute(HttpServletRequest hreq, HttpServletResponse hres, String pathInfo) {
		final boolean isDebugLog = isLogDebugEnabled();
		StringBuilder debugMsg = null;
		long debug_start = 0;
		if (isDebugLog) {
			debug_start = System.currentTimeMillis();
			debugMsg = new StringBuilder("::mprex:");
		}
		
		// e.g. non-cacheable: 	23423/bla/blu.html
		// e.g. cacheable: 		my.mapper.path/bla/blu.html
		String subInfo = pathInfo.substring(DispatcherAction.PATH_MAPPED.length());
		int slashPos = subInfo.indexOf('/');
		if (slashPos == -1) {
			DispatcherAction.sendNotFound("not found", hres);
			return;
		}

		// e.g. non-cacheable: 	23423
		// e.g. cacheable: 		my.mapper.path
		String smappath = subInfo.substring(0, slashPos);
		Mapper m = map.get(smappath);
		if (m == null) {
			logWarn(
					"Call to mapped resource, but mapper does not exist for path::"
							+ pathInfo, null);
			DispatcherAction.sendNotFound(pathInfo, hres);
			return;
		}
		String mod = subInfo.substring(slashPos);
		if (mod.indexOf("..") != -1) {
			logWarn("Illegal mapper path::" + mod + " contains '..'",
					null);
			DispatcherAction.sendForbidden(pathInfo, hres);
			return;
		}
		// /bla/blu.html
		MediaResource mr = m.handle(mod, hreq);
		ServletUtil.serveResource(hreq, hres, mr);

		if (isDebugLog) {
			long afterserved = System.currentTimeMillis();
			long syncIntroDiff = afterserved - debug_start;
			debugMsg.append("nanoseconds:").append(syncIntroDiff);
			logDebug(debugMsg.toString());
		}
	}

	/**
	 * remove a registered mapper if it is no longer needed.
	 * 
	 * @param mapper
	 */
	public void deregister(Mapper mapper) {
		map.values().remove(mapper);
	}

}
