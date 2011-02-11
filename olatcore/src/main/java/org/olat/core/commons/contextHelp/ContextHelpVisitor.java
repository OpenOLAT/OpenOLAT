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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.olat.core.logging.LogDelegator;
import org.olat.core.util.FileVisitor;

/**
 * Description:<br>
 * This visitor adds all context help pages on the classpath to a lookup index
 * <p>
 * Initial Date: 30.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 * 
 * @author gnaegi
 */
class ContextHelpVisitor extends LogDelegator implements FileVisitor {
	private static final String CHELP_DIR = "_chelp";
	private String basePath;
	private Map<String, String> contextHelpPagesLegacyLookupIndex;
	private Set<String> allContextHelpPages;
	private Set<String> blacklist;

	/**
	 * Packag scope constructor
	 * 
	 * @param basePathConfig
	 *            The base path to be subtracted from the file name to get the
	 *            classname
	 * @param contextHelpPagesLegacyLookupIndex
	 *            hash map that offers a way to lookup the package of a page
	 *            when only the page name is known (used for legacy help pages)
	 * @param allContextHelpPages
	 *            set that contains all help pages pathes
	 * @param blacklist
	 *            Set of page pathes that should not be indexed
	 */
	ContextHelpVisitor(String basePathConfig,
			Map<String, String> contextHelpPagesLegacyLookupIndex,
			Set<String> allContextHelpPages, Set<String> blacklist) {
		this.basePath = basePathConfig;
		this.contextHelpPagesLegacyLookupIndex = contextHelpPagesLegacyLookupIndex;
		this.allContextHelpPages = allContextHelpPages;
		this.blacklist = blacklist;
	}

	/**
	 * @see org.olat.core.util.FileVisitor#visit(java.io.File)
	 */
	public void visit(File file) {
		// collect all help files a drop-down list later;
		if (file.isFile()) { // regular file
			File parentFile = file.getParentFile();
			// 1) Check for files on source path
			if (parentFile.getName().equals(CHELP_DIR)) {
				String toBeChechedkFilName = file.getName();
				File grandParentFile = parentFile.getParentFile();
				String pPath = grandParentFile.getPath();
				// add one for the '/'
				String classPath = pPath.substring(basePath.length() + 1); 
				char c = File.separatorChar;
				classPath = classPath.replace(c, '.');
				addHelpPageToList(toBeChechedkFilName, classPath);

			// 2) Check for files within jars
			} else if (file.getName().endsWith(".jar")) {
				JarFile jar;
				try {
					jar = new JarFile(file);
					Enumeration<JarEntry> jarEntries = jar.entries();
					// Check in all jars - maybe we have context help files in
					// jar resources
					while (jarEntries.hasMoreElements()) {
						JarEntry jarEntry = jarEntries.nextElement();
						String jarEntryName = jarEntry.getName();
						// TODO:FG: check for \\ instead of / for windows, rare case
						if (jarEntryName.indexOf("/" + CHELP_DIR + "/") != -1) {
							if (!jarEntry.isDirectory()) {
								// Add file from jar to help page list
								// extract class name without trailing slashes
								int staticDirPos = jarEntryName.indexOf(CHELP_DIR);
								String packageName = jarEntryName.substring(0, staticDirPos - 1);
								packageName = packageName.replace("/", ".");
								String fileName = jarEntryName.substring(staticDirPos + CHELP_DIR.length() + 1);
								addHelpPageToList(fileName, packageName);							}
						}
					}
				} catch (IOException e) {
					logError(
							"Error while searching for context help file in a jar",
							e);
				}
			}
		}
		// else ignore
	}

	private void addHelpPageToList(String toBeChechedkFilName, String classPath) {
		String combinedPath = classPath + ":" + toBeChechedkFilName;
		// on the blacklist of not to be indexed pages?
		for (String blackListItem : blacklist) {
			if (combinedPath.startsWith(blackListItem))
				return;			
		}
		// already in the lookup index?
		if (allContextHelpPages.contains(combinedPath)) 
			return;
		// no, so add it to list of known help pages
		allContextHelpPages.add(combinedPath);
		// and add it also to lookup map
		if (contextHelpPagesLegacyLookupIndex.containsKey(toBeChechedkFilName)) {
			logDebug("Detected two help pages with the same name::" 
					+ toBeChechedkFilName 
					+ ", in package::" 
					+ contextHelpPagesLegacyLookupIndex.get(toBeChechedkFilName) 
					+ " and in package::" 
					+ classPath 
					+ ". Keeping the first one, legacy calls to second one will not work. To solve this, rename each help page to a unique name", null);
		} else {
			contextHelpPagesLegacyLookupIndex.put(toBeChechedkFilName, classPath);					
		}
	}
	
}
