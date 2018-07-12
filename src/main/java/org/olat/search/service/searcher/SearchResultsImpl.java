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

package org.olat.search.service.searcher;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.search.SearchResults;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.model.ResultDocument;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.indexer.Indexer;


/**
 * Data object to pass search results back from search service.
 * @author Christian Guretzki
 * 
 */
public class SearchResultsImpl implements SearchResults {

	private static final long serialVersionUID = 3950063141792217522L;
	private static final OLog log = Tracing.createLoggerFor(SearchResultsImpl.class);
	
	private static final String HIGHLIGHT_PRE_TAG  = "<span class=\"o_search_result_highlight\">"; 
	private static final String HIGHLIGHT_POST_TAG = "</span>";
	private static final String HIGHLIGHT_SEPARATOR = "...<br />";
	
	/* Define in module config */
	private int maxHits;
	private int totalHits;
	private int totalDocs;
	private long queryTime;
	private int numberOfIndexDocuments;
	/* List of ResultDocument. */
	private List<ResultDocument> resultList;
	private transient Indexer mainIndexer;

	/**
	 * Constructure for certain search-results. 
	 * Does not include any search-call to search-service. 
	 * Search call must be made before to create a Hits object. 
	 * @param hits           Search hits return from search.
	 * @param query          Search query-string.
	 * @param analyzer       Search analyser, must be the same like at creation of index.
	 * @param identity       Filter results for this identity (user). 
	 * @param roles          Filter results for this roles (role of user).
	 * @param doHighlighting Flag to enable highlighting search 
	 * @throws IOException
	 */
	public SearchResultsImpl(Indexer mainIndexer, IndexSearcher searcher, TopDocs docs, Query query, Analyzer analyzer, Identity identity,
			Roles roles, int firstResult, int maxReturns, boolean doHighlighting, boolean onlyDbKeys)
	throws IOException {
		this.mainIndexer = mainIndexer;
		resultList = initResultList(identity, roles, query, analyzer, searcher, docs, firstResult, maxReturns, doHighlighting, onlyDbKeys);
	}
	
	/**
	 * 
	 * @return  Length of result-list.
	 */
	@Override
	public int size() {
		return resultList == null ? 0 : resultList.size();
	}
	
  /**
   * @return List of ResultDocument.
   */
	@Override
	public List<ResultDocument> getList() {
		return resultList;
	}
	
	/**
	 * Set query response time in milliseconds.
	 * @param queryTime  Query response time in milliseconds.
	 */
	public void setQueryTime(long queryTime) {
		this.queryTime = queryTime;
	}

	/**
	 * @return  Query response time in milliseconds.
	 */
	public String getQueryTime() {
		return Long.toString(queryTime);
	}

	/**
	 * Set number of search-index-elements. 
	 * @param numberOfIndexDocuments  Number of search-index-elements.
	 */
	public void setNumberOfIndexDocuments(int numberOfIndexDocuments) {
		this.numberOfIndexDocuments = numberOfIndexDocuments;
	}

	/**
	 * @return  Number of search-index-elements. 
	 */
	public String getNumberOfIndexDocuments() {
		return Integer.toString(numberOfIndexDocuments);
	}
	
	/**
	 * @return  Number of maximal possible results.
	 */
	@Override
	public int getTotalHits() {
		return totalHits;
	}
	
	public int getTotalDocs() {
		return totalDocs;
	}
	
	public String getMaxHits() {
		return Integer.toString(maxHits);
	}
	
	public boolean hasTooManyResults() {
		return totalHits > maxHits;
	}

