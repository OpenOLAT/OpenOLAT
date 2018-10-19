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

package org.olat.search;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.core.commons.persistence.SortKey;
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
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxReturns, boolean doHighlighting)
	throws ServiceNotAvailableException, ParseException, QueryException;
	
	/**
	 * There isn't any check access made on this search.
	 * 
	 * @param queryString
	 * @param condQueries
	 * @param identity
	 * @param roles
	 * @param firstResult
	 * @param maxReturns
	 * @param orderBy
	 * @return A list of dbKey's
	 * @throws ServiceNotAvailableException
	 * @throws ParseException
	 * @throws QueryException
	 */
	public List<Long> doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxReturns, SortKey... orderBy)
	throws ServiceNotAvailableException, ParseException, QueryException;
	
	/**
	 * Search a document with the specified resource URL.
	 * 
	 * @param resourceUrl
	 * @return The whole document with all fields, null if not found.
	 * @throws ServiceNotAvailableException
	 * @throws ParseException
	 * @throws QueryException
	 */
	public Document doSearch(String resourceUrl)
			throws ServiceNotAvailableException, ParseException, QueryException;

	
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
	
	public boolean refresh();
	
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
