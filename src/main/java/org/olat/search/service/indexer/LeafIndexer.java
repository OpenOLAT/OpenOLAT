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

import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * @author Christian Guretzki
 */
public abstract class LeafIndexer extends AbstractHierarchicalIndexer {

	private static final Logger log = Tracing.createLoggerFor(LeafIndexer.class);

	protected void doIndexVFSLeafByMySelf(SearchResourceContext leafResourceContext, VFSLeaf leaf, OlatFullIndexer indexWriter, String filePath) throws InterruptedException {
		if (log.isDebugEnabled()) log.debug("Analyse VFSLeaf=" + leaf.getName());
		try {
			FileDocumentFactory documentFactory = CoreSpringFactory.getImpl(FileDocumentFactory.class);
			if (documentFactory.isFileSupported(leaf)) {
				String myFilePath = "";
				if (filePath.endsWith("/")) {
					myFilePath = filePath + leaf.getName();
				} else {
					myFilePath = filePath + "/" + leaf.getName();
				}
				leafResourceContext.setFilePath(myFilePath);

				Document document = documentFactory.createDocument(leafResourceContext, leaf);
				indexWriter.addDocument(document);
			} else {
				if (log.isDebugEnabled()) log.debug("Documenttype not supported. file=" + leaf.getName());
			}
		} catch (DocumentAccessException e) {
			if (log.isDebugEnabled()) log.debug("Can not access document." + e.getMessage());
		} catch (IOException ioEx) {
			log.warn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
		} catch (InterruptedException iex) {
			throw new InterruptedException(iex.getMessage());
		} catch (Exception ex) {
			log.warn("Exception: Can not index leaf=" + leaf.getName(), ex);
		}
	}
	
	/**
	 * @param leaf
	 * @return Full file-path of leaf without leaf-name
	 */
	protected String getPathFor(VFSLeaf leaf) {
		String path = "";
		VFSContainer parentContainer = leaf.getParentContainer();
		while (parentContainer.getParentContainer() != null) {
			String name = parentContainer.getName();
			if (parentContainer instanceof LocalFolderImpl && name.equals("coursefolder")) {
				// don't add the coursefolder to the path, the path is relative to the course folder
				break;
			}
			path = name + "/" + path;
			parentContainer = parentContainer.getParentContainer();
		}
		return path;
	}
}