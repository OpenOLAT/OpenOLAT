/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.docpool;

import java.io.File;

import org.olat.NewControllerFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.docpool.site.DocumentPoolContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocumentPoolModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String DIRECTORY = "docpool";
	public static final String INFOS_PAGE_DIRECTORY = "infospage";
	
	private static final String DOCUMENT_POOL_ENABLED = "docpool.enabled";
	private static final String TAXONOMY_TREE_KEY = "taxonomy.tree.key";
	private static final String WEBDAV_MOUNT_POINT = "docpool.webdav.mountpoint";
	private static final String TEMPLATES_DIRECTORY_ENABLED = "docpool.templates.directory.enabled";
	
	@Value("${docpool.enabled:true}")
	private boolean enabled;
	private String taxonomyTreeKey;
	@Value("${docpool.webdav.mountpoint:doc-pool}")
	private String webdavMountPoint;
	@Value("${docpool.templates.directory.enabled:true}")
	private boolean templatesDirectoryEnabled;
	
	@Autowired
	public DocumentPoolModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator("DocumentPoolSite",
				new DocumentPoolContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("DocumentPool",
				new DocumentPoolContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("DocPool",
				new DocumentPoolContextEntryControllerCreator());

		updateProperties();
		
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		File rootDirectory = new File(bcrootDirectory, DIRECTORY);
		File infosPageDirectory = new File(rootDirectory, INFOS_PAGE_DIRECTORY);
		if(!infosPageDirectory.exists()) {
			infosPageDirectory.mkdirs();
		}
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(DOCUMENT_POOL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String taxonomyTreeKeyObj = getStringPropertyValue(TAXONOMY_TREE_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyTreeKeyObj)) {
			taxonomyTreeKey = taxonomyTreeKeyObj;
		}
		
		String mountPointObj = getStringPropertyValue(WEBDAV_MOUNT_POINT, true);
		if(StringHelper.containsNonWhitespace(mountPointObj)) {
			webdavMountPoint = mountPointObj;
		}
		
		String templatesDirectoryEnabledObj = getStringPropertyValue(TEMPLATES_DIRECTORY_ENABLED, true);
		if(StringHelper.containsNonWhitespace(templatesDirectoryEnabledObj)) {
			templatesDirectoryEnabled = "true".equals(templatesDirectoryEnabledObj);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(DOCUMENT_POOL_ENABLED, Boolean.toString(enabled), true);
	}

	public String getTaxonomyTreeKey() {
		return taxonomyTreeKey;
	}

	public void setTaxonomyTreeKey(String taxonomyTreeKey) {
		this.taxonomyTreeKey = taxonomyTreeKey;
		setStringProperty(TAXONOMY_TREE_KEY, taxonomyTreeKey, true);
	}
	
	public String getWebDAVMountPoint() {
		return webdavMountPoint;
	}
	
	public void setWebDAVMountPoint(String mountPoint) {
		this.webdavMountPoint = mountPoint;
		setStringProperty(WEBDAV_MOUNT_POINT, mountPoint, true);
	}

	public boolean isTemplatesDirectoryEnabled() {
		return templatesDirectoryEnabled;
	}

	public void setTemplatesDirectoryEnabled(boolean enabled) {
		this.templatesDirectoryEnabled = enabled;
		setStringProperty(TEMPLATES_DIRECTORY_ENABLED, Boolean.toString(enabled), true);
	}
	
	public VFSContainer getInfoPageContainer() {
		String path = "/" + DIRECTORY + "/" + INFOS_PAGE_DIRECTORY;
		return VFSManager.olatRootContainer(path, null);
	}
}
