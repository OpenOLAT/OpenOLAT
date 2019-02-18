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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.search.model.AbstractOlatDocument;

/**
 * 
 * Initial date: 24.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class GetDocumentByCallable implements Callable<Document> {
	
	private static final OLog log = Tracing.createLoggerFor(GetDocumentByCallable.class);
	
	/**
	 * To prevent flooding the logs with errors during a re-index
	 */
	private static int countEx = 0;
	
	private final String resourceUrl;
	private final SearchServiceImpl searchService;
	
	public GetDocumentByCallable(String resourceUrl,  SearchServiceImpl searchService) {
		this.resourceUrl = resourceUrl;
		this.searchService = searchService;
	}
	
	public static int incrementEx() {
		return countEx++;
	}
	
	@Override
	public Document call() {
		Document doc = null;
		IndexSearcher searcher = null;
		try(KeywordAnalyzer analyzer=new KeywordAnalyzer()) {
			if (searchService.existIndex()) {
				searcher = searchService.getIndexSearcher();
				String url = Encoder.md5hash(resourceUrl);
				String queryStr = "+" + AbstractOlatDocument.RESOURCEURL_MD5_FIELD_NAME + ":\"" + url + "\"";
				QueryParser idQueryParser = new QueryParser(queryStr, analyzer);
				Query query = idQueryParser.parse(queryStr);
				
				TopDocs docs = searcher.search(query, 500);
				long numOfDocs = docs.totalHits;

				Set<String> retrievedFields = new HashSet<>();
				retrievedFields.add(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
				retrievedFields.add(AbstractOlatDocument.RESOURCEURL_MD5_FIELD_NAME);

				for(int i=0; i<numOfDocs; i++) {
					Document foundDoc = searcher.doc(docs.scoreDocs[i].doc, retrievedFields);
					String possibleResourceUrl = foundDoc.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
					if(resourceUrl.equals(possibleResourceUrl)) {
						doc = searcher.doc(docs.scoreDocs[i].doc);
					}
				}
			}
		} catch(IllegalStateException ise) {
			if(incrementEx() % 500 == 0) {
				log.error("", ise);
			} else {
				log.warn("", ise);
			}
		} catch (Exception naex) {
			log.error("", naex);
		} finally {
			searchService.releaseIndexSearcher(searcher);
		}
		return doc;
	}
}