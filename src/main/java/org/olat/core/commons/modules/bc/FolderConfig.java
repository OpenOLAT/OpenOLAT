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
* <p>
*/ 

package org.olat.core.commons.modules.bc;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.version.FolderVersioningConfigurator;

/**
 * Initial Date:  18.12.2002
 *
 * @author Mike Stock
 */
public class FolderConfig {

	// briefcase configuration default parameters
	/** LIMITULKB_DEFAULT configuration default value */
	private static final long LIMITULKB_DEFAULT = 20 * 1024l;
	/** QUOTAKB_DEFAULT configuration default value */
	private static final int QUOTAKB_DEFAULT = 20 * 1024;
	/** FOLDERROOT_DEFAULT configuration default value */
	private static final String FOLDERROOT_DEFAULT = "bcroot";
	/** USERHOMES_DEFAULT configuration default value */
	private static final String USERHOMES_DEFAULT = "/homes";
	/** GROUPHOMES_DEFAULT configuration default value */
	private static final String GROUPHOMES_DEFAULT = "/groups";
	/** USERHOMEPAGES_DEFAULT configuration default value */
	private static final String USERHOMEPAGES_DEFAULT = "/homepages";
	/** REPOSITORY_DEFAULT configuration default value */
	private static final String REPOSITORY_DEFAULT = "/repository";
	/** REPOSITORY_DEFAULT configuration default value */
	private static final String RESOURCES_DEFAULT = "/resources";

	private static long limitULKB = LIMITULKB_DEFAULT;
	private static long quotaKB = QUOTAKB_DEFAULT;
	private static long editFileSizeLimit;
	private static final String META_DIR = "/.meta";
	private static final String TMP_DIR = "/tmp";
	private static final String VERSION_DIR = "/.version";
	private static FolderVersioningConfigurator versioningConfigurator;
	private static boolean sendDocumentToExtern;
	private static boolean sendDocumentLinkOnly;
	
	
	/*
	 * IMPORTANT: Prepend webdav servlet mapping to the root path.
	 * This is necessary because webdav servlet only addressess
	 * ressources with its servlet mapping prepended.
	 */
	private static String folderRoot = FOLDERROOT_DEFAULT;
	private static String userHomes = USERHOMES_DEFAULT;
	private static String groupHomes = GROUPHOMES_DEFAULT;
	private static String userHomePages = USERHOMEPAGES_DEFAULT;
	private static String repositoryHome = REPOSITORY_DEFAULT;
	private static String resourcesHome = RESOURCES_DEFAULT;

	/** CONFIG_KEY_FOLDERPATH configuration key */
	public static final String CONFIG_KEY_FOLDERPATH = "folderpath";
	/** CONFIG_KEY_FOLDERDISPLAYNAME configuration key */
	public static final String CONFIG_KEY_FOLDERDISPLAYNAME = "folderdisplayname";
	/** CONFIG_KEY_FOLDERDISPLAYNAME configuration key */
	public static final String CONFIG_KEY_WEBDAVLINK = "webdavlink";

	/**
	 * Default constructor.
	 */
	public FolderConfig() { super(); }
	
	/* =================  Getters  ================ */

	/**
	 * Returns the maxULBytes.
	 * @return long
	 */
	public static long getLimitULKB() {
		return limitULKB;
	}

	/**
	 * Returns the userHomes.
	 * @return String
	 */
	public static String getUserHomes() {
		return userHomes;
	}
	
	/**
	 * Returns the userHome.
	 * @param username an olat username
	 * @return String
	 */
	public static String getUserHome(String username) {
		return getUserHomes()+"/"+username;
	}

	/**
	 * Returns the groupHomes.
	 * @return String
	 */
	public static String getGroupHomes() {
		return groupHomes;
	}

	/**
	 * Returns the userHomePages.
	 * @return String
	 */
	public static String getUserHomePages() {
		return userHomePages;
	}
	
	/**
	 * Returns the userHomePage.
	 * @param username an olat username
	 * @return String
	 */
	public static String getUserHomePage(String username) {
		return getUserHomePages()+"/"+username;
	}
	
	
	/**
	 * Returns the repository home.
	 * @return String
	 */
	public static String getRepositoryHome() {
		return repositoryHome;
	}
	
	/**
	 * Return the resources home
	 * @return
	 */
	public static String getResourcesHome() {
		return resourcesHome;
	}

	/**
	 * Returns briefcase homes root.
	 * @return String
	 */
	public static String getCanonicalRoot() {
		return folderRoot;
	}
	
	public static Path getCanonicalRootPath() {
		return Paths.get(folderRoot);
	}

	/**
	 * Returns canonical tmp dir.
	 * @return String
	 */
	public static String getCanonicalTmpDir() {
		return getCanonicalRoot() + TMP_DIR;
	}
	
	public static Path getCanonicalTmpPath() {
		return Paths.get(getCanonicalRoot(), TMP_DIR);
	}
	
