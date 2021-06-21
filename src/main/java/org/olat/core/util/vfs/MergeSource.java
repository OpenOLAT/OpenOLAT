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

package org.olat.core.util.vfs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Initial Date: 23.06.2005 <br>
 * @author Felix Jost
 */
public class MergeSource extends AbstractVirtualContainer {

	private VFSContainer parentContainer;
	private transient List<VFSContainer> mergedContainers;
	private transient List<VFSContainer> mergedContainersChildren;
	private VFSContainer rootWriteContainer;
	private VFSSecurityCallback securityCallback;

	/**
	 * 
	 */
	public MergeSource(VFSContainer parentContainer, String name) {
		super(name);
		this.parentContainer = parentContainer;
		mergedContainers = new ArrayList<>();
		mergedContainersChildren = new ArrayList<>();
	}
	
	protected void init() {
		if(mergedContainers == null) {
			mergedContainers = new ArrayList<>();
		}
		if(mergedContainersChildren == null) {
			mergedContainersChildren = new ArrayList<>(2);
		}
	}
	
	protected void setMergedContainers(List<VFSContainer> mergedContainers) {
		this.mergedContainers = mergedContainers;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getRelPath() {
		if(rootWriteContainer != null) {
			return rootWriteContainer.getRelPath();
		}
		return null;
	}

	/**
	 * Add container to this merge source. container will show up as its name as a childe of MergeSource.
	 * 
	 * @param container
	 */
	public void addContainer(VFSContainer container) {
		addContainerToList(container, mergedContainers);
	}
	
	public void addContainerToList(VFSContainer container, List<VFSContainer> containers) {
		VFSContainer newContainer = container;
		if (isContainerNameTaken(newContainer.getName(), containers)) {
			String newName = newContainer.getName() + "_" + CodeHelper.getRAMUniqueID();
			newContainer = new NamedContainerImpl(newName, container);
		}
		// set default filter if container does not already have its own default filter
		if (container.getDefaultItemFilter() != null) {
			container.setDefaultItemFilter(defaultFilter);
			newContainer.setDefaultItemFilter(defaultFilter);
		}
		newContainer.setParentContainer(this);
		containers.add(newContainer);
	}
	
	/**
	 * Add all children of this container to the root of this MergeSource.
	 * 
	 * @param container
	 * @param enableWrite If true, writes to the root of this MergeSource are directed to this container.
	 */
	public void addContainersChildren(VFSContainer container, boolean enableWrite) {
		container.setParentContainer(this);
		// set default filter if container does not already have its own default filter
		if (container.getDefaultItemFilter() != null) {
			container.setDefaultItemFilter(defaultFilter);
		}
		// add the container to the list of merged sources
		mergedContainersChildren.add(container);
		if (enableWrite) rootWriteContainer = container;
	}

	/**
	 * Check if the given container is semantically not a child but a container
	 * which items have been merged using the addContainersChildren() method.
	 * 
	 * @param container
	 * @return
	 */
	public boolean isContainersChild(VFSContainer container) {
		return mergedContainersChildren.contains(container);
	}

	@Override
	public VFSContainer getParentContainer() {
		return parentContainer;
	}
	
	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	@Override
	public List<VFSItem> getItems() {
		return getItems(null);
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		// remember: security callback and parent was already set during add to this MergeSource
		// and refreshed on any setSecurityCallback() so no need to handle the quota of children here.
		List<VFSItem> all = new ArrayList<>();
		if (filter == null && defaultFilter == null) {
			all.addAll(mergedContainers);
		} else {
			// custom filter or default filter is set
			for (VFSContainer mergedContainer : mergedContainers) {
				boolean passedFilter = true;
				// check for default filter
				if (defaultFilter != null && ! defaultFilter.accept(mergedContainer)) passedFilter = false;
				// check for custom filter
				if (passedFilter && filter != null && ! filter.accept(mergedContainer)) passedFilter = false;
				// only add when both filters passed the test
				if (passedFilter)	all.add(mergedContainer);
			}
		}

		for (VFSContainer container: mergedContainersChildren) {
			all.addAll(container.getItems(filter));
		}
		return all;
	}

	@Override
	public VFSStatus canWrite() {
		if (rootWriteContainer == null) return VFSConstants.NO;
		return rootWriteContainer.canWrite();
	}

	@Override
	public VFSContainer createChildContainer(String name) {
		if (canWrite() != VFSConstants.YES) return null;
		VFSContainer newContainer = rootWriteContainer.createChildContainer(name);
		if (newContainer != null)	newContainer.setDefaultItemFilter(defaultFilter);
		return newContainer;
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
		if (canWrite() != VFSConstants.YES) return null;
		return rootWriteContainer.createChildLeaf(name);
	}

	@Override
	public boolean isInPath(String path) {
		return rootWriteContainer.isInPath(path);
	}

	@Override
	public VFSStatus copyFrom(VFSItem source) {
		if (canWrite() != VFSConstants.YES) {
			throw new AssertException("Cannot create child container in merge source if not writable.");
		}
		return rootWriteContainer.copyFrom(source);
	}

	@Override
	public VFSStatus copyContentOf(VFSContainer container) {
		if (canWrite() != VFSConstants.YES) {
			throw new AssertException("Cannot create child container in merge source if not writable.");
		}
		
		VFSStatus status = null;
		for(VFSItem item:container.getItems()) {
			status = rootWriteContainer.copyFrom(item);
		}
		return status;
	}

	@Override
	public VFSItem resolve(String path) {
		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) return this;
		
		String childName = VFSManager.extractChild(path);
		String nextPath = path.substring(childName.length() + 1);
		// simple case
		for (VFSContainer container:mergedContainers) {
			if (container.getName().equals(childName)) {
				VFSItem vfsItem = container.resolve(nextPath);
				// set default filter on resolved file if it is a container
				if (vfsItem instanceof VFSContainer) {
					VFSContainer resolvedContainer = (VFSContainer) vfsItem;
					resolvedContainer.setDefaultItemFilter(defaultFilter);
				}
				return vfsItem;
			}
		}
		
		// check delegates
		for (VFSContainer container:mergedContainers) {
			// A namedContainer doesn't match with its own getName()! -> work with delegate
			boolean nameMatch = container.getName().equals(childName);
			if (container instanceof NamedContainerImpl && !nameMatch) {
				// Special case: sometimes the path refers to the named containers
				// delegate container, so try this one as well
				container = ((NamedContainerImpl) container).getDelegate();
				if(container == null) {
					// in case a corrupted course
					continue;
				}
				String name = container.getName();
				if (name == null) {
					// FXOLAT-195 The delegate of the named container does not
					// have a name, so abort the special case and continue with
					// next container
					continue;
				}
				nameMatch = name.equals(childName);
			}
			if (nameMatch) {
				VFSItem vfsItem = container.resolve(nextPath);
				// set default filter on resolved file if it is a container
				if (vfsItem instanceof VFSContainer) {
					VFSContainer resolvedContainer = (VFSContainer) vfsItem;
					resolvedContainer.setDefaultItemFilter(defaultFilter);
				}
				return vfsItem;
			}
		}

		for (VFSContainer container : mergedContainersChildren) {
			// A namedContainer doesn't match with its own getName()! -> work with delegate
			if (container instanceof NamedContainerImpl) {
				container = ((NamedContainerImpl) container).getDelegate();
			}
			VFSItem vfsItem = container.resolve(path);
			if (vfsItem != null) {
				// set default filter on resolved file if it is a container
				if (vfsItem instanceof VFSContainer) {
					VFSContainer resolvedContainer = (VFSContainer) vfsItem;
					resolvedContainer.setDefaultItemFilter(defaultFilter);
				}
				return vfsItem;
			}
		}
		return null;
	}

	
	@Override
	public VFSStatus canMeta() {
		return rootWriteContainer == null ? VFSConstants.NO : rootWriteContainer.canVersion();
	}

	@Override
	public VFSStatus canVersion() {
		return rootWriteContainer == null ? VFSConstants.NO: rootWriteContainer.canVersion();
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return rootWriteContainer == null ? null : rootWriteContainer.getMetaInfo();
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return securityCallback;
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		securityCallback = secCallback;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if (rootWriteContainer == null) {
			// Unwriteable merge source (e.g. users private folder), compare on object identity 
			return this.equals(vfsItem);
		}
		if (vfsItem instanceof MergeSource) {
			// A writeable merge source, compare on writeable root container
			return rootWriteContainer.equals(((MergeSource)vfsItem).rootWriteContainer);
		}
		return rootWriteContainer.equals(vfsItem);
	}

	public VFSContainer getRootWriteContainer() {
		return rootWriteContainer;
	}
	
	private boolean isContainerNameTaken(String containerName, List<VFSContainer> containers) {
		for (Iterator<VFSContainer> iter = containers.iterator(); iter.hasNext();) {
			VFSContainer container = iter.next();
			if (container.getName().equals(containerName)) {
				return true;
			}
		}
		return false;
	}
}
