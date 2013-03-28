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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.dispatcher.mapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;

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
public class MapperDispatcher extends LogDelegator implements Dispatcher {
	
	public MapperDispatcher() {
		//
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
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(hreq);
		Mapper m = CoreSpringFactory.getImpl(MapperService.class).getMapperById(usess, smappath);
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
}