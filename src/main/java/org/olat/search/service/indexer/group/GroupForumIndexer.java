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

import org.olat.collaboration.CollaborationTools;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.GroupDocument;
import org.olat.search.service.indexer.ForumIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index all group forums.
 * @author Christian Guretzki
 */
public class GroupForumIndexer extends ForumIndexer{

	private static final Logger log = Tracing.createLoggerFor(GroupForumIndexer.class);

	//Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.group.forum.message";

	/**
	 * Index a forum in a group.
	 * @param parentResourceContext
	 * @param businessGroup
	 * @param indexWriter
	 * @throws IOException
	 */
	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object businessObj, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		if (!(businessObj instanceof BusinessGroup) )
			throw new AssertException("businessObj must be BusinessGroup");
		BusinessGroup businessGroup = (BusinessGroup)businessObj;
		
		NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(businessGroup);
		ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);

		Property forumKeyProperty = npm.findProperty(null, null, CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_FORUM);
		// Check if forum-property exist
		if (forumKeyProperty != null) {
		  Long forumKey = forumKeyProperty.getLongValue();
		  Forum forum = fom.loadForum(forumKey);
		  SearchResourceContext forumSearchResourceContext = new SearchResourceContext(parentResourceContext);
		  forumSearchResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLFORUM);
		  forumSearchResourceContext.setDocumentType(TYPE);
			forumSearchResourceContext.setParentContextType(GroupDocument.TYPE);
			forumSearchResourceContext.setParentContextName(businessGroup.getName());
			if (forum == null) { // fxdiff: FXOLAT-104 warn about missing forums
				log.error("found a forum-key " + forumKey + " for businessgroup " + businessGroup.getName() + " [" + businessGroup.getKey() + "] to index a forum that could not be " +
						"found by key! skip indexing, check if forum should still be enabled. context: " + forumSearchResourceContext.getResourceUrl());
				return;
			}
		  doIndexAllMessages(forumSearchResourceContext, forum, indexWriter );
		}
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		ContextEntry ce = businessControl.popLauncherContextEntry();
		Long resourceableId = ce.getOLATResourceable().getResourceableId();
		Message message = CoreSpringFactory.getImpl(ForumManager.class).loadMessage(resourceableId);
		if(message == null)  return false;

		Message threadtop = message.getThreadtop();
		if(threadtop==null) {
			threadtop = message;
		}
		boolean isMessageHidden = Status.getStatus(threadtop.getStatusCode()).isHidden(); 
		//assumes that if is owner then is moderator so it is allowed to see the hidden forum threads
		//here it is checked if the identity is owner of the forum tool but it has no way to find out whether is owner of the group that owns the forum tool
		if(isMessageHidden) {
			return false;
		}		
		return super.checkAccess(contextEntry, businessControl, identity, roles);
	}

	@Override
	public String getSupportedTypeName() {
		return BusinessGroupMainRunController.ORES_TOOLFORUM.getResourceableTypeName();
	}
}