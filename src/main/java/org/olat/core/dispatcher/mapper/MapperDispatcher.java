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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.logging.Tracing;
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
public class MapperDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(MapperDispatcher.class);
	
	public MapperDispatcher() {
		//
	}

	/**
	 * @param hreq
	 * @param hres
	 */
	@Override
	public void execute(HttpServletRequest hreq, HttpServletResponse hres) throws IOException {
		String pathInfo = DispatcherModule.subtractContextPath(hreq);
		// e.g. non-cacheable: 	23423/bla/blu.html
		// e.g. cacheable: 		my.mapper.path/bla/blu.html
		String subInfo = pathInfo.substring(DispatcherModule.PATH_MAPPED.length());
		int slashPos = subInfo.indexOf('/');
		
		String smappath;
		if (slashPos == -1) {
			smappath = subInfo;
		} else {
			smappath = subInfo.substring(0, slashPos);
		}
		
		
		//legacy???
		DBFactory.getInstance().commitAndCloseSession();

		// e.g. non-cacheable: 	23423
		// e.g. cacheable: 		my.mapper.path
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(hreq);
		MapperService mapperService = CoreSpringFactory.getImpl(MapperService.class);
		Mapper m = mapperService.getMapperById(usess, smappath);
		if (m == null) {
			//an anonymous mapper?
			m = mapperService.getMapperById(null, smappath);
			if(m == null) {
				log.debug("Call to mapped resource, but mapper does not exist for path::{}", smappath);
				hres.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}
		String mod = slashPos > 0 ? subInfo.substring(slashPos) : "";
		if (mod.indexOf("..") != -1) {
			log.warn("Illegal mapper path::{} contains '..'", mod);
			hres.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		// /bla/blu.html
		MediaResource mr = m.handle(mod, hreq);
		if(mr != null) {
			ServletUtil.serveResource(hreq, hres, mr);
		} else {
			hres.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
}