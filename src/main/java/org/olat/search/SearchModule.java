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

/**
 * Search module config.
 * @author Christian Guretzki
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 *      
 * Initial Date: 15.06.200g <br>
 * @author Christian Guretzki
 */
public class SearchModule extends AbstractOLATModule {
	private static final OLog log = Tracing.createLoggerFor(SearchModule.class);
	
	// Definitions config parameter names in module-config
	public final static String CONF_SEARCH_SERVICE = "searchService";
	public final static String CONF_INDEX_PATH = "indexPath";
	public final static String CONF_PERMANENT_INDEX_PATH = "permanentIndexPath";
	public final static String CONF_TEMP_INDEX_PATH = "tempIndexPath";
	public final static String CONF_TEMP_SPELL_CHECK_PATH = "tempSpellCheckPath";
	public final static String CONF_GENERATE_AT_STARTUP = "generateIndexAtStartup";
	private static final String CONF_INDEX_INTERVAL = "indexInterval";
	private static final String CONF_MAX_HITS = "maxHits";
	private static final String CONF_MAX_RESULTS = "maxResults";
	private static final String CONF_FOLDER_POOL_SIZE = "folderPoolSize";
	private static final String CONF_RESTART_WINDOW_START = "restartWindowStart";
	private static final String CONF_RESTART_WINDOW_END = "restartWindowEnd";
	private static final String CONF_UPDATE_INTERVAL = "updateInterval";
	private static final String CONF_DOCUMENTS_PER_INTERVAL = "documentsPerInterval";
	private static final String CONF_RESTART_DAY_OF_WEEK = "restartDayOfWeek";
	private static final String CONF_PPT_FILE_ENABLED = "pptFileEnabled";
	private static final String CONF_EXCEL_FILE_ENABLED = "excelFileEnabled";
	private static final String CONF_PDF_FILE_ENABLED = "pdfFileEnabled";
	private static final String CONF_PDF_TEXT_BUFFERING = "pdfTextBuffering";
	private static final String CONF_PDF_EXTERNAL_INDEXER = "pdfExternalIndexer";
	private static final String CONF_PDF_EXTERNAL_INDEXER_CMD = "pdfExternExtractorCommand";
	private static final String CONF_SPELL_CHECK_ENABLED = "spellCheckEnabled";
	private static final String CONF_TEMP_PDF_TEXT_BUF_PATH = "pdfTextBufferPath";
	private static final String CONF_MAX_FILE_SIZE = "maxFileSize";
	private static final String CONF_RAM_BUFFER_SIZE_MB = "ramBufferSizeMb";
	private static final String CONF_USE_COMPOUND_FILE = "useCompoundFile";
	private static final String CONF_FILE_BLACK_LIST = "fileBlackList";
	
	// Default values
	private static final int    DEFAULT_INDEX_INTERVAL = 0;
	private static final int    DEFAULT_MAX_HITS = 1000;
	private static final int    DEFAULT_MAX_RESULTS = 100;
	private static final int    DEFAULT_FOLDER_POOL_SIZE = 0;
	private static final int    DEFAULT_RESTART_WINDOW_START = 0;
	private static final int    DEFAULT_RESTART_WINDOW_END = 24;
	private static final int    DEFAULT_UPDATE_INTERVAL = 0;
	private static final int    DEFAULT_DOCUMENTS_PER_INTERVAL = 4;
	private static final int    DEFAULT_RESTART_DAY_OF_WEEK = 8;
	private static final String DEFAULT_RAM_BUFFER_SIZE_MB = "48";
	
	private String searchService;
	private String fullIndexPath;
	private String fullPermanentIndexPath;
	private String fullTempIndexPath;
	private String fullTempSpellCheckPath;
	private long indexInterval;
	private boolean generateAtStartup;
	private int maxHits;
	private int maxResults;
	private List<String> fileBlackList;
	private List<String> customFileBlackList;

	private int folderPoolSize;
	private int restartWindowStart;
	private int restartWindowEnd;
	private long updateInterval;
	private int documentsPerInterval;
	private int restartDayOfWeek;
	private boolean pptFileEnabled;
	private boolean excelFileEnabled;
	private boolean pdfFileEnabled;
	private boolean pdfTextBuffering;
	private boolean pdfExternalIndexer;
	private String pdfExternalIndexerCmd;
	private boolean isSpellCheckEnabled;
	private String fullPdfTextBufferPath;
	private List<String> fileSizeSuffixes;

	private long maxFileSize;
	private List<Long> repositoryBlackList;
	
	private double ramBufferSizeMB;
	private boolean useCompoundFile;
	
	
	/**
	 * [used by spring]
	 */
	private SearchModule() {
	}
	
	/**
	 * [used by spring]
	 * @param fileSizeSuffixes
	 */
	public void setFileSizeSuffixes(List<String> fileSizeSuffixes) {
		this.fileSizeSuffixes = fileSizeSuffixes;
	}
	/**
	 * [used by spring]
	 * @param fileBlackList
	 */
	public void setFileBlackList(List<String> fileBlackList) {
		this.fileBlackList = fileBlackList;
	}
	
