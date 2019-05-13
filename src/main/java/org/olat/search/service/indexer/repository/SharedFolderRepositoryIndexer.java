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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index a repository entry of type shared folder.
 * @author Christian Guretzki
 */
public class SharedFolderRepositoryIndexer extends FolderIndexer {

	private static final Logger log = Tracing.createLoggerFor(SharedFolderRepositoryIndexer.class);

	private static final String NO_FOLDER_INDEXING_LOCKFILE = ".noFolderIndexing";
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.repository.entry.sharedfolder";

	public static final String ORES_TYPE_SHAREDFOLDER = SharedFolderFileResource.TYPE_NAME;

	@Override
	public String getSupportedTypeName() {	
		return ORES_TYPE_SHAREDFOLDER; 
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	@Override
	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		if (log.isDebugEnabled()) log.debug("Analyse Shared Folder RepositoryEntry...");

		resourceContext.setDocumentType(TYPE);

		VFSContainer sfContainer = SharedFolderManager.getInstance().getSharedFolder(repositoryEntry.getOlatResource());
		//only index if no lockfile found. see OLAT-5724
		if (sfContainer != null && sfContainer.resolve(NO_FOLDER_INDEXING_LOCKFILE) == null){
			SearchResourceContext folderContext = new SearchResourceContext(resourceContext);
			doIndexVFSContainer(folderContext,sfContainer,indexWriter,"", FolderIndexerAccess.FULL_ACCESS);
		}
	}
}
