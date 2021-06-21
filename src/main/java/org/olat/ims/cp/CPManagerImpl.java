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

package org.olat.ims.cp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.Logger;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XMLParser;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.ims.cp.objects.CPResource;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.ims.cp.ui.CPPage;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * The CP manager implementation.
 * <p>
 * In many cases, method calls are delegated to the content package object.
 * 
 * <P>
 * Initial Date: 04.07.2008 <br>
 * 
 * @author Sergio Trentini
 */
@Service("org.olat.ims.cp.CPManager")
public class CPManagerImpl implements CPManager {
	
	private static final Logger log = Tracing.createLoggerFor(CPManagerImpl.class);

	public static final String PACKAGE_CONFIG_FILE_NAME = "CPPackageConfig.xml";

	private static XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				CPPackageConfig.class, DeliveryOptions.class
			};
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("packageConfig", CPPackageConfig.class);
		configXstream.alias("deliveryOptions", DeliveryOptions.class);
	}

	@Override
	public CPPackageConfig getCPPackageConfig(OLATResourceable ores) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(ores);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		
		CPPackageConfig config;
		if(configXml.exists()) {
			config = (CPPackageConfig)configXstream.fromXML(configXml);
		} else {
			//set default config
			config = new CPPackageConfig();
			config.setDeliveryOptions(DeliveryOptions.defaultWithGlossary());
			setCPPackageConfig(ores, config);
		}
		return config;
	}

	@Override
	public void setCPPackageConfig(OLATResourceable ores, CPPackageConfig config) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(ores);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(config == null) {
			FileUtils.deleteFile(configXml);
		} else {
			try(OutputStream out = new FileOutputStream(configXml)) {
				configXstream.toXML(config, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	public ContentPackage load(VFSContainer directory, OLATResourceable ores) {
		ContentPackage cp;
		VFSItem file = directory.resolve("imsmanifest.xml");
		if (file instanceof VFSLeaf) {
			try(InputStream in = ((VFSLeaf)file).getInputStream()) {
				XMLParser parser = new XMLParser();
				DefaultDocument doc = (DefaultDocument) parser.parse(in, false);
				cp = new ContentPackage(doc, directory, ores);
				// If a wiki is imported or a new cp created, set a unique orga
				// identifier.
				if (cp.getLastError() == null && cp.isOLATContentPackage()
						&& CPCore.OLAT_ORGANIZATION_IDENTIFIER.equals(cp.getFirstOrganizationInManifest().getIdentifier())) {
					setUniqueOrgaIdentifier(cp);
				}
			} catch (IOException | OLATRuntimeException e) {
				cp = new ContentPackage(null, directory, ores);
				log.error("Reading imsmanifest failed. Dir: " + directory.getName() + ". Ores: " + ores.getResourceableId(), e);
				cp.setLastError("Exception reading XML for IMS CP: invalid xml-file ( " + directory.getName() + ")");
			}
		} else {
			cp = new ContentPackage(null, directory, ores);
			cp.setLastError("Exception reading XML for IMS CP: IMS-Manifest not found in " + directory.getName());
			log.error("IMS manifiest xml couldn't be found in dir {}. Ores: {}",  directory.getName(), ores.getResourceableId());
			throw new OLATRuntimeException(CPManagerImpl.class, "The imsmanifest.xml file was not found.", new IOException());
		}
		return cp;
	}

	@Override
	public ContentPackage createNewCP(OLATResourceable ores, String initalPageTitle) {
		// copy template cp to new repo-location
		if (copyTemplCP(ores)) {
			File cpRoot = FileResourceManager.getInstance().unzipFileResource(ores);
			if(log.isDebugEnabled()) {
				log.debug("createNewCP: cpRoot={}", cpRoot);
				log.debug("createNewCP: cpRoot.getAbsolutePath()={}", cpRoot.getAbsolutePath());
			}
			
			LocalFolderImpl vfsWrapper = new LocalFolderImpl(cpRoot);
			ContentPackage cp = load(vfsWrapper, ores);

			// Modify the copy of the template to get a unique identifier
			CPOrganization orga = setUniqueOrgaIdentifier(cp);
			setOrgaTitleToRepoEntryTitle(ores, orga);
			// Also set the translated title of the inital page.
			orga.getItems().get(0).setTitle(initalPageTitle);

			writeToFile(cp);
			
			//set the default settings for file delivery
			DeliveryOptions defOptions = DeliveryOptions.defaultWithGlossary();
			CPPackageConfig config = new CPPackageConfig();
			config.setDeliveryOptions(defOptions);
			setCPPackageConfig(ores, config);

			return cp;

		} else {
			log.error("CP couldn't be created. Error when copying template. Ores: {}", ores.getResourceableId());
			throw new OLATRuntimeException("ERROR while creating new empty cp. an error occured while trying to copy template CP", null);
		}
	}

	/**
	 * Sets the organization title to the title of the repository entry.
	 * 
	 * @param ores
	 * @param orga
	 */
	private void setOrgaTitleToRepoEntryTitle(OLATResourceable ores, CPOrganization orga) {
		// Set the title of the organization to the title of the resource.
		RepositoryManager resMgr = RepositoryManager.getInstance();
		RepositoryEntry cpEntry = resMgr.lookupRepositoryEntry(ores, false);
		if (cpEntry != null) {
			String title = cpEntry.getDisplayname();
			orga.setTitle(title);
		}
	}

	/**
	 * Assigns the organization a unique identifier in order to prevent any
	 * caching issues in the extjs menu tree later.
	 * 
	 * @param cp
	 * @return The first organization of the content package.
	 */
	private CPOrganization setUniqueOrgaIdentifier(ContentPackage cp) {
		CPOrganization orga = cp.getFirstOrganizationInManifest();
		String newOrgaIdentifier = "olatcp-" + CodeHelper.getForeverUniqueID();
		orga.setIdentifier(newOrgaIdentifier);
		return orga;
	}

	@Override
	public boolean isSingleUsedResource(CPResource res, ContentPackage cp) {
		return cp.isSingleUsedResource(res);
	}

	@Override
	public String addBlankPage(ContentPackage cp, String title) {
		return cp.addBlankPage(title);
	}

	@Override
	public String addBlankPage(ContentPackage cp, String title, String parentNodeID) {
		return cp.addBlankPage(parentNodeID, title);
	}

	@Override
	public void updatePage(ContentPackage cp, CPPage page) {
		cp.updatePage(page);
	}

	@Override
	public boolean addElement(ContentPackage cp, DefaultElement newElement) {
		return cp.addElement(newElement);

	}

	@Override
	public boolean addElement(ContentPackage cp, DefaultElement newElement, String parentIdentifier, int position) {
		return cp.addElement(newElement, parentIdentifier, position);
	}

	@Override
	public boolean addElementAfter(ContentPackage cp, DefaultElement newElement, String identifier) {
		return cp.addElementAfter(newElement, identifier);
	}

	@Override
	public void removeElement(ContentPackage cp, String identifier, boolean deleteResource) {
		cp.removeElement(identifier, deleteResource);
	}

	@Override
	public void moveElement(ContentPackage cp, String nodeID, String newParentID, int position) {
		cp.moveElement(nodeID, newParentID, position);
	}

	@Override
	public String copyElement(ContentPackage cp, String sourceID) {
		return cp.copyElement(sourceID, sourceID);
	}

	@Override
	public DefaultDocument getDocument(ContentPackage cp) {
		return cp.getDocument();
	}

	@Override
	public String getItemTitle(ContentPackage cp, String itemID) {
		return cp.getItemTitle(itemID);
	}

	@Override
	public DefaultElement getElementByIdentifier(ContentPackage cp, String identifier) {
		return cp.getElementByIdentifier(identifier);
	}

	@Override
	public CPTreeDataModel getTreeDataModel(ContentPackage cp) {
		return cp.buildTreeDataModel();
	}

	@Override
	public CPOrganization getFirstOrganizationInManifest(ContentPackage cp) {
		return cp.getFirstOrganizationInManifest();
	}

	@Override
	public CPPage getFirstPageToDisplay(ContentPackage cp) {
		return cp.getFirstPageToDisplay();
	}

	@Override
	public void writeToFile(ContentPackage cp) {
		cp.writeToFile();
	}

	@Override
	public String getPageByItemId(ContentPackage cp, String itemIdentifier) {
		return cp.getPageByItemId(itemIdentifier);
	}

	/**
	 * copies the default,empty, cp template to the new ores-directory
	 * 
	 * @param ores
	 * @return
	 */
	private boolean copyTemplCP(OLATResourceable ores) {
		File root = FileResourceManager.getInstance().getFileResourceRoot(ores);

		String packageName = ContentPackage.class.getCanonicalName();
		String path = packageName.replace('.', '/');
		path = path.replace("/ContentPackage", "/_resources/imscp.zip");

		path = VFSManager.sanitizePath(path);
		URL url = this.getClass().getResource(path);
		try {
			File f = new File(url.toURI());
			if (f.exists() && root.exists()) {
				FileUtils.copyFileToDir(f, root, "copy imscp template");
			} else {
				log.error("cp template was not copied. Source:  {} Target: {}", url, root.getAbsolutePath());
			}
		} catch (URISyntaxException e) {
			log.error("Bad url syntax when copying cp template. url: {} Ores: {}", url, ores.getResourceableId());
			return false;
		}

		return true;
	}
}
