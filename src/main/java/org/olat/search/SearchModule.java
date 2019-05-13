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

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 *      
 * Initial Date: 15.06.200g <br>
 * @author Christian Guretzki
 */
@Service("searchModule")
public class SearchModule extends AbstractSpringModule {
	private static final Logger log = Tracing.createLoggerFor(SearchModule.class);
	
	// Definitions config parameter names in module-config
	public static final String CONF_SEARCH_SERVICE = "searchService";
	public static final String CONF_INDEX_PATH = "indexPath";
	public static final String CONF_PERMANENT_INDEX_PATH = "permanentIndexPath";
	public static final String CONF_TEMP_INDEX_PATH = "tempIndexPath";
	public static final String CONF_TEMP_SPELL_CHECK_PATH = "tempSpellCheckPath";
	public static final String CONF_GENERATE_AT_STARTUP = "generateIndexAtStartup";
	private static final String CONF_PPT_FILE_ENABLED = "pptFileEnabled";
	private static final String CONF_EXCEL_FILE_ENABLED = "excelFileEnabled";
	private static final String CONF_PDF_FILE_ENABLED = "pdfFileEnabled";
	private static final String CONF_FILE_BLACK_LIST = "fileBlackList";
	private static final String CONF_GUEST_ENABLED = "search.guest.enabled";
	
	@Value("${search.service:enabled}")
	private String searchService;
	
	@Value("${search.guest.enabled:false}")
	private boolean guestEnabled;
	
	@Value("${search.index.tempIndex:/tmp}")
	private String tempIndexPath;
	@Value("${search.index.tempSpellcheck:/tmp}")
	private String tempSpellCheckPath;
	@Value("${search.index.pdfBuffer:/tmp}")
	private String tempPdfTextBufferPath;

	@Value("${search.index.path:/tmp}")
	private String indexPath;
	@Value("${search.permanent.index.path:/sidx}")
	private String permanentIndexPath;
	
	private String fullIndexPath;
	private String fullPermanentIndexPath;
	private String fullTempIndexPath;
	private String fullTempSpellCheckPath;
	
	private long indexInterval = 0;
	@Value("${generate.index.at.startup:true}")
	private boolean generateAtStartup;
	private int maxHits = 1000;
	private int maxResults = 100;

	@Value("${search.timeout:15}")
	private int searchTimeout;
	@Value("${search.folder.pool.size:3}")
	private int folderPoolSize;
	@Value("${restart.window.start}")
	private int restartWindowStart;
	@Value("${restart.window.end}")
	private int restartWindowEnd;
	private long updateInterval = 0;
	private int documentsPerInterval = 4;
	private int restartDayOfWeek = 0;
	private boolean pptFileEnabled = true;
	private boolean excelFileEnabled = true;
	private boolean pdfFileEnabled = true;
	private boolean pdfTextBuffering = true;
	@Value("${search.pdf.external:false}")
	private boolean pdfExternalIndexer;
	@Value("${search.pdf.external.command:convertpdf.sh}")
	private String pdfExternalIndexerCmd;
	private boolean isSpellCheckEnabled = true;
	private String fullPdfTextBufferPath;

	private long maxFileSize = 10485760;

	@Value("${search.ram.buffer.size:16}")
	private double ramBufferSizeMB;
	private boolean useCompoundFile = false;
	
	@Autowired @Qualifier("fileSizeSuffixes")
	private ArrayList<String> fileSizeSuffixes;
	@Autowired @Qualifier("fileBlackList")
	private ArrayList<String> fileBlackList;
	private ArrayList<String> customFileBlackList;
	@Autowired @Qualifier("repositoryBlackList")
	private ArrayList<Long> repositoryBlackList;
	
	@Autowired
	private FolderModule folderModule;
	
	@Autowired
	public SearchModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	/**
	 * Read config-parameter from configuration and store this locally.
	 */
	@Override
	public void initDefaultProperties() {
		log.debug("init start...");
		super.initDefaultProperties();
		
		log.debug("init indexPath=" + indexPath);
		fullIndexPath = buildPath(indexPath);
		fullPermanentIndexPath = buildPath(permanentIndexPath);
		
		fullTempIndexPath = buildPath(tempIndexPath);
		fullTempSpellCheckPath = buildPath(tempSpellCheckPath);
		fullPdfTextBufferPath = buildPath(tempPdfTextBufferPath);
	}
	
	private String buildPath(String path) {
		File f = new File(path);
		if(f.isAbsolute() && (f.exists() || f.mkdirs())) {
			return path;
		}
		return folderModule.getCanonicalTmpDir() + File.separator + path;
	}
	
	@Override
	public void init() {
		//black list
		String blackList = getStringPropertyValue(CONF_FILE_BLACK_LIST, true);
		if(StringHelper.containsNonWhitespace(blackList)) {
			String[] files = blackList.split(",");
			if(customFileBlackList == null) {
				customFileBlackList = new ArrayList<>();
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
		
		//guest enabled
		String guestEnabledObj = getStringPropertyValue(CONF_GUEST_ENABLED, true);
		if(StringHelper.containsNonWhitespace(guestEnabledObj)) {
			guestEnabled = "true".equals(guestEnabledObj);
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
	
	public int getSearchTimeout() {
		return searchTimeout;
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
		List<String> list = new ArrayList<>();
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

	public double getRAMBufferSizeMB() {
		return ramBufferSizeMB;
	}
	
	public void setRAMBufferSizeMB(double ramBufferSizeMB) {
		this.ramBufferSizeMB = ramBufferSizeMB;
	}

	public boolean getUseCompoundFile() {
		return useCompoundFile;
	}
	
	public boolean isGuestEnabled() {
		return guestEnabled;
	}
	
	public void setGuestEnabled(boolean enabled) {
		guestEnabled = enabled;
		setStringProperty(CONF_GUEST_ENABLED, enabled ? "true" : "false", true);
	}
	
	public boolean isSearchAllowed(Roles roles) {
		if(roles.isGuestOnly()) {
			return isGuestEnabled();
		}
		return true;
	}
}
