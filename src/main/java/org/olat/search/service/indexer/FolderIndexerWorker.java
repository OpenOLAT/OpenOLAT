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
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.DocumentException;
import org.olat.search.service.document.file.DocumentNotImplementedException;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * Common folder indexer. Index all files form a certain VFS-container as starting point.
 * @author Christian Guretzki
 */
public class FolderIndexerWorker implements Runnable{
	
	private static OLog log = Tracing.createLoggerFor(FolderIndexerWorker.class);

	public static final int STATE_RUNNING = 1;
	public static final int STATE_FINISHED = 2;

	private Thread folderIndexer = null;
	
	private SearchResourceContext parentResourceContext;
	private VFSContainer container;
	private OlatFullIndexer indexWriter;
	private String filePath;
	private FolderIndexerAccess accessRule;

	private String threadId;
	
	private int state = 0;

	public FolderIndexerWorker(int threadId) {
		this.threadId = Integer.toString(threadId);
	}

	public void start() {
	  folderIndexer = new Thread(this, "folderIndexer-" + threadId );
	  folderIndexer.setPriority(Thread.MIN_PRIORITY);
	  folderIndexer.setDaemon(true);
	  folderIndexer.start();
	  state = STATE_RUNNING;
	}
	
	public void run() {
		try {
			if (log.isDebug()) log.debug("folderIndexer-" + threadId + " run...");			
			doIndexVFSContainer(parentResourceContext, container, indexWriter, filePath, accessRule);
			if (log.isDebug()) log.debug("folderIndexer-" + threadId + " finished");
		} catch (IOException e) {
			log.warn("IOException in run", e);
		} catch (InterruptedException e) {
			// Can happen if indexing is interrupted
			if (log.isDebug()) log.debug("InterruptedException in run");
		} catch (Exception e) {
			log.warn("Exception in run", e);
		} finally {
			//db session a saved in a thread local
			DBFactory.getInstance().commitAndCloseSession();
			FolderIndexerWorkerPool.getInstance().release(this);
		}
		log.debug("folderIndexer-" + threadId + " end of run");
		state = STATE_FINISHED;
	}
	
	protected void doIndexVFSContainer(SearchResourceContext resourceContext, VFSContainer cont, OlatFullIndexer writer, String fPath, FolderIndexerAccess aRule)
	throws IOException, InterruptedException {
		// Items: List of VFSContainer & VFSLeaf
		String myFilePath = fPath;
		for (VFSItem item : cont.getItems()) {
			if (item instanceof VFSContainer) {
				// ok it is a container go further
				if (log.isDebug()) log.debug(item.getName() + " is a VFSContainer => go further ");
				if(aRule.allowed(item)) {
					doIndexVFSContainer(resourceContext, (VFSContainer)item, writer, myFilePath + "/" + ((VFSContainer)item).getName(), aRule);
				}
			} else if (item instanceof VFSLeaf) {
				// ok it is a file => analyse it
				if (log.isDebug()) log.debug(item.getName() + " is a VFSLeaf => analyse file");
				if(aRule.allowed(item)) {
					doIndexVFSLeaf(resourceContext, (VFSLeaf)item, writer, myFilePath);
				}
			} else {
				log.warn("Unkown element in item-list class=" + item.getClass());
			}
		}
	}

	protected void doIndexVFSLeaf(SearchResourceContext leafResourceContext, VFSLeaf leaf, OlatFullIndexer writer, String fPath) {
		if (log.isDebug()) log.debug("Analyse VFSLeaf=" + leaf.getName());
		try {
			if (SearchServiceFactory.getFileDocumentFactory().isFileSupported(leaf)) {
				String myFilePath = fPath + "/" + leaf.getName();
				leafResourceContext.setFilePath(myFilePath);
				//fxdiff FXOLAT-97: high CPU load tracker
				WorkThreadInformations.setInfoFiles(myFilePath, leaf);
				WorkThreadInformations.set("Index VFSLeaf=" + myFilePath + " at " + leafResourceContext.getResourceUrl());
  			Document document = FileDocumentFactory.createDocument(leafResourceContext, leaf);
  			if(document != null) {//document wihich are disabled return null
  				writer.addDocument(document);
  			}
			} else {
				if (log.isDebug()) log.debug("Documenttype not supported. file=" + leaf.getName());
			}
		} catch (DocumentAccessException e) {
			if (log.isDebug()) log.debug("Can not access document." + e.getMessage());
		} catch (DocumentNotImplementedException e) {
			if (log.isDebug()) log.debug("Documenttype not implemented." + e.getMessage());
		} catch (InterruptedException e) {
			if (log.isDebug()) log.debug("InterruptedException: Can not index leaf=" + leaf.getName() + ";" + e.getMessage());
		}catch (DocumentException dex) {
			log.debug("DocumentException: Can not index leaf=" + leaf.getName() + " , exception=" + dex);
		} catch (IOException ioEx) {
			log.warn("IOException: Can not index leaf=" + leaf.getName(), ioEx);
		} catch (Exception ex) {
			log.warn("Exception: Can not index leaf=" + leaf.getName(), ex);
		//fxdiff FXOLAT-97: high CPU load tracker
		} finally {
			WorkThreadInformations.unset();
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

	public String getId() {
		return threadId;
	}

	/**
	 * @return Returns the state.
	 */
	public int getState() {
		if ((folderIndexer != null) && folderIndexer.isAlive()) {
			return STATE_RUNNING;
		}
		return state;
	}
}
