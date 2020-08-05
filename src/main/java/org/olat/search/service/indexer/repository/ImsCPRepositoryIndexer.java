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

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.FolderIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index a repository entry of type IMS-CP.
 * @author Christian Guretzki
 */
public class ImsCPRepositoryIndexer extends FolderIndexer {


	private static final Logger log = Tracing.createLoggerFor(ImsCPRepositoryIndexer.class);
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to search for certain document types and lucene has problems with '_' 
	public static final String TYPE = "type.repository.entry.imscp";

	public static final String ORES_TYPE_CP = ImsCPFileResource.TYPE_NAME;

	@Override
	public String getSupportedTypeName() {	
		return ORES_TYPE_CP; 
	}

	@Override
	public void doIndex(SearchResourceContext resourceContext, Object parentObject, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		if (log.isDebugEnabled()) log.debug("Analyse IMS CP RepositoryEntry...");
		resourceContext.setDocumentType(TYPE);
    
		if (repositoryEntry != null) {
			File cpRoot = FileResourceManager.getInstance().unzipFileResource(repositoryEntry.getOlatResource());
			if (cpRoot != null) {
				SearchResourceContext cpContext = new SearchResourceContext(resourceContext);
				VFSContainer rootContainer = new LocalFolderImpl(cpRoot);
				doIndexVFSContainer(cpContext, rootContainer, indexWriter, "", FolderIndexerAccess.FULL_ACCESS);
			}
		}
	}
}