	/**
	 * Returns relative tmp dir.
	 * @return String
	 */
	public static String getRelativeTmpDir() {
		return TMP_DIR;
	}
	
	/**
	 * Returns the max File-Size in _Bytes_ that is allowed for online-editing.
	 * (plaintext and html wysiwyg editor)
	 * 
	 * @return
	 */
	public static long getMaxEditSizeLimit(){
		return editFileSizeLimit;
	}


	/* =================  Setters  ================ */

	/**
	 * Sets the maxULBytes.
	 * @param newLimitULKB The maxULBytes to set
	 */
	public static void setLimitULKB(long newLimitULKB) {
		limitULKB = newLimitULKB;
	}

	/**
	 * 
	 */
	public static void setMaxEditSizeLimit(long sizelimit) {
		editFileSizeLimit = sizelimit;
	}
	
	/**
	 * Sets the userHomes.
	 * @param newUserHomes The userHomes to set
	 */
	public static void setUserHomes(String newUserHomes) {
		userHomes = newUserHomes.replace('\\', '/');
	}

	/**
	 * Sets the groupHomes.
	 * @param newGroupHomes The groupHomes to set
	 */
	public static void setGroupHomes(String newGroupHomes) {
		groupHomes = newGroupHomes.replace('\\', '/');
	}

	/**
	 * Sets the repositoryHomes.
	 * @param newRepositoryHome The repositoryHomes to set
	 */
	public static void setRepositoryHome(String newRepositoryHome) {
		repositoryHome = newRepositoryHome.replace('\\', '/');
	}

	/**
	 * Sets the folderRoot.
	 * @param newFolderRoot The newFolderRoot to set
	 */
	public static void setFolderRoot(String newFolderRoot) {
		folderRoot = newFolderRoot.replace('\\', '/');
	}
	
	/**
	 * Allow to send document to extern e-mail addresses
	 * @param sendDocumentToExtern_
	 */
	public static void setSendDocumentToExtern(boolean sendDocumentToExtern_) {
		sendDocumentToExtern = sendDocumentToExtern_;
	}
	
	/**
	 * Restrict sending e-mail to links to the documents (which enforce login for
	 * the recipient)
	 * @param sendDocumentLinkOnly_
	 */
	public static void setSendDocumentLinkOnly(boolean sendDocumentLinkOnly_) {
		sendDocumentLinkOnly = sendDocumentLinkOnly_;
	}

	/**
	 * @return the canonical path to the meta root directory.
	 */
	public static String getCanonicalMetaRoot() {
		return getCanonicalRoot() + META_DIR;
	}
	
	public static Path getCanonicalMetaRootPath() {
		return Paths.get(getCanonicalRoot(), META_DIR);
	}
	
	/**
	 * @return the canonical path to the version root directory
	 */
	public static String getCanonicalVersionRoot() {
		return getCanonicalRoot() + VERSION_DIR;
	}
	
	public static Path getCanonicalVersionRootPath() {
		return Paths.get(getCanonicalRoot(), VERSION_DIR);
	}

	/**
	 * @return the canonical path to the repository root directory.
	 */
	public static String getCanonicalRepositoryHome() {
		return getCanonicalRoot() + getRepositoryHome();
	}
	
	public static String getCanonicalResourcesHome() {
		return getCanonicalRoot() + getResourcesHome();
	}

	/**
	 * @return default quota in KB
	 */
	public static long getDefaultQuotaKB() {
		return quotaKB;
	}

	/**
	 * @param l
	 */
	public static void setDefaultQuotaKB(long l) {
		quotaKB = l;
	}
	
	/**
	 * Allow to send document to extern e-mail addresses
	 * @return true to allow extern e-mail address
	 */
	public static boolean getSendDocumentToExtern() {
		return sendDocumentToExtern;
	}
	
	/**
	 * Restrict sending e-mail to links to the documents (which enforce login for
	 * the recipient)
	 * @return true to restrict to links only
	 */
	public static boolean getSendDocumentLinkOnly() {
		return sendDocumentLinkOnly;
	}
	
	public static FolderVersioningConfigurator getVersioningConfigurator() {
		return versioningConfigurator;
	}

	public static void setVersioningConfigurator(FolderVersioningConfigurator versioningConfigurator) {
		FolderConfig.versioningConfigurator = versioningConfigurator;
	}

	/**
	 * @return -1 if the number of revisions for the file is unlimited; 0 if versions are not allowed;
	 * 	1 - n is the maximum allowed number of revisions
	 */
	public static int versionsAllowed(String relPath) {
		if(versioningConfigurator == null) {
			return 0;
		}
		return versioningConfigurator.versionAllowed(relPath);
	}
	
	/**
	 * @return true if versioning is enabled for the container
	 */
	public static boolean versionsEnabled(VFSContainer container) {
		if(versioningConfigurator == null) {
			return false;
		}
		return versioningConfigurator.versionEnabled(container);
	}
}
