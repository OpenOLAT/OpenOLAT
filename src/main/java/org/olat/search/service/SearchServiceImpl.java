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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.search.QueryException;
import org.olat.search.SearchModule;
import org.olat.search.SearchResults;
import org.olat.search.SearchService;
import org.olat.search.SearchServiceStatus;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.service.indexer.FullIndexerStatus;
import org.olat.search.service.indexer.Index;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.search.service.indexer.MainIndexer;
import org.olat.search.service.searcher.JmsSearchProvider;
import org.olat.search.service.searcher.SearchResultsImpl;
import org.olat.search.service.spell.SearchSpellChecker;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * 
 * @author Christian Guretzki
 */
public class SearchServiceImpl implements SearchService {
	private static final OLog log = Tracing.createLoggerFor(SearchServiceImpl.class);
	
	private Index indexer;	
	private SearchModule searchModuleConfig;
	private MainIndexer mainIndexer;
	private Scheduler scheduler;

	private Analyzer analyzer;
	private IndexSearcher searcher;
	private DirectoryReader reader;
	private DirectoryReader permReader;
	
	private LifeFullIndexer lifeIndexer;
	private SearchSpellChecker searchSpellChecker;
	private String indexPath;
	private String permanentIndexPath;
	
	/** Counts number of search queries since last restart. */
	private long queryCount = 0;
	private Object createIndexSearcherLock = new Object();

	private String fields[] = {
			AbstractOlatDocument.TITLE_FIELD_NAME, AbstractOlatDocument.DESCRIPTION_FIELD_NAME,
			AbstractOlatDocument.CONTENT_FIELD_NAME, AbstractOlatDocument.AUTHOR_FIELD_NAME,
			AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, AbstractOlatDocument.FILETYPE_FIELD_NAME,
			QItemDocument.STUDY_FIELD, QItemDocument.IDENTIFIER_FIELD,
			QItemDocument.MASTER_IDENTIFIER_FIELD, QItemDocument.KEYWORDS_FIELD,
			QItemDocument.COVERAGE_FIELD, QItemDocument.ADD_INFOS_FIELD,
			QItemDocument.LANGUAGE_FIELD, QItemDocument.EDU_CONTEXT_FIELD,
			QItemDocument.ITEM_TYPE_FIELD, QItemDocument.ASSESSMENT_TYPE_FIELD,
			QItemDocument.ITEM_VERSION_FIELD, QItemDocument.ITEM_STATUS_FIELD,
			QItemDocument.COPYRIGHT_FIELD, QItemDocument.EDITOR_FIELD,
			QItemDocument.EDITOR_VERSION_FIELD, QItemDocument.FORMAT_FIELD
	};

	
	/**
	 * [used by spring]
	 */
	private SearchServiceImpl(SearchModule searchModule, MainIndexer mainIndexer, JmsSearchProvider searchProvider,
			Scheduler scheduler, LifeFullIndexer lifeIndexer) {
		log.info("Start SearchServiceImpl constructor...");
		this.scheduler = scheduler;
		this.searchModuleConfig = searchModule;
		this.mainIndexer = mainIndexer;
		this.lifeIndexer = lifeIndexer;
		analyzer = new StandardAnalyzer(SearchService.OO_LUCENE_VERSION);
		searchProvider.setSearchService(this);
	}
	
	/**
	 * Start the job indexer
	 */
	public void startIndexing() {
		if (indexer==null) throw new AssertException ("Try to call startIndexing() but indexer is null");
		
		try {
			Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
			JobDetail detail = scheduler.getJobDetail("org.olat.search.job.enabled", Scheduler.DEFAULT_GROUP);
			scheduler.triggerJob(detail.getName(), detail.getGroup());
			log.info("startIndexing...");
		} catch (SchedulerException e) {
			log.error("Error trigerring the indexer job: ", e);
		}
	}

	/**
	 * Interrupt the job indexer
	 */
	public void stopIndexing() {
		if (indexer==null) throw new AssertException ("Try to call stopIndexing() but indexer is null");

		try {
			Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
			JobDetail detail = scheduler.getJobDetail("org.olat.search.job.enabled", Scheduler.DEFAULT_GROUP);
			scheduler.interrupt(detail.getName(), detail.getGroup());
			log.info("stopIndexing.");
		} catch (SchedulerException e) {
			log.error("Error interrupting the indexer job: ", e);
		}
	}
	
