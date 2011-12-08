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
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/ 

package org.olat.core.commons.contextHelp;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3>
 * The context help module offers configuration methods for the context help system
 * <p>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class ContextHelpModule extends AbstractOLATModule implements Destroyable {
	public static final String CHELP_DIR = "/_chelp/";
	//fxdiff FXOLAT-185:fix loading of files in jar
	public static final String CHELP_STATIC_DIR = CHELP_DIR + "_static/";
	
	private static final String CONFIG_CONTEXTHELP_ENABLED = "contextHelpEnabled";
	private static final String CONFIG_RATING_ENABLED = "ratingEnabled";

	private static boolean isContextHelpEnabled = true;
	private static boolean isContextRatingEnabled = true;
	
	
	// Initilize context help lookup cache - VM scope, clustersave
	private static final Map<String,String> contextHelpPagesLegacyLookupIndex = new HashMap<String,String>();
	private static final Set<String> allContextHelpPages = new HashSet<String>();
	private static Set blacklist;
	
	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// load configuration
		isContextHelpEnabled = getBooleanConfigParameter(CONFIG_CONTEXTHELP_ENABLED, true);
		isContextRatingEnabled = getBooleanConfigParameter(CONFIG_RATING_ENABLED, true);
		
		// search for help pages
		if (isContextHelpEnabled) {			
			refreshContextHelpIndex();
		}
	}
	
	@Override
	protected void initDefaultProperties() {
		// context help has no user configurable properties so far
	}


	@Override
	protected void initFromChangedProperties() {
		// context help has no user configurable properties so far
	}

	/**
	 * [used by spring]
	 * @param persistedProperties
	 */
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
	
	/**
	 * [used by spring]
	 * @param blackist
	 */
	public void setCHelpBlacklist(Set blackist) {
		blacklist = blackist;
	}
	
	/**
	 * 
	 * @see org.olat.core.configuration.Destroyable#destroy()
	 */
	public void destroy() {
		contextHelpPagesLegacyLookupIndex.clear();
		allContextHelpPages.clear();
	}
	
	
	public static synchronized void refreshContextHelpIndex() {
		contextHelpPagesLegacyLookupIndex.clear();
		allContextHelpPages.clear();
		
		// 1)  Search for context help files from compiled web app classpath
		String srcPath = WebappHelper.getContextRoot() + "/WEB-INF/classes";
		ContextHelpVisitor srcVisitor = new ContextHelpVisitor(srcPath, contextHelpPagesLegacyLookupIndex, allContextHelpPages, blacklist);
		FileUtils.visitRecursively(new File(srcPath), srcVisitor);
		// 2) Search in libs directory
		String libDirPath = WebappHelper.getContextRoot() + "/WEB-INF/lib";
		ContextHelpVisitor libVisitor = new ContextHelpVisitor(libDirPath, contextHelpPagesLegacyLookupIndex, allContextHelpPages, blacklist);
		FileUtils.visitRecursively(new File(libDirPath), libVisitor);		
	}


	/**
	 * @return true: use context help; false: don't show context help
	 */
	public static boolean isContextHelpEnabled() {
		return isContextHelpEnabled;
	}

	/**
	 * @return true: allow users to rate help pages; false: don't allow users to rate help pages
	 */
	public static boolean isContextRatingEnabled() {
		return isContextRatingEnabled;
	}

	/**
	 * @return lookup map that can be used to find the package of a page when
	 *         only the page name is known (legacy mode)
	 */
	public static Map<String, String> getContextHelpPagesLegacyLookupIndex() {
		return contextHelpPagesLegacyLookupIndex;
	}

	/**
	 * @return Set containing all help pages using the combined format:
	 *         package.name:page_name.html
	 */
	public static Set<String> getAllContextHelpPages() {
		return allContextHelpPages;
	}

	/**
	 * Create a set of chelp pages that should not be indexed at all. This is
	 * usefull to not show chelp pages of modules that are not enabled
	 * 
	 * @return
	 * 
	 * log.info("Excluding chelp path::" + strLine + " - found on blacklist::" + blackListFile.getAbsolutePath());		
	 */
	
}
