/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.search.service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.search.SearchResults;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.service.searcher.SearchResultsImpl;

/**
 * 
 * Initial date: 24.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SearchCallable implements Callable<SearchResults> {
	
	private static final OLog log = Tracing.createLoggerFor(SearchCallable.class);
	
	private final Locale locale;
	private final String queryString;
	private final List<String> condQueries;
	private final Identity identity;
	private final Roles roles;
	private final int firstResult;
	private final int maxResults;
	private final boolean doHighlighting;
	private final SearchServiceImpl searchService;
	
	public SearchCallable(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, boolean doHighlighting, SearchServiceImpl searchService) {
		this.queryString = queryString;
		this.condQueries = condQueries;
		this.locale = locale;
		this.identity = identity;
		this.roles = roles;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.doHighlighting = doHighlighting;
		this.searchService = searchService;
	}
	
	@Override
	public SearchResults call() throws ParseException {
		IndexSearcher searcher = null;
		try {
			boolean debug = log.isDebug();
			
			if (!searchService.existIndex()) {
				log.warn("Index does not exist, can't search for queryString: "+queryString);
				throw new ServiceNotAvailableException("Index does not exist");
			}

			if(debug) log.debug("queryString=" + queryString);
			searcher = searchService.getIndexSearcher();
			BooleanQuery.Builder queryBuilder = searchService.createQuery(queryString, condQueries, locale);
			if(debug) log.debug("query=" + queryBuilder);
			
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			long startTime = System.currentTimeMillis();
			int n = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();
	
			Query query = queryBuilder.build();
			TopDocs docs = searcher.search(query, n);
			long queryTime = System.currentTimeMillis() - startTime;
			if(debug) log.debug("hits.length()=" + docs.totalHits);
			SearchResultsImpl searchResult = new SearchResultsImpl(searchService.getMainIndexer(), searcher, docs, query, searchService.getAnalyzer(), identity, roles, firstResult, maxResults, doHighlighting, false);
			searchResult.setQueryTime(queryTime);
			searchResult.setNumberOfIndexDocuments(docs.totalHits);
			if(debug) log.debug("found=" + docs.totalHits);
			
			return searchResult;
		} catch(ParseException pex) {
			throw pex;
		} catch (Exception naex) {
			log.error("", naex);
			return null;
		} finally {
			searchService.releaseIndexSearcher(searcher);
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
}