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
*/

package org.olat.repository.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class RepositoryHandlerFactory {

	private static RepositoryHandlerFactory INSTANCE;
	private static Map<String, RepositoryHandler> handlerMap;
	
	/**
	 * 
	 */
	private RepositoryHandlerFactory() {
		// singleton
	}

	static {
		INSTANCE = new RepositoryHandlerFactory();
		handlerMap = new HashMap<String, RepositoryHandler>(10);

		registerHandler(new WebDocumentHandler());
		registerHandler(new ImsCPHandler());
		registerHandler(new SCORMCPHandler());
		registerHandler(new CourseHandler());
		registerHandler(new SharedFolderHandler());
		registerHandler(new WikiHandler());
		registerHandler(new PodcastHandler());
		registerHandler(new BlogHandler());
		registerHandler(new GlossaryHandler());
		registerHandler(new PortfolioHandler());
	}

	public static void registerHandler(RepositoryHandler handler) {
		for (String type : handler.getSupportedTypes()) {
			handlerMap.put(type, handler);
		}
	}
	
	/**
	 * @return Singleton.
	 */
	public static RepositoryHandlerFactory getInstance() {	return INSTANCE; }
	
	/**
	 * Get the repository handler for this repository entry.
	 * @param re
	 * @return the handler or null if no appropriate handler could be found
	 */
	public RepositoryHandler getRepositoryHandler(RepositoryEntry re) {
		OLATResourceable ores = re.getOlatResource();
		if (ores == null) throw new AssertException("No handler found for resource. ores is null.");
		return getRepositoryHandler(ores.getResourceableTypeName());
	}
	
	/**
	 * Get a repository handler which supports the given resourceable type.
	 * @param resourceableTypeName
	 * @return the handler or null if no appropriate handler could be found
	 */
	public RepositoryHandler getRepositoryHandler(String resourceableTypeName) {
		return handlerMap.get(resourceableTypeName);
	}
	
	/**
	 * Get a set of types this factory supports.
	 * @return Set of supported types.
	 */
	public static Set<String> getSupportedTypes() {
		return handlerMap.keySet();
	}
}
