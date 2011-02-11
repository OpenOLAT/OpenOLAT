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

package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.search.service.SearchResourceContext;

/**
 * Common abstract indexer. Used as base class for indexers.
 * @author Christian Guretzki
 */
public abstract class AbstractIndexer implements Indexer {
	
	private static final OLog log = Tracing.createLoggerFor(AbstractIndexer.class);
	
	protected Map<String,Indexer> childIndexers = new HashMap<String,Indexer>();
	

	/**
	 * Bean setter method used by spring. 
	 * @param indexerList
	 */
	public void setIndexerList(List<Indexer> indexerList) {
		if (indexerList == null)
			throw new AssertException("null value for indexerList not allowed.");

		try {
			for (Indexer indexer:indexerList) {
				childIndexers.put(indexer.getSupportedTypeName(), indexer);
				log.debug("Adding indexer from configuraton. TypeName=" + indexer.getSupportedTypeName());
			} 
		}	catch (ClassCastException cce) {
				throw new StartupException("Configured indexer is not of type Indexer", cce);
		}
	}

	/**
	 * Iterate over all child indexer define in indexer-list.
	 * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext, java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
	public void doIndex(SearchResourceContext searchResourceContext, Object object, OlatFullIndexer indexerWriter) throws IOException, InterruptedException {
		for (Indexer indexer : childIndexers.values()) {
			if (log.isDebug()) log.debug("Start doIndex for indexer.typeName=" + indexer.getSupportedTypeName());
			try {
			  indexer.doIndex(searchResourceContext, object, indexerWriter);
			} catch (InterruptedException iex) {
				throw iex;
			}	catch (Throwable ex) {
				// FIXME:chg: Workaround to fix indexing-abort
				log.warn("Exception in diIndex indexer.typeName=" + indexer.getSupportedTypeName(),ex);
			}
		}
	}
	
	/**
	 * 
	 * @param businessControl
	 * @param identity
	 * @param roles
	 * @return
	 */
	public boolean checkAccess(BusinessControl businessControl, Identity identity, Roles roles) {
		if (log.isDebug()) log.debug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
		ContextEntry contextEntry = businessControl.popLauncherContextEntry();
		if (contextEntry != null) {
			// there is an other context-entry => go further
			OLATResourceable ores = contextEntry.getOLATResourceable();
		  String type = ores.getResourceableTypeName();
			Indexer indexer = this.childIndexers.get(type);
			return indexer.checkAccess(contextEntry, businessControl, identity, roles);
		} else {
			// rearch the end context entry list 
			return true;
		}
	}
	
}
