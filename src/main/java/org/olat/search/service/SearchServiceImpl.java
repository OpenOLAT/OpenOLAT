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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
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
import org.olat.search.service.indexer.IndexerEvent;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.search.service.indexer.MainIndexer;
import org.olat.search.service.searcher.ConditionalQueryAnalyzer;
import org.olat.search.service.searcher.JmsSearchProvider;
import org.olat.search.service.spell.SearchSpellChecker;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * 
 * @author Christian Guretzki
 */
public class SearchServiceImpl implements SearchService, GenericEventListener {
	private static final OLog log = Tracing.createLoggerFor(SearchServiceImpl.class);
	
	private final JobKey indexerJobKey = new JobKey("org.olat.search.job.enabled", Scheduler.DEFAULT_GROUP);
	
	private Index indexer;	
	private SearchModule searchModuleConfig;
	private MainIndexer mainIndexer;
	private Scheduler scheduler;
	private CoordinatorManager coordinatorManager;

	private final Analyzer analyzer;
	private final ConditionalQueryAnalyzer conditionalQueryAnalyzer;
	
	private LifeFullIndexer lifeIndexer;
	private SearchSpellChecker searchSpellChecker;
	private String indexPath;
	private String permanentIndexPath;
	private String indexerCron;
	
	/** Counts number of search queries since last restart. */
	private long queryCount = 0;
	
	private ExecutorService searchExecutor;
	private OOSearcherManager indexSearcherRefMgr;

