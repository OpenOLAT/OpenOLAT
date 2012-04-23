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

package org.olat.search.service.indexer.repository;


import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.search.SearchModule;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.StartupException;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index the whole OLAT-repository.
 * @author Christian Guretzki
 * 
 */
public class RepositoryIndexer extends DefaultIndexer {
	
  private RepositoryManager repositoryManager;
	private List<Long> repositoryBlackList;

	/**
	 * [used by spring]
	 * @param repositoryManager
	 */
  private RepositoryIndexer(RepositoryManager repositoryManager, SearchModule searchModule) {
  	this.repositoryManager = repositoryManager;
  	this.repositoryBlackList = searchModule.getRepositoryBlackList();
	}
	

  /**
   * Loops over all repository-entries. Index repository meta data. 
   * Go further with repository-indexer for certain type if available. 
   * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext, java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
   */
  public void doIndex(SearchResourceContext parentResourceContext, Object businessObj, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
  	Roles roles = new Roles(true, true, true, true, false, true, false);
  	int counter = 0;
  	//fxdiff VCRP-1,2: access control of resources
  	List<RepositoryEntry> repositoryList = repositoryManager.genericANDQueryWithRolesRestriction(null,null,null,null,null,roles, null);
  	if (isLogDebugEnabled()) logDebug("RepositoryIndexer repositoryList.size=" + repositoryList.size());
  	// loop over all repository-entries
		Iterator<RepositoryEntry> iter = repositoryList.iterator();
		RepositoryEntry repositoryEntry = null;

		// committing here to make sure the loadBusinessGroup below does actually
		// reload from the database and not only use the session cache 
		// (see org.hibernate.Session.get(): 
		//  If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
		DBFactory.getInstance().commitAndCloseSession();
		
		while(iter.hasNext()) {
			try {
				repositoryEntry = iter.next();
				
				// reload the repositoryEntry here before indexing it to make sure it has not been deleted in the meantime
				RepositoryEntry reloadedRepositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryEntry.getKey());
				if (reloadedRepositoryEntry==null) {
					logInfo("doIndex: repositoryEntry was deleted while we were indexing. The deleted repositoryEntry was: "+repositoryEntry);
					continue;
				}
				repositoryEntry = reloadedRepositoryEntry;

				if (isLogDebugEnabled()) logDebug("Index repositoryEntry=" + repositoryEntry + "  counter=" + counter++ + " with ResourceableId=" + repositoryEntry.getOlatResource().getResourceableId());
				if (!isOnBlacklist(repositoryEntry.getOlatResource().getResourceableId()) ) {
					SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
					searchResourceContext.setBusinessControlFor(repositoryEntry);
					Document document = RepositoryEntryDocument.createDocument(searchResourceContext, repositoryEntry);
					indexWriter.addDocument(document);
					// Pass created-date & modified-date in context to child indexer because the child have no dates
		      // TODO:chg: Check ob courseNode keine Daten hat 
					searchResourceContext.setLastModified(repositoryEntry.getLastModified());
					searchResourceContext.setCreatedDate(repositoryEntry.getCreationDate());
					// go further with resource
					Indexer repositoryEntryIndexer = RepositoryEntryIndexerFactory.getInstance().getRepositoryEntryIndexer(repositoryEntry);
					if (repositoryEntryIndexer != null) {
					  repositoryEntryIndexer.doIndex(searchResourceContext, repositoryEntry, indexWriter);
					} else {
						if (isLogDebugEnabled()) logDebug("No RepositoryEntryIndexer for " + repositoryEntry.getOlatResource()); // e.g. RepositoryEntry				
					}
				} else {
					logWarn("RepositoryEntry is on black-list and excluded from search-index, repositoryEntry=" + repositoryEntry, null);
				}
			} catch (Throwable ex) {
				// create meaninfull debugging output to find repo entry that is somehow broken
				String entryDebug = "NULL";
				if (repositoryEntry != null) {
					entryDebug = "resId::" + repositoryEntry.getResourceableId() + " resTypeName::" + repositoryEntry.getResourceableTypeName() + " resName::" + repositoryEntry.getResourcename();
				}
				logWarn("Exception=" + ex.getMessage() + " for repo entry " + entryDebug, ex);
				DBFactory.getInstance(false).rollbackAndCloseSession();
			}
		}
		if (isLogDebugEnabled()) logDebug("RepositoryIndexer finished.  counter=" + counter);
	}

	private boolean isOnBlacklist(Long key) {
		return repositoryBlackList.contains(key);
		
	}


	/**
	 * Bean setter method used by spring. 
	 * @param indexerList
	 */
	public void setIndexerList(List<Indexer> indexerList) {
		if (indexerList == null)
			throw new AssertException("null value for indexerList not allowed.");

		try {
			for (Iterator<Indexer> iter = indexerList.iterator(); iter.hasNext();) {
				Indexer reporsitoryEntryIndexer = iter.next();
				RepositoryEntryIndexerFactory.getInstance().registerIndexer(reporsitoryEntryIndexer);
				if (isLogDebugEnabled()) logDebug("Adding indexer from configuraton:: ");
			} 
		}	catch (ClassCastException cce) {
				throw new StartupException("Configured indexer is not of type RepositoryEntryIndexer", cce);
		}
	}

	/**
	 * 
	 * @see org.olat.search.service.indexer.Indexer#getSupportedTypeName()
	 */
	public String getSupportedTypeName() {
		return OresHelper.calculateTypeName(RepositoryEntry.class);
	}

	/**
	 * 
	 * @see org.olat.search.service.indexer.Indexer#checkAccess(org.olat.core.id.context.ContextEntry, org.olat.core.id.context.BusinessControl, org.olat.core.id.Identity, org.olat.core.id.Roles)
	 */
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if (isLogDebugEnabled()) logDebug("checkAccess for businessControl=" + businessControl + "  identity=" + identity + "  roles=" + roles);
		Long repositoryKey = contextEntry.getOLATResourceable().getResourceableId();
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryKey);
		if (repositoryEntry != null) {
			boolean isOwner = repositoryManager.isOwnerOfRepositoryEntry(identity,repositoryEntry);
			boolean isAllowedToLaunch = false;
			if (!isOwner) {
				if (repositoryEntry.getOwnerGroup() == null) {
					// FIXME:chg: Inconsistent RepositoryEntry without owner-group, should not exit => Workaround no access
					return false;
				}
  			isAllowedToLaunch = repositoryManager.isAllowedToLaunch(identity, roles, repositoryEntry);
			}
			if (isLogDebugEnabled()) logDebug("isOwner=" + isOwner + "  isAllowedToLaunch=" + isAllowedToLaunch);
  		if (isOwner || isAllowedToLaunch) {
				Indexer repositoryEntryIndexer = RepositoryEntryIndexerFactory.getInstance().getRepositoryEntryIndexer(repositoryEntry);
				if (isLogDebugEnabled()) logDebug("repositoryEntryIndexer=" + repositoryEntryIndexer);
				if (repositoryEntryIndexer != null) {
				  return super.checkAccess(contextEntry, businessControl, identity, roles)
				  		&& repositoryEntryIndexer.checkAccess(contextEntry, businessControl, identity, roles);
				} else {
					// No Indexer => no access
					return false;
				}
			} else {
	  		return false;
			}
		} else {
			logWarn("Can not found RepositoryEntry with key=" + repositoryKey, null);
			return false;
		}
	}

}
