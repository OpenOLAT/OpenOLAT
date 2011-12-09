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
package org.olat.core.util.vfs.version;

import java.io.IOException;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.configuration.PersistedPropertiesChangedEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Description:<br>
 * A very simple configuration bean for versioning. There is a default value for
 * the maximum allowed number of revisions, this number can be overridden by an
 * second value saved in the persisted properties.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class SimpleVersionConfig implements GenericEventListener, FolderVersioningConfigurator {

	private static final String MAX_NUMBER_OF_VERSIONS = "maxnumber.versions";
	private static final String COURSE_PATH = "/course/";
	private static final String SOLUTIONS_PATH = "/solutions/";
	private static final String RETURNBOXES_PATH = "/returnboxes/";
	private static final String DROPBOXES = "/dropboxes/";
	private static final String TASKFOLDERS = "/taskfolders/";
	
	private static final String[] EXCLUSIONS_IN_COURSE_PATH = {SOLUTIONS_PATH, RETURNBOXES_PATH, DROPBOXES, TASKFOLDERS};

	private Long maxNumOfVersions;
	private int maxNumberOfVersionsConfig = -1;
	private PersistedProperties persistedProperties;

	private String courseRoot;

	
	/**
	 * [used by spring]
	 */
	private SimpleVersionConfig() {
		//
	}

	public void event(Event event) {
		if (event instanceof PersistedPropertiesChangedEvent) {
			// Reload the properties
			if (!((PersistedPropertiesChangedEvent)event).isEventOnThisNode()) {
				persistedProperties.loadPropertiesFromFile();
			}
			maxNumOfVersions = null;
		}
	}
	
	public void setCoordinator(CoordinatorManager coordinatorManager) {
		//nothing to do
	}
	
	/**
	 * [used by spirng]
	 * @param persistedProperties
	 */
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.persistedProperties = persistedProperties;
	}

	/**
	 * @return default maximum number of versions, defined in xml file
	 */
	public Integer getDefaultMaxNumberOfVersions() {
		return maxNumberOfVersionsConfig;
	}

	public void setDefaultMaxNumberOfVersions(Integer maxNumberOfVersionsConfig) {
		this.maxNumberOfVersionsConfig = maxNumberOfVersionsConfig;
	}

	/**
	 * @return maximum number of revisions, defined in admin. of Olat
	 */
	public Long getMaxNumberOfVersionsProperty() {
		if(maxNumOfVersions != null) {
			return maxNumOfVersions;
		}
		
		if (getPersistedProperties() == null) {
			return null;
		}
		String maxNumOfVersionsStr = getPersistedProperties().getStringPropertyValue(MAX_NUMBER_OF_VERSIONS, true);
		if (maxNumOfVersionsStr == null || maxNumOfVersionsStr.length() == 0) {
			return null;
		}
		maxNumOfVersions = Long.parseLong(maxNumOfVersionsStr);
		return maxNumOfVersions;
	}

	public void setMaxNumberOfVersionsProperty(Long maxNumber) {
		if (getPersistedProperties() != null) {
			getPersistedProperties().setStringProperty(MAX_NUMBER_OF_VERSIONS, maxNumber.toString(), true);
		}
	}

	public int versionAllowed(String relPath) {
		return getVersionAllowed() ;
	}

	public boolean versionEnabled(VFSContainer container) {
		if (container instanceof NamedContainerImpl) {
			container = ((NamedContainerImpl) container).getDelegate();
		}
		if (container instanceof MergeSource) {
			container = ((MergeSource)container).getRootWriteContainer();
		}
		
		if (container instanceof LocalFolderImpl) {
			try {
				LocalFolderImpl folderImpl = (LocalFolderImpl)container;
				String path = folderImpl.getBasefile().getCanonicalPath();
				String root = getCourseRoot();
				if (path.startsWith(root)) {
					for(String exclusion:EXCLUSIONS_IN_COURSE_PATH) {
						if(path.indexOf(exclusion) > 0) {
							return false;
						}
					}
				}
				return getVersionAllowed() != 0;
			} catch (IOException e) {
				//fail silently;
			}
		}
		return false;
	}
	
	private int getVersionAllowed() {
		Long max = getMaxNumberOfVersionsProperty();
		if (max != null) {
			return max.intValue();
		}
		Integer configMax = getDefaultMaxNumberOfVersions();
		if (configMax != null) {
			return configMax.intValue();
		}
		return 0;
	}

	/**
	 * 
	 * @return the persisted properties
	 */
	private PersistedProperties getPersistedProperties() {
		return persistedProperties;
	}
	
	private String getCourseRoot() {
		if(courseRoot == null) {
			courseRoot = FolderConfig.getCanonicalRoot();
			courseRoot += COURSE_PATH;
		}
		return courseRoot;
	}
}
