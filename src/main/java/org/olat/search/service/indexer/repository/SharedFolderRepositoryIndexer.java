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
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index a repository entry of type shared folder.
 * @author Christian Guretzki
 */
public class SharedFolderRepositoryIndexer extends FolderIndexer implements Indexer {
	private static final OLog log = Tracing.createLoggerFor(SharedFolderRepositoryIndexer.class);
	// fxdiff: see LibraryManager
	private static final String NO_FOLDER_INDEXING_LOCKFILE = ".noFolderIndexing";
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.repository.entry.sharedfolder";

	public final static String ORES_TYPE_SHAREDFOLDER = SharedFolderFileResource.TYPE_NAME;
	
	public SharedFolderRepositoryIndexer() {
		// Repository types
		
	}
	
	/**
	 * 
	 */
	public String getSupportedTypeName() {	
		return ORES_TYPE_SHAREDFOLDER; 
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */

	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		if (log.isDebug()) log.debug("Analyse Shared Folder RepositoryEntry...");

		resourceContext.setDocumentType(TYPE);
    resourceContext.setParentContextType(TYPE);
		resourceContext.setParentContextName(repositoryEntry.getDisplayname());
    
		VFSContainer sfContainer = SharedFolderManager.getInstance().getSharedFolder(repositoryEntry.getOlatResource());
		// fxdiff: only index if no lockfile found. see OLAT-5724
		if (sfContainer.resolve(NO_FOLDER_INDEXING_LOCKFILE) == null){
			doIndexVFSContainer(resourceContext,sfContainer,indexWriter,"", FolderIndexerAccess.FULL_ACCESS);
		}
	}


	/**
	 * Bean setter method used by spring. 
	 * @param indexerList
	 */
	public void setIndexerList(List indexerList) {
	}

	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}

}
