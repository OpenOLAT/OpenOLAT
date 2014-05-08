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

package org.olat.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Initial Date:  19.05.2005
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class RepositoryEntryImportExport {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntryImportExport.class);

	private static final String CONTENT_FILE = "repo.zip";
	private static final String PROPERTIES_FILE = "repo.xml";
	private static final String PROP_ROOT = "RepositoryEntryProperties";
	private static final String PROP_SOFTKEY = "Softkey";
	private static final String PROP_RESOURCENAME = "ResourceName";
	private static final String PROP_DISPLAYNAME = "DisplayName";
	private static final String PROP_DECRIPTION = "Description";
	private static final String PROP_INITIALAUTHOR = "InitialAuthor";
	private boolean propertiesLoaded = false;

	private RepositoryEntry re;
	private File baseDirectory;
	private RepositoryEntryImport repositoryProperties;
	
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
		// save repository entry properties
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(new File(baseDirectory, PROPERTIES_FILE));
			XStream xstream = getXStream();
			xstream.toXML(new RepositoryEntryImport(re), fOut);
		} catch (IOException ioe) {
			throw new OLATRuntimeException("Error writing repo properties.", ioe);
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
		MediaResource mr = rh.getAsMediaResource(re.getOlatResource(), false);
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(new File(baseDirectory, CONTENT_FILE));
			IOUtils.copy(mr.getInputStream(), fOut);
		} catch (IOException fnfe) {
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
			File inputFile = new File(baseDirectory, PROPERTIES_FILE);
			XStream xstream = getXStream();
			repositoryProperties = (RepositoryEntryImport)xstream.fromXML(inputFile);
			propertiesLoaded = true;
		} catch (Exception ce) {
			throw new OLATRuntimeException("Error importing repository entry properties.", ce);
		}
	}
	
	public static RepositoryEntryImport getConfiguration(Path repoXmlPath) {
		try (InputStream in=Files.newInputStream(repoXmlPath)) {
			XStream xstream = getXStream();
			return (RepositoryEntryImport)xstream.fromXML(in);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static XStream getXStream() {
		XStream xStream = XStreamHelper.createXStreamInstance();
		xStream.alias(PROP_ROOT, RepositoryEntryImport.class);
		xStream.aliasField(PROP_SOFTKEY, RepositoryEntryImport.class, "softkey");
		xStream.aliasField(PROP_RESOURCENAME, RepositoryEntryImport.class, "resourcename");
		xStream.aliasField(PROP_DISPLAYNAME, RepositoryEntryImport.class, "displayname");
		xStream.aliasField(PROP_DECRIPTION, RepositoryEntryImport.class, "description");
		xStream.aliasField(PROP_INITIALAUTHOR, RepositoryEntryImport.class, "initialAuthor");
		return xStream;
	}

	/**
	 * @return The softkey
	 */
	public String getSoftkey() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getSoftkey();
	}
	
	/**
	 * @return The display name
	 */
	public String getDisplayName() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getDisplayname();
	}
	
	/**
	 * @return the resource name
	 */
	public String getResourceName() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getResourcename();
	}
	
	/**
	 * @return the descritpion
	 */
	public String getDescription() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getDescription();
	}
	
	/**
	 * @return the initial author
	 */
	public String getInitialAuthor() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getInitialAuthor();
	}
	
	public class RepositoryEntryImport {
		private String softkey;
		private String resourcename;
		private String displayname;
		private String description;
		private String initialAuthor;
		
		public RepositoryEntryImport() {
			//
		}
		
		public RepositoryEntryImport(RepositoryEntry re) {
			this.softkey = re.getSoftkey();
			this.resourcename = re.getResourcename();
			this.displayname = re.getDisplayname();
			this.description = re.getDescription();
			this.initialAuthor = re.getInitialAuthor();
		}
		
		public String getSoftkey() {
			return softkey;
		}
		
		public void setSoftkey(String softkey) {
			this.softkey = softkey;
		}
		
		public String getResourcename() {
			return resourcename;
		}
		
		public void setResourcename(String resourcename) {
			this.resourcename = resourcename;
		}
		
		public String getDisplayname() {
			return displayname;
		}
		
		public void setDisplayname(String displayname) {
			this.displayname = displayname;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setDescription(String description) {
			this.description = description;
		}
		
		public String getInitialAuthor() {
			return initialAuthor;
		}
		
		public void setInitialAuthor(String initialAuthor) {
			this.initialAuthor = initialAuthor;
		}
	}
}



