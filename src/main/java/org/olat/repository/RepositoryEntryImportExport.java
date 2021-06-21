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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.HttpServletResponseOutputStream;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
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
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryImportExport.class);

	private static final String CONTENT_FILE = "repo.zip";
	public static final String PROPERTIES_FILE = "repo.xml";
	private static final String PROP_ROOT = "RepositoryEntryProperties";
	private static final String PROP_SOFTKEY = "Softkey";
	private static final String PROP_RESOURCENAME = "ResourceName";
	private static final String PROP_DISPLAYNAME = "DisplayName";
	private static final String PROP_DECRIPTION = "Description";
	private static final String PROP_INITIALAUTHOR = "InitialAuthor";
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.alias(PROP_ROOT, RepositoryEntryImport.class);
		xstream.aliasField(PROP_SOFTKEY, RepositoryEntryImport.class, "softkey");
		xstream.aliasField(PROP_RESOURCENAME, RepositoryEntryImport.class, "resourcename");
		xstream.aliasField(PROP_DISPLAYNAME, RepositoryEntryImport.class, "displayname");
		xstream.aliasField(PROP_DECRIPTION, RepositoryEntryImport.class, "description");
		xstream.aliasField(PROP_INITIALAUTHOR, RepositoryEntryImport.class, "initialAuthor");
		xstream.omitField(RepositoryEntryImport.class, "outer-class");
		xstream.omitField(RepositoryEntry.class, "educationalType");
		xstream.ignoreUnknownElements();
	}
	
	
	private boolean propertiesLoaded = false;

	private RepositoryEntry re;
	private File baseDirectory;
	private RepositoryEntryImport repositoryProperties;
	
	/**
	 * Create a RepositoryEntryImportExport instance to do an export.
	 * 
	 * @param re
	 * @param baseDirectory
	 */
	public RepositoryEntryImportExport(RepositoryEntry re, File baseDirectory) {
		this.re = re;
		this.baseDirectory = baseDirectory;
	}
	
	/**
	 * Create a RepositoryEntryImportExport instance to do an import.
	 * 
	 * @param baseDirectory
	 */
	public RepositoryEntryImportExport(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	
	public RepositoryEntryImportExport(File baseDirectory, String subDir) {
		this.baseDirectory = new File(baseDirectory, subDir);
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
	
	public boolean exportDoExport(String zipPath, ZipOutputStream zout) {
		exportDoExportProperties(zipPath, zout);
		return exportDoExportContent(zipPath, zout);
	}
	
	public void exportDoExportProperties(String zipPath, ZipOutputStream zout) {
		// save repository entry properties
		RepositoryEntryImport imp = new RepositoryEntryImport(re);
		RepositoryManager rm = RepositoryManager.getInstance();
		
		if (re.getEducationalType() != null) {
			String educationalTypeIdentifier = re.getEducationalType().getIdentifier();
			imp.setEducationalTypeIdentifier(educationalTypeIdentifier);
		}
		
		VFSLeaf image = rm.getImage(re);
		if(image instanceof LocalFileImpl) {
			imp.setImageName(image.getName());
			ZipUtil.addFileToZip(ZipUtil.concat(zipPath, image.getName()) , ((LocalFileImpl)image).getBasefile(), zout);
		}

		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		VFSLeaf movie = repositoryService.getIntroductionMovie(re);
		if(movie instanceof LocalFileImpl) {
			imp.setMovieName(movie.getName());
			ZipUtil.addFileToZip(ZipUtil.concat(zipPath, movie.getName()), ((LocalFileImpl)movie).getBasefile(), zout);
		}
		
		try(ShieldOutputStream fOut = new ShieldOutputStream(zout)) {
			addLicenseInformations(imp, re);
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(zipPath, PROPERTIES_FILE)));
			xstream.toXML(imp, fOut);
			zout.closeEntry();
		} catch (IOException ioe) {
			throw new OLATRuntimeException("Error writing repo properties.", ioe);
		}
	}
	
	/**
	 * Export metadata of a repository entry to a file.
	 * Only one repository entry's metadata may be exported into a directory. The
	 * file name of the properties file will be the same for all repository entries!
	 */
	public void exportDoExportProperties() {
		// save repository entry properties
		try(FileOutputStream fOut = new FileOutputStream(new File(baseDirectory, PROPERTIES_FILE))) {
			RepositoryEntryImport imp = new RepositoryEntryImport(re);
			RepositoryManager rm = RepositoryManager.getInstance();
			VFSLeaf image = rm.getImage(re);
			if(image instanceof LocalFileImpl) {
				imp.setImageName(image.getName());
				FileUtils.copyFileToDir(((LocalFileImpl)image).getBasefile(), baseDirectory, "");
				
			}

			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			VFSLeaf movie = repositoryService.getIntroductionMovie(re);
			if(movie instanceof LocalFileImpl) {
				imp.setMovieName(movie.getName());
				FileUtils.copyFileToDir(((LocalFileImpl)movie).getBasefile(), baseDirectory, "");
			}
			
			addLicenseInformations(imp, re);
			
			xstream.toXML(imp, fOut);
		} catch (IOException ioe) {
			throw new OLATRuntimeException("Error writing repo properties.", ioe);
		}
	}
	
	private void addLicenseInformations(RepositoryEntryImport imp, RepositoryEntry entry) {
		LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		ResourceLicense license = licenseService.loadLicense(entry.getOlatResource());
		if (license != null) {
			imp.setLicenseTypeKey(String.valueOf(license.getLicenseType().getKey()));
			imp.setLicenseTypeName(license.getLicenseType().getName());
			imp.setLicensor(license.getLicensor());
			imp.setLicenseText(LicenseUIFactory.getLicenseText(license));
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
		try(FileOutputStream fOut = new FileOutputStream(new File(baseDirectory, CONTENT_FILE));
				InputStream in = mr.getInputStream()) {
			if(in == null) {
				HttpServletResponse hres = new HttpServletResponseOutputStream(fOut);
				mr.prepare(hres);	
			} else {
				IOUtils.copy(in, fOut);
			}
			fOut.flush();
		} catch (IOException fnfe) {
			return false;
		} finally {
			mr.release();
		}
		return true;
	}
	
	public boolean exportDoExportContent(String zipPath, ZipOutputStream zout) {
		// export resource
		RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
		MediaResource mr = rh.getAsMediaResource(re.getOlatResource());
		try(OutputStream fOut = new ShieldOutputStream(zout);
				InputStream in = mr.getInputStream()) {
			zout.putNextEntry(new ZipEntry(ZipUtil.concat(zipPath, CONTENT_FILE)));
			if(in == null) {
				HttpServletResponse hres = new HttpServletResponseOutputStream(fOut);
				mr.prepare(hres);	
			} else {
				IOUtils.copy(in, fOut);
			}
			fOut.flush();
			zout.closeEntry();
		} catch (IOException fnfe) {
			return false;
		} finally {
			mr.release();
		}
		return true;
	}
	
	public RepositoryEntry importContent(RepositoryEntry newEntry, VFSContainer mediaContainer, Identity initialAuthor) {
		if(!anyExportedPropertiesAvailable()) return newEntry;

		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		if(StringHelper.containsNonWhitespace(getImageName())) {
			File newFile = new File(baseDirectory, getImageName());
			VFSLeaf newImage = new LocalFileImpl(newFile);
			repositoryManager.setImage(newImage, newEntry, initialAuthor);
		}
		if(StringHelper.containsNonWhitespace(getMovieName())) {
			String movieName = getMovieName();
			String extension = FileUtils.getFileSuffix(movieName);
			File newFile = new File(baseDirectory, movieName);
			try(InputStream inStream = new FileInputStream(newFile)) {
				VFSLeaf movieLeaf = mediaContainer.createChildLeaf(newEntry.getKey() + "." + extension);
				VFSManager.copyContent(inStream, movieLeaf, initialAuthor);
			} catch(IOException e) {
				log.error("", e);
			}
		}

		return setRepoEntryPropertiesFromImport(newEntry);
	}

	/**
	 * Update the repo entry property from the current import information in the database
	 * 
	 * @param newEntry
	 * @return
	 */
	public RepositoryEntry setRepoEntryPropertiesFromImport(RepositoryEntry newEntry) {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		
		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		
		importLicense(newEntry);
		
		String educationalTypeIdentifier = repositoryProperties.getEducationalTypeIdentifier();
		RepositoryEntryEducationalType educationalType = repositoryManager.getEducationalType(educationalTypeIdentifier);
		
		return repositoryManager.setDescriptionAndName(newEntry, newEntry.getDisplayname(), null,
				repositoryProperties.getAuthors(), repositoryProperties.getDescription(),
				repositoryProperties.getObjectives(), repositoryProperties.getRequirements(),
				repositoryProperties.getCredits(), repositoryProperties.getMainLanguage(),
				repositoryProperties.getLocation(), repositoryProperties.getExpenditureOfWork(), null, null, null,
				educationalType);
	}

	private void importLicense(RepositoryEntry newEntry) {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		boolean hasLicense = StringHelper.containsNonWhitespace(repositoryProperties.getLicenseTypeName());
		if (hasLicense) { 
			String licenseTypeName = repositoryProperties.getLicenseTypeName();
			LicenseType licenseType = licenseService.loadLicenseTypeByName(licenseTypeName);
			if (licenseType == null) {
				licenseType = licenseService.createLicenseType(licenseTypeName);
				licenseType.setText(repositoryProperties.getLicenseText());
				licenseService.saveLicenseType(licenseType);
			}
			ResourceLicense license = licenseService.loadOrCreateLicense(newEntry.getOlatResource());
			license.setLicenseType(licenseType);
			license.setLicensor(repositoryProperties.getLicensor());
			if (licenseService.isFreetext(licenseType)) {
				license.setFreetext(repositoryProperties.getLicenseText());
			}
			licenseService.update(license);
		}
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
		if(baseDirectory != null && baseDirectory.exists()) {
			if(baseDirectory.getName().endsWith(".zip")) {
				try(FileSystem fs = FileSystems.newFileSystem(baseDirectory.toPath(), null)) {
					Path fPath = fs.getPath("/");
					Path manifestPath = fPath.resolve("export").resolve(PROPERTIES_FILE);
					repositoryProperties = (RepositoryEntryImport)XStreamHelper.readObject(xstream, manifestPath);
				} catch(Exception e) {
					log.error("", e);
				}
			} else {
				File inputFile = new File(baseDirectory, PROPERTIES_FILE);
				if(inputFile.exists()) {
					repositoryProperties = (RepositoryEntryImport)XStreamHelper.readObject(xstream, inputFile);
				} else {
					repositoryProperties = new RepositoryEntryImport();
				}
			}
		} else {
			repositoryProperties = new RepositoryEntryImport();
		}
		propertiesLoaded = true;
	}
	
	/**
	 * Get the repo entry import metadata from the given path. E.g. usefull
	 * when reading from an unzipped archive.
	 * 
	 * @param repoXmlPath
	 * @return The RepositoryEntryImport or NULL
	 */
	public static RepositoryEntryImport getConfiguration(Path repoXmlPath) {
		try (InputStream in=Files.newInputStream(repoXmlPath)) {
			return (RepositoryEntryImport)xstream.fromXML(in);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * Get the repo entry import metadata from the given stream. E.g. usefull
	 * when reading from an ZIP file without inflating it.
	 * 
	 * @param repoMetaFileInputStream
	 * @return The RepositoryEntryImport or NULL
	 */
	public static RepositoryEntryImport getConfiguration(InputStream repoMetaFileInputStream) {
		return (RepositoryEntryImport)xstream.fromXML(repoMetaFileInputStream);
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
	
	public String getMovieName() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getMovieName();
	}
	
	public String getImageName() {
		if(!propertiesLoaded) {
			loadConfiguration();
		}
		return repositoryProperties.getImageName();
	}
	
	public class RepositoryEntryImport {
		
		private Long key;
		private String softkey;
		private String resourcename;
		private String displayname;
		private String description;
		private String initialAuthor;
		
		private String authors;
		private String mainLanguage;
		private String objectives;
		private String requirements;
		private String credits;
		private String expenditureOfWork;
		private String location;
		private String educationalTypeIdentifier;
		
		private String movieName;
		private String imageName;
		
		private String licenseTypeKey;
		private String licenseTypeName;
		private String licensor;
		private String licenseText;
		
		public RepositoryEntryImport() {
			//
		}
		
		public RepositoryEntryImport(RepositoryEntry re) {
			key = re.getKey();
			softkey = re.getSoftkey();
			resourcename = re.getResourcename();
			displayname = re.getDisplayname();
			description = re.getDescription();
			initialAuthor = re.getInitialAuthor();
			
			authors = re.getAuthors();
			mainLanguage = re.getMainLanguage();
			objectives = re.getObjectives();
			requirements = re.getRequirements();
			credits = re.getCredits();
			expenditureOfWork = re.getExpenditureOfWork();
		}
		
		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public String getMovieName() {
			return movieName;
		}

		public void setMovieName(String movieName) {
			this.movieName = movieName;
		}

		public String getImageName() {
			return imageName;
		}

		public void setImageName(String imageName) {
			this.imageName = imageName;
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

		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = authors;
		}

		public String getMainLanguage() {
			return mainLanguage;
		}

		public void setMainLanguage(String mainLanguage) {
			this.mainLanguage = mainLanguage;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getEducationalTypeIdentifier() {
			return educationalTypeIdentifier;
		}

		public void setEducationalTypeIdentifier(String educationalTypeIdentifier) {
			this.educationalTypeIdentifier = educationalTypeIdentifier;
		}

		public String getObjectives() {
			return objectives;
		}

		public void setObjectives(String objectives) {
			this.objectives = objectives;
		}

		public String getRequirements() {
			return requirements;
		}

		public void setRequirements(String requirements) {
			this.requirements = requirements;
		}

		public String getCredits() {
			return credits;
		}

		public void setCredits(String credits) {
			this.credits = credits;
		}

		public String getExpenditureOfWork() {
			return expenditureOfWork;
		}

		public void setExpenditureOfWork(String expenditureOfWork) {
			this.expenditureOfWork = expenditureOfWork;
		}

		public String getLicenseTypeKey() {
			return licenseTypeKey;
		}

		public void setLicenseTypeKey(String licenseTypeKey) {
			this.licenseTypeKey = licenseTypeKey;
		}

		public String getLicenseTypeName() {
			return licenseTypeName;
		}

		public void setLicenseTypeName(String licenseTypeName) {
			this.licenseTypeName = licenseTypeName;
		}

		public String getLicensor() {
			return licensor;
		}

		public void setLicensor(String licensor) {
			this.licensor = licensor;
		}

		public String getLicenseText() {
			return licenseText;
		}

		public void setLicenseText(String licenseText) {
			this.licenseText = licenseText;
		}
	}
}