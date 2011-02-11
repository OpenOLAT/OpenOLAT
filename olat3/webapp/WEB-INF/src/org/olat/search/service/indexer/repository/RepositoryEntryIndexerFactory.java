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
* <p>
*/ 

package org.olat.search.service.indexer.repository;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.indexer.Indexer;

/**
 * Factory to get indexer of certain repository entry. E.g. CourseIndexer
 * @author Christian Guretzki
 * 
 */
public class RepositoryEntryIndexerFactory {

	private static RepositoryEntryIndexerFactory INSTANCE;
	private Map handlerMap = new HashMap(10);
	
	static { INSTANCE = new RepositoryEntryIndexerFactory(); }
	
	/**
	 * 
	 */
	private RepositoryEntryIndexerFactory() {
		// singleton
	}

	public void registerIndexer(Indexer indexer) {
		handlerMap.put(indexer.getSupportedTypeName(), indexer);
	}
	
	/**
	 * @return Singleton.
	 */
	public static RepositoryEntryIndexerFactory getInstance() {	return INSTANCE; }
	
	/**
	 * Get the repository handler for this repository entry.
	 * @param re
	 * @return the handler or null if no appropriate handler could be found
	 */
	public Indexer getRepositoryEntryIndexer(RepositoryEntry re) {
		OLATResourceable ores = re.getOlatResource();
		if (ores == null) throw new AssertException("RepositoryEntry has no OlatResource [re.getOlatResource()==null].");
		return getRepositoryEntryIndexer(ores.getResourceableTypeName());
	}
	
	/**
	 * Get a repository handler which supports the given resourceable type.
	 * @param resourceableTypeName
	 * @return the handler or null if no appropriate handler could be found
	 */
	public Indexer getRepositoryEntryIndexer(String resourceableTypeName) {
		return (Indexer)handlerMap.get(resourceableTypeName);
	}


}
