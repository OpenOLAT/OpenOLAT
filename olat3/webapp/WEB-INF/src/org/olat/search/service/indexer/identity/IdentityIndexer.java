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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.search.service.indexer.identity;

import java.io.IOException;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.AbstractIndexer;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * <h3>Description:</h3>
 * <p>
 * The identity indexer indexes public information about a user such as the
 * profile or the users public folder
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class IdentityIndexer extends AbstractIndexer {
	private static final OLog log = Tracing.createLoggerFor(IdentityIndexer.class);
	public final static String TYPE = "type.identity";
	private List<Indexer> indexerList;


	/**
	 * @see org.olat.search.service.indexer.Indexer#getSupportedTypeName()
	 */
	public String getSupportedTypeName() {
		return Identity.class.getSimpleName();	
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */

	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,
			InterruptedException {
		
  	int counter = 0;
  	BaseSecurity secMgr = BaseSecurityManager.getInstance();
  	List<Identity> identities = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_ACTIV);
  	if (log.isDebug()) log.debug("Found " + identities.size() + " active identities to index");

  	// committing here to make sure the loadBusinessGroup below does actually
  	// reload from the database and not only use the session cache 
  	// (see org.hibernate.Session.get(): 
  	//  If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
  	DBFactory.getInstance().commitAndCloseSession();
  	
  	for (Identity identity : identities) {
  		try {
				// reload the businessGroup here before indexing it to make sure it has not been deleted in the meantime
  			Identity reloadedIdentity = secMgr.findIdentityByName(identity.getName());
				if (reloadedIdentity==null || (reloadedIdentity.getStatus()>=Identity.STATUS_VISIBLE_LIMIT)) {
					log.info("doIndex: identity was deleted while we were indexing. The deleted identity was: "+identity);
					continue;
				}
				identity = reloadedIdentity;

  			if (log.isDebug()) log.debug("Indexing identity::" + identity.getName() + " and counter::" + counter);  	  	
  	  	// Create a search context for this identity. The search context will open the users visiting card in a new tab
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey()));
				searchResourceContext.setParentContextType(TYPE);

				// delegate indexing work to all configured indexers
				for (Indexer indexer : indexerList) {
					indexer.doIndex(searchResourceContext, identity, indexWriter);
				}
				
  			counter++;
			} catch (Exception ex) {
				log.warn("Exception while indexing identity::" + identity.getName() + ". Skipping this user, try next one.", ex);
				DBFactory.getInstance(false).rollbackAndCloseSession();
			}
		}
  	if (log.isDebug()) log.debug("IdentityIndexer finished with counter::" + counter);
  	
  	
	}

	/**
	 * Bean setter method used by spring.
	 * 
	 * @param indexerList
	 */
	public void setIndexerList(List<Indexer> indexerList) {
		this.indexerList = indexerList;
		super.setIndexerList(indexerList);
	}

	/**
	 * @see org.olat.search.service.indexer.Indexer#checkAccess(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.id.context.BusinessControl, org.olat.core.id.Identity,
	 *      org.olat.core.id.Roles)
	 */
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}
}