	public Index getInternalIndexer() {
		return indexer;
	}

	public void init() {
		log.info("init searchModuleConfig=" + searchModuleConfig);

		log.info("Running with indexPath=" + searchModuleConfig.getFullIndexPath());
		log.info("        tempIndexPath=" + searchModuleConfig.getFullTempIndexPath());
		log.info("        generateAtStartup=" + searchModuleConfig.getGenerateAtStartup());
		log.info("        indexInterval=" + searchModuleConfig.getIndexInterval());

		searchSpellChecker = new SearchSpellChecker();
		searchSpellChecker.setIndexPath(searchModuleConfig.getFullIndexPath());
		searchSpellChecker.setSpellDictionaryPath(searchModuleConfig.getSpellCheckDictionaryPath());
		searchSpellChecker.setSpellCheckEnabled(searchModuleConfig.getSpellCheckEnabled());
		
	  indexer = new Index(searchModuleConfig, searchSpellChecker, mainIndexer, lifeIndexer);

	  indexPath = searchModuleConfig.getFullIndexPath();	
	  permanentIndexPath = searchModuleConfig.getFullPermanentIndexPath();

  	if (startingFullIndexingAllowed()) {
  		try {
				JobDetail detail = scheduler.getJobDetail("org.olat.search.job.enabled", Scheduler.DEFAULT_GROUP);
				scheduler.triggerJob(detail.getName(), detail.getGroup());
			} catch (SchedulerException e) {
				log.error("", e);
			}
  	}
  	log.info("init DONE");
	}

