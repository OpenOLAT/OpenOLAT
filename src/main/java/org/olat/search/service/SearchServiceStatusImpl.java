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

package org.olat.search.service;

import java.util.Date;

import org.olat.search.SearchService;
import org.olat.search.SearchServiceStatus;
import org.olat.search.service.indexer.FullIndexerStatus;
import org.olat.search.service.indexer.Index;
import org.olat.search.service.indexer.LifeFullIndexer;

/**
 * Collection of search-service states. Includes state of full-index, index and search.
 * 
 * @author Christian Guretzki
 */
public class SearchServiceStatusImpl implements SearchServiceStatus {
	
	private final FullIndexerStatus fullIndexerStatus;
	private final FullIndexerStatus lifeIndexerStatus;
	
	private boolean indexExists; 
	private long queryCount;
	

	/**
	 * Creates a new search-service status.
	 * The values will be read in constructur. 
	 * To get a status for an other moment, you must create a new SearchServiceStatus.
	 * @param indexer  Reference to index-component.
	 * @param search   Reference to search-component.
	 */
	public SearchServiceStatusImpl(Index indexer, LifeFullIndexer lifeIndexer, SearchService search) {
		fullIndexerStatus = indexer.getFullIndexStatus();
		lifeIndexerStatus = lifeIndexer.getStatus();
		indexExists = indexer.existIndex();
		queryCount = search.getQueryCount();
	}

	@Override
	public String getStatus() {
		String fullStatus = fullIndexerStatus.getStatus();
		String lifeStatus = lifeIndexerStatus.getStatus();
		
		String status = FullIndexerStatus.STATUS_STOPPED;
		if(FullIndexerStatus.STATUS_RUNNING.equals(fullStatus) || FullIndexerStatus.STATUS_RUNNING.equals(lifeStatus)) {
			status = FullIndexerStatus.STATUS_RUNNING;
		} else if(FullIndexerStatus.STATUS_FINISHED.equals(fullStatus) || FullIndexerStatus.STATUS_FINISHED.equals(lifeStatus)) {
			status = FullIndexerStatus.STATUS_FINISHED;
		}
		return status;
	}

	/**
	 * @return  Status of full-indexer.
	 */
	public FullIndexerStatus getFullIndexerStatus() {
		return fullIndexerStatus;
	}
	
	/**
	 * @return  Status of life-indexer.
	 */
	public FullIndexerStatus getLifeIndexerStatus() {
		return lifeIndexerStatus;
	}

	@Override
	public Date getLastFullIndexTime() {
		long fullTime = fullIndexerStatus.getLastFullIndexTime();
		long lifeTime = lifeIndexerStatus.getLastFullIndexTime();
		return (lifeTime > fullTime) ? new Date(lifeTime) : new Date(fullTime);
	}
	
	/**
	 * @return  TRUE when an search index exists.
	 */
	public boolean getIndexExists() {
		return indexExists;
	}

	/**
	 * @return  Number of handled search queries since last restart. 
	 */
	public long getQueryCount() {
		return queryCount;
	}
}