	 /**
	 * [used by spring]
	 * @param fileBlackList
	 */
	public void setRepositoryBlackList(List<Long> repositoryBlackList) {
		this.repositoryBlackList = repositoryBlackList;
	}

	/**
	 * Read config-parameter from configuration and store this locally.
	 */
	@Override
	public void initDefaultProperties() {
		log.debug("init start...");

		searchService = getStringConfigParameter(CONF_SEARCH_SERVICE, "enabled", false);
		
		String indexPath = getStringConfigParameter(CONF_INDEX_PATH, "/tmp", false);
		String permanentIndexPath = getStringConfigParameter(CONF_PERMANENT_INDEX_PATH, "/sidx", false);

		log.debug("init indexPath=" + indexPath);
		String tempIndexPath = getStringConfigParameter(CONF_TEMP_INDEX_PATH, "/tmp", false);
		String tempSpellCheckPath = getStringConfigParameter(CONF_TEMP_SPELL_CHECK_PATH, "/tmp",false);
		String tempPdfTextBufferPath = getStringConfigParameter(CONF_TEMP_PDF_TEXT_BUF_PATH, "/tmp", false);
    
		fullIndexPath = buildPath(indexPath);
		fullPermanentIndexPath = buildPath(permanentIndexPath);
		
    fullTempIndexPath = buildPath(tempIndexPath);
    fullTempSpellCheckPath = buildPath(tempSpellCheckPath);
    fullPdfTextBufferPath = buildPath(tempPdfTextBufferPath);

    generateAtStartup = getBooleanConfigParameter(CONF_GENERATE_AT_STARTUP, true);
    indexInterval = getIntConfigParameter(CONF_INDEX_INTERVAL, DEFAULT_INDEX_INTERVAL);
    maxHits = getIntConfigParameter(CONF_MAX_HITS, DEFAULT_MAX_HITS);
    maxResults = getIntConfigParameter(CONF_MAX_RESULTS, DEFAULT_MAX_RESULTS);
    folderPoolSize = getIntConfigParameter(CONF_FOLDER_POOL_SIZE, DEFAULT_FOLDER_POOL_SIZE);
    restartWindowStart = getIntConfigParameter(CONF_RESTART_WINDOW_START, DEFAULT_RESTART_WINDOW_START);
    restartWindowEnd = getIntConfigParameter(CONF_RESTART_WINDOW_END, DEFAULT_RESTART_WINDOW_END);
    updateInterval = getIntConfigParameter(CONF_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL);
    documentsPerInterval = getIntConfigParameter(CONF_DOCUMENTS_PER_INTERVAL, DEFAULT_DOCUMENTS_PER_INTERVAL);
    restartDayOfWeek = getIntConfigParameter(CONF_RESTART_DAY_OF_WEEK, DEFAULT_RESTART_DAY_OF_WEEK);
    pptFileEnabled = getBooleanConfigParameter(CONF_PPT_FILE_ENABLED, true);	
    excelFileEnabled = getBooleanConfigParameter(CONF_EXCEL_FILE_ENABLED, true);
    pdfFileEnabled = getBooleanConfigParameter(CONF_PDF_FILE_ENABLED, true);
    pdfTextBuffering = getBooleanConfigParameter(CONF_PDF_TEXT_BUFFERING, true);
    pdfExternalIndexer = getBooleanConfigParameter(CONF_PDF_EXTERNAL_INDEXER, false);
    pdfExternalIndexerCmd = getStringConfigParameter(CONF_PDF_EXTERNAL_INDEXER_CMD, "convertpdf.sh", false);
    
    isSpellCheckEnabled = getBooleanConfigParameter(CONF_SPELL_CHECK_ENABLED, true);
    maxFileSize = Integer.parseInt(getStringConfigParameter(CONF_MAX_FILE_SIZE, "0", false));
    ramBufferSizeMB = Double.parseDouble(getStringConfigParameter(CONF_RAM_BUFFER_SIZE_MB, DEFAULT_RAM_BUFFER_SIZE_MB, false));
    useCompoundFile = getBooleanConfigParameter(CONF_USE_COMPOUND_FILE, false);
	}
	
	private String buildPath(String path) {
		File f = new File(path);
		if(f.isAbsolute() && (f.exists() || f.mkdirs())) {
			return path;
		}
		return FolderConfig.getCanonicalTmpDir() + File.separator + path;
	}
	
