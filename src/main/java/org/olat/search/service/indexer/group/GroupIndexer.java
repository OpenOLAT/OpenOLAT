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
import java.util.List;

import org.apache.logging.log4j.Logger;
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
import org.olat.group.BusinessGroupService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.GroupDocument;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index all business-groups. Includes group-forums and groups-folders. 
 * @author Christian Guretzki
 */
public class GroupIndexer extends AbstractHierarchicalIndexer {
	
	private static final Logger log = Tracing.createLoggerFor(GroupIndexer.class);
	
	private BusinessGroupService businessGroupService;
	
	/**
	 * [used by Spring]
	 * @param businessGroupService
	 */
	public void setBusinessGroupService(BusinessGroupService businessGroupService) {
		this.businessGroupService = businessGroupService;
	}
	
	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		long startTime = System.currentTimeMillis();

		List<BusinessGroup> groupList = businessGroupService.loadAllBusinessGroups();
		if (log.isDebugEnabled()) log.debug("GroupIndexer groupList.size={}", groupList.size());
  	
		// committing here to make sure the loadBusinessGroup below does actually
		// reload from the database and not only use the session cache 
		// (see org.hibernate.Session.get(): 
		//  If the instance, or a proxy for the instance, is already associated with the session, return that instance or proxy.)
		DBFactory.getInstance().commitAndCloseSession();

		// loop over all groups
		for(BusinessGroup businessGroup:groupList) {
			if(indexWriter.isInterupted()) {
				DBFactory.getInstance().commitAndCloseSession();
				log.info("Groups indexer interrupted");
				return;
			}
			try {
				// reload the businessGroup here before indexing it to make sure it has not been deleted in the meantime
				BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(businessGroup);
				if (reloadedBusinessGroup==null) {
					log.info("doIndex: businessGroup was deleted while we were indexing. The deleted businessGroup was: {}", businessGroup);
					continue;
				}
				businessGroup = reloadedBusinessGroup;
				
				if (log.isDebugEnabled()) log.debug("Index BusinessGroup={}", businessGroup);
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(businessGroup);
				Document document = GroupDocument.createDocument(searchResourceContext, businessGroup);
				indexWriter.addDocument(document);
		    // Do index child 
			  super.doIndex(searchResourceContext, businessGroup, indexWriter);
			} catch(Exception ex) {
				log.error("Exception indexing group={}", businessGroup, ex);
				DBFactory.getInstance().rollbackAndCloseSession();
			} catch (Error err) {
				log.error("Error indexing group={}", businessGroup, err);
				DBFactory.getInstance().rollbackAndCloseSession();
			}
			DBFactory.getInstance().commitAndCloseSession();
	  }
		long indexTime = System.currentTimeMillis() - startTime;
		if (log.isDebugEnabled()) log.debug("GroupIndexer finished in {} ms", indexTime);
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(roles.isGuestOnly()) {
			return false;
		}
		
		Long key = contextEntry.getOLATResourceable().getResourceableId();
		BusinessGroup group = businessGroupService.loadBusinessGroup(key);
		if(group == null || roles.isGuestOnly()) {
			return false;
		}
		boolean inGroup = businessGroupService.isIdentityInBusinessGroup(identity, group);
		if (inGroup) {
			return super.checkAccess(contextEntry, businessControl, identity, roles)
					&& super.checkAccess(businessControl, identity, roles);
		} else {
			// get the list of next context entries
			List<ContextEntry> entries = businessControl.getEntriesDownTheControls();
			entries.remove(contextEntry);
			// only show the name and description of the group, not it's content, forum or folders
			if(entries.isEmpty()) {
				AccessControlModule acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
				if(acModule.isEnabled()) {
					ACService acService = CoreSpringFactory.getImpl(ACService.class);
					OLATResource resource = group.getResource();
					if(acService.isResourceAccessControled(resource, new Date())) {
						return super.checkAccess(contextEntry, businessControl, identity, roles)
								&& super.checkAccess(businessControl, identity, roles);
					}
				}
			}
			return false;
		}
	}

	@Override
	public String getSupportedTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}
}
