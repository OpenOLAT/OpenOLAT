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

package org.olat.search.service.indexer.group;


import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.GroupDocument;
import org.olat.search.service.indexer.AbstractIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index all business-groups. Includes group-forums and groups-folders. 
 * @author Christian Guretzki
 */
public class GroupIndexer extends AbstractIndexer {
	
	private BusinessGroupManager businessGroupManager;

	public GroupIndexer() {
		businessGroupManager = BusinessGroupManagerImpl.getInstance();
		//-> OLAT-3367 OLATResourceable ores = OresHelper.lookupType(BusinessGroup.class);
		//-> OLAT-3367 CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, ores);
	}
	

  public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		long startTime = System.currentTimeMillis();
  	List groupList = businessGroupManager.getAllBusinessGroups();
  	if (Tracing.isDebugEnabled(GroupIndexer.class)) Tracing.logDebug("GroupIndexer groupList.size=" + groupList.size(), GroupIndexer.class);
  	
		// committing here to make sure the loadBusinessGroup below does actually
		// reload from the database and not only use the session cache 
		// (see org.hibernate.Session.get(): 
		//  If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
		DBFactory.getInstance().commitAndCloseSession();

		// loop over all groups
		Iterator iter = groupList.iterator();
		while(iter.hasNext()) {
			BusinessGroup businessGroup = null;
			try {
				businessGroup = (BusinessGroup)iter.next();
				
				// reload the businessGroup here before indexing it to make sure it has not been deleted in the meantime
				BusinessGroup reloadedBusinessGroup = businessGroupManager.loadBusinessGroup(businessGroup.getKey(), false);
				if (reloadedBusinessGroup==null) {
					Tracing.logInfo("doIndex: businessGroup was deleted while we were indexing. The deleted businessGroup was: "+businessGroup, GroupIndexer.class);
					continue;
				}
				businessGroup = reloadedBusinessGroup;
				
				if (Tracing.isDebugEnabled(GroupIndexer.class)) Tracing.logDebug("Index BusinessGroup=" + businessGroup , GroupIndexer.class);
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(businessGroup);
			  Document document = GroupDocument.createDocument(searchResourceContext, businessGroup);
			  indexWriter.addDocument(document);
		    // Do index child 
			  super.doIndex(searchResourceContext, businessGroup, indexWriter);
			} catch(Exception ex) {
				Tracing.logError("Exception indexing group=" + businessGroup, ex , GroupIndexer.class);
				DBFactory.getInstance(false).rollbackAndCloseSession();
			} catch (Error err) {
				Tracing.logError("Error indexing group=" + businessGroup, err , GroupIndexer.class);
				DBFactory.getInstance(false).rollbackAndCloseSession();
			}
	  }
		long indexTime = System.currentTimeMillis() - startTime;
		if (Tracing.isDebugEnabled(GroupIndexer.class)) Tracing.logDebug("GroupIndexer finished in " + indexTime + " ms", GroupIndexer.class);
	}


	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		Long key = contextEntry.getOLATResourceable().getResourceableId();
		BusinessGroupManager bman = BusinessGroupManagerImpl.getInstance();
		BusinessGroup group = bman.loadBusinessGroup(key, false);
		boolean inGroup = bman.isIdentityInBusinessGroup(identity, group);
		if (inGroup) {
			return super.checkAccess(businessControl, identity, roles);
		} else {
			AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
			if(acModule.isEnabled()) {
				ACFrontendManager acFrontendManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
				OLATResource resource = OLATResourceManager.getInstance().findResourceable(group);
				if(acFrontendManager.isResourceAccessControled(resource, new Date())) {
					return super.checkAccess(businessControl, identity, roles);
				}
			}
			return false;
		}
	}


	public String getSupportedTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}
}