	private List<ResultDocument> initResultList(Identity identity, Roles roles, Query query, Analyzer analyzer, IndexSearcher searcher, TopDocs docs,
			int firstResult, int maxReturns, final boolean doHighlight, boolean onlyDbKeys)
	throws IOException {

		Set<String> fields = AbstractOlatDocument.getFields();
		if(onlyDbKeys) {
			fields.clear();
			fields.add(AbstractOlatDocument.DB_ID_NAME);
		} else if(!doHighlight) {
			fields.remove(AbstractOlatDocument.CONTENT_FIELD_NAME);
		}
		
		maxHits = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();
		totalHits = docs.totalHits;
		totalDocs = (docs.scoreDocs == null ? 0 : docs.scoreDocs.length);
		int numOfDocs = Math.min(maxHits, docs.totalHits);
		List<ResultDocument> res = new ArrayList<>(maxReturns + 1);
		for (int i=firstResult; i<numOfDocs && res.size() < maxReturns; i++) {
			Document doc;
			if(doHighlight) {
				doc = searcher.doc(docs.scoreDocs[i].doc);
			} else {
				doc = searcher.doc(docs.scoreDocs[i].doc, fields);
			}
			
			String reservedTo = doc.get(AbstractOlatDocument.RESERVED_TO);
			if(StringHelper.containsNonWhitespace(reservedTo) && !"public".equals(reservedTo)
					&& !reservedTo.contains(identity.getKey().toString())) {
				continue;//admin cannot see private documents
			}

			ResultDocument rDoc = createResultDocument(doc, i, query, analyzer, doHighlight, identity, roles);
			if(rDoc != null) {
				res.add(rDoc);
			}
			
			if(i % 10 == 0) {
				// Do commit after certain number of documents because the transaction should not be too big
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		return res;
	}
	
	/**
	 * Create a result document. Return null if the identity has not enough privileges to see the document.
	 * @param doc
	 * @param query
	 * @param analyzer
	 * @param doHighlight
	 * @param identity
	 * @param roles
	 * @return
	 * @throws IOException
	 */
	private ResultDocument createResultDocument(Document doc, int pos, Query query, Analyzer analyzer, boolean doHighlight, Identity identity, Roles roles) 
	throws IOException {
		String resourceUrl = doc.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
		if(resourceUrl == null) {
			resourceUrl = "";
		}	
		BusinessControl businessControl = BusinessControlFactory.getInstance().createFromString(resourceUrl);
		boolean hasAccess = mainIndexer.checkAccess(null, businessControl, identity, roles);
		
		ResultDocument resultDoc;
		if(hasAccess) {
			resultDoc = new ResultDocument(doc, pos);
			if (doHighlight) {
				doHighlight(query, analyzer, doc, resultDoc);
			}
		} else {
			resultDoc = null;
		}
		return resultDoc;
	}
	
	/**
	 * Highlight (bold,color) query words in result-document. Set HighlightResult for content or description. 
	 * @param query
	 * @param analyzer
	 * @param doc
	 * @param resultDocument
	 * @throws IOException
	 */
	private void doHighlight(Query query, Analyzer analyzer, Document doc, ResultDocument resultDocument) throws IOException {
		Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(HIGHLIGHT_PRE_TAG,HIGHLIGHT_POST_TAG) ,
				new SimpleHTMLEncoder(), new QueryScorer(query));
		// Get 3 best fragments of content and seperate with a "..."
		try {
			//highlight content
			String content = doc.get(AbstractOlatDocument.CONTENT_FIELD_NAME);
			TokenStream tokenStream = analyzer.tokenStream(AbstractOlatDocument.CONTENT_FIELD_NAME, new StringReader(content));
			String highlightResult = highlighter.getBestFragments(tokenStream, content, 3, HIGHLIGHT_SEPARATOR);
			
			// if no highlightResult is in content => look in description
			if (highlightResult.length() == 0) {
			  String description = doc.get(AbstractOlatDocument.DESCRIPTION_FIELD_NAME);
			  tokenStream = analyzer.tokenStream(AbstractOlatDocument.DESCRIPTION_FIELD_NAME, new StringReader(description));
			  highlightResult = highlighter.getBestFragments(tokenStream, description, 3, HIGHLIGHT_SEPARATOR);
			  resultDocument.setHighlightingDescription(true);
			}  
			resultDocument.setHighlightResult(highlightResult);
			
			//highlight title
			String title = doc.get(AbstractOlatDocument.TITLE_FIELD_NAME);
			title = title.trim();
			if(title.length() > 128) {
				title = FilterFactory.getHtmlTagAndDescapingFilter().filter(title);
				title = Formatter.truncate(title, 128);
			}
			tokenStream = analyzer.tokenStream(AbstractOlatDocument.TITLE_FIELD_NAME, new StringReader(title));
			String highlightTitle = highlighter.getBestFragments(tokenStream, title, 3, " ");
			resultDocument.setHighlightTitle(highlightTitle);
		} catch (InvalidTokenOffsetsException e) {
			log.warn("", e);
		}
	}
}
