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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.model.AbstractOlatDocument;

/**
 * 
 * Initial date: 24.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SearchOrderByCallable implements Callable<List<Long>> {
	
	private static final OLog log = Tracing.createLoggerFor(SearchOrderByCallable.class);
	
	private String queryString;
	private List<String> condQueries;
	private Locale locale;
	private SortKey[] orderBy;
	private int firstResult;
	private int maxResults;
	private SearchServiceImpl searchService;
	
	public SearchOrderByCallable(String queryString, List<String> condQueries, SortKey[] orderBy, Locale locale,
			int firstResult, int maxResults,  SearchServiceImpl searchService) {
		this.queryString = queryString;
		this.condQueries = condQueries;
		this.locale = locale;
		this.orderBy = orderBy;
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.searchService = searchService;
	}
	
	@Override
	public List<Long> call() {
		IndexSearcher searcher = null;
		int found = -1;
		try {
			if (!searchService.existIndex()) {
				log.warn("Index does not exist, can't search for queryString: "+queryString);
				throw new ServiceNotAvailableException("Index does not exist");
			}
			
			searcher = searchService.getIndexSearcher();
			BooleanQuery.Builder queryBuilder = searchService.createQuery(queryString, condQueries, locale);
			
			//only search document with an primary key
			String idNotNull = AbstractOlatDocument.DB_ID_NAME + ":[* TO *]";
			QueryParser idQueryParser = new QueryParser(idNotNull, searchService.getAnalyzer());
			Query idQuery = idQueryParser.parse(idNotNull);
			queryBuilder.add(idQuery, Occur.MUST);

			int n = searchService.getSearchModuleConfig().getMaxHits();
			TopDocs docs;
			if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
				SortField[] sortFields = new SortField[orderBy.length];
				for(int i=0; i<orderBy.length; i++) {
					sortFields[i] = new SortField(orderBy[i].getKey(), SortField.Type.STRING_VAL, orderBy[i].isAsc());
				}
				Sort sort = new Sort(sortFields);
				docs = searcher.search(queryBuilder.build(), n, sort);
			} else {
				docs = searcher.search(queryBuilder.build(), n);
			}

			long numOfDocs = Math.min(n, docs.totalHits);
			Set<String> retrievedFields = new HashSet<>();
			retrievedFields.add(AbstractOlatDocument.DB_ID_NAME);
			
			List<Long> res = new ArrayList<>();
			for (int i=firstResult; i<numOfDocs && res.size() < maxResults; i++) {
				Document doc = searcher.doc(docs.scoreDocs[i].doc, retrievedFields);
				String dbKeyStr = doc.get(AbstractOlatDocument.DB_ID_NAME);
				if(StringHelper.containsNonWhitespace(dbKeyStr)) {
					res.add(Long.parseLong(dbKeyStr));
				}
			}
			
			found = res.size();
			return res;
		} catch (Exception naex) {
			log.error("", naex);
			return null;
		} finally {
			searchService.releaseIndexSearcher(searcher);
			DBFactory.getInstance().commitAndCloseSession();
			log.info("queryString=" + queryString + " (" + found + " hits)");
		}
	}
}