	/**
	 * Do search a certain query. The results will be filtered for the identity and roles.
	 * @param queryString   Search query-string.
	 * @param identity      Filter results for this identity (user). 
	 * @param roles         Filter results for this roles (role of user).
 	 * @return              SearchResults object for this query
	 */
	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles,
			int firstResult, int maxResults, boolean doHighlighting)
	throws ServiceNotAvailableException, ParseException {
		try {
			if (!existIndex()) {
				log.warn("Index does not exist, can't search for queryString: "+queryString);
				throw new ServiceNotAvailableException("Index does not exist");
			}
			
			log.info("queryString=" + queryString);
			IndexSearcher searcher = getIndexSearcher();
			BooleanQuery query = createQuery(queryString, condQueries);
			if (log.isDebug()) log.debug("query=" + query);
			
	    long startTime = System.currentTimeMillis();
	    int n = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();

	    TopDocs docs = searcher.search(query, n);
	    long queryTime = System.currentTimeMillis() - startTime;
	    if (log.isDebug()) log.debug("hits.length()=" + docs.totalHits);
	    SearchResultsImpl searchResult = new SearchResultsImpl(mainIndexer, searcher, docs, query, analyzer, identity, roles, firstResult, maxResults, doHighlighting, false);
	    searchResult.setQueryTime(queryTime);
	    searchResult.setNumberOfIndexDocuments(docs.totalHits);
	    queryCount++;
	    return searchResult;
		} catch (ServiceNotAvailableException naex) {
			// pass exception 
			throw new ServiceNotAvailableException(naex.getMessage());
		} catch (ParseException pex) {
			throw new ParseException("can not parse query=" + queryString);
		} catch (Exception ex) {
			log.warn("Exception in search", ex);
			throw new ServiceNotAvailableException(ex.getMessage());
		}
 	}
	
	@Override
	public List<Long> doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles,
			int firstResult, int maxResults, SortKey... orderBy)
	throws ServiceNotAvailableException, ParseException, QueryException {
		try {
			if (!existIndex()) {
				log.warn("Index does not exist, can't search for queryString: "+queryString);
				throw new ServiceNotAvailableException("Index does not exist");
			}
			
			log.info("queryString=" + queryString);
			IndexSearcher searcher = getIndexSearcher();
			BooleanQuery query = createQuery(queryString, condQueries);

	    int n = SearchServiceFactory.getService().getSearchModuleConfig().getMaxHits();
	    TopDocs docs;
	    if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
	    	SortField[] sortFields = new SortField[orderBy.length];
	    	for(int i=0; i<orderBy.length; i++) {
	    		sortFields[i] = new SortField(orderBy[i].getKey(), SortField.Type.STRING_VAL, orderBy[i].isAsc());
	    	}
	    	Sort sort = new Sort(sortFields);
	    	docs = searcher.search(query, n, sort);
	    } else {
	    	docs = searcher.search(query, n);
	    }

			int numOfDocs = Math.min(n, docs.totalHits);
			Set<String> retrievedFields = new HashSet<String>();
			retrievedFields.add(AbstractOlatDocument.DB_ID_NAME);
			
			List<Long> res = new ArrayList<Long>();
	    for (int i=firstResult; i<numOfDocs && res.size() < maxResults; i++) {
	    	Document doc = searcher.doc(docs.scoreDocs[i].doc, retrievedFields);
	    	String dbKeyStr = doc.get(AbstractOlatDocument.DB_ID_NAME);
	    	if(StringHelper.containsNonWhitespace(dbKeyStr)) {
	    		res.add(Long.parseLong(dbKeyStr));
	    	}
	    }
	    queryCount++;
	    return res;
		} catch (ServiceNotAvailableException naex) {
			// pass exception 
			throw new ServiceNotAvailableException(naex.getMessage());
		} catch (ParseException pex) {
			throw new ParseException("can not parse query=" + queryString);
		} catch (Exception ex) {
			log.warn("Exception in search", ex);
			throw new ServiceNotAvailableException(ex.getMessage());
		}
	}
	
	private BooleanQuery createQuery(String queryString, List<String> condQueries)
	throws ParseException {
		BooleanQuery query = new BooleanQuery();
		if(StringHelper.containsNonWhitespace(queryString)) {
			String[] fieldsArr = getFieldsToSearchIn();
			QueryParser queryParser = new MultiFieldQueryParser(SearchService.OO_LUCENE_VERSION, fieldsArr, analyzer);
			queryParser.setLowercaseExpandedTerms(false);//some add. fields are not tokenized and not lowered case
	  	Query multiFieldQuery = queryParser.parse(queryString.toLowerCase());
	  	query.add(multiFieldQuery, Occur.MUST);
		}
		
		if(condQueries != null && !condQueries.isEmpty()) {
			for(String condQueryString:condQueries) {
				QueryParser condQueryParser = new QueryParser(SearchService.OO_LUCENE_VERSION, condQueryString, analyzer);
				condQueryParser.setLowercaseExpandedTerms(false);
		  	Query condQuery = condQueryParser.parse(condQueryString);
		  	query.add(condQuery, Occur.MUST);
			}
		}
		return query;
	}
	
	private String[] getFieldsToSearchIn() {
		return fields;
	}

	/**
	 * Delegates impl to the searchSpellChecker.
	 * @see org.olat.search.service.searcher.OLATSearcher#spellCheck(java.lang.String)
	 */
	public Set<String> spellCheck(String query) {
		if(searchSpellChecker==null) throw new AssertException ("Try to call spellCheck() in Search.java but searchSpellChecker is null");
		return searchSpellChecker.check(query);
	}

	public long getQueryCount() {
		return queryCount;
	}
	
	public SearchServiceStatus getStatus() {
		return new SearchServiceStatusImpl(indexer,this);
	}

	public void setIndexInterval(long indexInterval) {
		if (indexer==null) throw new AssertException ("Try to call setIndexInterval() but indexer is null");
		indexer.setIndexInterval(indexInterval);
	}
	
	public long getIndexInterval() {
		if (indexer==null) throw new AssertException ("Try to call setIndexInterval() but indexer is null");
		return indexer.getIndexInterval();
	}
	
	/**
	 * 
	 * @return  Resturn search module configuration.
	 */
	public SearchModule getSearchModuleConfig() {
		return searchModuleConfig;
	}

	public void stop() {
		SearchServiceStatus status = getStatus();
		String statusStr = status.getStatus();
		if(statusStr.equals(FullIndexerStatus.STATUS_RUNNING)){
			stopIndexing();
		}
		try {
			if (searcher != null) {
				searcher.getIndexReader().close();
				searcher = null;
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public boolean isEnabled() {
		return true;
	}
	
	private IndexSearcher getIndexSearcher() throws ServiceNotAvailableException, IOException {
		if(searcher == null) {
			synchronized (createIndexSearcherLock) {//o_clusterOK by:fj if service is only configured on one vm, which is recommended way
				if (searcher == null) {
					try {
						getIndexSearcher(indexPath, permanentIndexPath);
					} catch(IOException ioEx) {
						log.warn("Can not create searcher", ioEx);
						throw new ServiceNotAvailableException("Index is not available");
					}
				}
			}
		}
	
		return getIndexSearcher(indexPath, permanentIndexPath);
	}

	private synchronized IndexSearcher getIndexSearcher(String path, String permanentPath)
	throws IOException {
		boolean hasChanged = false;
		if(reader == null) {
			hasChanged = true;
			reader = DirectoryReader.open(FSDirectory.open(new File(path)));
		} else {
			DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
			if(newReader != null) {
				hasChanged = true;
				reader = newReader;
			}
		}
		
		if(permReader == null) {
			hasChanged = true;
			permReader = DirectoryReader.open(FSDirectory.open(new File(permanentPath)));
		} else {
			DirectoryReader newReader = DirectoryReader.openIfChanged(permReader);
			if(newReader != null) {
				hasChanged = true;
				permReader = newReader;
			}
		}
		
		if(hasChanged) {
			MultiReader mReader = new MultiReader(reader, permReader);
			searcher = new IndexSearcher(mReader);
			//openIndexDate = reader.getVersion();
		}
		return searcher;
	}

	/**
	 * [used by spring]
	 * Spring setter to inject the available metadata
	 * 
	 * @param metadataFields
	 */
	public void setMetadataFields(SearchMetadataFieldsProvider metadataFields) {
		if (metadataFields != null) {
			// add metadata fields to normal fields
			String[] metaFields = ArrayHelper.toArray(metadataFields.getAdvancedSearchableFields());		
			String[] newFields = new String[fields.length + metaFields.length];
			System.arraycopy(fields, 0, newFields, 0, fields.length);
			System.arraycopy(metaFields, 0, newFields, fields.length, metaFields.length);
			fields = newFields;			
		}
	}

	/**
	 * Check if index exist.
	 * @return true : Index exists.
	 */
	private boolean existIndex()
	throws IOException {
		try {
			File indexFile = new File(searchModuleConfig.getFullIndexPath());
			Directory directory = FSDirectory.open(indexFile);
			File permIndexFile = new File(searchModuleConfig.getFullPermanentIndexPath());
			Directory permDirectory = FSDirectory.open(permIndexFile);
			return DirectoryReader.indexExists(directory) && DirectoryReader.indexExists(permDirectory);
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * Check if starting a generating full-index is allowed. 
	 * Depends config-parameter 'generateAtStartup', day of the week and
	 * config-parameter 'restartDayOfWeek', current time and the restart-
	 * window (config-parameter 'restartWindowStart', 'restartWindowEnd')
	 * @return  TRUE: Starting is allowed
	 */
	private boolean startingFullIndexingAllowed() {
  	if (searchModuleConfig.getGenerateAtStartup()) {
  		Calendar calendar = Calendar.getInstance();
  		calendar.setTime(new Date());
  		// check day, Restart only at config day of week, 0-7 8=every day 
  		int dayNow = calendar.get(Calendar.DAY_OF_WEEK);
  		int restartDayOfWeek = searchModuleConfig.getRestartDayOfWeek();
  		if (restartDayOfWeek == 0 || (dayNow == restartDayOfWeek) ) {
    		// check time, Restart only in the config time-slot e.g. 01:00 - 03:00
    		int hourNow = calendar.get(Calendar.HOUR_OF_DAY);
    		int restartWindowStart = searchModuleConfig.getRestartWindowStart();
    		int restartWindowEnd   = searchModuleConfig.getRestartWindowEnd();
    		if ( (restartWindowStart <= hourNow) && (hourNow < restartWindowEnd) ) {
    			return true;
    		}  			
  		}
  	}
  	return false;
	}

}
