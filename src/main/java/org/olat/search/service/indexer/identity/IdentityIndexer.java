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
package org.olat.search.service.indexer.identity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityPowerSearchQueries;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
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
public class IdentityIndexer extends AbstractHierarchicalIndexer {
	private static final Logger log = Tracing.createLoggerFor(IdentityIndexer.class);
	
	public static final String TYPE = "type.identity";

	@Override
	public String getSupportedTypeName() {
		return Identity.class.getSimpleName();	
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
		
		int counter = 0;
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		List<Long> identityKeys = secMgr.loadVisibleIdentityKeys();
		if (log.isDebugEnabled()) log.debug("Found {} active identities to index", identityKeys.size());
		DBFactory.getInstance().commitAndCloseSession();
  	
		for (Long identityKey : identityKeys) {
			if(indexWriter.isInterupted()) {
				DBFactory.getInstance().commitAndCloseSession();
				log.info("Identity indexer interrupted");
				return;
			}
			
			try {
				// reload the identity here before indexing it to make sure it has not been deleted in the meantime
				Identity identity = secMgr.loadIdentityByKey(identityKey);
				if (identity == null || (identity.getStatus().intValue() >=Identity.STATUS_VISIBLE_LIMIT.intValue())) {
					log.info("doIndex: identity was deleted while we were indexing. The deleted identity was: {}", identity);
					continue;
				}

				if (log.isDebugEnabled()) log.debug("Indexing identity::{} and counter::{}", identity.getKey(), counter);  	  	
				// Create a search context for this identity. The search context will open the users visiting card in a new tab
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey()));
				searchResourceContext.setParentContextType(TYPE);

				// delegate indexing work to all configured indexers
				for (Indexer indexer : getChildIndexers()) {
					indexer.doIndex(searchResourceContext, identity, indexWriter);
				}
				
				counter++;
			} catch (Exception ex) {
				log.warn("Exception while indexing identity::{}. Skipping this user, try next one.", identityKey, ex);
				DBFactory.getInstance().rollbackAndCloseSession();
			}
			DBFactory.getInstance().commitAndCloseSession();
		}
		if (log.isDebugEnabled()) log.debug("IdentityIndexer finished with counter::{}", counter);
	}
	
	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(roles.isGuestOnly()) {
			return false;
		}
		
		Long identityKey = contextEntry.getOLATResourceable().getResourceableId();
		List<OrganisationRef> organisations = roles.getOrganisationsWithRoles(OrganisationRoles.valuesWithoutGuestAndInvitee());
		SearchIdentityParams params = new SearchIdentityParams(null, null, false, null, null,
				null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		params.setOrganisations(organisations);
		params.setIdentityKeys(Collections.singletonList(identityKey));
		List<Identity> ids = CoreSpringFactory.getImpl(IdentityPowerSearchQueries.class).getIdentitiesByPowerSearch(params, 0, 1);
		return !ids.isEmpty();
	}
}
