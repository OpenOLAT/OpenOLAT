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

import org.olat.collaboration.CollaborationManager;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.GroupDocument;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index all group folders.
 * @author Christian Guretzki
 */
public class GroupFolderIndexer extends FolderIndexer{
	
  //Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.group.folder";
	
	private CollaborationManager collaborationManager;
	
	/**
	 * [used by Spring]
	 * @param collaborationManager
	 */
	public void setCollaborationManager(CollaborationManager collaborationManager) {
		this.collaborationManager = collaborationManager;
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object businessObj, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		if (!(businessObj instanceof BusinessGroup) )
			throw new AssertException("businessObj must be BusinessGroup");
		BusinessGroup businessGroup = (BusinessGroup)businessObj;
		String path = collaborationManager.getFolderRelPath(businessGroup);
		VFSContainer rootContainer = VFSManager.olatRootContainer(path, null);
		SearchResourceContext forumSearchResourceContext = new SearchResourceContext(parentResourceContext);
		forumSearchResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLFOLDER);
		forumSearchResourceContext.setDocumentType(TYPE);
		forumSearchResourceContext.setParentContextType(GroupDocument.TYPE);
		forumSearchResourceContext.setParentContextName(businessGroup.getName());
		doIndexVFSContainer(forumSearchResourceContext,rootContainer,indexWriter,"", FolderIndexerAccess.FULL_ACCESS);
	}

	public String getSupportedTypeName() {
		return BusinessGroupMainRunController.ORES_TOOLFOLDER.getResourceableTypeName();
	}
}