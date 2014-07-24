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
package org.olat.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.manager.WebDAVMergeSource;
import org.olat.core.commons.services.webdav.servlets.RequestUtil;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * This WebDAV provider delivery all folders in courses where the user
 * is owner or is editor via a right group.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class CoursefolderWebDAVMergeSource extends WebDAVMergeSource {	
	public CoursefolderWebDAVMergeSource(Identity identity) {
		super(identity);
	}
	
	@Override
	protected List<VFSContainer> loadMergedContainers() {
		RepositoryManager rm = RepositoryManager.getInstance();
		List<RepositoryEntry> courseEntries = rm.queryByEditor(getIdentity(), CourseModule.getCourseTypeName());
		List<VFSContainer> containers = new ArrayList<>();
		
		Map<String, VFSContainer> terms = null;
		VirtualContainer noTermContainer = null;
		
		WebDAVModule webDAVModule = CoreSpringFactory.getImpl(WebDAVModule.class);
		boolean useTerms = webDAVModule.isTermsFoldersEnabled();
		if (useTerms) {
			// prepare no-terms folder for all resources without semester term info or private date
			terms = new HashMap<String, VFSContainer>();
			noTermContainer = new VirtualContainer("other");
		}
		
		// Add all found repo entries to merge source
		for (RepositoryEntry re:courseEntries) {
			String courseTitle = RequestUtil.normalizeFilename(re.getDisplayname());
			NamedContainerImpl cfContainer = new CoursefolderWebDAVNamedContainer(courseTitle, re.getOlatResource());
			
			if (useTerms) {
				RepositoryEntryLifecycle lc = re.getLifecycle();
				if (lc != null && !lc.isPrivateCycle()) {
					// when a semester term info is found, add it to corresponding term folder
					String termSoftKey = lc.getSoftKey();
					VFSContainer termContainer = terms.get(termSoftKey);
					if (termContainer == null) {
						// folder for this semester term does not yet exist, create one and add to map
						String normalizedKey = RequestUtil.normalizeFilename(termSoftKey);
						termContainer = new VirtualContainer(normalizedKey);
						terms.put(termSoftKey, termContainer);
						addContainerToList(termContainer, containers);
					}
					termContainer.getItems().add(cfContainer);
				} else {
					// no semester term found, add to no-term folder
					noTermContainer.getItems().add(cfContainer);
				
				}
			} else {
				addContainerToList(cfContainer, containers);				
			}
		}

		if (useTerms) {
			// add no-terms folder if any have been found
			if (noTermContainer.getItems().size() > 0) {
				addContainerToList(noTermContainer, containers);
			}
		}

		return containers;
	}
}