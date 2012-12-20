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

package org.olat.core.commons.services.search;

import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;

/**
 * Interface to search service.
 * @author Christian Guretzki
 */
public interface SearchService {
  /**
   * 
   * @param query          Lucene query string
   * @param identity       Idenity of searching-user
   * @param roles          Roles of searching-user
   * @param doHighlighting Highlights founded text fragements in result  
   * @return               Search result for queury
   */
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, int firstResult, int maxReturns, boolean doHighlighting) throws ServiceNotAvailableException, ParseException, QueryException;
	
	/**
	 * Check a query for similar words.
	 * @param query
	 * @return List of similar words which exist in index. 
	 */
	public Set<String> spellCheck(String query) throws ServiceNotAvailableException;

	/**
	 * Start a new full index.
	 *
	 */
	public void startIndexing();

	/**
	 * Stop current full-indexing.
	 *
	 */
	public void stopIndexing();
	
	/**
	 * Initializes service.
	 */
	public void init();
	
	/**
	 * Return current state of search service, Includes full-indexing, index and search.
	 * @return
	 */
	public SearchServiceStatus getStatus();
	
	/**
	 * Get index-interval of running system
	 * @return indexInterval
	 */
	
	public long getIndexInterval();

	/**
	 * Change index-interval of running system 	immediately to reduce system load.
	 * Overwrites the config parameter 'indexInterval'. 
	 * The value will not be stored persistent and will be lost at next restart.
	 * @param indexInterval  New index-interval.
	 */
	public void setIndexInterval(long indexInterval);
	
	/**
	 * Stop search service.
	 */
	public void stop();
	
	/**
	 * access the module configuration
	 * @return
	 */
	public SearchModule getSearchModuleConfig();

	/**
	 * Return true when the search service is enabled
	 * @return
	 */
	public boolean isEnabled();

	public long getQueryCount();

}