	@Override
	public void init() {
		//black list
		String blackList = getStringPropertyValue(CONF_FILE_BLACK_LIST, true);
		if(StringHelper.containsNonWhitespace(blackList)) {
			String[] files = blackList.split(",");
			if(customFileBlackList == null) {
				customFileBlackList = new ArrayList<String>();
			} else {
				customFileBlackList.clear();
			}
			for(String file:files) {
				if(!customFileBlackList.contains(file) && !fileBlackList.contains(file)) {
					customFileBlackList.add(file);
				}
			}
		}
		
		//ppt enabled
		String pptEnabled = getStringPropertyValue(CONF_PPT_FILE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(pptEnabled)) {
			pptFileEnabled = "true".equals(pptEnabled);
		}
		
		//excel enabled
		String excelEnabled = getStringPropertyValue(CONF_EXCEL_FILE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(excelEnabled)) {
			excelFileEnabled = "true".equals(excelEnabled);
		}
		
		//pdf enabled
		String pdfEnabled = getStringPropertyValue(CONF_PDF_FILE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(pdfEnabled)) {
			pdfFileEnabled = "true".equals(pdfEnabled);
		}
		
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public void setCustomFileBlackList(List<String> files) {
		StringBuilder sb = new StringBuilder();
		for(String file:files) {
			if(sb.length() > 0) sb.append(',');
			sb.append(file);
		}
		setStringProperty(CONF_FILE_BLACK_LIST, sb.toString(), true);
	}
	
	public boolean isSearchServiceEnabled() {
		return "enabled".equals(searchService);
	}

	/**
	 * @return Absolute file path for the full-index.
	 */
	public String getFullIndexPath() {
		return fullIndexPath;
	}
	
	/**
	 * @return Return the path to the permanent index
	 */
	public String getFullPermanentIndexPath() {
		return fullPermanentIndexPath;
	}

	/**
	 * @return Absolute file path for the temporally index used to generate new an index.
	 */
	public String getFullTempIndexPath() {
		return fullTempIndexPath;
	}

	/**
	 * @return Absolute file path for the temporally spell-check index used to specll check search queries.
	 */
	public String getSpellCheckDictionaryPath() {
		return fullTempSpellCheckPath;
	}


	/**
	 * @return TRUE: Generate a full-index after system start.
	 */
	public boolean getGenerateAtStartup() {
		return generateAtStartup;
	}

	/**
	 * @return Sleep time in millisecond between indexing documents.
	 */
	public long getIndexInterval() {
		return indexInterval;
	}

	/**
	 * @return Number of maximal hits before filtering of results for a certain search-query.
	 */
	public int getMaxHits() {
		return maxHits;
	}

	/**
	 * @return Number of maximal displayed results for a certain search-query.
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * @return Space seperated list of non indexed files.
	 */
	public List<String> getFileBlackList() {
		List<String> list = new ArrayList<String>();
		if(fileBlackList != null) {
			list.addAll(fileBlackList);
		}
		if(customFileBlackList != null) {
			list.addAll(customFileBlackList);
		}
		return list;
	}
	
	public List<String> getCustomFileBlackList() {
		return customFileBlackList;
	}

	/**
	 * @return Number of FolderIndexWorker in Multithreaded mode.
	 */
	public int getFolderPoolSize() {
		return folderPoolSize;
	}

	/**
	 * @return Start hour for restart-window.
	 */
	public int getRestartWindowStart() {
		return restartWindowStart;
	}

	/**
	 * @return End hour for restart-window.
	 */
	public int getRestartWindowEnd() {
		return restartWindowEnd;
	}

	/**
	 * @return Time in millisecond between running updater.
	 */
	public long getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * @return Number of indexed documents before sleeping during indexing.
	 */
	public int getDocumentsPerInterval() {
		return documentsPerInterval ;
	}

	/**
	 * @return Restart only at this da of the week.
	 */
	public int getRestartDayOfWeek() {
		return restartDayOfWeek;
	}

	/**
	 * @return TRUE: index Power-Point-files.
	 */
	public boolean getPptFileEnabled() {
		return pptFileEnabled;
	}
	
	public void setPptFileEnabled(boolean enabled) {
		String value = Boolean.toString(enabled);
		this.setStringProperty(CONF_PPT_FILE_ENABLED, value, true);
	}
	
	/**
	 * @return TRUE: index Excel-files.
	 */
	public boolean getExcelFileEnabled() {
		return excelFileEnabled;
	}
	
	public void setExcelFileEnabled(boolean enabled) {
		String value = Boolean.toString(enabled);
		this.setStringProperty(CONF_EXCEL_FILE_ENABLED, value, true);
	}
	
	public boolean getPdfFileEnabled() {
		return pdfFileEnabled;
	}
	
	public void setPdfFileEnabled(boolean enabled) {
		String value = Boolean.toString(enabled);
		this.setStringProperty(CONF_PDF_FILE_ENABLED, value, true);
	}

	/**
	 * @return TRUE: store a temporary text file with content of extracted PDF text.
	 */
	public boolean getPdfTextBuffering() {
		return pdfTextBuffering;
	}

	public boolean isPdfExternalIndexer() {
		return pdfExternalIndexer;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getPdfExternalIndexerCmd() {
		return pdfExternalIndexerCmd;
	}

	/**
	 * @return TRUE: Spell-checker is enabled.
	 */
	public boolean getSpellCheckEnabled() {
		return isSpellCheckEnabled;
	}

	public String getPdfTextBufferPath() {
		return fullPdfTextBufferPath;
	}

	public List<String> getFileSizeSuffixes() {
		return fileSizeSuffixes;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}
	
	public List<Long> getRepositoryBlackList() {
		return repositoryBlackList;
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	public double getRAMBufferSizeMB() {
		return ramBufferSizeMB;
	}

	public boolean getUseCompoundFile() {
		return useCompoundFile;
	}
 
}
