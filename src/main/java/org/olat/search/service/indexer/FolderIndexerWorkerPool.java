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


import java.util.List;
import java.util.Vector;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceImpl;

/**
 * Pool of folder indexer worker. 
 * @author Christian Guretzki
 */
public class FolderIndexerWorkerPool {
  private OLog log = Tracing.createLoggerFor(FolderIndexerWorkerPool.class);
  private int poolSize = 10;
  private static FolderIndexerWorkerPool instance = null;
  private List<FolderIndexerWorker> indexerList;
  private List<FolderIndexerWorker> runningIndexerList;

	/** 
	 * 
	 */
  public FolderIndexerWorkerPool() {
  	poolSize = SearchServiceFactory.getService().getSearchModuleConfig().getFolderPoolSize();
  	indexerList = new Vector<FolderIndexerWorker>();
  	// 
  	for (int i = 0; i < poolSize; i++) {
  		indexerList.add(new FolderIndexerWorker(i));	
		}
  	runningIndexerList = new Vector<FolderIndexerWorker>(); 	
  }
    
	protected static FolderIndexerWorkerPool getInstance() {
		if (instance == null) {
			instance = new FolderIndexerWorkerPool();
		}
		return instance;
	}

	public void release(FolderIndexerWorker indexer) {
		if (log.isDebug()) log.debug("indexer=" + indexer.getId() + " released");
    runningIndexerList.remove(indexer);
		int id = Integer.parseInt(indexer.getId());
		indexerList.add(new FolderIndexerWorker(id));
		if (log.isDebug()) log.debug("Available indexer=" + indexerList.size() + " Running indexer=" + runningIndexerList.size());
	}

  public FolderIndexerWorker getIndexer( ) {
		while(indexerList.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
		}
		FolderIndexerWorker freeFolderIndexer = indexerList.remove(0);
		runningIndexerList.add(freeFolderIndexer);
		if (log.isDebug()) log.debug("indexer=" + freeFolderIndexer.getId() + " selected");
		if (log.isDebug()) log.debug("Available indexer=" + indexerList.size() + " Running indexer=" + runningIndexerList.size());
		return freeFolderIndexer;
	}
	
	/**
	 * 
	 * @return  Return true if any indexer is still running
	 */
	public boolean isIndexerRunning() {
		return indexerList.size() < this.poolSize;
	}
	
	public int getNumberOfRunningIndexer() {
		return runningIndexerList.size();
	}
	
	public int getNumberOfAvailableIndexer() {
		return indexerList.size();
	}
	
	public boolean isDisabled() {
		return poolSize == 0;
	}
	
}
