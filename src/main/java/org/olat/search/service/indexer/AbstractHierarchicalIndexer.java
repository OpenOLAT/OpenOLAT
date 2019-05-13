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

package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.search.service.SearchResourceContext;

/**
 * Common abstract indexer. Used as base class for indexers.
 * @author Christian Guretzki
 */
public abstract class AbstractHierarchicalIndexer extends DefaultIndexer {

	private static final Logger log = Tracing.createLoggerFor(AbstractHierarchicalIndexer.class);
	
	private final List<Indexer> childIndexers = new ArrayList<>();
	
	public List<Indexer> getChildIndexers() {
		return childIndexers;
	}
	
	/**
	 * Bean setter method used by spring. 
	 * @param indexerList
	 */
	public void setIndexerList(List<Indexer> indexerList) {
		if (indexerList == null) {
			throw new AssertException("null value for indexerList not allowed.");
		}

		try {
			for (Indexer indexer:indexerList) {
				childIndexers.add(indexer);
				log.debug("Adding indexer from configuraton. TypeName=" + indexer.getSupportedTypeName());
			} 
		} catch (ClassCastException cce) {
			throw new StartupException("Configured indexer is not of type Indexer", cce);
		}
	}
	
	public void addIndexer(Indexer indexer) {
		try {
			childIndexers.add(indexer);
			log.debug("Adding indexer from configuraton. TypeName=" + indexer.getSupportedTypeName());
		}	catch (ClassCastException cce) {
			throw new StartupException("Configured indexer is not of type Indexer", cce);
		}
	}

	/**
	 * Iterate over all child indexer define in indexer-list.
	 * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext, java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object object, OlatFullIndexer indexerWriter) throws IOException, InterruptedException {
		for (Indexer indexer : childIndexers) {
			if (log.isDebugEnabled()) log.debug("Start doIndex for indexer.typeName=" + indexer.getSupportedTypeName());
			try {
			  indexer.doIndex(searchResourceContext, object, indexerWriter);
			} catch (InterruptedException iex) {
				throw iex;
			} catch (Throwable ex) {
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
		boolean debug = log.isDebugEnabled();
		if(debug) log.debug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
		
		ContextEntry contextEntry = businessControl.popLauncherContextEntry();
		if (contextEntry != null) {
			// there is an other context-entry => go further
			OLATResourceable ores = contextEntry.getOLATResourceable();
		  String type = ores.getResourceableTypeName();
		  List<Indexer> indexers = getIndexerByType(type);
			if (indexers.isEmpty()) {
				// loop in child-indexers to check access for businesspath not stacked as on index-run
				for (Indexer childIndexer: childIndexers) {
					List<Indexer> foundSubChildIndexers = childIndexer instanceof  AbstractHierarchicalIndexer ? ((AbstractHierarchicalIndexer)childIndexer).getIndexerByType(type) : null;
					if (foundSubChildIndexers != null) {
						if (debug) log.debug("took a childindexer for ores= " + ores + " not directly linked (means businesspath is not the same stack as indexer -> childindexer). type= " +type + " . indexer parent-type not on businesspath=" + childIndexer.getSupportedTypeName());
						for(Indexer foundSubChildIndexer:foundSubChildIndexers) {
							boolean allow = foundSubChildIndexer.checkAccess(contextEntry, businessControl, identity, roles)
									&& super.checkAccess(contextEntry, businessControl, identity, roles);
							if(allow) {
								return true;
							}
						}
					}
				}				
				if(debug) log.debug("could not find an indexer for type="+type + " businessControl="+businessControl + " identity=" + identity);
			} else {
				for(Indexer indexer:indexers) {
					boolean allow = indexer.checkAccess(contextEntry, businessControl, identity, roles)
						&& super.checkAccess(contextEntry, businessControl, identity, roles);
					if(allow) {
						return true;
					}
				}
			}
			return false;
		} else {
			// rearch the end context entry list 
			return super.checkAccess(contextEntry, businessControl, identity, roles);
		}
	}
	
	public List<Indexer> getIndexerByType(String type) {
		List<Indexer> indexerByType = new ArrayList<>();
		if(childIndexers != null && !childIndexers.isEmpty()) {
			for(Indexer childIndexer:childIndexers) {
				if(type.equals(childIndexer.getSupportedTypeName())) {
					indexerByType.add(childIndexer);
				}
			}
		}
		return indexerByType;
	}
}
