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

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.search.QueryException;
import org.olat.search.SearchModule;
import org.olat.search.SearchResults;
import org.olat.search.SearchService;
import org.olat.search.SearchServiceStatus;
import org.olat.search.ServiceNotAvailableException;

/**
 * 
 * @author Christian Guretzki
 */
public class SearchServiceDisabled implements SearchService {
	private static final Logger log = Tracing.createLoggerFor(SearchServiceDisabled.class);
	
	/**
	 * [used by spring]
	 */
	private SearchServiceDisabled() {
		log.info("SearchService Disabled");
	}

	@Override
	public void startIndexing() {
		//
	}

	@Override
	public void stopIndexing() {
		//
	}
	
	@Override
	public boolean refresh() {
		return false;
	}

	@Override
	public void init() {
		//
	}

	@Override
	public SearchServiceStatus getStatus() {
		return null;
	}

	@Override
	public void setIndexInterval(long indexInterval) {
	}

	@Override
	public long getIndexInterval() {
		return 0;
	}
	
	/**
	 * 
	 * @return  Resturn search module configuration.
	 */
	@Override
	public SearchModule getSearchModuleConfig() {
		return null;
	}

	@Override
	public Set<String> spellCheck(String query) throws ServiceNotAvailableException {		
		log.error("call spellCheck on disabled search service");
		throw new ServiceNotAvailableException("call spellCheck on disabled search service");
	}

	@Override
	public void stop() {
		//
	}

	public boolean isEnabled() {
		return false;
	}

	@Override
	public long getQueryCount() {
		return 0;
	}

	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, boolean doHighlighting)
			throws ServiceNotAvailableException, ParseException, QueryException {
		log.error("call doSearch on disabled search service");
		throw new ServiceNotAvailableException("call doSearch on disabled search service");
	}

	@Override
	public List<Long> doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxReturns, SortKey... orderBy)
			throws ServiceNotAvailableException, ParseException, QueryException {
		log.error("call doSearch on disabled search service");
		throw new ServiceNotAvailableException("call doSearch on disabled search service");
	}

	@Override
	public Document doSearch(String resourceUrl)
			throws ServiceNotAvailableException, ParseException, QueryException {
		log.error("call doSearch on disabled search service");
		throw new ServiceNotAvailableException("call doSearch on disabled search service");
	}
}
