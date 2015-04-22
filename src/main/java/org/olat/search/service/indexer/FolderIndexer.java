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

package org.olat.search.service.indexer;

import java.io.IOException;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.search.service.SearchResourceContext;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * @author Christian Guretzki
 */
public abstract class FolderIndexer extends AbstractHierarchicalIndexer {

	protected void doIndexVFSContainer(SearchResourceContext parentResourceContext, VFSContainer container, OlatFullIndexer indexWriter, String filePath, FolderIndexerAccess accessRule)
	throws IOException, InterruptedException {
		FolderIndexerWorker runnableFolderIndexer = new  FolderIndexerWorker();
		runnableFolderIndexer.setAccessRule(accessRule);
		runnableFolderIndexer.setParentResourceContext(parentResourceContext);
		runnableFolderIndexer.setContainer(container);
		runnableFolderIndexer.setIndexWriter(indexWriter);
		runnableFolderIndexer.setFilePath(filePath);
		indexWriter.submit(runnableFolderIndexer);
	}
	
}