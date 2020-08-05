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
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * @author Christian Guretzki
 */
public class FolderIndexerWorker implements Callable<Boolean> {
	
	private static final Logger log = Tracing.createLoggerFor(FolderIndexerWorker.class);

	private SearchResourceContext parentResourceContext;
	private VFSContainer container;
	private OlatFullIndexer indexWriter;
	private String filePath;
	private FolderIndexerAccess accessRule;

	private final FileDocumentFactory docFactory;

	public FolderIndexerWorker() {
		docFactory = CoreSpringFactory.getImpl(FileDocumentFactory.class);
	}

	@Override
	public Boolean call() throws Exception {
		boolean allOk = false;
		try {			
			doIndexVFSContainer(parentResourceContext, container, indexWriter, filePath, accessRule);
			allOk = true;
		} catch (IOException e) {
			log.warn("IOException in run", e);
		} catch (InterruptedException e) {
			// Can happen if indexing is interrupted
			if (log.isDebugEnabled()) log.debug("InterruptedException in run");
		} catch (Exception e) {
			log.warn("Exception in run", e);
		} finally {
			//db session a saved in a thread local
			DBFactory.getInstance().commitAndCloseSession();
		}
		return allOk;
	}
	
	protected void doIndexVFSContainer(SearchResourceContext resourceContext, VFSContainer cont, OlatFullIndexer writer, String fPath, FolderIndexerAccess aRule)
	throws IOException, InterruptedException {
		// Items: List of VFSContainer & VFSLeaf
		String myFilePath = fPath;
		boolean debug = log.isDebugEnabled();
		for (VFSItem item : cont.getItems(new VFSSystemItemFilter())) {
			if (item instanceof VFSContainer) {
				// ok it is a container go further
				if (debug) log.debug("{} is a VFSContainer => go further ", item.getName());
				if(aRule.allowed(item)) {
					doIndexVFSContainer(resourceContext, (VFSContainer)item, writer, myFilePath + "/" + ((VFSContainer)item).getName(), aRule);
				}
			} else if (item instanceof VFSLeaf) {
				// ok it is a file => analyse it
				if (debug) log.debug("{} is a VFSLeaf => analyse file", item.getName() );
				if(aRule.allowed(item)) {
					doIndexVFSLeaf(resourceContext, (VFSLeaf)item, writer, myFilePath);
				}
			} else {
				log.warn("Unkown element in item-list class={}", item.getClass());
			}
		}
	}

	protected void doIndexVFSLeaf(SearchResourceContext leafResourceContext, VFSLeaf leaf, OlatFullIndexer writer, String fPath) {
		if (log.isDebugEnabled()) log.debug("Analyse VFSLeaf={}", leaf.getName());
		try {
			if (docFactory.isFileSupported(leaf)) {
				String myFilePath = fPath + "/" + leaf.getName();
				leafResourceContext.setFilePath(myFilePath);
				Document document = docFactory.createDocument(leafResourceContext, leaf);
				if(document != null) {//document which are disabled return null
					writer.addDocument(document);
				}
			} else {
				if (log.isDebugEnabled()) log.debug("Documenttype not supported. file={}", leaf.getName());
			}
		} catch (DocumentAccessException e) {
			if (log.isDebugEnabled()) log.debug("Can not access document.", e);
		} catch (InterruptedException e) {
			if (log.isDebugEnabled()) log.debug("InterruptedException: Can not index leaf={}", leaf.getName(), e);
		} catch (IOException ioEx) {
			log.warn("IOException: Can not index leaf={}", leaf.getName(), ioEx);
		} catch (Exception ex) {
			log.warn("Exception: Can not index leaf={}", leaf.getName(), ex);
		}
	}

	public void setParentResourceContext(SearchResourceContext newParentResourceContext) {
		this.parentResourceContext = newParentResourceContext;
	}

	public void setContainer(VFSContainer newContainer) {
		this.container = newContainer;
	}

	public void setIndexWriter(OlatFullIndexer newIndexWriter) {
		this.indexWriter = newIndexWriter;
	}

	public void setFilePath(String newFilePath) {
		this.filePath = newFilePath;
	}

	public void setAccessRule(FolderIndexerAccess accessRule) {
		this.accessRule = accessRule;
	}
}
