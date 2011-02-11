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
* <p>
*/ 

package org.olat.repository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

import com.anthonyeden.lib.config.Configuration;
import com.anthonyeden.lib.config.ConfigurationException;
import com.anthonyeden.lib.config.Dom4jConfiguration;
import com.anthonyeden.lib.config.XMLConfiguration;

/**
 * Initial Date:  19.05.2005
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class RepositoryEntryImportExport {

	private static final String CONTENT_FILE = "repo.zip";
	private static final String PROPERTIES_FILE = "repo.xml";
	
	private static final String PROP_ROOT = "RepositoryEntryProperties";
	private static final String PROP_SOFTKEY = "Softkey";
	private static final String PROP_RESOURCENAME = "ResourceName";
	private static final String PROP_DISPLAYNAME = "DisplayName";
	private static final String PROP_DECRIPTION = "Description";
	private static final String PROP_INITIALAUTHOR = "InitialAuthor";
	
	private static final long serialVersionUID = 1L;
	private boolean propertiesLoaded = false;

	private RepositoryEntry re;
	private File baseDirectory;
	private Configuration repositoryProperties;
	
	/**
	 * Create a RepositoryEntryImportExport instance to do an export.
	 * 
	 * @param re
	 * @param baseDirecotry
	 */
	public RepositoryEntryImportExport(RepositoryEntry re, File baseDirecotry) {
		this.re = re;
		this.baseDirectory = baseDirecotry;
	}
	
	/**
	 * Create a RepositoryEntryImportExport instance to do an import.
	 * 
	 * @param baseDirecotry
	 */
	public RepositoryEntryImportExport(File baseDirecotry) {
		this.baseDirectory = baseDirecotry;
	}
	
	public boolean anyExportedPropertiesAvailable() {
		return new File(baseDirectory, PROPERTIES_FILE).exists();
	}
	
	/**
	 * Export repository entry (contents and metadata.
	 * 
	 * @return True upon success, false otherwise.
	 */
	public boolean exportDoExport() {
		exportDoExportProperties();
		return exportDoExportContent();
	}
	/**
	 * Export metadata of a repository entry to a file.
	 * Only one repository entry's metadata may be exported into a directory. The
	 * file name of the properties file will be the same for all repository entries!
	 */
	public void exportDoExportProperties() {
		Dom4jConfiguration root = new Dom4jConfiguration(PROP_ROOT);
		
		root.addChild(PROP_SOFTKEY, re.getSoftkey());
		root.addChild(PROP_RESOURCENAME, re.getResourcename());
		root.addChild(PROP_DISPLAYNAME, re.getDisplayname());
		root.addChild(PROP_DECRIPTION, re.getDescription());
		root.addChild(PROP_INITIALAUTHOR, re.getInitialAuthor());

		// save repository entry properties
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(new File(baseDirectory, PROPERTIES_FILE));
			BufferedOutputStream bos = FileUtils.getBos(fOut);
			root.save(bos);
		} catch (IOException ioe) {
			throw new OLATRuntimeException("Error writing repo properties.", ioe);
		} catch (ConfigurationException cfe) {
			throw new OLATRuntimeException("Error writing repo properties.", cfe);
		} finally {
			FileUtils.closeSafely(fOut);
		}
	}

	/**
	 * Export a repository entry referenced by a course node to the given export directory.
	 * User importReferencedRepositoryEntry to import again.
	 * @return True upon success, false otherwise.
	 * 
	 */
	public boolean exportDoExportContent() {
		// export resource
		RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
		MediaResource mr = rh.getAsMediaResource(re.getOlatResource());
		FileOutputStream fOut = null;
		try {
			//TODO:MK
			fOut = new FileOutputStream(new File(baseDirectory, CONTENT_FILE));
			FileUtils.copy(mr.getInputStream(), fOut);
		} catch (FileNotFoundException fnfe) {
			return false;
		} finally {
			FileUtils.closeSafely(fOut);
			mr.release();
		}
		return true;
	}

	/**
	 * Returns the exported repository file.
	 * 
	 * @return exported repository file
	 */
	public File importGetExportedFile() {
		return new File(baseDirectory, CONTENT_FILE);
	}
	
	/**
	 * Read previousely exported Propertiesproperties
	 */
	private void loadConfiguration() {
		try {
			repositoryProperties = new XMLConfiguration(new File(baseDirectory, PROPERTIES_FILE));
		} catch (ConfigurationException ce) {
			throw new OLATRuntimeException("Error importing repository entry properties.", ce);
		}
		if (!repositoryProperties.getName().equals(PROP_ROOT))
			throw new AssertException("Invalid repository entry properties export file. Root does not match.");
	}

	/**
	 * Returns a property of the exported repository entry.
	 * 
	 * @param key
	 * @return String representing the properties value.
	 */
	public String getProperty(String key) {
		if (!propertiesLoaded) loadConfiguration();
		return repositoryProperties.getChildValue(key);
	}
	/**
	 * @return The softkey
	 */
	public String getSoftkey() { return getProperty(PROP_SOFTKEY); }
	
	/**
	 * @return The display name
	 */
	public String getDisplayName() { return getProperty(PROP_DISPLAYNAME); }
	
	/**
	 * @return the resource name
	 */
	public String getResourceName() { return getProperty(PROP_RESOURCENAME); }
	
	/**
	 * @return the descritpion
	 */
	public String getDescription() { return getProperty(PROP_DECRIPTION); }
	
	/**
	 * @return the initial author
	 */
	public String getInitialAuthor() { return getProperty(PROP_INITIALAUTHOR); }

}