	private String[] fields = {
			AbstractOlatDocument.TITLE_FIELD_NAME, AbstractOlatDocument.DESCRIPTION_FIELD_NAME,
			AbstractOlatDocument.CONTENT_FIELD_NAME, AbstractOlatDocument.AUTHOR_FIELD_NAME,
			AbstractOlatDocument.LOCATION_FIELD_NAME, AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME,
			AbstractOlatDocument.FILETYPE_FIELD_NAME, AbstractOlatDocument.PUBLICATION_DATE_FIELD_NAME,
			AbstractOlatDocument.CHANGED_FIELD_NAME, AbstractOlatDocument.CREATED_FIELD_NAME,
			QItemDocument.TAXONOMIC_PATH_FIELD, QItemDocument.TAXONOMIC_FIELD, 
			QItemDocument.IDENTIFIER_FIELD, QItemDocument.MASTER_IDENTIFIER_FIELD,
			QItemDocument.KEYWORDS_FIELD,
			QItemDocument.COVERAGE_FIELD, QItemDocument.ADD_INFOS_FIELD,
			QItemDocument.LANGUAGE_FIELD, QItemDocument.EDU_CONTEXT_FIELD,
			QItemDocument.ITEM_TYPE_FIELD, QItemDocument.ASSESSMENT_TYPE_FIELD,
			QItemDocument.ITEM_VERSION_FIELD, QItemDocument.ITEM_STATUS_FIELD,
			QItemDocument.EDITOR_FIELD, QItemDocument.EDITOR_VERSION_FIELD, 
			QItemDocument.FORMAT_FIELD, QItemDocument.TOPIC_FIELD
	};

	
	/**
	 * [used by spring]
	 */
	private SearchServiceImpl(SearchModule searchModule, MainIndexer mainIndexer,
			JmsSearchProvider searchProvider, CoordinatorManager coordinatorManager,
			Scheduler scheduler) {
		log.info("Start SearchServiceImpl constructor...");
		this.scheduler = scheduler;
		this.searchModuleConfig = searchModule;
		this.mainIndexer = mainIndexer;
		this.coordinatorManager = coordinatorManager;
		analyzer = new StandardAnalyzer();
		conditionalQueryAnalyzer = new ConditionalQueryAnalyzer();
		searchProvider.setSearchService(this);
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, IndexerEvent.INDEX_ORES);
	}
	
	public void setSearchExecutor(ExecutorService searchExecutor) {
		this.searchExecutor = searchExecutor;
	}

	/**
	 * [user by Spring]
	 * @param lifeIndexer
	 */
	public void setLifeIndexer(LifeFullIndexer lifeIndexer) {
		this.lifeIndexer = lifeIndexer;
	}
	
	/**
	 * [used by Spring]
	 * @param indexerCron
	 */
	public void setIndexerCron(String indexerCron) {
		this.indexerCron = indexerCron;
	}

	protected MainIndexer getMainIndexer() {
		return mainIndexer;
	}
	
	protected Analyzer getAnalyzer() {
		return analyzer;
	}
	
	/**
	 * Start the job indexer
	 */
	@Override
	public void startIndexing() {
		if (indexer==null) throw new AssertException ("Try to call startIndexing() but indexer is null");
		
		try {
			JobDetail detail = scheduler.getJobDetail(indexerJobKey);
			if(detail == null) {
				if("disabled".equals(indexerCron)) {
					indexer.startFullIndex();
				}
			} else {
				scheduler.triggerJob(indexerJobKey);
			}
			log.info("startIndexing...");
		} catch (SchedulerException e) {
			log.error("Error trigerring the indexer job: ", e);
		}
	}

	/**
	 * Interrupt the job indexer
	 */
	@Override
	public void stopIndexing() {
		if (indexer==null) throw new AssertException ("Try to call stopIndexing() but indexer is null");

		try {
			JobDetail detail = scheduler.getJobDetail(indexerJobKey);
			if(detail == null) {
				if("disabled".equals(indexerCron)) {
					indexer.stopFullIndex();
				}
			} else {
				scheduler.interrupt(indexerJobKey);
			}
			log.info("stopIndexing.");
		} catch (SchedulerException e) {
			log.error("Error interrupting the indexer job: ", e);
		}
	}
	
	public Index getInternalIndexer() {
		return indexer;
	}

	@Override
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
		searchSpellChecker.setSearchExecutor(searchExecutor);
		searchSpellChecker.setSearchModule(searchModuleConfig);
		
		indexer = new Index(searchModuleConfig, this, searchSpellChecker, mainIndexer, lifeIndexer, coordinatorManager);

		indexPath = searchModuleConfig.getFullIndexPath();	
		permanentIndexPath = searchModuleConfig.getFullPermanentIndexPath();
		
		createIndexSearcherManager();

		if (startingFullIndexingAllowed()) {
			try {
				scheduler.triggerJob(indexerJobKey);
			} catch (SchedulerException e) {
				log.error("", e);
			}
		}
		log.info("init DONE");
	}
	
	@Override
	public boolean refresh() {
		try {
			createIndexSearcherManager();
			return indexSearcherRefMgr != null;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	private void createIndexSearcherManager() {
		try {
			if(indexSearcherRefMgr == null) {
				if(existIndex()) {
					indexSearcherRefMgr = new OOSearcherManager(this);
				}
			} else {
				indexSearcherRefMgr.needRefresh();
			}
		} catch (IOException e) {
			log.error("Cannot initialized the searcher manager", e);
		}
	}

	@Override
	public void event(Event event) {
		if(event instanceof IndexerEvent) {
			if(IndexerEvent.INDEX_CREATED.equals(event.getCommand())) {
				createIndexSearcherManager();
			}
		}
	}

	/**
	 * Do search a certain query. The results will be filtered for the identity and roles.
	 * @param queryString   Search query-string.
	 * @param identity      Filter results for this identity (user). 
	 * @param roles         Filter results for this roles (role of user).
 	 * @return              SearchResults object for this query
	 */
	@Override
	public SearchResults doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, boolean doHighlighting)
	throws ServiceNotAvailableException, ParseException {
		
		Future<SearchResults> futureResults = null;
		try {
			SearchCallable run = new SearchCallable(queryString,  condQueries, identity, roles, locale, firstResult, maxResults, doHighlighting, this);
			futureResults = searchExecutor.submit(run);
			SearchResults results = futureResults.get(searchModuleConfig.getSearchTimeout(), TimeUnit.SECONDS);
			queryCount++;
			if(results != null && results.getException() instanceof IllegalStateException) {
				refresh();
			}
			return results;
		} catch (InterruptedException | TimeoutException e) {
			cancelSearch(futureResults);
			log.error("", e);
			return null;
		} catch (ExecutionException e) {
			Throwable e1 = e.getCause();
			if(e1 instanceof ParseException) {
				throw (ParseException)e1;
			} else if(e1 instanceof ServiceNotAvailableException) {
				throw (ServiceNotAvailableException)e1;
			}
			log.error("", e);
			return null;
		}
 	}
	
	@Override
	public List<Long> doSearch(String queryString, List<String> condQueries, Identity identity, Roles roles, Locale locale,
			int firstResult, int maxResults, SortKey... orderBy)
	throws ServiceNotAvailableException, ParseException, QueryException {
		
		Future<List<Long>> futureResults = null;
		try {
			SearchOrderByCallable run = new SearchOrderByCallable(queryString, condQueries, orderBy, locale, firstResult, maxResults, this);
			futureResults = searchExecutor.submit(run);
			List<Long> results = futureResults.get(searchModuleConfig.getSearchTimeout(), TimeUnit.SECONDS);
			queryCount++;
			if(results == null) {
				results = new ArrayList<>(1);
			}
			return results;
		} catch (NullPointerException e) {
			// something happens during execution of the query and the error will be there logged
			return new ArrayList<>(1);
		} catch (TimeoutException e) {
			cancelSearch(futureResults);
			log.error("", e);
			return new ArrayList<>(1);
		} catch (Exception e) {
			log.error("", e);
			return new ArrayList<>(1);
		}
	}
	
	@Override
	public Document doSearch(String queryString)
	throws ServiceNotAvailableException, ParseException, QueryException {
		Future<Document> futureResults = null;
		try {
			GetDocumentByCallable run = new GetDocumentByCallable(queryString, this);
			futureResults = searchExecutor.submit(run);
			return futureResults.get(searchModuleConfig.getSearchTimeout(), TimeUnit.SECONDS);
		} catch (NullPointerException e) {
			// something happens during execution of the query and the error will be there logged
			return null;
		} catch (TimeoutException e) {
			cancelSearch(futureResults);
			log.error("", e);
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private void cancelSearch(Future<?> search) {
		if(search != null) {
			try {
				search.cancel(false);
			} catch (Exception e) {
				log.error("Error canceling a search", e);
			}
		}
	}
	
	protected BooleanQuery.Builder createQuery(String queryString, List<String> condQueries, Locale locale)
	throws ParseException {
		BooleanQuery.Builder query = new BooleanQuery.Builder();
		if(StringHelper.containsNonWhitespace(queryString)) {
			String[] fieldsArr = getFieldsToSearchIn();
			Map<String,Float> boosters = getFieldsBoosters();
			QueryParser queryParser = new MultiFieldQueryParser(fieldsArr, analyzer, boosters);
			queryParser.setLocale(locale);//some add. fields are not tokenized and not lowered case
			Query multiFieldQuery = queryParser.parse(queryString.toLowerCase());
			query.add(multiFieldQuery, Occur.MUST);
		}
		
		if(condQueries != null && !condQueries.isEmpty()) {
			for(String condQueryString:condQueries) {
				QueryParser condQueryParser = new QueryParser(condQueryString, conditionalQueryAnalyzer);
				condQueryParser.setLocale(locale);
				Query condQuery = condQueryParser.parse(condQueryString);
				query.add(condQuery, Occur.MUST);
			}
		}
		return query;
	}
	
	private Map<String,Float> getFieldsBoosters() {
		Map<String,Float> boosters = new HashMap<>();
		boosters.put(AbstractOlatDocument.TITLE_FIELD_NAME, Float.valueOf(4.0f));
	
		boosters.put(AbstractOlatDocument.DESCRIPTION_FIELD_NAME, Float.valueOf(2.0f));
		boosters.put(AbstractOlatDocument.AUTHOR_FIELD_NAME, Float.valueOf(2.0f));
		boosters.put(AbstractOlatDocument.LOCATION_FIELD_NAME, Float.valueOf(2.0f));
		boosters.put(QItemDocument.LICENSE_TYPE_FIELD_NAME, 2.0f);
		boosters.put(QItemDocument.COVERAGE_FIELD, 2.0f);
		boosters.put(QItemDocument.KEYWORDS_FIELD, 2.0f);
		boosters.put(QItemDocument.LICENSE_TYPE_FIELD_NAME, 2.0f);

		boosters.put(AbstractOlatDocument.CONTENT_FIELD_NAME, Float.valueOf(0.5f));
		
		return boosters;
	}
	
	private String[] getFieldsToSearchIn() {
		return fields;
	}

	/**
	 * Delegates impl to the searchSpellChecker.
	 * @see org.olat.search.service.searcher.OLATSearcher#spellCheck(java.lang.String)
	 */
	@Override
	public Set<String> spellCheck(String query) {
		if(searchSpellChecker==null) throw new AssertException ("Try to call spellCheck() in Search.java but searchSpellChecker is null");
		if (StringHelper.containsNonWhitespace(query)) {
			return searchSpellChecker.check(query);
		}
		return new HashSet<>();
	}

	@Override
	public long getQueryCount() {
		return queryCount;
	}

	@Override
	public SearchServiceStatus getStatus() {
		return new SearchServiceStatusImpl(indexer,this);
	}

	@Override
	public void setIndexInterval(long indexInterval) {
		if (indexer==null) throw new AssertException ("Try to call setIndexInterval() but indexer is null");
		indexer.setIndexInterval(indexInterval);
	}

	@Override
	public long getIndexInterval() {
		if (indexer==null) throw new AssertException ("Try to call setIndexInterval() but indexer is null");
		return indexer.getIndexInterval();
	}
	
	/**
	 * 
	 * @return  Resturn search module configuration.
	 */
	@Override
	public SearchModule getSearchModuleConfig() {
		return searchModuleConfig;
	}

	@Override
	public void stop() {
		SearchServiceStatus status = getStatus();
		String statusStr = status.getStatus();
		if(statusStr.equals(FullIndexerStatus.STATUS_RUNNING)){
			stopIndexing();
		}
		try {
			if (indexSearcherRefMgr != null) {
				indexSearcherRefMgr.close();
				indexSearcherRefMgr = null;
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	protected IndexSearcher getIndexSearcher()
	throws ServiceNotAvailableException, IOException {
		if(indexSearcherRefMgr == null) {
			throw new ServiceNotAvailableException("Local search not available");
		}
		
		indexSearcherRefMgr.maybeRefresh();
		return indexSearcherRefMgr.acquire();
	}
	
	protected void releaseIndexSearcher(IndexSearcher s) {
		if(indexSearcherRefMgr != null) {
			try {
				indexSearcherRefMgr.release(s);
			} catch (IOException e) {
				log.error("Error while releasing index searcher", e);
			}
		}
	}
	

	private IndexSearcher newSearcher() throws IOException {
		DirectoryReader classicReader = DirectoryReader.open(FSDirectory.open(new File(indexPath).toPath()));
		DirectoryReader permanentReader = DirectoryReader.open(FSDirectory.open(new File(permanentIndexPath).toPath()));
		OOMultiReader mReader = new OOMultiReader(classicReader, permanentReader);
		return new IndexSearcher(mReader);
	}

	private static class OOMultiReader extends MultiReader {
		
		private final DirectoryReader reader;
		private final DirectoryReader permanentReader;
		
		public OOMultiReader(DirectoryReader reader, DirectoryReader permanentReader) throws IOException {
			super(reader, permanentReader);
			this.reader = reader;
			this.permanentReader = permanentReader;
		}

		public DirectoryReader getReader() {
			return reader;
		}

		public DirectoryReader getPermanentReader() {
			return permanentReader;
		}
	}

	private static class OOSearcherManager extends ReferenceManager<IndexSearcher> {
		
		private final SearchServiceImpl factory;
		private AtomicBoolean refresh = new AtomicBoolean(false);
		
		public OOSearcherManager(SearchServiceImpl factory) throws IOException {
			this.factory = factory;
			this.current = getSearcher(factory);
		}
		
		protected void needRefresh() {
			refresh.getAndSet(true);
		}

		@Override
		protected void decRef(IndexSearcher reference) throws IOException {
			if(reference != null) {
				reference.getIndexReader().decRef();
			}
		}

		@Override
		protected IndexSearcher refreshIfNeeded(IndexSearcher referenceToRefresh)
		throws IOException {
		    IndexSearcher searcher;
		    if(refresh.getAndSet(false)) {
		    	searcher = getSearcher(factory);
		    } else {
		    	final OOMultiReader r = (OOMultiReader)referenceToRefresh.getIndexReader();
		    	final IndexReader newReader = DirectoryReader.openIfChanged(r.getReader());
		    	final IndexReader newPermReader = DirectoryReader.openIfChanged(r.getPermanentReader());  
		    	if (newReader == null && newPermReader == null) {
		    		searcher = null;
		    	} else {
			    	searcher = getSearcher(factory);
			    }
		    }
		    return searcher;
		}

		@Override
		protected boolean tryIncRef(IndexSearcher reference) throws IOException {
			return reference.getIndexReader().tryIncRef();
		}

		@Override
		protected int getRefCount(IndexSearcher reference) {
			return reference.getIndexReader().getRefCount();
		}
		
		public static IndexSearcher getSearcher(SearchServiceImpl searcherFactory)
		throws IOException {
			return searcherFactory.newSearcher();
		}
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
	protected boolean existIndex()
	throws IOException {
		
			File indexFile = new File(searchModuleConfig.getFullIndexPath());
			if(indexFile.exists()) {
				File permIndexFile = new File(searchModuleConfig.getFullPermanentIndexPath());
				try(FSDirectory directory = FSDirectory.open(indexFile.toPath());
						FSDirectory permDirectory = FSDirectory.open(permIndexFile.toPath())) {
					return DirectoryReader.indexExists(directory) && DirectoryReader.indexExists(permDirectory);
				} catch(IOException e) {
					log.error("", e);
				}
			}
			return false;
		
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
